package io.github.Nudgi.network;

import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

// Ultimately this just creates a thread for each connected player, locks the inactive player thread, and unlocks the active player thread

public class TurnManager {
    public static final long DEFAULT_TURN_MILLIS = 15000;

    private final Object lock;
    private final long turnMillis;
    private final Supplier<Collection<Integer>> connected;
    private final BiConsumer<Integer, Long> onTurn;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "TurnManager-timer");
        t.setDaemon(true);
        return t;
    });
    private int current = -1;
    private long deadline;
    private long token;
    private ScheduledFuture<?> timeout;

    public TurnManager(Object lock, long turnMillis, Supplier<Collection<Integer>> connected, BiConsumer<Integer, Long> onTurn) {
        this.lock = lock;
        this.turnMillis = turnMillis;
        this.connected = connected;
        this.onTurn = onTurn;
    }

    public void refresh() {
        if (current == -1 || !connected.get().contains(current)) advance();
    }

    public boolean endTurn(int id) {
        if (id != current) return false;
        advance();
        return true;
    }

    public boolean isCurrent(int id) {
        return id == current;
    }

    public int getCurrentPlayer() {
        return current;
    }

    public long remainingMillis() {
        if (current == -1) return 0;
        return Math.max(0, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
    }

    public String currentTurnMessage() {
        return current == -1 ? null : "TURN " + current + " " + remainingMillis();
    }

    public void stop() {
        token++;
        if (timeout != null) timeout.cancel(false);
        scheduler.shutdownNow();
    }

    private void advance() {
        TreeSet<Integer> ids = new TreeSet<>(connected.get());
        if (ids.isEmpty()) {
            current = -1;
            token++;
            if (timeout != null) timeout.cancel(false);
            return;
        }
        Integer next = ids.higher(current);
        startTurn(next != null ? next : ids.first());
    }

    private void startTurn(int id) {
        if (timeout != null) timeout.cancel(false);
        current = id;
        final long t = ++token;
        deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(turnMillis);
        try {
            timeout = scheduler.schedule(() -> {
                synchronized (lock) {
                    if (t == token && current != -1) advance();
                }
            }, turnMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {}
        onTurn.accept(id, turnMillis);
    }
}
