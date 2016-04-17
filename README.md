# exchange-maven-plugin

This Maven plugin allows to create and update objects (entries) in Anypoint Exchange by using its public API, 
integrated as part of the development lifecycle.

## Goals
The plugin has one and only goal for upserting the objects into Exchange, which is **add**. It handles creation and 
updates as explained in the next section.

## Use cases
Main use cases of this plugin are:

+ Taking a mavenized project, gathering some specific data from it (such as project name, description, version, 
Mule Runtime, etc) and creating a new object in Anypoint Exchange (creation)
+ Taking a mavenized project that is already published in Anypoint Exchange and updating with the new version details 
after deployment phase (update)

This plugin does not:

+ Handle artifacts deployments
+ Provide upload methods to any hosting service

## Support

### Anypoint Exchange

This plugin works with both public and private Exchange organizations.

### Environment

This plugin works in Mac OS, Linux and Windows, with Maven v3.1.1 and above.

## Maven dependency

```
<plugin>
  <groupId>org.mule.tools</groupId>
  <artifactId>exchange-maven-plugin</artifactId>
  <version>${exchange.version}</version>
</plugin>
```

## Maven repository

Add the following Maven Repository to your settings.xml file.     

```
<pluginRepositories>
    <pluginRepository>
        <id>mule-public</id>
        <url>https://repository.mulesoft.org/nexus/content/repositories/releases</url>
    </pluginRepository>
</pluginRepositories>
```

## Configuration example

When running from command line, add the following configuration within Build -> Plugins section:

```
<plugin>
    <groupId>org.mule.tools</groupId>
    <artifactId>exchange-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <anypointUsername>${anypointUsername}</anypointUsername>
        <anypointPassword>${anypointPassword}</anypointPassword>
        <nameUrl>template-name-url</nameUrl>
        <objectType>template</objectType>
        <!-- Optional Values (with defaults) -->
        <versioningStrategy>incremental</versioningStrategy>
        <anypointUri>https://anypoint.mulesoft.com</anypointUri>
        <muleRuntimeVersion>3.7</muleRuntimeVersion>
    </configuration>
</plugin>
```

When running as part of the Maven deploy (e.g. in a Continuous Integration server), add after the latest plugin:

```
<plugin>
    <groupId>org.mule.tools</groupId>
    <artifactId>exchange-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <anypointUsername>${anypointUsername}</anypointUsername>
        <anypointPassword>${anypointPassword}</anypointPassword>
        <nameUrl>template-name-url</nameUrl>
        <objectType>template</objectType>
        <!-- Optional Values (with defaults) -->
        <versioningStrategy>incremental</versioningStrategy>
        <anypointUri>https://anypoint.mulesoft.com</anypointUri>
        <muleRuntimeVersion>3.7</muleRuntimeVersion>
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
mvn org.mule.tools:exchange-maven-plugin:1.0.0:add -DnameUrl=template-name-url -DobjectType=template -DversioningStrategy=incremental -DanypointUsername=username -DanypointPassword=password
```