/*
 *  Copyright (C) 2022-2024 JPEXS
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
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;

/**
 * @author JPEXS
 */
public class FolderListPanel extends JPanel {

    private List<TreeItem> items;

    private TreePath parentPath;

    private int selectedIndex = -1;

    private Map<Integer, TreeItem> selectedItems = new TreeMap<>();

    private static final int PREVIEW_SIZE = 150;

    private static final int BORDER_SIZE = 5;

    private static final int LABEL_HEIGHT = 20;

    private static final int CELL_HEIGHT = 2 * BORDER_SIZE + PREVIEW_SIZE + LABEL_HEIGHT;

    private static final int CELL_WIDTH = 2 * BORDER_SIZE + PREVIEW_SIZE;

    private static final Map<TreeNodeType, Icon> ICONS;

    protected Map<TreeItem, Set<Integer>> missingNeededCharacters = new WeakHashMap<>();

    private MainPanel mainPanel;

    static {
        ICONS = new HashMap<>();
        for (TreeNodeType treeNodeType : TreeNodeType.values()) {
            if (treeNodeType != TreeNodeType.UNKNOWN) {
                String tagTypeStr = treeNodeType.toString().toLowerCase(Locale.ENGLISH).replace("_", "");
                try {
                    ICONS.put(treeNodeType, View.getIcon(tagTypeStr + "32"));
                } catch (NullPointerException npe) {
                    System.err.println("ICON " + tagTypeStr + "32.png does not exist!");
                    ICONS.put(treeNodeType, View.getIcon("about32"));
                }
            }
        }
    }

    private static final SerializableImage noImage = new SerializableImage(PREVIEW_SIZE, PREVIEW_SIZE, BufferedImage.TYPE_INT_ARGB);

    static {
        noImage.fillTransparent();
    }

