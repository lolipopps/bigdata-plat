package com.bigdata.core.serviceimpl;

import com.bigdata.core.dao.DepartmentHeaderDao;
import com.bigdata.core.dao.UserDao;
import com.bigdata.core.entity.DepartmentHeader;
import com.bigdata.core.entity.User;
import com.bigdata.core.service.DepartmentHeaderService;
import com.bigdata.core.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门负责人接口实现
 *
 * @author Bigdata
 */
@Slf4j
@Service
@Transactional
public class DepartmentHeaderServiceImpl implements DepartmentHeaderService {

    @Autowired
    private DepartmentHeaderDao departmentHeaderDao;

    @Autowired
    private UserDao userDao;

    @Override
    public DepartmentHeaderDao getRepository() {
        return departmentHeaderDao;
    }


    @Override
    public List<UserVo> findHeaderByDepartmentId(String departmentId, Integer type) {

        List<UserVo> list = new ArrayList<>();
        List<DepartmentHeader> headers = departmentHeaderDao.findByDepartmentIdAndType(departmentId, type);
        headers.forEach(e -> {
            User u = userDao.findById(e.getUserId()).orElse(null);
            if (u != null) {
                list.add(new UserVo().setId(u.getId()).setUsername(u.getUsername()).setNickname(u.getNickname()));
            }
        });
        return list;
    }

    @Override
    public List<DepartmentHeader> findByDepartmentIdIn(List<String> departmentIds) {

        return departmentHeaderDao.findByDepartmentIdIn(departmentIds);
    }

    @Override
    public void deleteByDepartmentId(String departmentId) {

        departmentHeaderDao.deleteByDepartmentId(departmentId);
    }

    @Override
    public void deleteByUserId(String userId) {

        departmentHeaderDao.deleteByUserId(userId);
    }

    @Override
    public Boolean isDepartmentHeader(String userId, String departmentId) {

        List<DepartmentHeader> headers = departmentHeaderDao.findByUserIdAndDepartmentId(userId, departmentId);
        if (headers != null && !headers.isEmpty()) {
            return true;
        }
        return false;
    }
}