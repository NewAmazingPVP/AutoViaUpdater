package common;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class RealTimeRestartScheduler {
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> pendingRestart;

    public RealTimeRestartScheduler(ScheduledExecutorService executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public synchronized ScheduledFuture<?> scheduleRestart(Runnable restartTask, long delaySeconds) {
        Objects.requireNonNull(restartTask, "restartTask");
        cancelPendingRestart();
        pendingRestart = executor.schedule(restartTask, Math.max(0L, delaySeconds), TimeUnit.SECONDS);
        return pendingRestart;
    }

    public synchronized void cancelPendingRestart() {
        if (pendingRestart != null) {
            pendingRestart.cancel(false);
            pendingRestart = null;
        }
    }
}
