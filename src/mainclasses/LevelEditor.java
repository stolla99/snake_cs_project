package src.mainclasses;

import static src.utilityclasses.IconDrawer.set_icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.swing.*;
import src.gameclasses.Level;
import src.gameclasses.Settings;
import src.utilityclasses.AudioPlayer;
import src.utilityclasses.LevelSerializer;


/**
 * Implementation of the GUI and its backend to let users create their own level. Customizable
 * aspects: - Snake position and its initial direction - Apples (consumable) and walls (obstacle)
 * <p>
 * Implements a MouseListener and MouseMotionListener in order to track the user input.
 */
public class LevelEditor extends JFrame implements MouseListener, MouseMotionListener {

    /*
     *  -STATIC VARIABLES-
     *
     * Variables to differentiate between the four direction a snake can be placed in the level.
     * Default value is set to NORTH.
     */
    public final static int NORTH = 0;
    public final static int EAST = 1;
    public final static int SOUTH = 2;
    public final static int WEST = 3;
    /**
     * If the value is set to TRUE any level created at runtime will be listed otherwise (FALSE) the
     * level can be created and played but is not modifiable after the first save.
     */
    private final Boolean is_modifiable = Boolean.TRUE;
    /*
     *  -GUI RELATED VARIABLES-
     */
    private JPanel contentPane;
    private JToolBar toolbar;
    private JButton btn_menu;
    private JPanel jpanel_editor;
    private JButton btn_empty;
    private JButton btn_wall;
    private JButton btn_apple;
    private JButton btn_snake;
    private JButton btn_drag;
    private JButton btn_freehand;
    private JPanel jpanel_level_editor_menu;
    private JList list_levels;
    private JButton btn_create_level;
    private JButton btn_delete_level;
    private JButton btn_modify_level;
    private JLabel lbl_level_name;
    private JButton btn_levelEditor_back;
    private JComboBox<String> cmb_field_size;
    private ButtonGroup tile_tools = new ButtonGroup();

    /*
     *  -BACKEND LEVEL EDITOR VARIABLES-
     */
    private ButtonGroup draw_tools = new ButtonGroup();
    /**
     * Reference to the GUI of the GameEnvironment
     */
    private Component parent;

    /**
     * Accepted values of the game field array: 0 is empty, 1 is snake, 2 is wall and 3 is apple
     */
    private Integer[][] game_board;
    private Settings settings;
    private String selected_level;
    private Integer snake_direction;
    private int snake_dir;

    /**
     * Queue with all messages to user which will be shown the GUI.
     */
    private ConcurrentLinkedDeque<String> messages;

    /**
     * When the Timer is started first message in the queue will be polled. Timer has no repeats and
     * is started after an initial delay is over.
     */
    private Timer timeout = new Timer(0, e -> messages.pollFirst());

    /**
     * Object of type Timer responsible for the painting of the level editor. Repaints on the GUI
     * variable: jpanel_editor in the paintComponent() method.
     */
    private Timer update_caller;
    private CopyOnWriteArrayList<Integer[]> snake_coordinates = new CopyOnWriteArrayList<>();

    /**
     * Mapping from the {Small, Medium, Large, Huge} -> {sizes of the game field}.
     */
    private HashMap<String, Integer[]> field_size_map;

    /*
     *  -VOLATILE VARIABLES-
     */

    private volatile int x_pos_mouse = -1;
    private volatile int y_pos_mouse = -1;
    private volatile int x_pos_anchor = -1;
    private volatile int y_pos_anchor = -1;
    private volatile boolean mouse_pressed = false;

