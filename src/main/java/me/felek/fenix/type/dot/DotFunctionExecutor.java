package me.felek.fenix.type.dot;

import me.felek.fenix.type.Value;
import me.felek.fenix.type.impl.StringValue;

import java.util.List;

public class DotFunctionExecutor {
    public static DotOutput execute(Value value, String function, List<Value> args) {
        if (value instanceof StringValue) {
            Value out = ((DotFunctionProvider) ((StringValue) value)).execute(function, args.toArray(new Value[0]));
            return new DotOutput(true, out);
        }

        return new DotOutput(false, null);
    }
}
