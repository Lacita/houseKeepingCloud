package com.houseKeeping.logModel.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "sys_logs_arounds")
public class LogAround implements Serializable {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;
    private String args;
    private String result;
    private String requestTime;
}
