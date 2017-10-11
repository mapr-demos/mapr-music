package com.mapr.music.api;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.ESSearchResult;
import com.mapr.music.service.ESSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Endpoint for performing various searching.
 */
@Api(value = SearchEndpoint.ENDPOINT_PATH, description = "Search endpoint, which allows to perform various searching")
@Path(SearchEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class SearchEndpoint {

    public static final String ENDPOINT_PATH = "/search";

    @Inject
    private ESSearchService searchService;

    @GET
    @Path("/name")
    @ApiOperation(value = "Search by name entry")
    public ResourceDto<ESSearchResult> byNameEntry(@QueryParam("per_page") Integer perPage,
                                                   @QueryParam("page") Integer page,
                                                   @QueryParam("entry") String nameEntry) {

        return searchService.findByNameEntry(nameEntry, perPage, page);
    }

    @GET
    @Path("/artists/name")
    @ApiOperation(value = "Search artists by name entry")
    public ResourceDto<ESSearchResult> artistsByNameEntry(@QueryParam("per_page") Integer perPage,
                                                          @QueryParam("page") Integer page,
                                                          @QueryParam("entry") String nameEntry) {

        return searchService.findArtistsByNameEntry(nameEntry, perPage, page);
    }

    @GET
    @Path("/albums/name")
    @ApiOperation(value = "Search albums by name entry")
    public ResourceDto<ESSearchResult> albumsByNameEntry(@QueryParam("per_page") Integer perPage,
                                                         @QueryParam("page") Integer page,
                                                         @QueryParam("entry") String nameEntry) {

        return searchService.findAlbumsByNameEntry(nameEntry, perPage, page);
    }
}
