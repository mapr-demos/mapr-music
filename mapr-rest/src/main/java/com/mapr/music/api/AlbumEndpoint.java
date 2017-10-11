package com.mapr.music.api;


import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.RateDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.dto.TrackDto;
import com.mapr.music.model.AlbumRate;
import com.mapr.music.service.AlbumService;
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
 * Endpoint for accessing 'Album' resources.
 */
@Api(value = AlbumEndpoint.ENDPOINT_PATH, description = "Albums endpoint, which allows to manage 'Album' documents")
@Path(AlbumEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class AlbumEndpoint {

    public static final String ENDPOINT_PATH = "/albums";

    @Inject
    private AlbumService albumService;

    @Inject
    private RecommendationService recommendationService;

    @Inject
    private RateService rateService;

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get single album by it's identifier")
    public AlbumDto getAlbum(@ApiParam(value = "Album's identifier", required = true) @PathParam("id") String id) {
        return albumService.getAlbumById(id);
    }

    @GET
    @Path("/slug/{slug}")
    @ApiOperation(value = "Get single album by it's slug name")
    public AlbumDto getAlbumBySlugName(@ApiParam(value = "Slug name", required = true) @PathParam("slug") String slug) {
        return albumService.getAlbumBySlugName(slug);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of albums, which is represented by page")
    public ResourceDto<AlbumDto> getAlbumsPage(@QueryParam("per_page") Long perPage,
                                               @QueryParam("page") Long page,
                                               @QueryParam("sort") List<SortOption> sortOptions,
                                               @QueryParam("language") String language) {

        if (language != null) {
            return albumService.getAlbumsPageByLanguage(perPage, page, sortOptions, language);
        }

        return albumService.getAlbumsPage(perPage, page, sortOptions);
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
    public AlbumDto updateAlbum(@PathParam("id") String id, @Valid AlbumDto albumDto) {
        return albumService.updateAlbum(id, albumDto);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create album")
    public Response createAlbum(@Valid AlbumDto albumDto, @Context UriInfo uriInfo) {

        AlbumDto createdAlbum = albumService.createAlbum(albumDto);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(createdAlbum.getId());
        URI location = builder.build();

        return Response.status(Response.Status.CREATED).entity(createdAlbum).location(location).build();
    }

    @GET
    @Path("{id}/tracks")
    @ApiOperation(value = "Get list of album's tracks")
    public List<TrackDto> getAlbumTracks(@PathParam("id") String id) {
        return albumService.getAlbumTracksList(id);
    }

    @GET
    @Path("{album-id}/tracks/{track-id}")
    @ApiOperation(value = "Get single album's track")
    public TrackDto getAlbumsSingleTrack(@PathParam("album-id") String albumId,
                                         @PathParam("track-id") String trackId) {

        return albumService.getTrackById(albumId, trackId);
    }

    @PUT
    @Path("{id}/tracks")
    @ApiOperation(value = "Updates the whole list of album's tracks")
    public List<TrackDto> setAlbumTracks(@PathParam("id") String id, List<TrackDto> trackList) {
        return albumService.setAlbumTrackList(id, trackList);
    }

    @PUT
    @Path("{album-id}/tracks/{track-id}")
    @ApiOperation(value = "Update single album's track")
    public TrackDto updateAlbumsSingleTrack(@PathParam("album-id") String albumId,
                                            @PathParam("track-id") String trackId, TrackDto trackDto) {

        return albumService.updateAlbumTrack(albumId, trackId, trackDto);
    }

    @DELETE
    @Path("{album-id}/tracks/{track-id}")
    @ApiOperation(value = "Delete single album's track")
    public void updateAlbumsSingleTrack(@PathParam("album-id") String albumId, @PathParam("track-id") String trackId) {
        albumService.deleteAlbumTrack(albumId, trackId);
    }

    @POST
    @Path("{id}/tracks/")
    @ApiOperation(value = "Create single album's track")
    public TrackDto createAlbumsSingleTrack(@PathParam("id") String id, TrackDto trackDto) {
        return albumService.addTrackToAlbumTrackList(id, trackDto);
    }

    @GET
    @Path("/search/")
    @ApiOperation(value = "Search albums by specified parameters")
    public List<AlbumDto> searchAlbums(@QueryParam("name_entry") String nameEntry,
                                       @QueryParam("limit") Long limit) {

        return albumService.searchAlbums(nameEntry, limit);
    }

    @GET
    @Path("{id}/recommended/")
    @ApiOperation(value = "Get list of recommended albums for the specified album id")
    public List<AlbumDto> getRecommended(@Context SecurityContext sec, @PathParam("id") String albumId,
                                         @QueryParam("limit") Integer limit) {

        return recommendationService.getRecommendedAlbums(albumId, sec.getUserPrincipal(), limit);
    }

    @GET
    @Path("{id}/rating")
    @ApiOperation(value = "Get users rate for this album. Allowed only for authorized users.")
    public Response getAlbumRating(@ApiParam(value = "Album's identifier", required = true) @PathParam("id") String id,
                                   @Context SecurityContext sec) {

        if (sec.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(rateService.getAlbumRate(sec.getUserPrincipal(), id)).build();
    }

    @PUT
    @Path("{id}/rating")
    @ApiOperation(value = "Saves users rate for this album. Allowed only for authorized users.")
    public Response saveAlbumRating(@ApiParam(value = "Album's identifier", required = true) @PathParam("id") String id,
                                     @Context SecurityContext sec, RateDto albumRate) {

        if (sec.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(rateService.rateAlbum(sec.getUserPrincipal(), id, albumRate)).build();
    }
}