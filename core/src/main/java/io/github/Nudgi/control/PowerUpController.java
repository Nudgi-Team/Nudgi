package io.github.Nudgi.control;

import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PowerUp;

public class PowerUpController {
    private SpecialInstruction special;

    public PowerUpController() {
    }

    /**
     * Returns the special instruction set by a power-up activation, if any.
     * Afterwards, clears the active special instruction from this controller.
     * @return The special instruction currently set, which may be null.
     */
    public SpecialInstruction getSpecialInstruction() {
        SpecialInstruction current = special;
        special = null;
        return current;
    }

    private void speedBoost(Player user) {
        System.out.println("Power up 1 activated!");
        special = new SpeedBoost(2);
    }

    private void powerUp2(Player user) {
        System.out.println("Power up 2 activated!");
        // TODO do something
    }

    private void powerUp3(Player user) {
        System.out.println("Power up 3 activated!");
        // TODO do something
    }

    /**
     * Activates the desired power-up for the given user.
     * After calling this method, check the {@link #getSpecialInstruction() special instruction}
     * to see if additional actions are requested.
     * @param user the player using the power-up.
     * @param powerUp the power-up being used.
     */
    public void activatePowerup(Player user, PowerUp powerUp) {
        switch(powerUp) {
            case SPEED_BOOST -> speedBoost(user); 
            case POWER_UP2 -> powerUp2(user);
            case POWER_UP3 -> powerUp3(user);
        }
    }

    public sealed interface SpecialInstruction {}

    public record SpeedBoost(int boostedBy) implements SpecialInstruction {}
}
