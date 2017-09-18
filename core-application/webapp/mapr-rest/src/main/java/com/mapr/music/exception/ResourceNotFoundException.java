package com.mapr.music.exception;

/**
 * Indicates that certain resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}