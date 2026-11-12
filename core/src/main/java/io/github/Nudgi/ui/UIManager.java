package io.github.Nudgi.ui;

import io.github.Nudgi.ui.game.TreasureUI;
import io.github.Nudgi.ui.lobby.LobbyUI;
import io.github.Nudgi.ui.stage.StageManager;
import io.github.Nudgi.ui.stage.StageManager.Mode;


public class UIManager {
    private LobbyUI lobby;
    private TreasureUI treasureUi; 

    public UIManager() {
        StageManager.onModeChanged(this::setMode);
    }

    private void setMode(Mode mode) {
        switch(mode) {
            case LOBBY: setToLobby(); break;
            case GAME: setToGame(); break;
        }   
    }

    private void setToLobby() {
        lobby = new LobbyUI();
    }

    private void setToGame() {
        treasureUi = new TreasureUI();
    }

    public void onDevMode(Runnable... listeners) {
        lobby.onDevMode(listeners);
    }

    public void lobbyWaitingMode() {
        lobby.waitingMode();
    }
}
