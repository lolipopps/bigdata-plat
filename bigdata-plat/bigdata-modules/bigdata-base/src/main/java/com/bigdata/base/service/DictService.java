package com.bigdata.base.service;

import com.bigdata.base.entity.Dict;
import com.bigdata.core.base.BigdataBaseService;

import java.util.List;

/**
 * 字典接口
 * @author Bigdata
 */
public interface DictService extends BigdataBaseService<Dict, String> {

    /**
     * 排序获取全部
     * @return
     */
    List<Dict> findAllOrderBySortOrder();

    /**
     * 通过type获取
     * @param type
     * @return
     */
    Dict findByType(String type);

    /**
     * 模糊搜索
     * @param key
     * @return
     */
    List<Dict> findByTitleOrTypeLike(String key);
}