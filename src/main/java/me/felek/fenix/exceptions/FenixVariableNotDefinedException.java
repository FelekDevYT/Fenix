package me.felek.fenix.exceptions;

public class FenixVariableNotDefinedException extends FenixException {
    public FenixVariableNotDefinedException(String name) {
        super("VariableNotDefinedException", "variable " + name + " doesn't exists.");
    }
}
