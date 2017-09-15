package com.mapr.music.util;

import com.mapr.music.dto.ErrorDto;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CommonExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorDto(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage()))
                .build();
    }
}
