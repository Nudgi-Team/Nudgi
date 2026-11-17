package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.SourceContext;

public final class PlayerMovedMessage extends Message {
    private final Contextual<Integer> playerID = new Contextual<>(SourceContext.SERVER);
    private final int toTileID;

    /**
     * Constructs a PlayerMovedMessage {@link Message.SourceContext#SERVER originating from}
     * the server.
     * @param toTileID the tileID the player moved to.
     * @param playerID the playerID that moved.
     */
    public PlayerMovedMessage(int toTileID, int playerID) {
        super(SourceContext.SERVER);
        this.toTileID = toTileID;
        this.playerID.set(playerID);
    }

    /**
     * Constructs a PlayerMovedMessage {@link Message.SourceContext#CLIENT originating from}
     * the client.
     * @param toTileID the tileID the client is moving to.
     */
    public PlayerMovedMessage(int toTileID) {
        super(SourceContext.CLIENT);
        this.toTileID = toTileID;
    }

    private PlayerMovedMessage(SourceContext source, String[] args) {
        super(source);
        toTileID = Integer.parseInt(args[0]);
        playerID.contextualSet(() -> Integer.parseInt(args[1]));
    }

    /**
     * Returns the PlayerID associated with this message.
     * Only valid for messages with {@link SourceContext#SERVER}.
     * @return the PlayerID within this message.
     * @throws ContextMismatchException if called on a message with {@link SourceContext#CLIENT}
     */
    public int getPlayerID() {
        return playerID.get();
    }

    public int getTileID() {
        return toTileID;
    }

    @Override 
    public String[] serialized() {
        return new String[] { 
            String.valueOf(toTileID),
            playerID.serialize(String::valueOf)
        };
    }
}
