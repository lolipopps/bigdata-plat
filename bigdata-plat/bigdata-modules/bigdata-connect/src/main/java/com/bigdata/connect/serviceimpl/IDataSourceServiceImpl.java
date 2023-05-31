package com.bigdata.connect.serviceimpl;

import com.bigdata.connect.mapper.DataSourceMapper;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.service.IDataSourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试接口实现
 * @author bigdata
 */
@Slf4j
@Service
@Transactional
public class IDataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements IDataSourceService {

    @Autowired
    private DataSourceMapper DataSourceMapper;
}