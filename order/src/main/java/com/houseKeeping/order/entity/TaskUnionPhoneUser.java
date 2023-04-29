package com.houseKeeping.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.data.elasticsearch.annotations.Document;

import java.sql.Timestamp;

@TableName(value = "task_union_phone_user")
@Document(indexName = "taskinfo")
public class TaskUnionPhoneUser {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;
    private String taskId;
    private String phoneUserId;
    private long relationStatus;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp endTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


    public String getPhoneUserId() {
        return phoneUserId;
    }

    public void setPhoneUserId(String phoneUserId) {
        this.phoneUserId = phoneUserId;
    }


    public long getRelationStatus() {
        return relationStatus;
    }

    public void setRelationStatus(long relationStatus) {
        this.relationStatus = relationStatus;
    }


    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }


    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}
