package com.eddie.mall_seckill.inteceptor;

import com.eddie.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.eddie.common.constant.AuthServerConstant.LOGIN_USER;

/**
 @author EddieZhang
 @create 2023-02-27 4:15 PM
 */
@Component
public class SecKillInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResponseVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", uri);
        //判断是否是进行秒杀商品的请求路径 是才进行登录拦截
        if (match) {
            //从session中获取用户的登录状态
            MemberResponseVo userLogin = (MemberResponseVo) request.getSession().getAttribute(LOGIN_USER);
            if (null != userLogin) {
                //已经登录 放行 (将登录的用户保存到ThreadLocal中供线程中使用)
                threadLocal.set(userLogin);
                return true;
            } else {
                //未登录 拦截 并指引到登录界面进行登录 将提示信息赋值到attribute中返回给前端页面
                request.setAttribute("msg", "登录后才能参与秒杀");
                response.sendRedirect("http://auth.zhangjinhao.com/login");
                return false;
            }
        }
        return true;
    }
}
