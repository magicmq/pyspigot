"""
Contains decorators for scheduling tasks with the task manager.

Note that in Velocity, synchronous tasks are not allowed. Thus, any functions from the task manager that involve
synchronous tasks are not supported here.
"""

import pyspigot as ps

def _schedule(function, delay, delay_time_unit, interval, interval_time_unit, time_unit, function_args):
    task_manager = ps.task_manager()

    delay = int(delay or 0)
    interval = int(interval or 0)

    if interval > 0:
        if time_unit is not None:
            return task_manager.scheduleAsyncRepeatingTask(function, delay, time_unit, interval, time_unit, *function_args)
        if delay_time_unit is not None and interval_time_unit is not None:
            return task_manager.scheduleAsyncRepeatingTask(function, delay, delay_time_unit, interval, interval_time_unit, *function_args)
        else:
            return task_manager.scheduleAsyncRepeatingTask(function, delay, interval, *function_args)
    elif delay > 0:
        if time_unit is not None:
            return task_manager.runTaskLaterAsync(function, delay, time_unit, *function_args)
        elif delay_time_unit is not None:
            return task_manager.runTaskLaterAsync(function, delay, delay_time_unit, *function_args)
        else:
            return task_manager.runTaskLaterAsync(function, delay, *function_args)
    else:
        return task_manager.runTaskAsync(function, *function_args)


def async_task(delay=0, delay_time_unit=None, interval=0, interval_time_unit=None, time_unit=None, *args):
    """
    Schedule an asynchronous task by decorating a function. The decorated function will be called when the task executes.

    Rules:
      - If no delay or interval are specified (or both are 0), the task will execute immediately.
      - If delay > 0, the task will automatically be scheduled as a delayed task (to execute later).
      - If interval > 0, the task will automtically be scheduled as a repeating task.

    :param delay:    The delay, in ticks, to wait before executing the task.
    :param delay_time_unit: The Java TimeUnit of the delay. See https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/TimeUnit.html
    :param interval: The interval, in ticks, between each execution of the task.
    :param interval_time_unit: The Java TimeUnit of the interval. See https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/TimeUnit.html
    :param time_unit: A convenience parameter to set the interval and delay time unit simultaneously.
    :param args:     Positional arguments to pass to the task function when the task executes.
    """

    def _decorator(function):
        handle = _schedule(function, delay, delay_time_unit, interval, interval_time_unit, time_unit, args)

        function.scheduled_task = handle

        def _cancel():
            ps.task_manager().stopTask(handle)

        function.cancel = _cancel

        return function
    return _decorator


__all__ = ['async_task']
