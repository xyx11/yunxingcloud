package com.yunxingcloud.common.core;

public class TenantContext {
    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();
    private static final String DEFAULT_TENANT = "default";

    public static void setTenantId(String tenantId) {
        TENANT_HOLDER.set(tenantId != null ? tenantId : DEFAULT_TENANT);
    }

    public static String getTenantId() {
        return TENANT_HOLDER.get() != null ? TENANT_HOLDER.get() : DEFAULT_TENANT;
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
