package me.felek.fenix.type;

public abstract class Value {
    private ValueType type;

    public abstract Object getAsObject();
    public abstract ValueType getType();

    public abstract int asInt();
    public abstract float asFloat();
    public abstract String asString();
    public abstract boolean asBool();
}
