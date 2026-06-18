package me.felek.fenix.type.impl;

import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

public class StringValue extends Value {
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
}
