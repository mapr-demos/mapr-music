#!/bin/bash

#######################################################################
# Globals definition
#######################################################################
WORK_DIR=/usr/share/mapr-apps/mapr-music

# Check if 'DRILL_NODE' environment varaible set
if [ ! -z ${DRILL_NODE+x} ]; then # DRILL_NODE exists
    echo "Drill node: $DRILL_NODE"
else
    echo 'DRILL_NODE environment varaible is not set. Please set it and rerun.'
    exit 1
fi

# Change permissions
sudo chown -R $MAPR_CONTAINER_USER:$MAPR_CONTAINER_GROUP $WORK_DIR

# Start ElasticSearch
${WORK_DIR}/elasticsearch-5.6.3/bin/elasticsearch &

# Change Wildfly standalone.xml to configure Drill DataSource according to the specified Drill node
sed -i -e "s/yournodename/$DRILL_NODE/g" ${WORK_DIR}/wildfly-11.0.0.Beta1/standalone/configuration/standalone.xml

# Start Wildfly
${WORK_DIR}/wildfly-11.0.0.Beta1/bin/standalone.sh -b 0.0.0.0 &

# Deploy MapR Music REST Service
OUT=1
while [ $OUT -ne 0 ]
do
   ${WORK_DIR}/wildfly-11.0.0.Beta1/bin/jboss-cli.sh --connect --command="deploy --force $WORK_DIR/mapr-music-rest.war"
   OUT=$?
done

# Deploy MapR Music UI
${WORK_DIR}/wildfly-11.0.0.Beta1/bin/jboss-cli.sh --connect --command="deploy --force $WORK_DIR/mapr-music-ui.war"

# Add Wildfly users
#export WILDFLY_HOME="${WORK_DIR}/wildfly-11.0.0.Beta1/"
#${WORK_DIR}/add-wildfly-users.sh --path ${WORK_DIR}/dataset/users/

# Run MapR Music ElasricSearch Service
java -jar ${WORK_DIR}/elasticsearch-service.jar -r &

sleep infinity