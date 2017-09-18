package com.mapr.music.util;

import com.mapr.music.dto.ErrorDto;
import com.mapr.music.exception.ResourceNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorDto(Response.Status.NOT_FOUND.getStatusCode(), exception.getMessage()))
                .build();
    }
}
