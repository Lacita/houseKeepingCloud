package com.houseKeeping.order.service;

import com.houseKeeping.common.result.Result;
import com.houseKeeping.order.entity.TaskInfo;

import java.util.List;
import java.util.Map;

public interface OrderService {

    public Result<String> releasesNewTask(TaskInfo taskInfo);

    Result<String> receivingOrders(String taskID, String userID);

    Result<String> phoneUserUpdateOrderStatus(String orderUnionId, String status);

    Result<Map<String, Object>> showTasks(Integer page, Integer size);

    Result<List<TaskInfo>> selectTaskByPhoneUsers(String phoneUser, Integer page, Integer size);

    Result<List<TaskInfo>> queryListsByOpenID(String openid, Integer page, Integer size);

    Result<String> finishTaskByPhoneUser(String taskID);

    Result<List<TaskInfo>> queryFinishTaskByPhoneUser(String phoneUserID, Integer page, Integer size);

    TaskInfo getTaskMessage(String taskID);

    Result<String> cancelThaTasks(String taskID);

    Result<List<TaskInfo>> selectFinishOrderByReporter(String openid, Integer page, Integer size);

    Result<List<TaskInfo>> SearchTaskByName(String content, String orderStatus, String user, Integer current, Integer size);

    Result<List<TaskInfo>> SearchMyOrderTaskByName(String content, String orderStatus, String user, Integer current, Integer size);

    Result<List<TaskInfo>> SearchMyFinishOrderTaskByName(String content, String orderStatus, String user);

    Result<List<TaskInfo>> SearchMyOrderTaskByReporter(String content, String orderStatus, String user, Integer current, Integer size);

    Result<List<TaskInfo>> SearchMyFinishOrderTaskByReporter(String content, String orderStatus, String user,Integer current, Integer size);

    Result<String> finishOrderByAdmin(String id, String status);

    Result<Map<String, Object>> SearchTaskInfoByCondition(String content, String orderStatus, Integer page, Integer size);

    Result<Map<String, Object>> SearchTaskInfoFinish(Integer page, Integer size);

    Result<Map<String, Object>> showReadyTasks(Integer page, Integer size);

    String syncData();

}
