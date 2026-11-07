package io.github.Nudgi.ui.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class StageManager {
    public static final Viewport VIEW;
    public static final OrthographicCamera CAMERA;
    public static final Stage STAGE;
    static {
        float windowX = Gdx.graphics.getWidth();
        float windowY = Gdx.graphics.getHeight();
        CAMERA = new OrthographicCamera(windowX, windowY);
        VIEW = new FitViewport(windowX, windowY, CAMERA);
        STAGE = new Stage(VIEW);
        CAMERA.setToOrtho(false);
    }
    
    private static final List<Consumer<Mode>> listeners = new ArrayList<>();
    private static Mode mode;

    private StageManager() {}

    private static void runListener(Consumer<Mode> listener) {
        listener.accept(mode);
    }

    public static void setStageMode(Mode type) {
        STAGE.clear();
        mode = type;
        listeners.forEach(StageManager::runListener);
    }

    public static Mode getStageMode() {
        return mode;
    }

    /**
     * Registers a listener that runs when the Mode is switched. 
     * The mode switched into is passed in as an argument to the provided method.
     * The listener is also ran immediately with the current mode, if one is active.
     * @param listener the method to be called.
     */
    public static void onModeChanged(Consumer<Mode> listener) {
        if (mode != null) { listener.accept(mode); }
        listeners.add(listener);
    }

    /**
     * Returns the world size of {@link #VIEW}.
     * @return A Vector2 equal to {@code (VIEW.getWorldWidth(), VIEW.getWorldHeight())}.
     * */
    public static Vector2 getWorldSize() {
        return new Vector2(VIEW.getWorldWidth(), VIEW.getWorldHeight());
    }

    public static void addActors(Actor... actors) {
        for (Actor actor: actors) {
            STAGE.addActor(actor);
        }
    }

    public enum Mode {
        LOBBY,
        GAME
    }
}
