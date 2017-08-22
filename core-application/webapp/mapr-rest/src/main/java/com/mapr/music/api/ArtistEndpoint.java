package com.mapr.music.api;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Endpoint for accessing 'Artist' resources.
 */
@Api(value = AlbumEndpoint.ENDPOINT_PATH, description = "Artists endpoint, which allows to manage 'Artist' documents")
@Path(ArtistEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ArtistEndpoint {

    public static final String ENDPOINT_PATH = "/artists";

    @Inject
    private ArtistService artistService;

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get single artist by it's identifier")
    public Artist getArtist(@PathParam("id") String id, @QueryParam("field") final List<String> fieldList) {
        return (fieldList.size() == 0) ? artistService.getById(id) : artistService.getById(id, fieldList);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of artists, which is represented by page")
    public ResourceDto<Artist> getAllArtists(@QueryParam("per_page") Long perPage,
                                             @QueryParam("page") Long page,
                                             @QueryParam("sort_type") String order,
                                             @QueryParam("sort_fields") List<String> orderFields) {

        return artistService.getArtistsPage(perPage, page, order, orderFields);
    }
}
