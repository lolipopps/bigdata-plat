package com.bigdata.base.controller.manage;

import com.bigdata.core.base.BigdataBaseController;
import com.bigdata.core.common.constant.MessageConstant;
import com.bigdata.core.common.utils.PageUtil;
import com.bigdata.core.common.utils.ResultUtil;
import com.bigdata.core.common.utils.SecurityUtil;
import com.bigdata.core.common.vo.PageVo;
import com.bigdata.core.common.vo.Result;
import com.bigdata.core.entity.Message;
import com.bigdata.core.entity.MessageSend;
import com.bigdata.core.entity.User;
import com.bigdata.core.service.MessageSendService;
import com.bigdata.core.service.MessageService;
import com.bigdata.core.service.UserService;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author Bigdata
 */
@Slf4j
@RestController
@Api(tags = "消息发送管理接口")
@RequestMapping("/bigdata/messageSend")
@Transactional
public class MessageSendController extends BigdataBaseController<MessageSend, String> {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageSendService messageSendService;

    @Override
    public MessageSendService getService() {
        return messageSendService;
    }

    @Autowired
    private SecurityUtil securityUtil;

    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result<Page<MessageSend>> getByCondition(MessageSend ms,
                                                    PageVo pv) {

        Page<MessageSend> page = messageSendService.findByCondition(ms, PageUtil.initPage(pv));
        page.getContent().forEach(item -> {
            User u = userService.findById(item.getUserId());
            if (u != null) {
                item.setUsername(u.getUsername()).setNickname(u.getNickname());
            }
            Message m = messageService.findById(item.getMessageId());
            if (m != null) {
                if (m.getIsTemplate()) {
                    Message message = messageSendService.getTemplateMessage(item.getMessageId(),
                            new Gson().fromJson(item.getParams(), HashMap.class));
                    item.setTitle(message.getTitle()).setContent(message.getContent()).setType(m.getType());
                } else {
                    item.setTitle(m.getTitle()).setContent(m.getContent()).setType(m.getType());
                }
            }
        });
        return new ResultUtil<Page<MessageSend>>().setData(page);
    }

    @RequestMapping(value = "/all/{type}", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页获取")
    public Result batchOperation(@Param("0全部已读 1全部删除已读") @PathVariable Integer type) {

        User u = securityUtil.getCurrUserSimple();
        if (type == 0) {
            messageSendService.updateStatusByUserId(u.getId(), MessageConstant.MESSAGE_STATUS_READ);
        } else if (type == 1) {
            messageSendService.deleteByUserId(u.getId());
        }
        return ResultUtil.success("操作成功");
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ApiOperation(value = "编辑")
    public Result edit(MessageSend messageSend) {

        if (messageService.findById(messageSend.getMessageId()) != null) {
            messageSendService.update(messageSend);
        }
        return ResultUtil.success("操作成功");
    }
}
