package com.example.playmatch.mvp.common.exception;

import com.example.playmatch.mvp.common.error.MvpError;

public class MvpException extends RuntimeException {
    private final MvpError error;

    public MvpException(MvpError error) {
        super(error.title());
        this.error = error;
    }

    public MvpException(MvpError error, String message) {
        super(message);
        this.error = error;
    }

    public MvpError getError() {
        return error;
    }
}
