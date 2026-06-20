package me.felek.fenix.utils;

import me.felek.fenix.exceptions.FenixTypeException;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.impl.*;

public class ValueUtils {
    public static Value increment(Value value) {
        switch (value.getType()) {
            case INT -> {
                return new IntValue(value.asInt()+1);
            }
            case FLOAT -> {
                return new FloatValue(value.asFloat()+1);
            }
            case BOOL -> {
                return new BoolValue(!value.asBool());
            }
            default -> {
                throw new FenixTypeException();
            }
        }
    }

    public static Value decrement(Value value) {
        switch (value.getType()) {
            case INT -> {
                return new IntValue(value.asInt()-1);
            }
            case FLOAT -> {
                return new FloatValue(value.asFloat()-1);
            }
            case BOOL -> {
                return new BoolValue(!value.asBool());
            }
            default -> {
                throw new FenixTypeException();
            }
        }
    }

    public static Value negative(Value value) {
        switch (value.getType()) {
            case INT -> {
                return new IntValue(-value.asInt());
            }
            case FLOAT -> {
                return new FloatValue(-value.asFloat());
            }
            case BOOL -> {
                return new BoolValue(!value.asBool());
            }
            case STRING -> {
                return new StringValue(new StringBuilder(value.asString()).reverse().toString());
            }
        }

        return new NullValue();
    }

    public static Value mul(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() * value2.asInt());
            }
            case FLOAT -> {
                return new FloatValue(value1.asFloat() * value2.asFloat());
            }
            default -> {//String: "abc"*3, true*true=true&&true
                throw new FenixTypeException();
            }
        }
    }

    public static Value div(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() / value2.asInt());
            }
            case FLOAT -> {
                return new FloatValue(value1.asFloat() / value2.asFloat());
            }
            default -> {//String: "abc"*3, true*true=true&&true
                throw new FenixTypeException();
            }
        }
    }

    public static Value per(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() % value2.asInt());
            }
            case FLOAT -> {
                return new FloatValue(value1.asFloat() % value2.asFloat());
            }
            default -> {//String: "abc"*3, true*true=true&&true
                throw new FenixTypeException();
            }
        }
    }

    public static Value add(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() + value2.asInt());
            }
            case FLOAT -> {
                return new FloatValue(value1.asFloat() + value2.asFloat());
            }
            case STRING -> {
                return new StringValue(value1.asString() + value2.asString());
            }
            default -> {//String: "abc"*3, true*true=true&&true
                throw new FenixTypeException();
            }
        }
    }

    public static Value sub(Value value1, Value value2) {
        switch (value1.getType()) {
            case INT -> {
                return new IntValue(value1.asInt() - value2.asInt());
            }
            case FLOAT -> {
                return new FloatValue(value1.asFloat() - value2.asFloat());
            }
            default -> {//String: "abc"*3, true*true=true&&true
                throw new FenixTypeException();
            }
        }
    }

    public static Value pow(Value value1, Value value2) {
        switch (value1.getType()) {//todo: also check value2
            case INT -> {
                return new IntValue((int) Math.pow(value1.asInt(), value2.asInt()));
            }
            case FLOAT -> {
                return new FloatValue((float) Math.pow(value1.asFloat(), value2.asFloat()));
            }
            default -> {
                throw new FenixTypeException();
            }
        }
    }
}
