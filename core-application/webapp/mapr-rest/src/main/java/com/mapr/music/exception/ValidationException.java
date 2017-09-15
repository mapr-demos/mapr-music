package com.mapr.music.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Indicates that errors occurred while validating certain entity.
 */
public class ValidationException extends RuntimeException {

    private List<String> errors;

    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<String> errors) {
        this(message);
        setErrors(errors);
    }

    public ValidationException(String message, String... errors) {
        this(message);
        setErrors(Arrays.asList(errors));
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /**
     * @return list of errors which provides more precise information about entity's corrupted fields and
     * validation errors. If there is no errors set, immutable empty list will be returned.
     */
    public List<String> getErrors() {

        return (errors != null) ? errors : Collections.emptyList();
    }

}