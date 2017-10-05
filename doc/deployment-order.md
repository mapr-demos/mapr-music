# MapR Music deployment order

Since the recent version of MapR Music application consists of several modules and depends on Elastic Search and CDC, 
we have to adjust the environment and deploy the app in a proper way:

* Ensure that ElasticSearch is installed and run on the dev machine(along with Wildfly server)
* Create MapR-DB tables and change permissions as described 
[here](https://github.com/mapr-demos/mapr-music/blob/master/doc/music-dataset-generation.md#import-data-in-mapr-db). 
DO NOT import the data. We will do it later.

* Create changelogs and add them to the Artists and Albums tables as described 
[here](https://github.com/mapr-demos/mapr-music/blob/master/doc/change-data-capture.md). 

* Build and run `elasticsearch-service`:

```
$ cd mapr-music/core-application/streaming/elasticsearch-service
$ mvn clean install
$ cd target
$ java -jar elasticsearch-service-1.0-SNAPSHOT.jar
```
It will start service with listens Artists/Albums changelogs and publishes the changes to the ElasticSearch. Thus 
records for all documents that inserted into Artists/Albums tables will be created at ElasticSearch.

* Deploy MapR Music REST app

Note: ensure that MapR Music REST app is deployed BEFORE dataset import. Since 
[issue #31](https://github.com/mapr-demos/mapr-music/issues/31) MapR Music app maintains `/apps/statistics` table, 
which contains total number of Artists/Albums document. So we have to be sure that `StatisticService` of MapR Music up 
is run before dataset import.

* Import the dataset as described [here](https://github.com/mapr-demos/mapr-music/blob/master/doc/music-dataset-generation.md#import-data-in-mapr-db)

Note: the latest dataset archive with name `issue-73-dataset.tar.gz` can be found at Google Drive. It has only 500 
artist and 346 album documents and can be used for testing. Rating documents contained at multiple directories and 
should be imported one by one in case of lack of memory.

### Register users from dataset at Wildfly

Data Converter root directory contains 'add-wildfly-users.sh' script, which can be used to register users from dataset 
at Wildfly. Below you can see script usage information:
```
$ ./add-wildfly-users.sh -h
Usage: add-wildfly-users.sh [-p|--path] [-l|--limit] [-h|--help]
Options:
    --path      Specifies path to the 'users' dataset directory. Default value is current directory.
    --limit     Specifies maximum number of users, which will be registered at Wildfly. Dafault value is '3'.
    --help      Prints usage information.
```

Note: script will register 3 predefined users('jdoe, sdavis, mdupont') even without actual dataset. Each of the 
users has 'music' password.

Note: script assumes that `WILDFLY_HOME` environment variable is set and it points to the Wildfly root directory.

### Run recommendation engine

Recommendation engine can be run from the Dev machine in the following way:
```
$ cd mapr-music/core-application/processing/recommendation-engine/
$ mvn clean install scala:run
```
