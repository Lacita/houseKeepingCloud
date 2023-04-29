package com.houseKeeping.systemUser.feign;


import com.houseKeeping.systemUser.pojo.pojo.LogAround;
import com.houseKeeping.systemUser.pojo.pojo.LogBefore;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient("logModel")
public interface FeignService {

    @PostMapping("/log/logAround")
    void doLogAround(@RequestBody LogAround logAround);

    @PostMapping("/log/logBefore")
    void doLogBefore(@RequestBody LogBefore logBefore);


}
