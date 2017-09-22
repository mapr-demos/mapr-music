package com.mapr.music.api;

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
 * TODO maybe we should delete this separate endpoint. Added just for testing.
 * Endpoint for performing various searching.
 */
@Api(value = SearchEndpoint.ENDPOINT_PATH, description = "Search endpoint, which allows to perform various searching")
@Path(SearchEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class SearchEndpoint {

    // TODO change it to correspond to REST
    public static final String ENDPOINT_PATH = "/search";

    @Inject
    private ESSearchService searchService;

    @GET
    @Path("/name")
    @ApiOperation(value = "Search by name entry")
    public ESSearchResult byNameEntry(@QueryParam("entry") String nameEntry) {
        return searchService.findByNameEntry(nameEntry);
    }
}
