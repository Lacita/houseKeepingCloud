package com.houseKeeping.systemUser.Aspect;

import com.alibaba.fastjson.JSON;

import com.houseKeeping.systemUser.feign.FeignService;
import com.houseKeeping.systemUser.log.LogInfo;
import com.houseKeeping.systemUser.pojo.pojo.LogAround;
import com.houseKeeping.systemUser.pojo.pojo.LogBefore;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Resource
    private FeignService feignService;

    @Pointcut("@annotation(com.houseKeeping.systemUser.log.LogInfo)")
    public void logPoint() {
    }

    @Around("logPoint()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        Object proceed = proceedingJoinPoint.proceed(args);
        LogAround logAround = new LogAround();
        logAround.setArgs(args[0].toString());
        logAround.setResult(proceed.toString());
        feignService.doLogAround(logAround);
        return proceed;
    }

    @Before("logPoint()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogInfo annotation = method.getAnnotation(LogInfo.class);
        if (annotation == null) {
            log.info("无该对象");
        }
        String module = annotation.module();
        String description = annotation.description();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        StringBuilder requestPath = new StringBuilder();
        requestPath.append(request.getRemoteAddr())
                .append(":")
                .append(request.getRemotePort());
        //请求参数
        StringBuilder requestLog = new StringBuilder();
        Signature sign = joinPoint.getSignature();
        String[] paramNames = ((MethodSignature) sign).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        int paramLength = null == paramNames ? 0 : paramNames.length;
        if (paramLength == 0) {
            requestLog.append("{}");
        } else {
            requestLog.append("[");
            for (int i = 0; i < paramLength - 1; i++) {
                requestLog.append(paramNames[i]).append("=").append(JSON.toJSONString(paramValues[i])).append(",");
            }
            requestLog.append(paramNames[paramLength - 1]).append("=").append(com.alibaba.fastjson.JSON.toJSONString(paramValues[paramLength - 1])).append("]");
        }
        LogBefore logBefore = new LogBefore();
        logBefore.setModule(module);
        logBefore.setDescription(description);
        logBefore.setGetRequestUri(request.getRequestURI());
        logBefore.setGetMethod(request.getMethod());
        logBefore.setGetRemoteAddr(requestPath.toString());
        logBefore.setBody(requestLog.toString());
        feignService.doLogBefore(logBefore);
    }

}
