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
     * TODO 视图处理器（只负责跳转页面 没有别的业务操作的 进行视图跳转）
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("cartList");
        registry.addViewController("/success").setViewName("success");
    }

    /**
     * TODO 在WebMvcConfig中addInterceptors配置拦截器 并指定拦截器拦截的范围
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserCartInterceptor()).addPathPatterns("/**");
    }
}
