package com.yunxingcloud.common.enums;

public enum ErrorCode {

    // 通用
    SUCCESS(0, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或 Token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    RATE_LIMITED(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 认证 (1000-1099)
    AUTH_LOGIN_FAILED(1001, "用户名或密码错误"),
    AUTH_ACCOUNT_LOCKED(1002, "账户已被锁定，请{minutes}分钟后重试"),
    AUTH_TOKEN_EXPIRED(1003, "Token 已过期，请重新登录"),
    AUTH_TOKEN_INVALID(1004, "Token 无效"),
    AUTH_PASSWORD_WEAK(1005, "密码强度不足"),
    AUTH_CAPTCHA_INVALID(1006, "验证码错误"),

    // 用户 (1100-1199)
    USER_NOT_FOUND(1101, "用户不存在"),
    USER_DUPLICATE(1102, "用户名已存在"),
    USER_DISABLED(1103, "用户已被禁用"),
    USER_EMAIL_DUPLICATE(1104, "邮箱已被注册"),

    // 订单 (2000-2099)
    ORDER_CART_EMPTY(2001, "购物车为空"),
    ORDER_STOCK_INSUFFICIENT(2002, "库存不足"),
    ORDER_STATUS_INVALID(2003, "订单状态不允许此操作"),
    ORDER_NOT_FOUND(2004, "订单不存在"),
    ORDER_PAY_FAILED(2005, "支付失败"),
    ORDER_COUPON_INVALID(2006, "优惠券不可用"),
    ORDER_COUPON_MIN_NOT_MET(2007, "未达优惠券最低消费"),
    ORDER_DUPLICATE(2008, "请勿重复提交订单"),

    // 支付 (2100-2199)
    PAY_CHANNEL_UNSUPPORTED(2101, "不支持的支付渠道"),
    PAY_REFUND_EXCEED(2102, "退款金额超过可退金额"),
    PAY_GATEWAY_ERROR(2103, "支付网关异常"),

    // 库存 (2200-2299)
    INVENTORY_NOT_FOUND(2201, "库存记录不存在"),
    INVENTORY_BUSY(2202, "系统繁忙，请稍后重试"),
    INVENTORY_ALERT(2203, "库存低于预警阈值"),

    // 工单 (3000-3099)
    TICKET_CLOSED(3001, "工单已关闭，不可修改"),

    // 文件 (4000-4099)
    FILE_EMPTY(4001, "文件为空"),
    FILE_FORMAT(4002, "不支持的文件格式"),
    FILE_TOO_LARGE(4003, "文件大小超过限制");

    private final int code;
    private final String defaultMsg;

    ErrorCode(int code, String defaultMsg) {
        this.code = code;
        this.defaultMsg = defaultMsg;
    }

    public int getCode() { return code; }
    public String getDefaultMsg() { return defaultMsg; }
}