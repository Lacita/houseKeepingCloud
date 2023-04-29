package com.houseKeeping.controller;

import com.aliyuncs.exceptions.ClientException;
import com.houseKeeping.Log.LogInfo;
import com.houseKeeping.common.config.PatternConfig;
import com.houseKeeping.common.result.Result;
import com.houseKeeping.common.result.ResultUtils;
import com.houseKeeping.pojo.PhoneUsers;
import com.houseKeeping.service.ReceiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

@RestController
@RequestMapping("/receive")
public class ReceiveController {

    @Resource
    private ReceiveService receiveService;

    @LogInfo(module = "登录模块", description = "发送短信接口")
    @RequestMapping(value = "/send/login/message", method = RequestMethod.POST)
    public Result<String> sendLoginMessage(@RequestParam("phone") String phone, @RequestParam("timestamp") Long timestamp) throws ClientException {
        return receiveService.sendLoginMessage(phone, timestamp);
    }

    @LogInfo(module = "登录模块", description = "短信登录接口")
    @RequestMapping(value = "/message/login", method = RequestMethod.POST)
    public Result<String> loginByMessage(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        return receiveService.loginByMessage(phone, code);
    }

    @LogInfo(module = "接单用户状态调整", description = "接单用户状态调整接口")
    @RequestMapping(value = "/user/updatePhoneUserStatus", method = RequestMethod.POST)
    public Result<String> updatePhoneUserStatus(@RequestBody PhoneUsers phoneUsers) {
        if (StringUtils.isBlank(phoneUsers.getId())) {
            return ResultUtils.ERROR("ID不能为空");
        }
        return receiveService.updatePhoneUserStatus(phoneUsers.getId(), phoneUsers.getUserStatus());
    }

    @LogInfo(module = "逻辑删除接单用户", description = "逻辑删除接单用户接口")
    @RequestMapping(value = "/user/delLogicalPhoneUser", method = RequestMethod.POST)
    public Result<String> delLogicalPhoneUser(@RequestBody PhoneUsers phoneUsers) {
        if (StringUtils.isBlank(phoneUsers.getId())) {
            return ResultUtils.ERROR("ID不能为空");
        }
        return receiveService.delLogicalPhoneUser(phoneUsers.getId(), phoneUsers.getIsDelete());
    }

    @LogInfo(module = "注册手机用户", description = "注册手机用户接口")
    @RequestMapping(value = "/register/phone/users", method = RequestMethod.POST)
    public Result<String> registerPhoneUsers(@RequestBody PhoneUsers phoneUsers) {
        String username = phoneUsers.getUserName();
        String phone = phoneUsers.getPhone();
        String confirmPhone = phoneUsers.getConfirmPhone();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(phone) || StringUtils.isBlank(confirmPhone)) {
            return ResultUtils.ERROR("参数异常");
        }
        if (PatternConfig.isValidPhoneNumber(phone)) {
            return ResultUtils.ERROR("手机格式异常");
        }
        if (!phone.equals(confirmPhone)) {
            return ResultUtils.ERROR("两次手机输入不一致");
        }
        return receiveService.registerPhoneUser(username, phone);
    }

    @LogInfo(module = "查询接单用户", description = "查询接单用户接口")
    @RequestMapping(value = "/queryByPhone", method = RequestMethod.POST)
    public Result<String> queryByPhone(@RequestParam("phone") String phone) {
        return receiveService.queryByPhone(phone);
    }

    @LogInfo(module = "加载接单用户", description = "加载接单用户接口")
    @RequestMapping(value = "/selectPhoneUsers/start={start}&size={size}", method = RequestMethod.GET)
    public Result<HashMap<String, Object>> selectPhoneUsers(@PathVariable("start") Integer start, @PathVariable("size") Integer size) {
        return receiveService.selectPhoneUsers(start, size);
    }

    @LogInfo(module = "根据条件搜索接单用户", description = "根据条件搜索接单用户接口")
    @RequestMapping(value = "/selectPhoneUsersByCondition/start={start}&size={size}/username={username}/status={status}", method = RequestMethod.GET)
    public Result<HashMap<String, Object>> selectPhoneUsersByCondition(@PathVariable("start") Integer start, @PathVariable("size") Integer size,
                                                                       @PathVariable("username") String username,
                                                                       @PathVariable("status") String status) {
        return receiveService.selectPhoneUsersByCondition(start, size, username, status);
    }

}
