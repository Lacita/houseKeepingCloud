package com.houseKeeping.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysLogsBefore {

    private String id;
    private String module;
    private String description;
    private String getRequestUri;
    private String getMethod;
    private String getRemoteAddr;
    private String body;
    private LocalDateTime requestTime;
}
