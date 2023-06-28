package dev.magicmq.pyspigot.manager.task;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.python.core.PyBaseCode;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskManager {

    private static TaskManager manager;

    private final List<RepeatingTask> repeatingTasks;

    private TaskManager() {
        repeatingTasks = new ArrayList<>();
    }

    public int runTask(PyFunction function) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        Task task = new Task(script, function);
        return Bukkit.getScheduler().runTask(PySpigot.get(), task).getTaskId();
    }

    public int runTaskAsync(PyFunction function) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        Task task = new Task(script, function);
        return Bukkit.getScheduler().runTaskAsynchronously(PySpigot.get(), task).getTaskId();
    }

    public int runTaskLater(PyFunction function, long delay) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        Task task = new Task(script, function);
        return Bukkit.getScheduler().runTaskLater(PySpigot.get(), task, delay).getTaskId();
    }

    public int runTaskLaterAsync(PyFunction function, long delay) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        Task task = new Task(script, function);
        return Bukkit.getScheduler().runTaskLaterAsynchronously(PySpigot.get(), task, delay).getTaskId();
    }

    public int scheduleRepeatingTask(PyFunction function, long delay, long interval) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        RepeatingTask task = new RepeatingTask(script, function);
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(PySpigot.get(), task, delay, interval);
        task.setTaskId(bukkitTask.getTaskId());
        repeatingTasks.add(task);
        return bukkitTask.getTaskId();
    }

    public int scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        RepeatingTask task = new RepeatingTask(script, function);
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(PySpigot.get(), task, delay, interval);
        task.setTaskId(bukkitTask.getTaskId());
        repeatingTasks.add(task);
        return bukkitTask.getTaskId();
    }

    public void stopTask(int taskId) {
        for (Iterator<RepeatingTask> iterator = repeatingTasks.iterator(); iterator.hasNext();) {
            RepeatingTask next = iterator.next();
            if (next.getTaskId() == taskId) {
                Bukkit.getScheduler().cancelTask(next.getTaskId());
                iterator.remove();
            }
        }
    }

    public void stopTasks(Script script) {
        for (Iterator<RepeatingTask> iterator = repeatingTasks.iterator(); iterator.hasNext();) {
            RepeatingTask next = iterator.next();
            if (next.getScript().equals(script)) {
                Bukkit.getScheduler().cancelTask(next.getTaskId());
                iterator.remove();
            }
        }
    }

    public static TaskManager get() {
        if (manager == null)
            manager = new TaskManager();
        return manager;
    }
}
