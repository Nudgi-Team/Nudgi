package io.github.Nudgi.entities;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * A trivial implementation of Entity that can be used when 
 * identity semantics aren't required.
 * <p>
 * This class is designed for entities where, when clicked, the action performed doesn't depend on
 * knowing which button is clicked, only <i>that</i> it was clicked.
 * <p>
 * No identifier is used.
 * 
 * 
 * @param <A> actor type
 * @apiNote This class includes an {@link #addListener(Runnable...)} method and a {@link #TrivialEntity(A, Runnable...)} 
 * constructor, which allow you to use methods with no arguments as callbacks.
 * @see Entity
 * @deprecated Entity no longer has identity semantics, making this class obsolete.
 * Uses of this class may be replaced by the
 * {@link io.github.Nudgi.ui.OnClickListener OnClickListener} class.
 */
@Deprecated(forRemoval = true)
public class TrivialEntity<A extends Actor> extends Entity<A> {
    @Deprecated(forRemoval = true)
    public TrivialEntity(A actor) {
        super(actor);
    }

    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public TrivialEntity(A actor, Runnable... listeners) {
        this(actor);
        addListener(listeners);
    }

    @Deprecated(forRemoval = true)
    public void addListener(Runnable... listeners) {
        addListeners(listeners);
    }
}
