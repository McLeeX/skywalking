package org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener.endpoint;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.source.DBInstance;
import org.apache.skywalking.oap.server.core.source.DBInstanceStatement;

/**
 * custom_extend
 */
class DBSourceBuilder {

    @Getter
    @Setter
    private SourceBuilder sourceBuilder;
    @Setter
    @Getter
    private String dbInstance;
    @Setter
    @Getter
    private String dbStatement;
    @Setter
    @Getter
    private String dbBindVariables;
    @Setter
    @Getter
    private String dbType;
    @Setter
    @Getter
    private int dbInstanceId;
    @Setter
    @Getter
    private String dbInstanceName;
    @Setter
    @Getter
    private int dbInstanceStatementId;
    @Setter
    @Getter
    private String dbInstanceStatementName;

    private String dbUrl;

    private String getDbUrl() {
        if (dbUrl == null) {
            dbUrl = DBInstanceInventory.buildDBUrl(sourceBuilder.getDestServiceName(), dbInstance);
        }
        return dbUrl;
    }

    public DBSourceBuilder(SourceBuilder sourceBuilder) {
        this.setSourceBuilder(sourceBuilder);
    }

    public DBInstanceStatement toDBInstanceStatement() {
        DBInstanceStatement dbInstanceStatement = new DBInstanceStatement();
        dbInstanceStatement.setTimeBucket(this.sourceBuilder.getTimeBucket());
        dbInstanceStatement.setId(this.dbInstanceStatementId);
        dbInstanceStatement.setName(this.dbInstanceStatementName);
        dbInstanceStatement.setUrl(getDbUrl());
        dbInstanceStatement.setStatement(this.dbStatement);
        dbInstanceStatement.setLatency(this.sourceBuilder.getLatency());
        dbInstanceStatement.setStatus(this.sourceBuilder.isStatus());
        return dbInstanceStatement;
    }

    public DBInstance toDBInstance() {
        DBInstance dbInstance = new DBInstance();
        dbInstance.setTimeBucket(this.sourceBuilder.getTimeBucket());
        dbInstance.setId(this.dbInstanceId);
        dbInstance.setName(this.dbInstanceName);
        dbInstance.setUrl(getDbUrl());
        dbInstance.setLatency(this.sourceBuilder.getLatency());
        dbInstance.setStatus(this.sourceBuilder.isStatus());
        return dbInstance;
    }
}
