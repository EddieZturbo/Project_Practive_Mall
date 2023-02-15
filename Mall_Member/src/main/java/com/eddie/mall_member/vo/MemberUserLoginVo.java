package com.eddie.mall_member.vo;

import lombok.Data;


@Data
public class MemberUserLoginVo {

    private String loginacct;//登录账号（用户名/手机号）

    private String password;//密码

}
