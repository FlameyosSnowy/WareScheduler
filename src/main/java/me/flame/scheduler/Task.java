package me.flame.scheduler;

import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class Task {
    private final int taskId;
    private Future<?> future = null;
    private TimerTask task = null;

    private Consumer<Task> onCancel;

    public Task(Future<?> future, int taskId) {
        Objects.requireNonNull(future);
        this.future = future;
        this.taskId = taskId;
    }

    public Task(TimerTask task, int taskId) {
        Objects.requireNonNull(task);
        this.task = task;
        this.taskId = taskId;
    }

    public void cancel(boolean interrupt) {
        if (future != null) {
            future.cancel(interrupt);
            return;
        }

        task.cancel();
        if (onCancel != null) onCancel.accept(this);

    }

    public void cancel() {
        cancel(true);
    }

    public int taskId() {
        return taskId;
    }

    public Consumer<Task> onCancel() {
        return onCancel;
    }

    public void onCancel(Consumer<Task> task) {
        this.onCancel = task;
    }
}
