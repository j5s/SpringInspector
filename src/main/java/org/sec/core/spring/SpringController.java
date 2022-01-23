package org.sec.core.spring;

import org.sec.model.ClassReference;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringMVC中Controller的封装
 */
public class SpringController {
    // 是否为rest：由@RestController决定
    private boolean isRest;
    // Controller对应的真实类名
    private ClassReference.Handle className;
    // 类对象：方便调用
    private ClassReference classReference;
    // 每个Controller中都应该包含多个Mapping
    private final List<SpringMapping> mappings = new ArrayList<>();

    public boolean isRest() {
        return isRest;
    }

    public void setRest(boolean rest) {
        isRest = rest;
    }

    public ClassReference.Handle getClassName() {
        return className;
    }

    public void setClassName(ClassReference.Handle className) {
        this.className = className;
    }

    public ClassReference getClassReference() {
        return classReference;
    }

    public void setClassReference(ClassReference classReference) {
        this.classReference = classReference;
    }

    public List<SpringMapping> getMappings() {
        return mappings;
    }

    public void addMapping(SpringMapping mapping) {
        this.mappings.add(mapping);
    }
}
