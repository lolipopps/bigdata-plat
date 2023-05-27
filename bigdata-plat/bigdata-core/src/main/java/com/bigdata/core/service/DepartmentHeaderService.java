package com.bigdata.core.service;

import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.entity.DepartmentHeader;
import com.bigdata.core.vo.UserVo;

import java.util.List;

/**
 * 部门负责人接口
 * @author Bigdata
 */
public interface DepartmentHeaderService extends BigdataBaseService<DepartmentHeader, String> {

    /**
     * 通过部门和负责人类型获取
     * @param departmentId
     * @param type
     * @return
     */
    List<UserVo> findHeaderByDepartmentId(String departmentId, Integer type);

    /**
     * 通过部门获取
     * @param departmentIds
     * @return
     */
    List<DepartmentHeader> findByDepartmentIdIn(List<String> departmentIds);

    /**
     * 通过部门id删除
     * @param departmentId
     */
    void deleteByDepartmentId(String departmentId);

    /**
     * 通过userId删除
     * @param userId
     */
    void deleteByUserId(String userId);

    /**
     * 是否为部门负责人
     * @param userId
     * @param departmentId
     * @return
     */
    Boolean isDepartmentHeader(String userId, String departmentId);
}