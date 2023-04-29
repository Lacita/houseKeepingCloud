package com.houseKeeping.systemUser.Exception;

import lombok.Data;

@Data
public class MyException extends RuntimeException {

    private boolean flag;
    private int code;
    private String message;

    public MyException(boolean flag, int code, String message) {

        super(message);
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public MyException(String message) {

        super(message);
        this.message = message;
    }


}
