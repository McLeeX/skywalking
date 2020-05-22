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
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceStatementInventoryCache;
import org.apache.skywalking.oap.server.core.extend.ExtendModule;
import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceStatementInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBInstanceStatementInventoryCache implements IDBInstanceStatementInventoryCache {

    private static final Logger logger = LoggerFactory.getLogger(DBInstanceStatementInventoryCache.class);

    private final ModuleManager moduleManager;
    private final Cache<String, Integer> dbInstanceStatementNameCache;
    private final Cache<Integer, DBInstanceStatementInventory> dbInstanceStatementIdCache;

    private IDBInstanceStatementInventoryCacheDAO cacheDAO;

    public DBInstanceStatementInventoryCache(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;

        int initialCapacitySize = 1_000_00;

        dbInstanceStatementNameCache = CacheBuilder.newBuilder()
                                                   .initialCapacity(initialCapacitySize)
                                                   .maximumSize(1_000_000L)
                                                   .build();
        dbInstanceStatementIdCache = CacheBuilder.newBuilder()
                                                 .initialCapacity(initialCapacitySize)
                                                 .maximumSize(1_000_000L)
                                                 .build();
    }

    private IDBInstanceStatementInventoryCacheDAO getCacheDAO() {
        if (isNull(cacheDAO)) {
            cacheDAO = moduleManager.find(ExtendModule.NAME).provider().getService(IDBInstanceStatementInventoryCacheDAO.class);
        }
        return cacheDAO;
    }

    @Override
    public int getDBInstanceStatementId(int dbInstanceId, String dbStatement) {
        String id = DBInstanceStatementInventory.buildId(dbInstanceId, dbStatement);

        Integer dbInstanceStatementId = dbInstanceStatementNameCache.getIfPresent(id);

        if (Objects.isNull(dbInstanceStatementId) || dbInstanceStatementId == Const.NONE) {
            dbInstanceStatementId = getCacheDAO().getDBInstanceStatementId(dbInstanceId, dbStatement);
            if (dbInstanceStatementId != Const.NONE) {
                dbInstanceStatementNameCache.put(id, dbInstanceStatementId);
            }
        }
        return dbInstanceStatementId;
    }

    @Override
    public DBInstanceStatementInventory get(int dbInstanceStatementId) {
        DBInstanceStatementInventory dbInstanceStatementInventory = dbInstanceStatementIdCache.getIfPresent(dbInstanceStatementId);

        if (isNull(dbInstanceStatementInventory)) {
            dbInstanceStatementInventory = getCacheDAO().get(dbInstanceStatementId);
            if (nonNull(dbInstanceStatementInventory)) {
                dbInstanceStatementIdCache.put(dbInstanceStatementId, dbInstanceStatementInventory);
            } else {
                logger.warn("DBInstanceStatementInventory id {} is not in cache and persistent storage.", dbInstanceStatementId);
            }
        }

        return dbInstanceStatementInventory;
    }
}
