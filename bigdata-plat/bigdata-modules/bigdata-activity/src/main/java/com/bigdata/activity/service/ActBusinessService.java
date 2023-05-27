package com.bigdata.activity.service;

import com.bigdata.activity.entity.ActBusiness;
import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 业务申请接口
 * @author Bigdata
 */
public interface ActBusinessService extends BigdataBaseService<ActBusiness, String> {

    /**
     * 多条件分页获取申请列表
     * @param actBusiness
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<ActBusiness> findByCondition(ActBusiness actBusiness, SearchVo searchVo, Pageable pageable);

    /**
     * 通过流程定义id获取
     * @param procDefId
     * @return
     */
    List<ActBusiness> findByProcDefId(String procDefId);
}