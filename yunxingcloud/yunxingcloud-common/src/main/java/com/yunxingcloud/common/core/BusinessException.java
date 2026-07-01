package com.yunxingcloud.common.core;

import com.yunxingcloud.common.enums.ErrorCode;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getDefaultMsg());
        this.errorCode = errorCode;
        this.args = args;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public ErrorCode getErrorCode() { return errorCode; }
    public Object[] getArgs() { return args; }

    public int getCode() { return errorCode.getCode(); }
}