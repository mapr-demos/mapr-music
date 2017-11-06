# Building a UI with Angular

MapR Music Application has [MapR Music UI module](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-ui), 
which is [Angular](https://angular.io/) project. 

## Basics
This project was generated with [Angular CLI](https://github.com/angular/angular-cli). The Angular CLI is a command 
line interface tool that can create a project, add files, and perform a variety of ongoing development tasks such as 
testing, bundling, and deployment.

Building UI with Angular consists of the following steps:
* Set up the Development Environment
* Create a new project
* Serve the application
* Edit your Angular component

Let's consider them in details.

#### Set up the Development Environment

You need to set up your development environment before you can develop Angular app.

Install [Node.js® and npm](https://nodejs.org/en/download/). 
After that install the [Angular CLI](https://github.com/angular/angular-cli) globally:
```
$ npm install -g @angular/cli
```

#### Create a new project

Generate a new project and skeleton application by running the following command:
```
$ ng new your-application-name
```

#### Serve the application

Go to the project directory and launch the server:
```
$ cd your-application-name
$ ng serve --open
```

The `ng serve` command launches the server, watches your files, and rebuilds the app as you make changes to those files.

Using the `--open` (or just `-o`) option will automatically open your browser on `http://localhost:4200/`.

#### Edit your Angular component

The CLI created the first Angular component for you. This is the root component and it is named `app-root`. 
You can find it in `./src/app/app.component.ts`.

Open the component file and change it in some way:
```
export class AppComponent {
  title = 'Changed Title';
}
```

The browser reloads automatically with the revised title. That's about all you'd expect to do in a "Hello, World" app.
To get more details about Angular apps development, please refer 
[Angular Tour of Heroes tutorial](https://angular.io/tutorial) and [Angular Documentation](https://angular.io/docs).


## Build with Maven
[MapR Music UI module](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-ui) is Maven module, which can be built 
into `war` using Maven install command:
```
$ cd mapr-music/mapr-ui/
$ mvn clean install
```

You can turn your Angular project into Maven module by adding `pom.xml`. Here is listing of MapR Music UI `pom.xml`:
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.mapr.music</groupId>
  <artifactId>mapr-music-ui</artifactId>
  <packaging>war</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>mapr-music-ui</name>
  <url>https://github.com/mapr-demos/mapr-music</url>

  <properties>
    <war.plugin.version>3.1.0</war.plugin.version>
  </properties>

  <build>
    <finalName>mapr-music-ui</finalName>
    <plugins>
      <plugin>
        <artifactId>maven-clean-plugin</artifactId>
        <version>3.0.0</version>
        <configuration>
          <filesets>
            <fileset>
              <directory>node_modules</directory>
              <followSymlinks>false</followSymlinks>
            </fileset>
            <fileset>
              <directory>${project.basedir}</directory>
              <includes>
                <include>package-lock.json</include>
              </includes>
            </fileset>
          </filesets>
        </configuration>
      </plugin>
      <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <!-- Use the latest released version:
        https://repo1.maven.org/maven2/com/github/eirslett/frontend-maven-plugin/ -->
        <version>1.5</version>
        <configuration>
          <installDirectory>target</installDirectory>
          <nodeVersion>v8.1.0</nodeVersion>
        </configuration>
        <executions>
          <execution>
            <id>install node and npm</id>
            <goals>
              <goal>install-node-and-npm</goal>
            </goals>
          </execution>
          <execution>
            <id>npm install</id>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>install</arguments>
            </configuration>
          </execution>
          <execution>
            <id>npm build</id>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>run build</arguments>
            </configuration>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.1.0</version>
        <configuration>
          <!-- Java EE 7 doesn't require web.xml, Maven needs to
              catch up! -->
          <!--<webXml>WEB-INF/web.xml</webXml>-->
          <webResources>
            <resource>
              <!-- this is relative to the pom.xml directory -->
              <directory>target/dist</directory>
            </resource>

            <resource>
              <directory>${basedir}/WEB-INF</directory>
              <!--<filtering>true</filtering>-->
              <targetPath>WEB-INF</targetPath>
              <!--<includes>-->
                <!--<include>**/xmlgateway-context.xml</include>-->
              <!--</includes>-->
            </resource>

          </webResources>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.wildfly.plugins</groupId>
        <artifactId>wildfly-maven-plugin</artifactId>
        <version>1.2.0.Beta1</version>
      </plugin>
    </plugins>
  </build>
</project>

```
As you can see, `pom.xml` declares [frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin), which 
downloads/installs Node and NPM locally for your project, runs `npm install`, and then any combination of Bower, Grunt, 
Gulp, Jspm, Karma, or Webpack. It's supposed to work on Windows, OS X and Linux.

[maven-war-plugin](https://maven.apache.org/plugins/maven-war-plugin/)  is responsible for collecting all artifact 
dependencies, classes and resources of the web application and packaging them into a web application archive.

You can notice that `maven-war-plugin` refers `/WEB-INF/web.xml` file. Here is listing of such file for MapR Music UI 
app:
```
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
		 http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
  <servlet>
    <servlet-name>root-page</servlet-name>
    <jsp-file>/index.html</jsp-file>
  </servlet>
  <servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/inline.bundle.js</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/polyfills.bundle.js</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/vendor.bundle.js</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/styles.bundle.js</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/main.bundle.js</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>root-page</servlet-name>
    <url-pattern>/*</url-pattern>
  </servlet-mapping>
</web-app>

```
It declares Servlets Mappings for MapR Music UI application.

After executing `mvn clean install` project, you will be able to find web application archive at Maven's `target` 
directory. This `war` is ready to be deployed at Wildfly:
```
/wildfly-10.1.0.Final/bin$ ./jboss-cli.sh --connect
[standalone@localhost:9990 /] deploy ~/mapr-music/mapr-ui/target/mapr-music-ui.war
```

## Wildfly Deployment Descriptor for MapR Music UI 

In order to define MapR Music UI context root, `WEB-INF` directory of MapR Music UI project contains `jboss-web.xml` 
deployment descriptor file:
```
<?xml version="1.0" encoding="UTF-8"?>
<jboss-web xmlns="http://www.jboss.com/xml/ns/javaee"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="
  http://www.jboss.com/xml/ns/javaee
  http://www.jboss.org/j2ee/schema/jboss-web_5_1.xsd">
    <context-root>/</context-root>
</jboss-web>
```

It defines MapR Music UI context root to be `/`. Thus, MapR Music UI can be accessed by 
[http://localhost:8080/](http://localhost:8080/) instead of 
[http://localhost:8080/mapr-music-ui/](http://localhost:8080/mapr-music-ui/).
