package com.houseKeeping.systemUser.pojo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "sys_logs_before")
public class LogBefore implements Serializable {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;
    private String module;
    private String description;
    private String getRequestUri;
    private String getMethod;
    private String getRemoteAddr;
    private String body;
    private String requestTime;
}
