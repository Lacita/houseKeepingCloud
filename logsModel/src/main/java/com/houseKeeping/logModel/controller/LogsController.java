package com.houseKeeping.logModel.controller;

import com.houseKeeping.logModel.mapper.LogAroundMapper;
import com.houseKeeping.logModel.mapper.LogBeforeMapper;
import com.houseKeeping.logModel.pojo.LogAround;
import com.houseKeeping.logModel.pojo.LogBefore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@RestController
@RequestMapping("/log")
public class LogsController {

    @Resource
    private LogAroundMapper logAroundMapper;

    @Resource
    private LogBeforeMapper logBeforeMapper;

    @PostMapping("/logAround")
    public void doLogAround(@RequestBody LogAround logAround) {
        LogAround around = new LogAround();
        around.setArgs(logAround.getArgs());
        around.setResult(logAround.getResult());
        logAroundMapper.insert(around);
    }

    @PostMapping("/logBefore")
    public void doLogBefore(@RequestBody LogBefore logBefore) {
        LogBefore before = new LogBefore();
        before.setBody(logBefore.getBody());
        before.setDescription(logBefore.getDescription());
        before.setModule(logBefore.getModule());
        before.setGetMethod(logBefore.getGetMethod());
        before.setGetRemoteAddr(logBefore.getGetRemoteAddr());
        before.setGetRequestUri(logBefore.getGetRequestUri());
        logBeforeMapper.insert(before);
    }

}
