package com.bigdata.base.controller.manage;

import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.core.entity.Log;
import com.bigdata.core.entity.elasticsearch.EsLog;
import com.bigdata.core.service.LogService;
import com.bigdata.core.service.elasticsearch.EsLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;


/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "日志管理接口")
@RequestMapping("/bigdata/log")
@Transactional
public class LogController {

    @Value("${bigdata.logRecord.es:false}")
    private Boolean esRecord;

    @Autowired
    private EsLogService esLogService;

    @Autowired
    private LogService logService;

    @RequestMapping(value = "/getAllByPage", method = RequestMethod.GET)
    @ApiOperation(value = "分页获取全部")
    public Result getAllByPage(@RequestParam(required = false) Integer type,
                                       @RequestParam String key,
                                       SearchVo searchVo,
                                       PageVo pageVo) throws UnsupportedEncodingException {

        if (esRecord) {
            // 支持排序的字段
            if (!"costTime".equals(pageVo.getSort())) {
                pageVo.setSort("timeMillis");
            }
            Page<EsLog> es = esLogService.findByCondition(type, key.trim(), searchVo, PageUtil.initPage(pageVo));
            return ResultUtil.data(es);
        } else {
            Page<Log> log = logService.findByConfition(type, key, searchVo, PageUtil.initPage(pageVo));
            return ResultUtil.data(log);
        }
    }

    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    @ApiOperation(value = "批量删除")
    public Result delByIds(@RequestParam String[] ids) {

        for (String id : ids) {
            if (esRecord) {
                esLogService.deleteLog(id);
            } else {
                logService.delete(id);
            }
        }
        return ResultUtil.success("删除成功");
    }

    @RequestMapping(value = "/delAll", method = RequestMethod.POST)
    @ApiOperation(value = "全部删除")
    public Result delAll() {

        if (esRecord) {
            esLogService.deleteAll();
        } else {
            logService.deleteAll();
        }
        return ResultUtil.success("删除成功");
    }
}
