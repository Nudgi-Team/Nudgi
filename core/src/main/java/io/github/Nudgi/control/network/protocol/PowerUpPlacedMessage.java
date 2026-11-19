package io.github.Nudgi.control.network.protocol;


import io.github.Nudgi.entities.PowerUp;
import io.github.Nudgi.entities.PowerUpTileInfo;

public final class PowerUpPlacedMessage extends Message {
    private final PowerUpTileInfo placement;

    public PowerUpPlacedMessage(PowerUpTileInfo placement) {
        super(SourceContext.ANY);
        this.placement = placement;
    }

    PowerUpPlacedMessage(SourceContext source, String[] args) {
        this(new PowerUpTileInfo(PowerUp.valueOf(args[0]), Integer.parseInt(args[1])));
    }

    public PowerUpTileInfo getPlacement() {
        return placement;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { placement.powerUp().name(), String.valueOf(placement.onTileID()) };
    }
}
