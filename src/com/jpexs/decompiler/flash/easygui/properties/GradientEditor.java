/*
 *  Copyright (C) 2025-2026 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties;

import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.generictageditors.ChangeListener;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
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
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
    private GRADIENT gradient;
    private final ShapeColorEditor stopColorEditor;
    private final GradientEditorPanel inlineStops;
    private final JLabel mixedNotice = new JLabel("-");
    private boolean indeterminate;
    private boolean linearRGB;

    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
        mixedNotice.setVisible(indeterminate);
        repaint();
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

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
        this(filter, null);
    }

    public GradientEditor(GRADIENT gradient) {
        this(null, gradient);
    }

    public static GradientEditor createInline(GRADIENT gradient) {
        return new GradientEditor(null, gradient, true, true);
    }

    public static JPanel createPreview(GRADIENT gradient) {
        return new GradientEditor(null, gradient, false);
    }

    private GradientEditor(FILTER filter, GRADIENT gradient) {
        this(filter, gradient, true);
    }

    private GradientEditor(FILTER filter, GRADIENT gradient, boolean editable) {
        this(filter, gradient, editable, false);
    }

    private GradientEditor(FILTER filter, GRADIENT gradient, boolean editable, boolean inline) {
        this.filter = filter;
        this.gradient = gradient;
        setLayout(new BorderLayout(0, 4));
        if (inline) {
            inlineStops = new GradientEditorPanel(true);
            stopColorEditor = new ShapeColorEditor(color -> {
                colors.set(inlineStops.selectedColorIndex, color);
                inlineStops.repaint();
                fireChange();
            });
            add(stopColorEditor, BorderLayout.NORTH);
            JPanel strip = new JPanel(new BorderLayout(4, 0));
            strip.add(inlineStops, BorderLayout.CENTER);
            mixedNotice.setVisible(false);
            strip.add(mixedNotice, BorderLayout.EAST);
            add(strip, BorderLayout.CENTER);
            reset();
            return;
        }
        inlineStops = null;
        stopColorEditor = null;
        JPanel miniGradientPanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                drawCheckerboard(g, 4, 0, 0, getWidth(), getHeight());
                if (indeterminate) {
                    g.setColor(Color.black);
                    g.drawString("-", getWidth() / 2, getHeight() - 3);
                    return;
                }

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
                if (editable && GradientEditor.this.isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    List<Color> originalColors = new ArrayList<>(colors);
                    List<Float> originalRatios = new ArrayList<>(ratios);
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
                                dialog.dispose();
                                repaint();
                                if (!originalColors.equals(colors) || !originalRatios.equals(ratios)) {
                                    fireChange();
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

    public void setGradient(GRADIENT gradient) {
        this.gradient = gradient;
        setLinearRGB(gradient.interpolationMode == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE);
        setIndeterminate(false);
        reset();
    }

    public void setLinearRGB(boolean linearRGB) {
        this.linearRGB = linearRGB;
        repaint();
    }

    public void paintSwatch(Graphics2D graphics, Rectangle bounds, boolean radial, float focalPoint) {
        float[] positions = new float[ratios.size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = ratios.get(i);
        }
        graphics.setPaint(gradientPaint(bounds, positions, colors.toArray(new Color[0]), radial, focalPoint));
        graphics.fill(bounds);
    }

    private Paint gradientPaint(Rectangle bounds, float[] positions, Color[] stops, boolean radial, float focalPoint) {
        if (stops.length == 1) {
            return stops[0];
        }
        // Separate SWF hard stops only for painting, as Java2D requires strictly increasing fractions.
        float step = 0.000001f;
        for (int i = 0; i < positions.length; i++) {
            float upper = 1 - step * (positions.length - i - 1);
            float lower = i == 0 ? 0 : positions[i - 1] + step;
            positions[i] = Math.max(lower, Math.min(upper, positions[i]));
        }
        MultipleGradientPaint.ColorSpaceType colorSpace = linearRGB
                ? MultipleGradientPaint.ColorSpaceType.LINEAR_RGB : MultipleGradientPaint.ColorSpaceType.SRGB;
        if (radial) {
            AffineTransform transform = AffineTransform.getTranslateInstance(bounds.getCenterX(), bounds.getCenterY());
            transform.scale(bounds.width / 2.0, bounds.height / 2.0);
            float focus = Math.max(-0.99f, Math.min(0.99f, focalPoint));
            return new RadialGradientPaint(new Point2D.Float(), 1, new Point2D.Float(focus, 0), positions, stops,
                    MultipleGradientPaint.CycleMethod.NO_CYCLE, colorSpace, transform);
        }
        return new LinearGradientPaint(new Point2D.Float(bounds.x, bounds.y),
                new Point2D.Float(bounds.x + bounds.width, bounds.y), positions, stops,
                MultipleGradientPaint.CycleMethod.NO_CYCLE, colorSpace, new AffineTransform());
    }

    private void fireChange() {
        int selected = inlineStops == null ? 0 : inlineStops.selectedColorIndex;
        for (ChangeListener listener : new ArrayList<>(listeners)) {
            listener.change(this);
        }
        if (inlineStops != null) {
            inlineStops.selectedColorIndex = Math.min(selected, colors.size() - 1);
            inlineStops.refreshStopColor();
        }
    }

    private void loadParams(RGBA[] rgbaColors, int[] intRatios) {
        colors = new ArrayList<>();
        ratios = new ArrayList<>();
        int lastRatio = -1;
        for (int i = 0; i < rgbaColors.length; i++) {
            if (inlineStops != null) {
                ratios.add(intRatios[i] / 255f);
                colors.add(rgbaColors[i].toColor());
                continue;
            }
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

        if (colors.isEmpty()) {
            colors.add(Color.black);
            colors.add(Color.white);
            ratios.add(0f);
            ratios.add(1f);
        } else if (colors.size() == 1 && inlineStops == null) {
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
        } else if (gradient != null) {
            gradient.gradientRecords = new GRADRECORD[ratios.size()];
            for (int i = 0; i < ratios.size(); i++) {
                GRADRECORD record = new GRADRECORD();
                record.inShape3 = true;
                record.ratio = gradientRatio[i];
                record.color = gradientColors[i];
                gradient.gradientRecords[i] = record;
            }
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
        } else if (gradient != null) {
            RGBA[] gradientColors = new RGBA[gradient.gradientRecords.length];
            int[] gradientRatios = new int[gradient.gradientRecords.length];
            for (int i = 0; i < gradient.gradientRecords.length; i++) {
                gradientColors[i] = new RGBA(gradient.gradientRecords[i].color);
                gradientRatios[i] = gradient.gradientRecords[i].ratio;
            }
            loadParams(gradientColors, gradientRatios);
        } else {
            throw new RuntimeException("Invalid FILTER");
        }
        if (inlineStops != null) {
            inlineStops.selectedColorIndex = Math.min(inlineStops.selectedColorIndex, colors.size() - 1);
            inlineStops.refreshStopColor();
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
        private boolean stopsChanged;

        private Color interpolate(Color c1, Color c2, float t) {
            int r = (int) (c1.getRed() + t * (c2.getRed() - c1.getRed()));
            int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue() + t * (c2.getBlue() - c1.getBlue()));
            int a = (int) (c1.getAlpha() + t * (c2.getAlpha() - c1.getAlpha()));
            return new Color(r, g, b, a);
        }

        public GradientEditorPanel() {
            this(false);
        }

        GradientEditorPanel(boolean inline) {
            Dimension dim = new Dimension(inline ? 170 : 300, 50);
            setMinimumSize(dim);
            setPreferredSize(dim);
            if (inline) {
                setFocusable(true);
                getAccessibleContext().setAccessibleName(EasyStrings.translate("property.shape.color.stops"));
                addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (isEnabled() && (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)) {
                            int direction = e.getKeyCode() == KeyEvent.VK_LEFT ? -1 : 1;
                            selectedColorIndex = Math.max(0, Math.min(colors.size() - 1, selectedColorIndex + direction));
                            refreshStopColor();
                        }
                    }
                });
            }
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!inline && SwingUtilities.isLeftMouseButton(e)) {
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
                    if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                        if (inline) {
                            stopColorEditor.finishEditing();
                            requestFocusInWindow();
                        }
                        stopsChanged = false;
                        List<Rectangle> rects = getColorRects();
                        for (int r = 0; r < rects.size(); r++) {
                            Rectangle rect = rects.get(r);
                            if (rect.contains(e.getPoint())) {
                                newlySelected = selectedColorIndex != r;
                                selectedColorIndex = r;
                                dragIndex = r;
                                refreshStopColor();
                                repaint();
                                return;
                            }
                        }

                        if (colors.size() >= (gradient == null ? 255 : 15)) {
                            return;
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
                                    if (ratio > newRatio || (!inline && ratio == newRatio)) {
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
                                if (color1 == null) {
                                    color1 = color2;
                                    ratio1 = 0f;
                                }
                                float ratioDelta = ratio2.equals(ratio1) ? 0 : (newRatio - ratio1) / (ratio2 - ratio1);
                                Color newColor = interpolate(color1, color2, ratioDelta);

                                colors.add(newIndex, newColor);

                                dragIndex = newIndex;
                                selectedColorIndex = newIndex;

                                newlySelected = true;
                                stopsChanged = true;
                                refreshStopColor();
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
                            selectedColorIndex = Math.min(dragIndex, colors.size() - 1);
                        }
                        stopsChanged = true;
                        repaint();
                    }

                    dragIndex = -1;
                    draggedOut = false;
                    refreshStopColor();
                    if (inline && stopsChanged) {
                        stopsChanged = false;
                        fireChange();
                    }
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
                    if (isEnabled() && dragIndex > -1) {

                        if (isFixedZeroAlphaIndex(dragIndex)) {
                            return;
                        }

                        draggedOut = colors.size() > 2 && e.getY() > getHeight();

                        synchronized (GradientEditorPanel.this) {
                            if (Float.compare(newRatio, ratios.get(dragIndex)) == 0) {
                                repaint();
                                return;
                            }
                            int newIndex = -1;
                            for (int i = 0; i < ratios.size(); i++) {
                                if (i == dragIndex) {
                                    continue;
                                }
                                float ratio = ratios.get(i);
                                if (ratio > newRatio || (!inline && ratio == newRatio)) {
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
                            selectedColorIndex = newIndex;
                            stopsChanged = true;
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

        private void refreshStopColor() {
            if (stopColorEditor != null) {
                stopColorEditor.setColor(colors.get(selectedColorIndex));
                repaint();
            }
        }

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
            if (getWidth() <= 2 * COLOR_SIZE || getHeight() <= BOTTOM_HEIGHT) {
                return;
            }

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
            Rectangle bar = new Rectangle(COLOR_SIZE / 2, 0, getWidth() - COLOR_SIZE, getHeight() - BOTTOM_HEIGHT);
            if (inlineStops == this) {
                g2d.setPaint(gradientPaint(bar, ratiosArr, colorsArr, false, 0));
            } else {
                g2d.setPaint(new LinearGradientPaint(COLOR_SIZE / 2, 0, getWidth() - COLOR_SIZE, 0, ratiosArr, colorsArr));
            }
            g2d.fill(bar);
            g2d.setPaint(Color.black);
            g2d.draw(bar);

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
