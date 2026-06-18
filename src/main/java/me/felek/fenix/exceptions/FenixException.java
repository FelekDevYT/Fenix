package me.felek.fenix.exceptions;

public class FenixException extends RuntimeException {
    public FenixException(String type, String message) {
        super(String.format("[ %s ] %s", type, message));
    }
}
