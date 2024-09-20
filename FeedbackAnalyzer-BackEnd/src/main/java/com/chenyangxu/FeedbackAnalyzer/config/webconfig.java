package com.chenyangxu.FeedbackAnalyzer.config;

import com.chenyangxu.FeedbackAnalyzer.interceptors.LoginInterceptor;
import com.chenyangxu.FeedbackAnalyzer.interceptors.StudentInterceptor;
import com.chenyangxu.FeedbackAnalyzer.interceptors.TeacherInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class webconfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private StudentInterceptor StudentInterceptor;
    @Autowired
    private TeacherInterceptor teacherInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //登录接口与注册接口不拦截
        registry.addInterceptor(loginInterceptor).excludePathPatterns("/user/login","/user/register","/student/**","/teacher/**");
        registry.addInterceptor(StudentInterceptor).addPathPatterns("/student/**");
        registry.addInterceptor(teacherInterceptor).addPathPatterns("/teacher/**");
    }


}
