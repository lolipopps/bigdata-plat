package com.bigdata.file.manage;

import com.bigdata.core.common.constant.OssConstant;
import com.bigdata.core.common.constant.SettingConstant;
import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.core.entity.Setting;
import com.bigdata.core.service.SettingService;
import com.bigdata.file.manage.impl.*;
import cn.hutool.core.util.StrUtil;
import com.bigdata.file.manage.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 工厂模式
 * @author Bigdata
 */
@Component
public class FileManageFactory {

    @Autowired
    private SettingService settingService;

    @Autowired
    private QiniuFileManage qiniuFileManage;

    @Autowired
    private AliFileManage aliFileManage;

    @Autowired
    private TencentFileManage tencentFileManage;

    @Autowired
    private MinioFileManage minioFileManage;

    @Autowired
    private LocalFileManage localFileManage;

    /**
     * 使用配置的服务上传时location传入null 管理文件时需传入存储位置location
     * @param location
     * @return
     */
    public FileManage getFileManage(Integer location) {

        Setting setting = settingService.get(SettingConstant.OSS_USED);
        if (setting == null || StrUtil.isBlank(setting.getValue())) {
            throw new BigdataException("您还未配置OSS存储服务");
        }
        String type = setting.getValue();
        if ((type.equals(SettingConstant.QINIU_OSS) && location == null) || OssConstant.OSS_QINIU.equals(location)) {
            return qiniuFileManage;
        } else if ((type.equals(SettingConstant.ALI_OSS) && location == null) || OssConstant.OSS_ALI.equals(location)) {
            return aliFileManage;
        } else if ((type.equals(SettingConstant.TENCENT_OSS) && location == null) || OssConstant.OSS_TENCENT.equals(location)) {
            return tencentFileManage;
        } else if ((type.equals(SettingConstant.MINIO_OSS) && location == null) || OssConstant.OSS_MINIO.equals(location)) {
            return minioFileManage;
        } else if ((type.equals(SettingConstant.LOCAL_OSS) && location == null) || OssConstant.OSS_LOCAL.equals(location)) {
            return localFileManage;
        } else {
            throw new BigdataException("暂不支持该存储配置，请检查配置");
        }
    }
}
