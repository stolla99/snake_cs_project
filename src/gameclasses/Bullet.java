package src.gameclasses;

/**
 * Implementation of a projectile for the game mode "Gun" where the player can shoot bullets across
 * the game field. Each bullet has a position on the game field which is incremented along an axis
 * to simulate the travel of the projectile.
 */
public class Bullet {

    /**
     * Final variables depending on the level.
     */
    private final int square_size;
    private final int height;
    private final int width;
    /**
     * Only four directions are possible (UP, DOWN, LEFT, RIGHT)
     */
    private final Direction travel_dir;
    /*
     * Positions of the bullet shot by the player in the game mode with the gun.
     */
    public int x;
    public int y;
    public int origin_x;
    public int origin_y;
    /**
     * If the axis is set TRUE the y-axis is meant otherwise the x-axis. Note that a bullet can only
     * travel along the two axis.
     */
    private boolean axis;

    /**
     * Higher value increases the travel speed while a lower does the opposite. Indicates how far
     * the bullet travels each step.
     */
    private int inc;

    public Bullet(Direction curr_dir, int x, int y, Settings settings) {
        this.width = settings.field_res_width;
        this.height = settings.field_res_height;
        this.travel_dir = curr_dir;
        square_size = settings.square_size;
        this.x = x * square_size;
        this.y = y * square_size;

        switch (curr_dir) {
            case LEFT:
                axis = false;
                inc = -1;
                break;
            case RIGHT:
                this.x += square_size;
                axis = false;
                inc = 1;
                break;
            case UP:
                axis = true;
                inc = -1;
                break;
            case DOWN:
                this.y += square_size;
                axis = true;
                inc = 1;
                break;
        }
        inc *= 20;
        double random_shift = Math.random();
        if (axis) {
            this.x = this.x + (int) (random_shift * square_size);
        } else {
            this.y = this.y + (int) (random_shift * square_size);
        }
        this.origin_x = this.x;
        this.origin_y = this.y;
        if (axis) {
            this.y += inc;
        } else {
            this.x += inc;
        }
    }

    /**
     * Lets the bullet travel by incrementing one of (depending on the variable: inc) the positions
     * x, y.
     */
    public boolean travel() {
        if (axis) {
            y += inc;
            origin_y += inc;
        } else {
            x += inc;
            origin_x += inc;
        }
        return y < 0 || x < 0 || y > height || x > width;
    }

    /**
     * Method to get the current coordinates of the bullet on the game field.
     */
    public Integer[] get_game_board_coordinate() {
        Integer[] out = null;
        switch (travel_dir) {
            case LEFT:
                out = new Integer[]{(x / square_size) - 1, (y / square_size)};
                break;
            case UP:
                out = new Integer[]{(x / square_size), (y / square_size) - 1};
                break;
            case RIGHT:
            case DOWN:
                out = new Integer[]{(x / square_size), (y / square_size)};
                break;
        }
        return out;
    }
}
