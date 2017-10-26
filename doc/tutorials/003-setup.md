# Setup

## Install MapR

TODO

## Cluster configuration

#### MapR-DB Query Service configuration

The MapR Music Application application uses OJAI connector to access MapR-DB, which in it's turn uses MapR-DB Query Service powered 
by Drill. So you have to be sure that MapR-DB Query Service is enabled for you cluster and Drill is configured in a 
proper way. Execute the next command to ensure that Query Service is enabled:
`maprcli cluster queryservice getconfig -cluster < your-cluster-name >`

The following output indicates that Query Service is enabled:
```
$ maprcli cluster queryservice getconfig -cluster my.cluster.id
storageplugin  zookeeper      znode  clusterid           enabled  
dfs            zkhost:5181   drill  my.cluster.id       true   
```

To change Query Service configuration use `setconfig` command:

```
maprcli cluster queryservice setconfig 
              [ -cluster < cluster-name > ]
              -enabled < true | false >
              -clusterid < cluster-id of MapR Drill cluster >
              -storageplugin < Name of MapR Drill Storage plug-in >
              -znode < Root Zookeeper node used by MapR Drill cluster >
```

Example:
```
maprcli cluster queryservice setconfig \
   -enabled true \
   -clusterid my.cluster.id \
   -storageplugin dfs \
   -znode drill
```

Note, that in above command parameter `-clusterid`  refer to the value of the cluster-id parameter in the 
`drill-override.conf` file. Below you can see the minimal Drill configuration, which can be found at 
`/opt/mapr/drill/drill-<drill-version>/conf/drill-override.conf`:
```
drill.exec: {
	cluster-id: "my.cluster.id",
 	zk.connect: "zkhost:5181"
}
```

#### MapR CDC configuration

MapR Music Application uses CDC, so you have to ensure that the following prerequisites are met:
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

#### Create Tables

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

#### Create Changelog

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


## Installing and configuring MapR Client

The `mapr-client` package must be installed on each node where you will be building and running your applications. 
This package installs all of the MapR Libraries needed for application development regardless of programming language or 
type of MapR-DB table (binary or JSON).

Note: The package mapr-core contains the files that are in the mapr-client package. 
If you have installed the mapr-core package, you do not need to install the mapr-client package.

Complete the following steps to install the `mapr-client` package from a repository:
1. Configure the repository to point to http://package.mapr.com/releases/<release version>/<operating system> 

For example, if your VM has a CentOS operating system, edit the `/etc/yum.repos.d/mapr_core.repo` file and add the 
location.

2. Based on your operating system, run one of the following commands to install the package:
   * On Red Hat /Centos: `yum install mapr-client`
   * On Ubuntu: `apt-get install mapr-client`
   * On SUSE: `zypper install mapr-client`
   
3. Configure MapR Client by executing the following command:
 
 ```
 sudo /opt/mapr/server/configure.sh -N my.cluster.com -c -C cldbhost:7222 -Z zkhost:5181
 ```
    
Client applications connect to a cluster via CLDB nodes, which are listed in the connection request or in the 
`mapr-clusters.conf` file on the machine that submits the connection request. 
So ensure that your `/opt/mapr/conf/mapr-clusters.conf` contains following configuration:

`my.cluster.com secure=false cldbhost:7222`
Where:

* `my.cluster.id` is the name of your cluster
* `secure=false` specifies that the cluster secure mode is not enabled
* `cldbhost:7222` is the host and port of the CLDB.

