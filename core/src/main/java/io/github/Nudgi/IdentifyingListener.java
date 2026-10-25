package io.github.Nudgi;

/**
 * A simplified listener for use with events requiring the identity of the caller object.
 * @see OnClickListener
 */
@FunctionalInterface 
public interface IdentifyingListener {
    public void onClick(int triggeringID);
}