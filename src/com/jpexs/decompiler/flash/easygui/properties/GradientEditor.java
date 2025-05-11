/*
 * Copyright (C) 2025 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui.properties;

import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author JPEXS
 */
public class GradientEditor extends JPanel implements PropertyEditor {

    private List<Color> colors = new ArrayList<>();
    private List<Float> ratios = new ArrayList<>();
    private boolean colorDialogDisplayed = false;

    private List<ChangeListener> listeners = new ArrayList<>();
    private final FILTER filter;

    private static void drawCheckerboard(Graphics g, int tileSize, Rectangle rect) {
        drawCheckerboard(g, tileSize, rect.x, rect.y, rect.width, rect.height);
    }

    private static void drawCheckerboard(Graphics g, int tileSize, int startX, int startY, int width, int height) {
        Color lightGray = new Color(238, 238, 238); // light gray
        Color darkGray = new Color(189, 189, 189);  // darker gray

        for (int y = 0; y < height; y += tileSize) {
            for (int x = 0; x < width; x += tileSize) {
                boolean isLight = ((x / tileSize) + (y / tileSize)) % 2 == 0;
                g.setColor(isLight ? lightGray : darkGray);
                int tileW = tileSize;
                int tileH = tileSize;
                if (x + tileSize > width) {
                    tileW = width - x;
                }
                if (y + tileSize > height) {
                    tileH = height - y;
                }
                g.fillRect(startX + x, startY + y, tileW, tileH);
            }
        }
    }

