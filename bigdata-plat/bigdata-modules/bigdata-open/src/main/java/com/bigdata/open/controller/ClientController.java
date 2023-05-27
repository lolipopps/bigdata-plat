package com.bigdata.open.controller;

import com.bigdata.core.base.BigdataBaseController;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.open.entity.Client;
import com.bigdata.open.service.ClientService;
import cn.hutool.core.util.IdUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "客户端管理接口")
@RequestMapping("/bigdata/client")
@Transactional
public class ClientController extends BigdataBaseController<Client, String> {

    @Autowired
    private ClientService clientService;

    @Override
    public ClientService getService() {
        return clientService;
    }

    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result<Page<Client>> getByCondition(Client client,
                                               SearchVo searchVo,
                                               PageVo pageVo) {

        Page<Client> page = clientService.findByCondition(client, searchVo, PageUtil.initPage(pageVo));
        return new ResultUtil<Page<Client>>().setData(page);
    }

    @RequestMapping(value = "/getSecretKey", method = RequestMethod.GET)
    @ApiOperation(value = "生成随机secretKey")
    public Result<String> getSecretKey() {

        String secretKey = IdUtil.simpleUUID();
        return new ResultUtil<String>().setData(secretKey);
    }
}
