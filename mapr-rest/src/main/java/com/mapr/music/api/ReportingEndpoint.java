package com.mapr.music.api;


import com.mapr.music.model.Pair;
import com.mapr.music.service.ReportingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Endpoint for accessing 'Reporting' resources.
 * <p>
 * The "reporting" provide access to some analytical queries/data
 */
@Api(value = ReportingEndpoint.ENDPOINT_PATH, description = "Reporting endpoint, which provides access to analytical/reporting data")
@Path(ReportingEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class ReportingEndpoint {

    public static final String ENDPOINT_PATH = "/reporting";

    @Inject
    private ReportingService reportingService;


    @GET
    @Path("/artists/top-{count}-area")
    @ApiOperation(value = "Get the area with the most artists")
    public List<Pair> getTopArtistByArea(@ApiParam(value = "Number of lines", required = true) @PathParam("count") int count) {
        return reportingService.getTopArtistByArea(count);
    }

    @GET
    @Path("/albums/top-{count}-languages")
    @ApiOperation(value = "Get the languages with the most albums")
    public List<Pair> getTopLanguagesForAlbum(@ApiParam(value = "Number of lines", required = true) @PathParam("count") int count) {
        return reportingService.getTopLanguagesForAlbum(count);
    }

    @GET
    @Path("/albums/per-year-last-{count}")
    @ApiOperation(value = "Get the Number of Albums per year")
    public List<Pair> getNumberOfAlbumsPerYear(@ApiParam(value = "Number of years", required = true) @PathParam("count") int count) {
        return reportingService.getNumberOfAlbumsPerYear(count);
    }

}
