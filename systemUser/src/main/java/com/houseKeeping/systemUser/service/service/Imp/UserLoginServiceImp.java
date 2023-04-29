package com.houseKeeping.systemUser.service.service.Imp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houseKeeping.common.config.Utils;
import com.houseKeeping.common.constant.MessageConstant;
import com.houseKeeping.common.result.Result;
import com.houseKeeping.common.result.ResultUtils;
import com.houseKeeping.systemUser.Exception.MyException;
import com.houseKeeping.systemUser.config.config.TokenUtils;
import com.houseKeeping.systemUser.mapper.SystemUser;
import com.houseKeeping.systemUser.pojo.pojo.SysUser;
import com.houseKeeping.systemUser.service.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class UserLoginServiceImp implements UserLoginService {

    @Resource
    private SystemUser userLogin;

    public static final String RESET_PASSWORD = "123456";

    /*
    系统用户登录
     */
    @Override
    public Result<String> userLoginQuery(SysUser SysUser) {
        String username = SysUser.getUsername();
        String password = SysUser.getPassword();
        if (username.equals("") || password.equals("")) {
            return ResultUtils.ERROR("账号密码不能为空");
        }
        String md5CryptPassword = Utils.Md5CryptPassword(password);
        QueryWrapper<SysUser> queryCondition = new QueryWrapper<SysUser>();
        queryCondition.eq("username", username);
        queryCondition.eq("password", md5CryptPassword);
        SysUser sysUser = userLogin.selectOne(queryCondition);
        if (sysUser == null) {
            return ResultUtils.ERROR("账号密码错误");
        }
        long delete = SysUser.getIsDelete();
        long userStatus = SysUser.getUserStatus();
        if (delete == MessageConstant.USER_STATUS_BAN || userStatus == MessageConstant.USER_STATUS_BAN) {
            log.info("[INFO] {} 当前查询用户为:{}", Utils.getSysTime(), username);
            return ResultUtils.ERROR("用户无效");
        }
        try {
            String token = TokenUtils.sign(sysUser);
            return ResultUtils.SUCCESS(token);
        } catch (Exception e) {
            return ResultUtils.ERROR("token返回异常");
        }
    }

    /*
    注册系统用户
     */
    @Override
    @Transactional
    public Result<String> registerSystemUser(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return ResultUtils.ERROR("用户名跟密码不能为空");
        }
        Map<String, Object> queryCondition = new HashMap<>(1);
        queryCondition.put("username", username);
        List<SysUser> sysUserList = userLogin.selectByMap(queryCondition);
        if (sysUserList.size() > 0) {
            return ResultUtils.ERROR("用户已存在！");
        }
        String md5CryptPassword = Utils.Md5CryptPassword(password);
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(md5CryptPassword);
        int register = userLogin.insert(sysUser);
        if (register > 0) {
            return ResultUtils.SUCCESS("注册成功！");
        }
        return ResultUtils.ERROR("注册失败");
    }

    @Override
    public Result<HashMap<String, Object>> selectSystemUsers(int offset, int size) {
        Page<SysUser> queryPage = new Page<>(offset, size);
        try {
            IPage<SysUser> sysUserIPage = userLogin.selectPage(queryPage, null);
            List<SysUser> records = sysUserIPage.getRecords();
            long pageTotal = sysUserIPage.getTotal();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("data", records);
            hashMap.put("total", pageTotal);
            return ResultUtils.SUCCESS(hashMap);
        } catch (Exception e) {
            log.error("系统用户查询异常，异常信息为:{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateUserStatus(String id, byte status) {
        UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", id).set("user_status", status);
        int update = userLogin.update(null, updateWrapper);
        if (update > 0) {
            return ResultUtils.SUCCESS("状态更新成功");
        }
        return ResultUtils.ERROR("状态更新失败");
    }

    @Override
    @Transactional
    public Result<String> delLogicalUser(String userId, byte userStatus) {
        UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId).set("isDelete", userStatus);
        int logicalDelete = userLogin.update(null, updateWrapper);
        if (logicalDelete > 0) {
            return ResultUtils.SUCCESS("用户删除成功");
        }
        return ResultUtils.ERROR("用户删除失败");
    }

    @Override
    @Transactional
    public Result<String> resetPassword(String userId) {
        String md5CryptPassword = Utils.Md5CryptPassword(RESET_PASSWORD);
        UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId).set("password", md5CryptPassword);
        int resetUserPassword = userLogin.update(null, updateWrapper);
        if (resetUserPassword > 0) {
            return ResultUtils.SUCCESS("密码重置成功");
        }
        return ResultUtils.ERROR("密码重置失败");
    }

    @Override
    @Transactional
    public Result<String> addUser(SysUser SysUser) {
        if (StringUtils.isBlank(SysUser.getUsername()) || StringUtils.isBlank(SysUser.getPassword())) {
            return ResultUtils.ERROR("参数不能为空");
        }
        if (!SysUser.getPassword().equals(SysUser.getConfirmPassword())) {
            return ResultUtils.ERROR("两次输入密码不一致");
        }
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", SysUser.getUsername());
        List<SysUser> userList = userLogin.selectList(queryWrapper);
        if (userList.size() > 0) {
            return ResultUtils.ERROR("用户已注册");
        }
        String md5CryptPassword = Utils.Md5CryptPassword(SysUser.getPassword());
        SysUser user = new SysUser();
        user.setUsername(SysUser.getUsername());
        user.setPassword(md5CryptPassword);
        int insert = userLogin.insert(user);
        if (insert > 0) {
            return ResultUtils.SUCCESS("添加成功");
        }
        return ResultUtils.ERROR("添加失败");
    }

    @Override
    public Result<HashMap<String, Object>> selectSystemUsersByCondition(Integer start, Integer size, String username, String status) {
        // 定义初始页码
        if (start == null || size == null) {
            return ResultUtils.ERROR(null);
        }
        Page<SysUser> sysUserPage = new Page<>(start, size);
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(username), "username", username);
        queryWrapper.eq(StringUtils.isNotBlank(status), "user_status", status);
        try {
            IPage<SysUser> sysUserIPage = userLogin.selectPage(sysUserPage, queryWrapper);
            List<SysUser> sysUserList = sysUserIPage.getRecords();
            long total = sysUserIPage.getTotal();
            HashMap<String, Object> map = new HashMap<>();
            map.put("data", sysUserList);
            map.put("total", total);
            return ResultUtils.SUCCESS(map);
        } catch (Exception e) {
            log.error("当前用户条件查询异常，异常原因为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


}
