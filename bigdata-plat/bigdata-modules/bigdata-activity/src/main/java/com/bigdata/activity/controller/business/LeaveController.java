package com.bigdata.activity.controller.business;

import com.bigdata.activity.entity.ActBusiness;
import com.bigdata.activity.entity.business.Leave;
import com.bigdata.activity.service.ActBusinessService;
import com.bigdata.activity.service.business.LeaveService;
import com.bigdata.core.base.BigdataBaseController;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.utils.SecurityUtil;
import com.bigdata.core.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "请假申请接口")
@Transactional
@RequestMapping(value = "/bigdata/leave")
public class LeaveController extends BigdataBaseController<Leave, String> {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private ActBusinessService actBusinessService;

    @Autowired
    private SecurityUtil securityUtil;

    @Override
    public LeaveService getService() {
        return leaveService;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "添加申请草稿状态")
    public Result add(Leave leave,
                              @RequestParam String procDefId) {

        Leave le = leaveService.save(leave);
        // 保存至我的申请业务
        String userId = securityUtil.getCurrUserSimple().getId();
        ActBusiness actBusiness = new ActBusiness();
        actBusiness.setUserId(userId);
        actBusiness.setTableId(le.getId());
        actBusiness.setProcDefId(procDefId);
        actBusiness.setTitle(leave.getTitle());
        actBusinessService.save(actBusiness);
        return ResultUtil.data(null);
    }
}
