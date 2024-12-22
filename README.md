[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/dev.magicmq/pyspigot?nexusVersion=3&server=https%3A%2F%2Frepo.magicmq.dev)](https://repo.magicmq.dev/#browse/browse:maven-releases:dev%2Fmagicmq%2Fpyspigot)
[![Latest Snapshot](https://img.shields.io/badge/dynamic/xml?color=orange&label=Latest%20Snapshot&query=%2F%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Blast()%5D&url=https%3A%2F%2Frepo.magicmq.dev%2Frepository%2Fmaven-snapshots%2Fdev%2Fmagicmq%2Fpyspigot%2Fmaven-metadata.xml)](https://repo.magicmq.dev/#browse/browse:maven-snapshots:dev%2Fmagicmq%2Fpyspigot)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/magicmq/PySpigot/maven.yml?branch=master)
![Apache 2.0 License](https://img.shields.io/github/license/magicmq/PySpigot)

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

You can download the latest version of PySpigot from the [releases](https://github.com/magicmq/pyspigot/releases/) page, or from [Spigot](https://www.spigotmc.org/resources/pyspigot.111006/). Drop it into your plugins folder, and you're ready to begin writing scripts.

PySpigot also has an official Discord server for support, updates, and announcements. Click [here](https://discord.gg/f2u7nzRwuk) to join.

For complete documentation on writing/loading/running scripts, using PySpigot's provided managers, and using the plugin in general, see [PySpigot's documentation](https://pyspigot-docs.magicmq.dev/).

For Java developers, PySpigot has an API! See the [Javadocs](https://javadocs.magicmq.dev/pyspigot) for complete documentation of API available for you to use.

## A Note About Jython

The major drawback of Jython is that it currently only supports Python 2. Work towards a Python 3 implementation is currently ongoing over at [Jython's GitHub repository](https://github.com/jython/jython).

Regarding different avenues, other Python-Java interop projects support Python 3. One such example is [Py4J](https://www.py4j.org/), which is more akin to a "network bridge" between the Python and Java runtimes rather than a true Python implementation. Although it supports Python 3, Py4J would be incredibly difficult to implement as a scripting engine for Bukkit, as it relies heavily on time-consuming I/O operations and Callbacks, which would make the Minecraft server quite unstable. Additionally, Py4J would require a CPython installation (regular Python) on the same machine, a rather difficult requirement to fulfill when working within a containerized Minecraft instance (such as Pterodactyl, shared hosting providers, etc.).

### GraalPy

[GraalPython (GraalPy)](https://github.com/oracle/graalpython) is a high-performance implementation of Python that runs on the GraalVM. It is also a Python 3.11 compliant runtime. Normally, one would run GraalPy on the [GraalVM](https://www.graalvm.org/). The GraalVM supports [JIT compilation](https://en.wikipedia.org/wiki/Just-in-time_compilation), a popular concept in scripting where code is compiled at execution time rather than before execution. Theoretically, GraalPy could be used instead of Jython, however, it would require installation and usage of the GraalVM instead of a standard Java VM that's often used when running a Minecraft server (such as OpenJDK or Oracle JDK). Installing the GraalVM wouldn't be an easy task for the unaquainted. Therefore, this would likely hurt the user-friendliness of PySpigot, and I don't consider it a viable option.

GraalPy does in fact support embedding into Java projects, which means that it can be used on a standard VM (not the special GraalVM that would normally be required). I wanted to test this out, so I refactored most of PySpigot to utilize this embedded version of GraalPy over at the [implement-graalpy](https://github.com/magicmq/pyspigot/tree/implement-graalpy) branch, but the results of this migration were rather disappointing:

It seems that regarding performance, Jython **significantly outperforms** the standalone (embedded) version of GraalPy. For benchmarking, I created a simple PySpigot script that runs a performance-intensive task (computing factorials) both synchronously (by attaching the task to a PlayerJoinEvent) and asynchronously (by attaching the task to an AsyncPlayerChatEvent). I then simulated player joins and chats with fake players. Here is the code and results of these tests:

```py
import pyspigot as ps
from org.bukkit.event.player import PlayerJoinEvent
from org.bukkit.event.player import AsyncPlayerChatEvent

def player_join(event):
  i = factorial(100)


def player_chat(event):
  i = factorial(100)

ps.listener_manager().registerListener(player_join, PlayerJoinEvent)
ps.listener_manager().registerListener(player_chat, AsyncPlayerChatEvent)

def factorial(n):
  if n < 2:
    return 1
  else:
    return n * factorial(n - 1)
```

The following graph shows the difference in speed between Jython and GraalPy when I ran `factorial(100)` within a PlayerJoinEvent (synchronous):

![PlayerJoinEvent.png](https://i.imgur.com/9vpdazL.png)

As you may be able to tell from the above graph, GraalPy performed nearly **four times slower** on average than Jython. This difference in speed may not seem like much, but these performance issues would likely compound in situations where more complex code is being interpreted more often, as is typically the case with PySpigot scripts. In addition, there is **much greater variability** in computation time with GraalPy than with Jython (I.E. it's less consistent than Jython).

When I ran `factorial(100)` within an AsyncPlayerChatEvent (asynchronous), similar performance issues were observed:

![AsyncPlayerChatEvent](https://i.imgur.com/9w7jGuR.png)

This difference in performance is likely due to the fact that embedded GraalPy **does not utilize JIT compilation**, as this feature is only available when using GraalPy on the GraalVM. On a standard VM, GraalPy doesn't compile scripts, it only interprets them. In other words, Jython actually compiles scripts into bytecode, which can be read rather quickly at runtime by the VM, while GraalPy only interprets scripts at runtime, which is much slower. JIT compilation is responsible for the speed that GraalPy touts, but unfortunately, it isn't being used in this case. PySpigot is essentially obligated to use embedded GraalPy (for reasons I outlined above), so there isn't a reasonable way around this issue.

Not to mention, script loading times were much slower with GraalPy at around 7-9 seconds, whereas with Jython, scripts usually load in about 3 seconds or less.

All that being said, despite the advantages of GraalPy (Python 3 compliant, actively maintained), this performance issue is a major letdown and a significant drawback, and I don't plan on switching from Jython to GraalPy anytime soon.

### The Bottom Line

When I began this project, I looked at using several libraries, Py4J included. I also considered GraalPy, as outlined above. I've come to the conclusion that, although Jython only supports Python 2, it has several key advantages over other options in this specific use case, including:

- It runs entirely on the JVM, making it very easy to interface with Python code from the Java side.
- It runs entirely on the JVM, which gives all Python code direct access to the entire Java classpath at runtime. Ergo, Python code has full access to Java code and vice versa.
- It does not require any external Python installation to work. Drag and drop PySpigot into your plugins folder, and you're good to go.
- And finally, it is reasonably fast (fast enough for Minecraft's standards), given that scripts are compiled into bytecode and, again, it all runs entirely on the JVM.

Thus, for the foreseeable future, PySpigot will continue to utilize Jython.

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
