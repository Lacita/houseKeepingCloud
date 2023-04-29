package com.houseKeeping.systemUser.log;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogInfo {

    /*
    模块
     */
    String module() default "";

    /*
    描述
     */
    String description() default "";

}
