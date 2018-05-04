# How to deploy and use Drill from Java Web Application

This document explains how to configure your environment to use Apache Drill running on MapR Converged Data Platform to executre SQL Queries from a Java Web Application running in Wildfly.

**Prerequisites**

* MapR 6.0 Cluster
* MapR Music Dataset Installed (tables `/apps/albums` `/apps/artists` `/apps/languages`)

For this tutorial:

* `$WILDFLY_HOME` is the Wildfly home directory for example `~/wildfly-10.1.0.Final/`
* `$DRILL_HOME` is the Drill home directory for example `~/apache-drill-1.13.0/`

## Install Drill JDBC Driver

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
$ cp $DRILL_HOME/jars/jdbc-driver/drill-jdbc-all-1.13.0.jar \ 
     $WILDFLY_HOME/modules/system/layers/base/org/apache/drill/main
```

* Create a `$WILDFLY_HOME/modules/system/layers/base/org/apache/drill/main/module.xml` file to register the new JDBC Driver, with the following content:

```xml
<module xmlns="urn:jboss:module:1.3" name="org.apache.drill">
    <resources>
         <resource-root path="drill-jdbc-all-1.13.0.jar"/> 
    </resources>

    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
        <module name="sun.jdk"/>
    </dependencies>
</module>
```


## Register a new Datasource

* Edit the following file `$WILDFLY_HOME/standalone/configuration/standalone.xml`

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


## Use the Datasource in your Java Application

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




