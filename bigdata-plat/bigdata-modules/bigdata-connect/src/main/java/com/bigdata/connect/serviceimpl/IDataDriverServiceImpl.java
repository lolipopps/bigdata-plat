package com.bigdata.connect.serviceimpl;

import com.bigdata.connect.mapper.DataDriverMapper;
import com.bigdata.connect.entity.DataDriver;
import com.bigdata.connect.service.IDataDriverService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据驱动接口实现
 * @author bigdata
 */
@Slf4j
@Service
@Transactional
public class IDataDriverServiceImpl extends ServiceImpl<DataDriverMapper, DataDriver> implements IDataDriverService {

    @Autowired
    private DataDriverMapper dataDriverMapper;
}