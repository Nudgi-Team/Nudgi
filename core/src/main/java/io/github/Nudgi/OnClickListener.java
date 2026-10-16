package io.github.Nudgi;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Used as a bridge to libGDX's {@code ClickListener} and Actor event system.
 */
public class OnClickListener extends ClickListener {
    private final int id;
    private IdentifyingListener passThrough = null;

    /**
     * Creates an empty listener associated with the given ID. 
     * After constructing this listener, you must register it with an Actor
     * for it to begin detecting clicks. 
     * Then, use the {@code setListener} method to define what happens on a click.
     * Only one listener may be registered at a time for each {@link OnClickListener} instance.
     * @param id
     * @see #setListener(IdentifyingListener)
     */
    public OnClickListener(int id) {
        this.id = id;
    }

    /**
     * Defines what happens when this ClickListener detects a click on the attached Actor.
     * @param listener
     */
    public void setListener(IdentifyingListener listener) {
        passThrough = listener;
    }

    @Override 
    public void clicked(InputEvent event, float x, float y) { 
        if (passThrough != null) {
            passThrough.onClick(id);
        }
    }
}
