package com.bigdata.connect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.entity.DataSourceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试数据处理层
 * @author bigdata
 */
public interface DataSourceMapper extends BaseMapper<DataSource> {

    long countByExample(DataSourceExample example);

    int deleteByExample(DataSourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataSource record);

    int insertSelective(DataSource record);

    List<DataSource> selectByExampleWithBLOBs(DataSourceExample example);

    List<DataSource> selectByExample(DataSourceExample example);

    DataSource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataSource record, @Param("example") DataSourceExample example);

    int updateByExampleWithBLOBs(@Param("record") DataSource record, @Param("example") DataSourceExample example);

    int updateByExample(@Param("record") DataSource record, @Param("example") DataSourceExample example);

    int updateByPrimaryKeySelective(DataSource record);

    int updateByPrimaryKeyWithBLOBs(DataSource record);

    int updateByPrimaryKey(DataSource record);
}