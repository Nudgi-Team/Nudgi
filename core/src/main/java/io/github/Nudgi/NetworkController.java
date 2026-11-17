package io.github.Nudgi;

import io.github.Nudgi.network.protocol.*;

/**
 * 
 * @java 21
 */
public class NetworkController {
    private final MessageHandler handler;
    private final int id;
    private volatile Object lock;


    /**
     * Constructs a new NetworkController with the given handler and a default id of 0.
     * @param handler the handler for network events.
     */
    public NetworkController(MessageHandler handler) {
        this.handler = handler;
        id = 0;
    }

    /**
     * Constructs a new NetworkController with the given handler and id.
     * @param handler the handler for network events.
     * @param controllerID the id associated with this controller.
     */
    public NetworkController(MessageHandler handler, int controllerID) {
        this.handler = handler;
        this.id = controllerID;
    }

    /**
     * Sets the current lock for this controller. If a lock is set, calls to {@link #fire(Message)}
     * will be synchronized on it.
     * @param lock the new lock. Pass null to remove the lock from this controller.
     */
    public void synchronize(Object lock) {
        this.lock = lock; 
    }

    /**
     * Fires an event on the MessageHandler tied to this instance,
     * triggering the corresponding method based on the type
     * of Message received.
     * <p>
     * If a lock is present, calls to this method are synchronized on it.
     * @param msg the Message to use.
     */
    public void fire(Message msg) {
        Object current = lock;
        if (current != null) {
            synchronized (current) {
                dispatch(msg);
            }
        }
        else {
            dispatch(msg);
        }
    }

    private void dispatch(Message msg) {
        switch(msg) {
            case null                   -> System.out.println("null message - no effect");
            case PlayerMovedMessage m   -> handler.onPlayerMoved(id, m);
            case BoardMessage m         -> handler.onBoard(id, m);
            case ShiftMessage m         -> handler.onShift(id, m);
            case FullNotification _     -> handler.onFull(id);
            case StartNotification _    -> handler.onStart(id);
            case StepMessage m          -> handler.onStep(id, m);
            case TurnMessage m          -> handler.onTurn(id, m);
            case WelcomeMessage m       -> handler.onWelcome(id, m);
            case WinMessage m           -> handler.onWin(id, m);
            case NewPlayerMessage m     -> handler.onNewPlayer(id, m);
        }
    }
}
