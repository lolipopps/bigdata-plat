package com.bigdata.quartz.dao;

import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.quartz.entity.QuartzJob;

import java.util.List;

/**
 * 定时任务数据处理层
 * @author Bigdata
 */
public interface QuartzJobDao extends BigdataBaseDao<QuartzJob, String> {

    /**
     * 通过类名获取
     * @param jobClassName
     * @return
     */
    List<QuartzJob> findByJobClassName(String jobClassName);
}