package com.bigdata.core.dao;

import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.core.entity.MessageSend;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 消息发送数据处理层
 * @author Bigdata
 */
public interface MessageSendDao extends BigdataBaseDao<MessageSend, String> {

    /**
     * 通过消息id删除
     * @param messageId
     */
    @Modifying
    @Query("delete from MessageSend m where m.messageId = ?1")
    void deleteByMessageId(String messageId);

    /**
     * 批量更新消息状态
     * @param userId
     * @param status
     */
    @Modifying
    @Query("update MessageSend m set m.status=?2 where m.userId=?1")
    void updateStatusByUserId(String userId, Integer status);

    /**
     * 通过userId删除
     * @param userId
     * @param status
     */
    @Modifying
    @Query("delete from MessageSend m where m.userId = ?1 and m.status=?2")
    void deleteByUserId(String userId, Integer status);
}