    public FolderListPanel(final MainPanel mainPanel, List<TreeItem> items) {
        this.items = items;
        this.mainPanel = mainPanel;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    if (selectedIndex > -1) {
                        TreeItem selectedItem = FolderListPanel.this.items.get(selectedIndex);
                        TreePath subPath = parentPath.pathByAddingChild(selectedItem);
                        mainPanel.getCurrentTree().setSelectionPath(subPath);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int width = getWidth();

                int cols = width / CELL_WIDTH;
                int rows = (int) Math.ceil(FolderListPanel.this.items.size() / (float) cols);
                int x = e.getX() / CELL_WIDTH;
                int y = e.getY() / CELL_HEIGHT;
                int index = y * cols + x;
                if (index >= FolderListPanel.this.items.size()) {
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e) || selectedItems.isEmpty()) {
                    if (!e.isControlDown()) {
                        selectedItems.clear();
                    }
                    int oldSelectedIndex = selectedIndex;
                    selectedIndex = index;

                    if (e.isShiftDown() && oldSelectedIndex > -1) {
                        int minindex = Math.min(selectedIndex, oldSelectedIndex);
                        int maxindex = Math.max(selectedIndex, oldSelectedIndex);
                        for (int i = minindex; i <= maxindex; i++) {
                            selectedItems.put(i, FolderListPanel.this.items.get(i));
                        }
                        selectedIndex = oldSelectedIndex;
                    } else {
                        TreeItem ti = FolderListPanel.this.items.get(index);
                        if (!selectedItems.containsKey(selectedIndex)) {
                            selectedItems.put(selectedIndex, ti);
                        } else {
                            selectedItems.remove(selectedIndex);
                            selectedIndex = -1;
                        }
                    }
                }

                if (SwingUtilities.isRightMouseButton(e)) {
                    mainPanel.getContextPopupMenu().update(getSelectedItemsSorted());
                    mainPanel.getContextPopupMenu().show(FolderListPanel.this, e.getX(), e.getY());
                }
                repaint();
            }
        });
        setFocusable(true);
    }

    public synchronized void setItems(TreePath parentPath, List<TreeItem> items) {
        this.items = items;
        this.parentPath = parentPath;
        revalidate();
        repaint();
        selectedItems.clear();
        selectedIndex = -1;
        ((JScrollPane) getParent().getParent()).getVerticalScrollBar().setValue(0);
    }   
    
    public void clear() {
        items = new ArrayList<>();
        selectedItems.clear();
        selectedIndex = -1;
        parentPath = null;
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getParent().getSize().width - 1;
        int cols = width / CELL_WIDTH;
        int rows = (int) Math.ceil(items.size() / (float) cols);
        int height = rows * CELL_HEIGHT;
        int prefWidth = cols * CELL_WIDTH;
        return new Dimension(prefWidth, height);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Rectangle r = getVisibleRect();
        int width = getWidth();
        int cols = width / CELL_WIDTH;
        int start_y = r.y / CELL_HEIGHT;
        JLabel l = new JLabel();
        Font f = l.getFont().deriveFont(AffineTransform.getScaleInstance(0.8, 0.8));
        int finish_y = (int) Math.ceil((r.y + r.height) / (float) CELL_HEIGHT);
        Color color;
        Color selectedColor;
        Color selectedTextColor;
        Color borderColor;
        Color textColor;
        if (Configuration.useRibbonInterface.get()) {
            SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
            color = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor();
            selectedColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getBackgroundFillColor();
            borderColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.BORDER, ComponentState.ROLLOVER_SELECTED).getUltraDarkColor();
            textColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getForegroundColor();
            selectedTextColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getForegroundColor();
        } else {
            color = SystemColor.control;
            selectedColor = SystemColor.textHighlight;
            borderColor = SystemColor.controlShadow;
            textColor = SystemColor.controlText;
            selectedTextColor = SystemColor.textHighlightText;
        }

        for (int y = start_y; y <= finish_y; y++) {
            for (int x = 0; x < cols; x++) {
                int index = y * cols + x;
                if (index < items.size()) {

                    g.setColor(color);
                    if (selectedItems.containsKey(index)) {
                        g.setColor(selectedColor);
                    }
                    g.fillRect(x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);

                    TreeItem treeItem = items.get(index);

                    TreeNodeType type = AbstractTagTree.getTreeNodeType(treeItem);
                    Icon icon = ICONS.get(type);
                    icon.paintIcon(l, g, x * CELL_WIDTH + BORDER_SIZE + PREVIEW_SIZE / 2 - icon.getIconWidth() / 2, y * CELL_HEIGHT + BORDER_SIZE + PREVIEW_SIZE / 2 - icon.getIconHeight() / 2);
                    String s;
                    if (treeItem instanceof Tag) {
                        Tag t = (Tag) treeItem;
                        String uniqueId = t.getUniqueId();
                        s = ((Tag) treeItem).getTagName();
                        if (uniqueId != null) {
                            s = s + " (" + uniqueId + ")";
                        }
                    } else {
                        s = treeItem.toString();
                    }

                    int itemIndex = mainPanel.getCurrentTree().getFullModel().getItemIndex(treeItem);
                    if (itemIndex > 1) {
                        s += " [" + itemIndex + "]";
                    }

                    g.setFont(f);
                    g.setColor(borderColor);
                    g.drawLine(x * CELL_WIDTH, y * CELL_HEIGHT + BORDER_SIZE + PREVIEW_SIZE, x * CELL_WIDTH + CELL_WIDTH, y * CELL_HEIGHT + BORDER_SIZE + PREVIEW_SIZE);
                    g.drawRect(x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                    g.setColor(textColor);
                    if (selectedItems.containsKey(index)) {
                        g.setColor(selectedTextColor);
                    }
                    g.drawString(s, x * CELL_WIDTH + BORDER_SIZE, y * CELL_HEIGHT + BORDER_SIZE + PREVIEW_SIZE + LABEL_HEIGHT);

                }
            }
        }
    }

    public List<TreeItem> getSelectedItemsSorted() {
        return new ArrayList<>(selectedItems.values());
    }

    public boolean isSomethingSelected() {
        return !selectedItems.isEmpty();
    }
}
