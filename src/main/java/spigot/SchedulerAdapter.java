package spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SchedulerAdapter {

    private SchedulerAdapter() {
    }

    private static Object getGlobalRegionScheduler() {
        try {
            Method m = Bukkit.class.getMethod("getGlobalRegionScheduler");
            return m.invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void runGlobal(Plugin plugin, Runnable task) {
        Object grs = getGlobalRegionScheduler();
        if (grs != null) {
            try {
                Method execute = grs.getClass().getMethod("execute", Plugin.class, Runnable.class);
                execute.invoke(grs, plugin, task);
                return;
            } catch (Throwable ignored) {
            }
            try {
                Method run = grs.getClass().getMethod("run", Plugin.class, Consumer.class);
                Consumer<Object> consumer = (ignoredTask) -> task.run();
                run.invoke(grs, plugin, consumer);
                return;
            } catch (Throwable ignored) {
            }
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public static void runGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        Object grs = getGlobalRegionScheduler();
        if (grs != null) {
            try {
                Method runDelayed = grs.getClass().getMethod("runDelayed", Plugin.class, Runnable.class, long.class);
                runDelayed.invoke(grs, plugin, task, delayTicks);
                return;
            } catch (Throwable ignored) {
            }
            try {
                Method runDelayed = grs.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                Consumer<Object> consumer = (ignoredTask) -> task.run();
                runDelayed.invoke(grs, plugin, consumer, delayTicks);
                return;
            } catch (Throwable ignored) {
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static boolean runGlobalAndWait(Plugin plugin, Runnable task, long timeout, TimeUnit unit) {
        final CountDownLatch latch = new CountDownLatch(1);
        runGlobal(plugin, () -> {
            try {
                task.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            return latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
