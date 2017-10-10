package com.mapr.music.api;

import com.mapr.music.service.StatisticService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Endpoint for updating application statistics.
 */
@Api(value = StatisticsEndpoint.ENDPOINT_PATH, description = "Statistics endpoint, which allows update app statistics")
@Path(StatisticsEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsEndpoint {

    public static final String ENDPOINT_PATH = "/statistics";

    @Inject
    private StatisticService statisticService;

    @PUT
    @Path("/")
    @ApiOperation(value = "Updates statistics")
    public Response updateStatistics() {
        statisticService.recomputeStatistics();
        return Response.ok().build();
    }
}
