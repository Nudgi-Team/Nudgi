package io.github.Nudgi;

import io.github.Nudgi.network.protocol.*;

/**
 * Provides reactions to events, alongside their message objects for
 * non-notification messages.
 * <p>
 * The methods of this interface conventionally begin with the word "on", followed
 * by the name of the Message they respond to, without the word "Message"
 * or "Notification" (e.g. PlayerMovedMessage -> onPlayerMoved). 
 */
public interface MessageHandler {
    default void onPlayerMoved(int from, PlayerMovedMessage msg) {}
    default void onFull(int from) {}
    default void onStart(int from) {}
    default void onShift(int from, ShiftMessage msg) {}
    default void onStep(int from, StepMessage msg) {}
    default void onTurn(int from, TurnMessage msg) {}
    default void onWelcome(int from, WelcomeMessage msg) {}
    default void onWin(int from, WinMessage msg) {}
    default void onBoard(int from, BoardMessage msg) {}
    default void onNewPlayer(int from, NewPlayerMessage msg) {}
}
