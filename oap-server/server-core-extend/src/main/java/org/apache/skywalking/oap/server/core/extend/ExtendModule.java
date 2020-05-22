package org.apache.skywalking.oap.server.core.extend;

import org.apache.skywalking.oap.server.core.cache.IDBInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceStatementInventoryCache;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceInventoryRegister;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceStatementInventoryRegister;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceInventoryCacheDAO;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceStatementInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;

public class ExtendModule extends ModuleDefine {

    public static final String NAME = "extend";

    public ExtendModule() {
        super(NAME);
    }

    @Override
    public Class[] services() {
        return new Class[]{
                IDBInstanceInventoryCache.class,
                IDBInstanceStatementInventoryCache.class,
                IDBInstanceInventoryRegister.class,
                IDBInstanceStatementInventoryRegister.class,
                IDBInstanceInventoryCacheDAO.class,
                IDBInstanceStatementInventoryCacheDAO.class
        };
    }
}
