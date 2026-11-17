package io.github.Nudgi.network;

import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.badlogic.gdx.utils.Null;

import io.github.Nudgi.network.protocol.TurnMessage;

public class TurnManager {
    private final Supplier<Collection<Integer>> connected;
    private final Consumer<Integer> onTurn;
    private int current = -1;

    // null = no player may perform any action (useful later on when animations need to play)
    // any non-null value = the turn player may perform the designated action
    private TurnStep step = null; 

    public TurnManager(Supplier<Collection<Integer>> connected, Consumer<Integer> onTurn) {
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

    public TurnStep getStep() {
        return step;
    }

    public void setStep(@Null TurnStep step) {
        this.step = step;
    }

    public Optional<TurnMessage> currentTurnMessage() {
        return current == -1 ? Optional.empty() : Optional.of(new TurnMessage(current));
    }

    private void advance() {
        TreeSet<Integer> ids = new TreeSet<>(connected.get());
        if (ids.isEmpty()) {
            current = -1;
            return;
        }
        Integer next = ids.higher(current);
        startTurn(next != null ? next : ids.first());
    }

    private void startTurn(int id) {
        current = id;
        step = TurnStep.SHIFTING;
        onTurn.accept(id);
    }
}
