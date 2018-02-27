# Working with MapR-DB and Java

MapR Music Application is multi module **Maven** project. [Apache Maven](https://maven.apache.org/) is a software project management and comprehension tool.

Let's explore the project and its dependencies.

## Maven Dependencies

Maven is used to manage the project's build, reporting and documentation from a central piece of information. Maven 
manages project's modules, repositories and dependencies.

Let's start from root MapR Music `pom.xml`:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.mapr.music</groupId>
    <artifactId>mapr-music</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>
    <name>mapr-music</name>
    <url>https://github.com/mapr-demos/mapr-music</url>
    <modules>
        <module>mapr-ui</module>
        <module>mapr-rest</module>
        <module>data-generator</module>
        <module>elasticsearch-service</module>
        <module>recommendation-engine</module>
    </modules>
</project>
```

You can see the 5 modules used to build this project. You can build them using the simple command `mvn clean install` or any IDE with proper Maven integration.

#### MapR Music REST Service dependencies

MapR Music REST Service uses OJAI Driver to access MapR-DB JSON Tables. In order to use OJAI and MapR-DB you must add 
the MapR Maven Repository and the MapR OJAI Dependencies to your project.

* MapR Maven Repository

```xml
    <repository>
      <id>mapr-releases</id>
      <url>http://repository.mapr.com/maven/</url>
    </repository>
```

* MapR-DB and OJAI Dependencies

```xml
    <dependency>
      <artifactId>mapr-ojai-driver</artifactId>
      <groupId>com.mapr.ojai</groupId>
      <version>6.0.0-mapr</version>
    </dependency>
```

This dependencies make all MapR-DB JSON available to your Java application using the OJAI API.

* OJAI Connection

The first thing to do when you want to use MapR-DB is to get an OJAI connection to the cluster using the following code:

```
    // Create an OJAI connection to MapR cluster
    Connection connection = DriverManager.getConnection("ojai:mapr:");
	
```

In a previous step, you  have installed the MapR-Client, this client is used to determine which cluster to use to connect your application.
In a next step, you will also learn how to run the application using a Docker container, this container will also have the MapR-Client installed and configure to connect to the cluster.

The authentication of the application is done using the user that is running the Java process, or using a Ticket (MapR Ticket, or Kerberos) when you are using a Secured Cluster. This to explain that you should not be surprised by the fact that you do not see any username in the connection string, the user is linked to the system or ticket.

* Access a Document Store

OJAI expose the MapR-DB JSON Tables as a DocumentStore that you get from the Connection object:

```
    // Get an instance of OJAI DocumentStore
    DocumentStore store = connection.getStore("/apps/users");
```
The `/apps/users` is a path to a MapR-DB JSON Table that must exist.

* Create and insert a Document

Use the [`connection.newDocument()`](https://maprdocs.mapr.com/apidocs/60/OJAI/org/ojai/store/Connection.html#newDocument--) method to create a new document from a `String`, `Map`, Java Bean or JSON Object.
Once the document is created use the [`store.insertOrReplace()`](https://maprdocs.mapr.com/apidocs/60/OJAI/org/ojai/store/DocumentStore.html#insertOrReplace-org.ojai.Document-) method, or other, to insert the document in the table.

```
    store.insertOrReplace(userDocument);
```

We are using here the `insertOrReplace()` method, that insert the document if it does not already exist, or simply replace it. This means the database does not have to check if a document exist with this `_id`. If you do a simple `insert()` the database check if a document with this `_id` exists the server will raise an exception.

Also, MapR Music Application uses [Mapr-DB Change Data Capture](https://maprdocs.mapr.com/home/MapR-DB/DB-ChangeData/changeData-overview.html) feature, so you must also declare CDC dependencies:

```xml
    <dependency>
        <groupId>com.mapr.db</groupId>
        <artifactId>maprdb-cdc</artifactId>
        <version>6.0.0-mapr</version>
    </dependency>
```

You can now use `com.mapr.db.cdc.ChangeDataRecordDeserializer` as value deserializer for your Kafka Change Data 
Records consumer:
```java
    ...
    
    Properties consumerProperties = new Properties();
    consumerProperties.setProperty("group.id", "mapr.music.statistics");
    consumerProperties.setProperty("enable.auto.commit", "true");
    consumerProperties.setProperty("auto.offset.reset", "latest");
    consumerProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    consumerProperties.setProperty("value.deserializer", "com.mapr.db.cdc.ChangeDataRecordDeserializer");
    
    // Create and adjust consumer which is used to consume MapR-DB CDC events for Albums table.
    KafkaConsumer<byte[], ChangeDataRecord> albumsChangelogConsumer = new KafkaConsumer<>(consumerProperties);
    albumsChangelogConsumer.subscribe(Collections.singletonList(ALBUMS_CHANGE_LOG));
    ChangeDataRecordHandler albumsHandler = new ChangeDataRecordHandler(albumsChangelogConsumer);
    albumsHandler.setOnDelete((id) -> decrementAlbums());
    albumsHandler.setOnInsert((id) -> incrementAlbums());
    
    ...
    
```

#### MapR Music Recommendation Engine dependencies

MapR Music Recommendation Engine implemented using Scala [MapR-DB OJAI Connector for Apache Spark](https://maprdocs.mapr.com/home/Spark/NativeSparkConnectorJSON.html. The recommendation engine's `pom.xml` must declare MapR Maven Repository and MapR Spark dependencies.

* MapR Maven Repository

```xml
    <repository>
      <id>mapr-releases</id>
      <url>http://repository.mapr.com/maven/</url>
    </repository>
```

* MapR Spark dependencies

```xml

    <!-- MapR Spark Core -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-core_2.11</artifactId>
        <version>2.1.0-mapr-1707</version>
    </dependency>

    <!--  MapR-DB OJAI Connector for Apache Spark -->
    <dependency>
        <groupId>com.mapr.db</groupId>
        <artifactId>maprdb-spark</artifactId>
        <version>6.0.0-mapr</version>
    </dependency>

    <!-- MapR Spark ML library, which is used to get recommendation predictions -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-mllib_2.11</artifactId>
        <version>2.1.0-mapr-1707</version>
    </dependency>

```

* Loading data via MapR-DB OJAI Connector for Apache Spark

MapR-DB OJAI Connector for Apache Spark allows us to load rating data from MapR-DB JSON Tables into Spark Dataset:

```scala
val ds = spark.loadFromMapRDB(tableName)
      .map(row => MaprRating(row.getAs[String]("_id"), row.getAs[String]("user_id"), row.getAs[String]("document_id"),
        row.getAs[Double]("rating")))
```

## Calling Drill from Java

MapR Music, like many other applications, needs to do some advanced queries with some aggregations that are not available in OJAI, for example to count the number of albums per country, or year. Instead of write code to do the compute on the client side, it is possible to use SQL to do all the aggregation; for this you just need to use the Apache Drill JDBC Driver and execute queries from your Java application.

Drill configuration for web applications includes creating 
[Drill JDBC module](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/008-deploy-to-wildfly.md#drill-jdbc-driver-module) 
and defining 
[Drill Data Source](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/008-deploy-to-wildfly.md#drill-data-source) 
at Widlfly.

After that, you can use Drill Data Source from Java web app:
The `com.mapr.music.dao.impl.ReportingDaoImpl` class shows how to use the Datasource and access MapR-DB data using SQL.

1. Inject the Datasource

```java 
    @Resource(lookup = "java:/datasources/mapr-music-drill")
    DataSource ds;
```

2. Use the JDBC Connection to execute SQL queries
```java
    Statement st = ds.getConnection().createStatement(); 
    ResultSet rs = st.executeQuery("SELECT * FROM ...");
    ....
```

---

Next : [MapR-DB Indexes](007-mapr-db-indexes.md)
