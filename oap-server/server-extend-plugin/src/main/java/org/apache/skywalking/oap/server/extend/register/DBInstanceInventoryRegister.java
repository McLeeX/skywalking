package org.apache.skywalking.oap.server.extend.register;

import java.util.Objects;

import static java.util.Objects.isNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.extend.ExtendModule;
import org.apache.skywalking.oap.server.extend.cache.DBInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.ServiceInventoryCache;
import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.register.ServiceInventory;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceInventoryRegister;
import org.apache.skywalking.oap.server.core.register.worker.InventoryStreamProcessor;
import org.apache.skywalking.oap.server.library.module.ModuleDefineHolder;

@Slf4j
public class DBInstanceInventoryRegister implements IDBInstanceInventoryRegister {

    private final ModuleDefineHolder moduleDefineHolder;
    private IDBInstanceInventoryCache cacheService;
    private ServiceInventoryCache serviceInventoryCache;

    public DBInstanceInventoryRegister(ModuleDefineHolder moduleDefineHolder) {
        this.moduleDefineHolder = moduleDefineHolder;
    }

    private IDBInstanceInventoryCache getCacheService() {
        if (isNull(cacheService)) {
            cacheService = moduleDefineHolder.find(ExtendModule.NAME).provider().getService(IDBInstanceInventoryCache.class);
        }
        return cacheService;
    }

    private ServiceInventoryCache getServiceInventoryCacheService() {
        if (isNull(serviceInventoryCache)) {
            serviceInventoryCache = moduleDefineHolder.find(CoreModule.NAME).provider().getService(ServiceInventoryCache.class);
        }
        return serviceInventoryCache;
    }

    @Override
    public int getOrCreate(int serviceId, String dbInstance, int dbType) {
        int dbInstanceId = getCacheService().getDbInstanceInventoryId(serviceId, dbInstance);
        ServiceInventory serviceInventory = getServiceInventoryCacheService().get(serviceId);

        if (dbInstanceId == Const.NONE) {
            String dbUrl = DBInstanceInventory.buildDBUrl(serviceInventory.getName(), dbInstance);

            DBInstanceInventory dbInstanceInventory = new DBInstanceInventory();
            dbInstanceInventory.setServiceId(serviceId);
            dbInstanceInventory.setDbInstance(dbInstance);
            dbInstanceInventory.setDbUrl(dbUrl);
            dbInstanceInventory.setDbType(dbType);

            long now = System.currentTimeMillis();
            dbInstanceInventory.setRegisterTime(now);
            dbInstanceInventory.setHeartbeatTime(now);

            InventoryStreamProcessor.getInstance().in(dbInstanceInventory);
        }
        return dbInstanceId;
    }

    @Override
    public int get(int serviceId, String dbInstance, int dbType) {
        return getCacheService().getDbInstanceInventoryId(serviceId, dbInstance);
    }

    @Override
    public void heartbeat(int dbInstanceId, long heartBeatTime) {
        DBInstanceInventory dbInstanceInventory = getCacheService().get(dbInstanceId);
        if (Objects.nonNull(dbInstanceInventory)) {
            dbInstanceInventory.setHeartbeatTime(heartBeatTime);

            InventoryStreamProcessor.getInstance().in(dbInstanceInventory);
        } else {
            log.warn("DBInstance {} heartbeat, but not found in storage.", dbInstanceId);
        }
    }
}
