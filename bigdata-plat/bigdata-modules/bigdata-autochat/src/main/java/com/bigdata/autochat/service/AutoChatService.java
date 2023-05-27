package com.bigdata.autochat.service;

import com.bigdata.autochat.entity.AutoChat;
import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 问答助手客服接口
 * @author Bigdata
 */
public interface AutoChatService extends BigdataBaseService<AutoChat, String> {

    /**
     * 多条件分页获取
     * @param autoChat
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<AutoChat> findByCondition(AutoChat autoChat, SearchVo searchVo, Pageable pageable);

    /**
     * 完全匹配
     * @param title
     * @return
     */
    AutoChat findByTitle(String title);
}