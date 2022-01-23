package org.sec.core.spring;

/**
 * 每个Mapping中都会有对应的参数
 */
public class SpringParam {
    // 参数索引位置
    private int paramIndex;
    // 参数名
    private String paramName;
    // 参数类型
    private String paramType;
    // 对应前端在请求中的参数名：
    // 1.@RequestParam可以定义不同的（已处理）
    // 2.其他情况下应当和参数名一致
    private String reqName;

    public int getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getReqName() {
        return reqName;
    }

    public void setReqName(String reqName) {
        this.reqName = reqName;
    }
}
