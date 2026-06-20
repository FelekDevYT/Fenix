package me.felek.fenix.struct;

import me.felek.fenix.func.FenixFunction;
import me.felek.fenix.type.Value;
import me.felek.fenix.variable.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Struct {
    private final String name;
    private Map<String, FenixFunction> functions = new HashMap<>();
    private Map<String, Value> variables = new HashMap<>();
    private Environment env;

    public String getName() {
        return name;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public Struct(String name, Environment env) {
        this.name = name;
        this.env = env;
    }

    public Map<String, FenixFunction> getFunctions() {
        return functions;
    }

    public Map<String, Value> getVariables() {
        return variables;
    }
}
