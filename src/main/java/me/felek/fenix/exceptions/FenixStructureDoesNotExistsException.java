package me.felek.fenix.exceptions;

public class FenixStructureDoesNotExistsException extends FenixException {
    public FenixStructureDoesNotExistsException(String name) {
        super("StructureDoesNotExists", "Structure " + name + " does not exists.");
    }
}
