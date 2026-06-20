package me.felek.fenix.type.impl;

import me.felek.fenix.exceptions.FenixTypeException;
import me.felek.fenix.struct.Struct;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

public class SelfValue extends Value {
    private Struct self;

    public Struct getSelf() {
        return self;
    }

    public SelfValue(Struct self) {
        this.self = self;
    }

    @Override
    public Object getAsObject() {
        return self.getName();
    }

    @Override
    public ValueType getType() {
        return ValueType.STRUCT;
    }

    @Override
    public int asInt() {
        throw new FenixTypeException();
    }

    @Override
    public float asFloat() {
        throw new FenixTypeException();
    }

    @Override
    public String asString() {
        return self.getName();
    }

    @Override
    public boolean asBool() {
        throw new FenixTypeException();
    }
}
