package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.SourceContext;

public final class PlayerRespawnedMessage extends Message{
    int playerID;
    int tileID;

    public PlayerRespawnedMessage(int playerID, int tileID){
        super(SourceContext.SERVER);

        this.playerID = playerID;
        this.tileID = tileID;
    }

    private PlayerRespawnedMessage(SourceContext context, String[] args){
        super(context);

        playerID = Integer.parseInt(args[0]);
        tileID = Integer.parseInt(args[1]);
    }

    public int getPlayerID() {
        return playerID;
    }

    public int getTileID() {
        return tileID;
    }

    @Override
    protected String[] serialized() {
        return new String[] {
            String.valueOf(playerID),
            String.valueOf(tileID)
        };
    }
}