    public GradientEditor(FILTER filter) {
        this.filter = filter;
        setLayout(new BorderLayout());
        JPanel miniGradientPanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawCheckerboard(g, 4, 0, 0, getWidth(), getHeight());

                float[] ratiosArr = new float[ratios.size()];
                for (int i = 0; i < ratios.size(); i++) {
                    ratiosArr[i] = ratios.get(i);
                }
                Color[] colorsArr = colors.toArray(new Color[colors.size()]);
                g2d.setPaint(new LinearGradientPaint(0, 0, getWidth(), 0, ratiosArr, colorsArr));
                g2d.fill(new Rectangle(getWidth(), getHeight()));
                g2d.setPaint(Color.black);
                g2d.draw(new Rectangle(getWidth(), getHeight()));
            }
        };
        miniGradientPanel.setPreferredSize(new Dimension(100, 16));

        miniGradientPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JDialog dialog = new JDialog();
                    dialog.setUndecorated(true);
                    dialog.setResizable(false);
                    dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
                    dialog.setContentPane(new GradientEditorPanel());
                    dialog.pack();

                    Window window = SwingUtilities.getWindowAncestor(GradientEditor.this);

                    dialog.setLocationRelativeTo(window);
                    Point loc = SwingUtilities.convertPoint(miniGradientPanel, 0, 0, window);
                    if (loc.x + dialog.getWidth() > window.getWidth()) {
                        loc.x -= loc.x + dialog.getWidth() - window.getWidth();
                    }
                    if (loc.y + dialog.getHeight() > window.getHeight()) {
                        loc.y -= loc.y + dialog.getHeight() - window.getHeight();
                    }
                    SwingUtilities.convertPointToScreen(loc, window);

                    dialog.setLocation(loc);
                    dialog.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowDeactivated(WindowEvent e) {
                            if (!colorDialogDisplayed) {
                                dialog.setVisible(false);
                                List<ChangeListener> listeners2 = new ArrayList<>(listeners);
                                for (ChangeListener l : listeners2) {
                                    l.change(GradientEditor.this);
                                }
                            }
                        }
                    });
                    dialog.setVisible(true);
                }
            }
        });

        add(miniGradientPanel, BorderLayout.WEST);
        reset();
    }

    private void loadParams(RGBA[] rgbaColors, int[] intRatios) {
        colors = new ArrayList<>();
        ratios = new ArrayList<>();
        int lastRatio = -1;
        for (int i = 0; i < rgbaColors.length; i++) {
            if ((i > 0) && (intRatios[i - 1] == intRatios[i])) {
                if (lastRatio < 255) {
                    lastRatio++;
                }
            } else {
                if (intRatios[i] > lastRatio) {
                    lastRatio = intRatios[i];
                } else if (lastRatio < 255) {
                    lastRatio++;
                }
            }
            ratios.add(lastRatio / 255f);
            colors.add(rgbaColors[i].toColor());
            if (lastRatio == 255) {
                break;
            }
        }

        if (colors.size() == 1) {
            colors.add(colors.get(0));
            ratios.set(0, 0f);
            ratios.add(1f);
        }

    }

    @Override
    public boolean save() {
        int[] gradientRatio = new int[ratios.size()];
        RGBA[] gradientColors = new RGBA[ratios.size()];

        for (int i = 0; i < ratios.size(); i++) {
            gradientRatio[i] = Math.round(ratios.get(i) * 255);
            gradientColors[i] = new RGBA(colors.get(i));
        }

        if (filter instanceof GRADIENTBEVELFILTER) {
            GRADIENTBEVELFILTER bevel = (GRADIENTBEVELFILTER) filter;
            bevel.gradientColors = gradientColors;
            bevel.gradientRatio = gradientRatio;
        } else if (filter instanceof GRADIENTGLOWFILTER) {
            GRADIENTGLOWFILTER glow = (GRADIENTGLOWFILTER) filter;
            glow.gradientColors = gradientColors;
            glow.gradientRatio = gradientRatio;
        }
        return true;
    }

    @Override
    public void reset() {
        if (filter instanceof GRADIENTBEVELFILTER) {
            GRADIENTBEVELFILTER bevel = (GRADIENTBEVELFILTER) filter;
            loadParams(bevel.gradientColors, bevel.gradientRatio);
        } else if (filter instanceof GRADIENTGLOWFILTER) {
            GRADIENTGLOWFILTER glow = (GRADIENTGLOWFILTER) filter;
            loadParams(glow.gradientColors, glow.gradientRatio);
        } else {
            throw new RuntimeException("Invalid FILTER");
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private class GradientEditorPanel extends JPanel {

        int dragIndex = -1;
        boolean draggedOut = false;
        boolean newlySelected = true;

        private Color interpolate(Color c1, Color c2, float t) {
            int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
            int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
            int a = (int) (c1.getAlpha() + t * (c2.getAlpha() - c1.getAlpha()));
            return new Color(r, g, b, a);
        }

        public GradientEditorPanel() {
            Dimension dim = new Dimension(300, 50);
            setMinimumSize(dim);
            setPreferredSize(dim);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (newlySelected) {
                            return;
                        }
                        List<Rectangle> rects = getColorRects();
                        for (int r = 0; r < rects.size(); r++) {
                            Rectangle rect = rects.get(r);
                            if (rect.contains(e.getPoint())) {
                                if (selectedColorIndex == r) {
                                    colorDialogDisplayed = true;
                                    Color newColor = ViewMessages.showColorDialog(GradientEditorPanel.this, colors.get(selectedColorIndex), true);
                                    colorDialogDisplayed = false;
                                    if (newColor != null) {
                                        if (isFixedZeroAlphaIndex(selectedColorIndex)) {
                                            newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 0);
                                        }
                                        colors.set(selectedColorIndex, newColor);
                                        repaint();
                                    }
                                }
                                return;
                            }
                        }
                    }

                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        List<Rectangle> rects = getColorRects();
                        for (int r = 0; r < rects.size(); r++) {
                            Rectangle rect = rects.get(r);
                            if (rect.contains(e.getPoint())) {
                                newlySelected = selectedColorIndex != r;
                                selectedColorIndex = r;
                                dragIndex = r;
                                repaint();
                                return;
                            }
                        }

                        float newRatio = (e.getX() - COLOR_SIZE / 2) / (float) (getWidth() - COLOR_SIZE);
                        if (newRatio >= 0 && newRatio <= 1) {
                            synchronized (GradientEditorPanel.this) {
                                int newIndex = -1;
                                Color color1 = null;
                                Color color2 = null;
                                Float ratio1 = null;
                                Float ratio2 = null;
                                for (int i = 0; i < ratios.size(); i++) {
                                    float ratio = ratios.get(i);
                                    if (ratio >= newRatio) {
                                        if (newRatio == ratio) {
                                            if (Float.compare(newRatio, 0f) > 0) {
                                                newRatio -= 0.0000001;
                                            } else {
                                                newRatio += 0.0000001;
                                                i++;
                                            }
                                        }
                                        ratio2 = ratio;
                                        newIndex = i;
                                        color2 = colors.get(i);
                                        break;
                                    }
                                    color1 = colors.get(i);
                                    ratio1 = ratio;
                                }
                                if (newIndex == -1) {
                                    newIndex = ratios.size();
                                    color2 = color1;
                                    ratio2 = 1f;
                                }
                                ratios.add(newIndex, newRatio);
                                float ratioDelta = (newRatio - ratio1) / (ratio2 - ratio1);
                                Color newColor = interpolate(color1, color2, ratioDelta);

                                colors.add(newIndex, newColor);

                                dragIndex = newIndex;
                                selectedColorIndex = newIndex;

                                newlySelected = true;
                                repaint();
                            }
                        }

                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (draggedOut && dragIndex > -1) {
                        synchronized (GradientEditorPanel.this) {
                            colors.remove(dragIndex);
                            ratios.remove(dragIndex);
                        }
                        repaint();
                    }

                    dragIndex = -1;
                    draggedOut = false;
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    float newRatio = (e.getX() - COLOR_SIZE / 2) / (float) (getWidth() - COLOR_SIZE);
                    newRatio = Math.round(newRatio * 255) / 255f;
                    if (newRatio < 0 || newRatio > 1) {
                        return;
                    }
                    if (dragIndex > -1) {

                        if (isFixedZeroAlphaIndex(dragIndex)) {
                            return;
                        }

                        draggedOut = colors.size() > 2 && e.getY() > getHeight();

                        synchronized (GradientEditorPanel.this) {
                            int newIndex = -1;
                            for (int i = 0; i < ratios.size(); i++) {
                                if (i == dragIndex) {
                                    continue;
                                }
                                float ratio = ratios.get(i);
                                if (ratio >= newRatio) {
                                    if (newRatio == ratio) {
                                        if (Float.compare(newRatio, 0f) > 0) {
                                            newRatio -= 0.0000001;
                                        } else {
                                            newRatio += 0.0000001;
                                            i++;
                                        }
                                    }
                                    newIndex = i;
                                    break;
                                }
                            }
                            ratios.set(dragIndex, newRatio);
                            if (newIndex == -1) {
                                newIndex = ratios.size();
                            }
                            ratios.add(newIndex, newRatio);
                            colors.add(newIndex, colors.get(dragIndex));

                            if (newIndex <= dragIndex) {
                                dragIndex++;
                            } else {
                                newIndex--;
                            }

                            ratios.remove(dragIndex);
                            colors.remove(dragIndex);

                            dragIndex = newIndex;
                        }
                        repaint();
                    }
                }
            });
        }

        final int BOTTOM_HEIGHT = 15;
        final int COLOR_SIZE = 10;
        final int ARROW_HEIGHT = 4;

        int selectedColorIndex = 0;

        private boolean isFixedZeroAlphaIndex(int index) {
            if (filter instanceof GRADIENTGLOWFILTER && index == 0 && colors.get(0).getAlpha() == 0) {
                return true;
            }
            if (filter instanceof GRADIENTBEVELFILTER && ratios.get(index) == 128f / 255f && colors.get(index).getAlpha() == 0) {
                return true;
            }
            return false;
        }

        private List<Rectangle> getColorRects() {
            List<Rectangle> rects = new ArrayList<>();

            for (int i = 0; i < ratios.size(); i++) {
                rects.add(new Rectangle(Math.round((getWidth() - COLOR_SIZE) * ratios.get(i)), getHeight() - BOTTOM_HEIGHT + ARROW_HEIGHT, COLOR_SIZE, COLOR_SIZE));
            }
            return rects;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            drawCheckerboard(g, 5, COLOR_SIZE / 2, 0, getWidth() - COLOR_SIZE, getHeight() - BOTTOM_HEIGHT);

            float[] ratiosArr;
            Color[] colorsArr;

            synchronized (GradientEditorPanel.this) {
                ratiosArr = new float[ratios.size()];
                for (int i = 0; i < ratios.size(); i++) {
                    ratiosArr[i] = ratios.get(i);
                }

                colorsArr = colors.toArray(new Color[colors.size()]);
            }
            g2d.setPaint(new LinearGradientPaint(COLOR_SIZE / 2, 0, getWidth() - COLOR_SIZE, 0, ratiosArr, colorsArr));
            g2d.fill(new Rectangle(COLOR_SIZE / 2, 0, getWidth() - COLOR_SIZE, getHeight() - BOTTOM_HEIGHT));
            g2d.setPaint(Color.black);
            g2d.draw(new Rectangle(COLOR_SIZE / 2, 0, getWidth() - COLOR_SIZE, getHeight() - BOTTOM_HEIGHT));

            List<Rectangle> rects = getColorRects();

            for (int i = 0; i < ratios.size(); i++) {
                if (draggedOut && i == dragIndex) {
                    continue;
                }
                Rectangle rect = rects.get(i);
                drawCheckerboard(g, 3, rect);
                g2d.setPaint(colorsArr[i]);
                g2d.fill(rect);
                g2d.setColor(Color.black);
                g2d.draw(rect);
                g2d.setColor(selectedColorIndex == i ? Color.black : Color.white);
                Polygon arrow = new Polygon(new int[]{
                    Math.round((getWidth() - COLOR_SIZE) * ratios.get(i)),
                    Math.round((getWidth() - COLOR_SIZE) * ratios.get(i)) + COLOR_SIZE / 2,
                    Math.round((getWidth() - COLOR_SIZE) * ratios.get(i)) + COLOR_SIZE
                }, new int[]{
                    getHeight() - BOTTOM_HEIGHT + ARROW_HEIGHT,
                    getHeight() - BOTTOM_HEIGHT,
                    getHeight() - BOTTOM_HEIGHT + ARROW_HEIGHT
                }, 3);
                g2d.fill(arrow);
                g2d.setColor(Color.black);
                g2d.draw(arrow);
            }
        }
    }
}
