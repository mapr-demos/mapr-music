# How to deploy to Wildfly

This document explains how to configure environment and start developing MapR-DB web applications. 
As an example we will consider basic JAX-RS service, which is deployed in Wildfly 10 and provides REST API for 
accessing sample data, stored in MapR-DB. The sample data are represented as JSON documents and provides basic 
information about music albums.

Contents:
* MapR 6.0 Cluster setup
* Installing and configuring MapR Client
* Installing and configuring Wildfly
* Build and deployment

## MapR 6.0 Cluster setup

First of all, you should have properly configured MapR cluster with Drill installed. If you don't have one, please 
follow the instructions from: 
https://docstage.mapr.com/public/beta/60/AdvancedInstallation/Installing_6.0_beta_software.html


#### Storing the sample data

The first thing to be done for storing data - is creating JSON table using `maprcli`:
`maprcli table create -path /apps/albums -tabletype json`

In order to publish the data to MapR-DB we will use MapR-DB Shell. MapR-DB Shell is utility that allows to 
interact with JSON tables from the command line. As `mapr` user in a terminal enter the following commands to publish 
the data:
```
$ mapr dbshell

maprdb mapr:> insert /apps/albums --value '{"_id":"1","name":"Album1","genre":"Some genre 1","style":"Some style 1","barcode":"123456789","status":"official","packaging":"Keep Case","language":"eng","script":"Latin","format":"CD","country":"GB"}'

maprdb mapr:> insert /apps/albums --value '{"_id":"2","name":"Album2","genre":"Some genre 2","style":"Some style 2","barcode":"123456789","status":"official","packaging":"Keep Case","language":"eng","script":"Latin","format":"CD","country":"GB"}'

maprdb mapr:> insert /apps/albums --value '{"_id":"3","name":"Album4","genre":"Some genre 3","style":"Some style 3","barcode":"123456789","status":"official","packaging":"Keep Case","language":"eng","script":"Latin","format":"CD","country":"GB"}'
```

You can then find all the documents storing in `albums` table by executing the following:
```
maprdb mapr:> jsonoptions --pretty true

maprdb mapr:> find /apps/albums
```

The next thing is to change table permissions to allow any user to manage the table:
`$ maprcli table cf edit -path /apps/albums -cfname default -readperm p -writeperm p -traverseperm  p`

For more details about Permission Types see: 
https://docstage.mapr.com/public/beta/60/MapR-DB/JSON_DB/granting_or_denying_access_to_fields_with_aces.html


#### MapR-DB Query Service configuration

The sample application uses OJAI connector to access MapR-DB, which in it's turn uses MapR-DB Query Service powered 
by Drill. So you have to be sure that MapR-DB Query Service is enabled for you cluster and Drill is configured in a 
proper way. Execute the next command to ensure that Query Service is enabled:
`maprcli cluster queryservice getconfig -cluster < your-cluster-name >`

The following output indicates that Query Service is enabled:
```
$ maprcli cluster queryservice getconfig -cluster my.cluster.id
storageplugin  zookeeper      znode  clusterid           enabled  
dfs            zkhost:5181   drill  my.cluster.id       true   
```

To change Query Service configuration use `setconfig` command:

```
maprcli cluster queryservice setconfig 
              [ -cluster < cluster-name > ]
              -enabled < true | false >
              -clusterid < cluster-id of MapR Drill cluster >
              -storageplugin < Name of MapR Drill Storage plug-in >
              -znode < Root Zookeeper node used by MapR Drill cluster >
```

Note, that in above command parameter `-clusterid`  refer to the value of the cluster-id parameter in the 
`drill-override.conf` file. Below you can see the minimal Drill configuration, which can be found at 
`/opt/mapr/drill/drill-<drill-version>/conf/drill-override.conf`:
```
drill.exec: {
	cluster-id: "my.cluster.id",
 	zk.connect: "zkhost:5181"
}
```

## Installing and configuring MapR Client

The `mapr-client` package must be installed on each node where you will be building and running your applications. 
This package installs all of the MapR Libraries needed for application development regardless of programming language or 
type of MapR-DB table (binary or JSON).

Note: The package mapr-core contains the files that are in the mapr-client package. 
If you have installed the mapr-core package, you do not need to install the mapr-client package.

Complete the following steps to install the `mapr-client` package from a repository:
1. Configure the repository to point to http://package.mapr.com/releases/<release version>/<operating system> 

For example, if your VM has a CentOS operating system, edit the `/etc/yum.repos.d/mapr_core.repo` file and add the 
location.

2. Based on your operating system, run one of the following commands to install the package:
   * On Red Hat /Centos: `yum install mapr-client`
   * On Ubuntu: `apt-get install mapr-client`
   * On SUSE: `zypper install mapr-client`
    
