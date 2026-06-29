/*
 *
 */

package util;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import java.util.Optional;

public class EnumHelper {

    private EnumHelper() {}

    /**
     * Wandelt einen String in den entsprechenden Enum-Wert um.
     * @param <T>        Der Typ des Enums
     * @param enumClass  Die Klassen-Referenz des Enums
     * @param value      Der String-Name des Enum-Werts
     * @return           Der passende Enum-Wert oder NULL, wenn keiner passt
     * @throws IllegalArgumentException falls der String nicht passt
     */
    public static <T extends Enum<T>> T getEnumFromString(Class<T> enumClass, String value)
    throws IllegalArgumentException {
        validateInput(enumClass, value);
        for ( T constant : enumClass.getEnumConstants() ) {
            if ( constant.name().equalsIgnoreCase( value.trim() )) {
                return constant;
            }
        }
        return null;
    }

    public static <T extends Enum<T>> Optional<T> getEnumFromStringAsOptional(Class<T> enumClass, String value)
    throws IllegalArgumentException {
        try {
            return Optional.ofNullable( getEnumFromString( enumClass, value ));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static void validateInput(Class<?> enumClass, String value) throws IllegalArgumentException{
        if (value == null || enumClass == null ) {
            throw new IllegalArgumentException("Enum-Klasse oder String ist null.");
        }
        if ( value.isEmpty() ) {
            throw new IllegalArgumentException("String ist leer.");
        }
    }

}
