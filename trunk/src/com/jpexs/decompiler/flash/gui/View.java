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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicColorChooserUI;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;
import org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel;

/**
 * Contains methods for GUI
 *
 * @author JPEXS
 */
public class View {

    public static final Color DEFAULT_BACKGROUND_COLOR = new Color(217, 231, 250);
    public static Color swfBackgroundColor = DEFAULT_BACKGROUND_COLOR;

    /**
     * Sets windows Look and Feel
     */
    public static void setLookAndFeel() {

        //Save default font for Chinese characters
        final Font defaultFont = (new JLabel()).getFont();
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
        }
        try {

            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {

                    try {
                        UIManager.setLookAndFeel(new SubstanceOfficeBlue2007LookAndFeel());
                        UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 0.999);//This works for not changing labels color and not changing Dialogs title
                        UIManager.put("Tree.expandedIcon", getIcon("expand16"));
                        UIManager.put("Tree.collapsedIcon", getIcon("collapse16"));
                        UIManager.put("ColorChooserUI", BasicColorChooserUI.class.getName());
                        UIManager.put("ColorChooser.swatchesRecentSwatchSize", new Dimension(20, 20));
                        UIManager.put("ColorChooser.swatchesSwatchSize", new Dimension(20, 20));
                        UIManager.put("RibbonApplicationMenuPopupPanelUI", MyRibbonApplicationMenuPopupPanelUI.class.getName());
                        UIManager.put("RibbonApplicationMenuButtonUI", MyRibbonApplicationMenuButtonUI.class.getName());
                        UIManager.put("ProgressBarUI", MyProgressBarUI.class.getName());

                        FontPolicy pol = SubstanceLookAndFeel.getFontPolicy();
                        final FontSet fs = pol.getFontSet("Substance", null);

                        //Restore default font for chinese characters
                        SubstanceLookAndFeel.setFontPolicy(new FontPolicy() {
                            @Override
                            public FontSet getFontSet(String string, UIDefaults uid) {
                                return new FontSet() {
                                    @Override
                                    public FontUIResource getControlFont() {
                                        FontUIResource f = fs.getControlFont();
                                        return new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                    }

                                    @Override
                                    public FontUIResource getMenuFont() {
                                        FontUIResource f = fs.getMenuFont();
                                        return new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                    }

                                    @Override
                                    public FontUIResource getTitleFont() {
                                        FontUIResource f = fs.getTitleFont();
                                        return new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                    }

                                    @Override
                                    public FontUIResource getWindowTitleFont() {
                                        FontUIResource f = fs.getWindowTitleFont();
                                        return new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                    }

                                    @Override
                                    public FontUIResource getSmallFont() {
                                        FontUIResource f = fs.getSmallFont();
                                        return new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                    }

                                    @Override
                                    public FontUIResource getMessageFont() {
                                        FontUIResource f = fs.getMessageFont();
                                        return new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                    }
                                };
                            }
                        });
                    } catch (UnsupportedLookAndFeelException ex) {
                        Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        }


        UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, SubstanceConstants.TabContentPaneBorderKind.SINGLE_FULL);



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
        java.util.List<Image> images = new ArrayList<>();
        images.add(loadImage("icon16"));
        images.add(loadImage("icon32"));
        images.add(loadImage("icon48"));
        images.add(loadImage("icon256"));
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
    private static final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private static final String dispatchWindowClosingActionMapKey = "com.jpexs.dispatch:WINDOW_CLOSING";

    public static void installEscapeCloseOperation(final JDialog dialog) {
        Action dispatchClosing = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.dispatchEvent(new WindowEvent(
                        dialog, WindowEvent.WINDOW_CLOSING));
            }
        };
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                escapeStroke, dispatchWindowClosingActionMapKey);
        root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing);
    }

    public static ImageWrapperResizableIcon getResizableIcon(String resource) {
        return ImageWrapperResizableIcon.getIcon(View.class.getResource("/com/jpexs/decompiler/flash/gui/graphics/" + resource + ".png"), new Dimension(256, 256));
    }

    public static void execInEventDispatch(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException | InvocationTargetException ex) {
                Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static int showOptionDialog(final Component parentComponent, final Object message, final String title, final int optionType, final int messageType, final Icon icon, final Object[] options, final Object initialValue) {
        final int[] ret = new int[1];
        execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                ret[0] = JOptionPane.showOptionDialog(parentComponent, message, title, optionType, messageType, icon, options, initialValue);
            }
        });
        return ret[0];
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title, final int optionType) {
        return showConfirmDialog(parentComponent, message, title, optionType, JOptionPane.PLAIN_MESSAGE);
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title, final int optionType, final int messageTyp) {
        final int ret[] = new int[1];
        execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                ret[0] = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageTyp);
            }
        });
        return ret[0];
    }

    public static void showMessageDialog(final Component parentComponent, final Object message, final String title, final int messageType) {
        execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
            }
        });
    }

    public static void showMessageDialog(final Component parentComponent, final Object message) {
        execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentComponent, message);
            }
        });
    }

    public static String showInputDialog(final Object message, final Object initialSelection) {
        final String[] ret = new String[1];
        execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                ret[0] = JOptionPane.showInputDialog(message, initialSelection);
            }
        });
        return ret[0];
    }
}
