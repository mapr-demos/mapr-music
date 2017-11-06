package com.mapr.music.util;

import com.mapr.music.dto.ErrorDto;

import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {

        List<String> errors = e.getConstraintViolations().stream()
                .map(constraintViolation -> {
                    Path path = constraintViolation.getPropertyPath();
                    String message = constraintViolation.getMessage();
                    return String.format("'%s' %s", path, message);
                })
                .collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorDto(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage(), errors))
                .build();
    }
}
