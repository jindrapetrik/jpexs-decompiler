/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Contains methods for GUI
 *
 * @author JPEXS
 */
public class View {

    /**
     * Sets windows Look and Feel
     */
    public static void setLookAndFeel() {
        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException ignored) {
        } catch (ClassNotFoundException ignored) {
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        }


        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }

    /**
     * Loads image from resources
     *
     * @param name Name of the image
     * @return loaded Image
     */
    public static Image loadImage(String name) {
        java.net.URL imageURL = View.class.getResource("/com/jpexs/decompiler/flash/gui/graphics/" + name + ".png");
        return Toolkit.getDefaultToolkit().createImage(imageURL);
    }

    /**
     * Sets icon of specified frame to ASDec icon
     *
     * @param f Frame to set icon in
     */
    public static void setWindowIcon(Window f) {
        java.util.List<Image> images = new ArrayList<Image>();
        images.add(loadImage("icon16"));
        images.add(loadImage("icon32"));
        images.add(loadImage("icon48"));
        f.setIconImages(images);
    }

    /**
     * Centers specified frame on the screen
     *
     * @param f Frame to center on the screen
     */
    public static void centerScreen(Window f) {
        Dimension dim = f.getToolkit().getScreenSize();
        Rectangle abounds = f.getBounds();
        f.setLocation((dim.width - abounds.width) / 2,
                (dim.height - abounds.height) / 2);
    }

    public static ImageIcon getIcon(String name) {
        return new ImageIcon(View.class.getClassLoader().getResource("com/jpexs/decompiler/flash/gui/graphics/" + name + ".png"));
    }
}
