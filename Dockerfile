FROM maprtech/pacc:6.0.0_4.0.0_centos7

EXPOSE 8080
EXPOSE 9300
EXPOSE 9200

# Create a directory for your MapR Application and copy the Application
RUN mkdir -p /usr/share/mapr-apps/mapr-music
COPY ./mapr-rest/target/mapr-music-rest.war /usr/share/mapr-apps/mapr-music/mapr-music-rest.war
COPY ./mapr-ui/target/mapr-music-ui.war /usr/share/mapr-apps/mapr-music/mapr-music-ui.war
COPY ./recommendation-engine/target/recommendation-engine-1.0-SNAPSHOT.jar /usr/share/mapr-apps/mapr-music/recommendation-engine.jar
COPY ./elasticsearch-service/target/elasticsearch-service-1.0-SNAPSHOT.jar /usr/share/mapr-apps/mapr-music/elasticsearch-service.jar
COPY ./data-generator/target/data-generator-1.0-SNAPSHOT.jar /usr/share/mapr-apps/mapr-music/data-generator.jar

COPY ./bin/run.sh /usr/share/mapr-apps/mapr-music/run.sh
RUN chmod +x /usr/share/mapr-apps/mapr-music/run.sh

COPY ./bin/add-wildfly-users.sh /usr/share/mapr-apps/mapr-music/add-wildfly-users.sh
RUN chmod +x /usr/share/mapr-apps/mapr-music/add-wildfly-users.sh

# Install prerequisites
RUN sudo yum install -y unzip jq

# Install Wildfly
WORKDIR /usr/share/mapr-apps/mapr-music
RUN wget http://download.jboss.org/wildfly/11.0.0.Beta1/wildfly-11.0.0.Beta1.zip
RUN sudo unzip wildfly-11.0.0.Beta1.zip
RUN echo 'JAVA_OPTS="$JAVA_OPTS -Dmapr.library.flatclass"' >> wildfly-11.0.0.Beta1/bin/standalone.conf

# Configure Wildfly
COPY conf/standalone.xml wildfly-11.0.0.Beta1/standalone/configuration/standalone.xml

# Install Drill JDBC Driver
RUN wget http://apache.mediamirrors.org/drill/drill-1.13.0/apache-drill-1.13.0.tar.gz
RUN tar -zxf apache-drill-1.13.0.tar.gz
RUN mkdir -p wildfly-11.0.0.Beta1/modules/system/layers/base/org/apache/drill/main
RUN cp apache-drill-1.13.0/jars/jdbc-driver/drill-jdbc-all-1.13.0.jar wildfly-11.0.0.Beta1/modules/system/layers/base/org/apache/drill/main
COPY ./conf/wildfly-drill-module.xml wildfly-11.0.0.Beta1/modules/system/layers/base/org/apache/drill/main/module.xml

# Install ElasticSearch
RUN wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.3.tar.gz
RUN tar -zxf elasticsearch-5.6.3.tar.gz

# Copy dataset, which will be used to register Wildfly users
COPY ./dataset/dataset.tar.gz /usr/share/mapr-apps/mapr-music/dataset/dataset.tar.gz
RUN tar -zxf dataset/dataset.tar.gz --directory dataset/

# Register Wildfly users
RUN export WILDFLY_HOME=wildfly-11.0.0.Beta1/ && ./add-wildfly-users.sh --path dataset/users

CMD ["/usr/share/mapr-apps/mapr-music/run.sh"]
