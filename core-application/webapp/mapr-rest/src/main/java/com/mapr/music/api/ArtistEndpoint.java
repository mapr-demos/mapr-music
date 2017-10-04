package com.mapr.music.api;

import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.ArtistRate;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.RateService;
import com.mapr.music.service.RecommendationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.inject.Inject;
import javax.validation.Valid;
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

    @Inject
    private RecommendationService recommendationService;

    @Inject
    private RateService rateService;

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

    @GET
    @Path("/search/")
    @ApiOperation(value = "Search artists by specified parameters")
    public List<ArtistDto> searchArtists(@QueryParam("name_entry") String nameEntry,
                                         @QueryParam("limit") Long limit) {

        return artistService.searchArtists(nameEntry, limit);
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
    public ArtistDto updateArtist(@PathParam("id") String id, @Valid ArtistDto artistDto) {
        return artistService.updateArtist(id, artistDto);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create artist")
    public Response createArtist(@Valid ArtistDto artist, @Context UriInfo uriInfo) {

        ArtistDto createdArtist = artistService.createArtist(artist);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(createdArtist.getId());
        URI location = builder.build();

        return Response.status(Response.Status.CREATED).entity(createdArtist).location(location).build();
    }

    @GET
    @Path("{id}/recommended/")
    @ApiOperation(value = "Get list of recommended artists for the specified artist id")
    public List<ArtistDto> getRecommended(@Context SecurityContext sec, @PathParam("id") String artistId,
                                          @QueryParam("limit") Integer limit) {

        return recommendationService.getRecommendedArtists(artistId, sec.getUserPrincipal(), limit);
    }

    @GET
    @Path("{id}/rating")
    @ApiOperation(value = "Get users rate for this artist. Allowed only for authorized users.")
    public Response getAlbumRating(@ApiParam(value = "Artist's identifier", required = true) @PathParam("id") String id,
                                   @Context SecurityContext sec) {

        if (sec.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(rateService.getArtistRate(sec.getUserPrincipal(), id)).build();
    }

    @PUT
    @Path("{id}/rating")
    @ApiOperation(value = "Saves users rate for this artist. Allowed only for authorized users.")
    public Response saveAlbumRating(@ApiParam(value = "Artist's identifier", required = true) @PathParam("id") String id,
                                    @Context SecurityContext sec, ArtistRate artistRate) {

        if (sec.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(rateService.rateArtist(sec.getUserPrincipal(), id, artistRate)).build();
    }

}
