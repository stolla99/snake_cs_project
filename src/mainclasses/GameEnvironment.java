package src.mainclasses;

import static src.mainclasses.GameBoard.*;
import static src.utilityclasses.IconDrawer.set_icon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import src.gameclasses.Bullet;
import src.gameclasses.Direction;
import src.gameclasses.Level;
import src.gameclasses.MovementKeyListener;
import src.gameclasses.Settings;
import src.utilityclasses.AudioPlayer;
import src.utilityclasses.Leaderboard;
import src.utilityclasses.LeaderboardEntry;
import src.utilityclasses.LevelSerializer;
import src.utilityclasses.SettingsSerializer;

/**
 * Base class which manages the game logic and the GUI. Implements ActionListener for the main game
 * loop. Extends JFrame as a base for other JPanels, whose visibility is toggled.
 */
public class GameEnvironment extends JFrame implements ActionListener {
    /*
     *  -GUI RELATED VARIABLES-
     */

    private final MovementKeyListener controls;
    /**
     * GUI component whose purpose is to draw the core elements of game. Overrides the
     * paintComponent method of the JPanel.
     */
    public JPanel jpanel_game;
    /**
     * Direction of the snake depending on the user input. Default value is Direction.UP.
     */
    public Direction direction_current;
    private JLabel lbl_points;
    private JLabel lbl_name;
    private JPanel contentPane;
    private JToolBar toolbar;
    private JButton btn_pause;
    private JButton btn_menu;
    private JButton btn_settings;
    private JPanel tabbed_pane_settings;
    private JSlider sld_tick_speed;
    private JComboBox<String> cmb_level;
    private JButton btn_settings_apply;
    private JRadioButton rd_field_size_large;
    private JRadioButton rd_field_size_medium;
    private JRadioButton rd_field_size_small;
    private JRadioButton rd_field_size_huge;
    private JRadioButton rd_win_size_small;
    private JRadioButton rd_win_size_medium;
    private JRadioButton rd_win_size_large;
    private JRadioButton rd_win_size_huge;
    private JPanel jpanel_menu;
    private JButton btn_play;
    private JButton btn_scoreboard;
    private JButton btn_exit;
    private JButton btn_settings_menu;
    private JButton btn_apply_name;
    private JTextField txt_name;
    private JPanel jpanel_input;
    private JButton btn_about;
    private JPanel jpanel_about;
    private JLabel lbl_about;
    private JButton btn_about_back;
    private JCheckBox chk_in_game_music;
    private JCheckBox chk_in_menu_music;
    private JTextField txt_new_name;
    private JButton btn_controls;
    private JButton btn_controls_back;
    private JLabel lbl_controls;
    private JPanel jpanel_controls;
    private JPanel jpanel_scoreboard;
    private JTable table_scoreboard;
    private JButton btn_scoreboard_to_menu;
    private JScrollPane scroll_pane;
    private JButton btn_level_editor;
    private JList list_levels;
    private JRadioButton rd_mode_default;
    private JRadioButton rd_mode_gun;
    private JRadioButton rd_mode_speed;
    private JButton btn_play_again;
    private JButton btn_cancel_settings;
    private JButton btn_cancel;
    private JPanel jpanel_level_selection;
    private ButtonGroup field_size_group;
    private ButtonGroup window_size_group;

    /*
     *  -OBJECT VARIABLES-
     */
    private ButtonGroup game_mode_group;
    private String[] list_of_fruits_assets;
    // main
    private GameBoard game_board;
    private Settings settings;
    private Leaderboard leaderboard = new Leaderboard();

    // util
    private AudioPlayer audio_player;
    private String game_info;
    private CopyOnWriteArraySet<Bullet> active_bullets_set = new CopyOnWriteArraySet<>();

    // music
    private String in_game_music;
    private String in_menu_music;

    // game loop
    private Timer tick_event_caller;
    private Timer repaint_caller;
    private boolean timer_paused;

    /*
     *  -STATUS VARIABLES-
     */

    /**
     * When set to 0 SPACE has to be pressed and when set to 1 one ENTER has to be pressed in order
     * to start the game thread.
     */
    private int before_game_status;
    private int toggle_paused;
    private int toggle_settings;
    private boolean access_from_menu;
    private AtomicBoolean from_game = new AtomicBoolean(false);

