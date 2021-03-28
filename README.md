# PySpigot
PySpigot is a [Spigot/Bukkit](https://www.spigotmc.org/) plugin that can load, compile, and run Python scripts. These scripts can make use of Java standard libraries, as well as the Bukkit/Spigot API and APIs of any other loaded plugins.

Python uses [Jython](https://www.jython.org/), a Java implementation of Python that runs entirely on the JVM.

## Features

PySpigot has many features, including:
* Ability to load and run scripts via commands in console and in-game
* Ability to stop, reload, and unload scripts via commands in console and in-game  
* Ability to autorun scripts at server start
* Register Bukkit commands easily with PySpigot's command manager
* Register event listeners with PySpigot's listener manager
* Schedule and run synchronous/asynchronous tasks with PySpigot's task manager
* Easily load and read from/write to config files with PySpigot's config manager

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