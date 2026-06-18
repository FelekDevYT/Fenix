package me.felek.fenix.type.impl;

import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArrayValue extends Value {
    private List<Value> array = new ArrayList<>();

    public List<Value> getRawArray() {
        return array;
    }

    public ArrayValue(List<Value> array) {
        this.array = new ArrayList<>(array);
    }

    public List<Value> getArray() {
        return array;
    }

    public void addValue(Value value) {
        array.add(value);
    }

    public void addAll(Value... values) {
        array.addAll(Arrays.asList(values));
    }

    public void merge(ArrayValue value2) {
        array.addAll(value2.array);
    }

    public Value get(int idx) {
        return array.get(idx);
    }

    public void set(int idx, Value value) {
        array.set(idx, value);
    }

    @Override
    public Object getAsObject() {
        return array;
    }

    @Override
    public ValueType getType() {
        return ValueType.ARRAY;
    }

    @Override
    public int asInt() {
        return array.size();
    }

    @Override
    public float asFloat() {
        return array.size();
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder("[");
        for (Value value : array) {
            sb.append(value.asString()).append(", ");
        }
        return sb.substring(0, sb.length() - 2) + "]";
    }

    @Override
    public boolean asBool() {
        return array.isEmpty();
    }
}
