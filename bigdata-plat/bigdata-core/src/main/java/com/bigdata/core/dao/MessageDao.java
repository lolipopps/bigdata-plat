package com.bigdata.core.dao;

import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.core.entity.Message;

import java.util.List;

/**
 * 消息内容数据处理层
 * @author Bigdata
 */
public interface MessageDao extends BigdataBaseDao<Message, String> {

    /**
     * 通过创建发送标识获取
     * @param createSend
     * @return
     */
    List<Message> findByCreateSend(Boolean createSend);
}