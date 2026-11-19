package io.github.Nudgi.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;


/// Stateful InputListener allowing for functional interface semantics.
/// Each type of Responder can only have one listener at a time.
/// To have multiple responders for the same event, create multiple instances
/// of this class.
/// @see OnClickListener
/// @see #setResponder(Class, InputResponder)
public class OnInputListener extends InputListener {
    private final Map<Class<? extends InputResponder>, InputResponder> responders = new HashMap<>();

    /**
     * Sets the corresponding Responder. If a responder is 
     * already present for the provided type, it is replaced.
     * @param <T> the type of Responder to set.
     * @param clazz Class token of type {@code T}.
     * @param responder The responder to set.
     * @see InputResponder
     * @see #removeResponder(Class)
     */
    public <T extends InputResponder> void setResponder(Class<T> clazz, T responder) {
        responders.put(clazz, responder);
    }

    /**
     * Removes a Responder type from this listener.
     * @param <T> the type of Responder to remove.
     * @param clazz Class token of type {@code T}.
     * @see InputResponder
     * @see #clear()
     */
    public <T extends InputResponder> void removeResponder(Class<T> clazz) {
        responders.remove(clazz);
    }

    /**
     * Removes all Responders from this listener.
     * <p>
     * Note that this method does not disconnect this InputListener from the underlying Actor.
     */
    public void clear() {
        responders.clear();
    }

    private <T extends InputResponder> T getResponder(Class<T> clazz) {
        return clazz.cast(responders.get(clazz));
    }

    private <T extends InputResponder> void activateResponder(Class<T> clazz, Consumer<T> action) {
        T responder = getResponder(clazz);
        if (responder != null) {
            action.accept(responder);
        }
    }

    private <T extends InputResponder> boolean testResponder(Class<T> clazz, Predicate<T> action) {
        T responder = getResponder(clazz);
        return (responder != null) ? action.test(responder) : false;
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        activateResponder(MouseEnter.class, r -> r.respond(event, x, y, pointer, fromActor));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        activateResponder(MouseExit.class, r -> r.respond(event, x, y, pointer, toActor));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public boolean keyDown(InputEvent event, int keycode) {
        return testResponder(KeyDown.class, r -> r.respond(event, keycode));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public boolean keyTyped(InputEvent event, char character) {
        return testResponder(KeyTyped.class, r -> r.respond(event, character));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public boolean keyUp(InputEvent event, int keycode) {
        return testResponder(KeyUp.class, r -> r.respond(event, keycode));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public boolean mouseMoved(InputEvent event, float x, float y) {
        return testResponder(MouseMoved.class, r -> r.respond(event, x, y));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
        return testResponder(MouseScrolled.class, r -> r.respond(event, x, y, amountX, amountY));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return testResponder(TouchDown.class, r -> r.respond(event, x, y, pointer, button));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method should never be called directly; it is invoked
     * internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        activateResponder(TouchDragged.class, r -> r.respond(event, x, y, pointer));
    }

    /**
     * {@inheritDoc}
     * @deprecated This method is called internally by the LibGDX engine. To
     * register a listener, see {@link #setResponder(Class, InputResponder)}.
     */
    @Override
    @Deprecated
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        activateResponder(TouchUp.class, r -> r.respond(event, x, y, pointer, button));
    }

    /**
     * Base interface for responders of an InputListener.
     */
    public sealed interface InputResponder {}

    @FunctionalInterface 
    public non-sealed interface MouseEnter extends InputResponder {
        void respond(InputEvent event, float x, float y, int pointer, Actor toActor);
    }

    @FunctionalInterface 
    public non-sealed interface MouseExit extends InputResponder {
        void respond(InputEvent event, float x, float y, int pointer, Actor toActor);
    }

    /**
     * Note that returning {@code true}
     * from the underlying method 
     * will {@link com.badlogic.gdx.scenes.scene2d.Event#handle() handle} the event.
     */
    @FunctionalInterface 
    public non-sealed interface KeyUp extends InputResponder {
        boolean respond(InputEvent event, int keycode);
    }

    /**
     * Note that returning {@code true}
     * from the underlying method 
     * will {@link com.badlogic.gdx.scenes.scene2d.Event#handle() handle} the event.
     */
    @FunctionalInterface 
    public non-sealed interface KeyTyped extends InputResponder {
        boolean respond(InputEvent event, char character);
    }

    /**
     * Note that returning {@code true}
     * from the underlying method 
     * will {@link com.badlogic.gdx.scenes.scene2d.Event#handle() handle} the event.
     */
    @FunctionalInterface 
    public non-sealed interface KeyDown extends InputResponder {
        boolean respond(InputEvent event, int keycode);
    }

    /**
     * Note that returning {@code true}
     * from the underlying method 
     * will {@link com.badlogic.gdx.scenes.scene2d.Event#handle() handle} the event.
     */
    @FunctionalInterface 
    public non-sealed interface MouseMoved extends InputResponder {
        boolean respond(InputEvent event, float x, float y);
    }

    /**
     * Note that returning {@code true}
     * from the underlying method 
     * will {@link com.badlogic.gdx.scenes.scene2d.Event#handle() handle} the event.
     */
    @FunctionalInterface 
    public non-sealed interface MouseScrolled extends InputResponder {
        boolean respond(InputEvent event, float x, float y, float amountX, float amountY);
    }

    @FunctionalInterface 
    public non-sealed interface TouchUp extends InputResponder {
        void respond(InputEvent event, float x, float y, int pointer, int button);
    }

    @FunctionalInterface 
    public non-sealed interface TouchDragged extends InputResponder {
        void respond(InputEvent event, float x, float y, int pointer);
    }

    /**
     * Note that returning {@code true}
     * from the underlying method 
     * will {@link com.badlogic.gdx.scenes.scene2d.Event#handle() handle} the event.
     */
    @FunctionalInterface 
    public non-sealed interface TouchDown extends InputResponder {
        boolean respond(InputEvent event, float x, float y, int pointer, int button);
    }
}
