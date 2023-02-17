package eddie.com.mall_cart.interceptor;

import com.eddie.common.constant.AuthServerConstant;
import com.eddie.common.constant.CartConstant;
import com.eddie.common.vo.MemberResponseVo;
import eddie.com.mall_cart.to.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.eddie.common.constant.AuthServerConstant.LOGIN_USER;

/**
 @author EddieZhang
 @create 2023-02-17 9:37 PM
 */
public class UserCartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        //判断使用已经是用户登录状态 查看SpringSession中的LOGIN_USER 判断是否能获取到用户的登录信息
        MemberResponseVo memberResponseVo = (MemberResponseVo) request.getSession().getAttribute(LOGIN_USER);
        if (null != memberResponseVo) {
            //用户登录了
            userInfoTo.setUserId(memberResponseVo.getId());
        }

        //判断cookie中是否已经设置了临时用户的userKey
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //user-key
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    //标记为已是临时用户
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //给用户设置一个临时的userKey
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //指定目标方法之前 将userInfoTo保存进ThreadLocal中 以供后续线程内使用
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 目标方法执行后拦截
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!threadLocal.get().getTempUser()) {//判断是否已经set了一个月后过期的临时用户的userKey的cookie
            //没有就set一个
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, threadLocal.get().getUserKey());
            cookie.setDomain("zhangjinhao.com");//设置cookie的域名范围
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);//设置cookie的过期时间
            response.addCookie(cookie);
        }
    }
}
