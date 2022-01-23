package org.sec.core.spring;

import org.sec.model.MethodReference;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringMVC Controller中Mapping的封装
 */
public class SpringMapping {
    // 是否为rest：由类和@ResponseBody决定
    private boolean isRest;
    // 类关联
    private SpringController controller;
    // 方法名
    private MethodReference.Handle methodName;
    // 方法对象
    private MethodReference methodReference;
    // @RequestMapping中的路径
    private String path;
    // 从前端传递过来的参数Map：
    // 1.@RequestParam注解（已实现）
    // 2.直接以函数参数的方式传递
    // 3.封装一个对象传递（做起来可能比较麻烦）
    private List<SpringParam> paramMap = new ArrayList<>();

    public List<SpringParam> getParamMap() {
        return paramMap;
    }

    public void setParamMap(List<SpringParam> paramMap) {
        this.paramMap = paramMap;
    }

    public boolean isRest() {
        return isRest;
    }

    public void setRest(boolean rest) {
        isRest = rest;
    }

    public SpringController getController() {
        return controller;
    }

    public void setController(SpringController controller) {
        this.controller = controller;
    }

    public MethodReference.Handle getMethodName() {
        return methodName;
    }

    public void setMethodName(MethodReference.Handle methodName) {
        this.methodName = methodName;
    }

    public MethodReference getMethodReference() {
        return methodReference;
    }

    public void setMethodReference(MethodReference methodReference) {
        this.methodReference = methodReference;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
