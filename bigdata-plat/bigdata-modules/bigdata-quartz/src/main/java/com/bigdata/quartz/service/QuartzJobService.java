package com.bigdata.quartz.service;


import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.quartz.entity.QuartzJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 定时任务接口
 * @author Bigdata
 */
public interface QuartzJobService extends BigdataBaseService<QuartzJob, String> {

    /**
     * 通过类名获取
     * @param jobClassName
     * @return
     */
    List<QuartzJob> findByJobClassName(String jobClassName);

    /**
     * 分页获取
     * @param key
     * @param pageable
     * @return
     */
    Page<QuartzJob> findByCondition(String key, Pageable pageable);
}