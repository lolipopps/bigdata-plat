package com.bigdata.core.dao;


import com.bigdata.core.base.BigdataBaseDao;
import com.bigdata.core.entity.Member;

/**
 * 会员数据处理层
 * @author Bigdata
 */
public interface MemberDao extends BigdataBaseDao<Member, String> {

    /**
     * 通过用户名获取用户
     * @param username
     * @return
     */
    Member findByUsername(String username);

    /**
     * 通过手机获取用户
     * @param mobile
     * @return
     */
    Member findByMobile(String mobile);
}