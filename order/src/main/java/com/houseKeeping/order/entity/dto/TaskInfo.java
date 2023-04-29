package com.houseKeeping.order.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Document(indexName = "task")
@Data
public class TaskInfo implements Serializable {

    private String id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String ordername;
    @Field(type = FieldType.Keyword)
    private String openid;
    @Field(type = FieldType.Keyword)
    private long orderStatus;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String message;
    private String note;
}
