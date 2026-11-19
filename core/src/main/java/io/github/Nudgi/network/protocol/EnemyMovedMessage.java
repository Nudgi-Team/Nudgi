package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.SourceContext;

public final class EnemyMovedMessage extends Message {
    private final Contextual<Integer> enemyID = new Contextual<>(SourceContext.SERVER);
    private final int toTileID;

    /**
     * Constructs a PlayerMovedMessage {@link Message.SourceContext#SERVER originating from}
     * the server.
     * @param toTileID the tileID the player moved to.
     * @param enemyID the playerID that moved.
     */
    public EnemyMovedMessage(int enemyID, int toTileID) {
        super(SourceContext.SERVER);
        this.toTileID = toTileID;
        this.enemyID.set(enemyID);
    }

    /**
     * Constructs a PlayerMovedMessage {@link Message.SourceContext#CLIENT originating from}
     * the client.
     * @param toTileID the tileID the client is moving to.
     */
    public EnemyMovedMessage(int toTileID) {
        super(SourceContext.CLIENT);
        this.toTileID = toTileID;
    }

    private EnemyMovedMessage(SourceContext source, String[] args) {
        super(source);
        toTileID = Integer.parseInt(args[0]);
        enemyID.contextualSet(() -> Integer.parseInt(args[1]));
    }

    /**
     * Returns the PlayerID associated with this message.
     * Only valid for messages with {@link SourceContext#SERVER}.
     * @return the PlayerID within this message.
     * @throws ContextMismatchException if called on a message with {@link SourceContext#CLIENT}
     */
    public int getEnemyID() {
        return enemyID.get();
    }

    public int getTileID() {
        return toTileID;
    }

    @Override
    public String[] serialized() {
        return new String[] {
            String.valueOf(toTileID),
            enemyID.serialize(String::valueOf)
        };
    }
}
