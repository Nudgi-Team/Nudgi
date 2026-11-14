package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.SourceContext;

public final class TurnMessage extends Message {
    private final int turnID;

    public TurnMessage(int newId) {
        super(SourceContext.SERVER);
        this.turnID = newId;
    }

    TurnMessage(SourceContext source, String[] args) {
        this(Integer.valueOf(args[0]));
    }

    public int getTurnPlayerID() {
        return turnID;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { String.valueOf(turnID) };
    }
}