    public LevelEditor(Settings settings, Component parent, AudioPlayer audio_player,
        HashMap<String, Integer[]> field_size_map) {
        this.parent = parent;
        this.settings = settings;
        this.field_size_map = field_size_map;
        Set<Map.Entry<String, Integer[]>> field_types = field_size_map.entrySet();
        String[] field_size_types = new String[field_types.size()];
        field_types.forEach(new Consumer<>() {
            private int i = 0;

            @Override
            public void accept(Map.Entry<String, Integer[]> stringEntry) {
                field_size_types[i] = stringEntry.getKey();
                i++;
            }
        });
        cmb_field_size.setModel(new DefaultComboBoxModel<>(field_size_types));
        AtomicReference<String> currently_selected_field_res = new AtomicReference<>();
        field_size_map.entrySet().parallelStream().forEach((entry) -> {
            if (entry.getValue()[0] == settings.num_rect_x
                && entry.getValue()[1] == settings.num_rect_y) {
                currently_selected_field_res.set(entry.getKey());
            }
        });
        cmb_field_size.setSelectedItem(currently_selected_field_res);
        snake_dir = SOUTH;
        btn_empty.setMnemonic(0);
        btn_snake.setMnemonic(1);
        btn_wall.setMnemonic(2);
        btn_apple.setMnemonic(3);
        this.tile_tools.add(btn_apple);
        this.tile_tools.add(btn_empty);
        this.tile_tools.add(btn_snake);
        this.tile_tools.add(btn_wall);
        this.tile_tools.setSelected(btn_empty.getModel(), true);

        btn_drag.setMnemonic(0);
        btn_freehand.setMnemonic(1);
        this.draw_tools.add(btn_drag);
        this.draw_tools.add(btn_freehand);
        this.draw_tools.setSelected(btn_freehand.getModel(), true);

        int old_layout = settings.board_layout;
        settings.board_layout = -1;
        this.game_board = new Integer[settings.num_rect_y][settings.num_rect_x];
        for (Integer[] row : this.game_board) {
            Arrays.fill(row, 0);
        }

        settings.board_layout = old_layout;

        setContentPane(contentPane);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        resize_window();
        setResizable(false);

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final int x = (screenSize.width - getWidth()) / 2;
        final int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        add_listeners();
        create_editor_update_timer();
        timeout.setRepeats(false);
        messages = new ConcurrentLinkedDeque<>();
        set_enable_button_groups(false);
        jpanel_editor.setVisible(false);
        jpanel_level_editor_menu.setVisible(true);
        selected_level = "";
        lbl_level_name.setText(selected_level);
        load_identifier();
        init_components();
    }

    private void resize_window() {
        final int frameTopInset = this.getInsets().top;
        final int frameLeftInset = this.getInsets().left;
        final int frameRightInset = this.getInsets().right;
        final int frameBottomInset = this.getInsets().bottom;
        int toolBarHeight = toolbar.getHeight();
        setSize(new Dimension(
            settings.square_size * settings.num_rect_x + frameLeftInset + frameRightInset
                + jpanel_editor.getInsets().right + jpanel_editor.getInsets().left + 1,
            settings.square_size * settings.num_rect_y + toolBarHeight + frameTopInset
                + frameBottomInset + jpanel_editor.getInsets().top + jpanel_editor
                .getInsets().bottom
                + 1));
    }

    private void init_components() {
        set_icon(btn_menu, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_levelEditor_back, "circle-with-an-arrow-pointing-to-left.png");
        set_icon(btn_create_level, "add-square-button.png");
        set_icon(btn_modify_level, "edit-interface-sign.png");
        set_icon(btn_delete_level, "minus-sign-on-a-square-outline.png");
        set_icon(btn_empty, "double-sided-eraser.png");
        set_icon(btn_freehand, "pencil.png");
        set_icon(btn_drag, "move-option.png");
    }

    private void create_editor_update_timer() {
        update_caller = new Timer(10, e -> {
            jpanel_editor.repaint();
        });
        update_caller.setInitialDelay(0);
        update_caller.setRepeats(true);
    }

