package me.felek.fenix.type.impl;

import me.felek.fenix.exceptions.FenixNullException;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

public class NullValue extends Value {
    @Override
    public Object getAsObject() {
        throw new FenixNullException();
    }

    @Override
    public ValueType getType() {
        return ValueType.NULL;
    }

    @Override
    public int asInt() {
        throw new FenixNullException();
    }

    @Override
    public float asFloat() {
        throw new FenixNullException();
    }

    @Override
    public String asString() {
        return "null";
    }

    @Override
    public boolean asBool() {
        throw new FenixNullException();
    }
}
