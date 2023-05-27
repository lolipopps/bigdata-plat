package com.bigdata.file.controller;

import com.bigdata.core.common.constant.OssConstant;
import com.bigdata.core.common.constant.SettingConstant;
import com.bigdata.core.common.exception.LimitException;
import com.bigdata.core.common.limit.RedisRaterLimiter;
import com.bigdata.core.common.utils.Base64DecodeMultipartFile;
import com.bigdata.core.common.utils.CommonUtil;
import com.bigdata.core.common.utils.IpInfoUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.entity.Setting;
import com.bigdata.core.service.SettingService;
import com.bigdata.core.vo.OssSetting;
import com.bigdata.file.entity.File;
import com.bigdata.file.manage.FileManageFactory;
import com.bigdata.file.service.FileService;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "文件上传接口")
@RequestMapping("/bigdata/upload")
@Transactional
public class UploadController {

    @Value("${bigdata.maxUploadFile}")
    private Long maxUploadFile;

    @Autowired
    private RedisRaterLimiter redisRaterLimiter;

    @Autowired
    private IpInfoUtil ipInfoUtil;

    @Autowired
    private FileManageFactory fileManageFactory;

    @Autowired
    private SettingService settingService;

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ApiOperation(value = "文件上传")
    public Result upload(@RequestParam(required = false) MultipartFile file,
                                 @RequestParam(required = false) String base64,
                                 @RequestParam(required = false, defaultValue = "0") String categoryId,
                                 HttpServletRequest request) {

        if (file == null && StrUtil.isBlank(base64)) {
            return ResultUtil.error("缺少文件参数");
        }

        if (file != null && file.getSize() > maxUploadFile * 1024 * 1024) {
            return ResultUtil.error("文件大小过大，不能超过" + maxUploadFile + "MB");
        }

        Setting setting = settingService.get(SettingConstant.OSS_USED);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return ResultUtil.error(501, "您还未配置OSS存储服务");
        }

        // IP限流 在线Demo所需 5分钟限1个请求
        Boolean token = redisRaterLimiter.acquireByRedis("upload:" + ipInfoUtil.getIpAddr(request), 1L, 300000L);
        if (!token) {
            throw new LimitException("上传那么多干嘛，等等再传吧");
        }

        if (StrUtil.isNotBlank(base64)) {
            // base64上传
            file = Base64DecodeMultipartFile.base64Convert(base64);
        }
        String result = "";
        String fKey = CommonUtil.renamePic(file.getOriginalFilename());
        File f = new File();
        try {
            InputStream inputStream = file.getInputStream();
            // 上传至第三方云服务或服务器
            result = fileManageFactory.getFileManage(null).inputStreamUpload(inputStream, fKey, file);
            // 保存数据信息至数据库
            f.setLocation(getType(setting.getValue())).setTitle(file.getOriginalFilename()).setSize(file.getSize())
                    .setType(file.getContentType()).setFKey(fKey).setUrl(result).setCategoryId(categoryId);
            fileService.save(f);
        } catch (Exception e) {
            log.error(e.toString());
            return ResultUtil.error(e.toString());
        }
        if (setting.getValue().equals(SettingConstant.LOCAL_OSS)) {
            OssSetting os = new Gson().fromJson(settingService.get(SettingConstant.LOCAL_OSS).getValue(), OssSetting.class);
            result = os.getHttp() + os.getEndpoint() + "/" + f.getId();
        }
        return ResultUtil.data(result);
    }

    public Integer getType(String type) {
        switch (type) {
            case SettingConstant.QINIU_OSS:
                return OssConstant.OSS_QINIU;
            case SettingConstant.ALI_OSS:
                return OssConstant.OSS_ALI;
            case SettingConstant.TENCENT_OSS:
                return OssConstant.OSS_TENCENT;
            case SettingConstant.MINIO_OSS:
                return OssConstant.OSS_MINIO;
            case SettingConstant.LOCAL_OSS:
                return OssConstant.OSS_LOCAL;
            default:
                return -1;
        }
    }
}

