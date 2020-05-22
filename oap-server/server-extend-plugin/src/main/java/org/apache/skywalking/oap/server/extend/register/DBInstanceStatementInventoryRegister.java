package org.apache.skywalking.oap.server.extend.register;

import java.util.Objects;

import static java.util.Objects.isNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceStatementInventoryCache;
import org.apache.skywalking.oap.server.core.extend.ExtendModule;
import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceStatementInventoryRegister;
import org.apache.skywalking.oap.server.core.register.worker.InventoryStreamProcessor;
import org.apache.skywalking.oap.server.library.module.ModuleDefineHolder;

@Slf4j
public class DBInstanceStatementInventoryRegister implements IDBInstanceStatementInventoryRegister {

    private final ModuleDefineHolder moduleDefineHolder;
    private IDBInstanceStatementInventoryCache cacheService;
    private IDBInstanceInventoryCache dbInstanceInventoryCache;

    public DBInstanceStatementInventoryRegister(ModuleDefineHolder moduleDefineHolder) {
        this.moduleDefineHolder = moduleDefineHolder;
    }

    private IDBInstanceStatementInventoryCache getCacheService() {
        if (isNull(cacheService)) {
            cacheService = moduleDefineHolder.find(ExtendModule.NAME).provider().getService(IDBInstanceStatementInventoryCache.class);
        }
        return cacheService;
    }

    private IDBInstanceInventoryCache getDBInstanceInventoryCache() {
        if (isNull(dbInstanceInventoryCache)) {
            dbInstanceInventoryCache = moduleDefineHolder.find(ExtendModule.NAME).provider().getService(IDBInstanceInventoryCache.class);
        }
        return dbInstanceInventoryCache;
    }

    @Override
    public int getOrCreate(int dbInstanceId, String dbStatement) {
        int dbInstanceStatementId = getCacheService().getDBInstanceStatementId(dbInstanceId, dbStatement);
        DBInstanceInventory dbInstanceInventory = getDBInstanceInventoryCache().get(dbInstanceId);
        if (dbInstanceStatementId == Const.NONE) {
            DBInstanceStatementInventory dbInstanceStatementInventory = new DBInstanceStatementInventory();
            dbInstanceStatementInventory.setDbInstanceId(dbInstanceId);
            dbInstanceStatementInventory.setDbUrl(dbInstanceInventory.getDbUrl());
            dbInstanceStatementInventory.setDbStatement(dbStatement);

            long now = System.currentTimeMillis();
            dbInstanceStatementInventory.setRegisterTime(now);
            dbInstanceStatementInventory.setHeartbeatTime(now);

            InventoryStreamProcessor.getInstance().in(dbInstanceStatementInventory);
        }
        return dbInstanceStatementId;
    }

    @Override
    public int get(int dbInstanceId, String dbStatement) {
        return getCacheService().getDBInstanceStatementId(dbInstanceId, dbStatement);
    }

    @Override
    public void heartbeat(int dbInstanceStatementId, long heartBeatTime) {
        DBInstanceStatementInventory dbInstanceStatementInventory = getCacheService().get(dbInstanceStatementId);
        if (Objects.nonNull(dbInstanceStatementInventory)) {
            dbInstanceStatementInventory.setHeartbeatTime(heartBeatTime);

            InventoryStreamProcessor.getInstance().in(dbInstanceStatementInventory);
        } else {
            log.warn("DBInstanceStatement {} heartbeat, but not found in storage.", dbInstanceStatementId);
        }
    }
}
