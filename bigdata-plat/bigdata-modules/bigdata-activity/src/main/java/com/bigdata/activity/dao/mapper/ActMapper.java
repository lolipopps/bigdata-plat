package com.bigdata.activity.dao.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @author Bigdata
 */
public interface ActMapper {

    /**
     * 删除关联业务表
     * @param table
     * @param id
     * @return
     */
    Integer deleteBusiness(@Param("table") String table, @Param("id") String id);
}
