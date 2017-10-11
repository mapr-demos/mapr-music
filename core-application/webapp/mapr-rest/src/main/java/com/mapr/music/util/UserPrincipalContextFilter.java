package com.mapr.music.util;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * Class which has access to the SecurityContext. Uses the RESTeasy utility class to push the User Principal into the
 * context. This allows to access User Principal at other components.
 */
@Provider
public class UserPrincipalContextFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        SecurityContext securityContext = context.getSecurityContext();
        ResteasyProviderFactory.pushContext(Principal.class, securityContext.getUserPrincipal());
    }
}