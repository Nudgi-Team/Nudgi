package io.github.Nudgi.ui;

import java.util.Objects;
import java.util.function.Consumer;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * A stateful ClickListener that allows for functional interface semantics
 * when attaching hooks. This allows behavior to be injected dynamically
 * much easier.
 * @see OnInputListener
 */
public class OnClickListener extends ClickListener {
    private static final Consumer<ClickEvent> DEFAULT_ACTION = _ -> {};

    private Consumer<ClickEvent> action;

    /**
     * Creates a new OnClickListener with a default no-op action
     * and a ClickType of {@link ClickType#LEFT}.
     */
    public OnClickListener() {
        this(DEFAULT_ACTION);
    }

    /**
     * Creates a new OnClickListener with the defined
     * action and a default ClickType of {@link ClickType#LEFT}.
     * @param action the action to perform when a left click is detected.
     */
    public OnClickListener(Consumer<ClickEvent> action) {
        this(action, ClickType.LEFT);
    }

    /**
     * Creates a new OnClickListener with a default no-op action
     * and the provided ClickType.
     * @param button the ClickType that triggers the action in this listener.
     */
    public OnClickListener(ClickType button) {
        this(DEFAULT_ACTION, button);
    }

    /**
     * Creates a new OnClickListener with the defined
     * action and ClickType.
     * @param action the action to perform when the defined ClickType is detected.
     * @param button the ClickType that triggers the action in this listener.
     */
    public OnClickListener(Consumer<ClickEvent> action, ClickType button) {
        super(button.value());
        this.action = Objects.requireNonNull(action);
    }

    /**
     * Replaces the current action with the provided one.
     * @param newAction the new action to replace the previous.
     * @return This object, for chaining.
     * @see #chainAction(Consumer)
     * @see #resetAction()
     */
    public OnClickListener switchAction(Consumer<ClickEvent> newAction) {
        action = Objects.requireNonNull(newAction);
        return this;
    }

    /**
     * Chains an additional action to the one currently in use, which
     * happens immediately after the existing one is triggered.
     * @param then the additional action.
     * @return This object, for chaining.
     * @see #switchAction(Consumer)
     */
    public OnClickListener chainAction(Consumer<ClickEvent> then) {
        Objects.requireNonNull(then);
        action = (action == DEFAULT_ACTION) ? then : action.andThen(then);
        return this;
    }

    /**
     * Returns the action currently set on this listener.
     * @return the current action.
     */
    public Consumer<ClickEvent> getAction() {
        return action;
    }

    /**
     * Resets the current action to a default no-op action.
     */
    public void resetAction() {
        action = DEFAULT_ACTION;
    }

    /**
     * Triggers a click event, activating the current action with
     * the passed in parameters.
     * 
     * @param event
     * @param x
     * @param y
     * @apiNote This method is called internally by the LibGDX engine - 
     * see {@link #switchAction(Consumer)} or {@link #chainAction(Consumer)}
     * to register event hooks.
     */
    @Override 
    public void clicked(InputEvent event, float x, float y) {
        action.accept(new ClickEvent(event, x, y));
    }

    public enum ClickType {
        LEFT (Buttons.LEFT),
        RIGHT (Buttons.RIGHT),
        FORWARD (Buttons.FORWARD),
        MIDDLE (Buttons.MIDDLE),
        BACK (Buttons.BACK);

        private final int value;

        private ClickType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public record ClickEvent(InputEvent event, float x, float y) {}
}
