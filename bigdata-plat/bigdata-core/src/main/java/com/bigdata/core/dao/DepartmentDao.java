package com.bigdata.core.dao;

import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.core.entity.Department;

import java.util.List;

/**
 * 部门数据处理层
 * @author Bigdata
 */
public interface DepartmentDao extends BigdataBaseDao<Department, String> {

    /**
     * 通过父id获取 升序
     * @param parentId
     * @return
     */
    List<Department> findByParentIdOrderBySortOrder(String parentId);

    /**
     * 通过父id获取 升序 数据权限
     * @param parentId
     * @param departmentIds
     * @return
     */
    List<Department> findByParentIdAndIdInOrderBySortOrder(String parentId, List<String> departmentIds);

    /**
     * 通过父id和状态获取 升序
     * @param parentId
     * @param status
     * @return
     */
    List<Department> findByParentIdAndStatusOrderBySortOrder(String parentId, Integer status);

    /**
     * 部门名模糊搜索 升序
     * @param title
     * @return
     */
    List<Department> findByTitleLikeOrderBySortOrder(String title);

    /**
     * 部门名模糊搜索 升序 数据权限
     * @param title
     * @param departmentIds
     * @return
     */
    List<Department> findByTitleLikeAndIdInOrderBySortOrder(String title, List<String> departmentIds);

}