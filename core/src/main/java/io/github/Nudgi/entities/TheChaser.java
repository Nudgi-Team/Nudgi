package io.github.Nudgi.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.Nudgi.Board;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class TheChaser extends Enemy{

    /**
     * Constructs a new chaser enemy
     *
     * @param x             the position of the Image's bottom left corner.
     * @param y
     * @param width         the width of the Image.
     * @param height        the height of the Image.
     * @param stage         the {@link Stage} to place the Image onto.
     * @param enemyID       a unique identifier for this instance.
     */
    public TheChaser(float x, float y, float width, float height, Stage stage, int enemyID) {
        super(x, y, width, height, stage, enemyID, "TheChaser.png");
    }

    @Override
    public void move(Board board) {
        Vector2 current = board.getEnemyPosition(this);

        // Already standing on a player: stay put
        if (board.fetchTile(current).hasPlayer()) return;

        Optional<Vector2> step = board.nextStepToward(current, Tile::hasPlayer);
        if (step.isPresent()) {
            board.moveEnemy(this, step.get());
            return;
        }

        // No player reachable (walls in the way): wander randomly instead
        List<Vector2> options = board.getAdjacentPositions(current, true);
        if (options.isEmpty()) return;
        board.moveEnemy(this, options.get(new Random().nextInt(options.size())));
    }

}
