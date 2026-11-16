package io.github.Nudgi.entities;

/**
 * A simplified listener for use with events requiring the identity of the caller object.
 * @param <T> identifier type
 * @see Entity
 * @deprecated Use the built in {@link java.util.function.Consumer Consumer}
 * functional interface instead.
 */
@FunctionalInterface 
@Deprecated(forRemoval = true)
public interface IdentifyingListener<T> {
    @Deprecated(forRemoval = true)
    public void onClick(T triggeringID);
}