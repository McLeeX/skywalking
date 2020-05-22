package org.apache.skywalking.oap.server.core.source;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.DB_INSTANCE;
import lombok.Getter;
import lombok.Setter;

@ScopeDeclaration(id = DB_INSTANCE, name = "DBInstance")
@ScopeDefaultColumn.VirtualColumnDefinition(fieldName = "entityId", columnName = "entity_id", isID = true, type = String.class)
public class DBInstance extends Source {

    @Override
    public int scope() {
        return DefaultScopeDefine.DB_INSTANCE;
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
    private int latency;
    @Getter
    @Setter
    private boolean status;
}
