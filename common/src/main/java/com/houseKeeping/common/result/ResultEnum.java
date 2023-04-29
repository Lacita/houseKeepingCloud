package com.houseKeeping.common.result;

public enum ResultEnum {

    /**
     * 请求成功
     */
    SUCCESS(2000),
    /**
     * 请求失败
     */
    ERROR(3000),

    /**
     * 请求过期
     */
    AFTER_EXPIRED(3100),

    /**
     * 接口不存在
     */
    NOT_FOUND(404),

    /**
     * 内部错误
     */
    INTERNAL_SERVER_ERROR(5000);
    public int code;

    ResultEnum(int code) {
        this.code = code;
    }
}
