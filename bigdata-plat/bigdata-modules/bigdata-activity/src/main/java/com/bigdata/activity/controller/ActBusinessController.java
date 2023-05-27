package com.bigdata.activity.controller;

import com.bigdata.activity.entity.ActBusiness;
import com.bigdata.activity.entity.ActProcess;
import com.bigdata.activity.entity.business.Leave;
import com.bigdata.activity.service.ActBusinessService;
import com.bigdata.activity.service.ActProcessService;
import com.bigdata.activity.service.business.LeaveService;
import com.bigdata.activity.service.mybatis.IActService;
import com.bigdata.activity.vo.ActMessageVo;
import com.bigdata.core.common.constant.ActivitiConstant;
import com.bigdata.core.common.exception.BigdataException;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.common.vo.SearchVo;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "业务申请管理接口")
@RequestMapping("/bigdata/actBusiness")
@Transactional
public class ActBusinessController {

    @Autowired
    private ActBusinessService actBusinessService;

    @Autowired
    private IActService iActService;

    @Autowired
    private ActProcessService actProcessService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private LeaveService leaveService;

    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取申请列表")
    public Result<Page<ActBusiness>> getByCondition(ActBusiness actBusiness,
                                                    SearchVo searchVo,
                                                    PageVo pageVo) {

        Page<ActBusiness> page = actBusinessService.findByCondition(actBusiness, searchVo, PageUtil.initPage(pageVo));
        page.getContent().forEach(e -> {
            if (StrUtil.isNotBlank(e.getProcDefId())) {
                ActProcess actProcess = actProcessService.get(e.getProcDefId());
                e.setRouteName(actProcess.getRouteName()).setProcessName(actProcess.getName()).setVersion(actProcess.getVersion());
            }
            if (ActivitiConstant.STATUS_DEALING.equals(e.getStatus())) {
                // 关联当前任务
                List<Task> taskList = taskService.createTaskQuery().processInstanceId(e.getProcInstId()).list();
                if (taskList != null && taskList.size() == 1) {
                    e.setCurrTaskName(taskList.get(0).getName());
                } else if (taskList != null && taskList.size() > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < taskList.size() - 1; i++) {
                        sb.append(taskList.get(i).getName() + "、");
                    }
                    sb.append(taskList.get(taskList.size() - 1).getName());
                    e.setCurrTaskName(sb.toString());
                }
            }
        });
        return new ResultUtil<Page<ActBusiness>>().setData(page);
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    @ApiOperation(value = "提交申请 启动流程")
    public Result apply(ActBusiness act,
                                ActMessageVo m) {

        ActBusiness actBusiness = actBusinessService.findById(act.getId());
        if (actBusiness == null) {
            return ResultUtil.error("流程业务表actBusiness中无该数据ID: " + act.getId());
        }
        act.setTableId(actBusiness.getTableId()).setUserId(actBusiness.getUserId());
        // 根据你的业务需求放入相应流程所需变量
        act = putParams(act);
        String processInstanceId = actProcessService.startProcess(act, m);
        actBusiness.setProcInstId(processInstanceId);
        actBusiness.setStatus(ActivitiConstant.STATUS_DEALING);
        actBusiness.setResult(ActivitiConstant.RESULT_DEALING);
        actBusiness.setApplyTime(new Date());
        actBusinessService.update(actBusiness);
        return ResultUtil.success("操作成功");
    }

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ApiOperation(value = "流程选择组件启动流程")
    public Result start(ActBusiness act,
                                ActMessageVo m) {

        ActBusiness actBusiness = actBusinessService.findById(act.getId());
        if (actBusiness == null) {
            return ResultUtil.error("流程业务表actBusiness中无该数据ID: " + act.getId());
        }
        act.setTableId(actBusiness.getTableId()).setUserId(actBusiness.getUserId());
        // 根据你的业务需求放入相应流程所需变量
        act = putParams(act);
        String processInstanceId = actProcessService.startProcess(act, m);
        actBusiness.setProcDefId(act.getProcDefId());
        actBusiness.setTitle(act.getTitle());
        actBusiness.setProcInstId(processInstanceId);
        actBusiness.setStatus(ActivitiConstant.STATUS_DEALING);
        actBusiness.setResult(ActivitiConstant.RESULT_DEALING);
        actBusiness.setApplyTime(new Date());
        actBusinessService.update(actBusiness);
        return ResultUtil.success("操作成功");
    }

    /**
     * 放入相应流程所需变量 此处仅做演示
     * 【推荐使用绑定监听器设置变量 更加灵活】
     * @param act
     * @return
     */
    public ActBusiness putParams(ActBusiness act) {

        if (StrUtil.isBlank(act.getTableId())) {
            throw new BigdataException("关联业务表TableId不能为空");
        }
        // 如果属于请假流程
        Leave leave = leaveService.findById(act.getTableId());
        if (leave != null) {
            // 放入变量
            act.getParams().put("duration", leave.getDuration());
        }
        return act;
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ApiOperation(value = "撤回申请")
    public Result cancel(@RequestParam String id,
                                 @RequestParam String procInstId,
                                 @RequestParam(required = false) String reason) {

        if (StrUtil.isBlank(reason)) {
            reason = "";
        }
        runtimeService.deleteProcessInstance(procInstId, "canceled-" + reason);
        ActBusiness actBusiness = actBusinessService.get(id);
        actBusiness.setStatus(ActivitiConstant.STATUS_CANCELED);
        actBusiness.setResult(ActivitiConstant.RESULT_TO_SUBMIT);
        actBusinessService.update(actBusiness);
        return ResultUtil.success("操作成功");
    }

    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    @ApiOperation(value = "通过id删除草稿状态申请")
    public Result delByIds(@RequestParam String[] ids) {

        for (String id : ids) {
            ActBusiness actBusiness = actBusinessService.get(id);
            if (!ActivitiConstant.STATUS_TO_APPLY.equals(actBusiness.getStatus())) {
                return ResultUtil.error("删除失败, 仅能删除草稿状态的申请");
            }
            // 删除关联业务表
            ActProcess actProcess = actProcessService.get(actBusiness.getProcDefId());
            PageUtil.SQLInject(actProcess.getBusinessTable());
            iActService.deleteBusiness(actProcess.getBusinessTable(), actBusiness.getTableId());
            actBusinessService.delete(id);
        }
        return ResultUtil.success("删除成功");
    }
}
