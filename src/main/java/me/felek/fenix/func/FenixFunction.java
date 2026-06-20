package me.felek.fenix.func;

import me.felek.fenix.FenixParser;
import me.felek.fenix.type.ValueType;
import me.felek.fenix.variable.Environment;

import java.util.List;

public class FenixFunction {
    private final String name;
    private final List<RawArg> rawArgs;

    public void setBody(FenixParser.StatementContext body) {
        this.body = body;
    }

    private FenixParser.StatementContext body;
    private Environment env;
    private final ValueType returnType;

    public FenixFunction(String name, List<RawArg> rawArgs, FenixParser.StatementContext body, ValueType ret) {
        this.name = name;
        this.rawArgs = rawArgs;
        this.body = body;
        this.returnType = ret;
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
}
