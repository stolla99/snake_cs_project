package src.gameclasses;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class which describes the concept of a level with its attributes.
 */
public class Level implements Serializable {

    public String name;
    public Integer[][] game_board;

    public Boolean modifiable;

    /*
     * Snake specific variables. Initial length of the snake is always 2 (head, tail)
     */
    public Integer[] head;
    public Integer[] tail;
    public Integer direction;

    public Level(Integer[][] game_board, Integer[] head, Integer[] tail,
        Boolean modifiable, Integer direction, String name) {
        this.game_board = game_board;
        this.head = head;
        this.tail = tail;
        this.modifiable = modifiable;
        this.direction = direction;
        this.name = name;
    }

    public int get_width() {
        return game_board[0].length;
    }

    public int get_height() {
        return game_board.length;
    }

    /**
     * Default levels are written in the game_board if the string parameter is either "Walled" or
     * "Stripped". If Empty is passed nothing will be changed.
     */
    public void init_level(String level, int width, int height) {
        game_board = new Integer[height][width];
        for (Integer[] row : this.game_board) {
            Arrays.fill(row, 0);
        }
        boolean default_snake_position = true;
        int mid_x = width / 2;
        int mid_y = height / 2;
        switch (level) {
            case "Walled":
                for (int i = 0; i < width; i++) {
                    game_board[0][i] = 2;
                    game_board[height - 1][i] = 2;
                }
                for (int i = 0; i < height; i++) {
                    game_board[i][0] = 2;
                    game_board[i][width - 1] = 2;
                }
                break;
            case "Stripped":
                int third_width = width / 3;
                int third_height = height / 3;
                for (int i = 0; i < third_height; i++) {
                    game_board[third_height + i][third_width] = 2;
                    game_board[third_height + i][2 * third_width] = 2;
                }
                break;
            case "Empty":
                break;
            default:
                default_snake_position = false;
                System.err.println("@init_level: nothing changed");
                break;
        }
        if (default_snake_position) {
            tail = new Integer[]{mid_x, mid_y - 1};
            head = new Integer[]{mid_x, mid_y};
            game_board[tail[1]][tail[0]] = 1;
            game_board[head[1]][head[0]] = 1;
        }
    }
}
