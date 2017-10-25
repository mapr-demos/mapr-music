# How to import data

Repository contains sample [dataset](https://github.com/mapr-demos/mapr-music/tree/devel/dataset), which can be used by 
MapR Music Application. You can import dataset either manually or using 
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
Manual import described at [Generating Music dataset](https://github.com/mapr-demos/mapr-music/blob/devel/doc/music-dataset-generation.md#import-data-in-mapr-db)
documentation.
