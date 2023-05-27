package com.bigdata.file.dao;


import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.file.entity.File;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 文件管理数据处理层
 * @author Bigdata
 */
public interface FileDao extends BigdataBaseDao<File, String> {

    /**
     * 清空回收站
     * @param username
     * @param delFlag
     */
    @Modifying
    @Query("delete from File f where f.createBy = ?1 and f.delFlag = ?2")
    void deleteByCreateByAndDelFlag(String username, Integer delFlag);

    /**
     * 通过categoryId删除
     * @param categoryId
     */
    @Modifying
    @Query("delete from File f where f.categoryId = ?1")
    void deleteByCategoryId(String categoryId);
}