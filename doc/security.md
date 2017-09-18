# MapR Music Security

MapR Music application allows users to authorize themselves using Basic authorization. Current implementation of 
authentication/authorization uses Wildfly's application users and depends on Wildfly's 'ApplicationRealm'. 

## Creating new user
Since MapR Music application uses Wildfly's application users we can register new user using `add-user.sh` utility:
```
$ cd $JBOSS_HOME/bin
$ ./add-user.sh -a -u music -p music -g 'user,admin'
```

Note: the example above expects that `$JBOSS_HOME` environment variable is set correctly and points to the Wildfly 
directory (For instance: `/home/user1/wildfly-11.0.0.Beta1`).

Also, MapR Music provides endpoint to register new users:
```
$ curl -d '{"username":"music", "password": "music"}' -H "Content-Type: application/json" -X POST http://localhost:8080/mapr-music-rest/api/1.0/users
```
## Basic authorization
MapR Music application allows users to authorize themselves using Basic authorization. For example to get newly created 
user's information execute the following command:
```
$ curl -H "Authorization: Basic bXVzaWM6bXVzaWM=" -X GET http://localhost:8080/mapr-music-rest/api/1.0/users/current
```
