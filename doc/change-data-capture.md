# Change Data Capture

The Change Data Capture (CDC) system allows you to capture changes made to data records in MapR-DB tables 
(JSON or binary). These data changes are the result of inserts, updates, and deletions and are called change data 
records. Once the change data records are propagated to a topic, a MapR-ES/Kafka consumer application is used to read 
and process them.

## CDC prerequisites

In order to use CDC you have to ensure that the following prerequisites are met:
1. Enterprise license is installed
You must have Enterprise license with Database module(for tables), Streams module(for streams) and Changelog module(for 
changelogs) enabled. You can add a license using CLI:
* Obtain a valid license file from MapR.
* Copy the license file to a cluster node.
* Run the following command to add the license:
```
$ maprcli license add [ -cluster <name> ] -license <filename> -is_file true
```

2. MapR Gateway is configured

You must have configured MapR Gateway on your cluster. Otherwise, you will face the `ErrAllGatewaysUnreachable` error, 
while executing `maprcli table changelog list` command:
```
$ maprcli table changelog list -path /demo_table -json
{
	"timestamp":1505747325953,
	"timeofday":"2017-09-18 03:08:45.953 GMT+0000",
	"status":"OK",
	"total":1,
	"data":[
		{
			"cluster":"my.cluster.id",
			"changelog":"/demo_changelog:demo_table",
			"realTablePath":"/demo_changelog",
			"replicaState":"REPLICA_STATE_CREATE_SCHEDULE",
			"paused":false,
			"throttle":false,
			"idx":1,
			"networkencryption":false,
			"synchronous":false,
			"networkcompression":"lz4",
			"isUptodate":true,
			"minPendingTS":0,
			"maxPendingTS":0,
			"bytesPending":0,
			"bucketsPending":0,
			"copyTableCompletionPercentage":0,
			"errors":{
				"Code":"ErrAllGatewaysUnreachable",
				"Host": node IP address,
				"Msg":"All configured gateways to cluster my.cluster.id are unreachable"
			}
		}
	]
}
```

In order to configure MapR Gateway follow the next steps:
* Install the `mapr-gateway` package on a node:
```
sudo yum install mapr-gateway
```
* Run configure.sh using the following options:
```
/opt/mapr/server/configure.sh 
    -C <source cluster cldb list> 
    -Z <source cluster zk list> 
    -u <user> 
    -g <group> 
    -N <source cluster name>
```

* Specify the cluster gateway IP addresses with the maprcli cluster gateway set command in the following manner to let 
the source cluster know about the gateway running on this node:
```
maprcli cluster gateway set 
-dstcluster < source cluster name > 
-gateways < IP addresses for source cluster gateways >
```

* Check newly created gateway:
```
$ maprcli cluster gateway list
cluster             gatewayConfig  
my.cluster.id       < IP addresses for source cluster gateways >
```

## MapR Music CDC setup

1. Create Changelog Stream:
```
$ maprcli stream create -path /mapr_music_artists_changelog -ischangelog true -consumeperm p
```

Where:
* `-ischangelog` set to `true` to configure the stream to store change log
* `-consumeperm p` set the changelog consumer presentation to "public" allowing any application to subscribe to the events.

2. Add Changelog to the `artists` table:
```
$ maprcli table changelog add -path /apps/artists -changelog /mapr_music_artists_changelog:artists
```

3. Define MapR Music Managed Thread Factory within Wildfly
MapR Music Application consumes change data records in separate thread. Thus, we have to define Managed Thread Factory 
within Wildfly:
* Modify `$WILDFLY_HOME/standalone/configuration/standalone.xml` configuration file. Add the following code snippet as 
child of the `<managed-thread-factories>` element:
```
<managed-thread-factory name="maprMusicThreadFactory" jndi-name="java:jboss/ee/concurrency/factory/MaprMusicThreadFactory" context-service="default" priority="1"/>
```
* Restart Wildfly
* Now you can access Managed Thread Factory:
```
  @Resource(lookup = "java:jboss/ee/concurrency/factory/MaprMusicThreadFactory")
  private ManagedThreadFactory threadFactory;
  
  ...
  Thread thread = threadFactory.newThread(() -> {
             // Your runnable
          });
  thread.start();
  ...
  
```

Note: the example above expects that `$WILDFLY_HOME` environment variable is set correctly and points to the Wildfly 
directory (For instance: `~/wildfly-10.1.0.Final/`).
 
## Consuming change data records

1. Declare Kafka Consumer
```
    // Consumer configuration
    Properties consumerProperties = new Properties();
    consumerProperties.setProperty("enable.auto.commit", "true");
    consumerProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    consumerProperties.setProperty("value.deserializer", "com.mapr.db.cdc.ChangeDataRecordDeserializer");
```

Where:
* `key.deserializer` an array of bytes created by the CDC gateway
* `value.deserializer` the value deserializer, MapR CDC uses a optimized serialization format for all the events, 
so you must specify the `com.mapr.db.cdc.ChangeDataRecordDeserializer` deserializer.

2. Create the consumer and subscribe to the Changelog

```
    // Consumer used to consume MapR-DB CDC events
    KafkaConsumer<byte[], ChangeDataRecord> consumer = new KafkaConsumer<byte[], ChangeDataRecord>(consumerProperties);
    consumer.subscribe(Collections.singletonList("/mapr_music_artists_changelog:artists"));
```

3. Consume the events

You can now listen to the event and process each ChangeDataRecord.
```
  while (true) {
    ConsumerRecords<byte[], ChangeDataRecord> changeRecords = consumer.poll(KAFKA_CONSUMER_POLL_TIMEOUT);
      for (ConsumerRecord<byte[], ChangeDataRecord> consumerRecord : changeRecords) {
        
        // The ChangeDataRecord contains all the changes made to a document
        ChangeDataRecord changeDataRecord = consumerRecord.value();
        
        // Get identifier of changed document
        String documentId = changeDataRecord.getId().getString();
       
        // process events
        ...
        ...
    }
  }
```

4. Process Change Data Records

You can obtain the type of event (insert, update, delete), using the `changeDataRecord.getType()` method.  You can use 
the `ChangeDataRecordType` enum to check the type.

* Processing Deletes

Processing a delete is a simple operation since the operation is based a single change data record, so you can directly 
get the document id using `changeDataRecord.getId()` and then process the document deletion.

* Processing Inserts and Updates

Document mutations are stored into a list of ChangeNodes, that you can retrieve int the following way:
```
    // Use the ChangeNode Iterator to capture all the individual changes
    Iterator<KeyValue<FieldPath, ChangeNode>> cdrItr = changeDataRecord.iterator();
    while (cdrItr.hasNext()) {
      Map.Entry<FieldPath, ChangeNode> changeNodeEntry = cdrItr.next();
      String fieldPathAsString = changeNodeEntry.getKey().asPathString();
      ChangeNode changeNode = changeNodeEntry.getValue();
      ...
      ...
    }
```

Note: `fieldPathAsString` in the example above indicates path of changed field. Empty string or `null` value of 
`fieldPathAsString` indicates that document was inserted, otherwise - updated. Thus, to process the document update you 
have to check the field path using following code snippet: 
```
  if("field_name".equalsIgnoreCase(fieldPathAsString)) {
    // get the value using the proper method, depending of the expected type of the value
    // For exmaple: changeNode.getString() or changeNode.getMap()
  }

```
