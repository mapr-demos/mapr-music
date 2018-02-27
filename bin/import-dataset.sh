#!/bin/bash

#######################################################################
# Globals definition
#######################################################################

NUMBER_REGEX='^[0-9]+$'
DEFAULT_DATASET_ARCHIVE_NAME='dataset-full-ratings.tar.gz'
DEFAULT_BATCH_SIZE=20000
DEFAULT_TEMP_DIR_NAME='tmp'

# Names of dataset directories
ALBUMS_DIRECTORY='albums'
ARTISTS_DIRECTORY='artists'
LANGUAGES_DIRECTORY='languages'
RATINGS_ALBUMS_DIRECTORY='ratings-albums'
RATINGS_ARTISTS_DIRECTORY='ratings-artists'
USERS_DIRECTORY='users'

# MapR-DB JSON Tables
ALBUMS_TABLE='/apps/albums'
ARTISTS_TABLE='/apps/artists'
LANGUAGES_TABLE='/apps/languages'
RATINGS_ALBUMS_TABLE='/apps/albums_ratings'
RATINGS_ARTISTS_TABLE='/apps/artists_ratings'
USERS_TABLE='/apps/users'

# MapR-FS directories
ALBUMS_MFS_DIRECTORY='/tmp/albums'
ARTISTS_MFS_DIRECTORY='/tmp/artists'
LANGUAGES_MFS_DIRECTORY='/tmp/languages'
RATINGS_ALBUMS_MFS_DIRECTORY='/tmp/albums_ratings'
RATINGS_ARTISTS_MFS_DIRECTORY='/tmp/artists_ratings'
USERS_MFS_DIRECTORY='/tmp/users'


#######################################################################
# Functions definition
#######################################################################

function print_usage() {
    cat <<EOM
Usage: $(basename $0) [-p|--path] [-b|--batch] [-r|--recreate] [-h|--help]
Options:
    --path      Specifies path to the dataset archive directory. Default value is current directory.
                Assumes dataset archive name is 'dataset.tar.gz'.

    --batch     Specifies batch size of imported documents, allows to import large dataset
                and prevent problems related to lack of memory. Default value is '20000'.

    --recreate  When specified, MapR-DB JSON Tables for dataset will be recreated.
                Note, that in case of tables recreation all added changelogs will be deleted
                and must be readded manually after script completion.

    --help      Prints usage information.
EOM
}

function create_table() {

    TABLE_NAME=$1
    RECREATE=$2

    EXISTS=0
    maprcli table info -path $TABLE_NAME > /dev/null
    EXISTS=$?

    if [ "$EXISTS" -eq 0 ] && [ "$RECREATE" -eq 0 ]; then
        echo "error: Table '$TABLE_NAME' already exists. Exiting. "
        echo "Please, specify '[-r|--recreate]' if you want to recreate it."
        cleanup
        exit 1
    fi

    maprcli table delete -path $TABLE_NAME > /dev/null
    maprcli table create -path $TABLE_NAME -tabletype json
    OUT=$?
    if [ $OUT -eq 0 ];then
        echo "Table '$TABLE_NAME' successfully created!"
    else
        echo "error: Errors occured while creating '$USERNAME' table. Exiting."
        cleanup
        exit 1
    fi
}

