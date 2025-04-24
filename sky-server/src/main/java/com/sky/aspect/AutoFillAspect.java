package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;

/**
 * 公共字段自动注入的切面
 * @author Arc
 * @version v1.0
 */
@Aspect
@Slf4j
@Component
public class AutoFillAspect {
    /**
     * 公共字段自动注入的切点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 公共字段自动注入的通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("公共字段自动注入");
        // 获取当前被拦截的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取被拦截方法上的自动填充注解
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        // 获取当前被拦截的方法注解的参数
        OperationType value = annotation.value();
        // 获取当前被拦截的方法的参数
        Object[] args = joinPoint.getArgs();
        //准备要自动注入的公共字段的值
        Long updateUser = BaseContext.getCurrentId();
        LocalDateTime updateTime = LocalDateTime.now();

        if (args == null || args.length == 0){
            return;
        }

        // 获取参数类
        Object entity = args[0];
        try {
            if (value.equals(OperationType.INSERT)){

                //通过反射得到参数类中公共字段的方法
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 调用方法,设置公共字段
                setUpdateUser.invoke(entity,updateUser);
                setCreateTime.invoke(entity,updateTime);
                setCreateUser.invoke(entity,updateUser);
                setUpdateTime.invoke(entity,updateTime);
            } else if (value.equals(OperationType.UPDATE)) {

                //通过反射得到参数类中公共字段的方法
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 调用方法,设置公共字段
                setUpdateUser.invoke(entity,updateUser);
                setUpdateTime.invoke(entity,updateTime);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}
