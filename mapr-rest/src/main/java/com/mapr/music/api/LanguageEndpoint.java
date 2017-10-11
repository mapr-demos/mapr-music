package com.mapr.music.api;

import com.mapr.music.model.Language;
import com.mapr.music.service.AlbumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Endpoint for accessing 'Language' resources.
 */
@Api(value = LanguageEndpoint.ENDPOINT_PATH, description = "Language endpoint, which allows to access 'Language' documents")
@Path(LanguageEndpoint.ENDPOINT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class LanguageEndpoint {

    public static final String ENDPOINT_PATH = "/languages";

    @Inject
    private AlbumService albumService;

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of supported languages")
    public List<Language> getSupportedLanguages() {
        return albumService.getSupportedAlbumsLanguages();
    }
}
