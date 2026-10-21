package io.github.Nudgi.entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


/**
 * Abstract class containing methods faciliating
 * responses to clicks detected on the given Actor, as well as resizing operations.
 * Responses are added via IdentifyingListeners,
 * which allow for the identification of the entity that was clicked.
 * <p>
 * @param <T> identifier type, or {@link Void} for entities without identifiers
 * @param <A> actor type
 */
public abstract class Entity<T, A extends Actor> {
    private final List<IdentifyingListener<T>> listeners = new ArrayList<>();
    private A actor;
    private T id;
    private boolean paused;

    protected Entity(A actor, T id) {
        this.actor = actor;
        this.id = id;
        actor.addListener(new ClickDetector());
    }

    protected Entity(A actor, T id, IdentifyingListener<T> listener) {
        this(actor, id);
        addListener(listener);
    }

    protected Entity(A actor, T id, List<IdentifyingListener<T>> listeners) {
        this(actor, id);
        addListeners(listeners);
    }

    public A getActor() {
        return actor;
    }

    public T getID() {
        return id;
    }

    public void addListener(IdentifyingListener<T> listener) {
        listeners.add(listener);
    }

    public void addListeners(List<IdentifyingListener<T>> listeners) {
        this.listeners.addAll(listeners);
    }

    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Sets the pause status for this entity.
     * While paused, the entity will no longer respond to clicks,
     * but existing listeners are not removed.
     * @param flag true to pause listeners on this entity.
     */
    public void setPaused(boolean flag) {
        paused = flag;
    }

    public boolean isPaused() {
        return paused;
    }

    public Vector2 getPosition() {
        return new Vector2(actor.getX(), actor.getY());
    }

    public void setPosition(Vector2 v) {
        actor.setPosition(v.x, v.y);
    }

    public Vector2 getOrigin() {
        return new Vector2(actor.getOriginX(), actor.getOriginY());
    }

    public Vector2 getScale() {
        return new Vector2(actor.getScaleX(), actor.getScaleY());
    }

    public Vector2 getSize() {
        return new Vector2(actor.getWidth(), actor.getHeight());
    }
    
    @Override 
    public String toString() {
        return String.format("ACTOR %s with ID %s:\n", actor.toString(), id.toString())
            + String.format("position: %s\n", getPosition())
            + String.format("origin: %s\n", getOrigin())
            + String.format("scale: %s", getScale())
            + String.format("size: %s", getSize());
    }

    private class ClickDetector extends ClickListener {
        @Override 
        public void clicked(InputEvent event, float x, float y) { 
            if (paused) { return; }
            for (IdentifyingListener<T> listener: listeners) {
                listener.onClick(id);
            }
        }
    }
}
