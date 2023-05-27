package com.bigdata.core.service;


import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.core.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 日志接口
 * @author Bigdata
 */
public interface LogService extends BigdataBaseService<Log, String> {

    /**
     * 分页搜索获取日志
     * @param type
     * @param key
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<Log> findByConfition(Integer type, String key, SearchVo searchVo, Pageable pageable);

    /**
     * 删除所有
     */
    void deleteAll();
}
