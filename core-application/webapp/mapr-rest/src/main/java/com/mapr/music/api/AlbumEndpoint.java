package com.mapr.music.api;


import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Track;
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
    @Path("/slug/{slug}")
    @ApiOperation(value = "Get single album by it's slug name")
    public AlbumDto getAlbumBySlugName(@PathParam("slug") String slug) {
        return albumService.getAlbumBySlugName(slug);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of albums, which is represented by page")
    public ResourceDto<AlbumDto> getAlbumsList(@QueryParam("per_page") Long perPage,
                                               @QueryParam("page") Long page,
                                               @QueryParam("sort") List<SortOption> sortOptions) {

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

    @GET
    @Path("{id}/tracks")
    @ApiOperation(value = "Get list of album's tracks")
    public List<Track> getAlbumTracks(@PathParam("id") String id) {
        return albumService.getAlbumTracksList(id);
    }

    @GET
    @Path("{album-id}/tracks/{track-id}")
    @ApiOperation(value = "Get single album's track")
    public Track getAlbumsSingleTrack(@PathParam("album-id") String albumId,
                                      @PathParam("track-id") String trackId) {

        return albumService.getTrackById(albumId, trackId);
    }

    @PUT
    @Path("{id}/tracks")
    @ApiOperation(value = "Updates the whole list of album's tracks")
    public List<Track> setAlbumTracks(@PathParam("id") String id, List<Track> trackList) {
        return albumService.setAlbumTrackList(id, trackList);
    }

    @PUT
    @Path("{album-id}/tracks/{track-id}")
    @ApiOperation(value = "Update single album's track")
    public Track updateAlbumsSingleTrack(@PathParam("album-id") String albumId,
                                         @PathParam("track-id") String trackId, Track track) {

        return albumService.updateAlbumTrack(albumId, trackId, track);
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
    public Track createAlbumsSingleTrack(@PathParam("id") String id, Track track) {
        return albumService.addTrackToAlbumTrackList(id, track);
    }
}