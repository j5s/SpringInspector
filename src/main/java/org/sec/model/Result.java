package org.sec.model;

import java.util.Objects;

public class Result {
    private final ClassReference classReference;
    private final MethodReference methodReference;

    public Result(ClassReference classReference, MethodReference methodReference) {
        this.classReference = classReference;
        this.methodReference = methodReference;
    }

    public ClassReference getClassReference() {
        return classReference;
    }

    public MethodReference getMethodReference() {
        return methodReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Result result = (Result) o;
        return Objects.equals(classReference, result.classReference)
                && Objects.equals(methodReference, result.methodReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classReference, methodReference);
    }
}
