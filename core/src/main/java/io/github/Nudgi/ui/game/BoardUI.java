package io.github.Nudgi.ui.game;

import static io.github.Nudgi.ui.StageManager.STAGE;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import io.github.Nudgi.Board;
import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.entities.ShiftID;
import io.github.Nudgi.ui.Showable;

public class BoardUI implements Showable {
    private final Board boardState;
    private IntConsumer tileListener = _ -> {};
    private Consumer<ShiftID> arrowListener = _ -> {};

    public BoardUI(Board board) {
        boardState = board;
        board.forEachTile((_, tile) -> tile.addListeners(() -> tileListener.accept(tile.getID())));
        board.forEachArrow(arrow -> arrow.addListeners(() -> arrowListener.accept(arrow.getID())));
    }

    @Override 
    public void show() {
        for (Player player: PlayerManager.getPlayers()) {
            float tileSize = boardState.getTileSize();
            player.setSizeSquare(tileSize*0.9f);
        }
        
        boardState.forEachTile((_, tile) -> STAGE.addActor(tile.getActor()));
        boardState.forEachArrow(arrow -> STAGE.addActor(arrow.getActor()));
    }

    public void onTileClicked(IntConsumer action) {
        tileListener = action;
    }

    public void onarrowClicked(Consumer<ShiftID> action) {
        arrowListener = action;
    }
}
