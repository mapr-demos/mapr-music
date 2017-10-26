# Introduction

MapR Music Application uses the [MusicBrainz](https://musicbrainz.org/) database to explain how to develop an 
application with MapR Converged Data Platform.


## Contents

* [Introduction](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/001-introduction.md)
* [MapR Music Architecture](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/002-mapr-music-architecture.md)
* [Setup](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/003-setup.md)
* [Import the Data Set](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/004-import-the-data-set.md)
* [Working with MapR-DB](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/005-working-with-mapr-db.md)
* [Working with Java](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/006-working-with-java.md)
* [Create a REST API](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/007-create-a-rest-api.md)
* [Deploy to Java EE (Wildfly)](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/008-deploy-to-wildfly.md)
* [Building a UI with Angular](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/009-building-a-ui-with-angular.md)
* [Working Arrays](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/010-working-with-arrays.md)
* [CDC](https://github.com/mapr-demos/mapr-music/blob/devel/doc/tutorials/011-change-data-capture.md)

## Repository overview

[MapR Music Repository](https://github.com/mapr-demos/mapr-music/) contains following directories:

* [bin](https://github.com/mapr-demos/mapr-music/tree/devel/bin)

Directory contains scripts for registering users at Wildfly application server, importing MapR Music Data Set, testing 
recommendation engine and running the app in a Docker container.

* [conf](https://github.com/mapr-demos/mapr-music/tree/devel/conf)

Contains Wildfly configuration files, which are used while building MapR Music Docker image.

* [data-generator](https://github.com/mapr-demos/mapr-music/tree/devel/data-generator)

Utility application which allows to convert [MusicBrainz](https://musicbrainz.org/) database dump into MapR Music Data 
Set.

* [dataset](https://github.com/mapr-demos/mapr-music/tree/devel/dataset)

Sample, ready to use MapR Music dataset.

* [doc](https://github.com/mapr-demos/mapr-music/tree/devel/doc)

Application's documentation.

* [elasticsearch-service](https://github.com/mapr-demos/mapr-music/tree/devel/elasticsearch-service)

MapR Music Elastic Search Service, which listens changelogs and publishes the changes to the ElasticSearch.

* [mapr-rest](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-rest)

MapR Music REST Service, built on top of MapR-DB.

* [mapr-ui](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-ui)

Angular app, which provides user interface.

* [recommendation-engine](https://github.com/mapr-demos/mapr-music/tree/devel/recommendation-engine)

Recommendation Engine, built using Spark MLlib's Alternating Least Squares algorithm, which allows to make 
Albums/Artists recommendations.

Moreover, repository contains [Dockerfile](https://github.com/mapr-demos/mapr-music/blob/devel/Dockerfile), which allows 
you to build Mapr Music Docker image, which contains properly configured Wildfly application server, 
ElasticSearch and MapR Music components.

