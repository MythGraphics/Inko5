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

public enum InkoType {

    ABLEITEND(      'a', "ableitend"        ),
    SAUGEND(        's', "saugend"          ),
    SAUGEND_KIND(   'k', "saugend (Kind)"   );

    private final char code;
    private final String label;

    InkoType(char code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InkoType fromCode(char code) {
        for ( InkoType t : values() ) {
            if (t.code == code) {
                return t;
            }
        }
        return SAUGEND;
    }

    public String getLabel() {
        return label;
    }

    public char getCode() {
        return code;
    }

}
