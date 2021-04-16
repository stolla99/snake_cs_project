package src.utilityclasses;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Draws an icon onto an JButton. All image files are assumed to be in the folder png_icon/ . Before
 * the icon is drawn onto the GUI component the image is scaled down to a smaller resolution.
 * Accepts common image files like: .jpg and .png.
 */
public class IconDrawer {

    private static final String root_dir = "png_icon/";

    /**
     * Method loads the image file into an Image object and scales the image by 0.5 before adding
     * the image as the icon to the component (first parameter).
     */
    public static void set_icon(JButton component, String icon_file_name) {
        try {
            ClassLoader cl = IconDrawer.class.getClassLoader();
            InputStream cc = cl.getResourceAsStream(root_dir + icon_file_name);
            if (cc != null) {
                Image img = ImageIO.read(cc);
                int r = component.getHeight() / 2;
                img = img.getScaledInstance(r, r, Image.SCALE_SMOOTH);
                ImageIcon ic = new ImageIcon(img);
                component.setIcon(ic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
