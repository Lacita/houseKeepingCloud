package com.houseKeeping.order.controller;

import com.aliyun.oss.common.comm.ResponseMessage;
import com.houseKeeping.common.result.Result;
import com.houseKeeping.order.Log.LogInfo;
import com.houseKeeping.order.entity.TaskInfo;
import com.houseKeeping.order.service.OrderService;
import com.houseKeeping.order.service.PicUploadService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService initiatedTaskServices;

    @Resource
    private PicUploadService picUploadService;

    /***
     * 报单人报单接口
     * @param taskInfo 传入订单信息
     * @return
     */
    @LogInfo(module = "发布任务", description = "发布任务接口")
    @RequestMapping(value = "/add/task", method = RequestMethod.POST)
    @ApiOperation(value = "发布任务", notes = "发布任务接口")
    public Result<String> resultById(@RequestBody TaskInfo taskInfo) {
        return initiatedTaskServices.releasesNewTask(taskInfo);
    }

    @RequestMapping("/sync")
    public String syncData(){
        return initiatedTaskServices.syncData();
    }


    /***
     * 接单用户接单接口
     * @param taskID 传入任务单ID
     * @param userID 传入接单人ID
     * @return
     */
    @LogInfo(module = "接收订单", description = "接收任务订单")
    @ApiOperation(value = "接收订单", notes = "接收任务订单")
    @RequestMapping(value = "/receiving/orders", method = RequestMethod.POST)
    public Result<String> receivingOrders(@RequestParam(value = "taskID", required = true) String taskID,
                                          @RequestParam("userID") String userID) {
        return initiatedTaskServices.receivingOrders(taskID, userID);
    }

    /***
     * 接单用户更新订单状态
     * @param orderUnionId
     * @param status
     * @return
     */
    @ApiOperation(value = "接单用户更新订单", notes = "接单用户更新订单状态")
    @RequestMapping(value = "/phoneUser/updateOrder/status", method = RequestMethod.POST)
    public Result<String> phoneUserUpdateOrderStatus(@RequestParam("orderUnionId") String orderUnionId, @RequestParam("status") String status) {
        return initiatedTaskServices.phoneUserUpdateOrderStatus(orderUnionId, status);
    }

    /***
     * 展示任务列表
     * @param page 前端传入当前page
     * @param size 前端传入每次展示多少数据，默认为10
     * @return
     */
    @ApiOperation(value = "展示工单", notes = "展示工单接口")
    @RequestMapping(value = "/showTasks", method = RequestMethod.POST)
    public Result<Map<String, Object>> showTasks(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "10")
            Integer size) {
        return initiatedTaskServices.showTasks(page, size);
    }

    @ApiOperation(value = "展示待接工单", notes = "展示待接工单接口")
    @RequestMapping(value = "/showReadyTasks", method = RequestMethod.POST)
    public Result<Map<String, Object>> showReadyTasks(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "10")
            Integer size) {
        return initiatedTaskServices.showReadyTasks(page, size);
    }

    /***
     * 展示接单人订单列表展示
     * @param phoneUserID 传入接单人ID
     * @param page 传入前端页码
     * @param size 展示每页多少数据，默认为10
     * @return
     */
    @ApiOperation(value = "根据用户查询订单", notes = "根据用户查询订单接口")
    @RequestMapping(value = "/selectTaskByPhoneUsers", method = RequestMethod.POST)
    public Result<List<TaskInfo>> selectTaskByPhoneUsers(@RequestParam("phoneUserID") String phoneUserID,
                                                         @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                         @RequestParam(value = "sizesize", defaultValue = "10") Integer size) {
        return initiatedTaskServices.selectTaskByPhoneUsers(phoneUserID, page, size);
    }

    /***
     * 查询报单人报单列表
     * @param
     * @return
     */
    @ApiOperation(value = "报单人查询工单", notes = "报单人查询工单")
    @RequestMapping(value = "/queryListsByOpenID", method = RequestMethod.POST)
    public Result<List<TaskInfo>> queryListsByOpenID(@RequestParam("openid") String openid,
                                                     @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return initiatedTaskServices.queryListsByOpenID(openid, page, size);
    }

    /***
     * 接单人完工工单接口
     * @param taskID
     * @return
     */
    @ApiOperation(value = "接单人完工工单", notes = "接单人完工工单接口")
    @RequestMapping(value = "/finishTaskByPhoneUser", method = RequestMethod.POST)
    public Result<String> finishTaskByPhoneUser(@RequestParam("taskID") String taskID) {
        return initiatedTaskServices.finishTaskByPhoneUser(taskID);
    }

    /***
     * 查询接单人完工工单列表
     * @param phoneUserID
     * @return
     */
    @ApiOperation(value = "查询接单人完工工单列表", notes = "查询接单人完工工单列表")
    @RequestMapping(value = "/queryFinishTaskByPhoneUser", method = RequestMethod.POST)
    public Result<List<TaskInfo>> queryFinishTaskByPhoneUser(@RequestParam("phoneUserID") String phoneUserID,
                                                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                             @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return initiatedTaskServices.queryFinishTaskByPhoneUser(phoneUserID, page, size);
    }

    /***
     * 获取工单详情
     * @param taskID
     * @return
     */
    @ApiOperation(value = "获取工单详情", notes = "获取工单详情")
    @RequestMapping(value = "/getTaskMessage", method = RequestMethod.POST)
    public TaskInfo getTaskMessage(@RequestParam("taskID") String taskID) {
        return initiatedTaskServices.getTaskMessage(taskID);
    }

    /***
     * 取消接单
     * @param taskID
     * @return
     */
    @RequestMapping(value = "/cancelTheTask", method = RequestMethod.POST)
    @ApiOperation(value = "取消接单", notes = "取消接单")
    public Result<String> cancelThaTasks(@RequestParam("taskID") String taskID) {
        return initiatedTaskServices.cancelThaTasks(taskID);
    }

    /***
     * 报事人查询已完工工单列表
     * @param openid 传入报事人ID
     * @return
     */
    @RequestMapping(value = "/selectFinishOrderByReporter", method = RequestMethod.POST)
    @ApiOperation(value = "报事人查询已完工工单列表", notes = "报事人查询已完工工单列表")
    public Result<List<TaskInfo>> selectFinishOrderByReporter(@RequestParam("openid") String openid,
                                                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                              @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return initiatedTaskServices.selectFinishOrderByReporter(openid, page, size);
    }

    /**
     * 查询待接单信息
     *
     * @param content     查询内容
     * @param orderStatus 订单状态
     * @param user        用户ID
     * @return
     */
    @RequestMapping(value = "/SearchReadyTaskByName", method = RequestMethod.POST)
    @ApiOperation(value = "查询待接单信息", notes = "查询待接单信息")
    public Result<List<TaskInfo>> SearchTaskByName(@RequestParam("content") String content,
                                                   @RequestParam("orderStatus") String orderStatus,
                                                   @RequestParam("user") String user,
                                                   @RequestParam("current") Integer current,
                                                   @RequestParam("size") Integer size) {
        return initiatedTaskServices.SearchTaskByName(content, orderStatus, user, current, size);
    }

    /**
     * 查询已接单接口订单内容
     *
     * @param content     查询内容
     * @param orderStatus 订单状态
     * @param user        用户ID
     * @return
     */
    @ApiOperation(value = "查询已接单接口订单内容", notes = "查询已接单接口订单内容")
    @RequestMapping(value = "/SearchMyOrderTaskByName", method = RequestMethod.POST)
    public Result<List<TaskInfo>> SearchMyOrderTaskByName(@RequestParam("content") String content, @RequestParam("orderStatus") String orderStatus,
                                                          @RequestParam("user") String user,
                                                          @RequestParam("current") Integer current,
                                                          @RequestParam("size") Integer size) {
        return initiatedTaskServices.SearchMyOrderTaskByName(content, orderStatus, user, current, size);
    }

    /***
     * 查询完工工单内容
     * @param content
     * @param orderStatus
     * @param user
     * @return
     */
    @ApiOperation(value = "查询完工工单内容", notes = "查询完工工单内容")
    @RequestMapping(value = "/SearchMyFinishOrderTaskByName", method = RequestMethod.POST)
    public Result<List<TaskInfo>> SearchMyFinishOrderTaskByName(@RequestParam(value = "content") String content, @RequestParam("orderStatus") String orderStatus,
                                                                @RequestParam("user") String user) {
        return initiatedTaskServices.SearchMyFinishOrderTaskByName(content, orderStatus, user);
    }

    /***
     * 模糊查询报单人报单信息
     * @param content 传入查询信息
     * @param orderStatus 传入订单状态
     * @param user 传入用户ID
     * @return
     */
    @ApiOperation(value = "模糊查询报单人报单信息", notes = "模糊查询报单人报单信息")
    @RequestMapping(value = "/SearchMyOrderTaskByReporter", method = RequestMethod.POST)
    public Result<List<TaskInfo>> SearchMyOrderTaskByReporter(@RequestParam(value = "content") String content, @RequestParam("orderStatus") String orderStatus,
                                                              @RequestParam("user") String user,
                                                              @RequestParam("current") Integer current,
                                                              @RequestParam("size") Integer size) {
        return initiatedTaskServices.SearchMyOrderTaskByReporter(content, orderStatus, user, current, size);
    }

    /***
     * 模糊查询报单人已完工的工单信息
     * @param content 传入查询信息
     * @param orderStatus  传入订单状态
     * @param user  传入用户ID
     * @return
     */
    @ApiOperation(value = "模糊查询报单人已完工的工单信息", notes = "模糊查询报单人已完工的工单信息")
    @RequestMapping(value = "/SearchMyFinishOrderTaskByReporter", method = RequestMethod.POST)
    public Result<List<TaskInfo>> SearchMyFinishOrderTaskByReporter(@RequestParam(value = "content") String content, @RequestParam("orderStatus") String orderStatus,
                                                                    @RequestParam("user") String user,
                                                                    @RequestParam("current") Integer current,
                                                                    @RequestParam("size") Integer size) {
        return initiatedTaskServices.SearchMyFinishOrderTaskByReporter(content, orderStatus, user,current,size);
    }

    /***
     * 更新工单状态
     * @param id
     * @param status
     * @return
     */
    @ApiOperation(value = "管理员强制关闭工单", notes = "管理员强制关闭工单")
    @RequestMapping(value = "/finishOrderByAdmin", method = RequestMethod.POST)
    public Result<String> finishOrderByAdmin(@RequestParam(name = "taskID") String id,
                                             @RequestParam(name = "status") String status) {
        return initiatedTaskServices.finishOrderByAdmin(id, status);
    }

    /***
     * 后台数据条件查询
     * @param content 传入查询信息
     * @param orderStatus  传入订单状态
     * @return
     */
    @ApiOperation(value = "模糊查询报单人已完工的工单信息", notes = "模糊查询报单人已完工的工单信息")
    @RequestMapping(value = "/SearchTaskInfoByCondition", method = RequestMethod.POST)
    public Result<Map<String, Object>> SearchTaskInfoByCondition(@RequestParam(value = "content") String content,
                                                                 @RequestParam("orderStatus") String orderStatus,
                                                                 @RequestParam("page") Integer page,
                                                                 @RequestParam("size") Integer size) {
        return initiatedTaskServices.SearchTaskInfoByCondition(content, orderStatus, page, size);
    }

    /***
     * 查询已完工工单
     * @param page
     * @param size
     * @return
     */
    @ApiOperation(value = "查询已完工的工单信息", notes = "查询已完工的工单信息")
    @RequestMapping(value = "/SearchTaskInfoFinish/page={page}&size={size}", method = RequestMethod.GET)
    public Result<Map<String, Object>> SearchTaskInfoFinish(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
        return initiatedTaskServices.SearchTaskInfoFinish(page, size);
    }

    @RequestMapping("/upload")
    public String uploadPic(@RequestParam(name = "file",required = false) MultipartFile file){
        if (file == null) {
            return null;
        }
        return picUploadService.getPicPath(file);
    }

}
