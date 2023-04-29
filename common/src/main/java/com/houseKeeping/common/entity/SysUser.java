package com.houseKeeping.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
public class SysUser implements Serializable {
    private String userId;
    private String conpany;
    private String username;
    private String password;
    private byte userStatus;
    private String avatarUrl;
    private Date createtime;
    private long isDelete;
    private String confirmPassword;
}
