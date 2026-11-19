package io.github.Nudgi.control.network.protocol;

public final class TreasureMessage extends Message {
    private final int collectingID;
    private final Delta delta;
    
    public TreasureMessage(int idOfCollector, Delta treasureChange) {
        super(SourceContext.ANY);
        collectingID = idOfCollector;
        delta = treasureChange;
    }

    TreasureMessage(SourceContext source, String[] args) {
        this(Integer.valueOf(args[0]), Delta.valueOf(args[1]));
    }

    public int getCollectorID() {
        return collectingID;
    }

    public Delta getTreasureCountDelta() {
        return delta;
    }

    protected String[] serialized() {
        return new String[] { String.valueOf(collectingID), delta.name() };
    }

    public enum Delta {
        COLLECT,
        LOSE; // Enemies
    }
}
