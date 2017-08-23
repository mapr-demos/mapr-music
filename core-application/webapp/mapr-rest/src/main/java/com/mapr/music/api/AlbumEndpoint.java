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
        return albumService.getAlbumById(id);
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

    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Delete single album by it's identifier")
    public void deleteAlbum(@PathParam("id") String id) {
        albumService.deleteAlbumById(id);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update single album")
    public Album updateAlbum(@PathParam("id") String id, Album album) {
        return albumService.updateAlbum(id, album);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create album")
    public Album createAlbum(Album album) {
        return albumService.createAlbum(album);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Create album according to the specified JSON string. Note that although string in JSON " +
            "format, request's content-type must be set to text/plain.")
    public Album createAlbum(String albumJsonString) {
        return albumService.createAlbum(albumJsonString);
    }
}