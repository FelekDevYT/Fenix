package me.felek.fenix.exceptions.handled;

import me.felek.fenix.type.Value;

public class ReturnException extends RuntimeException {
    private Value returned;

    public ReturnException(Value returned) {
        this.returned = returned;
    }

    public Value getReturned() {
        return returned;
    }

    public void setReturned(Value returned) {
        this.returned = returned;
    }
}
