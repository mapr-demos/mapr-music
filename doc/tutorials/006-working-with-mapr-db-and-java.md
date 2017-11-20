# Working with MapR-DB and Java

MapR Music Application is multi module Maven project, which consists of 5 modules. Each of them, except of 
[Recommendation Engine](https://github.com/mapr-demos/mapr-music/tree/master/recommendation-engine), written using Java 
programming language. [Recommendation Engine](https://github.com/mapr-demos/mapr-music/tree/master/recommendation-engine)
uses Scala MapR-DB OJAI Connector for Apache Spark and therefore it is written on Scala.

## Maven Dependencies

Apache Maven is a software project management and comprehension tool. Based on the concept of a project object model 
(POM), Maven can manage a project's build, reporting and documentation from a central piece of information. Maven 
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

As you can see, it declares 5 MapR Music modules and allows you to build all of them at once using `mvn clean install` 
command.

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
      <version>6.0.0-mapr-beta</version>
    </dependency>
```
After that you can use OJAI classes in your project.

* OJAI Connection

The first thing to do when you want to use MapR-DB is to get an OJAI connection to the cluster using the following code:

```
    // Create an OJAI connection to MapR cluster
    Connection connection = DriverManager.getConnection("ojai:mapr:");
	
```

* Access a Document Store

OJAI expose the MapR-DB JSON Tables as a DocumentStore that you get from the Connection object:

```
    // Get an instance of OJAI DocumentStore
    DocumentStore store = connection.getStore("/apps/users");
```
The `/apps/users` is a path to a MapR-DB JSON Table that must exist.

* Create and insert a Document

Use the `connection.newDocument()` method to create a new document from a `String`, `Map`, Java Bean or JSON Object.
Once the document is created use the `store.insertOrUpdate()` method, or other, to insert the document in the table.

```
    store.insertOrReplace(userDocument);
```


Also, MapR Music Application uses Mapr-DB Change Data Capture feature, so you must also declare CDC dependencies:
```xml
    <dependency>
        <groupId>com.mapr.db</groupId>
        <artifactId>maprdb-cdc</artifactId>
        <version>6.0.0-mapr-beta</version>
    </dependency>
```

After that, you can use `com.mapr.db.cdc.ChangeDataRecordDeserializer` as value deserializer for your Kafka Change Data 
Records consumer:
```
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

MapR Music Recommendation Engine implemented using Scala MapR-DB OJAI Connector for Apache Spark. Therefore, 
Recommendation Engine's `pom.xml` must declare MapR Maven Repository and MapR Spark dependencies.

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
        <version>6.0.0-mapr-beta</version>
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
```
val ds = spark.loadFromMapRDB(tableName)
      .map(row => MaprRating(row.getAs[String]("_id"), row.getAs[String]("user_id"), row.getAs[String]("document_id"),
        row.getAs[Double]("rating")))
```

## Calling Drill from Java

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
