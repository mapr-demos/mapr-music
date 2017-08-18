package com.mapr.music.api;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.impl.ArtistServiceImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/artists")
public class ArtistEndpoint {

    // FIXME use DI
    private ArtistService artistService = new ArtistServiceImpl();

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Artist getArtist(@PathParam("id") String id, @QueryParam("field") final List<String> fieldList) {
        return (fieldList.size() == 0) ? artistService.getById(id) : artistService.getById(id, fieldList);
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public ResourceDto<Artist> getAllAlbums(@QueryParam("page") Long page) {
        return (page != null) ? artistService.getAll(page) : artistService.getAll();
    }
}
