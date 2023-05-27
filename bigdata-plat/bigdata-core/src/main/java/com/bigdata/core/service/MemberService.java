package com.bigdata.core.service;

import com.bigdata.core.base.BigdataBaseService;
import com.bigdata.core.common.vo.SearchVo;
import com.bigdata.core.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 会员接口
 * @author Bigdata
 */
public interface MemberService extends BigdataBaseService<Member, String> {

    /**
     * 多条件分页获取
     * @param member
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<Member> findByCondition(Member member, SearchVo searchVo, Pageable pageable);

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