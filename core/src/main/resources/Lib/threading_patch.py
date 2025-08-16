"""
This module patches the _pickSomeNonDaemonThread function in the threading module. _pickSomeNonDaemonThread is called
in the exit function of threading._MainThread to fetch an alive, non-daemon thread seen by the threading module. The
exit function then blocks/waits for the thread to die (via thread.join()). _pickSomeNonDaemonThread is called repeatedly
until it returns None (signifying all visible threads have been handled).

The patch here applies a condition which excludes any thread that is part of Bukkit, BungeeCord, or Velocity's scheduler
thread pool. These threads are kept alive for an arbitrary period of time; thus, they should not be waited on to die
with thread.join(). They are identified by their name (for example, "Craft Scheduler Thread" for Bukkit).

PySpigot imports this module into all scripts just prior to script unload, if "debug-options.patch-threading" is "true"
in the PySpigot config.yml. The patch is only applied if the script imported threading at an earlier point in time
(I.E. sys.modules contains "threading").

For more information, see https://github.com/magicmq/pyspigot/issues/18#issue-3012022678
"""
import sys


def _patch():
    """patches the _pickSomeNonDaemonThread function in the threading module."""
    if "threading" in sys.modules:
        import threading

        exclude_thread_names = [
            # Bukkit
            "Craft Scheduler Thread",
            # BungeeCord
            "Pool Thread",
            # Velocity
            "Task Executor"
        ]

        def _pickSomeNonDaemonThreadPatched():
            for t in threading.enumerate():
                if not t.isDaemon() and t.isAlive():
                    # In addition to excluding daemon threads and dead threads, exclude threads from Bukkit, BungeeCord,
                    # and Velocity thread pools
                    if all(thread_name not in t.getName() for thread_name in exclude_thread_names):
                        return t
            return None

        threading._pickSomeNonDaemonThread = _pickSomeNonDaemonThreadPatched