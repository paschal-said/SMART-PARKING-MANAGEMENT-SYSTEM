package com.anpr.parking.common;

import java.util.Locale;
import java.util.regex.Pattern;

public final class PlateUtil {
    private static final Pattern VALID = Pattern.compile("^[A-Z0-9]{4,12}$");

    private PlateUtil() {}

    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    public static boolean isValid(String normalized) {
        return normalized != null && VALID.matcher(normalized).matches();
    }
}
