package org.iam.exception;

/**
 * ID为空异常
 */
public class IdNullException extends BaseException {
    public IdNullException() {
        super("未传递id");
    }
    public IdNullException(String msg) {
        super(msg);
    }
} 