# Dockerizing

You can easily deploy and test MapR Music application by using Docker. MapR Music image based on 
[MapR Persistent Application Client Container image](https://docstage.mapr.com/60/AdvancedInstallation/UsingtheMapRPACC.html), 
which is a Docker-based container image that includes a container-optimized MapR client. The PACC provides seamless 
access to MapR Converged Data Platform services, including MapR-FS, MapR-DB, and MapR-ES. The PACC makes it fast and 
easy to run containerized applications that access data in MapR.

### Building MapR Music Docker image 

MapR Music Dockerfile allows you to build Mapr Music Docker image, which contains properly configured 
Wildfly application server, ElasticSearch and MapR Music components. 

In order to build Mapr Music Docker image execute the following commands:
```
$ mvn clean install
$ docker build -t mapr-music .
```

Note that users from sample dataset register as Wildfly users during the build so you can use each of usernames to 
login into application.

### Running container

In order to create and run container from existing `mapr-music` image use the following command:
```
$ docker run -it -e MAPR_CONTAINER_USER=mapr -e MAPR_CONTAINER_GROUP=mapr -e MAPR_CONTAINER_UID=5000 -e MAPR_CONTAINER_GID=5000 -e MAPR_CLDB_HOSTS=192.168.99.18 -e MAPR_CLUSTER='my.cluster.com' -e DRILL_NODE=192.168.99.18 --net=host -p 8080:8080  mapr-music

```

Where:
* `MAPR_CONTAINER_USER`
The user that the user application inside the Docker container will run as. This configuration is functionally 
equivalent to the Docker native `-u` or `--user`. Do not use Docker `-u` or `--user`, as the container needs to start as 
the root user to bring up FUSE before switching to the `MAPR_CONTAINER_USER`.

* `MAPR_CONTAINER_GROUP`
The group that the application inside the Docker container will run as. This is a companion to the 
`MAPR_CONTAINER_USER` option. If a group name is not provided, the default is users. Providing a group name is strongly 
recommended.

* `MAPR_CONTAINER_UID`
The UID that the application inside the Docker container will run as. This is a companion to the MAPR_CONTAINER_USER 
option. If a UID is not provided, the default is UID 1000. Providing a UID is strongly recommended.

* `MAPR_CONTAINER_GID`
The GID that the application inside the Docker container will run as. This is a companion to the MAPR_CONTAINER_USER 
option. If a GID is not provided, the default is GID 1000. Providing a GID is strongly recommended.

* `MAPR_CLDB_HOSTS`
The list of CLDB hosts of your MapR cluster.

* `MAPR_CLUSTER`
The name of the cluster.

* `DRILL_NODE`
Drill node hostname.

In the example above, containers Wildfly port `8080` is bound to the hosts `8080` port, so navigate to 
[http://localhost:8080](http://localhost:8080) in order to access MapR Music UI.
