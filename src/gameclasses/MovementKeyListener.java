package src.gameclasses;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import src.mainclasses.GameEnvironment;

/**
 * KeyListener that handles inputs relevant to snake movement.
 */
public class MovementKeyListener implements KeyListener {

    private GameEnvironment game_environment;
    private boolean enabled;

    public MovementKeyListener(GameEnvironment game_environment) {
        this.game_environment = game_environment;
        this.enabled = true;
    }

    /**
     * Checks if directional input is an executable direction.
     */
    private boolean is_legal_move(Direction comp1, Direction comp2) {
        return game_environment.direction_current.equals(comp1)
            || game_environment.direction_current
            .equals(comp2);
    }

    public void set_enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    /**
     * Allows one legal directional input that is passed through to gameEnvironment.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (enabled) {
            enabled = false;
            boolean left_right = is_legal_move(Direction.LEFT, Direction.RIGHT);
            boolean up_down = is_legal_move(Direction.UP, Direction.DOWN);
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    if (left_right) {
                        game_environment.direction_current = Direction.DOWN;
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (left_right) {
                        game_environment.direction_current = Direction.UP;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (up_down) {
                        game_environment.direction_current = Direction.LEFT;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (up_down) {
                        game_environment.direction_current = Direction.RIGHT;
                    }
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
