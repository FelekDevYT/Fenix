package me.felek.fenix.type.impl;

import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

public class FloatValue extends Value {
    private float value;

    public FloatValue(float value) {
        this.value = value;
    }

    @Override
    public Object getAsObject() {
        return value;
    }

    @Override
    public ValueType getType() {
        return ValueType.FLOAT;
    }

    @Override
    public int asInt() {
        return  (int) value;
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
        return value != 0.0;
    }
}
