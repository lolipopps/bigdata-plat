package com.bigdata.social.dao;


import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.social.entity.Social;

import java.util.List;

/**
 * Github登录数据处理层
 * @author Bigdata
 */
public interface SocialDao extends BigdataBaseDao<Social, String> {

    /**
     * 通过openId和平台获取
     * @param openId
     * @param platform
     * @return
     */
    Social findByOpenIdAndPlatform(String openId, Integer platform);

    /**
     * 通过userId和平台获取
     * @param relateUsername
     * @param platform
     * @return
     */
    Social findByRelateUsernameAndPlatform(String relateUsername, Integer platform);

    /**
     * 通过relateUsername获取
     * @param relateUsername
     * @return
     */
    List<Social> findByRelateUsername(String relateUsername);
}