package me.felek.fenix.type.impl;

import me.felek.fenix.exceptions.FenixUnknownDotFunctionException;
import me.felek.fenix.type.dot.DotFunctionProvider;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StringValue extends Value implements DotFunctionProvider {
    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public Object getAsObject() {
        return value;
    }

    @Override
    public ValueType getType() {
        return ValueType.STRING;
    }

    @Override
    public int asInt() {
        return Integer.parseInt(value);
    }

    @Override
    public float asFloat() {
        return Float.parseFloat(value);
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public boolean asBool() {
        return value.isEmpty();
    }

    @Override
    public Value execute(String function, Value... args) {
        switch (function) {
            case "charAt" -> {
                return new StringValue(String.valueOf(value.charAt(args[0].asInt())));
            }
            case "toLowerCase" -> {
                return new StringValue(value.toLowerCase());
            }
            case "toUpperCase" -> {
                return new StringValue(value.toUpperCase());
            }
            case "length" -> {
                return new IntValue(value.length());
            }
            case "isEmpty" -> {
                return new BoolValue(value.isEmpty());
            }
            case "contains" -> {
                return new BoolValue(value.contains(args[0].asString()));
            }
            case "startsWith" -> {
                return new BoolValue(value.startsWith(args[0].asString()));
            }
            case "endsWith" -> {
                return new BoolValue(value.endsWith(args[0].asString()));
            }
            case "substring" -> {
                if (args.length == 1) {//substring(1);
                    return new StringValue(value.substring(args[0].asInt()));
                } else if (args.length == 2) {
                    return new StringValue(value.substring(args[0].asInt(), args[1].asInt()));
                }
            }
            case "trim" -> {
                return new StringValue(value.trim());
            }
            case "replace" -> {
                return new StringValue(value.replace(args[0].asString(), args[1].asString()));
            }
            case "replaceAll" -> {
                return new StringValue(value.replaceAll(args[0].asString(), args[1].asString()));
            }
            case "split" -> {
                List<Value> values = new ArrayList<>();
                for (var s : value.split(args[0].asString())) {
                    values.add(new StringValue(s));
                }
                return new ArrayValue(values);
            }
            default -> {
                throw new FenixUnknownDotFunctionException(function);
            }
        }

        return new NullValue();
    }
}
