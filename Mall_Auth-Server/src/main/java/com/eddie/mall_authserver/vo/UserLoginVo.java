package com.eddie.mall_authserver.vo;

import lombok.Data;


@Data
public class UserLoginVo {

    private String loginacct;//登录账号（用户名/手机号）

    private String password;//密码
}
