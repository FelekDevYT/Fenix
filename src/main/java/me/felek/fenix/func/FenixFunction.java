package me.felek.fenix.func;

import me.felek.fenix.FenixParser;
import me.felek.fenix.struct.Modifier;
import me.felek.fenix.type.ValueType;
import me.felek.fenix.variable.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FenixFunction {
    private final String name;
    private final List<RawArg> rawArgs;
    private FenixParser.StatementContext body;
    private Environment env;
    private final ValueType returnType;
    private List<Modifier> functionModifiers;

    public List<Modifier> getFunctionModifiers() {
        return functionModifiers;
    }

    public void setFunctionModifiers(List<Modifier> functionModifiers) {
        this.functionModifiers = functionModifiers;
    }

    public FenixFunction(String name, List<RawArg> rawArgs, FenixParser.StatementContext body, ValueType ret) {
        this.name = name;
        this.rawArgs = rawArgs;
        this.body = body;
        this.returnType = ret;
        this.functionModifiers = new ArrayList<>();
    }

    public void setBody(FenixParser.StatementContext body) {
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<RawArg> getArgs() {
        return rawArgs;
    }

    public FenixParser.StatementContext getBody() {
        return body;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public ValueType getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "FenixFunction{" +
                "name='" + name + '\'' +
                ", returnType=" + returnType +
                ", functionModifiers=" + functionModifiers +
                '}';
    }
}
