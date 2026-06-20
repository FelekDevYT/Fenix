package me.felek.fenix.exceptions;

public class FenixUnknownDotFunctionException extends FenixException {
    public FenixUnknownDotFunctionException(String funcName) {
        super("UnknownDotFunctionException", "Could not find " + funcName + " dot function.");
    }
}
