package com.bigdata.core.service;


import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.entity.User;
import com.bigdata.core.entity.UserRole;

import java.util.List;

/**
 * 用户角色接口
 * @author Bigdata
 */
public interface UserRoleService extends BigdataBaseService<UserRole, String> {

    /**
     * 通过roleId查找
     * @param roleId
     * @return
     */
    List<UserRole> findByRoleId(String roleId);

    /**
     * 通过roleId查找用户
     * @param roleId
     * @return
     */
    List<User> findUserByRoleId(String roleId);

    /**
     * 删除用户角色
     * @param userId
     */
    void deleteByUserId(String userId);
}
