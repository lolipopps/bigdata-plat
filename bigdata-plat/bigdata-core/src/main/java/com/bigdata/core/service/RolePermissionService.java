package com.bigdata.core.service;

import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.entity.RolePermission;

import java.util.List;

/**
 * 角色权限接口
 * @author Bigdata
 */
public interface RolePermissionService extends BigdataBaseService<RolePermission, String> {

    /**
     * 通过permissionId获取
     * @param permissionId
     * @return
     */
    List<RolePermission> findByPermissionId(String permissionId);

    /**
     * 通过roleId获取
     * @param roleId
     * @return
     */
    List<RolePermission> findByRoleId(String roleId);

    /**
     * 通过roleId删除
     * @param roleId
     */
    void deleteByRoleId(String roleId);
}