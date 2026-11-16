package io.github.Nudgi.ui.game;

import static io.github.Nudgi.ui.StageManager.STAGE;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import com.badlogic.gdx.math.Vector2;

import io.github.Nudgi.Board;
import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.entities.ShiftID;

public class BoardUI {
    private IntConsumer tileListener = _ -> {};
    private Consumer<ShiftID> arrowListener = _ -> {};

    public BoardUI(Board board) {        
        Player[] players = PlayerManager.getPlayers();
        for (Player player: players) {
            Vector2 boardCornerPos = board.cornerToGrid(player.getStartingCorner());
            Vector2 worldPos = board.toWorldPos(boardCornerPos);
            float tileSize = board.getTileSize();

            player.setPosition(worldPos);
            player.setSizeSquare(tileSize*0.9f);
            STAGE.addActor(player.getActor());
            
            board.forEachTile((_, tile) -> STAGE.addActor(tile.getActor()));
            board.forEachArrow(arrow -> STAGE.addActor(arrow.getActor()));
            board.forEachTile((_, tile) -> tile.addListeners(() -> tileListener.accept(tile.getID())));
            board.forEachArrow(arrow -> arrow.addListeners(() -> arrowListener.accept(arrow.getID())));
        }
    }

    public void onTileClicked(IntConsumer action) {
        tileListener = action;
    }

    public void onarrowClicked(Consumer<ShiftID> action) {
        arrowListener = action;
    }
}
