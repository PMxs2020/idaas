package org.iam.exception;

public class ParamsInvalidException extends RuntimeException{
    public ParamsInvalidException(String message) {
        super(message);
    }
}
