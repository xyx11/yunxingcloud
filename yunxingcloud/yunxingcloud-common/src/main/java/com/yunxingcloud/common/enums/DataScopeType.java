package com.yunxingcloud.common.enums;

public enum DataScopeType {
    ALL("1", "全部"), CUSTOM("2", "自定义"), DEPT("3", "本部门"), DEPT_AND_CHILD("4", "本部门及以下"), SELF("5", "仅本人");
    private final String code; private final String name;
    DataScopeType(String code, String name) { this.code = code; this.name = name; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