    public GameEnvironment(Settings settings) {
        audio_player = new AudioPlayer();
        in_game_music = "in_game_music.wav";
        in_menu_music = "in_menu_music.wav";
        timer_paused = false;
        access_from_menu = false;
        toggle_paused = 2;
        toggle_settings = 2;
        before_game_status = 1;

        this.settings = settings;
        this.direction_current = src.gameclasses.Direction.UP;

        this.tabbed_pane_settings.setVisible(false);
        jpanel_game.setVisible(false);
        jpanel_about.setVisible(false);
        jpanel_controls.setVisible(false);
        jpanel_scoreboard.setVisible(false);
        jpanel_input.setVisible(false);
        jpanel_menu.setVisible(true);
        try {
            if (settings.in_menu_music) {
                audio_player.play_music(in_menu_music);
            }
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        setContentPane(contentPane);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        set_window_size();
        setResizable(false);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);

        try {
            InputStream inputStream = GameEnvironment.class.getClassLoader()
                .getResourceAsStream("png_game/fruits/fruits.txt");
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(inputStream),
                    Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            list_of_fruits_assets = textBuilder.toString().split("\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        add_listeners();
        controls = new MovementKeyListener(this);
        addKeyListener(controls);

        tick_event_caller = new Timer(settings.tick_speed, this);

        repaint_caller = new Timer(10, e -> {
            active_bullets_set.parallelStream().forEach((bullet -> {
                if (bullet.travel()) {
                    active_bullets_set.remove(bullet);
                }
            }));
            jpanel_game.repaint();
        });
        repaint_caller.start();

        init_components();
        set_toolbar_enabled(false);
        set_up_radio_buttons();

        game_info = "Press ENTER to play";
        lbl_points.setText("Points: -");
        lbl_name.setText(settings.player);

        jpanel_game.repaint();
    }

    private void set_toolbar_enabled(boolean enabled) {
        if (enabled) {
            btn_pause.setEnabled(true);
            btn_menu.setEnabled(true);
            btn_settings.setEnabled(true);
            lbl_points.setEnabled(true);
        } else {
            btn_pause.setEnabled(false);
            btn_menu.setEnabled(false);
            btn_settings.setEnabled(false);
            lbl_points.setEnabled(false);
        }
    }

    private void set_up_radio_buttons() {
        int res_width = getInsets().left + getInsets().right;
        int res_height = getInsets().bottom + getInsets().top + toolbar.getHeight();
        window_size_group = new ButtonGroup();
        window_size_group.add(rd_win_size_huge);
        window_size_group.add(rd_win_size_large);
        window_size_group.add(rd_win_size_medium);
        window_size_group.add(rd_win_size_small);

        rd_win_size_huge.setToolTipText(1760 + res_width + "x" + (990 + res_height));
        rd_win_size_large.setToolTipText(1280 + res_width + "x" + (720 + res_height));
        rd_win_size_medium.setToolTipText(1024 + res_width + "x" + (576 + res_height));
        rd_win_size_small.setToolTipText(640 + res_width + "x" + (360 + res_height));

        field_size_group = new ButtonGroup();
        field_size_group.add(rd_field_size_huge);
        field_size_group.add(rd_field_size_large);
        field_size_group.add(rd_field_size_medium);
        field_size_group.add(rd_field_size_small);

        rd_field_size_huge.setToolTipText("(128x72)");
        rd_field_size_large.setToolTipText("(96x54)");
        rd_field_size_medium.setToolTipText("(64x36)");
        rd_field_size_small.setToolTipText("(32x18)");

        game_mode_group = new ButtonGroup();
        game_mode_group.add(rd_mode_default);
        game_mode_group.add(rd_mode_gun);
        game_mode_group.add(rd_mode_speed);
        rd_mode_default.setMnemonic(0);
        rd_mode_gun.setMnemonic(1);
        rd_mode_speed.setMnemonic(2);
        rd_mode_default.setToolTipText("Default mode: boring");
        rd_mode_default.setToolTipText("Gun mode: press and hold S to shoot walls and apples");
        rd_mode_default.setToolTipText("Speed mode: more points MORE SPEED");
        switch (settings.game_mode) {
            case 0:
                game_mode_group.setSelected(rd_mode_default.getModel(), true);
                break;
            case 1:
                game_mode_group.setSelected(rd_mode_gun.getModel(), true);
                break;
            case 2:
                game_mode_group.setSelected(rd_mode_speed.getModel(), true);
                break;
        }
    }

    private void init_components() {
        String about_html_str = "<html>" +
            "<style>" +
            "h1 {text-align: center;}" +
            "h2 {text-align: center;}" +
            "p {text-align: center;}" +
            "div {text-align: center;}" +
            "</style>" +
            "<body>" +
            "    <h1>Snek the Game</h1>" +
            "    <h2>Developers</h2>" +
            "    <div>Arne Stoll</div>" +
            "    <div>Frederick Phillips</div>" +
            "    <h2>Contact Information</h2>" +
            "    <div><a href=\"\">arne.stoll.1@outlook.de</a></div>" +
            "    <div><a href=\"\">freddy_phillips@hotmail.de</a></div>" +
            "    <h2>Version Date</h2>" +
            "    <div>2021-04-14</div>" +
            "    <h2>Game Version</h2>" +
            "    <div>1.8.0</div>" +
            "    <h2>Credits</h2>" +
            "    <div>Icons made by <b>Dave Gandy</b> from <a href=\"\">www.flaticon.com</a></div>"
            +
            "    <div>Sounds and Music from <a href=\"\">www.mixkit.co</a></div>" +
            "</body>" +
            "</html>";
        lbl_about.setText(about_html_str);
        String controls_html_str = "<html>" +
            "<style>" +
            "h1{text-align:center;}" +
            "h2{text-align:center;}" +
            "p{text-align:center;}" +
            "div{text-align:center;}" +
            "</style>" +
            "<body>" +
            "<h1>Controls</h1>" +
            "<h2>Movement</h2>" +
            "<div>Arrow up, down, left, right</div>" +
            "<h2>Shoot gun</h2>" +
            "<div>Press and hold S</div>" +
            "<h2>Pause</h2>" +
            "<div>Press SPACE</div>" +
            "</body>" +
            "</html>";
        lbl_controls.setText(controls_html_str);

        set_icon(btn_settings, "settings.png");
        set_icon(btn_settings_menu, "settings.png");
        set_icon(btn_menu, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_pause, "pause-symbol.png");
        set_icon(btn_scoreboard, "list.png");
        set_icon(btn_level_editor, "magic-wand.png");
        set_icon(btn_controls, "light-bulb.png");
        set_icon(btn_about, "information-button.png");
        set_icon(btn_exit, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_about_back, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_cancel, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_cancel_settings, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_controls_back, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_scoreboard_to_menu, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_play, "arrow-pointing-right-in-a-circle.png");
        set_icon(btn_apply_name, "arrow-pointing-right-in-a-circle.png");
        set_icon(btn_settings_apply, "arrow-pointing-right-in-a-circle.png");
        set_icon(btn_play_again, "refresh-page-option.png");
    }

    private void set_window_size() {
        final int frameTopInset = this.getInsets().top;
        final int frameLeftInset = this.getInsets().left;
        final int frameRightInset = this.getInsets().right;
        final int frameBottomInset = this.getInsets().bottom;
        int toolBarHeight = toolbar.getHeight();
        setSize(
            new Dimension(
                settings.square_size * settings.num_rect_x + frameLeftInset + frameRightInset,
                settings.square_size * settings.num_rect_y + toolBarHeight + frameTopInset
                    + frameBottomInset));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (timer_paused) {
            return;
        }

        if (!game_board.try_movement(direction_current)) {
            before_game_status = 0;
            controls.set_enabled(false);
            game_info = "Press SPACE to reset game board";
            active_bullets_set.clear();
            SwingUtilities.invokeLater(() -> jpanel_game.repaint());
            jpanel_game.setVisible(false);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String date = formatter.format(new Date());
            String game_mode = "";
            switch (settings.game_mode) {
                case 0:
                    game_mode = "Default";
                    break;
                case 1:
                    game_mode = "Gun";
                    break;
                case 2:
                    game_mode = "Speed";
                    break;
            }
            leaderboard.add_entry(settings.player, game_board.getPoints(), date,
                String.valueOf((((settings.tick_speed - 100) / 10) * -1) + 11),
                game_board.level.name,
                game_mode);
            from_game.set(true);
            jpanel_scoreboard.setVisible(true);
            if (game_board.getPoints() > 0) {
                String points_str = (game_board.getPoints() == 1) ? " point" : " points";
                JOptionPane.showMessageDialog(new JFrame(),
                    "Congrats you just have scored " + game_board.getPoints() + points_str);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "Try harder. You just scored nothing");
            }
            try {
                audio_player.stop_music();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            repaint_caller.stop();
            tick_event_caller.stop();
        } else {
            controls.set_enabled(true);
        }

        if (settings.game_mode == 2 && game_board.getPoints() % 5 == 0) {
            // increase speed
            tick_event_caller
                .setDelay(Math.max(80, settings.tick_speed - (game_board.getPoints() * 2)));
        }

        lbl_points.setText(String.format("Points: %d", game_board.getPoints()));
    }

    private void createUIComponents() {
        game_info = "";
        tabbed_pane_settings = new JPanel() {
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if (aFlag) {
                    if (!from_game.get()) {
                        jpanel_level_selection.setVisible(false);
                    } else {
                        jpanel_level_selection.setVisible(true);
                        cmb_level.setSelectedItem(game_board.level.name);
                    }
                    field_size_group.clearSelection();
                    // settings onto components
                    chk_in_game_music.setSelected(settings.in_game_music);
                    chk_in_menu_music.setSelected(settings.in_menu_music);
                    txt_new_name.setText(settings.player);
                    sld_tick_speed.setValue((((settings.tick_speed - 100) / 10) * -1) + 11);
                    window_size_group.getElements().asIterator().forEachRemaining((rd) -> {
                        if (rd.getText().equals(settings.rd_window_size)) {
                            window_size_group.setSelected(rd.getModel(), true);
                        }
                    });
                    field_size_group.getElements().asIterator().forEachRemaining((rd) -> {
                        if (rd.getText().equals(settings.rd_num_rect)) {
                            field_size_group.setSelected(rd.getModel(), true);
                        }
                    });
                } else {
                    toggle_settings = 2;
                }
            }
        };

        jpanel_menu = new JPanel() {
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if (aFlag) {
                    from_game.set(false);
                }
            }
        };

        jpanel_game = new JPanel() {
            private CopyOnWriteArraySet<Integer[]> paint_hit_markers = new CopyOnWriteArraySet<>();

            private void paint_n_milliseconds(int n, Integer[] coordinate) {
                Timer timeout = new Timer(n, e -> paint_hit_markers.remove(coordinate));
                timeout.setRepeats(false);
                timeout.start();
            }

            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if (aFlag) {
                    from_game.set(true);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int pixel_size = settings.square_size;
                for (int y = 0; y < settings.num_rect_y; y++) {
                    for (int x = 0; x < settings.num_rect_x; x++) {
                        int status = game_board.get_status(x, y);
                        g.setColor(Color.WHITE);
                        g.fillRect(x * pixel_size, y * pixel_size, pixel_size, pixel_size);
                        switch (status) {
                            case 0:
                                g.setColor(Color.WHITE);
                                g.fillRect(x * pixel_size, y * pixel_size, pixel_size, pixel_size);
                                break;
                            case 1:
                                g.setColor(Color.RED);
                                int type = game_board.get_type(x, y);
                                if (HEAD_UP == type) {
                                    paint_img(g, "png_game/head.png", x, y, settings.square_size,
                                        0);
                                } else if (HEAD_DOWN == type) {
                                    paint_img(g, "png_game/head.png", x, y, settings.square_size,
                                        180);
                                } else if (HEAD_RIGHT == type) {
                                    paint_img(g, "png_game/head.png", x, y, settings.square_size,
                                        90);
                                } else if (HEAD_LEFT == type) {
                                    paint_img(g, "png_game/head.png", x, y, settings.square_size,
                                        270);
                                } else if (TAIL_DOWN == type) {
                                    paint_img(g, "png_game/tail.png", x, y, settings.square_size,
                                        0);
                                } else if (TAIL_LEFT == type) {
                                    paint_img(g, "png_game/tail.png", x, y, settings.square_size,
                                        90);
                                } else if (TAIL_UP == type) {
                                    paint_img(g, "png_game/tail.png", x, y, settings.square_size,
                                        180);
                                } else if (TAIL_RIGHT == type) {
                                    paint_img(g, "png_game/tail.png", x, y, settings.square_size,
                                        270);
                                } else if (VERTICAL_DOWN == type || VERTICAL_UP == type) {
                                    paint_img(g, "png_game/north.png", x, y, settings.square_size,
                                        0);
                                } else if (HORIZONTAL_LEFT == type || HORIZONTAL_RIGHT == type) {
                                    paint_img(g, "png_game/north.png", x, y, settings.square_size,
                                        90);
                                } else if (type == CORNER_EAST_NORTH || type == CORNER_NORTH_EAST) {
                                    paint_img(g, "png_game/south_east.png", x, y,
                                        settings.square_size, 270);
                                } else if (type == CORNER_EAST_SOUTH || type == CORNER_SOUTH_EAST) {
                                    paint_img(g, "png_game/south_east.png", x, y,
                                        settings.square_size, 0);
                                } else if (type == CORNER_SOUTH_WEST || type == CORNER_WEST_SOUTH) {
                                    paint_img(g, "png_game/south_east.png", x, y,
                                        settings.square_size, 90);
                                } else if (type == CORNER_WEST_NORTH || type == CORNER_NORTH_WEST) {
                                    paint_img(g, "png_game/south_east.png", x, y,
                                        settings.square_size, 180);
                                } else {
                                    System.out.println("Type: " + type);
                                    g.setColor(Color.GREEN);
                                    g.fillRect(x * pixel_size, y * pixel_size, pixel_size,
                                        pixel_size);
                                    g.setColor(Color.BLACK);
                                    g.drawOval(x * pixel_size, y * pixel_size, 6, 6);
                                }
                                break;
                            case 2:
                                g.setColor(Color.BLACK);
                                g.fillRect(x * pixel_size, y * pixel_size, pixel_size, pixel_size);
                                break;
                            case 3:
                                if (list_of_fruits_assets != null) {
                                    g.setColor(Color.WHITE);
                                    g.fillRect(x * pixel_size, y * pixel_size, pixel_size,
                                        pixel_size);
                                    if (game_board.random_index_for_fruit != null) {
                                        paint_img(g, "png_game/fruits/"
                                                + list_of_fruits_assets[game_board.random_index_for_fruit],
                                            x, y, settings.square_size, 0, 0.9);
                                    }
                                } else {
                                    g.setColor(Color.WHITE);
                                    g.fillRect(x * pixel_size, y * pixel_size, pixel_size,
                                        pixel_size);
                                    g.setColor(Color.RED);
                                    g.fillOval(x * pixel_size, y * pixel_size, pixel_size,
                                        pixel_size);
                                }
                                break;
                        }
                    }
                }
                for (Bullet bullet : active_bullets_set) {
                    Integer[] coordinate = bullet.get_game_board_coordinate();
                    SwingUtilities.invokeLater(() -> {
                        if (coordinate[0] >= 0 && coordinate[1] >= 0
                            && coordinate[0] < settings.num_rect_x
                            && coordinate[1] < settings.num_rect_y) {
                            switch (game_board.get_status(coordinate[0], coordinate[1])) {
                                case 1:
                                    try {
                                        audio_player.play_sound("hit.wav");
                                    } catch (LineUnavailableException | IOException
                                        | UnsupportedAudioFileException e) {
                                        e.printStackTrace();
                                    }
                                    game_board.slice_snake(coordinate[0], coordinate[1]);
                                    paint_hit_markers.add(coordinate);
                                    paint_n_milliseconds(200, coordinate);
                                    active_bullets_set.remove(bullet);
                                    break;
                                case 2:
                                    try {
                                        audio_player.play_sound("hit.wav");
                                    } catch (LineUnavailableException | IOException
                                        | UnsupportedAudioFileException e) {
                                        e.printStackTrace();
                                    }
                                    game_board.set_empty(coordinate[0], coordinate[1]);
                                    paint_hit_markers.add(coordinate);
                                    paint_n_milliseconds(200, coordinate);
                                    active_bullets_set.remove(bullet);
                                    break;
                                case 3:
                                    try {
                                        audio_player.play_sound("hit.wav");
                                    } catch (LineUnavailableException | IOException
                                        | UnsupportedAudioFileException e) {
                                        e.printStackTrace();
                                    }
                                    game_board.elongate_snake(coordinate[0], coordinate[1],
                                        direction_current);
                                    paint_hit_markers.add(coordinate);
                                    paint_n_milliseconds(200, coordinate);
                                    active_bullets_set.remove(bullet);
                                    break;
                            }
                        }
                    });
                    g.setColor(Color.RED);
                    g.drawLine(bullet.origin_x, bullet.origin_y, bullet.x, bullet.y);
                }
                paint_hit_markers.parallelStream().forEach(
                    (hit_marker) -> paint_img(g, "png_game/hit_marker.png", hit_marker[0],
                        hit_marker[1],
                        settings.square_size, 0));
                g.setColor(Color.BLACK);
                Font curr_font = g.getFont().deriveFont(15f);
                g.setFont(curr_font);
                int screen_mid_x = settings.field_res_width / 2;
                int screen_mid_y = settings.field_res_height / 2;
                int font_width = g.getFontMetrics().stringWidth(game_info);
                g.drawString(game_info, screen_mid_x - (font_width / 2), screen_mid_y - 5);
            }

            private void paint_img(Graphics g, String path, int x, int y, int pixel_size,
                int rotation,
                double scale_factor) {
                ClassLoader cl = GameEnvironment.class.getClassLoader();
                InputStream cc = cl.getResourceAsStream(path);
                if (cc != null) {
                    BufferedImage img;
                    try {
                        img = ImageIO.read(cc);
                        g.setColor(Color.WHITE);
                        g.fillRect(x * pixel_size, y * pixel_size, pixel_size, pixel_size);
                        double rotationRequired = Math.toRadians(rotation);
                        double locationX = img.getWidth() * 0.5;
                        double locationY = img.getHeight() * 0.5;
                        AffineTransform tx = AffineTransform
                            .getRotateInstance(rotationRequired, locationX, locationY);
                        AffineTransformOp op = new AffineTransformOp(tx,
                            AffineTransformOp.TYPE_BILINEAR);
                        BufferedImage img_scaled = new BufferedImage(img.getWidth(),
                            img.getHeight(),
                            img.getType());
                        op.filter(img, img_scaled);
                        int size = (int) (pixel_size * scale_factor);
                        g.drawImage(img_scaled.getScaledInstance(size, size, Image.SCALE_FAST),
                            x * pixel_size,
                            y * pixel_size, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        g.setColor(Color.RED);
                        g.fillRect(x * pixel_size, y * pixel_size, pixel_size, pixel_size);
                    }
                }
            }

            private void paint_img(Graphics g, String path, int x, int y, int pixel_size,
                int rotation) {
                ClassLoader cl = GameEnvironment.class.getClassLoader();
                InputStream cc = cl.getResourceAsStream(path);
                if (cc != null) {
                    BufferedImage img;
                    try {
                        img = ImageIO.read(cc);
                        g.setColor(Color.WHITE);
                        g.fillRect(x * pixel_size, y * pixel_size, pixel_size, pixel_size);
                        double rotationRequired = Math.toRadians(rotation);
                        double locationX = img.getWidth() * 0.5;
                        double locationY = img.getHeight() * 0.5;
                        AffineTransform tx = AffineTransform
                            .getRotateInstance(rotationRequired, locationX, locationY);
                        AffineTransformOp op = new AffineTransformOp(tx,
                            AffineTransformOp.TYPE_BILINEAR);
                        BufferedImage img_scaled = new BufferedImage(img.getWidth(),
                            img.getHeight(),
                            img.getType());
                        op.filter(img, img_scaled);
                        g.drawImage(
                            img_scaled.getScaledInstance(pixel_size, pixel_size, Image.SCALE_FAST),
                            x * pixel_size, y * pixel_size, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        g.setColor(Color.RED);
                        g.fillRect(x * pixel_size, y * pixel_size, pixel_size, pixel_size);
                    }
                }
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
            }
        };

        sld_tick_speed = new JSlider(1, 10, 5);
        sld_tick_speed.setPaintTicks(true);
        sld_tick_speed.setMajorTickSpacing(1);
        sld_tick_speed.setPaintLabels(true);
        sld_tick_speed.setSnapToTicks(true);

        String[] column_names = {"Ranking", "Player", "Points", "Date", "Difficulty", "Level",
            "Game Mode"};

        jpanel_scoreboard = new JPanel() {
            @Override
            public void setVisible(boolean aFlag) {
                super.setVisible(aFlag);
                if (aFlag) {
                    set_toolbar_enabled(false);
                    int ranking = 1;
                    scroll_pane.setPreferredSize(
                        new Dimension((int) (settings.field_res_width * 0.6), -1));
                    DefaultTableModel table_data_access = (DefaultTableModel) table_scoreboard
                        .getModel();
                    table_data_access.setRowCount(0);
                    for (LeaderboardEntry entry_lb : leaderboard.get_lb_list()) {
                        String[] entry_array = entry_lb.getAll();
                        entry_array[0] = String.valueOf(ranking++);
                        table_data_access.addRow(entry_array);
                    }
                    if (from_game.get()) {
                        btn_play_again.setText("Play again");
                        btn_play_again.setVisible(true);
                    } else {
                        btn_play_again.setVisible(false);
                    }
                }
            }
        };
        TableModel tableModel = new DefaultTableModel(column_names, column_names.length) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table_scoreboard = new JTable(tableModel);
        table_scoreboard.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        DefaultTableModel defaultTableModel = (DefaultTableModel) table_scoreboard.getModel();
        table_scoreboard.getTableHeader().getColumnModel().getColumn(3).setPreferredWidth(100);
        table_scoreboard.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(50);
        defaultTableModel.setRowCount(0);
        scroll_pane = new JScrollPane(table_scoreboard);
    }

    private void init_window_change_procedure(boolean status_leave) {
        if (status_leave) {
            tick_event_caller.stop();
        } else {
            game_info = "Press ENTER to play";
            jpanel_game.repaint();
            game_board.reset_game();
            before_game_status = 1;
            toggle_paused = 2;
            timer_paused = false;
            addKeyListener(controls);
            set_icon(btn_pause, "pause-symbol.png");
        }
    }

    private void add_listeners() {
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && before_game_status == 0) {
                    game_info = "Press ENTER to play";
                    before_game_status++;
                    game_board.reset_game();
                    lbl_points.setText(String.format("Points: %d", game_board.getPoints()));
                    jpanel_game.repaint();

                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && before_game_status == 1) {
                    controls.set_enabled(true);
                    try {
                        if (settings.in_game_music) {
                            audio_player.play_music(in_game_music);
                        }
                    } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                        ex.printStackTrace();
                    }
                    game_info = "";
                    switch (game_board.level.direction) {
                        case LevelEditor.NORTH:
                            direction_current = src.gameclasses.Direction.UP;
                            break;
                        case LevelEditor.EAST:
                            direction_current = src.gameclasses.Direction.RIGHT;
                            break;
                        case LevelEditor.SOUTH:
                            direction_current = src.gameclasses.Direction.DOWN;
                            break;
                        case LevelEditor.WEST:
                            direction_current = src.gameclasses.Direction.LEFT;
                            break;
                    }
                    before_game_status++;
                    tick_event_caller.start();
                    repaint_caller.start();
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE && tick_event_caller.isRunning()) {
                    if (toggle_paused == 2) {
                        game_info = "Game paused";
                        jpanel_game.repaint();
                        timer_paused = true;
                        repaint_caller.stop();
                        controls.set_enabled(false);
                        set_icon(btn_pause, "arrowhead-pointing-to-the-right.png");
                        toggle_paused >>= 1;
                        before_game_status = 1;
                    } else {
                        game_info = "";
                        toggle_paused <<= 1;
                        timer_paused = false;
                        repaint_caller.start();
                        controls.set_enabled(true);
                        set_icon(btn_pause, "pause-symbol.png");
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    boolean was_paused = timer_paused;
                    if (jpanel_game.isVisible()) {
                        if (!timer_paused) {
                            timer_paused = true;
                        }
                    }
                    boolean game_active = jpanel_game.isVisible();
                    String message;
                    if (game_active) {
                        message = "Game is running. Do you really want to quit?";
                    } else {
                        message = "Do you really want to quit the game?";
                    }
                    repaint_caller.stop();
                    int yes_no_pane = JOptionPane.showOptionDialog(new JFrame(),
                        message,
                        "Alert",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        null);
                    if (yes_no_pane == JOptionPane.YES_OPTION) {
                        if (game_active) {
                            jpanel_game.setVisible(false);
                            jpanel_menu.setVisible(true);
                            requestFocus();
                        } else {
                            System.exit(-1);
                        }
                    } else {
                        if (jpanel_game.isVisible() && !was_paused) {
                            timer_paused = false;
                            repaint_caller.start();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        Timer shot_timer = new Timer((int) (settings.tick_speed * 0.5), e -> {
            if (repaint_caller.isRunning() && tick_event_caller.isRunning()) {
                Integer[] head = game_board.get_head();
                active_bullets_set.add(new Bullet(direction_current, head[0], head[1], settings));
                SwingUtilities.invokeLater(() -> {
                    try {
                        audio_player.play_sound("gun_shot.wav");
                    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
        shot_timer.setInitialDelay(0);

        addKeyListener(new KeyListener() {
            private final Object obj = new Object();
            private boolean press_active = false;

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (settings.game_mode != 1) {
                    return;
                }
                synchronized (obj) {
                    if (e.getKeyCode() == KeyEvent.VK_S && !press_active) {
                        press_active = true;
                        shot_timer.start();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (settings.game_mode != 1) {
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    press_active = false;
                    shot_timer.stop();
                }
            }
        });

        btn_pause.addActionListener(e -> {
            play_select_sound();
            if (toggle_paused == 2) {
                game_info = "Game paused";
                timer_paused = true;
                controls.set_enabled(false);
                set_icon(btn_pause, "arrowhead-pointing-to-the-right.png");
                toggle_paused = 1;
            } else if (timer_paused) {
                if (!repaint_caller.isRunning()) {
                    repaint_caller.start();
                }
                game_info = "";
                toggle_paused = 2;
                timer_paused = false;
                controls.set_enabled(true);
                set_icon(btn_pause, "pause-symbol.png");
            } else {
                game_info = "";
                toggle_paused <<= 1;
                timer_paused = false;
                controls.set_enabled(true);
                set_icon(btn_pause, "pause-symbol.png");
            }
        });

        btn_settings_menu.addActionListener(e -> {
            play_select_sound();
            txt_new_name.setText(settings.player);
            access_from_menu = true;
            jpanel_menu.setVisible(false);
            tabbed_pane_settings.setVisible(true);
            String[] level_names = LevelSerializer.get_all_levels().keySet().toArray(new String[0]);
            cmb_level.setModel(new DefaultComboBoxModel<>(level_names));
        });

        btn_about.addActionListener(e -> {
            play_select_sound();
            jpanel_menu.setVisible(false);
            jpanel_about.setVisible(true);
        });

        btn_about_back.addActionListener(e -> {
            play_select_sound();
            jpanel_about.setVisible(false);
            jpanel_menu.setVisible(true);
        });

        btn_controls_back.addActionListener(e -> {
            play_select_sound();
            jpanel_controls.setVisible(false);
            jpanel_menu.setVisible(true);
        });

        btn_controls.addActionListener(e -> {
            play_select_sound();
            jpanel_controls.setVisible(true);
            jpanel_menu.setVisible(false);
        });

        btn_play.addActionListener(e -> {
            play_select_sound();
            txt_name.setText(settings.player);
            jpanel_menu.setVisible(false);
            jpanel_input.setVisible(true);
            list_levels.setListData(LevelSerializer.get_all_levels().keySet().toArray());
            txt_name.requestFocus();
        });

        btn_exit.addActionListener(e -> {
            play_select_sound();
            System.exit(-1);
        });

        btn_apply_name.addActionListener(e -> {
            play_select_sound();
            if (list_levels.isSelectionEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a level");
            } else {
                String name = txt_name.getText();
                if (name.isEmpty() || name.isBlank()) {
                    name = "Player";
                }
                String level_name = String.valueOf(list_levels.getSelectedValue());
                Level level = LevelSerializer.get_all_levels().get(level_name);
                if (level_name.equals("Walled") || level_name.equals("Empty") || level_name
                    .equals("Stripped")) {
                    level.init_level(level_name, settings.num_rect_x, settings.num_rect_y);
                    settings.set_field_size(level.get_width(), level.get_height());
                }
                if (settings.num_rect_x != level.get_width() || settings.num_rect_y != level
                    .get_height()) {
                    settings.set_field_size(level.get_width(), level.get_height());
                    System.err.println("@btn_apply_name: resolution mismatch, no default selected");
                }
                switch (level.direction) {
                    case LevelEditor.NORTH:
                        direction_current = src.gameclasses.Direction.UP;
                        break;
                    case LevelEditor.EAST:
                        direction_current = src.gameclasses.Direction.RIGHT;
                        break;
                    case LevelEditor.SOUTH:
                        direction_current = src.gameclasses.Direction.DOWN;
                        break;
                    case LevelEditor.WEST:
                        direction_current = src.gameclasses.Direction.LEFT;
                        break;
                }
                settings.player = name;
                lbl_name.setText(name);
                this.game_board = new GameBoard(settings, audio_player, level);
                this.game_board.length_index_selector = list_of_fruits_assets.length;
                this.game_board.random_index_for_fruit = 0;
                this.game_board.random_index_for_fruit = 0;

                set_toolbar_enabled(true);
                init_window_change_procedure(false);
                jpanel_input.setVisible(false);
                jpanel_game.setVisible(true);
                requestFocus();
            }
        });

        btn_settings.addActionListener(e -> {
            play_select_sound();
            if (toggle_settings == 2) {
                toggle_settings >>= 1;
                jpanel_game.setVisible(false);
                jpanel_menu.setVisible(false);
                init_window_change_procedure(true);
                try {
                    if (settings.in_menu_music) {
                        audio_player.play_music(in_menu_music);
                    }
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                    ex.printStackTrace();
                }
                txt_new_name.setText(settings.player);
                String[] level_names = LevelSerializer.get_all_levels().keySet()
                    .toArray(new String[0]);
                cmb_level.setModel(new DefaultComboBoxModel<>(level_names));
                tabbed_pane_settings.setVisible(true);
            } else {
                toggle_settings <<= 1;
                init_window_change_procedure(false);
                tabbed_pane_settings.setVisible(false);
                jpanel_game.setVisible(true);
            }
        });

        btn_menu.addActionListener(e -> {
            play_select_sound();
            set_toolbar_enabled(false);
            tabbed_pane_settings.setVisible(false);
            jpanel_game.setVisible(false);
            jpanel_menu.setVisible(true);
            try {
                if (settings.in_menu_music) {
                    audio_player.play_music(in_menu_music);
                }
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                ex.printStackTrace();
            }
            tick_event_caller.stop();
            game_info = "Press ENTER to play";
            before_game_status = 1;
            if (timer_paused) {
                timer_paused = false;
                toggle_paused = 2;
                controls.set_enabled(true);
                set_icon(btn_pause, "pause-symbol.png");
            }
            game_board.reset_game();
            init_window_change_procedure(true);
        });

        btn_settings_apply.addActionListener(e -> {
            play_select_sound();
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final Dimension screenSize = toolkit.getScreenSize();
            settings.tick_speed = 100 + (11 - sld_tick_speed.getValue()) * 10;
            tick_event_caller.setDelay(settings.tick_speed);
            for (AbstractButton game_mode_rd : Collections.list(game_mode_group.getElements())) {
                if (game_mode_group.isSelected(game_mode_rd.getModel())) {
                    settings.game_mode = game_mode_rd.getMnemonic();
                }
            }

            boolean flag = false;
            for (AbstractButton abstractButton : Collections
                .list(window_size_group.getElements())) {
                if (window_size_group.isSelected(abstractButton.getModel())) {
                    switch (abstractButton.getText()) {
                        case "Huge":
                            settings.rd_window_size = "Huge";
                            settings.set_field_res(1760, 990);
                            flag = true;
                            break;
                        case "Large":
                            settings.rd_window_size = "Large";
                            settings.set_field_res(1280, 720);
                            break;
                        case "Medium":
                            settings.rd_window_size = "Medium";
                            settings.set_field_res(1024, 576);
                            break;
                        case "Small":
                            settings.rd_window_size = "Small";
                            settings.set_field_res(640, 360);
                            break;
                    }
                }
            }
            for (AbstractButton abstractButton : Collections.list(field_size_group.getElements())) {
                if (field_size_group.isSelected(abstractButton.getModel())) {
                    switch (abstractButton.getText()) {
                        case "Huge":
                            settings.rd_num_rect = "Huge";
                            settings.set_field_size(128, 72);
                            break;
                        case "Large":
                            settings.rd_num_rect = "Large";
                            settings.set_field_size(96, 54);
                            break;
                        case "Medium":
                            settings.rd_num_rect = "Medium";
                            settings.set_field_size(64, 36);
                            break;
                        case "Small":
                            settings.rd_num_rect = "Small";
                            settings.set_field_size(32, 18);
                            break;
                    }
                }
            }
            String level_name = String.valueOf(cmb_level.getSelectedItem());
            Level level = LevelSerializer.get_all_levels().get(level_name);
            if (level_name.equals("Walled (Default)") || level_name.equals("Torus (Default)")
                || level_name.equals("Striped (Default)")) {
                level.init_level(level_name, settings.num_rect_x, settings.num_rect_y);
                settings.set_field_size(level.get_width(), level.get_height());
            }
            if (settings.num_rect_x != level.get_width() || settings.num_rect_y != level
                .get_height()) {
                settings.set_field_size(level.get_width(), level.get_height());
                System.err.println("@btn_settings_apply: resolution mismatch");
            }

            final int frameTopInset = getInsets().top;
            final int frameLeftInset = getInsets().left;
            final int frameRightInset = getInsets().right;
            final int frameBottomInset = getInsets().bottom;
            int toolBarHeight = toolbar.getHeight();
            setSize(new Dimension(
                settings.square_size * settings.num_rect_x + frameLeftInset + frameRightInset,
                settings.square_size * settings.num_rect_y + toolBarHeight + frameTopInset
                    + frameBottomInset));

            toggle_settings = 2;
            tabbed_pane_settings.setVisible(false);
            if (access_from_menu) {
                jpanel_menu.setVisible(true);
                access_from_menu = false;
            } else {
                jpanel_game.setVisible(true);
                set_toolbar_enabled(true);
            }
            final int x = (screenSize.width - getWidth()) / 2;
            final int y = flag ? 0 : ((screenSize.height - getHeight()) / 2);
            setLocation(x, y);

            if (!chk_in_game_music.isSelected()) {
                audio_player.mute_sound(in_game_music, true);
                settings.in_game_music = false;
            } else {
                audio_player.mute_sound(in_game_music, false);
                settings.in_game_music = true;
            }
            if (!chk_in_menu_music.isSelected()) {
                audio_player.mute_sound(in_menu_music, true);
                settings.in_menu_music = false;
            } else {
                audio_player.mute_sound(in_menu_music, false);
                settings.in_menu_music = true;
                try {
                    audio_player.play_music(in_menu_music);
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                    ex.printStackTrace();
                }
            }
            settings.player = txt_new_name.getText();
            before_game_status = 1;
            if (timer_paused) {
                timer_paused = false;
                toggle_paused <<= 1;
                controls.set_enabled(true);
                set_icon(btn_pause, "pause-symbol.png");
            }
            lbl_name.setText(settings.player);

            switch (level.direction) {
                case LevelEditor.NORTH:
                    direction_current = src.gameclasses.Direction.UP;
                    break;
                case LevelEditor.EAST:
                    direction_current = src.gameclasses.Direction.RIGHT;
                    break;
                case LevelEditor.SOUTH:
                    direction_current = src.gameclasses.Direction.DOWN;
                    break;
                case LevelEditor.WEST:
                    direction_current = Direction.LEFT;
                    break;
            }
            this.game_board = new src.mainclasses.GameBoard(settings, audio_player, level);
            this.game_board.length_index_selector = list_of_fruits_assets.length;
            this.game_board.random_index_for_fruit = 0;
            SettingsSerializer.update_file(settings);
            init_window_change_procedure(false);
            requestFocus();
        });

        btn_scoreboard.addActionListener(e -> {
            from_game.set(false);
            play_select_sound();
            if (jpanel_menu.isVisible()) {
                jpanel_menu.setVisible(false);
                jpanel_scoreboard.setVisible(true);
            }
        });

        btn_play_again.addActionListener(e -> {
            play_select_sound();
            jpanel_scoreboard.setVisible(false);
            jpanel_game.setVisible(true);
            set_toolbar_enabled(true);
            requestFocus();
        });

        btn_cancel_settings.addActionListener(e -> {
            play_select_sound();
            tabbed_pane_settings.setVisible(false);
            toggle_settings = 2;
            if (from_game.get()) {
                jpanel_game.setVisible(true);
            } else {
                jpanel_menu.setVisible(true);
            }
            access_from_menu = false;
            requestFocus();
        });

        btn_cancel.addActionListener(e -> {
            play_select_sound();
            jpanel_input.setVisible(false);
            jpanel_menu.setVisible(true);
        });

        btn_scoreboard_to_menu.addActionListener(e -> {
            play_select_sound();
            jpanel_scoreboard.setVisible(false);
            jpanel_menu.setVisible(true);
            set_toolbar_enabled(false);
            requestFocus();
        });

        btn_level_editor.addActionListener(e -> {
            play_select_sound();
            HashMap<String, Integer[]> field_size_map = new HashMap<>();
            field_size_map.put("Small", new Integer[]{32, 18});
            field_size_map.put("Medium", new Integer[]{64, 36});
            field_size_map.put("Large", new Integer[]{96, 54});
            field_size_map.put("Huge", new Integer[]{128, 72});
            LevelEditor levelEditor = new LevelEditor(settings, this, this.audio_player,
                field_size_map);
            levelEditor.setVisible(true);
            this.setVisible(false);
        });
    }

    private void play_select_sound() {
        try {
            audio_player.play_sound("select.wav");
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}