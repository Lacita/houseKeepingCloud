package com.houseKeeping.systemUser.service.service;

import com.houseKeeping.common.result.Result;
import com.houseKeeping.systemUser.pojo.pojo.SysUser;

import java.util.HashMap;

public interface UserLoginService {

    public Result<String> userLoginQuery(SysUser sysUsers);

    Result<String> registerSystemUser(String username, String password);

    Result<HashMap<String, Object>> selectSystemUsers(int offset, int size);

    Result<String> updateUserStatus(String id, byte status);

    Result<String> delLogicalUser(String userId, byte userStatus);

    Result<String> resetPassword(String userId);

    Result<String> addUser(SysUser sysUser);

    Result<HashMap<String, Object>> selectSystemUsersByCondition(Integer start, Integer size, String username, String status);

}
