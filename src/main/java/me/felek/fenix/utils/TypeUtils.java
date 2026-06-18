package me.felek.fenix.utils;

import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

import java.util.Locale;

public class TypeUtils {
    public static ValueType getTypeFromString(String type) {
        switch (type.toLowerCase()) {
            case "int" -> {
                return ValueType.INT;
            }
            case "string" -> {
                return ValueType.STRING;
            }
            case "bool" -> {
                return ValueType.BOOL;
            }
            case "float" -> {
                return ValueType.FLOAT;
            }
            default -> {
                return ValueType.NULL;
            }
        }
    }
}
