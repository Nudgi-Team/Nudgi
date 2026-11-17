package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.SourceContext;

public final class WinMessage extends Message {
    private final int playerID;

    public WinMessage(int winningPlayerID) {
        super(SourceContext.ANY);
        playerID = winningPlayerID;
    }

    WinMessage(SourceContext source, String[] args) {
        this(Integer.valueOf(args[0]));
    }

    public int getWinnerID() {
        return playerID;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { String.valueOf(playerID) };
    }
}
