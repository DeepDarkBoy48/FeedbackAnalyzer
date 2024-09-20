package com.chenyangxu.FeedbackAnalyzer.interceptors;

import com.chenyangxu.FeedbackAnalyzer.utils.JwtUtil;
import com.chenyangxu.FeedbackAnalyzer.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class TeacherInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头中的令牌
        String token = request.getHeader("Authorization");
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            //业务数据存到treadlocal中
            ThreadLocalUtil.set(claims);
            Integer role = (int) claims.get("role");
//            System.out.println(role);
            //0 student 1 teacher
            if (role==0){
                    response.setStatus(403);
                    return false;
            }
            System.out.println();
            return true;//放行

        } catch (Exception e) {
            //打印异常信息
            e.printStackTrace();
            //http响应状态码为401
            response.setStatus(401);
            return false;//不放行
        }
    }
    //视图渲染完毕后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}