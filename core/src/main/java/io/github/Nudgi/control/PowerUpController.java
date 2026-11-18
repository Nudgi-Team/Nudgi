package io.github.Nudgi.control;

import io.github.Nudgi.Board;
import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PowerUp;
import io.github.Nudgi.ui.UIManager.GameUI;

public class PowerUpController {
    private final Board board; 
    private final GameUI ui;

    public PowerUpController(Board board, GameUI ui) {
        this.board = board;
        this.ui = ui;
    }

    private void powerup1(Player user) {
        // TODO do something
    }

    private void powerUp2(Player user) {
        // TODO do something
    }

    private void powerUp3(Player user) {
        // TODO do something
    }

    public void activatePowerup(Player user, PowerUp powerUp) {
        switch(powerUp) {
            case POWER_UP1 -> powerup1(user); 
            case POWER_UP2 -> powerUp2(user);
            case POWER_UP3 -> powerUp3(user);
        }
    }
}
