# Users ratings influence

To check how users ratings influence on the recommendations, produced by [Recommendation engine](todo) we will create 
simple script, which initializes ratings data for Recommendation engine. After that we will train Recommendation engine 
on generated data and compare the expected result with actual one.

### Prerequisites
Script assumes that there is no rating data, so:
* `/apps/albums_ratings` and `/apps/artists_ratings` tables must be empty
* MapR Music App REST service is running
* You have access to the `users` dataset directory

### Script purpose

As described in [Creating recommendation engine](todo) document, recommendation engine produces recommendations 
according to the specified ratings data. It uses Collaborative filtering approach, which is based on similarity; 
the basic idea is people who liked similar items in the past will like similar items in the future. In the example below, 
Ted likes albums A, B, and C. Carol also likes albums A, B and C. Bob likes albums A and B. To recommend an album to 
Bob, we calculate that users who liked B also liked C, so C is a possible recommendation for Bob.

Users Item Rating Matrix:

| User/Album    | Album A | Album B | Album C |
| ------------- | ------- | ------- | ------- |
| Ted           | 5.0     | 5.0     | 5.0     |
| Carol         | 5.0     | 5.0     | 5.0     |
| Bob           | 5.0     | 5.0     | ???     |


So, the purpose of the script is to initialize ratings data in a similar way. We will pick several users and albums, 
rate these albums on behalf of these users and print album, which is expected to be recommended for the certain user.

### Script implementation

First of all, we have to obtain usernames to rate on behalf of users. In order to do so, we will parse User JSON 
documents from the dataset, which are contained at `users` directory:
```
USERNAMES_ARRAY=()

#######################################################################
# Get usernames
#######################################################################
FILES=${USERS_PATH}/*.json
for file in $FILES
do
    if [ -f "$file" ]; then
        tLen=${#USERNAMES_ARRAY[@]}
        if [ "$tLen" -lt "$SIZE" ] ; then
            USERNAME=$(jq -r '._id' $file)
            USERNAMES_ARRAY+=($USERNAME)
        else
            break;
        fi
    fi
done

```

After that, we will get albums to rate by doing HTTP call to MapR Music REST service:
```
ALBUM_IDS_ARRAY=()

#######################################################################
# Get Albums identifiers
#######################################################################
ALBUMS_RESPONSE=$(curl -s -X GET http://${REST_SERVICE_HOST}:${REST_SERVICE_PORT}/${ALBUMS_ENDPOINT}?per_page=${SIZE})
ALBUMS_IDS=$(echo ${ALBUMS_RESPONSE} | jq -r ".results[] | ._id")

# Save current IFS
SAVEIFS=$IFS
# Change IFS to new line. 
IFS=$'\n'
ALBUM_IDS_ARRAY=($ALBUMS_IDS)
# Restore IFS
IFS=$SAVEIFS
```

Now we can rate on behalf of users using MapR Music REST rating endpoint:
```
#######################################################################
# Rate
#######################################################################

expected_album_id=''
expected_user=''
user_index=0
for username in "${USERNAMES_ARRAY[@]}"
do
    user_index=$((user_index+1))
    album_index=0
    for albumId in "${ALBUM_IDS_ARRAY[@]}"
    do

        album_index=$((album_index+1))
        if [ "$user_index" -eq "$SIZE" ] && [ "$album_index" -eq "$SIZE" ]; then
            expected_album_id=$albumId
            expected_user=$username
            break;
        fi
        curl -s -u ${username}:${DEFAULT_USER_PASSWORD} -X PUT -H "Content-Type: application/json" -d '{"rating":5.0}' http://${REST_SERVICE_HOST}:${REST_SERVICE_PORT}/${ALBUMS_ENDPOINT}/${albumId}/rating > /dev/null
        # echo "User 'username':'$user_index' rates album '$albumId' "
    done

done


EXPECTED_ALBUM=$(curl -s -X GET http://${REST_SERVICE_HOST}:${REST_SERVICE_PORT}/${ALBUMS_ENDPOINT}/${expected_album_id})
```

[Here](https://github.com/mapr-demos/mapr-music/blob/feature/Recommendation_engine/user-rating-influence.sh) you can 
find the listing of the resulting script.

### Running the script

1. Specify location of the `users` dataset directory via `--path` option in order to run the script:
```
$ ./user-rating-influence.sh --path ~/mapr-music-dataset/users
```

2. After script completion you will be able to see output similar to:
```
$ ./user-rating-influence.sh --path ~/mapr-music-dataset/users
After model retraining the next album is expected to be recommended for user 'aleannon':
{
  "name": "A Definite Maybe",
  "barcode": "",
  "status": "Official",
  "packaging": "None",
  "language": "eng",
  "script": "28",
  "mbid": "06ce6c38-da57-4f28-81af-ca5c1c40d233",
  "_id": "06ce6c38-da57-4f28-81af-ca5c1c40d233",
  "slug": "a-definite-maybe-0",
  "artists": [
    {
      "name": "Indica",
      "_id": "a1fdaced-c497-4606-9f6e-941489e58263",
      "slug": "indica-0",
      "profile_image_url": "https://upload.wikimedia.org/wikipedia/commons/b/b7/Indica_paris.JPG",
      "rating": 2.9415584415584415
    }
  ],
  "tracks": [
    {
      "name": "A Definite Maybe",
      "length": 212787,
      "position": 1,
      "_id": "617e1f6b-59eb-4269-89c1-2f3b96e5e990"
    }
  ],
  "cover_image_url": "http://coverartarchive.org/release/06ce6c38-da57-4f28-81af-ca5c1c40d233/6299978915.jpg",
  "images_urls": [],
  "released_date": 1386288000000,
  "rating": 5
}
```

3. Retrain the Recommendation engine
```
$ cd core-application/processing/recommendation-engine
$ mvn clean install scala:run
```

4. Ensure that user really has expected album in recommendation list
```
$ export EXPECTED_ALBUM_NAME='A Definite Maybe'
$ export TEST_USERNAME='aleannon'
$ export DEFAULT_USER_PASSWORD='music'
$ curl -u ${TEST_USERNAME}:${DEFAULT_USER_PASSWORD} -X GET http://localhost:8080/mapr-music-rest/api/1.0/albums/00327e39-61f8-48b6-ab7d-0d374182ce1b/recommended?limit=10 | grep "$EXPECTED_ALBUM_NAME"
```

If user recommendation list contains expected album you will be able to see non-empty output in your shell.
