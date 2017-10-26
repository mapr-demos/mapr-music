# MapR Music Architecture

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

TODO: find appropriate scheme.

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

According to  [Release](https://github.com/mapr-demos/mapr-music/wiki/Design-docs#release) entity from the 
[Database Design](https://github.com/mapr-demos/mapr-music/wiki/Design-docs) such model will be similar to the  example 
below:
```
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable("/apps/albums")
public class Album {
    
    @JsonProperty("_id")
    private String id;
    private String name;
    private String genre;
    private String style;
    private String barcode;
    private String status;
    private String packaging;
    private String language;
    private String script;
    private String MBID;
    private String format;
    private String country;
    private List reviews;

    @JsonProperty("artist_list")
    private List artistList;

    @JsonProperty("catalog_numbers")
    private List catalogNumbers;

    @JsonProperty("track_list")
    private List trackList;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("images_urls")
    private List<String> imagesUrls;

    @JsonProperty("released_date")
    private Long releasedDate;
 
    // Getters and setters are omitted for the sake of brevity
}
```
The model class completely reflects [Release](https://github.com/mapr-demos/mapr-music/wiki/Design-docs#release) entity 
and using of Jackson annotations helps to bypass some mismatches, related to differences in naming conventions. Also 
you can notice `@MaprDbTable` annotation. It's custom annotation used to define MapR-DB JSON Table path so 
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

This approach is used by by [abstract DAO class](LINK), which implements common operations.

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
data. Source code of all interfaces and actual implementation classes can be found at [service package](LINK). 

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
            <li>'sort_type' - 'ASC' or 'DESC' sort type</li>
            <li>'sort_fields' - fields for ordering</li>
        </ul>
      </td>
      <td><code>curl -X GET "http://localhost:8080/mapr-music/api/1.0/albums?per_page=1&page=1548&sort_type=DESC&sort_fields=_id&sort_fields=name"</code></td>
    </tr>
    <tr>
          <td>GET</td>
          <td>/api/1.0/albums/{id}</td>
          <td>Get single album by it's identifier</td>
          <td><code>curl -X GET http://localhost:8080/mapr-music/api/1.0/albums/1</code></td>
    </tr>
    <tr>
          <td>DELETE</td>
          <td>/api/1.0/albums/{id}</td>
          <td>Delete single album by it's identifier</td>
          <td><code>curl -X DELETE http://localhost:8080/mapr-music/api/1.0/albums/1</code></td>
    </tr>
    <tr>
          <td>PUT</td>
          <td>/api/1.0/albums/{id}</td>
          <td>Update single album by it's identifier</td>
          <td><code>curl -d '{"name":"NEW NAME"}' -H "Content-Type: application/json" -X PUT http://localhost:8080/mapr-music/api/1.0/albums/1</code></td>
    </tr>
    <tr>
          <td>POST</td>
          <td>/api/1.0/albums</td>
          <td>Creates album according to the request body</td>
          <td><code>curl -d '{"name":"NEWLY CREATED"}' -H "Content-Type: application/json" -X POST http://localhost:8080/mapr-music/api/1.0/albums</code></td>
    </tr>
  </tbody>
</table>

Album service presentation layer implemented using JAX-RS. It communicates with business layer to get the Albums data in 
a suitable form and sends it as response to the client's request. Here is the listing of Album endpoint:
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

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get single album by it's identifier")
    public AlbumDto getAlbum(@PathParam("id") String id) {
        return albumService.getAlbumById(id);
    }

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of albums, which is represented by page")
    public ResourceDto<AlbumDto> getAllAlbums(@QueryParam("per_page") Long perPage,
                                              @QueryParam("page") Long page,
                                              @QueryParam("sort_type") String order,
                                              @QueryParam("sort_fields") List<String> orderFields) {

        return albumService.getAlbumsPage(perPage, page, order, orderFields);
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
    public AlbumDto createAlbum(Album album) {
        return albumService.createAlbum(album);
    }
}
```
