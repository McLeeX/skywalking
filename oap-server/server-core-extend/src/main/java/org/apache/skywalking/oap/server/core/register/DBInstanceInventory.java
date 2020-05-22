package org.apache.skywalking.oap.server.core.register;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.DB_INSTANCE_INVENTORY;
import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.register.worker.InventoryStreamProcessor;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.source.ScopeDeclaration;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;

@ScopeDeclaration(id = DB_INSTANCE_INVENTORY, name = "DBInstanceInventory")
@Stream(name = DBInstanceInventory.INDEX_NAME, scopeId = DB_INSTANCE_INVENTORY, builder = DBInstanceInventory.Builder.class, processor = InventoryStreamProcessor.class)
public class DBInstanceInventory extends RegisterSource {

    public static final String INDEX_NAME = "db_instance_inventory";

    public static final String SERVICE_ID = "service_id";
    public static final String DB_INSTANCE = "db_instance";
    public static final String DB_URL = "db_url";
    public static final String DB_TYPE = "db_type";

    @Getter
    @Setter
    @Column(columnName = SERVICE_ID)
    private int serviceId;

    @Getter
    @Setter
    @Column(columnName = DB_INSTANCE)
    private String dbInstance;

    @Getter
    @Setter
    @Column(columnName = DB_URL)
    private String dbUrl;

    @Getter
    @Setter
    @Column(columnName = DB_TYPE)
    private int dbType;

    @Override
    public String id() {
        return buildId(serviceId, dbInstance);
    }


    public DBInstanceInventory getClone() {
        DBInstanceInventory inventory = new DBInstanceInventory();
        inventory.setDbType(getDbType());
        inventory.setDbInstance(getDbInstance());
        inventory.setDbUrl(getDbUrl());
        inventory.setServiceId(getServiceId());
        inventory.setHeartbeatTime(getHeartbeatTime());
        inventory.setLastUpdateTime(getLastUpdateTime());
        inventory.setRegisterTime(getRegisterTime());
        inventory.setSequence(getSequence());
        return inventory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, dbInstance, dbUrl, dbType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBInstanceInventory)) return false;
        DBInstanceInventory that = (DBInstanceInventory) o;
        return serviceId == that.serviceId &&
                dbType == that.dbType &&
                Objects.equals(dbUrl, that.dbUrl) &&
                Objects.equals(dbInstance, that.dbInstance);
    }

    @Override
    public RemoteData.Builder serialize() {
        RemoteData.Builder remoteBuilder = RemoteData.newBuilder();
        remoteBuilder.addDataIntegers(getSequence());
        remoteBuilder.addDataIntegers(serviceId);
        remoteBuilder.addDataIntegers(dbType);
        remoteBuilder.addDataStrings(dbInstance);
        remoteBuilder.addDataStrings(dbUrl);
        remoteBuilder.addDataLongs(getRegisterTime());
        remoteBuilder.addDataLongs(getHeartbeatTime());
        return remoteBuilder;
    }

    @Override
    public void deserialize(RemoteData remoteData) {
        setSequence(remoteData.getDataIntegers(0));
        setServiceId(remoteData.getDataIntegers(1));
        setDbType(remoteData.getDataIntegers(2));

        setDbInstance(remoteData.getDataStrings(0));
        setDbUrl(remoteData.getDataStrings(1));

        setRegisterTime(remoteData.getDataLongs(0));
        setHeartbeatTime(remoteData.getDataLongs(1));
    }

    @Override
    public int remoteHashCode() {
        return 0;
    }

    @Override
    public boolean combine(RegisterSource registerSource) {
        boolean isChanged = super.combine(registerSource);
        DBInstanceInventory dbSqlInventory = (DBInstanceInventory) registerSource;

        if (dbSqlInventory.getLastUpdateTime() >= this.getLastUpdateTime()) {
            this.dbType = dbSqlInventory.getDbType();
            this.dbUrl = dbSqlInventory.getDbUrl();
            isChanged = true;
        }

        return isChanged;
    }

    public static String buildId(int serviceId, String dbInstance) {
        return serviceId + Const.ID_SPLIT + dbInstance;
    }

    public static String buildDBUrl(String serviceName, String dbInstance) {
        return serviceName + "/" + dbInstance;
    }

    public static class Builder implements StorageBuilder<DBInstanceInventory> {

        @Override
        public DBInstanceInventory map2Data(Map<String, Object> dbMap) {
            DBInstanceInventory inventory = new DBInstanceInventory();
            inventory.setSequence((Integer) dbMap.get(SEQUENCE));
            inventory.setServiceId((Integer) dbMap.get(SERVICE_ID));
            inventory.setDbInstance((String) dbMap.get(DB_INSTANCE));
            inventory.setDbUrl((String) dbMap.get(DB_URL));
            inventory.setDbType((Integer) dbMap.get(DB_TYPE));
            inventory.setRegisterTime((Long) dbMap.get(REGISTER_TIME));
            inventory.setHeartbeatTime((Long) dbMap.get(HEARTBEAT_TIME));
            return inventory;
        }

        @Override
        public Map<String, Object> data2Map(DBInstanceInventory storageData) {
            Map<String, Object> map = new HashMap<>();
            map.put(SEQUENCE, storageData.getSequence());
            map.put(SERVICE_ID, storageData.getServiceId());
            map.put(DB_INSTANCE, storageData.getDbInstance());
            map.put(DB_URL, storageData.getDbUrl());
            map.put(DB_TYPE, storageData.getDbType());
            map.put(REGISTER_TIME, storageData.getRegisterTime());
            map.put(HEARTBEAT_TIME, storageData.getHeartbeatTime());
            return map;
        }
    }

}
