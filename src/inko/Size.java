/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.3
 *
 */

public enum Size {

    KEINE,
    S,
    M,
    L,
    XL;

    public static Size parseSize(String s) {
        if ( s == null || s.isEmpty() ) {
            return KEINE;
        }
        switch ( s.toUpperCase() ) {
            case "S":   { return S; }
            case "M":   { return M; }
            case "L":   { return L; }
            case "XL":  { return XL; }
        }
        return KEINE;
    }

}
