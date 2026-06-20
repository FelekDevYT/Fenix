package me.felek.fenix.exceptions;

public class FenixAccessException extends FenixException {
    public FenixAccessException(String field) {
        super("AccessException", "Can't access to " + field + " field.");
    }
}
