package com.eddie.mall_authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.eddie.common.constant.AuthServerConstant;
import com.eddie.common.exception.BizCodeEnum;
import com.eddie.common.utils.R;
import com.eddie.mall_authserver.feign.MemberOpenFeign;
import com.eddie.mall_authserver.feign.ThirdSmsOpenFeign;
import com.eddie.mall_authserver.vo.UserLoginVo;
import com.eddie.mall_authserver.vo.UserRegisterVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 @author EddieZhang
 @create 2023-02-14 10:44 PM
 */
@Controller
public class LoginController {
    @Autowired
    ThirdSmsOpenFeign thirdSmsOpenFeign;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MemberOpenFeign memberOpenFeign;

    /**
     * 发送 手机验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {

        //TODO 解决接口防刷

        String value = (String) redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(value)) {//若redis中已经存在验证码了
            //验证码获取后的60s内不能再次获取
            if ((System.currentTimeMillis() - Long.parseLong(value.split("_")[1])) <= 60000L) {
                return R.error(
                        BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),
                        BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        //验证码再次校验（使用redis）
        redisTemplate.opsForValue().set(
                AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
                "1234" + "_" + System.currentTimeMillis(),//在redis中缓存验证码的值的时候顺便配上系统时间 防止60s内再次有请求进来发送验证码
                10,
                TimeUnit.MINUTES);
        return thirdSmsOpenFeign.sendCode(phone);
    }

    /**
     *
     * @param vo
     * @param redirectAttributes TODO 使用RedirectAttributes 当controller层需要重定向到指定页面时,携带数据
     *                           将设置的属性放到 **session** 中，session 中的属性在跳到页面后**马上销毁**
     *                           TODO 待解决分布式下session的问题
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        //后端的属性校验
        if (result.hasErrors()) {
            //将获取到的错误信息的集合 使用stream流的形式收集成map 以错误的字段为key 错误的信息为value
            Map<String, String> errorMap = result.getFieldErrors()
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    FieldError::getField,
                                    FieldError::getDefaultMessage));
            //将校验的错误信息封装到model的attribute中供前端使用
            redirectAttributes.addFlashAttribute("errors", errorMap);
            return "redirect:http://auth.zhangjinhao.com/regist";//TODO 请求重定向到注册页面 防止表单重复提交
        }

        //真正的注册 调用远程服务进行注册
        //验证验证码已经过期
        String code = (String) redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isNotEmpty(code)) {
            //验证码有效期内 判断验证码是否正确
            if (code.split("_")[0].equals(vo.getCode())) {
                //验证码正确(验证码一旦验证通过即可删除验证码)
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());//删除验证码
                //远程调用member服务的注册方法
                R register = memberOpenFeign.register(vo);
                if (register.getCode() == 0) {//注册服务成功完成
                    //跳转至登录界面
                    return "redirect:http://auth.zhangjinhao.com/login";
                } else {//失败
                    Map<String, String> errorMap = new HashMap<>();
                    //从远程服务调用的返回结果中获取调用失败的信息
                    errorMap.put("msg", register.getData("msg", new TypeReference<String>() {
                    }));
                    return "redirect:http://auth.zhangjinhao.com/regist";
                }
            } else {//验证码错误
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("code", "验证码输出错误");
                redirectAttributes.addFlashAttribute("errors", errorMap);
                return "redirect:http://auth.zhangjinhao.com/regist";
            }
        } else {//验证码失效 重新进入注册页面
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("code", "验证码已经过有效期");
            redirectAttributes.addFlashAttribute("errors", errorMap);
            return "redirect:http://auth.zhangjinhao.com/regist";
        }
    }

    @PostMapping("/loginMethod")
    public String loginMethod(UserLoginVo vo) {
        //远程调用member服务进行登录信息验证
        R login = memberOpenFeign.login(vo);
        if (login.getCode() != 0) {//调用远程服务进行登录不成功 重新进入到登录页面
            Map<String, String> errorMap = new HashMap<>();
            //从远程服务调用的返回结果中获取调用失败的信息
            errorMap.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            return "redirect:http://auth.zhangjinhao.com/login";
        } else {//成功 重定向到商城首页
            return "redirect:http://zhangjinhao.com";
        }
    }

}
