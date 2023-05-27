package com.bigdata.activity.utils;

import com.bigdata.activity.dao.ActProcessDao;
import com.bigdata.activity.entity.ActBusiness;
import com.bigdata.activity.entity.ActProcess;
import com.bigdata.activity.vo.EmailMessage;
import com.bigdata.core.common.constant.SettingConstant;
import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.core.common.sms.SmsUtil;
import com.bigdata.core.common.utils.EmailUtil;
import com.bigdata.core.entity.Message;
import com.bigdata.core.entity.Setting;
import com.bigdata.core.entity.User;
import com.bigdata.core.service.MessageSendService;
import com.bigdata.core.service.SettingService;
import com.bigdata.core.service.UserService;
import com.bigdata.core.vo.OtherSetting;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bigdata
 */
@Transactional
@Component
@Slf4j
public class MessageUtil {

    @Autowired
    private SmsUtil smsUtil;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSendService messageSendService;

    @Autowired
    private ActProcessDao actProcessDao;

    @Autowired
    private SettingService settingService;

    public OtherSetting getOtherSetting() {

        Setting setting = settingService.get(SettingConstant.OTHER_SETTING);
        if (StrUtil.isBlank(setting.getValue())) {
            throw new BigdataException("系统未配置访问域名");
        }
        return new Gson().fromJson(setting.getValue(), OtherSetting.class);
    }

    /**
     * 发送工作流消息
     * @param userId      发送用户
     * @param messageId   消息ID
     * @param actBusiness 业务关联表
     * @param smsMessage  短信消息
     * @param sendMessage 是否发站内信息
     * @param sendSms     是否发短信
     * @param sendEmail   是否发邮件
     */
    @Async
    public void sendActMessage(String userId, String messageId, ActBusiness actBusiness, String smsMessage,
                               Boolean sendMessage, Boolean sendSms, Boolean sendEmail) {

        User user = userService.get(userId);

        Map<String, String> params = new HashMap<>();
        params.put("nickname", user.getNickname());
        if (StrUtil.isNotBlank(actBusiness.getUserId())) {
            User applyer = userService.get(actBusiness.getUserId());
            if (applyer != null) {
                params.put("applyer", applyer.getNickname());
            }
        }
        ActProcess actProcess = actProcessDao.getById(actBusiness.getProcDefId());
        params.put("processName", actProcess.getName());

        // 站内消息
        if (sendMessage) {
            messageSendService.sendTemplateMessage(userId, messageId, params);
        }

        // 短信消息
        if (StrUtil.isNotBlank(user.getMobile()) && sendSms) {
            try {
                smsUtil.sendActMessage(user.getMobile(), smsMessage);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }

        // 邮件消息
        Message message = messageSendService.getTemplateMessage(messageId, params);
        // 填充模版消息 邮箱消息使用
        String title = message.getTitle(), content = HtmlUtil.cleanHtmlTag(message.getContent());
        if (StrUtil.isNotBlank(user.getEmail()) && sendEmail) {
            EmailMessage e = new EmailMessage().setContent(content).setFullUrl(getOtherSetting().getDomain());
            try {
                emailUtil.sendTemplateEmail(user.getEmail(), "【Bigdata】" + title, "act-message-email", e);
            } catch (Exception ex) {
                log.error(ex.toString());
            }
        }
    }
}
