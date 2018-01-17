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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;

/**
 *
 * @author JPEXS
 */
public class FolderPreviewPanel extends JPanel {

    private static ExecutorService executor;

    private List<TreeItem> items;

    private int selectedIndex = -1;

    private boolean repaintQueued;

    private int lastWidth;

    private int lastHeight;

    public Map<Integer, TreeItem> selectedItems = new HashMap<>();

    private Cache<Integer, SerializableImage> cachedPreviews;

    private static final int PREVIEW_SIZE = 150;

    private static final int BORDER_SIZE = 5;

    private static final int LABEL_HEIGHT = 20;

    private static final int CELL_HEIGHT = 2 * BORDER_SIZE + PREVIEW_SIZE + LABEL_HEIGHT;

    private static final int CELL_WIDTH = 2 * BORDER_SIZE + PREVIEW_SIZE;

    private static final SerializableImage noImage = new SerializableImage(PREVIEW_SIZE, PREVIEW_SIZE, BufferedImage.TYPE_INT_ARGB);

    static {
        noImage.fillTransparent();
        executor = Executors.newFixedThreadPool(Configuration.parallelSpeedUp.get() ? Configuration.getParallelThreadCount() : 1);
    }

    public FolderPreviewPanel(final MainPanel mainPanel, List<TreeItem> items) {
        this.items = items;
        cachedPreviews = Cache.getInstance(false, false, "preview");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    if (selectedIndex > -1) {
                        mainPanel.setTagTreeSelectedNode(FolderPreviewPanel.this.items.get(selectedIndex));
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int width = getWidth();

                int cols = width / CELL_WIDTH;
                int rows = (int) Math.ceil(FolderPreviewPanel.this.items.size() / (float) cols);
                int x = e.getX() / CELL_WIDTH;
                int y = e.getY() / CELL_HEIGHT;
                int index = y * cols + x;
                if (index >= FolderPreviewPanel.this.items.size()) {
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1 || selectedItems.isEmpty()) {
                    if (!e.isControlDown()) {
                        selectedItems.clear();
                    }
                    int oldSelectedIndex = selectedIndex;
                    selectedIndex = index;

                    if (e.isShiftDown() && oldSelectedIndex > -1) {
                        int minindex = Math.min(selectedIndex, oldSelectedIndex);
                        int maxindex = Math.max(selectedIndex, oldSelectedIndex);
                        for (int i = minindex; i <= maxindex; i++) {
                            selectedItems.put(i, FolderPreviewPanel.this.items.get(i));
                        }
                        selectedIndex = oldSelectedIndex;
                    } else {
                        TreeItem ti = FolderPreviewPanel.this.items.get(index);
                        if (!selectedItems.containsKey(selectedIndex)) {
                            selectedItems.put(selectedIndex, ti);
                        } else {
                            selectedItems.remove(selectedIndex);
                            selectedIndex = -1;
                        }
                    }
                }

                if (e.getButton() == MouseEvent.BUTTON3) {
                    mainPanel.tagTree.contextPopupMenu.update(new ArrayList<>(selectedItems.values()));
                    mainPanel.tagTree.contextPopupMenu.show(FolderPreviewPanel.this, e.getX(), e.getY());
                }
                repaint();
            }
        });
    }

    public synchronized void setItems(List<TreeItem> items) {
        this.items = items;
        executor.shutdownNow();
        executor = Executors.newFixedThreadPool(Configuration.parallelSpeedUp.get() ? Configuration.getParallelThreadCount() : 1);
        cachedPreviews.clear();
        revalidate();
        repaint();
        selectedItems.clear();
        selectedIndex = -1;
    }

    public void clear() {
        items = new ArrayList<>();
        executor.shutdownNow();
        cachedPreviews.clear();
        selectedItems.clear();
        selectedIndex = -1;
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getParent().getSize().width - 20;
        int cols = width / CELL_WIDTH;
        int rows = (int) Math.ceil(items.size() / (float) cols);
        int height = rows * CELL_HEIGHT;
        return new Dimension(width, height);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        repaintQueued = false;
        Rectangle r = getVisibleRect();
        int width = getWidth();

        int cols = width / CELL_WIDTH;
        int rows = (int) Math.ceil(items.size() / (float) cols);
        int height = rows * CELL_HEIGHT;

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

        //g.setColor(SubstanceLookAndFeel.getCurrentSkin().getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED));
        for (int y = start_y; y <= finish_y; y++) {
            for (int x = 0; x < cols; x++) {
                int index = y * cols + x;
                if (index < items.size()) {

                    g.setColor(color);
                    if (selectedItems.containsKey(index)) {
                        g.setColor(selectedColor);
                    }
                    g.fillRect(x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                    if (cachedPreviews.contains(index)) {
                        SerializableImage sImg = cachedPreviews.get(index);
                        if (sImg != null) {
                            BufferedImage img = cachedPreviews.get(index).getBufferedImage();
                            g.drawImage(img, x * CELL_WIDTH + BORDER_SIZE + PREVIEW_SIZE / 2 - img.getWidth() / 2, y * CELL_HEIGHT + BORDER_SIZE + PREVIEW_SIZE / 2 - img.getHeight() / 2, null);
                        }
                    } else {
                        cachedPreviews.put(index, noImage);
                        renderImageTask(index, items.get(index));
                    }
                    String s;
                    TreeItem treeItem = items.get(index);
                    if (treeItem instanceof Tag) {
                        s = ((Tag) treeItem).getTagName();
                        if (treeItem instanceof CharacterTag) {
                            s = s + " (" + ((CharacterTag) treeItem).getCharacterId() + ")";
                        }
                    } else {
                        s = treeItem.toString();
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

        if (lastWidth != width || lastHeight != height) {
            lastWidth = width;
            lastHeight = height;
            setSize(new Dimension(width, height));
        }
    }

    private synchronized void renderImageTask(final int index, final TreeItem treeItem) {
        executor.submit(() -> {
            cachedPreviews.put(index, renderImage(treeItem.getSwf(), treeItem));
            if (!repaintQueued) {
                repaintQueued = true;
                View.execInEventDispatchLater(() -> {
                    repaint();
                });
            }
            return null;
        });
    }

    private SerializableImage renderImage(SWF swf, TreeItem treeItem) {

        int width = 0;
        int height = 0;
        SerializableImage imgSrc = null;
        Matrix m = new Matrix();
        if (treeItem instanceof Frame) {
            Frame fn = (Frame) treeItem;
            RECT rect = swf.displayRect;
            double zoom = 1.0;
            if (rect.getWidth() > 0) {
                double ratio = (PREVIEW_SIZE - 1) * SWF.unitDivisor / (rect.getWidth());
                if (ratio < zoom) {
                    zoom = ratio;
                }
            }
            if (rect.getHeight() > 0) {
                double ratio = (PREVIEW_SIZE - 1) * SWF.unitDivisor / (rect.getHeight());
                if (ratio < zoom) {
                    zoom = ratio;
                }
            }

            Timeline timeline = swf.getTimeline();
            String key = "frame_" + fn.frame + "_" + timeline.id + "_" + zoom;
            imgSrc = swf.getFromCache(key);
            if (imgSrc == null) {
                imgSrc = SWF.frameToImageGet(timeline, fn.frame, fn.frame, null, 0, rect, new Matrix(), null, null, zoom);
                swf.putToCache(key, imgSrc);
            }

            width = imgSrc.getWidth();
            height = imgSrc.getHeight();
        } else if (treeItem instanceof ImageTag) {
            imgSrc = ((ImageTag) treeItem).getImageCached();
            width = imgSrc.getWidth();
            height = imgSrc.getHeight();
        } else if (treeItem instanceof BoundedTag) {
            BoundedTag boundedTag = (BoundedTag) treeItem;
            RECT rect = boundedTag.getRect();
            width = (int) (rect.getWidth() / SWF.unitDivisor) + 1;
            height = (int) (rect.getHeight() / SWF.unitDivisor) + 1;
            m.translate(-rect.Xmin, -rect.Ymin);
        }

        int w1 = width;
        int h1 = height;
        int w2 = PREVIEW_SIZE;
        int h2 = PREVIEW_SIZE;

        int w;
        int h = h1 * w2 / w1;
        if (h > h2) {
            w = w1 * h2 / h1;
        } else {
            w = w2;
        }

        double scale = (double) w / (double) w1;
        if (w1 <= w2 && h1 <= h2) {
            if (imgSrc != null) {
                return imgSrc;
            }

            scale = 1;
        }

        m = m.preConcatenate(Matrix.getScaleInstance(scale));
        width = (int) (scale * width);
        height = (int) (scale * height);
        if (width == 0 || height == 0) {
            return null;
        }

        SerializableImage image = new SerializableImage(width, height, SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();
        if (imgSrc == null) {
            DrawableTag drawable = (DrawableTag) treeItem;
            drawable.toImage(0, 0, 0, new RenderContext(), image, false, m, m, m, null);
        } else {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setTransform(m.toTransform());
            g.drawImage(imgSrc.getBufferedImage(), 0, 0, null);
        }
        return image;
    }
}
