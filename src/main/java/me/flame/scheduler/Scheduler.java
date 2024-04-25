package me.flame.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class Scheduler {
    static final Map<Integer, Task> TASKS = new HashMap<>(5);
    static int TASK_IDS = 0;

    static final Logger LOGGER = Logger.getLogger("Scheduling");

    private static final Timer TIMER = new Timer();

    public static TaskBuilder factory() {
        return new Scheduler.TaskBuilder();
    }

    public static AsyncScheduler.TaskBuilder async() {
        return new AsyncScheduler.TaskBuilder(AsyncScheduler.service);
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static AsyncScheduler.TaskBuilder async(ScheduledExecutorService service) {
        return new AsyncScheduler.TaskBuilder(service);
    }

    public static void cancel(int taskId) {
        Task task = TASKS.get(taskId);
        if (task != null) task.cancel();
    }

    public static class TaskBuilder {
        private Runnable task;
        private long delay = -1, repeat = -1;

        public Scheduler.TaskBuilder delay(long delay, long repeatDelay) {
            if (delay < 0 || repeatDelay < 0) {
                throw new IllegalArgumentException("Delay/repeat delay must not be under -1. \nDelay before Starting: " + delay + "\nRepeat period: " + repeatDelay);
            }
            this.delay = delay;
            this.repeat = repeatDelay;
            return this;
        }

        public Scheduler.TaskBuilder delay(long delay) {
            if (delay < 0) {
                throw new IllegalArgumentException("Delay/repeat delay must not be under -1. \nDelay before Starting: " + delay);
            }
            this.delay = delay;
            return this;
        }

        public Scheduler.TaskBuilder task(Runnable task) {
            Objects.requireNonNull(task);
            this.task = task;
            return this;
        }

        public Task execute() {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    task.run();
                }
            };
            Task scheduledTask = new Task(timerTask, TASK_IDS);
            TASK_IDS++;

            if (repeat == -1) {
                if (delay != -1) {
                    TIMER.schedule(timerTask, delay);
                    return scheduledTask;
                }
                LOGGER.warning("Could be useless synchronous scheduling, delay and repeat undefined, did you mean this to add delay/repeat?");
                task.run();
                return scheduledTask;
            }

            TIMER.scheduleAtFixedRate(timerTask, delay == -1 ? 0 : delay, repeat);
            return scheduledTask;
        }
    }
}
