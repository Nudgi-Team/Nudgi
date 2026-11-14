package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.SourceContext;

public final class NewPlayerMessage extends Message {
    private final int newPlayerID;

    public NewPlayerMessage(int newPlayerId) {
        super(SourceContext.SERVER);
        this.newPlayerID = newPlayerId;
    }

    NewPlayerMessage(SourceContext source, String[] args) {
        this(Integer.parseInt(args[0]));
    }

    public int getNewPlayerID() {
        return newPlayerID;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { String.valueOf(newPlayerID) };
    }
}
