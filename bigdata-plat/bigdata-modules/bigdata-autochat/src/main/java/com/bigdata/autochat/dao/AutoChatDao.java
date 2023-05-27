 package com.bigdata.autochat.dao;

import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.autochat.entity.AutoChat;

import java.util.List;

 /**
  * 问答助手客服数据处理层
  * @author Bigdata
  */
 public interface AutoChatDao extends BigdataBaseDao<AutoChat, String> {

     /**
      * 完全匹配
      * @param title
      * @return
      */
     List<AutoChat> findByTitle(String title);
 }