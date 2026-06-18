package me.felek.fenix.type.impl;

import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

public class IntValue extends Value {
    private int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public Object getAsObject() {
        return value;
    }

    @Override
    public ValueType getType() {
        return ValueType.INT;
    }

    @Override
    public int asInt() {
        return value;
    }

    @Override
    public float asFloat() {
        return value;
    }

    @Override
    public String asString() {
        return String.valueOf(value);
    }

    @Override
    public boolean asBool() {
        return value != 0;
    }
}
