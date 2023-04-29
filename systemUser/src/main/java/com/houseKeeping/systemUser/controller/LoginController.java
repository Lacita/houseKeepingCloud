package com.houseKeeping.systemUser.controller;

import com.houseKeeping.common.result.Result;
import com.houseKeeping.common.result.ResultUtils;
import com.houseKeeping.systemUser.log.LogInfo;
import com.houseKeeping.systemUser.pojo.pojo.SysUser;
import com.houseKeeping.systemUser.service.service.UserLoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

@RestController
@RequestMapping("/systemUser")
public class LoginController {

    @Resource
    private UserLoginService userLoginService;

    @LogInfo(module = "登录模块", description = "登录接口")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result<String> userLogin(@RequestBody SysUser sysUser) {
        return userLoginService.userLoginQuery(sysUser);
    }

    @LogInfo(module = "注册系统用户", description = "注册系统用户接口")
    @RequestMapping(value = "/register/system/users", method = RequestMethod.POST)
    public Result<String> registerSystemUser(@RequestBody SysUser SysUser) {
        String username = SysUser.getUsername();
        String password = SysUser.getPassword();
        return userLoginService.registerSystemUser(username, password);
    }


    @LogInfo(module = "用户状态调整", description = "用户状态调整接口")
    @RequestMapping(value = "/user/updateUserStatus", method = RequestMethod.POST)
    public Result<String> updateUserStatus(@RequestBody SysUser SysUser) {
        if (StringUtils.isBlank(SysUser.getUserId())) {
            return ResultUtils.ERROR("ID不能为空");
        }
        return userLoginService.updateUserStatus(SysUser.getUserId(), SysUser.getUserStatus());
    }


    @LogInfo(module = "新增系统用户", description = "新增系统用户接口")
    @RequestMapping(value = "/user/addUser", method = RequestMethod.POST)
    public Result<String> addUser(@RequestBody SysUser SysUser) {
        return userLoginService.addUser(SysUser);
    }


    @LogInfo(module = "逻辑删除用户", description = "逻辑删除用户接口")
    @RequestMapping(value = "/user/delLogicalUser", method = RequestMethod.POST)
    public Result<String> delLogicalUser(@RequestBody SysUser SysUser) {
        if (StringUtils.isBlank(SysUser.getUserId())) {
            return ResultUtils.ERROR("ID不能为空");
        }
        return userLoginService.delLogicalUser(SysUser.getUserId(), SysUser.getUserStatus());
    }

    @LogInfo(module = "重置用户密码", description = "逻辑删除用户接口")
    @RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST)
    public Result<String> resetPassword(@RequestBody SysUser SysUser) {
        if (StringUtils.isBlank(SysUser.getUserId())) {
            return ResultUtils.ERROR("ID不能为空");
        }
        return userLoginService.resetPassword(SysUser.getUserId());
    }


    @RequestMapping(value = "/selectSystemUsers/start={start}&size={size}", method = RequestMethod.GET)
    public Result<HashMap<String, Object>> selectSystemUsers(@PathVariable("start") Integer start, @PathVariable("size") Integer size) {
        return userLoginService.selectSystemUsers(start, size);
    }

    @LogInfo(module = "根据条件搜索系统用户", description = "根据条件搜索系统用户接口")
    @RequestMapping(value = "/selectSystemUsersByCondition/start={start}&size={size}/username={username}/status={status}", method = RequestMethod.GET)
    public Result<HashMap<String, Object>> selectSystemUsersByCondition(@PathVariable("start") Integer start, @PathVariable("size") Integer size,
                                                                        @PathVariable("username") String username,
                                                                        @PathVariable("status") String status) {
        return userLoginService.selectSystemUsersByCondition(start, size, username, status);
    }

}
