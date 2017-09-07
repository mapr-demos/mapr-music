package com.mapr.music.api;

import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
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
        return artistService.getArtistById(id);
    }

    @GET
    @Path("/slug/{slug}")
    @ApiOperation(value = "Get single artist by it's slug name")
    public ArtistDto getArtistBySlugName(@PathParam("slug") String slug) {
        return artistService.getArtistBySlugName(slug);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of artists, which is represented by page")
    public ResourceDto<ArtistDto> getAllArtists(@QueryParam("per_page") Long perPage,
                                                @QueryParam("page") Long page,
                                                @QueryParam("sort") List<SortOption> sortOptions) {

        return artistService.getArtistsPage(perPage, page, sortOptions);
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
    public Response createAlbum(Artist artist, @Context UriInfo uriInfo) {

        ArtistDto createdArtist = artistService.createArtist(artist);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(createdArtist.getId());
        URI location = builder.build();

        return Response.status(Response.Status.CREATED).entity(createdArtist).location(location).build();
    }

}
