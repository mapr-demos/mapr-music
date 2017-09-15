package com.mapr.music.util;

import org.jboss.resteasy.spi.DefaultOptionsMethodException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultOptionsExceptionMapper implements ExceptionMapper<DefaultOptionsMethodException> {

    @Override
    public Response toResponse(DefaultOptionsMethodException e) {
        return e.getResponse();
    }
}