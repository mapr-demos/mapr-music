package com.mapr.music.api;


import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.impl.AlbumServiceImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Endpoint for accessing 'Album' resources.
 */
@Path("/albums")
public class AlbumEndpoint {

    // FIXME use DI
    private AlbumService albumService = new AlbumServiceImpl();

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Album getAlbum(@PathParam("id") String id) {
        return albumService.getById(id);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public ResourceDto<Album> getAllAlbums(@QueryParam("page") Long page) {

        return (page != null) ? albumService.getAlbumsPage(page) : albumService.getAlbumsPage();
    }
}
