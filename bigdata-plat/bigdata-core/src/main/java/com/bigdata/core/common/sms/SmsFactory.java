package com.bigdata.core.common.sms;

import com.bigdata.core.common.constant.SettingConstant;
import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.core.entity.Setting;
import com.bigdata.core.service.SettingService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 工厂模式
 * @author Bigdata
 */
@Component
public class SmsFactory {

    @Autowired
    private SettingService settingService;

    @Autowired
    private AliSms aliSms;

    @Autowired
    private TencentSms tencentSms;

    public Sms getSms() {

        Setting setting = settingService.get(SettingConstant.SMS_USED);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            throw new BigdataException("您还未配置OSS存储服务");
        }
        String type = setting.getValue();
        if (type.equals(SettingConstant.ALI_SMS)) {
            return aliSms;
        } else if (type.equals(SettingConstant.TENCENT_SMS)) {
            return tencentSms;
        } else {
            throw new BigdataException("暂不支持该存储配置，请检查配置");
        }
    }
}
