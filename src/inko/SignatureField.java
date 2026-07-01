/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import java.awt.image.BufferedImage;
import java.time.LocalDate;

public enum SignatureField {

    PATIENT_ID("p_id", "int unsigned not null", Integer.class, null),
    SIGN_BERATUNG("UnterschriftBeratung", "mediumblob", BufferedImage.class, "⚕sign_beratung⚕"),
    DATE_BERATUNG("DatumBeratung", "date", LocalDate.class, "⚕date_beratung⚕"),
    SIGN_BINDUNG("UnterschriftBindung", "mediumblob", BufferedImage.class, "⚕sign_bindung⚕"),
    DATE_BINDUNG("DatumBindung", "date", LocalDate.class, "⚕date_bindung⚕"),
    SIGN_MEHRKOSTEN("UnterschriftMehrkosten", "mediumblob", BufferedImage.class, "⚕sign_mehrkosten⚕"),
    DATE_MEHRKOSTEN("DatumMehrkosten", "date", LocalDate.class, "⚕date_mehrkosten⚕");

    private final String dbName;
    private final String dbType;
    private final Class<?> type;
    private final String template;

    SignatureField(String dbName, String dbType, Class<?> type, String template) {
        this.dbName = dbName;
        this.dbType = dbType;
        this.type = type;
        this.template = template;
    }

    public String getDBName() {
        return dbName;
    }

    public String getDBType() {
        return dbType;
    }

    public Class<?> getType() {
        return type;
    }

    public String getTemplate() {
        return template;
    }

}
