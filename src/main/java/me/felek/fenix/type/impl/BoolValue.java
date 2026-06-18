package me.felek.fenix.type.impl;

import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

public class BoolValue extends Value {
    private boolean value;

    public BoolValue(boolean value) {
        this.value = value;
    }

    @Override
    public Object getAsObject() {
        return value;
    }

    @Override
    public ValueType getType() {
        return ValueType.BOOL;
    }

    @Override
    public int asInt() {
        return value?1:0;
    }

    @Override
    public float asFloat() {
        return value?1:0;
    }

    @Override
    public String asString() {
        return String.valueOf(value);
    }

    @Override
    public boolean asBool() {
        return value;
    }
}
