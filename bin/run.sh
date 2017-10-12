#!/bin/bash

#######################################################################
# Globals definition
#######################################################################



WORK_DIR=/usr/share/mapr-apps/mapr-music

# TODO start elastic
sudo sed -i -e "s/yournodename/$1/g" ${WORK_DIR}/wildfly-11.0.0.Beta1/standalone/configuration/standalone.xml
sudo ${WORK_DIR}/wildfly-11.0.0.Beta1/bin/standalone.sh -b 0.0.0.0 &

OUT=1
while [ $OUT -ne 0 ]
do
   sudo ${WORK_DIR}/wildfly-11.0.0.Beta1/bin/jboss-cli.sh --connect --command="deploy --force $WORK_DIR/mapr-music-rest.war"
   OUT=$?
done

sudo ${WORK_DIR}/wildfly-11.0.0.Beta1/bin/jboss-cli.sh --connect --command="deploy --force $WORK_DIR/mapr-music-ui.war"

sleep infinity