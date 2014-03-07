/*
 *  Copyright (C) 2010-2014 JPEXS
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicColorChooserUI;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;
import org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;

/**
 * Contains methods for GUI
 *
 * @author JPEXS
 */
public class View {

    public static final Color DEFAULT_BACKGROUND_COLOR = new Color(217, 231, 250);
    public static Color swfBackgroundColor = DEFAULT_BACKGROUND_COLOR;

    
    private static final BufferedImage transparentTexture;
    public static final TexturePaint transparentPaint;
    
    private static final Color transparentColor1 = new Color(0x99,0x99,0x99);
    private static final Color transparentColor2 = new Color(0x66,0x66,0x66);
    
    static{
        transparentTexture = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g=transparentTexture.getGraphics();
        g.setColor(transparentColor1);
        g.fillRect(0, 0, 16, 16);
        g.setColor(transparentColor2);
        g.fillRect(0, 0, 8, 8);
        g.fillRect(8, 8, 8, 8);
        transparentPaint = new TexturePaint(View.transparentTexture, new Rectangle(0,0,transparentTexture.getWidth(),transparentTexture.getHeight()));
    }
    
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

        execInEventDispatch(new Runnable() {
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
                    UIManager.put("TextField.background", Color.WHITE);
                    UIManager.put("FormattedTextField.background", Color.WHITE);

                    FontPolicy pol = SubstanceLookAndFeel.getFontPolicy();
                    final FontSet fs = pol.getFontSet("Substance", null);

                    //Restore default font for chinese characters
                    SubstanceLookAndFeel.setFontPolicy(new FontPolicy() {

                        private final FontSet fontSet = new FontSet() {

                            private FontUIResource controlFont;
                            private FontUIResource menuFont;
                            private FontUIResource titleFont;
                            private FontUIResource windowTitleFont;
                            private FontUIResource smallFont;
                            private FontUIResource messageFont;

                            @Override
                            public FontUIResource getControlFont() {
                                if (controlFont == null) {
                                    FontUIResource f = fs.getControlFont();
                                    controlFont = new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                }
                                return controlFont;
                            }

                            @Override
                            public FontUIResource getMenuFont() {
                                if (menuFont == null) {
                                    FontUIResource f = fs.getMenuFont();
                                    menuFont = new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                }
                                return menuFont;
                            }

                            @Override
                            public FontUIResource getTitleFont() {
                                if (titleFont == null) {
                                    FontUIResource f = fs.getTitleFont();
                                    titleFont = new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                }
                                return titleFont;
                            }

                            @Override
                            public FontUIResource getWindowTitleFont() {
                                if (windowTitleFont == null) {
                                    FontUIResource f = fs.getWindowTitleFont();
                                    windowTitleFont = new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                }
                                return windowTitleFont;
                            }

                            @Override
                            public FontUIResource getSmallFont() {
                                if (smallFont == null) {
                                    FontUIResource f = fs.getSmallFont();
                                    smallFont = new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                }
                                return smallFont;
                            }

                            @Override
                            public FontUIResource getMessageFont() {
                                if (messageFont == null) {
                                    FontUIResource f = fs.getMessageFont();
                                    messageFont = new FontUIResource(defaultFont.getName(), f.getStyle(), f.getSize());
                                }
                                return messageFont;
                            }
                        };

                        @Override
                        public FontSet getFontSet(String string, UIDefaults uid) {
                            return fontSet;
                        }
                    });
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

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
        centerScreen(f, 0); // todo, set screen to the currently active screen instead of the first screen in a multi screen setup, (maybe by using the screen where the main window is now classic or ribbon?)
    }

    public static void centerScreen(Window f, int screen) {

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allDevices = env.getScreenDevices();
        int topLeftX, topLeftY, screenX, screenY, windowPosX, windowPosY;

        if (screen < allDevices.length && screen > -1) {
            topLeftX = allDevices[screen].getDefaultConfiguration().getBounds().x;
            topLeftY = allDevices[screen].getDefaultConfiguration().getBounds().y;

            screenX = allDevices[screen].getDefaultConfiguration().getBounds().width;
            screenY = allDevices[screen].getDefaultConfiguration().getBounds().height;
        } else {
            topLeftX = allDevices[0].getDefaultConfiguration().getBounds().x;
            topLeftY = allDevices[0].getDefaultConfiguration().getBounds().y;

            screenX = allDevices[0].getDefaultConfiguration().getBounds().width;
            screenY = allDevices[0].getDefaultConfiguration().getBounds().height;
        }

        windowPosX = ((screenX - f.getWidth()) / 2) + topLeftX;
        windowPosY = ((screenY - f.getHeight()) / 2) + topLeftY;

        f.setLocation(windowPosX, windowPosY);
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

    public static MyResizableIcon getMyResizableIcon(String resource) {
        try {
            return new MyResizableIcon(ImageIO.read(View.class.getResourceAsStream("/com/jpexs/decompiler/flash/gui/graphics/" + resource + ".png")));
        } catch (IOException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void execInEventDispatch(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
                Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void execInEventDispatchLater(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
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

    public static SubstanceColorScheme getColorScheme() {
        return SubstanceColorSchemeUtilities.getActiveColorScheme(new JButton(), ComponentState.ENABLED);
    }

    public static void refreshTree(JTree tree, TreeModel model) {
        List<List<String>> expandedNodes = getExpandedNodes(tree);
        tree.setModel(model);
        expandTreeNodes(tree, expandedNodes);
    }

    private static List<List<String>> getExpandedNodes(JTree tree) {
        List<List<String>> expandedNodes = new ArrayList<>();
        int rowCount = tree.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            TreePath path = tree.getPathForRow(i);
            if (tree.isExpanded(path)) {
                List<String> pathAsStringList = new ArrayList<>();
                for (Object pathCompnent : path.getPath()) {
                    pathAsStringList.add(pathCompnent.toString());
                }
                expandedNodes.add(pathAsStringList);
            }
        }
        return expandedNodes;
    }

    private static void expandTreeNodes(JTree tree, List<List<String>> pathsToExpand) {
        for (List<String> pathAsStringList : pathsToExpand) {
            expandTreeNode(tree, pathAsStringList);
        }
    }

    private static void expandTreeNode(JTree tree, List<String> pathAsStringList) {
        TreeModel model = tree.getModel();
        Object node = model.getRoot();

        if (pathAsStringList.isEmpty()) {
            return;
        }
        if (!pathAsStringList.get(0).equals(node.toString())) {
            return;
        }

        List<Object> path = new ArrayList<>();
        path.add(node);

        for (int i = 1; i < pathAsStringList.size(); i++) {
            String name = pathAsStringList.get(i);
            int childCount = model.getChildCount(node);
            for (int j = 0; j < childCount; j++) {
                Object child = model.getChild(node, j);
                if (child.toString().equals(name)) {
                    node = child;
                    path.add(node);
                    break;
                }
            }
        }

        TreePath tp = new TreePath(path.toArray(new Object[path.size()]));
        tree.expandPath(tp);
    }
}
