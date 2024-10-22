/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTreeModel;
import com.jpexs.decompiler.flash.gui.translator.Translator;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
import javax.swing.plaf.TextUI;
import javax.swing.plaf.basic.BasicColorChooserUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
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
        if (Configuration.useRibbonInterface.get() && SubstanceLookAndFeel.getCurrentSkin() != null) {
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

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ignored) {
            //ignored
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

            UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 0.999); //This works for not changing labels color and not changing Dialogs title
            if (View.isOceanic()) {
                UIManager.put("Tree.expandedIcon", getIcon("expand16"));
                UIManager.put("Tree.collapsedIcon", getIcon("collapse16"));
            }
            UIManager.put("ColorChooserUI", BasicColorChooserUI.class.getName());
            UIManager.put("ColorChooser.swatchesRecentSwatchSize", new Dimension(20, 20));
            UIManager.put("ColorChooser.swatchesSwatchSize", new Dimension(20, 20));
            UIManager.put("RibbonApplicationMenuPopupPanelUI", MyRibbonApplicationMenuPopupPanelUI.class.getName());
            UIManager.put("RibbonApplicationMenuButtonUI", MyRibbonApplicationMenuButtonUI.class.getName());
            UIManager.put("ProgressBarUI", MyProgressBarUI.class.getName());
            if (View.isOceanic()) {
                UIManager.put("TextField.background", Color.white);
                UIManager.put("FormattedTextField.background", Color.white);
            }
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
     * Sets icon of the window.
     *
     * @param f
     * @param icon Icon identifier. Icon must exist in 16 and 32 variant
     */
    public static void setWindowIcon(Window f, String icon) {
        List<Image> images = new ArrayList<>();
        images.add(loadImage(icon + "16"));
        images.add(loadImage(icon + "32"));
        f.setIconImages(images);
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

    public static void centerScreenMain(Window f) {
        centerScreen(f, true);
    }

    /**
     * Centers specified frame on the screen
     *
     * @param f Frame to center on the screen
     */
    public static void centerScreen(Window f) {
        centerScreen(f, false);
    }

    public static void centerScreen(Window f, boolean mainWindow) {
        int topLeftX;
        int topLeftY;
        int screenX;
        int screenY;
        int windowPosX;
        int windowPosY;
        GraphicsDevice device;

        if (mainWindow || Main.getMainFrame() == null) {
            device = getMainDefaultScreenDevice();
        } else {
            device = getWindowDevice(Main.getMainFrame().getWindow());
        }

        topLeftX = device.getDefaultConfiguration().getBounds().x;
        topLeftY = device.getDefaultConfiguration().getBounds().y;

        screenX = device.getDefaultConfiguration().getBounds().width;
        screenY = device.getDefaultConfiguration().getBounds().height;

        Insets bounds = Toolkit.getDefaultToolkit().getScreenInsets(device.getDefaultConfiguration());
        screenX = screenX - bounds.right;
        screenY = screenY - bounds.bottom;

        windowPosX = ((screenX - f.getWidth()) / 2) + topLeftX;
        windowPosY = ((screenY - f.getHeight()) / 2) + topLeftY;

        f.setLocation(windowPosX, windowPosY);
    }

    public static void saveScreen() {
        MainFrame mainFrame = Main.getMainFrame();
        if (mainFrame == null) {
            return;
        }
        Window w = mainFrame.getWindow();
        if (w == null) {
            return;
        }
        GraphicsDevice device = getWindowDevice(w);

        GraphicsDevice[] allDevices = getEnv().getScreenDevices();
        int deviceIndex = -1;
        for (int i = 0; i < allDevices.length; i++) {
            if (allDevices[i] == device) {
                deviceIndex = i;
                break;
            }
        }
        if (deviceIndex != -1) {
            Configuration.lastMainWindowScreenIndex.set(deviceIndex);
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            Configuration.lastMainWindowScreenX.set(bounds.x);
            Configuration.lastMainWindowScreenY.set(bounds.y);
            Configuration.lastMainWindowScreenWidth.set(bounds.width);
            Configuration.lastMainWindowScreenHeight.set(bounds.height);
        }
    }

    public static GraphicsDevice getMainDefaultScreenDevice() {
        if (!Configuration.rememberLastScreen.get()) {
            return getEnv().getDefaultScreenDevice();
        }

        int deviceIndex = Configuration.lastMainWindowScreenIndex.get();
        GraphicsDevice[] allDevices = getEnv().getScreenDevices();

        if (deviceIndex >= allDevices.length || deviceIndex == -1) {
            return getEnv().getDefaultScreenDevice();
        }

        Rectangle expectedBounds = allDevices[deviceIndex].getDefaultConfiguration().getBounds();
        if (Configuration.lastMainWindowScreenX.get() != expectedBounds.x
                || Configuration.lastMainWindowScreenY.get() != expectedBounds.y
                || Configuration.lastMainWindowScreenWidth.get() != expectedBounds.width
                || Configuration.lastMainWindowScreenHeight.get() != expectedBounds.height) {
            return getEnv().getDefaultScreenDevice();
        }
        return allDevices[deviceIndex];
    }

    public static int getWindowDeviceIndex(Window window) {
        GraphicsDevice device = getWindowDevice(window);
        GraphicsDevice[] allDevices = getEnv().getScreenDevices();
        for (int i = 0; i < allDevices.length; i++) {
            if (allDevices[i] == device) {
                return i;
            }
        }
        return -1;
    }

    public static GraphicsDevice getWindowDevice(Window window) {
        Rectangle bounds = window.getBounds();
        return Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()).stream()
                // pick devices where window located
                .filter(d -> d.getDefaultConfiguration().getBounds().intersects(bounds))
                // sort by biggest intersection square
                .sorted((f, s) -> Long.compare(//
                square(f.getDefaultConfiguration().getBounds().intersection(bounds)),
                square(s.getDefaultConfiguration().getBounds().intersection(bounds))))
                // use one with the biggest part of the window
                .reduce((f, s) -> s) //

                // fallback to default device
                .orElse(window.getGraphicsConfiguration().getDevice());
    }

    private static long square(Rectangle rec) {
        return Math.abs(rec.width * rec.height);
    }

    public static ImageIcon getIcon(String name, int size) {
        ImageIcon icon = getIcon(getPreferredIconName(name, size));
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

    private static String getPreferredIconName(String resource, int preferredSize) {
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
        return getResizableIcon(getPreferredIconName(resource, preferredSize));
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
                //ignored
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
                    for (Object pathComponent : path.getPath()) {
                        pathAsStringList.add(pathComponent.toString());
                    }
                    expandedNodes.add(pathAsStringList);
                }
            } catch (IndexOutOfBoundsException | NullPointerException ex) {
                // TreeNode was removed, ignore
            }
        }
        return expandedNodes;
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
                String childStr = child.toString();
                int index = 1;
                if (model instanceof AbstractTagTreeModel) {
                    AbstractTagTreeModel aModel = (AbstractTagTreeModel) model;
                    index = aModel.getItemIndex((TreeItem) child);
                    if (index > 1) {
                        childStr += " [" + index + "]";
                    }
                }
                if (childStr.equals(name)) {
                    node = child;
                    path.add(node);
                    break;
                }
            }
        }

        TreePath tp = new TreePath(path.toArray(new Object[path.size()]));
        return tp;
    }

    public static void expandTreeNodes(JTree tree, List<List<String>> pathsToExpand) {
        for (List<String> pathAsStringList : pathsToExpand) {
            expandTreeNode(tree, pathAsStringList);
        }
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

    public static boolean isOceanic() {
        return SubstanceLookAndFeel.getCurrentSkin() instanceof OceanicSkin;
    }

    /**
     * This calls JTextComponent.viewToModel2D on Java 9+ or viewToModel on Java
     * 8.
     *
     * @param editor
     * @param pt
     * @return
     */
    public static int textComponentViewToModel(JTextComponent editor, Point2D pt) {
        try {
            return (int) (Integer) JTextComponent.class.getDeclaredMethod("viewToModel2D", Point2D.class).invoke(editor, pt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            //method does not exist, we must be on Java8
        }

        //Try older method
        Point p = new Point((int) Math.round(pt.getX()), (int) Math.round(pt.getY()));
        try {
            return (int) (Integer) JTextComponent.class.getDeclaredMethod("viewToModel", Point.class).invoke(editor, p);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    /**
     * This calls JTextComponent.viewToModel2D on Java 9+ or viewToModel on Java
     * 8.
     *
     * @param editor
     * @param pt
     * @return
     */
    public static int textComponentViewToModel(JTextComponent editor, Point pt) {
        Point2D p2d = new Point2D.Double(pt.x, pt.y);
        return textComponentViewToModel(editor, p2d);
    }

    /**
     * This calls JTextComponent.modelToView2D on Java 9+ or modelToView on Java
     * 8.
     *
     * @param editor
     * @param pos
     * @return
     * @throws javax.swing.text.BadLocationException
     */
    public static Rectangle2D textComponentModelToView(JTextComponent editor, int pos) throws BadLocationException {
        try {
            return (Rectangle2D) JTextComponent.class.getDeclaredMethod("modelToView2D", int.class).invoke(editor, pos);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            //method does not exist, we must be on Java8
        }

        //Try older method
        try {
            return (Rectangle) JTextComponent.class.getDeclaredMethod("modelToView", int.class).invoke(editor, pos);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * This calls TextUI.modelToView2D on Java 9+ or modelToView on Java 8.
     *
     * @param textUi
     * @param t
     * @param pos
     * @param bias
     * @return
     * @throws javax.swing.text.BadLocationException
     */
    public static Rectangle2D textUIModelToView(TextUI textUi, JTextComponent t, int pos, Position.Bias bias) throws BadLocationException {
        try {
            return (Rectangle2D) TextUI.class.getDeclaredMethod("modelToView2D", JTextComponent.class, int.class, Position.Bias.class).invoke(textUi, t, pos, bias);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            //method does not exist, we must be on Java8
        }

        //Try older method
        try {
            return (Rectangle) TextUI.class.getDeclaredMethod("modelToView", JTextComponent.class, int.class, Position.Bias.class).invoke(textUi, t, pos, bias);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Creates locale using Locale.of on Java19, using constructor on older
     * Javas.
     *
     * @param language
     * @param country
     * @return
     */
    public static Locale createLocale(String language, String country) {
        try {
            return (Locale) Locale.class.getDeclaredMethod("of", String.class, String.class).invoke(null, language, country);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            try {
                return Locale.class.getDeclaredConstructor(String.class, String.class).newInstance(language, country);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException ex1) {
                Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex1);
                return null;
            }
        }
    }

    /**
     * Creates locale using Locale.of on Java19, using constructor on older
     * Javas.
     *
     * @param language
     * @return
     */
    public static Locale createLocale(String language) {
        try {
            return (Locale) Locale.class.getDeclaredMethod("of", String.class).invoke(null, language);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            try {
                return Locale.class.getDeclaredConstructor(String.class).newInstance(language);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException ex1) {
                Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex1);
                return null;
            }
        }
    }

    public static JFileChooser getFileChooserWithIcon(String iconName) {
        return new JFileChooser() {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                setWindowIcon(dialog, iconName);
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                return dialog;
            }
        };
    }
}
