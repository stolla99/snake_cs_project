package src.mainclasses;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;
import src.gameclasses.Direction;
import src.gameclasses.Level;
import src.gameclasses.Settings;
import src.utilityclasses.AudioPlayer;

/**
 * The GameBoard is responsible for keeping track everything that happens on the field. The board
 * itself is represented by a 2-dimensional Integer Array filled with numbers coresponding to their
 * ingame counterparts.
 */
public class GameBoard {

    /*
     *  -MAIN VARIABLES-
     */

    // Game

    /**
     * Integer values describing snake parts.
     */

    public static int TAIL_UP = 0;
    public static int TAIL_DOWN = 1;
    public static int TAIL_RIGHT = 2;
    public static int TAIL_LEFT = 3;
    public static int HEAD_UP = 4;
    public static int HEAD_DOWN = 5;
    public static int HEAD_RIGHT = 6;
    public static int HEAD_LEFT = 7;
    public static int VERTICAL_UP = 10;
    public static int VERTICAL_DOWN = 11;
    public static int HORIZONTAL_LEFT = 12;
    public static int HORIZONTAL_RIGHT = 13;
    public static int CORNER_NORTH_EAST = 20;
    public static int CORNER_EAST_NORTH = 21;
    public static int CORNER_EAST_SOUTH = 22;
    public static int CORNER_SOUTH_EAST = 23;
    public static int CORNER_SOUTH_WEST = 24;

    /*
     *  -STATE INTEGERS-
     */
    public static int CORNER_WEST_SOUTH = 25;
    public static int CORNER_WEST_NORTH = 26;
    public static int CORNER_NORTH_WEST = 27;
    /**
     * Relative positions of connected snake parts.
     */
    private static int BELOW = 0;
    private static int ABOVE = 1;
    private static int LEFT = 2;
    private static int RIGHT = 3;
    private final String game_over_sound = "hurt.wav";
    private final String eating_apple_sound = "eating.wav";
    public Level level;
    // Indexes
    public Integer random_index_for_fruit;
    public Integer length_index_selector;
    /**
     * Board int to ingame object:
     * <p>
     * 0 : empty 1 : snake 2 : wall 3 : apple
     */
    protected Integer[][] game_board;
    protected List<Integer> copy_gameBoard;
    /**
     * Represent snake as list of integer coordinates.
     */
    private CopyOnWriteArrayList<Integer[]> snake_coordinates;
    // Audio
    private AudioPlayer audio_player;
    // Values
    private int width;
    private int height;
    private int curr_pos_y;
    private int curr_pos_x;
    private int points;
    private int steps_done;
    private int threshold;
    private int num_apples;

    public GameBoard(Settings settings, AudioPlayer audio_player, Level level) {
        this.audio_player = audio_player;
        this.threshold = settings.num_rect_x + settings.num_rect_y;
        this.width = settings.num_rect_x;
        this.height = settings.num_rect_y;
        this.game_board = level.game_board;
        snake_coordinates = new CopyOnWriteArrayList<>();
        snake_coordinates.add(level.head);
        snake_coordinates.add(level.tail);
        this.curr_pos_x = level.head[0];
        this.curr_pos_y = level.head[1];

        make_copy();

        this.points = 0;
        this.steps_done = 0;

        this.level = level;

        for (int i = 0; i < settings.num_rect_x; i++) {
            for (int j = 0; j < settings.num_rect_y; j++) {
                if (game_board[j][i] == 3) {
                    num_apples++;
                }
            }
        }
        generate_apple();
    }

    private void make_copy() {
        copy_gameBoard = new LinkedList<>();
        for (Integer[] row : game_board) {
            copy_gameBoard.addAll(Arrays.asList(row));
        }
    }

    /**
     * Returns the coordanates of the snakes head as a size 2 Integer Array.
     */
    public Integer[] get_head() {
        return snake_coordinates.get(0);
    }

    /**
     * Empties spot on the board at a specific x y coordinate.
     */
    public void set_empty(int x, int y) {
        game_board[y][x] = 0;
    }

