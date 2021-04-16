package src.mainclasses;

import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import src.gameclasses.Settings;
import src.utilityclasses.SettingsSerializer;


/*

 .M"""bgd                      `7MM
,MI    "Y                        MM
`MMb.     `7MMpMMMb.   .gP"Ya    MM  ,MP'
  `YMMNq.   MM    MM  ,M'   Yb   MM ;Y
.     `MM   MM    MM  8M""""""   MM;Mm
Mb     dM   MM    MM  YM.    ,   MM `Mb.
P"Ybmmd"  .JMML  JMML. `Mbmmd' .JMML. YA.

              the Game

Made by Arne Stoll and Frederik Phillips.
Created as a project during the computer science bachelors.

Including:
  - Level editor to get most out of hour long snek game sessions.
  - Various game modes if you ever wanted to shoot apples with a gun instead of eating them.
  - Creative and outstanding visuals with a self drawn snake.
  - Bumpy music to jam out to.
  - Leaderboard to keep track of your sick highscores.

*/
public class Main extends JDialog {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
            | IllegalAccessException e) {
            e.printStackTrace();
        }
        Main startWindow = new Main();
        Settings settings = SettingsSerializer.load_file();
        if (settings == null) {
            settings = new Settings();
        }
        new GameEnvironment(settings).setVisible(true);
        startWindow.setVisible(false);
    }
}
