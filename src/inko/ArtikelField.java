/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.1
 *
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum ArtikelField {

    ID("id", "int unsigned not null auto_increment primary key", "ID", Integer.class),
    NAME("name", "text", "Name", String.class),
    PZN("pzn", "int unsigned", "PZN", Integer.class),
    SIZE("size", "tinytext", "Größe", Size.class),
    PACK_QUANTITY("packQuantity", "tinyint unsigned", "Packungsgröße", String.class),
    TYPE("type", "tinytext", "Typ", ArtikelType.class);

    public final static List<ArtikelField> INSERT_FIELDS;
    public final static String INSERT_PLACEHOLDERS;
    public final static String INSERT_COLUMNS;

    static {
        // alle DB-Felder außer ID (für Insert)
        INSERT_FIELDS = Collections.unmodifiableList(
            Arrays.stream( values() )
                  .filter(f -> f != ID)
                  .collect( Collectors.toList() )
        );

        INSERT_PLACEHOLDERS = INSERT_FIELDS.stream()
            .map(f -> "?")
            .collect( Collectors.joining( ", " ));

        INSERT_COLUMNS = INSERT_FIELDS.stream()
            .map(f -> f.dbName)
            .collect( Collectors.joining( ", " ));
    }

    private final String dbName;
    private final String dbType;
    private final String uiName;
    private final Class<?> type;

    ArtikelField(String dbName, String dbType, String uiName, Class<?> type) {
        this.dbName = dbName;
        this.dbType = dbType;
        this.uiName = uiName;
        this.type = type;
    }

    public String getDBName() {
        return dbName;
    }

    public String getDBType() {
        return dbType;
    }

    public String getUIName() {
        return uiName;
    }

    public Class<?> getType() {
        return type;
    }

}
