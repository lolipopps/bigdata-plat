package com.bigdata.activity.dao;

import com.bigdata.activity.entity.ActCategory;
import com.bigdata.core.base.BigdataBaseDao;

import java.util.List;

/**
 * 流程分类数据处理层
 * @author Bigdata
 */
public interface ActCategoryDao extends BigdataBaseDao<ActCategory, String> {

    /**
     * 通过父id获取
     * @param parentId
     * @return
     */
    List<ActCategory> findByParentIdOrderBySortOrder(String parentId);

    /**
     * 通过名称模糊搜索
     * @param title
     * @return
     */
    List<ActCategory> findByTitleLikeOrderBySortOrder(String title);
}