/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.TexturePaint;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicColorChooserUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
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

    public static Color getDefaultBackgroundColor() {
        if (Configuration.useRibbonInterface.get()) {
            return SubstanceLookAndFeel.getCurrentSkin().getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor();
        } else {
            return SystemColor.control;
        }
    }

    private static Color swfBackgroundColor = null;

    public static void setSwfBackgroundColor(Color swfBackgroundColor) {
        View.swfBackgroundColor = swfBackgroundColor;
    }

    public static Color getSwfBackgroundColor() {
        if (swfBackgroundColor == null) {
            return getDefaultBackgroundColor();
        }
        return swfBackgroundColor;
    }

    private static final BufferedImage transparentTexture;

    public static final TexturePaint transparentPaint;

    private static FontPolicy defaultFontPolicy;

    private static final Color transparentColor1 = new Color(0x99, 0x99, 0x99);

    private static final Color transparentColor2 = new Color(0x66, 0x66, 0x66);

    static {
        transparentTexture = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = transparentTexture.getGraphics();
        g.setColor(transparentColor1);
        g.fillRect(0, 0, 16, 16);
        g.setColor(transparentColor2);
        g.fillRect(0, 0, 8, 8);
        g.fillRect(8, 8, 8, 8);
        transparentPaint = new TexturePaint(View.transparentTexture, new Rectangle(0, 0, transparentTexture.getWidth(), transparentTexture.getHeight()));
    }

    /**
     * Sets windows Look and Feel
     */
    public static void setLookAndFeel() {

        // Save default font for Chinese characters
        final Font defaultFont = (new JLabel()).getFont();
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
        }

        try {
            LookAndFeel oldLookAndFeel = UIManager.getLookAndFeel();
            if (!(oldLookAndFeel instanceof SubstanceOfficeBlue2007LookAndFeel)) {
                UIManager.setLookAndFeel(new SubstanceOfficeBlue2007LookAndFeel());
                oldLookAndFeel.uninitialize();
            }

            SubstanceSkin currentSkin = SubstanceLookAndFeel.getCurrentSkin();
            if (currentSkin != null) {
                String currentSkinName = currentSkin.getClass().getName();
                String newSkinName = Configuration.guiSkin.get();
                if (!currentSkinName.equals(newSkinName)) {
                    SubstanceLookAndFeel.setSkin(newSkinName);
                }
            } else {
                Logger.getLogger(View.class.getName()).log(Level.SEVERE, "Current skin is null");
                SubstanceLookAndFeel.setSkin("com.jpexs.decompiler.flash.gui.OceanicSkin");
            }

            UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 0.999);//This works for not changing labels color and not changing Dialogs title
            UIManager.put("Tree.expandedIcon", getIcon("expand16"));
            UIManager.put("Tree.collapsedIcon", getIcon("collapse16"));
            UIManager.put("ColorChooserUI", BasicColorChooserUI.class.getName());
            UIManager.put("ColorChooser.swatchesRecentSwatchSize", new Dimension(20, 20));
            UIManager.put("ColorChooser.swatchesSwatchSize", new Dimension(20, 20));
            UIManager.put("RibbonApplicationMenuPopupPanelUI", MyRibbonApplicationMenuPopupPanelUI.class.getName());
            UIManager.put("RibbonApplicationMenuButtonUI", MyRibbonApplicationMenuButtonUI.class.getName());
            UIManager.put("ProgressBarUI", MyProgressBarUI.class.getName());
            UIManager.put("TextField.background", Color.white);
            UIManager.put("FormattedTextField.background", Color.white);
            UIManager.put("CommandButtonUI", MyCommandButtonUI.class.getName());

            if (defaultFontPolicy == null) {
                defaultFontPolicy = SubstanceLookAndFeel.getFontPolicy();
            }

            FontPolicy pol = defaultFontPolicy;
            final FontSet fs = pol.getFontSet("Substance", null);

            double fontSizeMultiplier = Configuration.guiFontSizeMultiplier.get();

            // Restore default font for chinese characters
            SubstanceLookAndFeel.setFontPolicy(new FontPolicy() {
                private final FontSet fontSet = new FontSet() {
                    private FontUIResource controlFont;

                    private FontUIResource menuFont;

                    private FontUIResource titleFont;

                    private FontUIResource windowTitleFont;

                    private FontUIResource smallFont;

                    private FontUIResource messageFont;

                    private int getFontSize(int defaultFontSize) {
                        return (int) (defaultFontSize * fontSizeMultiplier);
                    }

                    @Override
                    public FontUIResource getControlFont() {
                        if (controlFont == null) {
                            FontUIResource f = fs.getControlFont();
                            controlFont = new FontUIResource(defaultFont.getName(), f.getStyle(), getFontSize(f.getSize()));
                        }
                        return controlFont;
                    }

                    @Override
                    public FontUIResource getMenuFont() {
                        if (menuFont == null) {
                            FontUIResource f = fs.getMenuFont();
                            menuFont = new FontUIResource(defaultFont.getName(), f.getStyle(), getFontSize(f.getSize()));
                        }
                        return menuFont;
                    }

                    @Override
                    public FontUIResource getTitleFont() {
                        if (titleFont == null) {
                            FontUIResource f = fs.getTitleFont();
                            titleFont = new FontUIResource(defaultFont.getName(), f.getStyle(), getFontSize(f.getSize()));
                        }
                        return titleFont;
                    }

                    @Override
                    public FontUIResource getWindowTitleFont() {
                        if (windowTitleFont == null) {
                            FontUIResource f = fs.getWindowTitleFont();
                            windowTitleFont = new FontUIResource(defaultFont.getName(), f.getStyle(), getFontSize(f.getSize()));
                        }
                        return windowTitleFont;
                    }

                    @Override
                    public FontUIResource getSmallFont() {
                        if (smallFont == null) {
                            FontUIResource f = fs.getSmallFont();
                            smallFont = new FontUIResource(defaultFont.getName(), f.getStyle(), getFontSize(f.getSize()));
                        }
                        return smallFont;
                    }

                    @Override
                    public FontUIResource getMessageFont() {
                        if (messageFont == null) {
                            FontUIResource f = fs.getMessageFont();
                            messageFont = new FontUIResource(defaultFont.getName(), f.getStyle(), getFontSize(f.getSize()));
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
    public static BufferedImage loadImage(String name) {
        URL imageURL = View.class.getResource("/com/jpexs/decompiler/flash/gui/graphics/" + name + ".png");
        try {
            return ImageIO.read(imageURL);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Sets icon of specified frame to ASDec icon
     *
     * @param f Frame to set icon in
     */
    public static void setWindowIcon(Window f) {
        if (Configuration.useRibbonInterface.get()) {
            List<Image> images = new ArrayList<>();
            MyResizableIcon[] icons = MyRibbonApplicationMenuButtonUI.getIcons();
            MyResizableIcon icon = icons[1];
            int[] sizes = new int[]{256, 128, 64, 42, 40, 32, 20, 16};
            for (int size : sizes) {
                icon.setIconSize(size, size);
                BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
                icon.paintIcon(f, bi.getGraphics(), 0, 0);
                images.add(bi);
            }
            f.setIconImages(images);
        } else {
            List<Image> images = new ArrayList<>();
            images.add(loadImage("icon16"));
            images.add(loadImage("icon32"));
            images.add(loadImage("icon48"));
            images.add(loadImage("icon256"));
            f.setIconImages(images);
        }
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

        GraphicsDevice[] allDevices = getEnv().getScreenDevices();
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

    public static ImageIcon getIcon(String name, int size) {
        ImageIcon icon = getIcon(getPrefferedIconName(name, size));
        if (icon.getIconWidth() == size && icon.getIconHeight() == size) {
            return icon;
        }
        icon.getImage();
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        bi.createGraphics().drawImage(icon.getImage(), 0, 0, size, size, null, null);
        return new ImageIcon(bi);
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

    public static boolean iconExists(String resource) {
        return View.class.getResource("/com/jpexs/decompiler/flash/gui/graphics/" + resource + ".png") != null;
    }

    private static String getPrefferedIconName(String resource, int preferredSize) {
        Matcher m = Pattern.compile("(.*[^0-9])([0-9]+)").matcher(resource);
        if (m.matches()) {
            int origSize = Integer.parseInt(m.group(2));
            String name = m.group(1);
            if (origSize != preferredSize) {
                if (iconExists(name + preferredSize)) {
                    return name + preferredSize;
                }
            }
        }
        return resource;
    }

    public static ImageWrapperResizableIcon getResizableIcon(String resource, int preferredSize) {
        return getResizableIcon(getPrefferedIconName(resource, preferredSize));
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

    public static void checkAccess() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("This method should be called from UI thread.");
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
        execInEventDispatch(() -> {
            ret[0] = JOptionPane.showOptionDialog(parentComponent, message, title, optionType, messageType, icon, options, initialValue);
        });
        return ret[0];
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType) {
        return showConfirmDialog(parentComponent, message, title, optionType, JOptionPane.PLAIN_MESSAGE);
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title, final int optionType, final int messageTyp) {
        final int[] ret = new int[1];
        execInEventDispatch(() -> {
            ret[0] = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageTyp);
        });
        return ret[0];
    }

    public static int showConfirmDialog(Component parentComponent, String message, String title, int optionType, ConfigurationItem<Boolean> showAgainConfig, int defaultOption) {
        return showConfirmDialog(parentComponent, message, title, optionType, JOptionPane.PLAIN_MESSAGE, showAgainConfig, defaultOption);
    }

    public static int showConfirmDialog(final Component parentComponent, String message, final String title, final int optionType, final int messageType, ConfigurationItem<Boolean> showAgainConfig, int defaultOption) {

        JCheckBox donotShowAgainCheckBox = null;
        JPanel warPanel = null;
        if (showAgainConfig != null) {
            if (!showAgainConfig.get()) {
                return defaultOption;
            }

            JLabel warLabel = new JLabel("<html>" + message.replace("\r\n", "<br>") + "</html>");
            warPanel = new JPanel(new BorderLayout());
            warPanel.add(warLabel, BorderLayout.CENTER);
            donotShowAgainCheckBox = new JCheckBox(AppStrings.translate("message.confirm.donotshowagain"));
            warPanel.add(donotShowAgainCheckBox, BorderLayout.SOUTH);
        }

        final int[] ret = new int[1];
        final Object messageObj = warPanel == null ? message : warPanel;
        execInEventDispatch(() -> {
            ret[0] = JOptionPane.showConfirmDialog(parentComponent, messageObj, title, optionType, messageType);
        });

        if (donotShowAgainCheckBox != null) {
            showAgainConfig.set(!donotShowAgainCheckBox.isSelected());
        }

        return ret[0];
    }

    public static void showMessageDialog(final Component parentComponent, final String message, final String title, final int messageType) {
        showMessageDialog(parentComponent, message, title, messageType, null);
    }

    public static void showMessageDialog(final Component parentComponent, final String message, final String title, final int messageType, ConfigurationItem<Boolean> showAgainConfig) {

        execInEventDispatch(() -> {
            Object msg = message;
            JCheckBox donotShowAgainCheckBox = null;
            if (showAgainConfig != null) {
                if (!showAgainConfig.get()) {
                    return;
                }

                JLabel warLabel = new JLabel("<html>" + message.replace("\r\n", "<br>") + "</html>");
                final JPanel warPanel = new JPanel(new BorderLayout());
                warPanel.add(warLabel, BorderLayout.CENTER);
                donotShowAgainCheckBox = new JCheckBox(AppStrings.translate("message.confirm.donotshowagain"));
                warPanel.add(donotShowAgainCheckBox, BorderLayout.SOUTH);
                msg = warPanel;
            }
            final Object fmsg = msg;

            JOptionPane.showMessageDialog(parentComponent, fmsg, title, messageType);
            if (donotShowAgainCheckBox != null) {
                showAgainConfig.set(!donotShowAgainCheckBox.isSelected());
            }
        });
    }

    public static void showMessageDialog(final Component parentComponent, final Object message) {
        execInEventDispatch(() -> {
            JOptionPane.showMessageDialog(parentComponent, message);
        });
    }

    public static String showInputDialog(final Object message, final Object initialSelection) {
        final String[] ret = new String[1];
        execInEventDispatch(() -> {
            ret[0] = JOptionPane.showInputDialog(message, initialSelection);
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

    public static List<List<String>> getExpandedNodes(JTree tree) {
        List<List<String>> expandedNodes = new ArrayList<>();
        int rowCount = tree.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            try {
                TreePath path = tree.getPathForRow(i);
                if (tree.isExpanded(path)) {
                    List<String> pathAsStringList = new ArrayList<>();
                    for (Object pathCompnent : path.getPath()) {
                        pathAsStringList.add(pathCompnent.toString());
                    }
                    expandedNodes.add(pathAsStringList);
                }
            } catch (IndexOutOfBoundsException | NullPointerException ex) {
                // TreeNode was removed, ignore
            }
        }
        return expandedNodes;
    }

    public static void expandTreeNodes(JTree tree, List<List<String>> pathsToExpand) {
        for (List<String> pathAsStringList : pathsToExpand) {
            expandTreeNode(tree, pathAsStringList);
        }
    }

    private static TreePath expandTreeNode(JTree tree, List<String> pathAsStringList) {
        TreePath tp = getTreePathByPathStrings(tree, pathAsStringList);
        tree.expandPath(tp);
        return tp;
    }

    public static TreePath getTreePathByPathStrings(JTree tree, List<String> pathAsStringList) {
        TreeModel model = tree.getModel();
        if (model == null) {
            return null;
        }

        Object node = model.getRoot();

        if (pathAsStringList.isEmpty()) {
            return null;
        }
        if (!pathAsStringList.get(0).equals(node.toString())) {
            return null;
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
        return tp;
    }

    public static void expandTreeNodes(JTree tree, TreePath parent, boolean expand) {
        expandTreeNodesRecursive(tree, parent, expand);
    }

    private static void expandTreeNodesRecursive(JTree tree, TreePath parent, boolean expand) {
        TreeModel model = tree.getModel();

        Object node = parent.getLastPathComponent();
        int childCount = model.getChildCount(node);
        for (int j = 0; j < childCount; j++) {
            Object child = model.getChild(node, j);
            TreePath path = parent.pathByAddingChild(child);
            expandTreeNodesRecursive(tree, path, expand);
        }

        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public static void addEditorAction(JEditorPane editor, AbstractAction a, String key, String name, String keyStroke) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        a.putValue(Action.ACCELERATOR_KEY, ks);
        a.putValue(Action.NAME, name);

        String actionName = key;
        ActionMap amap = editor.getActionMap();
        InputMap imap = editor.getInputMap(JTextComponent.WHEN_FOCUSED);
        imap.put(ks, actionName);
        amap.put(actionName, a);

        JPopupMenu pmenu = editor.getComponentPopupMenu();
        JMenuItem findUsagesMenu = new JMenuItem(a);
        pmenu.add(findUsagesMenu);
    }

    public static boolean navigateUrl(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    URI uri = new URI(url);
                    desktop.browse(uri);
                    return true;
                } catch (URISyntaxException | IOException ex) {
                    Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return false;
    }

    public static JTable autoResizeColWidth(final JTable table, final TableModel model) {
        View.execInEventDispatch(() -> {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setModel(model);

            int margin = 5;

            for (int i = 0; i < table.getColumnCount(); i++) {
                int vColIndex = i;
                DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
                TableColumn col = colModel.getColumn(vColIndex);
                int width;

                // Get width of column header
                TableCellRenderer renderer = col.getHeaderRenderer();

                if (renderer == null) {
                    renderer = table.getTableHeader().getDefaultRenderer();
                }

                Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

                width = comp.getPreferredSize().width;

                // Get maximum width of column data
                for (int r = 0; r < table.getRowCount(); r++) {
                    renderer = table.getCellRenderer(r, vColIndex);
                    comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
                            r, vColIndex);
                    width = Math.max(width, comp.getPreferredSize().width);
                }

                // Add margin
                width += 2 * margin;

                // Set the width
                col.setPreferredWidth(width);
            }

            ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
                    SwingConstants.LEFT);

            // table.setAutoCreateRowSorter(true);
            table.getTableHeader().setReorderingAllowed(false);
        });

        return table;
    }

    private static GraphicsEnvironment env;

    public static GraphicsEnvironment getEnv() {
        if (env == null) {
            env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        }
        return env;
    }

    private static GraphicsConfiguration conf;

    public static GraphicsConfiguration getDefaultConfiguration() {
        if (conf == null) {
            conf = getEnv().getDefaultScreenDevice().getDefaultConfiguration();
        }
        return conf;
    }

    public static BufferedImage toCompatibleImage(BufferedImage image) {
        if (image.getColorModel().equals(getDefaultConfiguration().getColorModel())) {
            return image;
        }

        return getDefaultConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());
    }

    public static VolatileImage createRenderImage(int width, int height, int transparency) {
        VolatileImage image = getDefaultConfiguration().createCompatibleVolatileImage(width, height, transparency);

        int valid = image.validate(getDefaultConfiguration());

        if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
            image = createRenderImage(width, height, transparency);
            return image;
        }

        return image;
    }
}
