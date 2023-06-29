.. _tasks:

Tasks
=====

Through PySpigot, you can interact with Bukkit's task scheduler and schedule/run synchronous and asynchronous tasks. These allow you to run code on a thread other than the main thread as well as run code repeatedly at a fixed interval.

See `PySpigot's Managers`_ for instructions on how to import the task manager into your script.

.. note:: This is not a comprehensive guide to scheduling tasks. For a more complete guide to tasks and scheduler programming, see Bukkit's tutorial on using the scheduler: https://bukkit.fandom.com/wiki/Scheduler_Programming. Note that much of this information will not be useful because it pertains to Java, but the background information is nevertheless helpful.

Task Code Example
#################

Let's take a look at the following code that defines and starts a task:

.. code-block:: python
    :linenos:

    from dev.magicmq.pyspigot import PySpigot as ps

    def run_task():
        #Do something...

    task_id = ps.scheduler.scheduleRepeatingTask(run_task, 0, 100)

On line 1, we import PySpigot as ``ps`` to utilize the task manager (``scheduler``).

On line 3, we define a function called ``run_task`` that takes no arguments.

Like listeners, all tasks must be registered and run with PySpigot's task manager. There are many different ways to start tasks depending on if we want it to be synchronous, ascynchronous, and/or repeating, but here we want our task to be synchronous and repeating, so we use ``scheduleRepeatingTask(function, delay, interval)``, which takes three arguments:

* The first argument accepts the function that should be called when the task runs (either once or repeatedly at a fixed interval).
* The second argument is the delay (in ticks) that the scheduler should wait before starting the task when it is registered.
* The third argument is the interval (in ticks) that the task should be run.

Therefore, on line 6, we register our task as a synchronous repeating task with the task manager. This will return a task id, which we then store as a variable called ``task_id``. We store this task id in case we want to cancel our task later. Cancelling a task requires the task ID.

To summarize:

* Like listeners, tasks are defined as functions in your script. Task functions do not take any arguments and do not return anything.
* All tasks must be registered with PySpigot's command manager. To schedule and run a synchronous repeating task, use ``scheduler.scheduleRepeatingTask(function, delay, interval)``.

Task Manager Usage
##################

In addition to scheduling synchronous repeating tasks, the task manager has many other functions to schedule other types of tasks as well as stop tasks:

* ``scheduler.runTask(function)``: Run a synchronous task as soon as possible. Takes the function to call when the task runs.
* ``scheduler.runTaskAsync(function)``: Run an asychronous task (a task on a thread other than the main server thread). Takes the function to call when the task runs.
* ``scheduler.runTaskLater(function, delay)``: Run a synchronous task at some point in the future after the specified delay. Takes the function to call when the task runs and the delay to wait (in ticks) before running the task.
* ``scheduler.runTaskLaterAsync(function, delay)``: Run an asynchronous task at some point in the future after the specified delay. Takes the function to call when the task runs and the delay to wait (in ticks) before running the task.
* ``scheduler.scheduleRepeatingTask(function, delay, interval)``: Run a synchronous repeating task that repeats every specified interval. Takes the function to call each time the task runs, the delay to wait (in ticks) before running the task, and the interval (in ticks) at which the task should be run.
* ``scheduler.scheduleAsyncRepeatingTask(function, delay, interval)``: Run an asynchronous repeating task that repeats every specified interval. Takes the function to call each time the task runs, the delay to wait (in ticks) before running the task, and the interval (in ticks) at which the task should be run.
* ``scheduler.stopTask(id)``: Stop/Cancel a task. Takes the id of the task to stop.