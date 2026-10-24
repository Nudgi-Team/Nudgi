package io.github.Nudgi.entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * A trivial implementation of Entity that can be used when 
 * identity semantics aren't required.
 * <p>
 * This class is designed for entities where, when clicked, the action performed doesn't depend on
 * knowing which button is clicked, only <i>that</i> it was clicked.
 * <p>
 * No identifier is used.
 * Instead, all IdentifyingListeners within this 
 * implementation always receive {@code null} as an argument.
 * 
 * 
 * @param <A> actor type
 * @apiNote This class includes an {@link #addListener(Runnable...)} method and a {@link #TrivialEntity(Actor, Runnable...)} 
 * constructor, which allow you to use methods with no arguments as callbacks.
 * @see Entity
 * @see IdentifyingListener
 */
public final class TrivialEntity<A extends Actor> extends Entity<Void, A> {
    public TrivialEntity(A actor) {
        super(actor, null);
    }

    public TrivialEntity(A actor, Runnable... listeners) {
        this(actor);
        addListener(listeners);
    }

    public void addListener(Runnable... listeners) {
        List<IdentifyingListener<Void>> converted = new ArrayList<>();
        for (Runnable l: listeners) {
            converted.add(_void -> l.run());
        }
        addListeners(converted);
    }
}
