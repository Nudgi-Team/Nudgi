package io.github.Nudgi.entities;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * A trivial implementation of Entity that can be used when 
 * further specialization isn't required. No identifier is used.
 * @param <A> actor type
 * @see Entity
 */
public class TrivialEntity<A extends Actor> extends Entity<Void, A> {
    public TrivialEntity(A actor) {
        super(actor, null);
    }

    public TrivialEntity(A actor, IdentifyingListener<Void> listener) {
        super(actor, null, listener);
    }

    public TrivialEntity(A actor, List<IdentifyingListener<Void>> listeners) {
        super(actor, null, listeners);
    }
}
