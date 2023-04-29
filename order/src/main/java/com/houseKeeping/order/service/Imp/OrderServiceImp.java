package com.houseKeeping.order.service.Imp;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houseKeeping.common.config.PatternConfig;
import com.houseKeeping.common.constant.RedisConstant;
import com.houseKeeping.common.result.Result;
import com.houseKeeping.common.result.ResultUtils;
import com.houseKeeping.order.Exception.MyException;
import com.houseKeeping.order.config.Utils;
import com.houseKeeping.order.entity.TaskInfo;
import com.houseKeeping.order.entity.TaskInfoShow;
import com.houseKeeping.order.entity.TaskUnionPhoneUser;
import com.houseKeeping.order.mapper.OrderMapper;
import com.houseKeeping.order.mapper.OrderUnionMapper;
import com.houseKeeping.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImp implements OrderService {

    @Resource
    private Utils utils;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderUnionMapper orderUnionMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /*
    发布新任务
     */
    @Transactional
    @Override
    public Result<String> releasesNewTask(TaskInfo taskInfo) {
        String phone = taskInfo.getPhone();
        String reporter = taskInfo.getReporter();
        if (phone.isEmpty() || reporter.isEmpty()) {
            return ResultUtils.ERROR("报事人及联系手机不能为空");
        }
        if (PatternConfig.isValidPhoneNumber(phone)) {
            return ResultUtils.ERROR("手机号码非法,请检查");
        }
        String openid = taskInfo.getOpenid();
        if (openid.isEmpty()) {
            return ResultUtils.ERROR("微信用户未登录！");
        }
        // 创建新的实体类对象
        TaskInfo info = new TaskInfo();
        info.setOrdername(taskInfo.getOrdername());
        info.setOpenid(openid);
        info.setReporter(reporter);
        info.setPhone(phone);
        info.setArea(taskInfo.getArea());
        info.setMessage(taskInfo.getMessage());
        info.setImg(taskInfo.getImg());
        info.setNote(taskInfo.getNote());
        info.setOrderStatus(1);
        int newTask = orderMapper.insert(info);
        if (newTask > 0) {
            return ResultUtils.SUCCESS("任务发布成功");
        }
        return ResultUtils.ERROR("任务发布失败");
    }

    /**
     * 接单方法，传入参数主要为任务单ID以及做单人ID进行相关联
     *
     * @param taskID 传入任务ID
     * @param userID 传入用户ID
     */

    @Transactional
    @Override
    public Result<String> receivingOrders(String taskID, String userID) {
        // 添加互斥锁处理接单并发问题
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(RedisConstant.REDIS_LOCK_RECEIVE_TASK + taskID, 1);
        // 为此分布式锁设置1分钟存活时间
        redisTemplate.expire(RedisConstant.REDIS_LOCK_RECEIVE_TASK + taskID, 60, TimeUnit.SECONDS);
        if (BooleanUtil.isFalse(setIfAbsent)) {
            return ResultUtils.ERROR("已被用户接单");
        }
        if (taskID.isEmpty() || userID.isEmpty()) {
            return ResultUtils.ERROR("任务单或用户ID不存在");
        }
        // 更新任务单状态，写入接单人ID以及调整单据
        UpdateWrapper<TaskInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", taskID).eq("order_status", 1);
        updateWrapper.set("order_status", 2).set("pickup_user", userID);
        int updateTaskStatus = orderMapper.update(null, updateWrapper);
        // 如果更新失败，则直接返回
        if (!(updateTaskStatus > 0)) {
            return ResultUtils.ERROR("接单失败");
        }
        TaskUnionPhoneUser taskUnionPhoneUser = new TaskUnionPhoneUser();
        taskUnionPhoneUser.setTaskId(taskID);
        taskUnionPhoneUser.setPhoneUserId(userID);
        int receivingOrders = orderUnionMapper.insert(taskUnionPhoneUser);
        if (receivingOrders > 0) {
            // 释放锁
            redisTemplate.delete(RedisConstant.REDIS_LOCK_RECEIVE_TASK + taskID);
            return ResultUtils.SUCCESS("接单成功");
        }
        return ResultUtils.ERROR("接单失败");
    }

     /*
     接单人员调整工单状态
     */

    @Transactional
    @Override
    public Result<String> phoneUserUpdateOrderStatus(String orderUnionId, String status) {
        if (orderUnionId.isEmpty()) {
            return ResultUtils.ERROR("单据号不能为空");
        }
        TaskUnionPhoneUser unionPhoneUser = orderUnionMapper.selectById(orderUnionId);
        if (unionPhoneUser == null) {
            return ResultUtils.ERROR("订单不存在");
        }
        String updateTime = Utils.getSysTime();
        UpdateWrapper<TaskUnionPhoneUser> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("id", orderUnionId);
        userUpdateWrapper.set("relation_status", status).set("update_time", updateTime);
        int updateOrderStatus = orderUnionMapper.update(null, userUpdateWrapper);
        if (updateOrderStatus > 0) {
            return ResultUtils.SUCCESS("订单变更完成");
        }
        return ResultUtils.ERROR("订单变更失败");

    }

    /*
    用在小程序展示任务列表
     */
    @Override
    public Result<Map<String, Object>> showTasks(Integer page, Integer size) {
        Page<TaskInfo> infoPage = new Page<>(page, size);
        QueryWrapper<TaskInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByDesc("createtime");
        IPage<TaskInfo> taskInfoIPage = orderMapper.selectPage(infoPage, queryWrapper);
        List<TaskInfo> records = taskInfoIPage.getRecords();
        long total = taskInfoIPage.getTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("data", records);
        map.put("total", total);
        return ResultUtils.SUCCESS(map);
    }

    /*
    根据用户工单查看任务单
     */
    @Override
    public Result<List<TaskInfo>> selectTaskByPhoneUsers(String phoneUser, Integer page, Integer size) {
        int start = 0;
        start = page;
        start = start <= 0 ? 0 : start * size;
        List<TaskInfo> taskInfos = orderMapper.selectTaskByPhoneUsers(phoneUser, start, size);
        return ResultUtils.SUCCESS(taskInfos);
    }

    @Override
    public Result<List<TaskInfo>> queryListsByOpenID(String openid, Integer page, Integer size) {
        Page<TaskInfo> taskInfoPage = new Page<>(page, size);
        QueryWrapper<TaskInfo> taskInfoQueryWrapper = new QueryWrapper<>();
        taskInfoQueryWrapper.eq(StringUtils.isNotBlank(openid), "openid", openid);
        taskInfoQueryWrapper.in("order_status",1,2);
        IPage<TaskInfo> taskInfoIPage = orderMapper.selectPage(taskInfoPage, taskInfoQueryWrapper);
        List<TaskInfo> records = taskInfoIPage.getRecords();
        return ResultUtils.SUCCESS(records);
    }

    /**
     * 完成工单
     *
     * @param taskID 传入当前接单用户ID
     */
    @Override
    @Transactional
    public Result<String> finishTaskByPhoneUser(String taskID) {
        String endTime = Utils.getSysTime();
        UpdateWrapper<TaskInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("order_status", 3).set("end_time", endTime);
        updateWrapper.eq("id", taskID);
        int finishTask = orderMapper.update(null, updateWrapper);
        if (!(finishTask > 0)) {
            return ResultUtils.ERROR("订单关闭失败");
        }
        return ResultUtils.SUCCESS("订单关闭成功");
    }

    /**
     * 查询当前用户关闭的工单
     *
     * @param phoneUserID 接单人ID
     */
    @Override
    public Result<List<TaskInfo>> queryFinishTaskByPhoneUser(String phoneUserID, Integer page, Integer size) {
        int start = 0;
        start = page;
        start = start <= 0 ? 0 : start * size;
        List<TaskInfo> taskInfos = orderMapper.selectFinishByPhoneUser(phoneUserID, start, size);
        return ResultUtils.SUCCESS(taskInfos);
    }

    /***
     * 展示工单信息
     * @param taskID 传入工单号
     */
    @Override
    public TaskInfo getTaskMessage(String taskID) {
        if (StringUtils.isBlank(taskID)) {
            return null;
        }
        return orderMapper.selectById(taskID);
    }

    /**
     * 取消接单，订单回复正常可接单模式
     *
     * @param taskID 任务单号
     */
    @Override
    public Result<String> cancelThaTasks(String taskID) {
        if (StringUtils.isEmpty(taskID)) {
            return ResultUtils.ERROR("订单号不能为空");
        }
        UpdateWrapper<TaskInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("order_status", 1).set("pickup_user", "");
        updateWrapper.eq("id", taskID);
        int cancelThaTasks = orderMapper.update(null, updateWrapper);
        if (cancelThaTasks > 0) {
            return ResultUtils.SUCCESS("取消成功");
        }
        return ResultUtils.ERROR("取消失败");
    }

    /***
     * 查询报事人工单列表展示
     * @param openid 传入报事人ID
     * @param page  传入页面信息
     * @param size  传入展示数量
     */
    @Override
    public Result<List<TaskInfo>> selectFinishOrderByReporter(String openid, Integer page, Integer size) {
        if (StringUtils.isBlank(openid)) {
            return ResultUtils.ERROR(null);
        }
        Page<TaskInfo> infoPage = new Page<>(page, size);
        QueryWrapper<TaskInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_status", 3).eq(StringUtils.isNotBlank(openid), "openid", openid).orderByDesc("createtime");
        IPage<TaskInfo> taskInfoIPage = orderMapper.selectPage(infoPage, queryWrapper);
        List<TaskInfo> records = taskInfoIPage.getRecords();
        return ResultUtils.SUCCESS(records);
    }

    @Override
    public Result<List<TaskInfo>> SearchTaskByName(String content, String orderStatus, String user, Integer current, Integer size) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 封装查询条件
        if (StringUtils.isNotBlank(content)) {
            boolQuery.must(QueryBuilders.multiMatchQuery(content, "ordername", "message"));
        }
//        if (StringUtils.isNotBlank(user)) {
//            boolQuery.must(QueryBuilders.matchQuery("pickup_user", user));
//        }
        if (StringUtils.isNotBlank(orderStatus)) {
            boolQuery.must(QueryBuilders.termQuery("order_status", 1));
        }
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        ScoreSortBuilder scoreSortBuilder = SortBuilders.scoreSort().order(SortOrder.DESC);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery).withPageable(pageRequest)
                .withSorts(scoreSortBuilder).build();
        try {
            List<String> collect = elasticsearchRestTemplate.search(nativeSearchQuery, TaskInfo.class)
                    .getSearchHits().stream().map(searchHit -> searchHit.getContent().getId()).collect(Collectors.toList());
            List<TaskInfo> taskInfos = orderMapper.selectBatchIds(collect);
            return ResultUtils.SUCCESS(taskInfos);
        } catch (Exception e) {
            log.error("任务名称查询失败，错误代码为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }

    /***
     * 用于查询接单人的订单状态接口
     * @param content 输入查询的内容
     * @param orderStatus 工单状态
     * @param user 接单用户id
     * @return 返回
     */
    @Override
    public Result<List<TaskInfo>> SearchMyOrderTaskByName(String content, String orderStatus, String user, Integer current, Integer size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(content, "ordername", "message"));
        }
        if (StringUtils.isNotBlank(user)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("pickup_user", user));
        }
        if (StringUtils.isNotBlank(orderStatus)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("order_status", orderStatus));
        }
