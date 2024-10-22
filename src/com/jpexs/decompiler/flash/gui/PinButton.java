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
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;

/**
 * @author JPEXS
 */
public class PinButton extends JPanel {

    //private static final Border raisedBorder = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(3, 5, 0, 5));
    //private static final Border loweredBorder = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), BorderFactory.createEmptyBorder(3, 5, 0, 5));
    private List<ActionListener> actionListeners = new ArrayList<>();
    private List<ChangeListener> changeListeners = new ArrayList<>();

    private boolean pinned;

    private boolean mouseOverPin = false;
    private boolean mouseOver = false;

    private JLabel button;

    private TreeItem item;

    private boolean selected;

    private Color color;
    private Color hilightedColor;
    private Color hilightedTextColor;
    private Color selectedColor;
    private Color selectedTextColor;
    private Color borderColor;
    private Color textColor;

    private JLabel label;
    private MainPanel mainPanel;

    public PinButton(MainPanel mainPanel, TreeItem item, boolean pinned) {
        this.mainPanel = mainPanel;
        //setBorder(raisedBorder);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        this.item = item;
        this.pinned = pinned;

        if (Configuration.useRibbonInterface.get()) {
            SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
            color = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor();
            hilightedColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getBackgroundFillColor();
            borderColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.BORDER, ComponentState.ROLLOVER_SELECTED).getUltraDarkColor();
            textColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getForegroundColor();
            hilightedTextColor = skin.getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getForegroundColor();
        } else {
            color = SystemColor.control;
            hilightedColor = SystemColor.textHighlight;
            borderColor = SystemColor.controlShadow;
            textColor = SystemColor.controlText;
            hilightedTextColor = SystemColor.textHighlightText;
        }

        Color color2 = Color.white;
        selectedColor = new Color(
                (color.getRed() + color2.getRed()) / 2,
                (color.getGreen() + color2.getGreen()) / 2,
                (color.getBlue() + color2.getBlue()) / 2
        );

        Color color3 = Color.black;
        selectedTextColor = new Color(
                (textColor.getRed() + color3.getRed()) / 2,
                (textColor.getGreen() + color3.getGreen()) / 2,
                (textColor.getBlue() + color3.getBlue()) / 2
        );

        label = new JLabel();
        label.setIcon(AbstractTagTree.getIconFor(item));
        refresh();
        button = new JLabel();
        button.setMinimumSize(new Dimension(10 + 16, 16));
        button.setPreferredSize(new Dimension(10 + 16, 16));

        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (mainPanel.checkEdited()) {
                        return;
                    }
                    List<TreeItem> itemList = new ArrayList<>();
                    itemList.add(item);
                    mainPanel.getContextPopupMenu().update(itemList);
                    mainPanel.getContextPopupMenu().show(PinButton.this, 0, PinButton.this.getHeight());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    /*if (selected) {
                        setBorder(loweredBorder);
                    } else {
                        setBorder(raisedBorder);
                    }*/
                    fireAction();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                updateIcon();
                if (selected) {
                    setBackground(selectedColor);
                    label.setForeground(selectedTextColor);
                } else {
                    setBackground(color);
                    label.setForeground(textColor);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                if (selected) {
                    setBackground(selectedColor);
                    label.setForeground(selectedTextColor);
                } else {
                    setBackground(hilightedColor);
                    label.setForeground(hilightedTextColor);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseOver = true;
                updateIcon();
            }

        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        label.addMouseListener(adapter);
        label.addMouseMotionListener(adapter);

        button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (pinned) {
            button.setIcon(View.getIcon("pinned16"));
            button.setToolTipText(AppStrings.translate("unpin"));
        } else {
            button.setToolTipText(AppStrings.translate("pin"));
        }
        MouseAdapter buttonAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOverPin = true;
                mouseOver = true;
                if (selected) {
                    setBackground(selectedColor);
                    label.setForeground(selectedTextColor);
                } else {
                    setBackground(hilightedColor);
                    label.setForeground(hilightedTextColor);
                }
                updateIcon();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOverPin = false;
                mouseOver = false;
                if (selected) {
                    setBackground(selectedColor);
                    label.setForeground(selectedTextColor);
                } else {
                    setBackground(color);
                    label.setForeground(textColor);
                }
                updateIcon();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    PinButton.this.pinned = !PinButton.this.pinned;
                    if (PinButton.this.pinned) {
                        button.setToolTipText(AppStrings.translate("unpin"));
                    } else {
                        button.setToolTipText(AppStrings.translate("pin"));
                    }
                    updateIcon();
                    fireChange();
                }
            }

        };
        button.addMouseListener(buttonAdapter);
        button.addMouseMotionListener(buttonAdapter);

        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);
    }

    private void updateIcon() {

        if (pinned) {
            button.setIcon(View.getIcon("pinned16"));
        } else if (mouseOverPin) {
            button.setIcon(View.getIcon(pinned ? "pinned16" : "pin16"));
        } else if (mouseOver) {
            button.setIcon(View.getIcon(pinned ? "pinned16" : "canpin16"));
        } else {
            button.setIcon(null);
        }
    }

    private void fireAction() {
        ActionEvent ev = new ActionEvent(this, 0, "");
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed(ev);
        }
    }

    private void fireChange() {
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(ev);
        }
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public boolean isPinned() {
        return pinned;
    }

    public TreeItem getItem() {
        return item;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            setBackground(selectedColor);
            label.setForeground(selectedTextColor);
        } else if (!mouseOver) {
            setBackground(color);
            label.setForeground(textColor);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(borderColor);
        g.drawLine(0, 0, getWidth() - 1, 0);
        g.drawLine(0, 0, 0, getHeight() - 1);
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
        g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
        if (selected) {
            g.setColor(hilightedColor);
            g.drawLine(0, 0, getWidth() - 1, 0);
            g.drawLine(0, 1, getWidth() - 1, 1);
            g.drawLine(0, 2, getWidth() - 1, 2);
        }
    }

    private String getTreeItemPath(TreeItem item) {
        TreePath path = mainPanel.getCurrentTree().getFullModel().getTreePath(item);
        if (path == null) {
            return "";
        }
        StringBuilder pathString = new StringBuilder();
        for (int i = 1; i < path.getPathCount(); i++) {
            if (pathString.length() > 0) {
                pathString.append(" / ");
            }
            pathString.append(mainPanel.itemToString((TreeItem) path.getPathComponent(i)));
        }
        return pathString.toString();
    }

    public void refresh() {
        label.setText(mainPanel.itemToString(item));
        label.setToolTipText(getTreeItemPath(item));
    }
}
