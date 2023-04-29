package com.houseKeeping.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@TableName(value = "task_info")
@Document(indexName = "taskinfo")
public class TaskInfo {

    @TableId(type = IdType.ID_WORKER_STR)
    private String id;
    @TableField(value = "ordername")
    private String ordername;
    @TableField(value = "openid")
    private String openid;
    private String reporter;
    private String phone;
    private String area;
    private String message;
    private String img;
    //  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8",locale = "zh")
//  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//  @TableField(value = "createtime")
    private String createtime;
    private long orderStatus;
    @TableField(value = "isDelete")
    private long isDelete;
    private String pickupUser;
    //  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8",locale = "zh")
//  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String beginWork;
    private String note;
    //  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endTime;

}
