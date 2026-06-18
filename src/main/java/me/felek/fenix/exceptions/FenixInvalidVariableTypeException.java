package me.felek.fenix.exceptions;

public class FenixInvalidVariableTypeException extends FenixException {
    public FenixInvalidVariableTypeException(String varName, String invalidType) {
        super("InvalidVariableTypeException", "Invalid variable type " + invalidType + " of variable " + varName);
    }
}
