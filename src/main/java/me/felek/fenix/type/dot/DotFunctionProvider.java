package me.felek.fenix.type.dot;

import me.felek.fenix.type.Value;

@FunctionalInterface
public interface DotFunctionProvider {
    Value execute(String function, Value... args);
}