    private void add_listeners() {
        btn_menu.addActionListener(e -> {
            if (jpanel_level_editor_menu.isVisible()) {
                parent.setVisible(true);
                setVisible(false);
            } else if (jpanel_editor.isVisible()) {
                set_enable_button_groups(false);
                LevelSerializer
                    .add_gameBoard(lbl_level_name.getText(), this.game_board,
                        snake_coordinates.get(0),
                        snake_coordinates.get(1), is_modifiable, this.snake_direction,
                        selected_level);
                jpanel_editor.setVisible(false);
                jpanel_level_editor_menu.setVisible(true);
            }
        });

        jpanel_editor.addMouseListener(this);
        jpanel_editor.addMouseMotionListener(this);

        btn_apple.addActionListener(e -> {
            messages.addLast("Apple selected");
            tile_tools.clearSelection();
            btn_drag.setEnabled(true);
            tile_tools.setSelected(btn_apple.getModel(), true);
        });

        btn_wall.addActionListener(e -> {
            messages.addLast("Wall selected");
            tile_tools.clearSelection();
            btn_drag.setEnabled(true);
            tile_tools.setSelected(btn_wall.getModel(), true);
        });

        btn_empty.addActionListener(e -> {
            messages.addLast("Empty tile selected");
            tile_tools.clearSelection();
            btn_drag.setEnabled(true);
            tile_tools.setSelected(btn_empty.getModel(), true);
        });

        btn_snake.addActionListener(e -> {
            messages.addLast("Press ARROWS to alter direction of the snake");
            tile_tools.clearSelection();
            btn_drag.setEnabled(false);
            tile_tools.setSelected(btn_snake.getModel(), true);
        });

        btn_drag.addActionListener(e -> {
            messages.addLast("Dragging active");
            draw_tools.clearSelection();
            draw_tools.setSelected(btn_drag.getModel(), true);
        });

        btn_freehand.addActionListener(e -> {
            messages.addLast("Free hand drawing active");
            draw_tools.clearSelection();
            draw_tools.setSelected(btn_freehand.getModel(), true);
        });

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        snake_dir = NORTH;
                        break;
                    case KeyEvent.VK_RIGHT:
                        snake_dir = EAST;
                        break;
                    case KeyEvent.VK_DOWN:
                        snake_dir = SOUTH;
                        break;
                    case KeyEvent.VK_LEFT:
                        snake_dir = WEST;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        btn_modify_level.addActionListener(e -> {
            if (!list_levels.isSelectionEmpty()) {
                jpanel_level_editor_menu.setVisible(false);
                jpanel_editor.setVisible(true);
                selected_level = String.valueOf(list_levels.getSelectedValue());
                lbl_level_name.setText(selected_level);
                Level level = LevelSerializer.get_all_levels().get(selected_level);
                settings.set_field_size(level.get_width(), level.get_height());
                resize_window();
                this.game_board = level.game_board;
                this.snake_coordinates.clear();
                this.snake_coordinates.add(level.head);
                this.snake_coordinates.add(level.tail);
                this.snake_direction = level.direction;
                this.lbl_level_name.setText(selected_level);
                set_enable_button_groups(true);
                this.requestFocus();
                resize_window();
                update_caller.start();
            }
        });

        btn_create_level.addActionListener(e -> {
            String identifier = "";
            Set<String> all_identifier = LevelSerializer.get_all_levels().keySet();
            while (identifier.isEmpty() || all_identifier.contains(identifier)) {
                identifier = JOptionPane
                    .showInputDialog(new JFrame(), "Please enter the level name");
                if (identifier == null) {
                    return;
                }
            }
            Integer[] field_size = field_size_map
                .get(String.valueOf(cmb_field_size.getSelectedItem()));
            settings.set_field_size(field_size[0], field_size[1]);
            Integer[][] new_board = new Integer[settings.num_rect_y][settings.num_rect_x];
            for (Integer[] row : new_board) {
                Arrays.fill(row, 0);
            }
            int mid_x = settings.field_res_width / 2 / settings.square_size;
            int mid_y = settings.field_res_height / 2 / settings.square_size;
            new_board[mid_y][mid_x] = 1;
            new_board[mid_y + 1][mid_x] = 1;

            this.snake_direction = SOUTH;
            LevelSerializer.add_gameBoard(identifier, new_board, new Integer[]{mid_x, mid_y + 1},
                new Integer[]{mid_x, mid_y}, is_modifiable, SOUTH, identifier);
            load_identifier();
        });

        btn_delete_level.addActionListener(e -> {
            if (!list_levels.isSelectionEmpty()) {
                LevelSerializer.remove_gameBoard(String.valueOf(list_levels.getSelectedValue()));
            }
            load_identifier();
        });

        btn_levelEditor_back.addActionListener(e -> {
            parent.setVisible(true);
            setVisible(false);
        });
    }

