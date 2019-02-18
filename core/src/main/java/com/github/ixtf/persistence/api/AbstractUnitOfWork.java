package com.github.ixtf.persistence.api;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author jzb 2019-02-18
 */
public abstract class AbstractUnitOfWork implements UnitOfWork {
    protected final List<Object> newList = Lists.newArrayList();
    protected final List<Object> dirtyList = Lists.newArrayList();
    protected final List<Object> cleanList = Lists.newArrayList();
    protected final List<Object> deleteList = Lists.newArrayList();

    @Override
    synchronized public UnitOfWork registerNew(Object o) {
        if (!newList.contains(o)) {
            newList.add(o);
        }
        return this;
    }

    @Override
    synchronized public UnitOfWork registerDirty(Object o) {
        if (!dirtyList.contains(o)) {
            dirtyList.add(o);
        }
        return this;
    }

    @Override
    synchronized public UnitOfWork registerClean(Object o) {
        if (!cleanList.contains(o)) {
            cleanList.add(o);
        }
        return this;
    }

    @Override
    synchronized public UnitOfWork registerDelete(Object o) {
        if (!deleteList.contains(o)) {
            deleteList.add(o);
        }
        return this;
    }
}
