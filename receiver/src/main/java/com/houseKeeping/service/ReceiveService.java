package com.houseKeeping.service;

import com.aliyuncs.exceptions.ClientException;
import com.houseKeeping.common.result.Result;

import java.util.HashMap;

public interface ReceiveService {

    Result<String> sendLoginMessage(String phone, Long timestamp) throws ClientException;

    Result<String> loginByMessage(String phone, String code);

    Result<String> registerPhoneUser(String userName, String phone);

    Result<String> queryByPhone(String phone);

    Result<HashMap<String, Object>> selectPhoneUsers(Integer start, Integer size);

    Result<String> updatePhoneUserStatus(String id, byte userStatus);

    Result<String> delLogicalPhoneUser(String id, byte isDelete);

    Result<HashMap<String, Object>> selectPhoneUsersByCondition(Integer start, Integer size, String username, String status);

}
