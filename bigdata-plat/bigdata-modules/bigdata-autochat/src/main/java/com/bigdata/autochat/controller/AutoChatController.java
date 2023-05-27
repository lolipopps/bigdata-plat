package com.bigdata.autochat.controller;

import com.bigdata.autochat.dao.mapper.AutoChatMapper;
import com.bigdata.autochat.entity.AutoChat;
import com.bigdata.autochat.service.AutoChatService;
import com.bigdata.autochat.vo.AssociateVo;
import com.bigdata.autochat.vo.GuessVo;
import com.bigdata.autochat.vo.MessageVo;
import com.bigdata.core.base.BigdataBaseController;
import com.bigdata.core.common.constant.SettingConstant;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.core.entity.Setting;
import com.bigdata.core.service.SettingService;
import com.bigdata.core.vo.AutoChatSetting;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "问答助手客服管理接口")
@RequestMapping("/bigdata/autoChat")
@Transactional
public class AutoChatController extends BigdataBaseController<AutoChat, String> {

    @Autowired
    private AutoChatService autoChatService;

    @Autowired
    private AutoChatMapper autoChatMapper;

    @Autowired
    private SettingService settingService;

    @Override
    public AutoChatService getService() {
        return autoChatService;
    }

    public AutoChatSetting getChatSetting() {

        Setting setting = settingService.get(SettingConstant.CHAT_SETTING);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            return null;
        }
        return new Gson().fromJson(setting.getValue(), AutoChatSetting.class);
    }

    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result<Page<AutoChat>> getByCondition(AutoChat autoChat, SearchVo searchVo, PageVo pageVo) {

        Page<AutoChat> page = autoChatService.findByCondition(autoChat, searchVo, PageUtil.initPage(pageVo));
        page.forEach(e -> {
            if (StrUtil.isNotBlank(e.getContent())) {
                e.setContentText(HtmlUtil.cleanHtmlTag(e.getContent()));
            }
        });
        return new ResultUtil<Page<AutoChat>>().setData(page);
    }

    @RequestMapping(value = "/ask", method = RequestMethod.GET)
    @ApiOperation(value = "问答接口")
    public Object ask(@ApiParam("问题") @RequestParam String q,
                      @ApiParam("最大回答数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        // 先直接匹配
        AutoChat autoChat = autoChatService.findByTitle(q);
        if (autoChat != null) {
            return new MessageVo().card(autoChat.getId(), autoChat.getContent(), autoChat.getEvaluable());
        }
        // 全文索引
        List<AutoChat> list = autoChatMapper.find(q, pageSize);
        if (list == null || list.size() == 0) {
            AutoChatSetting chatSetting = getChatSetting();
            String content = "暂未没找到相匹配的回答";
            if (chatSetting != null && StrUtil.isNotBlank(chatSetting.getNoDataReply())) {
                content = chatSetting.getNoDataReply();
            }
            return new MessageVo().card(content, false);
        } else if (list.size() == 1) {
            autoChat = list.get(0);
            return new MessageVo().card(autoChat.getId(), autoChat.getContent(), autoChat.getEvaluable());
        } else {
            List<MessageVo> result = new ArrayList<>();
            // 第一条回答
            autoChat = list.get(0);
            MessageVo answer = new MessageVo().card(autoChat.getId(), autoChat.getContent(), autoChat.getEvaluable());
            result.add(answer);
            list.remove(0);
            // 其余放猜你想问列表 2-4条
            if (list.size() == 1) {
                list.add(autoChat);
            } else if (list.size() > 4) {
                list = list.subList(0, 4);
            }
            list.forEach(e->{
                e.setContent(e.getTitle());
            });
            List<GuessVo> promotions = new ArrayList<>();
            promotions.add(new GuessVo().setList(list));
            MessageVo promotion = new MessageVo().promotion(promotions);
            result.add(promotion);

            return result;
        }
    }

    @RequestMapping(value = "/associate", method = RequestMethod.GET)
    @ApiOperation(value = "联想搜索")
    public AssociateVo associate(@ApiParam("问题") @RequestParam String q,
                                 @ApiParam("最大回答数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        List<AutoChat> list = autoChatMapper.find(q, pageSize);
        return new AssociateVo().setKeyword(q).setList(list);
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.GET)
    @ApiOperation(value = "赞踩")
    public Result evaluate(String messageId, String evaluateType) {

        AutoChat autoChat = autoChatService.get(messageId);
        if ("good".equals(evaluateType)) {
            autoChat.setGood(autoChat.getGood() + 1);
        } else {
            autoChat.setBad(autoChat.getBad() + 1);
        }
        autoChatService.update(autoChat);
        return ResultUtil.success("操作成功");
    }
}