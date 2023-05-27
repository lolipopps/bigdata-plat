package com.bigdata.activity.serviceimpl;

import com.bigdata.activity.dao.ActNodeDao;
import com.bigdata.activity.entity.ActNode;
import com.bigdata.activity.service.ActNodeService;
import com.bigdata.core.common.constant.ActivitiConstant;
import com.bigdata.core.dao.DepartmentDao;
import com.bigdata.core.dao.RoleDao;
import com.bigdata.core.dao.UserDao;
import com.bigdata.core.entity.Department;
import com.bigdata.core.entity.Role;
import com.bigdata.core.entity.User;
import com.bigdata.core.vo.RoleDTO;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程节点用户接口实现
 * @author Bigdata
 */
@Slf4j
@Service
@Transactional
public class ActNodeServiceImpl implements ActNodeService {

    @Autowired
    private ActNodeDao actNodeDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Override
    public ActNodeDao getRepository() {
        return actNodeDao;
    }

    @Override
    public List<User> findUserByNodeId(String nodeId) {

        List<ActNode> listNode = actNodeDao.findByNodeIdAndType(nodeId, ActivitiConstant.NODE_USER);
        List<User> list = new ArrayList<>();
        listNode.forEach(e -> {
            User u = userDao.findById(e.getRelateId()).orElse(null);
            if (u != null) {
                list.add(u);
            }
        });
        return list;
    }

    @Override
    public List<Role> findRoleByNodeId(String nodeId) {

        List<ActNode> listNode = actNodeDao.findByNodeIdAndType(nodeId, ActivitiConstant.NODE_ROLE);
        List<Role> list = new ArrayList<>();
        listNode.forEach(e -> {
            Role r = roleDao.findById(e.getRelateId()).orElse(null);
            if (r != null) {
                list.add(r);
            }
        });
        return list;
    }

    @Override
    public List<Department> findDepartmentByNodeId(String nodeId) {

        List<ActNode> listNode = actNodeDao.findByNodeIdAndType(nodeId, ActivitiConstant.NODE_DEPARTMENT);
        List<Department> list = new ArrayList<>();
        listNode.forEach(e -> {
            Department d = departmentDao.findById(e.getRelateId()).orElse(null);
            if (d != null) {
                list.add(d);
            }
        });
        return list;
    }

    @Override
    public List<String> findDepartmentIdsByNodeId(String nodeId) {

        List<ActNode> listNode = actNodeDao.findByNodeIdAndType(nodeId, ActivitiConstant.NODE_DEPARTMENT);
        List<String> list = new ArrayList<>();
        listNode.forEach(e -> {
            list.add(e.getRelateId());
        });
        return list;
    }

    @Override
    public Boolean hasChooseDepHeader(String nodeId) {

        List<ActNode> listNode = actNodeDao.findByNodeIdAndType(nodeId, ActivitiConstant.NODE_DEP_HEADER);
        if (listNode != null && listNode.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean hasCustomUser(String nodeId) {

        List<ActNode> listNode = actNodeDao.findByNodeIdAndType(nodeId, ActivitiConstant.NODE_CUSTOM);
        if (listNode != null && listNode.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void deleteByNodeId(String nodeId) {

        actNodeDao.deleteByNodeId(nodeId);
    }

    @Override
    public void deleteByRelateId(String relateId) {

        actNodeDao.deleteByRelateId(relateId);
    }

    @Override
    public Boolean hasRecord(String processDefId, User user) {

        List<ActNode> nodeList = actNodeDao.findByNodeId(processDefId);
        if (nodeList == null || nodeList.size() == 0) {
            return false;
        }
        for (ActNode actNode : nodeList) {
            if (StrUtil.isBlank(actNode.getRelateId())) {
                continue;
            }
            if (ActivitiConstant.NODE_ROLE.equals(actNode.getType())) {
                for (RoleDTO r : user.getRoles()) {
                    if (actNode.getRelateId().equals(r.getId())) {
                        return true;
                    }
                }
            }
            if (ActivitiConstant.NODE_DEPARTMENT.equals(actNode.getType())) {
                if (actNode.getRelateId().equals(user.getDepartmentId())) {
                    return true;
                }
            }
            if (ActivitiConstant.NODE_USER.equals(actNode.getType())) {
                if (actNode.getRelateId().equals(user.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}