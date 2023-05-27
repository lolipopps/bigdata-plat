package com.bigdata.activity.serviceimpl.mybatis;

import com.bigdata.activity.dao.mapper.RunIdentityMapper;
import com.bigdata.activity.service.mybatis.IRunIdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Bigdata
 */
@Service
public class IRunIdentityServiceImpl implements IRunIdentityService {

    @Autowired
    private RunIdentityMapper runIdentityMapper;

    @Override
    public List<String> selectByConditions(String taskId, String type) {
        return runIdentityMapper.selectByConditions(taskId, type);
    }
}
