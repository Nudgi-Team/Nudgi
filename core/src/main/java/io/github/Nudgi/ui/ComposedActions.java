package io.github.Nudgi.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public final class ComposedActions {
    private ComposedActions() {}

    private static Action getFlickerAction(float delay, boolean start) {
        return Actions.forever(Actions.sequence(
            Actions.delay(delay, Actions.visible(start)),
            Actions.delay(delay, Actions.visible(!start))
        ));
    }
    
    /**
     * Beginning with invisible, continuously flickers the actor
     * between invisible and visible states, separated by delay.
     * @param delay the time between each transition.
     * @return the Flicker action.
     * @see #alternateFlicker(float)
     */
    public static Action flicker(float delay) {
        return getFlickerAction(delay, false);
    }

    /**
     * Similar to {@link #flicker(float)}, except it begins with visible.
     * Useful for alternating between two separate sprites.
     * @param delay the time between each transition.
     * @return the Alternate Flicker action.
     */
    public static Action alternateFlicker(float delay) {
        return getFlickerAction(delay, true);
    }
}
