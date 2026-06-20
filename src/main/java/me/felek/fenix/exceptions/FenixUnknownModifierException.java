package me.felek.fenix.exceptions;

public class FenixUnknownModifierException extends FenixException {
    public FenixUnknownModifierException(String um) {
        super("UnknownModifierException", "Unknown modifier: " + um);
    }
}
