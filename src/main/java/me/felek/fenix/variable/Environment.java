package me.felek.fenix.variable;

import me.felek.fenix.exceptions.FenixVariableAlreadyDefinedException;
import me.felek.fenix.exceptions.FenixVariableNotDefinedException;
import me.felek.fenix.func.FenixFunction;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.impl.ArrayValue;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private Map<String, Value> localVariables = new HashMap<>();
    private Map<String, FenixFunction> functions = new HashMap<>();
    private Environment parent;

    public Environment() {

    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Value get(String name) {
        if (localVariables.containsKey(name)) {
            return localVariables.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new FenixVariableNotDefinedException(name);
    }

    public void assign(String name, Value value) {
        if (localVariables.containsKey(name)) {
            localVariables.put(name, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value);
            return;
        }

        throw new FenixVariableNotDefinedException(name);
    }

    public void define(String name, Value value) {
        if (localVariables.containsKey(name)) {
            throw new FenixVariableAlreadyDefinedException(name);
        }
        localVariables.put(name, value);
    }

    public void defineFunction(String name, FenixFunction function) {
        functions.put(name, function);
    }

    public FenixFunction getFunction(String name) {
        return functions.get(name);//todo: check if not defined
    }

    public void assignArray(String arrName, int idx, Value value) {
        if (localVariables.get(arrName) instanceof ArrayValue) {
            ArrayValue arr = (ArrayValue) localVariables.get(arrName);
            arr.set(idx, value);
            return;
        }

        throw new RuntimeException();//todo: exception
    }

    @Override
    public String toString() {
        return "Environment{" +
                "localVariables=" + localVariables +
                ", functions=" + functions +
                ", parent=" + parent +
                '}';
    }
}
