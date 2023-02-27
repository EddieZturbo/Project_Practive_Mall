package eddie.com.mall_cart.config;

import eddie.com.mall_cart.interceptor.UserCartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 @author EddieZhang
 @create 2023-02-14 11:02 PM
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * TODO 在WebMvcConfig中addInterceptors配置拦截器 并指定拦截器拦截的范围
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserCartInterceptor()).addPathPatterns("/**");
    }
}
