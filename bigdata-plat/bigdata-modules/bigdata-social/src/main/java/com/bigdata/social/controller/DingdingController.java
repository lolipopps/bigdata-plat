package com.bigdata.social.controller;

import com.bigdata.core.common.annotation.SystemLog;
import com.bigdata.core.common.constant.SecurityConstant;
import com.bigdata.core.common.constant.SocialConstant;
import com.bigdata.core.common.enums.LogType;
import com.bigdata.core.common.redis.RedisTemplateHelper;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.utils.SecurityUtil;
import com.bigdata.core.common.vo.Result;
import com.bigdata.social.entity.Social;
import com.bigdata.social.service.SocialService;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * https://ding-doc.dingtalk.com/doc#/serverapi2/kymkv6
 * @author Bigdata
 */
@Slf4j
@Api(tags = "钉钉登录接口")
@RequestMapping("/bigdata/social/dingding")
@Controller
public class DingdingController {

    @Value("${bigdata.social.dingding.appId}")
    private String appId;

    @Value("${bigdata.social.dingding.appSecret}")
    private String appSecret;

    @Value("${bigdata.social.dingding.callbackUrl}")
    private String callbackUrl;

    @Value("${bigdata.social.callbackFeUrl}")
    private String callbackFeUrl;

    @Value("${bigdata.social.callbackFeRelateUrl}")
    private String callbackFeRelateUrl;

    private static final String STATE = SecurityConstant.DINGDING_STATE;

    private static final Integer TYPE = SocialConstant.SOCIAL_TYPE_DINGDING;

    @Autowired
    private SocialService socialService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RedisTemplateHelper redisTemplate;

    /**
     * 申请令牌地址
     */
    private static final String AUTHORIZE_URL = "https://oapi.dingtalk.com/connect/qrconnect";

    /**
     * 获取用户信息地址
     */
    private static final String GET_USERINFO_URL = "https://oapi.dingtalk.com/sns/getuserinfo_bycode";

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ApiOperation(value = "获取企业微信认证链接")
    @ResponseBody
    public Result login() {

        // 生成并保存state 忽略该参数有可能导致CSRF攻击
        String state = String.valueOf(System.currentTimeMillis());
        redisTemplate.set(STATE + state, "VALID", 3L, TimeUnit.MINUTES);

        // 传递参数appid、response_type、scope、state、redirect_uri
        String url = AUTHORIZE_URL + "?appid=" + appId + "&response_type=code&scope=snsapi_login&state=" + state
                + "&redirect_uri=" + callbackUrl;

        return ResultUtil.data(url);
    }

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    @ApiOperation(value = "获取accessToken")
    @SystemLog(description = "钉钉关联登录", type = LogType.LOGIN)
    public String callback(@RequestParam(required = false) String code,
                           @RequestParam(required = false) String state) throws UnsupportedEncodingException {

        if (StrUtil.isBlank(code)) {
            return "redirect:" + callbackFeUrl + "?error=" + URLEncoder.encode("您未同意授权", "utf-8");
        }
        // 验证state
        String v = redisTemplate.get(STATE + state);
        redisTemplate.delete(STATE + state);
        if (StrUtil.isBlank(v)) {
            return "redirect:" + callbackFeUrl + "?error=" + URLEncoder.encode("授权超时或state不正确", "utf-8");
        }

        // 获取User信息
        Long timestamp = System.currentTimeMillis();
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, appSecret.getBytes("UTF-8"));
        String signature = new String(Base64.encodeBase64(mac.digest(timestamp.toString())));
        signature = URLEncoder.encode(signature, "UTF-8");
        String userUrl = GET_USERINFO_URL + "?accessKey=" + appId + "&timestamp=" + timestamp + "&signature=" + signature;

        String result = HttpUtil.post(userUrl, "{\"tmp_auth_code\": \"" + code + "\"}");
        if (!result.contains("user_info")) {
            return "redirect:" + callbackFeUrl + "?error=" + URLEncoder.encode("获取用户信息失败", "utf-8");
        }

        // 获取unionid
        JsonObject userJson = JsonParser.parseString(result).getAsJsonObject().get("user_info").getAsJsonObject();
        String nick = userJson.get("nick").getAsString();
        String openid = userJson.get("openid").getAsString();

        // 存入数据库
        Social dingding = socialService.findByOpenIdAndPlatform(openid, TYPE);
        if (dingding == null) {
            Social newding = new Social().setOpenId(openid).setUsername(nick).setPlatform(TYPE);
            dingding = socialService.save(newding);
        }

        String url = "";
        // 判断是否绑定账号
        if (StrUtil.isNotBlank(dingding.getRelateUsername())) {
            // 已绑定 直接登录
            String JWT = securityUtil.getToken(dingding.getRelateUsername(), true);
            // 存入redis
            String JWTKey = IdUtil.simpleUUID();
            redisTemplate.set(JWTKey, JWT, 2L, TimeUnit.MINUTES);
            url = callbackFeUrl + "?related=1&JWTKey=" + JWTKey;
        } else {
            // 未绑定 Redis存入id
            String idToken = IdUtil.simpleUUID();
            redisTemplate.set(idToken, dingding.getId(), 5L, TimeUnit.MINUTES);
            url = callbackFeRelateUrl + "?socialType=" + TYPE + "&id=" + idToken;
        }
        return "redirect:" + url;
    }
}
