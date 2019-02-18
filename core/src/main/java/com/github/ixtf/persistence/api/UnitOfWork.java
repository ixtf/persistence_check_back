package com.github.ixtf.persistence.api;

/**
 * @author jzb 2019-02-18
 */
public interface UnitOfWork {

    UnitOfWork registerNew(Object o);

    UnitOfWork registerDirty(Object o);

    UnitOfWork registerClean(Object o);

    UnitOfWork registerDelete(Object o);

    UnitOfWork commit();

    UnitOfWork rollback();
}
