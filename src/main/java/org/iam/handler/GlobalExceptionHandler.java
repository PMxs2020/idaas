package org.iam.handler;

import lombok.extern.slf4j.Slf4j;
import org.iam.constant.MessageConstant;
import org.iam.exception.BaseException;
import org.iam.util.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("异常信息：{}", ex.getMessage());
        String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return Result.error().message(errorMessage);
    }
    /**
     * 捕获重复字段异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());
        String exinfo=ex.getMessage();
        if(exinfo.contains("Duplicate entry")){
            return Result.error().message(exinfo.split(" ")[2].replace('\'', ' ')  + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error().message(MessageConstant.UNKNOWN_ERROR);
    }
    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error().message(ex.getMessage());
    }
}
