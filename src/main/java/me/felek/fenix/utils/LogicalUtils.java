package me.felek.fenix.utils;

import me.felek.fenix.exceptions.FenixTypeException;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.impl.BoolValue;
import me.felek.fenix.type.impl.FloatValue;
import me.felek.fenix.type.impl.IntValue;

public class LogicalUtils {
    public static Value and(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() & value2.asInt());
            }
            case BOOL -> {
                return new BoolValue(value1.asBool() && value2.asBool());
            }
            case STRING -> {
                return new BoolValue(value1.asString().equals(value2.asString()));
            }
            default -> {
                throw new FenixTypeException();
            }
        }
    }

    public static Value or(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() | value2.asInt());
            }
            case BOOL -> {
                return new BoolValue(value1.asBool() || value2.asBool());
            }
            default -> {
                throw new FenixTypeException();
            }
        }
    }

    public static Value xor(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() ^ value2.asInt());
            }
            case BOOL -> {
                return new BoolValue(value1.asBool() ^ value2.asBool());
            }
            default -> {
                throw new FenixTypeException();
            }
        }
    }
}
