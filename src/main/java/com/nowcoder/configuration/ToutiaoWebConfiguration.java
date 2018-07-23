package com.nowcoder.configuration;

import com.nowcoder.interceptor.LoginRequiredInterceptor;
import com.nowcoder.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class ToutiaoWebConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注意两个拦截器的注册顺序
        registry.addInterceptor(passportInterceptor);//用户是否注册
        registry.addInterceptor(loginRequiredInterceptor).
                addPathPatterns("/msg/*").addPathPatterns("/like").addPathPatterns("/dislike");;//用户是否有权限打开setting
        super.addInterceptors(registry);

    }
}
