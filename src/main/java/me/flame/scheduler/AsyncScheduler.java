package me.flame.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class AsyncScheduler {
    static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    private static boolean eagerService;

    /**
     * Will make the static scheduled executor service allocate ALL the threads in the CPU.
     * @apiNote Please know what you're doing when executing this,
     *          <p>
     *          this may have huge impacts on performance and block.
     *          <p>
     *          Use this if you know you will use EVERY thread.
     *          <p>
     *          Side effects of using this improperly: resource-exhaustion and a big unnecessary block
     * @since 1.0.0
     */
    public static void eagerThreadInitialization() {
        if (service != null && (eagerService || NUM_THREADS == 1)) return;
        AsyncScheduler.service = Executors.newScheduledThreadPool(NUM_THREADS);
        AsyncScheduler.eagerService = true;
    }

    public static class TaskBuilder {
        private final ScheduledExecutorService service;
        private Runnable task;
        private long delay = -1, repeat = -1;

        public TaskBuilder(ScheduledExecutorService service) {
            Objects.requireNonNull(service);
            this.service = service;
        }

        public AsyncScheduler.TaskBuilder delay(long delay, long repeatDelay) {
            this.delay = delay;
            this.repeat = repeatDelay;
            return this;
        }

        public AsyncScheduler.TaskBuilder task(Runnable task) {
            this.task = task;
            return this;
        }

        public Task execute() {
            if (repeat == -1) {
                if (delay != -1) return getTask(service.schedule(task, delay, TimeUnit.MILLISECONDS));
                return getTask(service.submit(task));
            }
            return getTask(service.scheduleAtFixedRate(task, delay == -1 ? 0 : delay, repeat, TimeUnit.MILLISECONDS));
        }
    }

    private static @NotNull Task getTask(Future<?> future) {
        Task scheduledTask = new Task(future, Scheduler.TASK_IDS);
        Scheduler.TASK_IDS++;
        return scheduledTask;
    }
}
