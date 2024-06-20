package com.tem.booksys.interceptors;

import com.tem.booksys.utils.JwtUtil;
import com.tem.booksys.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate  stringRedisTemplate;
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        try{
            //从redis中获取相同的token
            ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
            String redis = operations.get(token);
            if (redis == null){
                //token失效了
                throw new RuntimeException();
            }
            //验证token
            Map<String,Object> claims = JwtUtil.parseToken(redis);
            //将用户信息放入线程缓存池中方便取出使用
            ThreadLocalUtil.set(claims);
            //放行
            return true;
        }catch (Exception e){
            response.setStatus(401);
            return false;
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
