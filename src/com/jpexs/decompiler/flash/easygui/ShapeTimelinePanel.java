/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.ShapeLayers;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/** One frame for each style bank, with the frontmost layer at the top. */
final class ShapeTimelinePanel extends JPanel {

    private static final int FRAME_WIDTH = 40;
    private static final int ROW_HEIGHT = TimelineBodyPanel.FRAME_HEIGHT;

    private final TimelineDepthPanel depths;
    private final TimelineLayerDrag layerDrag;
    private final TimelineLayerSelection layerSelection = new TimelineLayerSelection();
    private int dropRow = -1;
    private List<SHAPEWITHSTYLE> layers = Collections.emptyList();
    private int activeLayer;
    private Set<Integer> selectedLayers = Collections.emptySet();

    private final JPanel frames = new JPanel() {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle clip = g.getClipBounds();
            Set<Integer> selectedDepths = layerSelection.get().depths;
            TimelineBodyPanel.drawFrameGrid(g, clip, FRAME_WIDTH, row -> selectedDepths.contains(layerAt(row) + 1));
            int lastRow = Math.min(layers.size() - 1, (clip.y + clip.height - 1) / ROW_HEIGHT);
            for (int row = clip.y / ROW_HEIGHT; row <= lastRow; row++) {
                int layer = layerAt(row);
                int y = row * ROW_HEIGHT;
                boolean empty = !ShapeLayers.hasEdges(layers.get(layer));
                boolean selected = selectedLayers.isEmpty() ? layer == activeLayer : selectedLayers.contains(layer);
                boolean rowSelected = selectedDepths.contains(layer + 1);
                g.setColor(selected || rowSelected ? TimelineBodyPanel.getSelectedColor()
                        : empty ? TimelineBodyPanel.getEmptyFrameColor() : TimelineBodyPanel.getFrameColor());
                g.fillRect(0, y, FRAME_WIDTH, ROW_HEIGHT);
                g.setColor(TimelineBodyPanel.BORDER_COLOR);
                g.drawRect(0, y, FRAME_WIDTH, ROW_HEIGHT);
                g.setColor(TimelineBodyPanel.KEY_COLOR);
                TimelineBodyPanel.drawKeyMarker(g, FRAME_WIDTH / 2, y, empty);
            }
            if (!layers.isEmpty()) {
                g.setColor(TimelineBodyPanel.SELECTED_BORDER_COLOR);
                g.drawLine(FRAME_WIDTH / 2, clip.y, FRAME_WIDTH / 2, clip.y + clip.height);
            }
            if (dropRow >= 0) {
                TimelineLayerDrag.paintDropLine(g, getWidth(), dropRow * ROW_HEIGHT);
            }
        }
    };

    ShapeTimelinePanel(EasySwfPanel swfPanel, IntConsumer selectLayer, IntConsumer addLayer, Consumer<Set<Integer>> removeLayers,
            Consumer<TimelineDepthMove> moveLayers) {
        super(new BorderLayout());
        depths = new TimelineDepthPanel(swfPanel, true, layerSelection);
        layerSelection.addListener(frames::repaint);
        TimelineTimePanel time = new TimelineTimePanel(FRAME_WIDTH);
        JScrollPane scroll = new FasterScrollPane(frames);
        scroll.setRowHeaderView(depths);
        scroll.setColumnHeaderView(time);
        scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, depths.getHeader());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        layerDrag = new TimelineLayerDrag(depths, scroll, 0, this::setDropRow, moveLayers);

        frames.setFocusable(true);
        frames.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                int key = event.getKeyCode();
                if (key == KeyEvent.VK_DELETE) {
                    swfPanel.deleteShapeSelection();
                    event.consume();
                    return;
                }
                if (layers.isEmpty() || (key != KeyEvent.VK_UP && key != KeyEvent.VK_DOWN)) {
                    return;
                }
                int layer = activeLayer + (key == KeyEvent.VK_UP ? 1 : -1);
                if (layer >= 0 && layer < layers.size()) {
                    layerSelection.clear();
                    selectLayer.accept(layer);
                }
                event.consume();
            }
        });

        MouseAdapter layerMouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int layer = layerAt(event);
                if (SwingUtilities.isLeftMouseButton(event) && layer >= 0
                        && (event.getSource() != depths || !layerDrag.wasDragged() && !depths.isOverControls(event))) {
                    frames.requestFocusInWindow();
                    if (event.getSource() == depths) {
                        int depth = layer + 1;
                        layerSelection.click(depth, event.isControlDown(), event.isShiftDown());
                        TimelineLayerSelection.State selection = layerSelection.get();
                        swfPanel.selectShapeLayerRows(selection.depths, depth);
                        layerSelection.set(selection);
                    } else {
                        layerSelection.clear();
                        swfPanel.selectShapeLayerContents(layer);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent event) {
                showLayerMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                showLayerMenu(event);
            }

            private void showLayerMenu(MouseEvent event) {
                int layer = layerAt(event);
                if (!event.isPopupTrigger() || layer < 0) {
                    return;
                }
                JPopupMenu menu = new JPopupMenu();
                JMenuItem add = new JMenuItem(EasyStrings.translate("timeline.shape.addLayer"));
                add.addActionListener(action -> addLayer.accept(layer));
                menu.add(add);
                Set<Integer> selected = layerSelection.contextDepths(layer + 1);
                JMenuItem remove = new JMenuItem(EasyStrings.translate(selected.size() == 1 ? "timeline.shape.removeLayer" : "timeline.shape.removeLayers"));
                remove.setEnabled(swfPanel.canRemoveShapeLayers(selected));
                remove.addActionListener(action -> removeLayers.accept(selected));
                menu.add(remove);
                menu.show(event.getComponent(), event.getX(), event.getY());
            }
        };
        frames.addMouseListener(layerMouse);
        depths.addMouseListener(layerMouse);
    }

    void setLayers(List<SHAPEWITHSTYLE> layers, int activeLayer) {
        layerDrag.cancel();
        layerSelection.clear();
        this.layers = layers;
        depths.setDepthCount(layers.size());
        frames.setPreferredSize(new Dimension(FRAME_WIDTH + 1, layers.size() * ROW_HEIGHT + 1));
        frames.revalidate();
        boolean activeChanged = this.activeLayer != activeLayer;
        setSelection(activeLayer, Collections.emptySet());
        if (!activeChanged) {
            scrollToLayer();
        }
    }

    TimelineLayerSelection getLayerSelection() {
        return layerSelection;
    }

    void setSelection(int activeLayer, Set<Integer> selectedLayers) {
        boolean activeChanged = this.activeLayer != activeLayer;
        this.activeLayer = activeLayer;
        this.selectedLayers = selectedLayers;
        frames.repaint();
        if (activeChanged) {
            scrollToLayer();
        }
    }

    private void scrollToLayer() {
        if (!layers.isEmpty()) {
            frames.scrollRectToVisible(new Rectangle(0, layerAt(activeLayer) * ROW_HEIGHT, FRAME_WIDTH, ROW_HEIGHT));
        }
    }

    private void setDropRow(int row) {
        if (dropRow != row) {
            dropRow = row;
            frames.repaint();
        }
    }

    private int layerAt(MouseEvent event) {
        if (event.getSource() == depths) {
            return depths.getDepthAt(event) - 1;
        }
        return event.getX() < FRAME_WIDTH ? layerAt(event.getY() / ROW_HEIGHT) : -1;
    }

    private int layerAt(int row) {
        return layers.size() - 1 - row;
    }
}
