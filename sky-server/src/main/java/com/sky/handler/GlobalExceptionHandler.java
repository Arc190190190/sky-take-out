package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.constant.MySQLErrorCodeConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQLIntegrityConstraintViolationException异常
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        int errorCode = ex.getErrorCode();
        if (errorCode == MySQLErrorCodeConstant.DUPLICATE_ENTRY || message.contains("Duplicate entry")) {
            return Result.error(MessageConstant.ALREADY_EXISTS); // 账号已存在
        } else if (errorCode == MySQLErrorCodeConstant.NONNULL_VIOLATION || message.contains("cannot be null")) {
            return Result.error(MessageConstant.FIELD_REQUIRED); // 字段不能为空
        } else if (errorCode == MySQLErrorCodeConstant.FOREIGN_KEY_CONSTRAINT_FAILS || message.contains("foreign key constraint")) {
            return Result.error(MessageConstant.FOREIGN_KEY_FAILED); // 外键约束失败
        } else {
            log.error("未知的完整性约束冲突异常，错误码：{}，消息：{}", errorCode, message, ex);
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