    /**
     * Gun shots can cut the snake at a certain spot and remove the points gained for the cut part.
     */
    public void slice_snake(int x, int y) {
        var ref = new Object() {
            boolean found = false;
        };
        snake_coordinates.removeIf(integers -> {
            if (!ref.found && integers[0] == x && integers[1] == y) {
                ref.found = true;
                game_board[integers[1]][integers[0]] = 0;
                return true;
            } else if (ref.found) {
                game_board[integers[1]][integers[0]] = 0;
                return true;
            } else {
                return false;
            }
        });
        points = snake_coordinates.size() - 1;
    }

    /**
     * Returns the state of a spot at a specific x y coordinate on the board.
     */
    public int get_status(int x, int y) {
        return game_board[y][x];
    }

    /**
     * Returns the type of the snake part at position x y, i.e. the directional information. If the
     * given x y coordinate is not on the spot it returns -1.
     */
    public int get_type(int x, int y) {
        if (get_status(x, y) != 1) {
            return -1;
        }
        Integer[] pre = null;
        Integer[] post = null;
        for (int i = 0; i < snake_coordinates.size(); i++) {
            if (snake_coordinates.get(i)[0] % width == x
                && snake_coordinates.get(i)[1] % height == y) {
                if (snake_coordinates.size() != 1) {
                    if (i == 0) {
                        pre = snake_coordinates.get(i + 1);
                    } else if (i == snake_coordinates.size() - 1) {
                        post = snake_coordinates.get(i - 1);
                    } else {
                        post = snake_coordinates.get(i - 1);
                        pre = snake_coordinates.get(i + 1);
                    }
                }
            }
        }

        Integer[] origin = new Integer[]{x, y};
        if (pre == null && post == null) {
            // cannot happen since the snake has always the length 2
            return -2;
        } else {
            if (pre == null) {
                return compare(origin, post);
            } else if (post == null) {
                return 4 + compare(origin, pre);
            } else {
                int pre_stat = compare(origin, pre);
                int post_stat = compare(origin, post);
                if (pre_stat == ABOVE && post_stat == BELOW) {
                    return VERTICAL_DOWN;
                } else if (pre_stat == BELOW && post_stat == ABOVE) {
                    return VERTICAL_UP;
                } else if (pre_stat == RIGHT && post_stat == LEFT) {
                    return HORIZONTAL_LEFT;
                } else if (pre_stat == LEFT && post_stat == RIGHT) {
                    return HORIZONTAL_RIGHT;
                } else if (pre_stat == ABOVE && post_stat == RIGHT) {
                    return CORNER_NORTH_EAST;
                } else if (pre_stat == RIGHT && post_stat == ABOVE) {
                    return CORNER_EAST_NORTH;
                } else if (pre_stat == RIGHT && post_stat == BELOW) {
                    return CORNER_EAST_SOUTH;
                } else if (pre_stat == BELOW && post_stat == RIGHT) {
                    return CORNER_SOUTH_EAST;
                } else if (pre_stat == BELOW && post_stat == LEFT) {
                    return CORNER_SOUTH_WEST;
                } else if (pre_stat == LEFT && post_stat == BELOW) {
                    return CORNER_WEST_SOUTH;
                } else if (pre_stat == LEFT && post_stat == ABOVE) {
                    return CORNER_WEST_NORTH;
                } else if (pre_stat == ABOVE && post_stat == LEFT) {
                    return CORNER_NORTH_WEST;
                } else {
                    return -1;
                }
            }
        }
    }

    /**
     * Gives relative position of the point to the origin.
     */
    private int compare(Integer[] origin, Integer[] point) {
        boolean opposite = false;
        if (point[1] == height - 1 && origin[1] == 0
            || origin[1] == height - 1 && point[1] == 0
            || point[0] == width - 1 && origin[0] == 0
            || origin[0] == width - 1 && point[0] == 0) {
            opposite = true;
        }
        if (point[1] > origin[1]) {
            return (!opposite) ? BELOW : ABOVE;
        } else if (point[1] < origin[1]) {
            return (!opposite) ? ABOVE : BELOW;
        } else {
            if (point[0] < origin[0]) {
                return (!opposite) ? LEFT : RIGHT;
            } else if (point[0] > origin[0]) {
                return (!opposite) ? RIGHT : LEFT;
            } else {
                return -3;
            }
        }
    }

    private void generate_apple() {
        if (num_apples == 0) {
            Random rand = new Random();
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            while (game_board[y][x] != 0) {
                x = rand.nextInt(width);
                y = rand.nextInt(height);
            }
            game_board[y][x] = 3;
            num_apples++;
            if (random_index_for_fruit != null && length_index_selector != null) {
                this.random_index_for_fruit = (int) (Math.random() * length_index_selector);
            }
        }
    }