function import_documents() {

    local DIRECTORY=$1
    local TABLE_NAME=$2
    local MFS_DIRECTORY=$3
    local BATCH_SIZE=$4
    local TEMP_DIR=$5

    # Check if specified directory exists
    if [ ! -d ${DIRECTORY} ]; then
        echo "error: Dataset directory '$DIRECTORY' does not exist. Exiting."
        cleanup
        exit 1
    fi

    # Delete MFS direcory
    { hadoop fs -rm -r $MFS_DIRECTORY; } 2> /dev/null

    # Check number of documents to prevent redundant copying
    DOCUMENTS_NUM=$(ls -l $DIRECTORY | grep ^- | wc -l)
    if [ "$DOCUMENTS_NUM" -lt "$BATCH_SIZE" ];then
        load_into_mapr $DIRECTORY $TABLE_NAME $MFS_DIRECTORY
    else

        BATCHES=$(expr $DOCUMENTS_NUM / $BATCH_SIZE + 1)

        echo "Number of documents('$DOCUMENTS_NUM') is greater than batch size('$BATCH_SIZE')."
        echo "Documents will be imported batch by batch. Total number of batches: '$BATCHES'"

        for (( b=0; b<$BATCHES; b++ ))
        do
            echo "Processing batch '$b' ..."

            # Clean temp directory
            { rm ${TEMP_DIR}/*; } 2> /dev/null

            # Copy files to temp directory
            OFFSET=$(expr $b \* $BATCH_SIZE + 1)
            find ${DIRECTORY} -maxdepth 1 -type f | tail -n "+$OFFSET" | head -n "$BATCH_SIZE" |xargs cp -t "$TEMP_DIR"

            # Load into MapR-DB
            load_into_mapr $TEMP_DIR $TABLE_NAME $MFS_DIRECTORY
        done
    fi
}

function load_into_mapr() {

    local LOAD_DIRECTORY=$1
    local LOAD_TABLE_NAME=$2
    local LOAD_MFS_DIRECTORY=$3

    local LOAD_DOCUMENTS_NUM=$(ls -l $LOAD_DIRECTORY | grep ^- | wc -l)
    echo "Importing '$LOAD_DOCUMENTS_NUM' documents into '$LOAD_TABLE_NAME' MapR-DB JSON Table."

    # Loading into MapR-FS
    hadoop fs -copyFromLocal $LOAD_DIRECTORY $LOAD_MFS_DIRECTORY

    # Import into MapR-DB JSON Table using importJSON utility
    mapr importJSON -idField _id -src ${LOAD_MFS_DIRECTORY}/* -dst $LOAD_TABLE_NAME -mapreduce false

    # Delete MFS direcory
    { hadoop fs -rm -r $LOAD_MFS_DIRECTORY; } 2> /dev/null
}

function change_table_permissions() {
    local TABLE_NAME=$1
    maprcli table cf edit -path $TABLE_NAME -cfname default -readperm p -writeperm p -traverseperm  p
}

function create_temp_directory() {

    # Create temporary directory to import documents batch by batch
    mkdir $TEMP_DIRECTORY
    OUT=$?
    if [ $OUT -eq 1 ]; then
        echo "error: Cannot create temporary directory '$TEMP_DIRECTORY'. Exiting."
        cleanup
        exit 1
    fi
}

function cleanup() {
    { rm -Rf $ARCHIVE_DIRECTORY_PATH/$TEMP_DIRECTORY \
    $ARCHIVE_DIRECTORY_PATH/$ALBUMS_DIRECTORY \
    $ARCHIVE_DIRECTORY_PATH/$ARTISTS_DIRECTORY \
    $ARCHIVE_DIRECTORY_PATH/$LANGUAGES_DIRECTORY \
    $ARCHIVE_DIRECTORY_PATH/$RATINGS_ALBUMS_DIRECTORY \
    $ARCHIVE_DIRECTORY_PATH/$RATINGS_ARTISTS_DIRECTORY \
    $ARCHIVE_DIRECTORY_PATH/$USERS_DIRECTORY ; } 2> /dev/null
}

#######################################################################
# Parse options
#######################################################################

OPTS=`getopt -o hrb:p: --long help,recreate,batch:,path: -n 'add-wildfly-users.sh' -- "$@"`
eval set -- "$OPTS"

ARCHIVE_DIRECTORY_PATH='.'
BATCH=${DEFAULT_BATCH_SIZE}
RECREATE_TABLES=0
while true ; do
    case "$1" in
        -b|--batch)
            case "$2" in
                "") shift 2 ;;
                *) BATCH=$2 ; if ! [[ $BATCH =~ $NUMBER_REGEX ]] ; then echo "error: Batch size is not valid" >&2; exit 1; fi; shift 2 ;;
            esac ;;
        -r|--recreate) RECREATE_TABLES=1 ; shift ;;
        -h|--help) print_usage ; exit 0 ;;
        -p|--path)
            case "$2" in
                "") shift 2 ;;
                *) ARCHIVE_DIRECTORY_PATH=$2 ; shift 2 ;;
            esac ;;
        --) shift ; break ;;

        *) break ;;
    esac
done

ARCHIVE_PATH=${ARCHIVE_DIRECTORY_PATH}/${DEFAULT_DATASET_ARCHIVE_NAME}
TEMP_DIRECTORY=${ARCHIVE_DIRECTORY_PATH}/${DEFAULT_TEMP_DIR_NAME}

# Check if dataset exists
if [ ! -f ${ARCHIVE_PATH} ]; then
    echo "error: Cannot find dataset archive '$ARCHIVE_PATH'"
    cleanup
    exit 1
fi

#######################################################################
# Create MapR-DB tables
#######################################################################
create_table $ALBUMS_TABLE $RECREATE_TABLES
create_table $ARTISTS_TABLE $RECREATE_TABLES
create_table $LANGUAGES_TABLE $RECREATE_TABLES
create_table $RATINGS_ALBUMS_TABLE $RECREATE_TABLES
create_table $RATINGS_ARTISTS_TABLE $RECREATE_TABLES
create_table $USERS_TABLE $RECREATE_TABLES

#######################################################################
# Extracting dataset archive
#######################################################################
echo "Extracting dataset archive to '$ARCHIVE_DIRECTORY_PATH'"
tar -zxf $ARCHIVE_PATH --directory $ARCHIVE_DIRECTORY_PATH
echo "Archive is extracted"

#######################################################################
# Import documents into MapR-DB JSON Tables using importJSON utility
#######################################################################
create_temp_directory
import_documents $ARCHIVE_DIRECTORY_PATH/$ALBUMS_DIRECTORY $ALBUMS_TABLE $ALBUMS_MFS_DIRECTORY $BATCH $TEMP_DIRECTORY
import_documents $ARCHIVE_DIRECTORY_PATH/$ARTISTS_DIRECTORY $ARTISTS_TABLE $ARTISTS_MFS_DIRECTORY $BATCH $TEMP_DIRECTORY
import_documents $ARCHIVE_DIRECTORY_PATH/$LANGUAGES_DIRECTORY $LANGUAGES_TABLE $LANGUAGES_MFS_DIRECTORY $BATCH $TEMP_DIRECTORY
import_documents $ARCHIVE_DIRECTORY_PATH/$RATINGS_ALBUMS_DIRECTORY $RATINGS_ALBUMS_TABLE $RATINGS_ALBUMS_MFS_DIRECTORY $BATCH $TEMP_DIRECTORY
import_documents $ARCHIVE_DIRECTORY_PATH/$RATINGS_ARTISTS_DIRECTORY $RATINGS_ARTISTS_TABLE $RATINGS_ARTISTS_MFS_DIRECTORY $BATCH $TEMP_DIRECTORY
import_documents $ARCHIVE_DIRECTORY_PATH/$USERS_DIRECTORY $USERS_TABLE $USERS_MFS_DIRECTORY $BATCH $TEMP_DIRECTORY

#######################################################################
# Change MapR-DB Tables permissions
#######################################################################
change_table_permissions $ALBUMS_TABLE
change_table_permissions $ARTISTS_TABLE
change_table_permissions $LANGUAGES_TABLE
change_table_permissions $RATINGS_ALBUMS_TABLE
change_table_permissions $RATINGS_ARTISTS_TABLE
change_table_permissions $USERS_TABLE

cleanup

exit 0
