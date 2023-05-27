package com.bigdata.core.service;

import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.core.entity.StopWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 禁用词管理接口
 * @author Bigdata
 */
public interface StopWordService extends BigdataBaseService<StopWord, String> {

    /**
    * 多条件分页获取
    * @param stopWord
    * @param searchVo
    * @param pageable
    * @return
    */
    Page<StopWord> findByCondition(StopWord stopWord, SearchVo searchVo, Pageable pageable);

}