    /**
     * Resets the gameBoard for a new game.
     */
    public void reset_game() {
        this.curr_pos_x = level.head[0];
        this.curr_pos_y = level.head[1];
        snake_coordinates = new CopyOnWriteArrayList<>();
        Iterator<Integer> copy_iterator = copy_gameBoard.iterator();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                game_board[y][x] = copy_iterator.next();
            }
        }
        snake_coordinates.add(level.head);
        snake_coordinates.add(level.tail);
        game_board[level.head[1]][level.head[0]] = 1;
        game_board[level.tail[1]][level.tail[0]] = 1;
        num_apples = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (game_board[j][i] == 3) {
                    num_apples++;
                }
            }
        }
        generate_apple();
        this.points = 0;
        this.steps_done = 0;
    }

    /**
     * When an apple was eaten the snake gets longer.
     */
    public void elongate_snake(int x, int y, src.gameclasses.Direction direction_current) {
        switch (direction_current) {
            case DOWN:
                curr_pos_y++;
                curr_pos_y = curr_pos_y % height;
                break;
            case UP:
                curr_pos_y--;
                if (curr_pos_y < 0) {
                    curr_pos_y = height - 1;
                }
                break;
            case LEFT:
                curr_pos_x--;
                if (curr_pos_x < 0) {
                    curr_pos_x = width - 1;
                }
                break;
            case RIGHT:
                curr_pos_x++;
                curr_pos_x = curr_pos_x % width;
                break;
        }
        snake_coordinates.add(0, new Integer[]{curr_pos_x, curr_pos_y});
        game_board[y][x] = 0;
        game_board[curr_pos_y][curr_pos_x] = 1;
        num_apples--;
        generate_apple();
        points++;
    }

    public int getPoints() {
        return this.points;
    }

    /**
     * Attempts the movement of the snake in a direction, depending on what type of spot the snake
     * moves to. Returns a boolean whether the the snake successfully (true) moved or it died
     * (false).
     */
    public boolean try_movement(Direction direction_current) {
        switch (direction_current) {
            case DOWN:
                curr_pos_y++;
                curr_pos_y = curr_pos_y % height;
                break;
            case UP:
                curr_pos_y--;
                if (curr_pos_y < 0) {
                    curr_pos_y = height - 1;
                }
                break;
            case LEFT:
                curr_pos_x--;
                if (curr_pos_x < 0) {
                    curr_pos_x = width - 1;
                }
                break;
            case RIGHT:
                curr_pos_x++;
                curr_pos_x = curr_pos_x % width;
                break;
        }
        steps_done++;
        if (steps_done > threshold && num_apples == 1) {
            outer:
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (game_board[j][i] == 3) {
                        num_apples--;
                        game_board[j][i] = 0;
                        generate_apple();
                        break outer;
                    }
                }
            }
            generate_apple();
            steps_done = 0;
            System.err.println("@try_movement: reset apple");
        }
        switch (game_board[curr_pos_y][curr_pos_x]) {
            case 0:
                snake_coordinates.add(0, new Integer[]{curr_pos_x, curr_pos_y});
                Integer[] to_be_dropped = snake_coordinates.remove(snake_coordinates.size() - 1);
                Objects.requireNonNull(to_be_dropped);
                game_board[to_be_dropped[1]][to_be_dropped[0]] = 0;
                game_board[curr_pos_y][curr_pos_x] = 1;
                return true;
            case 1:
            case 2:
                SwingUtilities.invokeLater(() -> {
                    try {
                        audio_player.play_sound(game_over_sound);
                    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                        e.printStackTrace();
                    }
                });
                return false;
            case 3:
                SwingUtilities.invokeLater(() -> {
                    try {
                        audio_player.play_sound(eating_apple_sound);
                    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                        e.printStackTrace();
                    }
                });
                snake_coordinates.add(0, new Integer[]{curr_pos_x, curr_pos_y});
                game_board[curr_pos_y][curr_pos_x] = 1;
                steps_done = 0;
                num_apples--;
                generate_apple();
                points++;
                return true;
            default:
                System.err.println("@try_movement: entered default case");
                return false;
        }
    }
}