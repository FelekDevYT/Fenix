package me.felek.fenix.type.impl;

import me.felek.fenix.exceptions.FenixTypeException;
import me.felek.fenix.struct.Struct;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

public class ObjectValue extends Value {
    @Override
    public Object getAsObject() {
        return object;
    }

    @Override
    public ValueType getType() {
        return ValueType.OBJECT;
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
        return object.getName();
    }

    @Override
    public boolean asBool() {
        throw new FenixTypeException();
    }

    private Struct object;

    public void setObject(Struct object) {
        this.object = object;
    }

    public Struct getObject() {
        return object;
    }

    public ObjectValue(Struct object) {
        this.object = object;
    }
}
