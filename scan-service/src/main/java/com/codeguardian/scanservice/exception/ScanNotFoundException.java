package com.codeguardian.scanservice.exception;

public class ScanNotFoundException extends RuntimeException {

    public ScanNotFoundException(String message) {
        super(message);
    }
}
