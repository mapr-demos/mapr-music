package com.mapr.music.api;

import com.mapr.music.dto.UserDto;
import com.mapr.music.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Api(value = ArtistEndpoint.ENDPOINT_PATH, description = "Artists endpoint, which allows to manage 'Artist' documents")
@Path(UserEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class UserEndpoint {

    public static final String ENDPOINT_PATH = "/users";

    @Inject
    private UserService userService;

    @GET
    @Path("current")
    @ApiOperation(value = "Get authenticated user info")
    public UserDto getCurrentUserInfo(@Context SecurityContext securityContext) {
        return userService.getUserByPrincipal(securityContext.getUserPrincipal());
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Creates new user account")
    public UserDto registerUser(UserDto user) {
        return userService.register(user);
    }
}
