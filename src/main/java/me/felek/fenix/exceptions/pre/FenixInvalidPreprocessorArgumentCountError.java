package me.felek.fenix.exceptions.pre;

public class FenixInvalidPreprocessorArgumentCountError extends FenixPreprocessorError{
    public FenixInvalidPreprocessorArgumentCountError(String directive) {
        super("InvalidArgumentCountError", "Invalid count of arguments for " + directive + " directive.");
    }
}
