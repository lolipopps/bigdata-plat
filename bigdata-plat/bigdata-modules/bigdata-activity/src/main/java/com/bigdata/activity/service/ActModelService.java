package com.bigdata.activity.service;

import com.bigdata.activity.entity.ActModel;
import com.bigdata.core.base.BigdataBaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 模型管理接口
 * @author Bigdata
 */
public interface ActModelService extends BigdataBaseService<ActModel, String> {

    /**
     * 多条件分页获取
     * @param actModel
     * @param pageable
     * @return
     */
    Page<ActModel> findByCondition(ActModel actModel, Pageable pageable);
}