package me.felek.fenix.exceptions.pre;

import me.felek.fenix.exceptions.FenixException;

public class FenixPreprocessorError extends FenixException {
    public FenixPreprocessorError(String directive, String msg) {
        super(directive, msg);
    }
}
