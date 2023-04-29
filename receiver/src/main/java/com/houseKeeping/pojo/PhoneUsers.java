package com.houseKeeping.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.Table;
import java.sql.Date;

@Data
@TableName(value = "phone_users")
public class PhoneUsers {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;
    @TableField(value = "username")
    private String userName;
    private String phone;
    private byte userStatus;
    private Date createtime;
    @TableField(value = "isDelete")
    private byte isDelete;
    private String conpany;
    @TableField(exist = false)
    private String confirmPhone;


}
