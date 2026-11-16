package io.github.Nudgi.ui;

import io.github.Nudgi.Board;
import io.github.Nudgi.ui.StageManager.Mode;
import io.github.Nudgi.ui.game.BoardUI;
import io.github.Nudgi.ui.game.TreasureUI;
import io.github.Nudgi.ui.lobby.LobbyUI;


public class UIManager {
    private LobbyUI lobby;
    private TreasureUI treasureUi; 
    private BoardUI boardUi;
    private Board board;

    public UIManager(Board board) {
        this.board = board;
        StageManager.onModeChanged(this::setMode);
    }

    public GameUI getGameUI() {
        return new GameUI(treasureUi, boardUi);
    }

    private void setMode(Mode mode) {
        switch(mode) {
            case LOBBY -> setToLobby();
            case GAME -> setToGame();
        }   
    }

    private void setToLobby() {
        lobby = new LobbyUI();
    }

    private void setToGame() {
        treasureUi = new TreasureUI();
        boardUi = new BoardUI(board);
    }

    public void onDevMode(Runnable... listeners) {
        lobby.onDevMode(listeners);
    }

    public void lobbyWaitingMode() {
        lobby.waitingMode();
    }

    public record GameUI(TreasureUI treasure, BoardUI board) {}
}
