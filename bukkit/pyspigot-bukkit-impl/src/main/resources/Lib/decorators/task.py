"""
Contains decorators for scheduling tasks with the task manager.
"""

import pyspigot as ps


def _schedule(is_async, function, delay, interval, function_args):
    task_manager = ps.task_manager()

    delay = int(delay or 0)
    interval = int(interval or 0)

    if interval > 0:
        if is_async:
            return task_manager.scheduleAsyncRepeatingTask(function, delay, interval, *function_args)
        else:
            return task_manager.scheduleRepeatingTask(function, delay, interval, *function_args)
    elif delay > 0:
        if is_async:
            return task_manager.runTaskLaterAsync(function, delay, *function_args)
        else:
            return task_manager.runTaskLater(function, delay, *function_args)
    else:
        if is_async:
            return task_manager.runTaskAsync(function, *function_args)
        else:
            return task_manager.runTask(function, *function_args)


def task(delay=0, interval=0, *args):
    """
    Schedule a synchronous task by decorating a function. The decorated function will be called when the task executes.

    Rules:
      - If no delay or interval are specified (or both are 0), the task will execute immediately.
      - If delay > 0, the task will automatically be scheduled as a delayed task (to execute later).
      - If interval > 0, the task will automtically be scheduled as a repeating task.

    :param delay:    The delay, in ticks, to wait before executing the task.
    :param interval: The interval, in ticks, between each execution of the task.
    :param args:     Positional arguments to pass to the task function when the task executes.
    """

    def _decorator(function):
        handle = _schedule(False, function, delay, interval, args)

        function.scheduled_task = handle

        def _cancel():
            ps.task_manager().stopTask(handle)

        function.cancel = _cancel

        return function
    return _decorator


def async_task(delay=0, interval=0, *args):
    """
    Schedule an asynchronous task by decorating a function. The decorated function will be called when the task executes.

    Rules:
      - If no delay or interval are specified (or both are 0), the task will execute immediately.
      - If delay > 0, the task will automatically be scheduled as a delayed task (to execute later).
      - If interval > 0, the task will automtically be scheduled as a repeating task.

    :param delay:    The delay, in ticks, to wait before executing the task.
    :param interval: The interval, in ticks, between each execution of the task.
    :param args:     Positional arguments to pass to the task function when the task executes.
    """

    def _decorator(function):
        handle = _schedule(True, function, delay, interval, args)

        function.scheduled_task = handle

        def _cancel():
            ps.task_manager().stopTask(handle)

        function.cancel = _cancel

        return function
    return _decorator


def sync_callback_task(delay=0, *args):
    """
    Schedule a new asynchronous task with a synchronous callback by decorating a function. Data returned from the initially called function
    (asynchronous portion) is automatically passed to the synchronous callback function as a function argument.

    To register the synchronous callback function, this function attaches a

    :param delay: The delay, in ticks, to wait before executing the task
    :param interval: The interval, in ticks, at which the task should be executed
    :param args: Arguments to pass through to the task function
    """

    delay = int(delay or 0)

    def _decorator(function):
        task_manager = ps.task_manager()

        handles = []

        def _callback(callback_function):
            if delay > 0:
                scheduled_task = task_manager.runSyncCallbackTaskLater(function, callback_function, delay, *args)
            else:
                scheduled_task = task_manager.runSyncCallbackTask(function, callback_function, *args)
            function.scheduled_task = scheduled_task
            handles.append(scheduled_task)
            return callback_function

        function.callback = _callback

        def _cancel():
            while handles:
                handle = handles.pop()
                task_manager.stopTask(handle)

        function.cancel = _cancel

        return function
    return _decorator


__all__ = [
    'task',
    'async_task',
    'sync_callback_task'
]
