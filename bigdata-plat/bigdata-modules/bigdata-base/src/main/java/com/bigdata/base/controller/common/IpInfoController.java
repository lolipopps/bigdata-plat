package com.bigdata.base.controller.common;

import com.bigdata.core.common.utils.IpInfoUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "IP接口")
@RequestMapping("/bigdata/common/ip")
@Transactional
public class IpInfoController {

    @Autowired
    private IpInfoUtil ipInfoUtil;

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "IP及天气相关信息")
    public Result upload(HttpServletRequest request) {

        String result = ipInfoUtil.getIpCity(request);
        return ResultUtil.data(result);
    }
}