    /**
     * Loads only the names of each level using the LevelSerializer and adds them into the
     * list_levels in the GUI.
     */
    private void load_identifier() {
        Vector<String> levels_vec = new Vector<>();
        LevelSerializer.get_all_levels().forEach((s, level) -> {
            if (level.modifiable) {
                levels_vec.add(s);
            }
        });
        list_levels.setListData(levels_vec.toArray());
    }

    private void set_enable_button_groups(boolean b) {
        Iterator<AbstractButton> iterator = draw_tools.getElements().asIterator();
        while (iterator.hasNext()) {
            iterator.next().setEnabled(b);
        }
        iterator = tile_tools.getElements().asIterator();
        while (iterator.hasNext()) {
            iterator.next().setEnabled(b);
        }
    }

    private void createUIComponents() {
        jpanel_editor = new JPanel() {
            private void paint_message_n_msec(int n) {
                timeout.setInitialDelay(n);
                timeout.start();
            }

            private void draw_thick_rect(Graphics g, int x, int y, int width, int height,
                int thickness) {
                g.drawRect(x, y, width, height);
                for (int i = 1; i <= thickness; i++) {
                    g.drawRect(x - i, y - i, width + 2 * i, height + 2 * i);
                    g.drawRect(x + i, y + i, width - 2 * i, height - 2 * i);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int y = 0; y < settings.num_rect_y; y++) {
                    for (int x = 0; x < settings.num_rect_x; x++) {
                        switch (game_board[y][x]) {
                            case 1:
                                Integer[] head = snake_coordinates.get(0);
                                Integer[] tail = snake_coordinates.get(1);
                                int shift = settings.square_size / 2;
                                g.setColor(Color.RED);
                                g.drawLine(head[0] * settings.square_size + shift,
                                    head[1] * settings.square_size + shift,
                                    tail[0] * settings.square_size + shift,
                                    tail[1] * settings.square_size + shift);
                                g.fillOval(head[0] * settings.square_size + shift - 3,
                                    head[1] * settings.square_size + shift - 3, 6, 6);
                                break;
                            case 2:
                                g.setColor(Color.BLACK);
                                g.fillRect(x * settings.square_size + 2,
                                    y * settings.square_size + 2,
                                    settings.square_size - 4, settings.square_size - 4);
                                break;
                            case 3:
                                g.setColor(Color.WHITE);
                                g.fillRect(x * settings.square_size, y * settings.square_size,
                                    settings.square_size,
                                    settings.square_size);
                                g.setColor(Color.RED);
                                g.fillOval(x * settings.square_size + 2,
                                    y * settings.square_size + 2,
                                    settings.square_size - 4, settings.square_size - 4);
                                break;
                            default:
                                break;
                        }
                        if (x == x_pos_mouse / settings.square_size
                            && y == y_pos_mouse / settings.square_size) {
                            if (tile_tools.getSelection().getMnemonic() == 1) {
                                g.setColor(Color.DARK_GRAY);
                                draw_thick_rect(g, x * settings.square_size,
                                    y * settings.square_size,
                                    settings.square_size, settings.square_size, 2);
                                int[] point = get_dir(snake_dir, x, y);
                                draw_thick_rect(g, point[0] * settings.square_size,
                                    point[1] * settings.square_size,
                                    settings.square_size, settings.square_size, 2);
                            } else {
                                g.setColor(Color.DARK_GRAY);
                                draw_thick_rect(g, x * settings.square_size,
                                    y * settings.square_size,
                                    settings.square_size, settings.square_size, 2);
                            }
                        }
                        g.setColor(Color.BLACK);
                        g.drawRect(x * settings.square_size, y * settings.square_size,
                            settings.square_size,
                            settings.square_size);
                        if (mouse_pressed && tile_tools.getSelection().getMnemonic() != 1) {
                            if (draw_tools.getSelection().getMnemonic() == 0) {
                                g.setColor(Color.RED);
                                draw_thick_rect(g, Math.min(x_pos_anchor, x_pos_mouse),
                                    Math.min(y_pos_anchor, y_pos_mouse),
                                    Math.abs(x_pos_anchor - x_pos_mouse),
                                    Math.abs(y_pos_anchor - y_pos_mouse), 1);
                            } else {
                                if (y_pos_mouse < settings.field_res_height
                                    && x_pos_mouse < settings.field_res_width) {
                                    game_board[y_pos_mouse / settings.square_size][x_pos_mouse
                                        / settings.square_size] = tile_tools.getSelection()
                                        .getMnemonic();
                                }
                            }
                        }
                        if (!messages.isEmpty()) {
                            String message = messages.peekFirst();
                            g.setFont(g.getFont().deriveFont(15f));
                            int font_width = g.getFontMetrics().stringWidth(message);
                            int font_height = g.getFontMetrics().getHeight();
                            g.setColor(Color.BLACK);
                            g.fillRect(5, 10, font_width + 10, font_height + 10);
                            g.setColor(Color.WHITE);
                            g.drawString(message, 10, 10 + font_height);
                            paint_message_n_msec(4000);
                        }
                    }
                }
            }
        };
    }

