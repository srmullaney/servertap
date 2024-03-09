package io.servertap.utils;

import java.util.UUID;

public class ValidationUtils {

    public static UUID safeUUID(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

}
