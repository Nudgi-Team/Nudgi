package io.github.Nudgi.network.protocol;

import io.github.Nudgi.network.TurnStep;
import io.github.Nudgi.network.protocol.config.SourceContext;

public final class StepMessage extends Message {
    private final TurnStep step;

    public StepMessage(TurnStep newStep) {
        super(SourceContext.SERVER);
        step = newStep;
    }

    StepMessage(SourceContext source, String[] args) {
        this(TurnStep.valueOf(args[0]));
    }

    public TurnStep getStep() {
        return step;
    }

    @Override 
    protected String[] serialized() {
        return new String[] { step.name() };
    }
}
