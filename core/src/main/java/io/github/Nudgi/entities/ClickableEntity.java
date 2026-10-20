package io.github.Nudgi.entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


/**
 * Abstract class containing methods faciliating
 * responses to clicks detected on the given Actor. Responses are added via IdentifyingListeners,
 * which allow for the identification of the entity that was clicked.
 * @param <T> identifier type
 * @param <A> actor type
 */
public abstract class ClickableEntity<T, A extends Actor> {
    private final List<IdentifyingListener<T>> listeners = new ArrayList<>();
    private A actor;
    private T id;
    private boolean paused;

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

    public void addListener(IdentifyingListener<T> listener) {
        listeners.add(listener);
    }

    public void addListeners(List<IdentifyingListener<T>> listeners) {
        this.listeners.addAll(listeners);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void addToStage(Stage stage) {
        stage.addActor(actor);
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
