package io.github.Nudgi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private Board board;
    private GameManager game;
    private Stage gameStage;
    private Viewport viewport;

    @Override
    public void create() {
        gameStage = new Stage();

        int marginSize = 42;
        Vector2 windowSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float boardSize = Math.min(windowSize.x, windowSize.y) - marginSize*2;

        OrthographicCamera camera = new OrthographicCamera(windowSize.x, windowSize.y);
        camera.setToOrtho(false);

        viewport = new FitViewport(windowSize.x, windowSize.y, camera);
        gameStage.setViewport(viewport);
        board = new Board(new Vector2(windowSize.x/2-boardSize/2,windowSize.y/2-boardSize/2), boardSize, new Vector2(9,9), gameStage);
        game = new GameManager(board, gameStage);
        Gdx.input.setInputProcessor(gameStage);
    }

    @Override 
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.update();
        gameStage.getViewport().apply();
        gameStage.act();
        gameStage.draw();
    }

    @Override
    public void dispose() {
        game.dispose();
        gameStage.dispose();
    }
}
