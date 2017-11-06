#!/bin/bash

#######################################################################
# Globals definition
#######################################################################

NUMBER_REGEX='^[0-9]+$'
DEFAULT_USER_PASSWORD='music'

REST_SERVICE_HOST='localhost'
REST_SERVICE_PORT=8080
ALBUMS_ENDPOINT='mapr-music-rest/api/1.0/albums'

#######################################################################
# Functions definition
#######################################################################

function print_usage() {
    cat <<EOM
Usage: $(basename $0) [-p|--path] [-s|--size] [-h|--help]
Options:
    --path      Specifies path to the 'users' dataset directory. Default value is current directory.
    --size      Specifies size of Users Item Rating Matrix. Default value is '10'. Max value is '50'.
    --help      Prints usage information.
EOM
}

#######################################################################
# Parse options
#######################################################################

OPTS=`getopt -o hs:p: --long help,size:,path: -n 'user-rating-influence.sh' -- "$@"`
eval set -- "$OPTS"

USERS_PATH='.'
SIZE=10
while true ; do
    case "$1" in
        -s|--size)
            case "$2" in
                "") SIZE=10 ; shift 2 ;;
                *) SIZE=$2 ; if ! [[ $SIZE =~ $NUMBER_REGEX ]] ; then echo "error: Size is not valid" >&2; exit 1; fi; shift 2 ;;
            esac ;;
        -h|--help) print_usage ; exit 0 ;;
        -p|--path)
            case "$2" in
                "") shift 2 ;;
                *) USERS_PATH=$2 ; shift 2 ;;
            esac ;;
        --) shift ; break ;;

        *) break ;;
    esac
done

USERNAMES_ARRAY=()
ALBUM_IDS_ARRAY=()

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

#######################################################################
# Rate
#######################################################################
usernamesLength=${#USERNAMES_ARRAY[@]}
albumsLength=${#ALBUM_IDS_ARRAY[@]}
if [ "$usernamesLength" -ne "$albumsLength" ] ; then
    echo "ERROR: Number of Albums ids ('$albumsLength') does not match number of usersnames ('$usernamesLength')."
    echo "Check if REST Service is running and '--path' option points to valid 'users' dataset." 
    exit 1
fi

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

RED='\033[0;31m'
GREEN='\033[0;32m'
NO_COLOR='\033[0m' # No Color
echo -e "${GREEN}After model retraining the next album is expected to be recommended for user '${RED}$expected_user${GREEN}':${NO_COLOR}"
echo ${EXPECTED_ALBUM} | jq -r

exit 0