Client applications connect to a cluster via CLDB nodes, which are listed in the connection request or in the 
`mapr-clusters.conf` file on the machine that submits the connection request. 
So, create or modify `/opt/mapr/conf/mapr-clusters.conf` and add the following configuration:

`my.cluster.com secure=false mapr60:7222`
Where:

* `my.cluster.id` is the name of your cluster
* `secure=false` specifies that the cluster secure mode is not enabled
* `cldbhost:7222` is the host and port of the CLDB.

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
To get more details about loading MapR Native Library see: 
https://docstage.mapr.com/public/beta/60/DevelopmentGuide/c-loading-mapr-native-library.html
 
Start application server in standalone mode:
```
/wildfly-10.1.0.Final/bin$ export JAVA_OPTS='-Dmapr.library.flatclass'

/wildfly-10.1.0.Final/bin$ ./standalone.sh
```

## Creating MapR-FS Wildfly module

While trying to redeploy the app you can face `UnsatisfiedLinkError: Native Library * already loaded in another classloader` 
error. In order to support app redeployment we have to create MapR-FS Wildfly module. Wildfly module will be loaded only 
once  and can be shared between different web apps. Thus, MapR Native Library will be loaded once while loading MapR-FS 
Wildfly module.

To create MapR-FS Wildfly module we have to:
#### Get maprfs JAR and it's dependencies

Lets use maven dependency plugin to download maprfs jar and it's dependencies:
* First of all create 'pom.xml', declare maprfs dependency and maven dependency plugin

```
$ mkdir maprfs-module
$ cd maprfs-module/
$ nano pom.xml
```

Paste the following into `pom.xml`:
```
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
        <mapr.library.version>6.0.0-mapr-beta</mapr.library.version>
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

#### Create directory structure for MapR-FS Wildfly module

```
$ cd $JBOSS_HOME/modules/system/layers/base/
$ mkdir -p com/maprfs/module/main
$ cd com/maprfs/module/main
```

#### Create `module.xml` module descriptor file and paste the following code snippet

```
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.3" name="com.maprfs.module">
    <resources>
        <resource-root path="maprfs-6.0.0-mapr-beta.jar"/>
        <resource-root path="commons-logging-1.1.3.jar"/>
        <resource-root path="protobuf-java-2.5.0.jar"/>
        <resource-root path="hadoop-annotations-2.7.0-mapr-1707-beta.jar"/>
        <resource-root path="hadoop-auth-2.7.0-mapr-1707-beta.jar"/>
        <resource-root path="hadoop-common-2.7.0-mapr-1707-beta.jar"/>
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

#### Copy JARs to the newly created Wildfly module

Assuming that `home` directory contains `maprfs-module` with `pom.xml`, created at 'Get maprfs JAR and it's dependencies' 
step.
```
$ cp ~/maprfs-module/target/dependency/*.jar .
```

#### Create `jboss-deployment-structure.xml` file at `WEB-INF` project directory

`jboss-deployment-structure.xml` declares dependency on MapR-FS module:
```
<?xml version="1.0"?>
<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
    <deployment>
        <dependencies>
            <module name="com.maprfs.module"/>
        </dependencies>
    </deployment>
</jboss-deployment-structure>

```

#### Change client application `pom.xml`

At this point you have to change client's web application `pom.xml` to use `maprfs` as provided dependency:
```
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

At this point you are ready to build and deploy the sample application. 
Clone repository and build the project using Maven:
```
$ git clone https://github.com/mapr-demos/mapr-music.git

$ cd mapr-music/

$ git checkout feature/Basic_service

$ cd core-application/webapp/mapr-rest/

$ mvn clean install
```

Use Wildfly Command Line Interface (CLI) management tool to deploy the app:
```
/wildfly-10.1.0.Final/bin$ ./jboss-cli.sh --connect

[standalone@localhost:9990 /] deploy ~/mapr-music/core-application/webapp/mapr-rest/target/mapr-music.war
```

Ensure that application is running by executing the following command:
```
$ curl -v http://localhost:8080/mapr-music/api/1.0/albums/1

*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /mapr-music/api/1.0/albums/1 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.47.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< X-Powered-By: Undertow/1
< Access-Control-Allow-Headers: origin, content-type, accept, authorization
< Server: WildFly/10
< Date: Thu, 24 Aug 2017 12:51:53 GMT
< Connection: keep-alive
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Credentials: true
< Content-Type: application/json
< Content-Length: 197
< Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD
< Access-Control-Max-Age: 1209600
< 
* Connection #0 to host localhost left intact
{"name":"Album1","genre":"Somegenre1","style":"Somestyle1","barcode":"123456789","status":"official","packaging":"KeepCase","language":"eng","script":"Latin","format":"CD","country":"GB","_id":"1"}
```
