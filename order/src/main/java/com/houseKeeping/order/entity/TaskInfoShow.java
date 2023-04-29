package com.houseKeeping.order.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.sql.Date;

@Data
@Document(indexName = "taskinfo")
public class TaskInfoShow {
    private String id;
    private String ordername;
    private String openid;
    private String reporter;
    private String phone;
    private String area;
    private String message;
    private String img;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8", locale = "zh")
    private Date createtime;
    private long orderStatus;
    private long isDelete;
    private String pickupUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8", locale = "zh")
    private Date beginWork;
    private String note;
    private Date endTime;
    private String userNameTask;
    private String userPhoneTask;
}
