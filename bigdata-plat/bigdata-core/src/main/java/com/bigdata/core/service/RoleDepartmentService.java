package com.bigdata.core.service;

import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.entity.RoleDepartment;

import java.util.List;

/**
 * 角色部门接口
 * @author Bigdata
 */
public interface RoleDepartmentService extends BigdataBaseService<RoleDepartment, String> {

    /**
     * 通过roleId获取
     * @param roleId
     * @return
     */
    List<RoleDepartment> findByRoleId(String roleId);

    /**
     * 通过角色id删除
     * @param roleId
     */
    void deleteByRoleId(String roleId);

    /**
     * 通过角色id删除
     * @param departmentId
     */
    void deleteByDepartmentId(String departmentId);
}