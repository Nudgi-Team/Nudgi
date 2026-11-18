package io.github.Nudgi.control.network.protocol;

import io.github.Nudgi.Direction;
import io.github.Nudgi.control.network.protocol.config.SourceContext;
import io.github.Nudgi.entities.ShiftID;

public final class ShiftMessage extends Message {
    private final ShiftID id;

    public ShiftMessage(ShiftID shiftID) {
        super(SourceContext.ANY);
        id = shiftID;
    }

    private ShiftMessage(SourceContext source, String[] args) {
        super(source);
        Direction dir = Direction.valueOf(args[0]);
        int index = Integer.valueOf(args[1]);
        id = new ShiftID(index, dir);
    }

    public ShiftID getShiftID() {
        return id;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { id.dir().name(), String.valueOf(id.index()) };
    }
}