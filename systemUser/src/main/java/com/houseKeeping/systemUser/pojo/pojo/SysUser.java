package com.houseKeeping.systemUser.pojo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
@TableName(value = "sys_users")
public class SysUser implements Serializable {
    @TableId(type = IdType.ID_WORKER_STR)
    private String userId;
    private String conpany;
    private String username;
    private String password;
    private byte userStatus;
    @TableField(value = "avatarUrl")
    private String avatarUrl;
    private Date createtime;
    @TableField(value = "isDelete")
    private long isDelete;
    @TableField(exist = false)
    private String confirmPassword;

}
