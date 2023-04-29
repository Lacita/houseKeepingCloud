package com.houseKeeping.service.Imp;


import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houseKeeping.Exception.MyException;
import com.houseKeeping.common.config.PatternConfig;
import com.houseKeeping.common.constant.RedisConstant;
import com.houseKeeping.common.result.Result;
import com.houseKeeping.common.result.ResultUtils;
import com.houseKeeping.config.Utils;
import com.houseKeeping.mapper.ReceiveManager;
import com.houseKeeping.pojo.PhoneUsers;
import com.houseKeeping.service.ReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReceiveServiceImp implements ReceiveService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ReceiveManager receiveManager;

    @Resource
    private Utils utils;

    @Override
    public Result<String> sendLoginMessage(String phone, Long timestamp)  {
        if (StringUtils.isBlank(phone)) {
            return ResultUtils.ERROR("手机号不能为空");
        }
        if (PatternConfig.isValidPhoneNumber(phone)) {
            return ResultUtils.ERROR("手机格式非法");
        }
        // 获取已注册数据库的手机用户
        QueryWrapper<PhoneUsers> usersQueryWrapper = new QueryWrapper<>();
        usersQueryWrapper.eq("phone", phone);
        PhoneUsers phoneUsers = receiveManager.selectOne(usersQueryWrapper);
        if (phoneUsers == null) {
            return ResultUtils.ERROR("用户不存在!");
        }
        // 设置互斥锁，并设置5分钟存活时间，避免用户频繁发送验证码
        Boolean ifAbsentPhoneRecord = redisTemplate.opsForValue().setIfAbsent(RedisConstant.REDIS_SENT_CODE + phone, "1");
        redisTemplate.expire(RedisConstant.REDIS_SENT_CODE + phone, 5L, TimeUnit.MINUTES);
        if (BooleanUtils.isTrue(ifAbsentPhoneRecord)) {
            return ResultUtils.ERROR("验证码获取频繁");
        }
        String numbers = RandomUtil.randomNumbers(6);
        HashMap<String, String> mesCode = new HashMap<>();
        mesCode.put("code", numbers);
        if (!utils.send(mesCode, phone)) {
            return ResultUtils.ERROR("短信发送失败");
        }
        log.info("本次短信登录为：{}", numbers);
        // 通过将短信信息存储进redis中，完成单点登录
        redisTemplate.opsForValue().set(RedisConstant.REDIS_MESSAGE_KEY + phone, numbers, 5, TimeUnit.MINUTES);
        return ResultUtils.SUCCESS(numbers);
    }

    /*
    通过短信信息登录
     */
    @Override
    public Result<String> loginByMessage(String phone, String code) {
        if (PatternConfig.isValidPhoneNumber(phone)) {
            return ResultUtils.ERROR("手机号码非法");
        }
        String redisGetCode = redisTemplate.opsForValue().get(RedisConstant.REDIS_MESSAGE_KEY + phone);
        if (StringUtils.isEmpty(redisGetCode)) {
            return ResultUtils.ERROR("验证码已过期");
        }
        if (!redisGetCode.equals(code)) {
            return ResultUtils.ERROR("验证码不正确");
        }
        QueryWrapper<PhoneUsers> usersQueryWrapper = new QueryWrapper<>();
        usersQueryWrapper.eq("phone", phone);
        PhoneUsers users = receiveManager.selectOne(usersQueryWrapper);
        if (users == null) {
            return ResultUtils.ERROR("用户不存在，请联系系统人员");
        }
        String usersId = users.getId();
        return ResultUtils.SUCCESS(usersId);
    }

       /*
    注册接单员接口
     */

    @Override
    public Result<String> registerPhoneUser(String userName, String phone) {
        if (phone.isEmpty() || userName.isEmpty()) {
            return ResultUtils.ERROR("用户名及手机号码不能为空");
        }
        if (PatternConfig.isValidPhoneNumber(phone)) {
            return ResultUtils.ERROR("用户手机号码不合法");
        }
        QueryWrapper<PhoneUsers> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        PhoneUsers phoneUsers = receiveManager.selectOne(queryWrapper);
        if (phoneUsers != null) {
            return ResultUtils.ERROR("手机已注册");
        }
        PhoneUsers users = new PhoneUsers();
        users.setPhone(phone);
        users.setUserName(userName);
        int registerPhoneUsers = receiveManager.insert(users);
        if (registerPhoneUsers > 0) {
            return ResultUtils.SUCCESS("接单人员注册成功！");
        }

        return ResultUtils.ERROR("注册失败，请联系运维人员");
    }


    @Override
    public Result<String> queryByPhone(String phone) {
        QueryWrapper<PhoneUsers> usersQueryWrapper = new QueryWrapper<PhoneUsers>().eq("phone", phone);
        PhoneUsers phoneUsers = receiveManager.selectOne(usersQueryWrapper);
        if (null == phoneUsers) {
            return ResultUtils.ERROR("用户不存在");
        }
        return ResultUtils.SUCCESS(phoneUsers.getId());

    }

    @Override
    public Result<HashMap<String, Object>> selectPhoneUsers(Integer start, Integer size) {
        Page<PhoneUsers> usersPage = new Page<>(start, size);
        try {
            IPage<PhoneUsers> phoneUsersIPage = receiveManager.selectPage(usersPage, null);
            return getHashMapPageResult(phoneUsersIPage);
        } catch (Exception e) {
            log.error("当前查询异常为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


    @Override
    public Result<String> updatePhoneUserStatus(String id, byte userStatus) {
        UpdateWrapper<PhoneUsers> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("user_status", userStatus);
        int update = receiveManager.update(null, updateWrapper);
        if (update > 0) {
            return ResultUtils.SUCCESS("状态更新成功");
        }
        return ResultUtils.ERROR("状态更新失败");
    }


    @Override
    public Result<String> delLogicalPhoneUser(String id, byte isDelete) {
        UpdateWrapper<PhoneUsers> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("isDelete", isDelete);
        int update = receiveManager.update(null, updateWrapper);
        if (update > 0) {
            return ResultUtils.SUCCESS("用户删除成功");
        }
        return ResultUtils.ERROR("用户删除失败");
    }


    @Override
    public Result<HashMap<String, Object>> selectPhoneUsersByCondition(Integer start, Integer size, String username, String status) {
        if (start == null || size == null) {
            return ResultUtils.ERROR(null);
        }
        Page<PhoneUsers> phoneUsersPage = new Page<>(size, size);
        QueryWrapper<PhoneUsers> phoneUsersQueryWrapper = new QueryWrapper<>();
        phoneUsersQueryWrapper.like(StringUtils.isNotBlank(username), "username", username);
        phoneUsersQueryWrapper.eq(StringUtils.isNotBlank(status), "user_status", status);
        try {
            IPage<PhoneUsers> phoneUsersIPage = receiveManager.selectPage(phoneUsersPage, phoneUsersQueryWrapper);
            return getHashMapPageResult(phoneUsersIPage);
        } catch (Exception e) {
            log.error("条件分页查询异常，异常为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }

    /**
     * 根据IPage获取数据以及total
     *
     * @param phoneUsersIPage 页码对象
     * @return map
     */
    private Result<HashMap<String, Object>> getHashMapPageResult(IPage<PhoneUsers> phoneUsersIPage) {
        List<PhoneUsers> records = phoneUsersIPage.getRecords();
        long total = phoneUsersIPage.getTotal();
        HashMap<String, Object> map = new HashMap<>();
        map.put("data", records);
        map.put("total", total);
        return ResultUtils.SUCCESS(map);
    }


}