//        Optional.ofNullable(content).map(c -> boolQueryBuilder.should(QueryBuilders.multiMatchQuery(content,"ordername","message")));
//        Optional.ofNullable(user).map(u -> boolQueryBuilder.must(QueryBuilders.termQuery("pickup_user",user)));
//        Optional.ofNullable(orderStatus).map(o ->boolQueryBuilder.must(QueryBuilders.termQuery("order_status",orderStatus)));
        ScoreSortBuilder scoreSortBuilder = SortBuilders.scoreSort().order(SortOrder.DESC);
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withPageable(pageRequest)
                .withSorts(scoreSortBuilder).build();
        try {
            SearchHits<TaskInfo> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, TaskInfo.class);
            List<SearchHit<TaskInfo>> hitsSearchHits = searchHits.getSearchHits();
            List<String> collect = hitsSearchHits.stream().map(searchHit ->searchHit.getContent().getId()).collect(Collectors.toList());
            List<TaskInfo> taskInfoList = orderMapper.selectBatchIds(collect);
            return ResultUtils.SUCCESS(taskInfoList);
        } catch (Exception e) {
            log.error("任务名称查询失败，错误代码为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }

    @Override
    public Result<List<TaskInfo>> SearchMyFinishOrderTaskByName(String content, String orderStatus, String user) {
        // 获取查询的索引
//        SearchRequest request = new SearchRequest(MessageConstant.IDX);
//        // 创建构造器
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        List<TaskInfo> taskInfos = new ArrayList<>();
//        // 当查询内容为空，则检索当前用户号上的完结工单
//        if (content == "" || null == content) {
//            BoolQueryBuilder nullContentQuery = QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termQuery("pickup_user",user))
//                    .must(QueryBuilders.termQuery("order_status", orderStatus));
//            sourceBuilder.query(nullContentQuery);
//            sourceBuilder.size(20);
//            request.source(sourceBuilder);
//            return getArrayListResult(request, taskInfos);
//        }
//        // 若查询内容不为空，则根据内容，客户ID以及订单状态进行查询
//        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                .must(QueryBuilders.multiMatchQuery(content, "ordername", "message"))
//                .must(QueryBuilders.termQuery("order_status", 3))
//                .must(QueryBuilders.termQuery("pickup_user",user));
//        // 返回查询结果对象
//        sourceBuilder.query(queryBuilder);
//        request.source(sourceBuilder);
//        return getArrayListResult(request, taskInfos);
        return null;
    }

    /***
     * 根据用户ID查询待接单工单以及进行中工单
     * @param content 工单内容
     * @param orderStatus 工单状态
     * @param user 发起人ID
     */
    @Override
    public Result<List<TaskInfo>> SearchMyOrderTaskByReporter(String content, String orderStatus, String user, Integer current, Integer size) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(content, "ordername", "message"));
        }
        if (StringUtils.isNotBlank(user)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("openid", user));
        }
        if (StringUtils.isNotBlank(orderStatus)) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("order_status", 3));
        }
        ScoreSortBuilder sortBuilder = SortBuilders.scoreSort().order(SortOrder.DESC);
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withPageable(pageRequest)
                .withSorts(sortBuilder).build();
        try {
            List<String> list = elasticsearchRestTemplate.search(nativeSearchQuery, TaskInfo.class).getSearchHits()
                    .stream().map(search -> search.getContent().getId()).collect(Collectors.toList());
            System.out.println("list值为:"+list.toString());
            List<TaskInfo> taskInfos = orderMapper.selectBatchIds(list);
            return ResultUtils.SUCCESS(taskInfos);
        } catch (Exception e) {
            log.error("任务名称查询失败，错误代码为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


    @Override
    public Result<List<TaskInfo>> SearchMyFinishOrderTaskByReporter(String content, String orderStatus, String user, Integer current, Integer size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(content, "ordername", "message"));
        }
        if (StringUtils.isNotBlank(user)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("openid", user));
        }
        if (StringUtils.isNotBlank(orderStatus)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("order_status", 3));
        }
        ScoreSortBuilder sortBuilder = SortBuilders.scoreSort().order(SortOrder.DESC);
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withPageable(pageRequest)
                .withSorts(sortBuilder).build();
        try {
            List<String> list = elasticsearchRestTemplate.search(nativeSearchQuery, TaskInfo.class).getSearchHits()
                    .stream().map(search -> search.getContent().getId()).collect(Collectors.toList());
            System.out.println("list值为:"+list.toString());
            List<TaskInfo> taskInfos = orderMapper.selectBatchIds(list);
            return ResultUtils.SUCCESS(taskInfos);
        } catch (Exception e) {
            log.error("任务名称查询失败，错误代码为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> finishOrderByAdmin(String id, String status) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(status)) {
            return ResultUtils.ERROR("任务单及订单状态不能为空");
        }
        String endTime = Utils.getSysTime();
        UpdateWrapper<TaskInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("order_status", 3).set("end_time", endTime).set("note", "Admin").eq("id", id);
        int updateOrderByAdmin = orderMapper.update(null, updateWrapper);
        if (updateOrderByAdmin > 0) {
            return ResultUtils.SUCCESS("状态调整成功");
        }
        return ResultUtils.ERROR("状态调整失败");
    }

    @Override
    public Result<Map<String, Object>> SearchTaskInfoByCondition(String content, String orderStatus, Integer page, Integer size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.multiMatchQuery(content, "ordername", "message"));
        }
        if (StringUtils.isNotBlank(orderStatus)) {
            boolQueryBuilder.should(QueryBuilders.termQuery("order_status", orderStatus));
        }
        ScoreSortBuilder sortBuilder = SortBuilders.scoreSort().order(SortOrder.DESC);
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        try {
            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withPageable(pageRequest).
                    withSorts(sortBuilder).build();
            SearchHits<TaskInfo> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, TaskInfo.class);
            List<String> list = searchHits.getSearchHits().stream().map(searchHit -> searchHit.getContent().getId()).collect(Collectors.toList());
            List<TaskInfo> taskInfoList = orderMapper.selectBatchIds(list);
            long totalHits = searchHits.getTotalHits();
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("data", taskInfoList);
            hashMap.put("total", totalHits);
            return ResultUtils.SUCCESS(hashMap);
        } catch (Exception e) {
            log.error("任务名称查询失败，错误代码为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }

    }

    @Override
    public Result<Map<String, Object>> SearchTaskInfoFinish(Integer page, Integer size) {
        if (page == null || size == null) {
            return ResultUtils.ERROR(null);
        }
        int offSet = 0;
        offSet = page;
        // 判断页码，小于0则判断为0
        offSet = offSet - 1 <= 0 ? 0 : (offSet - 1) * size;
        List<TaskInfoShow> taskInfos = orderMapper.selectTaskDetailList(offSet, size);
        int finishCount = orderMapper.SearchTaskInfoFinishCount();
        HashMap<String, Object> map = new HashMap<>();
        map.put("data", taskInfos);
        map.put("total", finishCount);
        return ResultUtils.SUCCESS(map);
    }

    @Override
    public Result<Map<String, Object>> showReadyTasks(Integer page, Integer size) {
        Page<TaskInfo> infoPage = new Page<>(page, size);
        QueryWrapper<TaskInfo> taskInfoQueryWrapper = new QueryWrapper<>();
        taskInfoQueryWrapper.eq("order_status", 1).eq("isDelete", 0).orderByDesc("createtime");
        try {
            IPage<TaskInfo> infoIPage = orderMapper.selectPage(infoPage, taskInfoQueryWrapper);
            List<TaskInfo> records = infoIPage.getRecords();
            long total = infoIPage.getTotal();
            Map<String, Object> map = new HashMap<>();
            map.put("data", records);
            map.put("total", total);
            return ResultUtils.SUCCESS(map);
        } catch (Exception e) {
            log.error("完结工单查询失败，错误代码为：{}", e.getMessage());
            throw new MyException(e.getMessage());
        }

    }

    @Override
    public String syncData() {
        List<TaskInfo> taskInfos = orderMapper.selectList(null);
        elasticsearchRestTemplate.save(taskInfos);
        return "success";
    }


}
