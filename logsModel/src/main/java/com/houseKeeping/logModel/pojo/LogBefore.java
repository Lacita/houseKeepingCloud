package com.houseKeeping.logModel.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_logs_before")
public class LogBefore {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;
    private String module;
    private String description;
    @TableField("get_requestURI")
    private String getRequestUri;
    private String getMethod;
    private String getRemoteAddr;
    private String body;
    private String requestTime;
}