    private int[] get_dir(int direction, int x, int y) {
        int[] point = new int[2];
        switch (direction) {
            case 0:
                point[0] = x;
                if (y - 1 < 0) {
                    point[1] = settings.num_rect_y - 1;
                } else {
                    point[1] = y - 1;
                }
                break;
            case 1:
                point[0] = (x + 1) % settings.num_rect_x;
                point[1] = y;
                break;
            case 2:
                point[0] = x;
                point[1] = (y + 1) % settings.num_rect_y;
                break;
            case 3:
                if (x - 1 < 0) {
                    point[0] = settings.num_rect_x - 1;
                } else {
                    point[0] = x - 1;
                }
                point[1] = y;
                break;
        }
        return point;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / settings.square_size;
        int y = e.getY() / settings.square_size;
        if (tile_tools.getSelection().getMnemonic() == 1) {
            int[] point = get_dir(snake_dir, x, y);
            int s1 = game_board[y][x];
            int s2 = game_board[point[1]][point[0]];
            if (s1 == 0 && s2 == 0) {
                snake_coordinates.parallelStream().forEach((p) -> game_board[p[1]][p[0]] = 0);
                snake_coordinates.clear();
                snake_coordinates.add(new Integer[]{point[0], point[1]});
                snake_coordinates.add(new Integer[]{x, y});
                game_board[y][x] = 1;
                game_board[point[1]][point[0]] = 1;
                this.snake_direction = this.snake_dir;
            } else {
                messages.addLast("Snake cannot be placed here");
            }
        } else {
            if (e.getX() < settings.field_res_width && e.getY() < settings.field_res_height) {
                int s1 = game_board[y][x];
                if (s1 != 1) {
                    game_board[y][x] = tile_tools.getSelection().getMnemonic();
                }
            }

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draw_tools.getSelection().getMnemonic() == 0
            && tile_tools.getSelection().getMnemonic() != 1
            && mouse_pressed) {
            for (int xi = (Math.min(x_pos_anchor, x_pos_mouse) / settings.square_size);
                xi <= (Math.max(x_pos_anchor, x_pos_mouse) / settings.square_size); xi++) {
                for (int yi = (Math.min(y_pos_anchor, y_pos_mouse) / settings.square_size);
                    yi <= (Math.max(y_pos_anchor, y_pos_mouse) / settings.square_size); yi++) {
                    int s1 = game_board[yi][xi];
                    if (s1 != 1) {
                        game_board[yi][xi] = tile_tools.getSelection().getMnemonic();
                    }
                }
            }
        }
        mouse_pressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draw_tools.getSelection().getMnemonic() == 0) {
            if (!mouse_pressed) {
                mouse_pressed = true;
                x_pos_anchor = e.getX();
                y_pos_anchor = e.getY();
            } else {
                int x = e.getX();
                int y = e.getY();
                if (x <= settings.field_res_width && y <= settings.field_res_height && x >= 0
                    && y >= 0) {
                    x_pos_mouse = x;
                    y_pos_mouse = y;
                }
            }
        } else {
            mouse_pressed = true;
            int x = e.getX();
            int y = e.getY();
            if (x <= settings.field_res_width && y <= settings.field_res_height && x >= 0
                && y >= 0) {
                x_pos_mouse = x;
                y_pos_mouse = y;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (x <= settings.field_res_width && y <= settings.field_res_height) {
            x_pos_mouse = x;
            y_pos_mouse = y;
        } else {
            x_pos_mouse = -1;
            y_pos_mouse = -1;
        }
    }
}