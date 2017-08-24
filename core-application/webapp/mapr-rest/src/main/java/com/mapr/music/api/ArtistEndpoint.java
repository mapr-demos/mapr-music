package com.mapr.music.api;

import com.mapr.music.dto.ArtistDto;
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
@Api(value = ArtistEndpoint.ENDPOINT_PATH, description = "Artists endpoint, which allows to manage 'Artist' documents")
@Path(ArtistEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ArtistEndpoint {

    public static final String ENDPOINT_PATH = "/artists";

    @Inject
    private ArtistService artistService;

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get single artist by it's identifier")
    public ArtistDto getArtist(@PathParam("id") String id) {
        return artistService.getById(id);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of artists, which is represented by page")
    public ResourceDto<ArtistDto> getAllArtists(@QueryParam("per_page") Long perPage,
                                                @QueryParam("page") Long page,
                                                @QueryParam("sort_type") String order,
                                                @QueryParam("sort_fields") List<String> orderFields) {

        return artistService.getArtistsPage(perPage, page, order, orderFields);
    }

    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Delete single artist by it's identifier")
    public void deleteArtist(@PathParam("id") String id) {
        artistService.deleteArtistById(id);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update single artist")
    public ArtistDto updateArtist(@PathParam("id") String id, Artist artist) {
        return artistService.updateArtist(id, artist);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create artist")
    public ArtistDto createArtist(Artist artist) {
        return artistService.createArtist(artist);
    }

}
