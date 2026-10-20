package io.github.Nudgi.entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public abstract class ClickableEntity<T, A extends Actor> {
    private A actor;
    private T id;
    private final List<IdentifyingListener<T>> listeners = new ArrayList<>();

    protected ClickableEntity(A actor, T id) {
        this.actor = actor;
        this.id = id;
        actor.addListener(new ClickDetector());
    }

    protected ClickableEntity(A actor, T id, List<IdentifyingListener<T>> listeners) {
        this(actor, id);
        addListeners(listeners);
    }

    public A getActor() {
        return actor;
    }

    public T getID() {
        return id;
    }

    public final void addListener(IdentifyingListener<T> listener) {
        listeners.add(listener);
    }

    public final void addListeners(List<IdentifyingListener<T>> listeners) {
        this.listeners.addAll(listeners);
    }

    public final void clearListeners() {
        listeners.clear();
    }

    private class ClickDetector extends ClickListener {
        @Override 
        public void clicked(InputEvent event, float x, float y) { 
            for (IdentifyingListener<T> listener: listeners) {
                listener.onClick(id);
            }
        }
    }
}
