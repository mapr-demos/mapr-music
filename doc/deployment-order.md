# MapR Music deployment order

Since the recent version of MapR Music application consists of several modules and depends on Elastic Search and CDC, 
we have to adjust the environment and deploy the app in a proper way:

* Ensure that ElasticSearch is installed and run on the dev machine(along with Wildfly server)
* Create MapR-DB tables and change permissions as described 
[here](https://github.com/mapr-demos/mapr-music/blob/master/doc/music-dataset-generation.md#import-data-in-mapr-db). 
DO NOT import the data. We will do it later.
* Since [issue #31](https://github.com/mapr-demos/mapr-music/issues/31) MapR Music app uses `/apps/statistics` table. 
So we need to create it and change permissions as  with other tables(see previous step).
* Create changelogs and add them to the Artists and Albums tables as described 
[here](https://github.com/mapr-demos/mapr-music/blob/master/doc/change-data-capture.md). 

Note: Create Albums changelog in the same way. Use `/mapr_music_albums_changelog` as stream name and `albums` as topic 
name(Resulting full changelog path: `/mapr_music_albums_changelog:albums`)

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

Note: new dataset archive with name `27-09-lists-refactored.tar.gz` can be found at Google Drive.
