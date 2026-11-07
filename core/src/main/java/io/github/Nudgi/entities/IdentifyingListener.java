package io.github.Nudgi.entities;

/**
 * A simplified listener for use with events requiring the identity of the caller object.
 * @param <T> identifier type
 * @see Entity
 */
@FunctionalInterface 
public interface IdentifyingListener<T> {
    public void onClick(T triggeringID);
}