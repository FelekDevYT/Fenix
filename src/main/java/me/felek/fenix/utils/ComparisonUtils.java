package me.felek.fenix.utils;

import me.felek.fenix.exceptions.FenixTypeException;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.impl.BoolValue;

public class ComparisonUtils {
    public static Value greater(Value value1, Value value2) {
        float[] values = getComparisonValues(value1, value2);
        return new BoolValue(values[0] > values[1]);
    }

    public static Value lower(Value value1, Value value2) {
        float[] vals = getComparisonValues(value1, value2);
        return new BoolValue(vals[0] < vals[1]);
    }

    public static Value greaterEq(Value value1, Value value2) {
        float[] vals = getComparisonValues(value1, value2);
        return new BoolValue(vals[0] >= vals[1]);
    }

    public static Value lowerEq(Value value1, Value value2) {
        float[] vals = getComparisonValues(value1, value2);
        return new BoolValue(vals[0] <= vals[1]);
    }

    public static Value eq(Value value1, Value value2) {//todo: should be special for strings btw
        float[] vals = getComparisonValues(value1, value2);
        return new BoolValue(vals[0] == vals[1]);
    }

    public static Value notEq(Value value1, Value value2) {//todo: should be special for strings btw
        float[] vals = getComparisonValues(value1, value2);
        return new BoolValue(vals[0] != vals[1]);
    }

    private static float[] getComparisonValues(Value value1, Value value2) {
        float v1 = 0;
        float v2 = 0;
        switch (value1.getType()) {
            case FLOAT -> {
                v1 = value1.asFloat();
                v2 = value2.asFloat();
            }
            case INT -> {
                v1 = value1.asInt();
                v2 = value2.asInt();
            }
            case STRING -> {
                v1 = value1.asString().length();
                v2 = value2.asString().length();
            }
            default -> {
                throw new FenixTypeException();
            }
        }

        return new float[]{v1, v2};
    }
}
