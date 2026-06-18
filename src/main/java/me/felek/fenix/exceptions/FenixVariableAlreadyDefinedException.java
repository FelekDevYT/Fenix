package me.felek.fenix.exceptions;

public class FenixVariableAlreadyDefinedException extends FenixException {
    public FenixVariableAlreadyDefinedException(String name) {
        super("VariableAlreadyDefinedException", "Variable " + name + " already defined.");
    }
}
