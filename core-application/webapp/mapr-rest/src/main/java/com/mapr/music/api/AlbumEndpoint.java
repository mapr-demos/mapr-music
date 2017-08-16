package com.mapr.music.api;


import com.mapr.music.model.Album;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * Endpoint for accessing 'Album' resources.
 */
@Path("/albums")
public class AlbumEndpoint {

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Album getAlbum(@PathParam("id") String id) {
        return new Album();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Album> getAllAlbums(@QueryParam("page") Long page) {
        return Collections.emptyList();
    }
}
