package com.chenyangxu.FeedbackAnalyzer.controller;

import com.chenyangxu.FeedbackAnalyzer.pojo.entity.Result;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.User;
import com.chenyangxu.FeedbackAnalyzer.pojo.vo.UserVo;
import com.chenyangxu.FeedbackAnalyzer.service.UserService;
import com.chenyangxu.FeedbackAnalyzer.utils.JwtUtil;
import com.chenyangxu.FeedbackAnalyzer.utils.Md5Util;
import com.chenyangxu.FeedbackAnalyzer.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;

    /**
     * register
     *
     * @param username
     * @param password
     * @param role
     * @return
     */
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$", message = "wrong username") String username,
                           @Pattern(regexp = "^\\S{5,16}$", message = "wrong password") String password,
                           @NotNull(message = "Role cannot be null") @Min(value = 0, message = "Role must be 0 or 1") @Max(value = 1, message = "Role must be 0 or 1") Integer role) {
        log.info("username:{},password:{},role:{}", username, password,role);
        //查询用户
        User u = userService.findByUserName(username);
        if (u == null) {
            //注册
            userService.register(username, password, role);
            return Result.success();
        } else {
            //注册
            return Result.error("user has been taken");
        }
    }

    /**
     * login
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username,
                                @Pattern(regexp = "^\\S{5,16}$") String password) {
        log.info("username:{},password:{}", username, password);
        //根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        //判断用户是否存在
        if (loginUser == null) {
            return Result.error("wrong username");
        }
        //判断密码是否正确
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            //登录成功生成token
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            claims.put("role", loginUser.getRole());
            String token = JwtUtil.genToken(claims);
//            //把token存储到redis中
//            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
//            operations.set(token,token,1, TimeUnit.HOURS);
            return Result.success(token);
        }
        return Result.error("wrong password");
    }


    //1到请求头中获取userid
//    @GetMapping("/userInfo")
//    public Result<UserVo> userInfo(@RequestHeader(name = "Authorization") String token) {
//        //根据用户名查询用户
//        Map<String, Object> map = JwtUtil.parseToken(token);
//        String username = (String) map.get("username");
//        User user = userService.findByUserName(username);
//        UserVo userVo = new UserVo();
//        BeanUtils.copyProperties(user, userVo);
//        return Result.success(userVo);
//    }


    //2treadlocal获取userid
    /**
     * 获取所有用户信息
     * @return
     */
    @GetMapping("/userInfo")
    public Result<UserVo> userInfo() {
        //根据用户名查询用户
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return Result.success(userVo);
    }

    /**
     * 更新用户信息 @Validated校验数据
     * @param user
     * @return
     */
    @PutMapping("/update")
    public  Result update(@RequestBody @Validated User user){
        userService.update(user);
        return Result.success();
    }

    /**
     * 更新用户头像
     * @param avatarUrl
     * @return
     */
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    /**
     * 更换密码
     * @param params
     * @param token
     * @return
     */
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String, String> params,@RequestHeader("Authorization") String token) {
        //1.校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error("缺少必要的参数");
        }

        //原密码是否正确
        //调用userService根据用户名拿到原密码,再和old_pwd比对
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUserName(username);
        if (!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码填写不正确");
        }

        //newPwd和rePwd是否一样
        if (!rePwd.equals(newPwd)){
            return Result.error("两次填写的新密码不一样");
        }

        //2.调用service完成密码更新
        userService.updatePwd(newPwd);
        //删除原来的token
//        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
//        operations.getOperations().delete(token);
        return Result.success();
    }
}
