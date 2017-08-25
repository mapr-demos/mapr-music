package com.mapr.music.api;


import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.service.AlbumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
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
    public AlbumDto getAlbum(@PathParam("id") String id) {
        return albumService.getAlbumById(id);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of albums, which is represented by page")
    public ResourceDto<AlbumDto> getAllAlbums(@QueryParam("per_page") Long perPage,
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
    public AlbumDto updateAlbum(@PathParam("id") String id, Album album) {
        return albumService.updateAlbum(id, album);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create album")
    public Response createAlbum(Album album, @Context UriInfo uriInfo) {

        AlbumDto createdAlbum = albumService.createAlbum(album);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(createdAlbum.getId());
        URI location = builder.build();

        return Response.status(Response.Status.CREATED).entity(createdAlbum).location(location).build();
    }
}