[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/dev.magicmq/pyspigot?nexusVersion=3&server=https%3A%2F%2Frepo.magicmq.dev)](https://repo.magicmq.dev/#browse/browse:maven-releases:dev%2Fmagicmq%2Fpyspigot)
[![Latest Snapshot](https://img.shields.io/badge/dynamic/xml?color=orange&label=Latest%20Snapshot&query=%2F%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Blast()%5D&url=https%3A%2F%2Frepo.magicmq.dev%2Frepository%2Fmaven-snapshots%2Fdev%2Fmagicmq%2Fpyspigot%2Fmaven-metadata.xml)](https://repo.magicmq.dev/#browse/browse:maven-snapshots:dev%2Fmagicmq%2Fpyspigot)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/magicmq/PySpigot/maven.yml?branch=master)
![Apache 2.0 License](https://img.shields.io/github/license/magicmq/ItemAPI)

# PySpigot
PySpigot is a [Spigot/Bukkit](https://www.spigotmc.org/) plugin that can load, compile, and run Python scripts. These scripts can make use of Java standard libraries, as well as the Bukkit/Spigot API and APIs of any other loaded plugins.

PySpigot utilizes [GraalPython](https://www.graalvm.org/python/) for Java-Python interoperability. More specifically, PySpigot uses a special version of GraalPyython that is designed to be embedded into other Java applications called [Polyglot](https://www.graalvm.org/latest/reference-manual/polyglot-programming/). Under the hood, Polyglot uses [Truffle](https://github.com/oracle/graal/tree/master/truffle) and the [Sulong LLVM Runtime](https://github.com/oracle/graal/tree/master/sulong).

## A Note About GraalPy and Jython

Prior to version 0.7.0, PySpigot relied on [Jython](https://www.jython.org/) for Java-Python interoperability. I ultimately made the decision to switch to GraalPy because GraalPy offers mostly the same features and similar advantages (over other frameworks) as Jython. Additionally, GraalPy supports Python 3 syntax and libraries, and it is much faster than Jython (although in the embedded context on the standard JVM (not GraalSDK) the difference in speed may be less significant). GraalPy is also being actively updated and maintained by several active contributors and has a growing community, which means that bugs will continue to receive fixes, new features will be released, and so on.

The major drawback of Jython is that it currently only supports Python 2. Work towards a Python 3 implementation is currently ongoing over at [Jython's GitHub repository](https://github.com/jython/jython). It's also quite slow, but in the context of Minecraft (and in the embedded context, as discussed above), this matters less.

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

You can download the latest version of PySpigot from the [releases](https://github.com/magicmq/pyspigot/releases/) page, or from [Spigot](https://www.spigotmc.org/resources/pyspigot.111006/). Drop it into your plugins folder, and you're ready to begin writing scripts.

PySpigot also has an official Discord server for support, updates, and announcements. Click [here](https://discord.gg/f2u7nzRwuk) to join.

For complete documentation on writing/loading/running scripts, using PySpigot's provided managers, and using the plugin in general, see [PySpigot's documentation](https://pyspigot-docs.magicmq.dev/).

For Java developers, PySpigot has an API! See the [Javadocs](https://javadocs.magicmq.dev/pyspigot) for complete documentation of API available for you to use.

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
