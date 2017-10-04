#!/bin/bash

#######################################################################
# Globals definition
#######################################################################

NUMBER_REGEX='^[0-9]+$'
DEFAULT_USER_PASSWORD='music'

#######################################################################
# Functions definition
#######################################################################

function print_usage() {
    cat <<EOM
Usage: $(basename $0) [-p|--path] [-l|--limit] [-h|--help]
Options:
    --path      Specifies path to the 'users' dataset directory. Default value is current directory.
    --limit     Specifies maximum number of users, which will be registered at Wildfly. Dafault value is '3'.
    --help      Prints usage information.
EOM
}

function create_wildfly_user () {

    USERNAME=$(jq -r '._id' $1)

    ${WILDFLY_HOME}/bin/add-user.sh -a -u ${USERNAME} -p ${DEFAULT_USER_PASSWORD} -g 'user,admin' > /dev/null

    OUT=$?
    if [ $OUT -eq 0 ];then
        echo "User '$USERNAME' with password '$DEFAULT_USER_PASSWORD' successfully registered at Wildfly!"
    else
        echo "Errors occured while registering user '$USERNAME' at Wildfly"
    fi
}

#######################################################################
# Parse options
#######################################################################

OPTS=`getopt -o hl:p: --long help,limit:,path: -n 'add-wildfly-users.sh' -- "$@"`
eval set -- "$OPTS"

USERS_PATH='.'
LIMIT=-1
while true ; do
    case "$1" in
        -l|--limit)
            case "$2" in
                "") LIMIT=3 ; shift 2 ;;
                *) LIMIT=$2 ; if ! [[ $LIMIT =~ $NUMBER_REGEX ]] ; then echo "error: Limit is not valid" >&2; exit 1; fi; shift 2 ;;
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


# Check if 'WILDFLY_HOME' environment varaible set
if [ ! -z ${WILDFLY_HOME+x} ]; then # WILDFLY_HOME exists
    echo "Found Wildfly at $WILDFLY_HOME"
else
    echo 'WILDFLY_HOME environment varaible is not set. Please set it and rerun the script.'
    exit 1
fi

USERS_CREATED=0
FILES=${USERS_PATH}/*.json
for file in $FILES
do
    if [ -f "$file" ]; then
        if [ "$LIMIT" -eq -1 ] || [ "$USERS_CREATED" -lt "$LIMIT" ]; then
            create_wildfly_user $file
            USERS_CREATED=$((USERS_CREATED + 1))
        fi
    fi
done

exit 0
