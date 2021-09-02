package com.sakana.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.sakana.blog.dao.pojo.SysUser;
import com.sakana.blog.service.LoginService;
import com.sakana.blog.service.SysUserService;
import com.sakana.blog.utils.JWTUtils;
import com.sakana.blog.vo.ErrorCode;
import com.sakana.blog.vo.Result;
import com.sakana.blog.vo.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    private static final String slat = "mszlu!@#";
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 1、检查参数是否合法
         * 2、根据用户名和密码去user表中查询是否存在
         * 3、如果不存在 那么登录失败
         * 4、如果存在使用jwt生成token 返回给前端
         * 5、token放入redis中，redis token：user信息 设置过期时间
         * （登陆认证的时间 先认证token字符串是否合法，然后去redis认证是否存在）
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        //如果account或者password是空的，就返回error
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        //md5Hex方法加密
        password = DigestUtils.md5Hex(password + slat);
        SysUser sysUser = sysUserService.findUser(account, password);
        if (sysUser == null) {
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(sysUser), 1, TimeUnit.DAYS);
        return Result.success(token);
    }

    //check的结果返回的是null
    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        //解析token
        Map<String, Object> stringStringMap = JWTUtils.checkToken(token);
        //如果失败
        if (stringStringMap == null) {
            return null;
        }
        //如果成功，去redis中寻找
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        //redis中不存在
        if (StringUtils.isBlank(userJson)) {
            return null;
        }
        //用json解析为user对象
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        return sysUser;
    }

    @Override
    public Result logout(String token) {
        //删除token
        redisTemplate.delete("TOKEN_" + token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 1、判断参数是否合法
         * 2、判断账户是否存在，存在的话就返回账户已经被注册
         * 3、如果账户不存在，注册用户
         * 4、生成token
         * 5、存入redis并返回
         * 6、注意 加上事物，一旦中金啊的任何过程出现问题，那么注册的用户需要回滚
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        //如果账户或者密码或者昵称为空
        //那么就返回error和提示语
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser = sysUserService.findUserByAccount(account);
        //如果这个账号已经存在了
        if (sysUser != null) {
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(), ErrorCode.ACCOUNT_EXIST.getMsg());
        }
        //现在账号是存在的
        sysUser=new SysUser();
        //一些set操作
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        //登录的时候是加密了的，注册的时候也加密了
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        //暂时用不到的一些属性
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        //save一下这个用户
        this.sysUserService.save(sysUser);
        //login方法的最后两行（登陆一下）
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(sysUser), 1, TimeUnit.DAYS);
        return Result.success(token);
    }
}
