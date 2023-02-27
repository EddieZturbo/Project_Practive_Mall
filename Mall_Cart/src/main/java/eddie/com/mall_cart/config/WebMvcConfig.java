package eddie.com.mall_cart.config;

import eddie.com.mall_cart.interceptor.UserCartInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 @author EddieZhang
 @create 2023-02-14 11:02 PM
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    UserCartInterceptor userCartInterceptor;


    /**
     * TODO 在WebMvcConfig中addInterceptors配置拦截器 并指定拦截器拦截的范围
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userCartInterceptor).addPathPatterns("/**");
    }
}
