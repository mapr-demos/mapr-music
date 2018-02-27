# Setting up your environment

## Install MapR

To deploy MapR-Music application you must have a MapR 6.0 cluster running, you have various options:

* [Use MapR Container for Developer](https://maprdocs.mapr.com/home/MapRContainerDevelopers/MapRContainerDevelopersOverview.html)
* [Use a MapR Sandbox VM](https://maprdocs.mapr.com/home/SandboxHadoop/c_sandbox_overview.html)
* [Install a new cluster](https://maprdocs.mapr.com/home/install.html) 

Once you have a running cluster, you can start to create the various tables and import data.

## Create Tables

1. Create MapR-DB JSON Tables, which will be used by MapR Music Application:
```
$ maprcli table create -path /apps/albums -tabletype json
$ maprcli table create -path /apps/artists -tabletype json
$ maprcli table create -path /apps/languages -tabletype json
$ maprcli table create -path /apps/albums_ratings -tabletype json
$ maprcli table create -path /apps/artists_ratings -tabletype json
$ maprcli table create -path /apps/users -tabletype json
$ maprcli table create -path /apps/statistics -tabletype json
$ maprcli table create -path /apps/recommendations -tabletype json

```

2. Change table permissions to allow access for the MapR Music application:
```
$ maprcli table cf edit -path /apps/albums -cfname default -readperm p -writeperm p -traverseperm  p
$ maprcli table cf edit -path /apps/artists -cfname default -readperm p -writeperm p -traverseperm  p
$ maprcli table cf edit -path /apps/languages -cfname default -readperm p -writeperm p -traverseperm  p
$ maprcli table cf edit -path /apps/albums_ratings -cfname default -readperm p -writeperm p -traverseperm  p
$ maprcli table cf edit -path /apps/artists_ratings -cfname default -readperm p -writeperm p -traverseperm  p
$ maprcli table cf edit -path /apps/users -cfname default -readperm p -writeperm p -traverseperm  p
$ maprcli table cf edit -path /apps/statistics -cfname default -readperm p -writeperm p -traverseperm  p
$ maprcli table cf edit -path /apps/recommendations -cfname default -readperm p -writeperm p -traverseperm  p
```

## Create Changelog

1. Create Changelog Streams for Artists and Albums:
```
$ maprcli stream create -path /apps/mapr_music_changelog -ischangelog true -consumeperm p
```

Where:
* `-ischangelog` set to `true` to configure the stream to store change log
* `-consumeperm p` set the changelog consumer presentation to "public" allowing any application to subscribe to the events.

2. Add Changelog to the `artists` and `albums` tables:
```
$ maprcli table changelog add -path /apps/artists -changelog /apps/mapr_music_changelog:artists
$ maprcli table changelog add -path /apps/albums -changelog /apps/mapr_music_changelog:albums
```

This command creates a new topic where all the changes will be published. To consume the changes you need to create a new Apache Kafka Consumer.

## Installing and configuring MapR Client


> Note that if you are using MapR Container for Developers, the MapR Client is automatically installed and configure you do not need to do it again.

The `mapr-client` package must be installed on each node where you will be building and running your applications. 
This package installs all of the MapR Libraries needed for application development regardless of programming language or 
type of MapR-DB table (binary or JSON).

Follow the instructions of MapR Documentation:

* [Installing MapR Client](https://maprdocs.mapr.com/home/AdvancedInstallation/SettingUptheClient-install-mapr-client.html)


---

Next : [Import the dataset](004-import-the-data-set.md)