package com.eddie.mall_ware.exception;

import com.eddie.common.exception.BizCodeEnum;
import com.eddie.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * //TODO 集中处理异常
 @author EddieZhang
 @create 2023-01-10 21:37
 */
//@ControllerAdvice
//@ResponseBody
@Slf4j
@RestControllerAdvice(basePackages = {"com.eddie.mall_ware.controller"})//指定要进行统一异常处理的controller所在包
public class GlobalExceptionHandler {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)//指定要进行处理的异常类型
    public R validationExceptionHandle(MethodArgumentNotValidException e) {
        log.error("错误：{}", e.toString());
        BindingResult bindingResult = e.getBindingResult();//获取参数验证的异常结果
        HashMap<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach((item) -> {
            String errorName = item.getField();
            String errorMessage = item.getDefaultMessage();
            map.put(errorName, errorMessage);
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("错误：{}", throwable.toString());
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMessage());
    }

    @ExceptionHandler(value = RuntimeException.class)
    public R handleException(RuntimeException runtimeException) {
        log.error("错误：{}", runtimeException.toString());
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),runtimeException.getMessage());
    }

}
