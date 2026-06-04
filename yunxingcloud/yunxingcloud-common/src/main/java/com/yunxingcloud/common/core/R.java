package com.yunxingcloud.common.core;

import java.io.Serializable;

public class R<T> implements Serializable {

    private static final int SUCCESS = 200;
    private static final int ERROR = 500;

    private int code;
    private String message;
    private T data;

    public R() {}

    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS, "success", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, "success", data);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(SUCCESS, message, data);
    }

    public static <T> R<T> fail() {
        return new R<>(ERROR, "fail", null);
    }

    public static <T> R<T> fail(String message) {
        return new R<>(ERROR, message, null);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
