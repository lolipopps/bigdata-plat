package com.bigdata.social.service;

import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.social.entity.Social;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Github登录接口
 * @author Bigdata
 */
public interface SocialService extends BigdataBaseService<Social, String> {

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

    /**
     * 分页多条件获取
     * @param social
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<Social> findByCondition(Social social, SearchVo searchVo, Pageable pageable);
}