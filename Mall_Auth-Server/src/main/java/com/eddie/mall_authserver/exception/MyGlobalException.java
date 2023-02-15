package com.eddie.mall_authserver.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 @author EddieZhang
 @create 2023-02-15 3:36 PM
 */
@ControllerAdvice
public class MyGlobalException extends RuntimeException{
//    /**
//     * TODO Validation全局异常处理
//     * @param e
//     * @param redirectAttributes
//     * @return
//     */
//    @ExceptionHandler(value = MethodArgumentNotValidException.class)//指定要进行处理的异常类型
//    public String validationExceptionHandle(MethodArgumentNotValidException e,RedirectAttributes redirectAttributes){
//        BindingResult bindingResult = e.getBindingResult();//获取参数验证的异常结果
//        Map<String, String> errorMap = bindingResult.getFieldErrors()
//                .stream()
//                .collect(
//                        Collectors.toMap(
//                                FieldError::getField,
//                                FieldError::getDefaultMessage));
//        //将校验的错误信息封装到model的attribute中供前端使用
//        redirectAttributes.addFlashAttribute("errors", errorMap);
//        return "redirect:http://auth.zhangjinhao.com/regist";
//    }
}
