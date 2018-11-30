# Current Version
From https://forums.mulesoft.com/questions/94804/maven-exchange-api.html, this project is out of scope and we must refer to https://docs.mulesoft.com/anypoint-exchange/to-publish-assets-maven

# exchange-maven-plugin

This Maven plugin allows to create and update objects (entries) in Anypoint Exchange by using its public API, 
integrated as part of the development lifecycle.

## Goals
The plugin has one and only goal for upserting the objects into Exchange, which is **add**. It handles creation and 
updates as explained in the next section.

## Use cases
Main use cases of this plugin are:

+ Taking a mavenized Mule project, gathering some specific data from it (such as project name, description, version, 
Mule Runtime, etc) and creating a new object in Anypoint Exchange (creation).
+ Taking a mavenized Mule project that is already published in Anypoint Exchange and updating with the new version 
details after deployment phase (update).

This plugin does not:

+ Handle artifacts deployments.
+ Provide upload methods to any hosting service.

### Integration with Exchange workflow

+ When the plugin creates a new Exchange object, its state will be set as "Work in progress".
+ When the plugin updates an existent Exchange object, it will remain with the same state the object had before. 
If the user cannot publish to the BG, the modified object will be submitted for revision by an Admin.

### Notes

+ The **anypointPassword** parameter works with either password or CS token.
+ The **businessGroup** parameter works with either BG route (check examples below) or its API ID. 

## Support

### Anypoint Exchange

This plugin works with both public and private Exchange organizations.

### Environment

This plugin works in Mac OS, Linux and Windows, with Maven v3.1.1 and above.

## Maven dependency

```xml
<plugin>
  <groupId>org.mule.tools</groupId>
  <artifactId>exchange-maven-plugin</artifactId>
  <version>${exchange.plugin.version}</version>
</plugin>
```

## Maven repository

Add the following Maven Repository to your settings.xml file.     

```xml
<pluginRepositories>
    <pluginRepository>
        <id>mule-public</id>
        <url>https://repository.mulesoft.org/nexus/content/repositories/releases</url>
    </pluginRepository>
</pluginRepositories>
```

## Configuration example

When running from command line, add the following configuration within Build -> Plugins section:

```xml
<plugin>
    <groupId>org.mule.tools</groupId>
    <artifactId>exchange-maven-plugin</artifactId>
    <version>${exchange.plugin.version}</version>
    <configuration>
        <anypointUsername>${anypointUsername}</anypointUsername>
        <anypointPassword>${anypointPassword}</anypointPassword>
        <nameUrl>template-name-url</nameUrl>
        <objectType>template</objectType>
        <businessGroup>MasterOrg\BG1</businessGroup>
        <!-- Optional Values (with defaults) -->
        <versioningStrategy>incremental</versioningStrategy>
        <anypointUri>https://anypoint.mulesoft.com</anypointUri>
        <muleRuntimeVersion>3.8</muleRuntimeVersion>
    </configuration>
</plugin>
```

When running as part of the Maven deploy (e.g. in a Continuous Integration server), add after the latest plugin:

```xml
<plugin>
    <groupId>org.mule.tools</groupId>
    <artifactId>exchange-maven-plugin</artifactId>
    <version>${exchange.plugin.version}</version>
    <configuration>
        <anypointUsername>${anypointUsername}</anypointUsername>
        <anypointPassword>${anypointPassword}</anypointPassword>
        <nameUrl>template-name-url</nameUrl>
        <objectType>template</objectType>
        <businessGroup>MasterOrg\BG1</businessGroup>
        <!-- Optional Values (with defaults) -->
        <versioningStrategy>incremental</versioningStrategy>
        <anypointUri>https://anypoint.mulesoft.com</anypointUri>
        <muleRuntimeVersion>3.8</muleRuntimeVersion>
    </configuration>
    <executions>
        <execution>
            <id>exchange-update</id>
            <phase>deploy</phase>
            <goals>
                <goal>add</goal>
            </goals>
        </execution>
    </executions>    
</plugin>
```

## Running goal

```
mvn exchange:add
```

## Running from commandline without configuration

```
mvn org.mule.tools:exchange-maven-plugin:0.1.0:add -DnameUrl=template-name-url -DobjectType=template -DversioningStrategy=incremental -DbusinessGroup=MasterOrg -DanypointUsername=username -DanypointPassword=password
```
