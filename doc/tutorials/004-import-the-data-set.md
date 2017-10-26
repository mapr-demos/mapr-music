# Import the Data Set

Repository contains sample [dataset](https://github.com/mapr-demos/mapr-music/tree/devel/dataset), which can be used by 
MapR Music Application. This document explains the Data Set structure and shows various ways to import it into MapR-DB.

## Data Set description

Sample dataset archive [dataset.tar.gz](https://github.com/mapr-demos/mapr-music/tree/devel/dataset) contains JSON 
documents, placed in 6 directories:
1. `albums`

Contains `6117` Album JSON documents, which are ready to be imported into MapR-DB JSON Table.
<details> 
  <summary>Example of such Album document</summary>
  
  ```
  {
     "name": "Runnin' Wild",
     "barcode": "016861796327",
     "status": "Official",
     "packaging": "",
     "language": "eng",
     "script": "28",
     "mbid": "0a1aa2f3-b3bf-4527-b235-1027888e6f7f",
     "_id": "0a1aa2f3-b3bf-4527-b235-1027888e6f7f",
     "slug_name": "runnin-wild",
     "slug_postfix": {
        "$numberLong": 2
     },
     "MBID": "0a1aa2f3-b3bf-4527-b235-1027888e6f7f",
     "artists": [
        {
           "rating": 2.7837837,
           "profile_image_url": "https://upload.wikimedia.org/wikipedia/commons/a/a0/AirbourneAtTampere.jpg",
           "slug": "airbourne-0",
           "name": "Airbourne",
           "id": "5365f55c-b1e1-48f9-b09f-078f7a14cb1f"
        }
     ],
     "tracks": [
        {
           "id": "32a9ee2d-7a3f-3b36-b554-168a86e5c734",
           "name": "Stand Up for Rock 'n' Roll",
           "length": {
              "$numberLong": 241200
           },
           "position": {
              "$numberLong": 1
           },
           "mbid": "32a9ee2d-7a3f-3b36-b554-168a86e5c734",
           "MBID": "32a9ee2d-7a3f-3b36-b554-168a86e5c734"
        },
        {
           "id": "dd5afba2-782c-3b41-8e53-d119529b5884",
           "name": "Too Much, Too Young, Too Fast",
           "length": {
              "$numberLong": 222720
           },
           "position": {
              "$numberLong": 3
           },
           "mbid": "dd5afba2-782c-3b41-8e53-d119529b5884",
           "MBID": "dd5afba2-782c-3b41-8e53-d119529b5884"
        },
        {
           "id": "84d47443-29d5-3aa4-82e9-41b633f7c778",
           "name": "Fat City",
           "length": {
              "$numberLong": 206760
           },
           "position": {
              "$numberLong": 5
           },
           "mbid": "84d47443-29d5-3aa4-82e9-41b633f7c778",
           "MBID": "84d47443-29d5-3aa4-82e9-41b633f7c778"
        },
        {
           "id": "287b244f-7d41-3d67-b5aa-8e0513949fee",
           "name": "Cheap Wine & Cheaper Women",
           "length": {
              "$numberLong": 190160
           },
           "position": {
              "$numberLong": 9
           },
           "mbid": "287b244f-7d41-3d67-b5aa-8e0513949fee",
           "MBID": "287b244f-7d41-3d67-b5aa-8e0513949fee"
        },
        {
           "id": "233eedc5-8737-3fea-abbf-4fb6c18e354f",
           "name": "Hellfire",
           "length": {
              "$numberLong": 145853
           },
           "position": {
              "$numberLong": 11
           },
           "mbid": "233eedc5-8737-3fea-abbf-4fb6c18e354f",
           "MBID": "233eedc5-8737-3fea-abbf-4fb6c18e354f"
        },
        {
           "id": "06a5f4c4-ff01-37d2-8da1-f886564e3512",
           "name": "Blackjack",
           "length": {
              "$numberLong": 162066
           },
           "position": {
              "$numberLong": 6
           },
           "mbid": "06a5f4c4-ff01-37d2-8da1-f886564e3512",
           "MBID": "06a5f4c4-ff01-37d2-8da1-f886564e3512"
        },
        {
           "id": "dac52e35-9237-37c2-a16e-d93cdac49902",
           "name": "What's Eatin' You",
           "length": {
              "$numberLong": 216426
           },
           "position": {
              "$numberLong": 7
           },
           "mbid": "dac52e35-9237-37c2-a16e-d93cdac49902",
           "MBID": "dac52e35-9237-37c2-a16e-d93cdac49902"
        },
        {
           "id": "d4213364-1fb5-3483-b5f2-13bbc350039d",
           "name": "Girls in Black",
           "length": {
              "$numberLong": 195986
           },
           "position": {
              "$numberLong": 8
           },
           "mbid": "d4213364-1fb5-3483-b5f2-13bbc350039d",
           "MBID": "d4213364-1fb5-3483-b5f2-13bbc350039d"
        },
        {
           "id": "3dc69111-7112-3dc7-b6c0-85a30919fb17",
           "name": "Heartbreaker",
           "length": {
              "$numberLong": 236160
           },
           "position": {
              "$numberLong": 10
           },
           "mbid": "3dc69111-7112-3dc7-b6c0-85a30919fb17",
           "MBID": "3dc69111-7112-3dc7-b6c0-85a30919fb17"
        },
        {
           "id": "4dd38c94-8013-3e9f-913b-d71286826bb0",
           "name": "Runnin' Wild",
           "length": {
              "$numberLong": 218040
           },
           "position": {
              "$numberLong": 2
           },
           "mbid": "4dd38c94-8013-3e9f-913b-d71286826bb0",
           "MBID": "4dd38c94-8013-3e9f-913b-d71286826bb0"
        },
        {
           "id": "c0ffeea5-ffc6-316f-819c-13958450e9be",
           "name": "Diamond in the Rough",
           "length": {
              "$numberLong": 173960
           },
           "position": {
              "$numberLong": 4
           },
           "mbid": "c0ffeea5-ffc6-316f-819c-13958450e9be",
           "MBID": "c0ffeea5-ffc6-316f-819c-13958450e9be"
        }
     ],
     "cover_image_url": "http://coverartarchive.org/release/0a1aa2f3-b3bf-4527-b235-1027888e6f7f/929069980.jpg",
     "images_urls": [
        "http://coverartarchive.org/release/0a1aa2f3-b3bf-4527-b235-1027888e6f7f/929075399.jpg",
        "http://coverartarchive.org/release/0a1aa2f3-b3bf-4527-b235-1027888e6f7f/929079251.jpg"
     ],
     "released_date": {
        "$dateDay": "2007-6-25"
     },
     "rating": 2.631579
  }
  ```
  
</details>

2. `artists`

Contains `10281` Artist JSON documents, which are ready to be imported into MapR-DB JSON Table. 
<details> 
  <summary>Example of such Artist document</summary>
  
  ```
  {
     "name": "David Cook",
     "gender": "Male",
     "area": "United States",
     "deleted": false,
     "mbid": "966e1095-b172-415c-bae5-53f8041fd050",
     "_id": "966e1095-b172-415c-bae5-53f8041fd050",
     "slug_name": "david-cook",
     "slug_postfix": {
        "$numberLong": 0
     },
     "MBID": "966e1095-b172-415c-bae5-53f8041fd050",
     "disambiguation_comment": "American Idol",
     "albums": [
        {
           "cover_image_url": "http://coverartarchive.org/release/78d08954-e79f-4a80-929d-71cc0ecc7b9d/6964754870.jpg",
           "slug": "analog-heart-0",
           "name": "Analog Heart",
           "id": "78d08954-e79f-4a80-929d-71cc0ecc7b9d"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/1fdff2a1-1bdf-499a-a50c-e5d742958094/10875910782.jpg",
           "slug": "david-cook-1",
           "name": "David Cook",
           "id": "1fdff2a1-1bdf-499a-a50c-e5d742958094"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/d4cccd1c-61fb-4939-aa53-49798314724e/2144368240.jpg",
           "slug": "david-cook-2",
           "name": "David Cook",
           "id": "d4cccd1c-61fb-4939-aa53-49798314724e"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/2fb7fb0d-8764-4c7d-9bf4-d314914cd7a0/8621569448.jpg",
           "slug": "this-loud-morning-1",
           "name": "This Loud Morning",
           "id": "2fb7fb0d-8764-4c7d-9bf4-d314914cd7a0"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/40184dbe-40fa-4845-b7e6-ca20242853eb/7976913345.jpg",
           "slug": "the-last-goodbye-0",
           "name": "The Last Goodbye",
           "id": "40184dbe-40fa-4845-b7e6-ca20242853eb"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/e21facb9-ecf7-407e-990a-ff465ace43a1/9322135862.jpg",
           "slug": "this-loud-morning-2",
           "name": "This Loud Morning",
           "id": "e21facb9-ecf7-407e-990a-ff465ace43a1"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/528568d0-ce68-42b8-b122-f57a57763637/2466862952.jpg",
           "slug": "this-loud-morning-3",
           "name": "This Loud Morning",
           "id": "528568d0-ce68-42b8-b122-f57a57763637"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/4bb7977f-c67a-4bf6-ab26-994d59a06717/12602733617.jpg",
           "slug": "this-quiet-night-0",
           "name": "This Quiet Night",
           "id": "4bb7977f-c67a-4bf6-ab26-994d59a06717"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/924fab61-e21c-4065-a711-f2f55fe2e6d9/1452573615.jpg",
           "slug": "always-be-my-baby-0",
           "name": "Always Be My Baby",
           "id": "924fab61-e21c-4065-a711-f2f55fe2e6d9"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/0014a89f-978c-401f-b3cb-86d14d41ea0d/12905818584.jpg",
           "slug": "digital-vein-0",
           "name": "Digital Vein",
           "id": "0014a89f-978c-401f-b3cb-86d14d41ea0d"
        },
        {
           "cover_image_url": "http://coverartarchive.org/release/1cd1b0f8-a049-484f-a2a2-73bf7bbb8295/17079514876.jpg",
           "slug": "gimme-heartbreak-0",
           "name": "Gimme Heartbreak",
           "id": "1cd1b0f8-a049-484f-a2a2-73bf7bbb8295"
        }
     ],
     "profile_image_url": "https://upload.wikimedia.org/wikipedia/commons/a/a0/David_Cook_Toads_cropped.jpg",
     "images_urls": [],
     "begin_date": {
        "$numberLong": 409190400000
     },
     "rating": 2.919355
  }
  ```
  
</details>

3. `languages`

Contains `56` Language JSON documents, which are ready to be imported into MapR-DB JSON Table. 
<details> 
  <summary>Example of such Language document</summary>
  
  ```
  {
     "name": "English",
     "_id": "eng"
  }
  ```
  
</details>

4. `ratings-albums`

Contains `175413` Album Rating JSON documents, which are ready to be imported into MapR-DB JSON Table. 
<details> 
  <summary>Example of such Album Rating document</summary>
  
  ```
  {
     "_id": "0000d95f-e2db-400b-b1a4-53f4461f3c68",
     "user_id": "aschimmel",
     "document_id": "90f0350d-d10a-429c-83dc-a6b674771f01",
     "rating": 1
  }
  ```
  
</details>

5. `rating-artists`

Contains `316065` Artist Rating JSON documents, which are ready to be imported into MapR-DB JSON Table. 
<details> 
  <summary>Example of such Artist Rating document</summary>
  
  ```
  {
     "_id": "0000a7c3-f2bc-433a-90c2-9b2d5f6fe9c2",
     "user_id": "jgislason",
     "document_id": "75cef459-189d-4a9a-bc0d-ecab4880a162",
     "rating": 3
  }
  ```
  
</details>

6. `users`

Contains `300` User JSON documents, which are ready to be imported into MapR-DB JSON Table. 
<details> 
  <summary>Example of such User document</summary>
  
  ```
  {
     "_id": "jdoe",
     "first_name": "John",
     "last_name": "Doe"
  }
  ```
  
</details>

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
