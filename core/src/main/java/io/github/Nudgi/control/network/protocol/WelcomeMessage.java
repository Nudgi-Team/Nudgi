package io.github.Nudgi.control.network.protocol;

public final class WelcomeMessage extends Message {
    private final int id;

    public WelcomeMessage(int newId) {
        super(SourceContext.SERVER);
        id = newId;
    }

    WelcomeMessage(SourceContext source, String[] args) {
        this(Integer.valueOf(args[0]));
    }

    public int getNewID() {
        return id;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { String.valueOf(id) };
    }
}
