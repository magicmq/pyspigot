![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/dev.magicmq/pyspigot?nexusVersion=3&server=https%3A%2F%2Frepo.magicmq.dev)
![Latest Snapshot](https://img.shields.io/badge/dynamic/xml?color=orange&label=Latest%20Snapshot&query=%2F%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Blast()%5D&url=https%3A%2F%2Frepo.magicmq.dev%2Frepository%2Fmaven-snapshots%2Fdev%2Fmagicmq%2Fpyspigot%2Fmaven-metadata.xml)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/magicmq/PySpigot/maven.yml?branch=master)
![Apache 2.0 License](https://img.shields.io/github/license/magicmq/ItemAPI)

# PySpigot
PySpigot is a [Spigot/Bukkit](https://www.spigotmc.org/) plugin that can load, compile, and run Python scripts. These scripts can make use of Java standard libraries, as well as the Bukkit/Spigot API and APIs of any other loaded plugins.

PySpigot utilizes [Jython](https://www.jython.org/), a Java implementation of Python that runs entirely on the JVM.

## A Note About Jython

The major drawback of Jython is that it currently only supports Python 2. Work towards a Python 3 implementation is currently ongoing over at [Jython's GitHub repository](https://github.com/jython/jython).

Regarding different avenues, other Python-Java interop projects support Python 3. One such example is [Py4J](https://www.py4j.org/), which is more akin to a "network bridge" between the Python and Java runtimes rather than a true Python implementation. Although it supports Python 3, Py4J would be incredibly difficult to implement as a scripting engine for Bukkit, as it relies heavily on time-consuming I/O operations and Callbacks, which would make the Minecraft server quite unstable. Additionally, Py4J would require a CPython installation (regular Python) on the same machine, a rather difficult requirement to fulfill when working within a containerized Minecraft instance (such as Pterodactyl, shared hosting providers, etc.).

When I began this project, I looked at using several libraries, Py4J included. I came to the conclusion that, although Jython only supports Python 2, it has several key advantages over other libraries in this specific use case, including:

- It runs entirely on the JVM, making it very easy to interface with Python code from the Java side.
- It runs entirely on the JVM, which gives all Python code direct access to the entire Java classpath at runtime. Ergo, Python code has full access to Java code and vice versa.
- It does not require any external Python installation to work. Drag and drop PySpigot into your plugins folder, and you're good to go.
- And finally, it is reasonably fast (fast enough for Minecraft's standards), given that, again, it runs entirely on the JVM.

Thus, for the foreseeable future, PySpigot will continue to utilize Jython.

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
Replace `{VERSION}` with the version shown above, without the `v`.

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
Replace `{VERSION}` with the version shown above, without the `v`.

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
