package com.bigdata.connect.mapper;

import com.bigdata.connect.entity.DataDriverDetails;
import com.bigdata.connect.entity.DataDriverDetailsExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataDriverDetailsMapper {
    long countByExample(DataDriverDetailsExample example);

    int deleteByExample(DataDriverDetailsExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataDriverDetails record);

    int insertSelective(DataDriverDetails record);

    List<DataDriverDetails> selectByExample(DataDriverDetailsExample example);

    DataDriverDetails selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataDriverDetails record, @Param("example") DataDriverDetailsExample example);

    int updateByExample(@Param("record") DataDriverDetails record, @Param("example") DataDriverDetailsExample example);

    int updateByPrimaryKeySelective(DataDriverDetails record);

    int updateByPrimaryKey(DataDriverDetails record);
}