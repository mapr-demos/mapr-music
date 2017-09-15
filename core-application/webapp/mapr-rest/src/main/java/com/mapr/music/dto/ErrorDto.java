package com.mapr.music.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDto {

    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("error_list")
    private List<String> errorList;

    public ErrorDto() {
    }

    public ErrorDto(int status, String message, String... errors) {
        this.status = status;
        this.message = message;

        if (errors != null && errors.length > 0) {
            this.errorList = Arrays.asList(errors);
        }
    }

    public ErrorDto(int status, String message, List<String> errorList) {
        this.status = status;
        this.message = message;
        this.errorList = errorList;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrorList() {
        return (errorList != null) ? errorList : Collections.emptyList();
    }

    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
    }
}
