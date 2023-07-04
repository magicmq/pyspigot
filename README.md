![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/dev.magicmq/pyspigot?nexusVersion=3&server=https%3A%2F%2Frepo.magicmq.dev)
![Latest Snapshot](https://img.shields.io/badge/dynamic/xml?color=orange&label=Latest%20Snapshot&query=%2F%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Blast()%5D&url=https%3A%2F%2Frepo.magicmq.dev%2Frepository%2Fmaven-snapshots%2Fdev%2Fmagicmq%2Fpyspigot%2Fmaven-metadata.xml)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/magicmq/PySpigot/maven.yml?branch=master)
![Apache 2.0 License](https://img.shields.io/github/license/magicmq/ItemAPI)

# PySpigot
PySpigot is a [Spigot/Bukkit](https://www.spigotmc.org/) plugin that can load, compile, and run Python scripts. These scripts can make use of Java standard libraries, as well as the Bukkit/Spigot API and APIs of any other loaded plugins.

PySpigot utilizes [Jython](https://www.jython.org/), a Java implementation of Python that runs entirely on the JVM.

## Features

PySpigot has many features, including:
- Load scripts on server start and via commands
- Stop, reload, and unload server scripts via commands
- Register event listeners
- Register commands
- Schedule tasks (synchronous and asynchronous)
- Work with config files
- Register ProtocolLib packet listeners
- Register PlaceholderAPI placeholder expansions
- Comprehensive logging of errors and exceptions on a per-script basis, to file
- Load Java libraries you'd like to work with at runtime
- Write scripts in Python syntax
- Scripts have complete access to the Bukkit/Spigot API, as well as APIs of other plugins, so anything is possible.
- And more!

## Using PySpigot

For complete documentation on writing/loading/running scripts, using PySpigot's provided managers, and using the plugin in general, see PySpigot's documentation.

For Java developers, PySpigot has an API! See the Javadocs for complete documentation of API available to you.

## Adding PySpigot as a Dependency

### Maven

Add the following repository:
```
<repository>
    <id>magicmq-repo</id>
    <url>https://repo.magicmq.dev/repository/maven-releases/</url>
</repository>
```
Then, add the following dependency:
```
<dependency>
    <groupId>dev.magicmq</groupId>
    <artifactId>pyspigot</artifactId>
    <version>{VERSION}</version>
    <scope>provided</scope>
</dependency>
```
Replace `{VERSION}` with the version shown above.

### Gradle

Add the following repository:
```
repositories {
    ...
    magicmq-repo { url 'https://repo.magicmq.dev/repository/maven-releases/' }
}
```
Then, add the following dependency:
```
dependencies {
    ...
    compileOnly 'dev.magicmq:pyspigot:{VERSION}'
}
```
Replace `{VERSION}` with the version shown above.

### Manual Usage

Releases are also published on GitHub [here](https://github.com/magicmq/PySpigot/releases). You may download the JAR and import it yourself into your IDE of choice, or you may install it into your local repository.

## Building

Building requires [Maven](https://maven.apache.org/) and [Git](https://git-scm.com/). Maven 3+ is recommended for building the project. Follow these steps:

1. Clone the repository: `git clone https://github.com/magicmq/PySpigot.git`
2. Enter the repository root: `cd PySpigot`
3. Build with Maven: `mvn clean package`
4. Built files will be located in the `target` directory.

## Issues/Suggestions

Do you have any issues or suggestions? [Submit an issue report.](https://github.com/magicmq/PySpigot/issues/new)
