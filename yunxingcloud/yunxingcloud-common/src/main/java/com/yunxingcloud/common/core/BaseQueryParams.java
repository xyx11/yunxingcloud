package com.yunxingcloud.common.core;

import com.yunxingcloud.common.annotation.DataScope;

public class BaseQueryParams {
    private DataScope dataScope;
    public DataScope getDataScope() { return dataScope; }
    public void setDataScope(DataScope dataScope) { this.dataScope = dataScope; }
}
