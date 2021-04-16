package src.gameclasses;

import java.io.Serializable;

/**
 * Stores all settings relevant values.
 */
public class Settings implements Serializable {

    // Game
    public String player;
    public int tick_speed;
    public int board_layout;

    /**
     * Gamemodes:
     * <p>
     * 0 : default 1 : gun 2 : speed
     */
    public int game_mode;

    // Music mute booleans
    public boolean in_menu_music;
    public boolean in_game_music;

    // Window and field size variables
    public String rd_num_rect;
    public String rd_window_size;
    public int square_size;
    public int num_rect_x;
    public int num_rect_y;
    public int field_res_width;
    public int field_res_height;

    /**
     * Constructor sets default values.
     */
    public Settings() {
        this.player = "";
        this.rd_window_size = "Large";
        this.rd_num_rect = "Medium";
        this.game_mode = 0;
        this.tick_speed = 160;
        this.board_layout = 0;
        this.in_game_music = true;
        this.in_menu_music = true;
        this.field_res_width = 1280;
        this.field_res_height = 720;
        this.num_rect_x = 64;
        this.num_rect_y = 36;
        this.square_size = field_res_height / num_rect_y;
    }

    /**
     * Sets field resolution and sizes of field squares.
     */
    public void set_field_res(int x, int y) {
        field_res_width = x;
        field_res_height = y;
        square_size = x / num_rect_x;
    }

    /**
     * Sets number of squares, size of squares and size name.
     */
    public void set_field_size(int x, int y) {
        num_rect_x = x;
        num_rect_y = y;
        switch (x) {
            case 128:
                rd_num_rect = "Huge";
                break;
            case 96:
                rd_num_rect = "Large";
                break;
            case 64:
                rd_num_rect = "Medium";
                break;
            case 32:
                rd_num_rect = "Small";
                break;
        }
        square_size = field_res_width / x;
    }
}