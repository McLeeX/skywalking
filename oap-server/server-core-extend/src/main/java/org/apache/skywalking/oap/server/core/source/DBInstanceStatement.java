package org.apache.skywalking.oap.server.core.source;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.DB_INSTANCE_STATEMENT;
import lombok.Getter;
import lombok.Setter;

@ScopeDeclaration(id = DB_INSTANCE_STATEMENT, name = "DBInstanceStatement")
@ScopeDefaultColumn.VirtualColumnDefinition(fieldName = "entityId", columnName = "entity_id", isID = true, type = String.class)
public class DBInstanceStatement extends Source {

    @Override
    public int scope() {
        return DefaultScopeDefine.DB_INSTANCE_STATEMENT;
    }

    @Override
    public String getEntityId() {
        return String.valueOf(id);
    }

    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "name", requireDynamicActive = true)
    private String name;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "url")
    private String url;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "statement")
    private String statement;
    @Getter
    @Setter
    private int latency;
    @Getter
    @Setter
    private boolean status;

}
