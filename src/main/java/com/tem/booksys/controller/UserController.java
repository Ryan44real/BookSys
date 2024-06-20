package com.tem.booksys.controller;

import com.tem.booksys.entiy.*;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.UserService;
import com.tem.booksys.utils.JwtUtil;
import com.tem.booksys.utils.Md5Util;
import com.tem.booksys.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/check")
    public Result check(){
        return Result.success();
    }
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$")String password,
                           @RequestParam("mail") @Email String mail,@RequestParam("code") String code,@RequestParam(value = "type",required = false)Integer type){

        //查询用户是否存在
        User u = userService.findByUserName(username);
        if (u==null){
            //从redis中获取相同的token
            ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
            String redis = operations.get(mail);
            if (redis == null) return Result.error("验证码已经过期");
            if (!redis.equals(code)) return Result.error("验证码错误");
            userService.register(username,password,mail,type);
            return Result.success();
        }else {
            return Result.error("用户名已经占用了");
        }
    }
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$")String password) {
        //根据用户名查找用户
        User u = userService.findByUserName(username);
//        ValueOperations<String,String> operations1 = stringRedisTemplate.opsForValue();
//        String id = String.valueOf(u.getId());
//        String result = operations1.get(id);
//        if (result != null) return Result.error("该用户已经登录");
        //判断用户是否存在
        if (u == null) {
            return Result.error("该用户不存在");
        } else {
            //判断用户密码是否正确
            if (Md5Util.getMD5String(password).equals(u.getPassword())){
                Map<String,Object>  claims = new HashMap<>();
                claims.put("id",u.getId());
                claims.put("username",u.getUsername());
                claims.put("userState",u.getState());
                claims.put("userType",u.getType());
                String token = JwtUtil.genToken(claims);
                //把token存储到redis中
                ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
                operations.set(String.valueOf(u.getId()),token,1, TimeUnit.HOURS);
//                operations.set(token,token,1, TimeUnit.HOURS);
                return Result.success(String.valueOf(u.getId()));
            }else {
                return Result.error("密码错误");
            }
        }
    }
    
    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader(name = "Authorization") String token*/){

        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        return Result.success(userService.findByUserName(username));

    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user){
//        System.out.println(user);
         userService.update(user);
//        System.out.println("HHHHH");

         return Result.success();
    }
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        System.out.println(avatarUrl);
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token){
//        1.校验参数
        String oldPassword = params.get("oldpassword");
        System.out.println(oldPassword);

        String newPassword = params.get("password");
        System.out.println(newPassword);
        String rePassword = params.get("repassword");
        System.out.println(rePassword);
        if (!StringUtils.hasLength(oldPassword) || !StringUtils.hasLength(newPassword) || !StringUtils.hasLength(rePassword)){
            return Result.error("缺少必要的参数");     //保证参数都到位
        }
        Map<String,Object> o = ThreadLocalUtil.get();
        //  检验密码是否正确

        if (!Md5Util.checkPassword(oldPassword,userService.findByUserName((String) o.get("username")).getPassword())){
            return Result.error("原密码错误");
        }

        //检验二次密码输入是否一致
        if (!rePassword.equals(newPassword)){
            return Result.error("两次密码不一样");
        }

        if (!newPassword.matches(("^\\S{5,16}"))){
            return Result.error("新密码要在5-16位");
        }
//        2.调用Service完成密码更新
        userService.updatePwd(newPassword);
        //删除原来的Token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();
    }

    // 随机验证码
    public static String generateSixDigitCode() {
        String digits = "0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(digits.charAt(random.nextInt(digits.length())));
        }

        return code.toString();
    }

    @Autowired
    private JavaMailSender sender;

    @GetMapping("/sendmail")
    public Result sendMail(@Email String mail){
        SimpleMailMessage message = new SimpleMailMessage();
        // 发送邮件的标题
        message.setSubject("【验证码】");
        String s = generateSixDigitCode();
        // 发送邮件的内容
        message.setText("你的验证码为" + s +
                "如非本人操作，请忽略此邮件，由此给您带来的不便请谅解!"
        );
        // 指定要接收邮件的用户邮箱账号
        message.setTo(mail);
        // 发送邮件的邮箱账号
        message.setFrom("lsj18938740943@163.com");
        // 调用send方法发送邮件即可
        sender.send(message);
        ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
        operations.set(mail,s,90, TimeUnit.SECONDS);
        return Result.success();
    }

    @Autowired
    private UserMapper userMapper;
    @GetMapping("/userInfoForborrow")
    public Result<Map<String,Integer>> userInfoForBorrow(String userId){
        Integer res = userMapper.userInfoForBorrow(userId);
        Integer res2 = userMapper.getOverdue(userId);
        Map<String,Integer> map = new HashMap<>();
        if (res == null) res=0;
        if (res2 == null) res2=0;
        map.put("borrowNum",res);
        map.put("overdueNum",res2);
        return Result.success(map);
    }

    @GetMapping("/getUserList")
    public  Result<PageBean<User>> getUserList(Integer pageNum, Integer pageSize,
                                               @RequestParam(required = false) String username,
                                               @RequestParam(required = false) Integer state)
    {
        PageBean<User> pb = userService.list(pageNum,pageSize,username,state);
        return Result.success(pb);
    }


    @PatchMapping("/editUser")
    public Result editUser(@RequestBody Map<String,String> map){
        String id = map.get("id");
//        System.out.println(id);
        String nickname = map.get("nickname");
        String password = map.get("password");
        String repassword = map.get("repassword");
        System.out.println(password);
        System.out.println(repassword);
        if (password == null || nickname != null)
        { userMapper.updateNickname(nickname, Integer.valueOf(id));
        System.out.println(1);}
        if (password !=null) {userService.updatePwds(password, id);}
        return Result.success();
    }

    @GetMapping("/deleteUserMsg")
    public Result deleteUser(){
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        System.out.println(id);
        userMapper.deleteUserMsg(id);
        return Result.success();
    }

    @GetMapping("/upgradeUserState")
    public Result upgradeUserState(String id,Integer state){
        userMapper.updateState(id,state);
        return Result.success();
    }

    @GetMapping("/deleteUser")
    public Result deleteUser(String id){
        System.out.println("test"+id);
        userMapper.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/getUserNumService")
    public Result getUserNumService(){
        Integer res = userService.getUserNumService();
        return Result.success(res);
    }
//    @GetMapping("/breakUser")
////    public Result breakUser(String id){
//        Boolean res = userService.breakUserService(id);
//        if (res) {return Result.success("踢出成功");}
//        else return Result.error("踢出失败");
//
//    }

    @PostMapping("/editPswByEmail")
    public Result editPswByEmail(@RequestParam("username")String username,@RequestParam("mail") @Email String mail,@RequestParam("code") String code,@RequestParam("password") String password){
        String userMail =  userMapper.getEmailByUsername(username);
        if (userMail.equals(mail)) {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redis = operations.get(mail);
            if (redis == null) return Result.error("验证码已经过期");
            if (!redis.equals(code)) return Result.error("验证码错误");
//            userService.updatePwd(password);
            String pwd = Md5Util.getMD5String(password);
            userMapper.updatePswByEmail(username,mail,pwd);
            return Result.success();
        }else return Result.error("邮箱不匹配");

    }
}
