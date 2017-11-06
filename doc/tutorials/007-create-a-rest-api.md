# Create a REST API

## MapR Music REST Service API

[MapR Music REST Service](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-rest) is core module of MapR 
Music application. It exposes REST API, which allows to manage Albums, Artists, Users resources. 
[MapR Music UI](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-ui) is built on top of MapR Music REST Service 
API and provides user interface to perform various operations.

#### MapR Music REST Service Endpoints

| Endpoint                                 | Description                                                              |
| ---------------------------------------- | ------------------------------------------------------------------------ |
| /albums/                                 | Allows to manage Albums resources.                                       |
| /albums/{id}/tracks                      | Get track list of specified Album.                                       |
| /albums/{id}/recommended                 | Recommendations for specified Artist.                                    |
| /albums/{id}/rating                      | User rating for specified Artist.                                        |
| /albums/slug/{slug}                      | Get Artists by it's slug name.                                           |
| /artists/                                | Allows to manage Artists resources.                                      |
| /artists/slug/{slug}                     | Get Artists by it's slug name.                                           |
| /artists/{id}/recommended                | Recommendations for specified Artist.                                    |
| /artists/{id}/rating                     | User rating for specified Artist.                                        |
| /users/current                           | Get authorized user info.                                                |
| /users                                   | Allows to register new users.                                            |
| /languages                               | Get list of supported languages.                                         |
| /reporting/artists/top-{count}-area	     | Get the area with the most artists.                                      |
| /reporting/albums/top-{count}-languages  | Get the languages with the most albums.                                  |
| /reporting/albums/per-year-last-{count}  | Get the Number of Albums per year.                                       |
| /search/name                             | Search Artists and Albums by name entry. Uses Elastic Search.            |
| /search/artists/name                     | Search artists by name entry. Uses Elastic Search.                       |
| /search/albums/name                      | Search albums by name entry. Uses Elastic Search.                        |

Note: to get detailed information about endpoints, navigate to [API Reference](http://localhost:8080/api-reference) 
page of MapR Music UI.

## Basics 

#### JAX-RS `javax.ws.rs.core.Application` class

Creating JAX-RS Web Application starts with defining main class, that extends `javax.ws.rs.core.Application` class. 
Below, you can see code snippet of such `MaprMusicApp` class:
```
@ApplicationPath("/api/1.0/")
public class MaprMusicApp extends Application {

    private Set<Object> singletons = new HashSet<>();

    public MaprMusicApp() {
        // Configure and Initialize Swagger
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/mapr-music-rest/api/1.0/");
        beanConfig.setResourcePackage("com.mapr.music.api");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {

        Set<Class<?>> resources = new HashSet<>();
        resources.add(AlbumEndpoint.class);
        resources.add(ArtistEndpoint.class);
        
        ...

        return resources;
    }

}
```

Where:
* `getSingletons()`

Get a set of root resource, provider and feature instances. Fields and properties of returned instances are injected 
with their declared dependencies by the runtime prior to use.

* `getClasses()`

Get a set of root resource, provider and feature classes. The default life-cycle for resource class instances is 
per-request. The default life-cycle for providers (registered directly or via a feature) is singleton.

#### `beans.xml` deployment descriptor

In order to use dependency injection in your application, you must define `beans.xml` deployment descriptor file. 
Below you can see basic `beans.xml` file, which is used in MapR Music REST Service:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns="http://xmlns.jcp.org/xml/ns/javaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
                      http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
        bean-discovery-mode="all">
</beans>
```

#### `web.xml` deployment descriptor

Although, providing `web.xml` deployment descriptor file is not required, MapR Music REST Service uses this file to 
define security constrains on various resources. As you can see below, all modification operations require users to be 
authorized:
```xml
<?xml version="1.0"?>
<web-app version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">

    <security-constraint>
        <web-resource-collection>
            <web-resource-name>api</web-resource-name>
            <url-pattern>/api/1.0/albums/*</url-pattern>
            <url-pattern>/api/1.0/artists/*</url-pattern>
            <url-pattern>/api/1.0/languages/*</url-pattern>
            <http-method>POST</http-method>
            <http-method>PUT</http-method>
            <http-method>DELETE</http-method>
        </web-resource-collection>
        <auth-constraint>
            <role-name>admin</role-name>
        </auth-constraint>
    </security-constraint>

    <security-constraint>
        <web-resource-collection>
            <web-resource-name>api</web-resource-name>
            <url-pattern>/api/1.0/users/current</url-pattern>
            <http-method>GET</http-method>
        </web-resource-collection>
        <auth-constraint>
            <role-name>admin</role-name>
        </auth-constraint>
    </security-constraint>

    <login-config>
        <auth-method>BASIC</auth-method>
        <realm-name>ApplicationRealm</realm-name>
    </login-config>

    <security-role>
        <role-name>admin</role-name>
    </security-role>
</web-app>
```

#### Endpoint example

Presentation layer of MapR Music REST Service is responsible of exposing REST API. It implemented using JAX-RS 
annotations. It communicates with business layer to get the model data in a suitable form and sends it as response to 
the client's request. Here is code snippet of Album endpoint:
```
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
    
    ...
    
}
```

Where:

* `@Path`

JAX-RS annotation which identifies the URI path that a resource class or class method will serve requests for.

* `@Produces`

JAX-RS annotation which defines the media type(s) that the methods of a resource class or MessageBodyWriter can produce.

* `@Inject`

Identifies injectable constructors, methods, and fields.

* `@GET`

JAX-RS annotation which indicates that the annotated method responds to HTTP GET requests

* `@Api`

Swagger annotation, which is used to describe entire Albums endpoint. It allows to document API in a simple way.

* `@ApiOperation`

Swagger annotation, which is used to document separate method.


MapR Music REST Service implemented using layered architecture pattern. To get more info, see 
[MapR Music Architecture](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/002-mapr-music-architecture.md#mapr-music-rest-service-architecture).
