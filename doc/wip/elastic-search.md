MapR Cluster Configuaration:
* Create Albums/Artists changelogs(Descibed here: https://github.com/mapr-demos/mapr-music/blob/master/doc/change-data-capture.md)

Dev machine configuration:
1. Install and run the ElasticSearch
2. Go to 'mapr-music/core-application/webapp' and run 'mvn clean install -Dmaven.test.skip=true'
3. Go to 'mapr-music/core-application/streaming/elasticsearch-service/target' and run jar: 'java -jar elasticsearch-service-1.0-SNAPSHOT.jar'
(It will start service with listens Artists/Albums changelogs and publishes the changes to the ElasticSearch)

4. Deploy MapR Music app
5. Change one of the album's name to be for exampple 'TEST ALBUM'
5. Change one of the artists's name to be for exampple 'TeST Artist'
(You will be able to see that changes are sent to the ES at 'elasticsearch-service' logs)

6. Perform GET /mapr-music-rest/api/1.0/search/name?entry=test

You will be able to see something, similar to: 

```
{
    "total": 2,
    "albums": [
        {
            "name": "TEST ALBUM",
            "_id": "a92fec84-2aec-4a07-b065-4e6e56999e15"
        }
    ],
    "artists": [
        {
            "name": "TeST Artist",
            "_id": "0a6f7d33-e760-4bbf-9173-ca749fee77c5"
        }
    ]
}
```
