package me.felek.fenix.exceptions;

public class FenixNullException extends FenixException {
    public FenixNullException() {
        super("NullException", "Null value wants to become non null!");
    }
}
