package com.bigdata.activity.serviceimpl.mybatis;

import com.bigdata.activity.dao.mapper.ActMapper;
import com.bigdata.activity.service.mybatis.IActService;
import com.bigdata.core.common.exception.BigdataException;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Bigdata
 */
@Service
public class IActServiceImpl implements IActService {

    @Autowired
    private ActMapper actMapper;

    @Override
    public Integer deleteBusiness(String table, String id) {

        if (StrUtil.isBlank(table) || StrUtil.isBlank(id)) {
            throw new BigdataException("关联业务表名或id为空");
        }
        return actMapper.deleteBusiness(table, id);
    }
}
