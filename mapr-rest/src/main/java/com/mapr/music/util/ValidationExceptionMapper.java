package com.mapr.music.util;

import com.mapr.music.dto.ErrorDto;
import com.mapr.music.exception.ValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorDto(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage(), e.getErrors()))
                .build();
    }
}