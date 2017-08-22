package com.mapr.music.api;


import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.service.AlbumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Endpoint for accessing 'Album' resources.
 */
@Api(value = AlbumEndpoint.ENDPOINT_PATH, description = "Albums endpoint, which allows to manage 'Album' documents")
@Path(AlbumEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class AlbumEndpoint {

    public static final String ENDPOINT_PATH = "/albums";

    @Inject
    private AlbumService albumService;

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get single album by it's identifier")
    public Album getAlbum(@PathParam("id") String id) {
        return albumService.getById(id);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of albums, which is represented by page")
    public ResourceDto<Album> getAllAlbums(@QueryParam("per_page") Long perPage,
                                           @QueryParam("page") Long page,
                                           @QueryParam("sort_type") String order,
                                           @QueryParam("sort_fields") List<String> orderFields) {

        return albumService.getAlbumsPage(perPage, page, order, orderFields);
    }
}