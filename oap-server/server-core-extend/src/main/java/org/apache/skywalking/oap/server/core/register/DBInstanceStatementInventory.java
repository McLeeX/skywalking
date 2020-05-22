package org.apache.skywalking.oap.server.core.register;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.DB_INSTANCE_STATEMENT_INVENTORY;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.register.worker.InventoryStreamProcessor;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.source.ScopeDeclaration;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;

@ScopeDeclaration(id = DB_INSTANCE_STATEMENT_INVENTORY, name = "DBInstanceStatementInventory")
@Stream(name = DBInstanceStatementInventory.INDEX_NAME, scopeId = DefaultScopeDefine.DB_INSTANCE_STATEMENT_INVENTORY, builder = DBInstanceStatementInventory.Builder.class, processor = InventoryStreamProcessor.class)
public class DBInstanceStatementInventory extends RegisterSource {

    public static final String INDEX_NAME = "db_instance_statement_inventory";

    public static final String DB_INSTANCE_ID = "db_instance_id";
    public static final String DB_URL = "db_url";
    public static final String DB_STATEMENT = "db_statement";

    @Getter
    @Setter
    @Column(columnName = DB_INSTANCE_ID)
    private int dbInstanceId;
    @Getter
    @Setter
    @Column(columnName = DB_URL)
    private String dbUrl;
    @Getter
    @Setter
    @Column(columnName = DB_STATEMENT)
    private String dbStatement;

    @Override
    public String id() {
        return buildId(dbInstanceId, dbStatement);
    }

    public DBInstanceStatementInventory getClone() {
        DBInstanceStatementInventory inventory = new DBInstanceStatementInventory();
        inventory.setDbInstanceId(getDbInstanceId());
        inventory.setDbUrl(getDbUrl());
        inventory.setDbStatement(getDbStatement());
        inventory.setHeartbeatTime(getHeartbeatTime());
        inventory.setLastUpdateTime(getLastUpdateTime());
        inventory.setRegisterTime(getRegisterTime());
        inventory.setSequence(getSequence());
        return inventory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBInstanceStatementInventory)) return false;
        DBInstanceStatementInventory that = (DBInstanceStatementInventory) o;
        return Objects.equals(dbInstanceId, that.dbInstanceId) &&
                Objects.equals(dbStatement, that.dbStatement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbInstanceId, dbStatement);
    }

    @Override
    public RemoteData.Builder serialize() {
        RemoteData.Builder remoteBuilder = RemoteData.newBuilder();
        remoteBuilder.addDataIntegers(getSequence());
        remoteBuilder.addDataIntegers(dbInstanceId);

        remoteBuilder.addDataStrings(dbUrl);
        remoteBuilder.addDataStrings(dbStatement);

        remoteBuilder.addDataLongs(getRegisterTime());
        remoteBuilder.addDataLongs(getHeartbeatTime());
        return remoteBuilder;
    }

    @Override
    public void deserialize(RemoteData remoteData) {
        setSequence(remoteData.getDataIntegers(0));
        setDbInstanceId(remoteData.getDataIntegers(1));

        setDbUrl(remoteData.getDataStrings(0));
        setDbStatement(remoteData.getDataStrings(1));

        setRegisterTime(remoteData.getDataLongs(0));
        setHeartbeatTime(remoteData.getDataLongs(1));
    }

    @Override
    public int remoteHashCode() {
        return 0;
    }

    @Override
    public boolean combine(RegisterSource registerSource) {
        return super.combine(registerSource);
    }

    public static String buildId(int dbInstanceId, String dbStatement) {
        return dbInstanceId + Const.ID_SPLIT + sqlSignature(dbStatement);
    }


    public static class Builder implements StorageBuilder<DBInstanceStatementInventory> {

        @Override
        public DBInstanceStatementInventory map2Data(Map<String, Object> dbMap) {
            DBInstanceStatementInventory inventory = new DBInstanceStatementInventory();
            inventory.setSequence((Integer) dbMap.get(SEQUENCE));
            inventory.setDbInstanceId((Integer) dbMap.get(DB_INSTANCE_ID));
            inventory.setDbUrl((String) dbMap.get(DB_URL));
            inventory.setDbStatement((String) dbMap.get(DB_STATEMENT));
            inventory.setRegisterTime((Long) dbMap.get(REGISTER_TIME));
            inventory.setHeartbeatTime((Long) dbMap.get(HEARTBEAT_TIME));
            return inventory;
        }

        @Override
        public Map<String, Object> data2Map(DBInstanceStatementInventory storageData) {
            Map<String, Object> map = new HashMap<>();
            map.put(SEQUENCE, storageData.getSequence());
            map.put(DB_INSTANCE_ID, storageData.getDbInstanceId());
            map.put(DB_URL, storageData.getDbUrl());
            map.put(DB_STATEMENT, storageData.getDbStatement());
            map.put(REGISTER_TIME, storageData.getRegisterTime());
            map.put(HEARTBEAT_TIME, storageData.getHeartbeatTime());
            return map;
        }
    }

    private static String sqlSignature(String origString) {
        return DigestUtils.md5Hex(origString) + Const.ID_SPLIT + Optional.ofNullable(origString).map(String::length).orElse(0);
    }
}
