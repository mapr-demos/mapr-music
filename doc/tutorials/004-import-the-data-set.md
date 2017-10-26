# Import the Data Set

Repository contains sample [dataset](https://github.com/mapr-demos/mapr-music/tree/devel/dataset), which can be used by 
MapR Music Application. This document explains the Data Set structure and shows various ways to import it into MapR-DB.

## Data Set description

TODO

## How to import the Data Set

You can import dataset either manually or using 
[import-dataset.sh](https://github.com/mapr-demos/mapr-music/blob/devel/bin/import-dataset.sh) script.

### 'import-dataset.sh' script

[import-dataset.sh](https://github.com/mapr-demos/mapr-music/blob/devel/bin/import-dataset.sh) will extract dataset 
archive and import it to MapR-DB JSON Tables. Below you can find script usage information:
```
$ import-dataset.sh -h
Usage: import-dataset.sh [-p|--path] [-b|--batch] [-r|--recreate] [-h|--help]
Options:
    --path      Specifies path to the dataset archive. Default value is current directory. 
                Assumes dataset archive name is 'dataset.tar.gz'.

    --batch     Specifies batch size of imported documents, allows to import large dataset 
                and prevent problems related to lack of memory. Default value is '20000'.

    --recreate  When specified, MapR-DB JSON Tables for dataset will be recreated.

    --help      Prints usage information.
```

Here is example of script usage:
```
$ ./import-dataset.sh -r -b 10000 --path /path/to/dataset/directory/
```

Note, that script assumes that dataset archive has default name(`dataset.tar.gz`).

### Manual import

* Copy the dataset archive to one of the nodes of your cluster:

```
$ scp dataset.tar.gz youruser@nodehostname:/dataset/path/at/node
```

* Extract the archive:

```
$ tar -zxf dataset.tar.gz
```

* Load data into MapR-FS:
```
$ hadoop fs -copyFromLocal albums/ /tmp/albums
$ hadoop fs -copyFromLocal artists/ /tmp/artists
$ hadoop fs -copyFromLocal languages/ /tmp/languages
$ hadoop fs -copyFromLocal users/ /tmp/users
$ hadoop fs -copyFromLocal ratings-albums/ /tmp/albums_ratings
$ hadoop fs -copyFromLocal ratings-artists/ /tmp/artists_ratings
```

* Import data into MapR-DB JSON Tables using `importJSON` tool:
```
$ mapr importJSON -idField _id -src /tmp/albums/* -dst /apps/albums -mapreduce false
$ mapr importJSON -idField _id -src /tmp/artists/* -dst /apps/artists -mapreduce false
$ mapr importJSON -idField _id -src /tmp/languages/* -dst /apps/languages -mapreduce false
$ mapr importJSON -idField _id -src /tmp/users/* -dst /apps/users -mapreduce false
$ mapr importJSON -idField _id -src /tmp/albums_ratings/* -dst /apps/albums_ratings -mapreduce false
$ mapr importJSON -idField _id -src /tmp/artists_ratings/* -dst /apps/artists_ratings -mapreduce false
```

Note: in case of lack of memory while importing ratings documents try to split ratings into multiple sets and import 
them one by one.

After that dataset is ready to be used by MapR-Music application.
