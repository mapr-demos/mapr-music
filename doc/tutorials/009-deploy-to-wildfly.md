# Deploy to Wildfly

[MapR Music REST Service](https://github.com/mapr-demos/mapr-music/tree/master/mapr-rest) and 
[MapR Music UI](https://github.com/mapr-demos/mapr-music/tree/master/mapr-ui) are deployed on Wildfly application server.


This document explains how to properly install and configure Wildfly application server, create required by MapR Music 
app modules.

You can also run the application using [MapR Persistent Application Client Container (PACC)](https://maprdocs.mapr.com/home/AdvancedInstallation/UsingtheMapRPACC.html), in this case all the configuration, deployment and run is done in the container, see [Run MapR Music in Docker Container](015-run-the-application-in-docker.md)

## Installing and configuring Wildfly

As an application server we will use Wildfly 10. In order to download it, follow the link: http://wildfly.org/downloads/
Extract the archive to a suitable location of your machine with installed MapR Client. For example:
```
$ unzip wildfly-10.1.0.Final.zip -d .
$ cd wildfly-10.1.0.Final/bin/

```

Now we have to specify `-Dmapr.library.flatclass` parameter via `JAVA_OPTS` environment variable to be sure that MapR 
Native Library is loaded in a proper way. Otherwise you will face `UnsatisfiedLinkError`. 
The parameter `-Dmapr.library.flatclass`, when specified with Java, disables the injection of code via the root class 
loader, thus disabling the loading of the MapR Native Library using the root class loader. 
To get more details about loading MapR Native Library in the MapR Documentation [Loading the MapR Native Library
](https://maprdocs.mapr.com/home/DevelopmentGuide/c-loading-mapr-native-library.html).

Start application server in standalone mode:
```
/wildfly-10.1.0.Final/bin$ export JAVA_OPTS='-Dmapr.library.flatclass'

/wildfly-10.1.0.Final/bin$ ./standalone.sh
```

#### Wildfly Managed Thread Factory
MapR Music Application consumes change data records in separate threads. Thus, we have to define Managed Thread Factory 
within Wildfly:
* Modify `$WILDFLY_HOME/standalone/configuration/standalone.xml` configuration file. Add the following code snippet as 
child of the `<managed-thread-factories>` element:

```xml
<managed-thread-factory name="maprMusicThreadFactory" jndi-name="java:jboss/ee/concurrency/factory/MaprMusicThreadFactory" context-service="default" priority="1"/>
```
* Restart Wildfly
* Now you can access Managed Thread Factory:

```java 
  @Resource(lookup = "java:jboss/ee/concurrency/factory/MaprMusicThreadFactory")
  private ManagedThreadFactory threadFactory;
  
  ...
  Thread thread = threadFactory.newThread(() -> {
             // Your runnable
          });
  thread.start();
  ...
  
```

#### Drill Data Source

MapR Music App uses Drill, so we need to define Drill Data source at Wildfly:

* Edit the following file `$WILDFLY_HOME/standalone/configuration/standalone.xml`

Where, `$WILDFLY_HOME` is the Wildfly home directory for example `~/wildfly-10.1.0.Final/`

* Find the `<datasources>` element and add the new datasource:

```xml
    <datasource jndi-name="java:/datasources/mapr-music-drill" pool-name="Emapr-music-drill" enabled="true" use-java-context="true">
        <connection-url>jdbc:drill:drillbit=[mapr-cluster-node]:31010</connection-url>
            <driver>drill</driver>
            <security>
               <user-name>mapr</user-name>
               <password>maprpassword</password>
            </security>
    </datasource>
```

* replace `[mapr-cluster-node]` with one of the node of your MapR 6.0 cluster

* replace the username and password to match your environment.

* Find the `<drivers>` element and add the new driver:
```xml
    <driver name="drill" module="org.apache.drill">
        <driver-class>org.apache.drill.jdbc.Driver</driver-class>
    </driver>
```

Also, you must create Drill JDBC Driver module to use Drill Data Source from Java. See next section in order to do so.

## Wildfly Modules

#### Drill JDBC Driver module

Get Drill and its JDBC Driver
* Dowload Apache Drill 1.11 from [Drill Web site](https://drill.apache.org/)
* Unzip Drill

Install JDBC Driver into Wildfly

* Create the following directory structure

```
$ cd $WILDFLY_HOME
$ mkdir -p modules/system/layers/base/org/apache/drill/main
```

* Copy the JDBC Driver in the new directory

```
$ cp $DRILL_HOME/jars/jdbc-driver/drill-jdbc-all-1.11.0.jar \ 
     $WILDFLY_HOME/modules/system/layers/base/org/apache/drill/main
```

* Create a `$WILDFLY_HOME/modules/system/layers/base/org/apache/drill/main/module.xml` file to register the new JDBC 
Driver, with the following content:

```xml
<module xmlns="urn:jboss:module:1.3" name="org.apache.drill">
    <resources>
         <resource-root path="drill-jdbc-all-1.11.0.jar"/> 
    </resources>

    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
        <module name="sun.jdk"/>
    </dependencies>
</module>
```

After that, you can use Drill Data Source from Java app:
The `com.mapr.music.dao.impl.ReportingDaoImpl` class shows how to use the Datasource and access MapR-DB data using SQL.

1. Inject the Datasource

```java 
    @Resource(lookup = "java:/datasources/mapr-music-drill")
    DataSource ds;
```

2. Use the JDBC Connection
```java
    Statement st = ds.getConnection().createStatement(); 
    ResultSet rs = st.executeQuery("SELECT * FROM ...");
    ....
```

#### MapR-FS Wildfly module (Optional)

While trying to redeploy the app you can face `UnsatisfiedLinkError: Native Library * already loaded in another classloader` 
error. In order to support app redeployment we have to create MapR-FS Wildfly module. Wildfly module will be loaded only 
once  and can be shared between different web apps. Thus, MapR Native Library will be loaded once while loading MapR-FS 
Wildfly module.

To create MapR-FS Wildfly module we have to:

##### Get maprfs JAR and it's dependencies

Lets use maven dependency plugin to download maprfs jar and it's dependencies:
* First of all create 'pom.xml', declare maprfs dependency and maven dependency plugin

```
$ mkdir maprfs-module
$ cd maprfs-module/
$ nano pom.xml
```

Paste the following into `pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.maprfs.module</groupId>
    <artifactId>maprfs-module</artifactId>
    <packaging>jar</packaging>
    <version>1.0</version>
    <name>maprfs-module</name>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <dependency.plugin.version>3.0.1</dependency.plugin.version>
        <mapr.library.version>6.0.0-mapr</mapr.library.version>
    </properties>

    <dependencies>
        <dependency>
            <artifactId>maprfs</artifactId>
            <groupId>com.mapr.hadoop</groupId>
            <version>${mapr.library.version}</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>mapr-releases</id>
            <url>http://repository.mapr.com/maven/</url>
        </repository>
    </repositories>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>${dependency.plugin.version}</version>
                <executions>
                    <execution>
                        <id>copy-dependencies</id>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-dependencies</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

```

* Download dependencies by executing `mvn dependency:copy-dependencies`

Dependencies will be downloaded at `target/dependency/`:
```
$ cd target/dependency/
$ ls
```

##### Create directory structure for MapR-FS Wildfly module

```
$ cd $JBOSS_HOME/modules/system/layers/base/
$ mkdir -p com/maprfs/module/main
$ cd com/maprfs/module/main
```

##### Create `module.xml` module descriptor file and paste the following code snippet

```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.3" name="com.maprfs.module">
    <resources>
        <resource-root path="maprfs-6.0.0-mapr.jar"/>
        <resource-root path="commons-logging-1.1.3.jar"/>
        <resource-root path="protobuf-java-2.5.0.jar"/>
        <resource-root path="hadoop-annotations-2.7.0-mapr-1707.jar"/>
        <resource-root path="hadoop-auth-2.7.0-mapr-1707.jar"/>
        <resource-root path="hadoop-common-2.7.0-mapr-1707.jar"/>
        <resource-root path="htrace-core-3.1.0-incubating.jar"/>
        <resource-root path="commons-configuration-1.6.jar"/>
        <resource-root path="commons-lang-2.6.jar"/>
        <resource-root path="commons-collections-3.2.2.jar"/>
        <resource-root path="slf4j-log4j12-1.7.10.jar"/>
        <resource-root path="zookeeper-3.4.5-mapr-1503.jar"/>
        <resource-root path="json-20080701.jar"/>
    </resources>

    <dependencies>
        <module name="javaee.api"/>
        <module name="com.google.guava"/>
        <module name="org.slf4j"/>
        <module name="org.apache.log4j"/>
    </dependencies>
</module>

```

Module descriptor file contains resources and dependencies declarations.

##### Copy JARs to the newly created Wildfly module

Assuming that `home` directory contains `maprfs-module` with `pom.xml`, created at 'Get maprfs JAR and it's dependencies' 
step.
```
$ cp ~/maprfs-module/target/dependency/*.jar .
```

##### Create `jboss-deployment-structure.xml` file at `WEB-INF` project directory

`jboss-deployment-structure.xml` declares dependency on MapR-FS module:

```xml 
<?xml version="1.0"?>
<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
    <deployment>
        <dependencies>
            <module name="com.maprfs.module"/>
        </dependencies>
    </deployment>
</jboss-deployment-structure>

```

##### Change client application `pom.xml`

At this point you have to change client's web application `pom.xml` to use `maprfs` as provided dependency:

```xml 
    <!--MapR-FS provided by newly created module-->
    <dependency>
        <artifactId>maprfs</artifactId>
        <groupId>com.mapr.hadoop</groupId>
        <version>${mapr.library.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- OJAI Driver which is used to connect to MapR cluster -->
    <dependency>
        <artifactId>mapr-ojai-driver</artifactId>
        <groupId>com.mapr.ojai</groupId>
        <version>${mapr.library.version}</version>
        <exclusions>
            <exclusion>
                <artifactId>maprfs</artifactId>
                <groupId>com.mapr.hadoop</groupId>
            </exclusion>
        </exclusions>
    </dependency>
```

Now you have to restart Wildfly and rebuild the project to use redeployment.


## Build and deployment

At this point you are ready to build and deploy the MapR Music application. 
Clone repository and build the project using Maven:
```
$ git clone https://github.com/mapr-demos/mapr-music.git

$ cd mapr-music/

$ mvn clean install
```

Use Wildfly Command Line Interface (CLI) management tool to deploy REST Service and UI:

```
/wildfly-10.1.0.Final/bin$ ./jboss-cli.sh --connect

[standalone@localhost:9990 /] deploy ~/mapr-music/mapr-rest/target/mapr-music-rest.war
[standalone@localhost:9990 /] deploy ~/mapr-music/mapr-ui/target/mapr-music-ui.war
```

Ensure that MapR REST Service is running by executing the following command:
```
$  curl -v http://localhost:8080/mapr-music-rest/api/1.0/albums?per_page=1
  *   Trying 127.0.0.1...
  * Connected to localhost (127.0.0.1) port 8080 (#0)
  > GET /mapr-music-rest/api/1.0/albums?per_page=1 HTTP/1.1
  > Host: localhost:8080
  > User-Agent: curl/7.47.0
  > Accept: */*
  > 
  < HTTP/1.1 200 OK
  < X-Powered-By: Undertow/1
  < Access-Control-Allow-Headers: origin, content-type, accept, authorization
  < Server: WildFly/11
  < Date: Thu, 26 Oct 2017 19:21:55 GMT
  < Connection: keep-alive
  < Access-Control-Allow-Origin: *
  < Access-Control-Allow-Credentials: true
  < Content-Type: application/json
  < Content-Length: 557
  < Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD
  < Access-Control-Max-Age: 1209600
  < 
  * Connection #0 to host localhost left intact
  {"pagination":{"items":6127,"page":1,"pages":6127,"per_page":1},"results":[{"name":"No I in Threesome","barcode":"0094639625628","_id":"00031241-434d-4f54-b170-f64db965e1fe","slug":"no-i-in-threesome-1","artists":[{"name":"Interpol","_id":"b23e8a63-8f47-4882-b55b-df2c92ef400e","slug":"interpol-0","profile_image_url":"http://www.xsilence.net/images/artistes/interpol/interviews/paris20021031575215800692.jpeg"}],"cover_image_url":"http://coverartarchive.org/release/00031241-434d-4f54-b170-f64db965e1fe/13327546856.jpg","released_date":1192406400000}]}
```

Navigate to [http://localhost:8080](http://localhost:8080) in order to access MapR Music UI.

---
Next: [Build the user interface with Angular](010-building-a-ui-with-angular.md)
