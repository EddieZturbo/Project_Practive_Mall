package com.eddie.mall_seckill.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.spring.webflux.exception.SentinelBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.eddie.common.exception.BizCodeEnum;
import com.eddie.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 @author EddieZhang
 @create 2023-03-25 1:26 PM
 */
@Configuration//TODO 自定义流控响应数据
public class SentinelGlobalConfig implements UrlBlockHandler {

    @Override
    public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {

        R error = R.error(BizCodeEnum.CURRENT_LIMITING_EXCEPTION.getCode(), BizCodeEnum.CURRENT_LIMITING_EXCEPTION.getMessage());
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(error));
        /**
         BlockException 异常接口，其子类为Sentinel五种规则异常的实现类
         AuthorityException 授权异常
         DegradeException 降级异常
         FlowException 限流异常
         ParamFlowException 参数限流异常
         SystemBlockException 系统负载异常
         String msg = null;
         if (e instanceof FlowException) {
         msg = "限流了";
         } else if (e instanceof DegradeException) {
         msg = "降级了";
         } else if (e instanceof ParamFlowException) {
         msg = "热点参数限流";
         } else if (e instanceof SystemBlockException) {
         msg = "系统规则（负载/...不满足要求）";
         } else if (e instanceof AuthorityException) {
         msg = "授权规则不通过";
         }
         */
    }
}
