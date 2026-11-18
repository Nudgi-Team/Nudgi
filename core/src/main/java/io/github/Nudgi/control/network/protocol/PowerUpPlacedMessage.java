package io.github.Nudgi.control.network.protocol;

import java.util.Arrays;
import java.util.stream.Stream;

import io.github.Nudgi.entities.PowerUp;
import io.github.Nudgi.entities.PowerUpTileInfo;

public final class PowerUpPlacedMessage extends Message {
    private final PowerUpTileInfo[] placements;

    public PowerUpPlacedMessage(PowerUpTileInfo[] placements) {
        super(SourceContext.ANY);
        this.placements = placements.clone();
    }

    PowerUpPlacedMessage(SourceContext source, String[] args) {
        super(source);
        placements = new PowerUpTileInfo[args.length/2];
        for (int i = 0; i < args.length; i += 2) {
            PowerUp powerUp = PowerUp.valueOf(args[i]);
            int onTile = Integer.parseInt(args[i+1]);
            placements[i/2] = new PowerUpTileInfo(powerUp, onTile);
        }
    }

    public PowerUpTileInfo[] getPlacements() {
        return placements.clone();
    }

    @Override 
    protected String[] serialized() {
        return Arrays.stream(placements)
            .flatMap(info -> Stream.of(info.powerUp().name(), info.onTileID()))
            .map(String::valueOf)
            .toArray(String[]::new);
    }
}
