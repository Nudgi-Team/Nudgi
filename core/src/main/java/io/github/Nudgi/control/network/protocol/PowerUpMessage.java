package io.github.Nudgi.control.network.protocol;

import io.github.Nudgi.control.network.protocol.config.SourceContext;
import io.github.Nudgi.entities.PowerUp;

public final class PowerUpMessage extends Message {
    private final PowerUp powerUp;
    private final int userId;
    private final PowerUpAction action;

    public PowerUpMessage(PowerUp powerUp, int userId, PowerUpAction action) {
        super(SourceContext.ANY);
        this.powerUp = powerUp;
        this.userId = userId;
        this.action = action;
    }

    PowerUpMessage(SourceContext source, String[] args) {
        this(PowerUp.valueOf(args[0]), Integer.parseInt(args[1]), PowerUpAction.valueOf(args[2]));
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public int getUserId() {
        return userId;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { powerUp.name(), String.valueOf(userId), action.name() };
    }

    public enum PowerUpAction {
        COLLECT,
        ACTIVATE
    }
}
