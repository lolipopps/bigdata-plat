package com.bigdata.connect.mapper;

import com.bigdata.connect.dto.DataSourceDTO;
import com.bigdata.connect.request.DataSourceUnionRequest;
 
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtDataSourceMapper {


    List<DataSourceDTO> queryUnion(DataSourceUnionRequest request);

    List<DataSourceDTO> findByPanelId(@Param("panelId") String panelId);

    List<DataSourceDTO> findByTableIds(@Param("tableIds") List<String> tableIds);

    DataSourceDTO queryDetails(@Param("DataSourceId") String DataSourceId, @Param("userId") String userId);

 }
