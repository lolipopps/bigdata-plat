package com.bigdata.social.vo;

import lombok.Data;

/**
 * @author Bigdata
 */
@Data
public class GithubUserInfo {

    /**
     * 唯一id
     */
    private String id;

    /**
     * 用户名
     */
    private String login;

    /**
     * 头像
     */
    private String avatar_url;
}
