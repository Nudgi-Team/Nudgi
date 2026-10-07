package io.github.Nudgi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Board board;
    private GameManager game;
    private Stage uiStage;

    @Override
    public void create() {
        int marginSize = 20;
        Vector2 windowSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float boardSize = Math.min(windowSize.x, windowSize.y) - marginSize*2;

        
        batch = new SpriteBatch();
        uiStage = new Stage();
        board = new Board(new Vector2(windowSize.x/2-boardSize/2,windowSize.y/2-boardSize/2), boardSize, new Vector2(9,9));
        game = new GameManager(board, uiStage);
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        game.draw(batch);
        batch.end();
        uiStage.draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        game.dispose();
        uiStage.dispose();
    }
}
