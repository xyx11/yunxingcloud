package com.yunxingcloud.common.core;

public enum ErrorCode {

    // 通用
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    RATE_LIMITED(429, "请求过于频繁，请稍后重试"),
    LOCKED(423, "账号已被锁定"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 认证
    LOGIN_FAILED(1001, "用户名或密码错误"),
    CAPTCHA_ERROR(1002, "验证码错误"),
    TOKEN_EXPIRED(1003, "Token 已过期"),
    TOKEN_INVALID(1004, "Token 无效"),
    ACCOUNT_LOCKED(1005, "账号已被锁定，请30分钟后重试"),

    // 用户
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXISTS(2002, "用户名已存在"),
    EMAIL_EXISTS(2003, "邮箱已注册"),
    PASSWORD_WEAK(2004, "密码强度不足"),

    // 业务
    CONFIG_KEY_EXISTS(3001, "参数键名已存在"),
    JOB_EXISTS(3002, "任务已存在"),
    MENU_HAS_CHILDREN(3003, "菜单存在子节点，无法删除"),
    FILE_TOO_LARGE(4001, "文件大小超过限制"),
    FILE_TYPE_INVALID(4002, "不支持的文件类型");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
