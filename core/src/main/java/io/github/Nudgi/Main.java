package io.github.Nudgi;

import static io.github.Nudgi.ui.stage.StageManager.STAGE;
import static io.github.Nudgi.ui.stage.StageManager.VIEW;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.Nudgi.ui.stage.StageManager;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private Board board;
    private GameManager game;
    private NetworkController controller; 

    @Override
    public void create() {
        int marginSize = 90;
        Vector2 windowSize = StageManager.getWorldSize();
        float boardSize = Math.min(windowSize.x, windowSize.y) - marginSize*2;

        board = new Board(new Vector2(windowSize.x/2-boardSize/2,windowSize.y/2-boardSize/2), boardSize, new Vector2(9,9));
        game = new GameManager(board);
        controller = new NetworkController(game);
        game.setDispatcher(controller::fire);
        Gdx.input.setInputProcessor(STAGE);
    }

    @Override 
    public void resize(int width, int height) {
        VIEW.update(width, height);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.update();
        STAGE.getViewport().apply();
        STAGE.act();
        STAGE.draw();
    }

    @Override
    public void dispose() {
        game.dispose();
        STAGE.dispose();
    }
}
