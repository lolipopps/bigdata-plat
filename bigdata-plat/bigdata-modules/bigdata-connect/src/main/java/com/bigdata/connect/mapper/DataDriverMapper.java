package com.bigdata.connect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.connect.entity.DataDriver;
import com.bigdata.connect.entity.DataDriverExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据驱动数据处理层
 * @author bigdata
 */
public interface DataDriverMapper extends BaseMapper<DataDriver> {

    long countByExample(DataDriverExample example);

    int deleteByExample(DataDriverExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataDriver record);

    int insertSelective(DataDriver record);

    List<DataDriver> selectByExample(DataDriverExample example);

    DataDriver selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataDriver record, @Param("example") DataDriverExample example);

    int updateByExample(@Param("record") DataDriver record, @Param("example") DataDriverExample example);

    int updateByPrimaryKeySelective(DataDriver record);

    int updateByPrimaryKey(DataDriver record);

}