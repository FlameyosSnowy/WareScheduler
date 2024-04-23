package me.flame.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class Scheduler {
    static final List<Future<?>> TASKS = new ArrayList<>(5);
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

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
        Scheduler.service = Executors.newScheduledThreadPool(NUM_THREADS);
        Scheduler.eagerService = true;
    }

    public static SyncScheduler.TaskBuilder sync() {
        return new SyncScheduler.TaskBuilder();
    }

    public static Scheduler.TaskBuilder factory() {
        return new Scheduler.TaskBuilder(Scheduler.service);
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Scheduler.TaskBuilder factory(ScheduledExecutorService service) {
        return new Scheduler.TaskBuilder(service);
    }

    public static class TaskBuilder {
        private final ScheduledExecutorService service;
        private Runnable task;
        private int delay = -1, repeat = -1;

        public TaskBuilder(ScheduledExecutorService service) {
            Objects.requireNonNull(service);
            this.service = service;
        }

        public Scheduler.TaskBuilder delay(int delay) {
            this.delay = delay;
            return this;
        }

        public Scheduler.TaskBuilder repeat(int repeatDelay) {
            this.repeat = repeatDelay;
            return this;
        }

        public Scheduler.TaskBuilder task(Runnable task) {
            this.task = task;
            return this;
        }

        public Future<?> execute() {
            if (repeat == -1) {
                if (delay == -1) {
                    return service.submit(task);
                }
            }
        }
    }
}
