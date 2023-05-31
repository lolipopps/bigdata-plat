package com.bigdata.connect.controller;

import com.bigdata.connect.dto.DBTableDTO;
import com.bigdata.connect.dto.DataSourceDTO;
import com.bigdata.connect.request.ApiDefinition;
import com.bigdata.connect.request.UpdataDsRequest;
import com.bigdata.core.base.BigdataBaseController;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.utils.SecurityUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.connect.entity.DataSource;
import com.bigdata.connect.service.DataSourceService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author huyt
 */
@Slf4j
@RestController
@Api(tags = "生成代码管理接口")
@RequestMapping("/bigdata/DataSource")
@Transactional
public class DataSourceController extends BigdataBaseController<DataSource, String> {

    @Autowired
    private DataSourceService DataSourceService;

    @Autowired
    private SecurityUtil securityUtil;

    @Override
    public DataSourceService getService() {
        return DataSourceService;
    }


    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result<Page<DataSource>> getByCondition(DataSource DataSource, SearchVo searchVo, PageVo pageVo) {

        Page<DataSource> page = DataSourceService.findByCondition(DataSource, searchVo, PageUtil.initPage(pageVo));
        return new ResultUtil<Page<DataSource>>().setData(page);
    }


    @ApiOperation("新增数据源")
    @PostMapping("/add")
    public DataSource addDataSource(@RequestBody DataSourceDTO DataSource) throws Exception {
        return DataSourceService.addDataSource(DataSource);
    }


    @ApiOperation("数据源类型")
    @GetMapping("/types")
    public Collection types() throws Exception {
        return DataSourceService.types();
    }

    @ApiIgnore
    @PostMapping("/validate")
    public Result validate(@RequestBody DataSourceDTO DataSource) throws Exception {
        return DataSourceService.validate(DataSource);
    }

    @ApiOperation("验证数据源")
    @GetMapping("/validate/{DataSourceId}")
    public Result validate(@PathVariable String DataSourceId) {
        return DataSourceService.validate(DataSourceId);
    }


    @ApiOperation("查询数据源详情")
    @PostMapping("/get/{id}")
    public DataSourceDTO getDataSource(@PathVariable String id) throws Exception {
        return DataSourceService.getDataSourceDetails(id);
    }

    @ApiOperation("删除数据源")
    @PostMapping("/delete/{DataSourceID}")
    public Result deleteDataSource(@PathVariable(value = "DataSourceID") String DataSourceID) throws Exception {
        DataSource DataSource = DataSourceService.get(DataSourceID);
        Result result = DataSourceService.deleteDataSource(DataSourceID);

        return result;
    }


}
