package com.mediatek.ngin3d.utils;

public class Ngin3dException extends RuntimeException {
    public Ngin3dException() {
        super();
    }

    public Ngin3dException(String message) {
        super(message);
    }

    public Ngin3dException(String message, Throwable cause) {
        super(message, cause);
    }

    public Ngin3dException(Throwable cause) {
        super(cause);
    }
}
