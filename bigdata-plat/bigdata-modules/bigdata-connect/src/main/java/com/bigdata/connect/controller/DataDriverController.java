package com.bigdata.connect.controller;

import com.bigdata.connect.dto.DataDriverDTO;
import com.bigdata.connect.entity.DataDriverDetails;
import com.bigdata.core.base.BigdataBaseController;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.connect.entity.DataDriver;
import com.bigdata.connect.service.DataDriverService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huyt
 */
@Slf4j
@RestController
@Api(tags = "生成代码管理接口")
@RequestMapping("/bigdata/dataDriver")
@Transactional
public class DataDriverController extends BigdataBaseController<DataDriver, String> {

    @Autowired
    private DataDriverService dataDriverService;

    @Override
    public DataDriverService getService() {
        return dataDriverService;
    }


    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result<Page<DataDriver>> getByCondition(DataDriver dataDriver, SearchVo searchVo, PageVo pageVo) {

        Page<DataDriver> page = dataDriverService.findByCondition(dataDriver, searchVo, PageUtil.initPage(pageVo));
        return new ResultUtil<Page<DataDriver>>().setData(page);
    }

    @ApiOperation("驱动列表")
    @PostMapping("/list")
    public List<DataDriverDTO> listDeDriver() throws Exception{
        return dataDriverService.list();
    }

    @ApiOperation("删除驱动")
    @PostMapping("/delete")
    public void delete(@RequestBody DataDriver deDriver) throws Exception{
        dataDriverService.delete(deDriver);
    }


    @ApiOperation("驱动列表")
    @GetMapping("/list/{type}")
    public List<DataDriverDTO> listDeDriver(@PathVariable String type) throws Exception{
        return listDeDriver().stream().filter(driverDTO -> driverDTO.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
    }


    @ApiOperation("添加驱动")
    @PostMapping("/save")
    public Result<DataDriver> save(@RequestBody DataDriver deDriver) {
        return new ResultUtil<DataDriver>().setData(dataDriverService.save(deDriver));
    }

    @ApiOperation("更新驱动")
    @PostMapping("/update")
    public Result<DataDriver> update(@RequestBody DataDriver deDriver) {
        return new ResultUtil<DataDriver>().setData(dataDriverService.update(deDriver));
    }


    @ApiOperation("驱动文件列表")
    @GetMapping("/listDriverDetails/{id}")
    public List<DataDriverDetails> listDriverDetails(@PathVariable String id) throws Exception{
        return dataDriverService.listDriverDetails(id);
    }


    @ApiOperation("删除驱动文件")
    @PostMapping("/deleteDriverFile")
    public void deleteDriverFile(@RequestBody DataDriverDetails deDriverDetails) throws Exception{
        dataDriverService.deleteDriverFile(deDriverDetails.getId());
    }


    @ApiOperation("驱动文件上传")
    @PostMapping("file/upload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "id", value = "驱动D", required = true, dataType = "String")
    })
    public DataDriverDetails excelUpload(@RequestParam("id") String id, @RequestParam("file") MultipartFile file) throws Exception {
        return dataDriverService.saveJar(file, id);
    }

}
