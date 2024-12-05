package org.iam.exception;

public class SessionNotExistException extends RuntimeException {
    public SessionNotExistException(String message) {
        super(message);
    }
}
