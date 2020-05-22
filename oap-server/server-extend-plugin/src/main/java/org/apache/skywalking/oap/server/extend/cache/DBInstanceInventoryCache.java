/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.extend.cache;

import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.extend.ExtendModule;
import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

@Slf4j
public class DBInstanceInventoryCache implements IDBInstanceInventoryCache {

    private final ModuleManager moduleManager;
    private final Cache<String, Integer> dbInstanceNameCache;
    private final Cache<Integer, DBInstanceInventory> dbInstanceIdCache;

    private IDBInstanceInventoryCacheDAO cacheDAO;

    public DBInstanceInventoryCache(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;

        int initialCapacitySize = 1_000_00;

        dbInstanceNameCache = CacheBuilder.newBuilder()
                                          .initialCapacity(initialCapacitySize)
                                          .maximumSize(1_000_000L)
                                          .build();
        dbInstanceIdCache = CacheBuilder.newBuilder()
                                        .initialCapacity(initialCapacitySize)
                                        .maximumSize(1_000_000L)
                                        .build();
    }

    private IDBInstanceInventoryCacheDAO getCacheDAO() {
        if (isNull(cacheDAO)) {
            cacheDAO = moduleManager.find(ExtendModule.NAME).provider().getService(IDBInstanceInventoryCacheDAO.class);
        }
        return cacheDAO;
    }

    @Override
    public int getDbInstanceInventoryId(int serviceId, String dbInstance) {
        String id = DBInstanceInventory.buildId(serviceId, dbInstance);

        Integer dbInstanceId = dbInstanceNameCache.getIfPresent(id);

        if (Objects.isNull(dbInstanceId) || dbInstanceId == Const.NONE) {
            dbInstanceId = getCacheDAO().getDBInstanceId(serviceId, dbInstance);
            if (dbInstanceId != Const.NONE) {
                dbInstanceNameCache.put(id, dbInstanceId);
            }
        }
        return dbInstanceId;
    }

    @Override
    public DBInstanceInventory get(int dbInstanceId) {

        DBInstanceInventory dbInstanceInventory = dbInstanceIdCache.getIfPresent(dbInstanceId);

        if (isNull(dbInstanceInventory)) {
            dbInstanceInventory = getCacheDAO().get(dbInstanceId);
            if (nonNull(dbInstanceInventory)) {
                dbInstanceIdCache.put(dbInstanceId, dbInstanceInventory);
            } else {
                log.warn("DBInstanceInventory id {} is not in cache and persistent storage.", dbInstanceId);
            }
        }

        return dbInstanceInventory;
    }
}
