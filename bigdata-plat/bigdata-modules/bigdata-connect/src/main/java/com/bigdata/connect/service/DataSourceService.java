package com.bigdata.connect.service;

import com.bigdata.connect.dto.DataSourceDTO;
import com.bigdata.connect.dto.DataSourceType;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.request.UpdataDsRequest;
import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Service
public interface DataSourceService extends BigdataBaseService<DataSource, String> {

    /**
     * 多条件分页获取
     * @param DataSource
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<DataSource> findByCondition(DataSource DataSource, SearchVo searchVo, Pageable pageable);

    Result validate(@NotNull DataSourceDTO DataSource);

    Result validate(String DataSourceId);

    DataSourceDTO getDataSourceDetails(String DataSourceId);

    Result deleteDataSource(String DataSourceId) throws Exception;

    DataSource addDataSource(DataSourceDTO DataSource) throws Exception;

    void preCheckDs(DataSourceDTO DataSource) throws Exception;

    DataSourceDTO insert(DataSourceDTO DataSource);

    Collection<DataSourceType> types();
}
