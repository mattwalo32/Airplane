package com.walowtech.plane.util;

import java.security.InvalidParameterException;

/**
 * Utility class for maintaining the integrity of the code and its variables.
 * Useful for debugging.
 *
 * @author Matthew Walowski
 * @version 1.0.0
 * @since 2018-08-09
 */
public class CodeIntegrityUtils {

    /**
     * Throws an exception if the given object is null.
     *
     * @param pObject Object to check
     * @param pMessage Message to display in exception
     */
    public static void checkNotNull(Object pObject, String pMessage) {
        if(pObject == null)
            throw new InvalidParameterException(pMessage);
    }

    /**
     * Throws an exception if the given string is empty or null.
     *
     * @param pString String to check.
     * @param pMessage Message to display in exception.
     */
    public static void checkNotEmpty(String pString, String pMessage) {
        if(pString == null || pString.trim().equals(""))
            throw new InvalidParameterException(pMessage);
    }
}
