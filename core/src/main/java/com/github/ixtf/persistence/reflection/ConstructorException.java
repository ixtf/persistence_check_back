package com.github.ixtf.persistence.reflection;

public class ConstructorException extends RuntimeException {

    public ConstructorException(Class clazz) {
        super("This class must have a no arg with either public and default visibility: " + clazz.getName());
    }
}