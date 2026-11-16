package io.github.Nudgi.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


/**
 * Represents a clickable actor.
 * 
 * Identifiers are no longer supported.
 * 
 * @param <A> actor type
 * @apiNote Remember that for any Actor to show up, including the one within an Entity,
 * you must add it to a {@code Stage}!
 * @see TrivialEntity
 * @see Consumer
 * @see io.github.Nudgi.ui.StageManager
 */
public class Entity<A extends Actor> {
    private final List<Runnable> listeners = new ArrayList<>();
    private final A actor;
    private boolean paused;
    
    public Entity(A actor) {
        this.actor = actor;
        actor.addListener(new ClickDetector());
    }

    public Entity(A actor, Runnable... listeners) {
        this(actor);
        addListeners(listeners);
    }

    public A getActor() {
        return actor;
    }

    public final void addListeners(Runnable... listeners) {
        this.listeners.addAll(List.of(listeners));
    }

    public final void clearListeners() {
        listeners.clear();
    }

    /**
     * Sets the pause status for this entity.
     * While paused, the entity will no longer respond to clicks,
     * but existing listeners are not removed.
     * @param flag true to pause listeners on this entity. false to resume.
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

    public void setSizeSquare(float sideLength) {
        actor.setSize(sideLength, sideLength);
    }
    
    @Override 
    public String toString() {
        return String.format("ENTITY with Actor %s\n", actor.toString())
            + String.format("position: %s\n", getPosition())
            + String.format("origin: %s\n", getOrigin())
            + String.format("scale: %s\n", getScale())
            + String.format("size: %s\n", getSize());
    }

    private class ClickDetector extends ClickListener {
        @Override 
        public void clicked(InputEvent event, float x, float y) { 
            if (paused) { return; }
            listeners.forEach(Runnable::run);
        }
    }
}
