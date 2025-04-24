"""
This module patches the _pickSomeNonDaemonThread function in the threading module. _pickSomeNonDaemonThread
is called in the exit function of threading._MainThread to fetch all non-daemon threads seen by the threading
module. The exit function blocks/waits for the thread to die (via thread.join()).

The patch applies a condition that excludes returning any thread that is part of Bukkit's scheduler thread
pool. These threads are identified by the name "Craft Scheduler Thread" and are kept alive for an arbitrary
period of time. Thus, they should not be waited on to die with thread.join().

PySpigot imports this module into all scripts just prior to script unload, if "debug-options.patch-threading"
is "true" in the PySpigot config.yml. The patch is only applied if the script imported threading at an earlier
point in time (I.E. sys.modules contains "threading").

For more information, see https://github.com/magicmq/pyspigot/issues/18#issue-3012022678
"""
import sys


def _patch():
    """patches the _pickSomeNonDaemonThread function in the threading module."""
    if "threading" in sys.modules:
        import threading

        def _pickSomeNonDaemonThreadPatched():
            for t in threading.enumerate():
                # In addition to excluding daemon threads and dead threads, exclude Craft Scheduler Threads
                if not t.isDaemon() and t.isAlive() and "Craft Scheduler Thread" not in t.getName():
                    return t
            return None

        threading._pickSomeNonDaemonThread = _pickSomeNonDaemonThreadPatched