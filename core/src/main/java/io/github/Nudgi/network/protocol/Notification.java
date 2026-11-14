package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.Category;
import io.github.Nudgi.network.protocol.config.SourceContext;

/**
 * A Notification is a subtype of Message used when the message
 * does not contain a payload, thus being "notification-only."
 * <p>
 * The serialized() method comes pre-implemented, and this Message is automatically
 * constructed with a SourceContext of {@link SourceContext#ANY}.
 * <p>
 * For more complex messages, extend {@link Message} instead.
 * <p>
 * Remember that your concrete {@code Class} token must still be registered
 * within {@link Category}.
 * @apiNote This class allows you to have an empty body in the subclass, e.g.:
 * <pre>
 * {@code public final class MyNotification extends Notification {}}
 * </pre>
 * @see Message
 */
public abstract sealed class Notification 
extends Message 
permits FullNotification, StartNotification {
    public Notification() {
        super(SourceContext.ANY);
    }

    @Override 
    protected final String[] serialized() {
        return new String[0];
    }
}
