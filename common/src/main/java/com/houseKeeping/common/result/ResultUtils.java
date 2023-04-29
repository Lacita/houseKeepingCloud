package com.houseKeeping.common.result;

public class ResultUtils {

    public static <T> Result<T> SUCCESS(T data) {
        return new Result<T>(true, ResultEnum.SUCCESS.code, data);
    }

    public static <T> Result<T> ERROR(T data) {
        return new Result<T>(false, ResultEnum.ERROR.code, data);
    }

    public static <T> Result<T> AFTER_EXPIRE(T data) {
        return new Result<T>(false, ResultEnum.AFTER_EXPIRED.code, data);
    }

}
