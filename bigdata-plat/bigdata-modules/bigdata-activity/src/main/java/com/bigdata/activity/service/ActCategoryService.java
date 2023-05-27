package com.bigdata.activity.service;

import com.bigdata.activity.entity.ActCategory;
import com.bigdata.core.base.BigdataBaseService;

import java.util.List;

/**
 * 流程分类接口
 * @author Bigdata
 */
public interface ActCategoryService extends BigdataBaseService<ActCategory, String> {

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