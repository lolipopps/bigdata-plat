package com.bigdata.core.dao;

import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.core.entity.Role;

import java.util.List;

/**
 * 角色数据处理层
 * @author Bigdata
 */
public interface RoleDao extends BigdataBaseDao<Role, String> {

    /**
     * 获取默认角色
     * @param defaultRole
     * @return
     */
    List<Role> findByDefaultRole(Boolean defaultRole);
}
