package io.github.Nudgi.network.protocol.config;

 /**
 * Represents the source context of a message, or where this message came from.
 * The server receives messages sourced from clients, and vice versa.
 * <p>
 * Used by subclasses of Message for conditional logic in asymmetric, bidirectional messages.
 */
public enum SourceContext {
    SERVER,
    CLIENT,
    /**
     * Indicates the message may be sent from either side, with the exact
     * same kind of information.
     */
    ANY
}