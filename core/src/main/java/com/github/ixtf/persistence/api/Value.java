package com.github.ixtf.persistence.api;


/**
 * db 中的字段值
 *
 * @author jzb 2019-02-14
 */
public interface Value {

    /**
     * 未转换的值.
     */
    Object get();

    /**
     * 转换为指定类型
     */
    <T> T get(Class<T> clazz);

}

