# Introduction

MapR Music Application uses the [MusicBrainz](https://musicbrainz.org/) database to explain how to develop an 
application with MapR Converged Data Platform with a focus on MapR-DB JSON.


## Contents

* [Introduction](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/001-introduction.md)
* [MapR Music Architecture](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/002-mapr-music-architecture.md)
* [Setup](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/003-setup.md)
* [Import the Data Set](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/004-import-the-data-set.md)
* [Working with MapR-DB](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/005-working-with-mapr-db.md)
* [Working with Java](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/006-working-with-java.md)
* [Create a REST API](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/007-create-a-rest-api.md)
* [Deploy to Java EE (Wildfly)](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/008-deploy-to-wildfly.md)
* [Building a UI with Angular](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/009-building-a-ui-with-angular.md)
* [Working Arrays](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/010-working-with-arrays.md)
* [CDC](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/011-change-data-capture.md)
* [Creating Recommendation Engine](https://github.com/mapr-demos/mapr-music/blob/master/doc/tutorials/012-creating-recommendation-engine.md)

## Repository overview

[MapR Music Repository](https://github.com/mapr-demos/mapr-music/) contains following directories:

* [bin](https://github.com/mapr-demos/mapr-music/tree/master/bin)

Directory contains scripts for registering users at Wildfly application server, importing MapR Music Data Set, testing 
recommendation engine and running the app in a Docker container.

* [conf](https://github.com/mapr-demos/mapr-music/tree/master/conf)

Contains Wildfly configuration files, which are used while building MapR Music Docker image.

* [data-generator](https://github.com/mapr-demos/mapr-music/tree/master/data-generator)

Utility application which allows to convert [MusicBrainz](https://musicbrainz.org/) database dump into MapR Music Dataset.

* [dataset](https://github.com/mapr-demos/mapr-music/tree/master/dataset)

Sample, ready to use MapR Music dataset, generated with the script described above.

* [doc](https://github.com/mapr-demos/mapr-music/tree/master/doc)

Application's documentation and tutorial.

* [elasticsearch-service](https://github.com/mapr-demos/mapr-music/tree/master/elasticsearch-service)

MapR Music Elasticsearch Service, which listens albums and artists table events and publishes the changes to the Elasticsearch. This service use MapR-DB Change Data Capture (CDC) feature.

* [mapr-rest](https://github.com/mapr-demos/mapr-music/tree/master/mapr-rest)

MapR Music REST Service, built on top of MapR-DB. This Web application developped in Java use [JAX-RS](https://github.com/jax-rs) and [OJAI](https://maprdocs.mapr.com/home/MapR-DB/JSON_DB/develop-apps-jsonDB.html) to expose CRUD operations at the top of MapR-DB tables.

* [mapr-ui](https://github.com/mapr-demos/mapr-music/tree/master/mapr-ui)

[Angular](https://angular.io/) application that consume the REST APIs to allow user to browse, edit and create albums and artists.

* [recommendation-engine](https://github.com/mapr-demos/mapr-music/tree/master/recommendation-engine)

Recommendation Engine, built using Spark MLlib's Alternating Least Squares (ALS) algorithm, to create Albums/Artists recommendations. This service uses the [MapR-DB OJAI Connector for Apache Spark](https://maprdocs.mapr.com/home/Spark/NativeSparkConnectorJSON.html) to read albums, artists ratings, and writes the recommendations back into MapR-DB.

Moreover, repository contains [Dockerfile](https://github.com/mapr-demos/mapr-music/blob/master/Dockerfile), which allows 
you to build Mapr Music Docker image based on [MapR PACC](https://mapr.com/products/persistent-application-client-container/), download,configure and start: Wildfly, Elasticsearh and deploy MapR Music application.
