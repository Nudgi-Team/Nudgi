package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.protocol.config.SourceContext;

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
        COLLECT (-1),
        LOSE (1);

        private final int delta;

        private Delta(int deltaValue) {
            delta = deltaValue;
        }

        public int deltaValue() {
            return delta;
        }
    }
}
