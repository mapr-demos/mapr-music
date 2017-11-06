# MapR Music Architecture

MapR Music Application consists of the following modules:
1. [MapR Music REST Service](https://github.com/mapr-demos/mapr-music/tree/master/mapr-rest)

MapR Music REST Service, built on top of MapR-DB. 

2. [MapR Music UI](https://github.com/mapr-demos/mapr-music/tree/master/mapr-ui)
                  
Angular app, which provides user interface.

3. [Elastic Search Service](https://github.com/mapr-demos/mapr-music/tree/master/elasticsearch-service)
                           
MapR Music Elastic Search Service, which listens changelogs and publishes the changes to the ElasticSearch.

4. [Recommendation Engine](https://github.com/mapr-demos/mapr-music/tree/master/recommendation-engine)

Recommendation Engine, built using Spark MLlib's Alternating Least Squares algorithm, which allows to make 
Albums/Artists recommendations.

5. [Data Generator](https://github.com/mapr-demos/mapr-music/tree/master/data-generator)
                   
Utility application which allows to convert [MusicBrainz](https://musicbrainz.org/) database dump into MapR Music Data 
Set.

## MapR Music REST Service Architecture

For developing MapR Music Application the layered architecture pattern was chosen. It's the most common architecture pattern, 
also known as the n-tier architecture pattern. It is the de facto standard for most Java EE applications and therefore 
is widely known by most architects, designers, and developers.

Album service will have three tiers, organized into horizontal layers, each layer performing a specific role within the 
application:
* Presentation Layer

Communicates with other tiers and provides results to the browser or other clients.

* Business Layer

Performs business logic on domains.

* Data Layer

Includes the data persistence mechanisms.

## Data layer

Albums data will be stored in 
[MapR-DB JSON Tables](https://docstage.mapr.com/public/beta/60/MapR-DB/JSON_DB/json_tables.html) so we need to implement 
persistence mechanism, which accesses 
[MapR-DB](https://docstage.mapr.com/public/beta/60/MapR-DB/developing_client_applications_for_mapr_db.html) and 
stores/queries data to/from it. This can be accomplished by using 
[OJAI Connection and Driver interfaces](https://docstage.mapr.com/public/beta/OJAI/index.html). Define OJAI Driver 
dependency in your `pom.xml`:
```
    <properties>
        <mapr.library.version>6.0.0-mapr-beta</mapr.library.version>
        ...
    </properties>

    <dependencies>
        <!-- OJAI Driver which is used to connect to MapR cluster -->
        <dependency>
            <artifactId>mapr-ojai-driver</artifactId>
            <groupId>com.mapr.ojai</groupId>
            <version>${mapr.library.version}</version>
        </dependency>
        ...
    </dependencies>
```

In order to implement Data layer we will use [DAO](https://en.wikipedia.org/wiki/Data_access_object) pattern. DAO stands 
for 'data access object', the object that provides an abstract interface to some type of database or other persistence 
mechanism. But first off all, we need to define Album model. 

Below you can see code snippet of 
[Album](https://github.com/mapr-demos/mapr-music/blob/master/mapr-rest/src/main/java/com/mapr/music/model/Album.java) 
model class:
```
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable("/apps/albums")
public class Album {
    
    @JsonProperty("_id")
    private String id;

    @JsonProperty("slug_name")
    private String slugName;

    @JsonProperty("slug_postfix")
    private Long slugPostfix;

    @JsonProperty("artists")
    private List<Artist.ShortInfo> artists;

    @JsonProperty("catalog_numbers")
    private List catalogNumbers;

    @JsonProperty("tracks")
    private List<Track> trackList;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("images_urls")
    private List<String> imagesUrls;

    private ODate releasedDate;

    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("barcode")
    private String barcode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("packaging")
    private String packaging;

    @JsonProperty("language")
    private String language;

    @JsonProperty("script")
    private String script;

    @JsonProperty("MBID")
    private String MBID;

    @JsonProperty("format")
    private String format;

    @JsonProperty("country")
    private String country;

    @JsonProperty("reviews")
    private List reviews;

    @JsonProperty("rating")
    private Double rating;
 
    // Getters and setters are omitted for the sake of brevity
}
```

You can notice `@MaprDbTable` annotation. It's custom annotation used to define MapR-DB JSON Table path so 
implementation of DAO will know which table to query.

OJAI Driver allows to create OJAI connection to the cluster and access the OJAI Document Store in the following way:
```
    ...
    
    // Create an OJAI connection to MapR cluster
    Connection connection = DriverManager.getConnection(CONNECTION_URL);

    // Get an instance of OJAI DocumentStore
    final DocumentStore store = connection.getStore(tablePath);

    // Create an OJAI Document form the JSON string (there are other ways too)
    final Document createdOjaiDoc = connection.newDocument(artistJsonString);
    
    // Insert the document into the OJAI store
    store.insertOrReplace(createdOjaiDoc);

    // Close this instance of OJAI DocumentStore
    store.close();

    // Close the OJAI connection and release any resources held by the connection
    connection.close();
    
    ...
```

This approach is used by by 
[MaprDbDao](https://github.com/mapr-demos/mapr-music/blob/master/mapr-rest/src/main/java/com/mapr/music/dao/MaprDbDao.java) 
class, which implements common operations.

OJAI [Connection](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/Connection.html) interface along with 
[DocumentStore](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html) interface defines all
key methods to interact with MapR-DB.

#### Query the Documents in the DocumentStore 

Get all Documents using 
[DocumentStore#find()](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#find--) method. You 
can specify conditions using [DocumentStore#find(QueryCondition condition)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#find-org.ojai.store.QueryCondition-)
or get single Document by it's identifier using [DocumentStore#findById(String id)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#findById-java.lang.String-)
method.

#### Use projection by specifying the list of fields that should be returned in the read document. 

Here listed some methods that allow to use projection:
* [DocumentStore#find(FieldPath... fieldPaths)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#find-org.ojai.FieldPath...-)
* [DocumentStore#find(String... fieldPaths)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#find-java.lang.String...-)
* [DocumentStore#find(QueryCondition condition, String... fieldPaths)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#find-org.ojai.store.QueryCondition-java.lang.String...-)
* [DocumentStore#findById(String id, FieldPath... fieldPaths)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#findById-java.lang.String-org.ojai.FieldPath...-)

#### Create Documents and insert them into DocumentStore

There are several way to create new OJAI Document using 
[Connection](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/Connection.html) interface:
* [Connection#newDocument()](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/Connection.html#newDocument--) - creates and returns a new, empty instance of an OJAI Document.
* [Connection#newDocument(String jsonString)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/Connection.html#newDocument-java.lang.String-) - returns a new instance of OJAI Document parsed from the specified JSON string.
* [Connection#newDocument(Object bean))](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/Connection.html#newDocument-java.lang.Object-) - returns a new instance of Document built from the specified Java bean.
* [Connection#newDocument(Map<String,Object> map)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/Connection.html#newDocument-java.util.Map-) - returns a new instance of Document constructed from the specified Map.

Created OJAI Document can be inserted in DocumentStore using 
[DocumentStore#insert(Document doc)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#insert-org.ojai.Document-) 
method.

#### Update Documents

In order to update the Document we should construct corresponding 
[DocumentMutation](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentMutation.html) object. This 
interface defines the APIs to perform mutation of a Document already stored in a DocumentStore. After that you can update the Document using following methods:
* [DocumentStore#update(String _id, DocumentMutation mutation)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#update-java.lang.String-org.ojai.store.DocumentMutation-)
* [DocumentStore#update(Value _id, DocumentMutation mutation)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#update-org.ojai.Value-org.ojai.store.DocumentMutation-)

For instance:
```
    ...
    
    // Create an OJAI connection to MapR cluster
    Connection connection = DriverManager.getConnection(CONNECTION_URL);

    // Get an instance of OJAI DocumentStore
    final DocumentStore store = connection.getStore(tablePath);

    String documentId = "ae425a74-7da0-49d8-a583-1b74943bde9a";
    
    // Create a DocumentMutation to update the zipCode field
    DocumentMutation mutation = connection.newMutation()
        .set("address.zipCode", 95196L);
    
    // Update the Document with '_id' = "ae425a74-7da0-49d8-a583-1b74943bde9a"
    store.update(documentId, mutation);

    // Close this instance of OJAI DocumentStore
    store.close();

    // Close the OJAI connection and release any resources held by the connection
    connection.close();
    
    ...
```

#### Delete Documents

Document can be deleted using 
[DocumentStore#delete(String _id)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#delete-java.lang.String-) 
method. Also, it's possible to delete the set of Documents that match some condition using 
[DocumentStore#checkAndDelete(String _id, QueryCondition condition)](https://docstage.mapr.com/public/beta/OJAI/org/ojai/store/DocumentStore.html#checkAndDelete-java.lang.String-org.ojai.store.QueryCondition-) 
method.


For more information about OJAI Driver refer [OJAI API library javadoc](https://docstage.mapr.com/public/beta/OJAI/index.html).

## Business Layer

Business layer is responsible of performing business logic. It provides data to the presentation layer in a suitable 
form using DTO pattern, implements pagination logic, communicates with Data Layer in order to fetch and persist the 
data. Source code of all interfaces and actual implementation classes can be found at 
[service package](https://github.com/mapr-demos/mapr-music/tree/master/mapr-rest/src/main/java/com/mapr/music/service). 

## Presentation Layer

Presentation Layer of Album Service is represented by REST API endpoints.

<table>
  <tbody>
    <tr>
      <th>HTTP Method</th>
      <th>Path</th>
      <th>Description</th>
      <th>Example</th>
    </tr>
    <tr>
      <td>GET</td>
      <td>/api/1.0/albums</td>
      <td>Get list of albums, which is represented by page. Supports the following query options:
        <ul>
            <li>'page' - specifies page number</li>
            <li>'per_page' - specifies number of albums per page</li>
            <li>'sort' - sort string, which contains 'ASC' or 'DESC' sort type and comma separated list of sort fields</li>
        </ul>
      </td>
      <td><code>curl -X GET "http://localhost:8080/mapr-music-rest/api/1.0/albums?per_page=2&page=1548&sort=ASC,released_date,name"</code></td>
    </tr>
    <tr>
          <td>GET</td>
          <td>/api/1.0/albums/{id}</td>
          <td>Get single album by it's identifier</td>
          <td><code>curl -X GET http://localhost:8080/mapr-music-rest/api/1.0/albums/1</code></td>
    </tr>
    <tr>
          <td>DELETE</td>
          <td>/api/1.0/albums/{id}</td>
          <td>Delete single album by it's identifier</td>
          <td><code>curl -u jdoe:music -X DELETE http://localhost:8080/mapr-music-rest/api/1.0/albums/1</code></td>
    </tr>
    <tr>
          <td>PUT</td>
          <td>/api/1.0/albums/{id}</td>
          <td>Update single album by it's identifier</td>
          <td><code>curl -u jdoe:music -d '{"name":"NEW NAME"}' -H "Content-Type: application/json" -X PUT http://localhost:8080/mapr-music-rest/api/1.0/albums/1</code></td>
    </tr>
    <tr>
          <td>POST</td>
          <td>/api/1.0/albums</td>
          <td>Creates album according to the request body</td>
          <td><code>curl -u jdoe:music -d '{"name":"NEWLY CREATED"}' -H "Content-Type: application/json" -X POST http://localhost:8080/mapr-music-rest/api/1.0/albums</code></td>
    </tr>
  </tbody>
</table>

Note: to get detailed endpoints description, follow [API Reference](http://localhost:8080/api-reference) link, at MapR Music UI.

Note: all modification operations require user authorization.
 
Presentation layer implemented using JAX-RS annotations. It communicates with business layer to get the model data in 
a suitable form and sends it as response to the client's request. Here is code snippet of Album endpoint:
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
