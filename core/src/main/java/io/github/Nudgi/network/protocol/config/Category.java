package io.github.Nudgi.network.protocol.config;

import java.util.Arrays;

import io.github.Nudgi.network.protocol.*;

/**
 * Represents the Category, or type, of a particular Message.
 * <p>
 * All concrete subclasses of Message must register their unique class token here,
 * paired with an arbitrary enum name.
 */
public enum Category {
    WELCOME (WelcomeMessage.class),
    BOARD (BoardMessage.class),
    PLAYER_MOVED (PlayerMovedMessage.class),
    SHIFT (ShiftMessage.class),
    FULL (FullNotification.class),
    WIN (WinMessage.class),
    TURN (TurnMessage.class),
    STEP (StepMessage.class),
    START (StartNotification.class),
    NEW_PLAYER (NewPlayerMessage.class);

    private final Class<? extends Message> messageType;

    private Category(Class<? extends Message> type) {
        messageType = type;
    }

    public Class<? extends Message> getAssociatedType() {
        return messageType;
    }

    public static Category find(Class<? extends Message> byType) {
        return Arrays.stream(values())
            .filter(cat -> cat.messageType == byType)
            .findFirst()
            .orElseThrow(() -> new Message.IllegalMessageException("error: missing Category definition for " + byType));
    }
}
