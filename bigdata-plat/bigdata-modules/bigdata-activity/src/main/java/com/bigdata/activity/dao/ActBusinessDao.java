package com.bigdata.activity.dao;

import com.bigdata.activity.entity.ActBusiness;
import com.bigdata.core.base.BigdataBaseDao;

import java.util.List;

/**
 * 申请业务数据处理层
 * @author Bigdata
 */
public interface ActBusinessDao extends BigdataBaseDao<ActBusiness, String> {

    /**
     * 通过流程定义id获取
     * @param procDefId
     * @return
     */
    List<ActBusiness> findByProcDefId(String procDefId);
}