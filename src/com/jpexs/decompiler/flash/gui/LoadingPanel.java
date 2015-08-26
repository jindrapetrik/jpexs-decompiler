/*
 *  Copyright (C) 2010-2015 JPEXS
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

/**
 * Panel with loading animation
 *
 * @author JPEXS
 */
public class LoadingPanel extends JPanel {

    BufferedImage lastImage;

    int lastSize = 0;

    Color col;

    double rotation = 0;

    Timer drawTimer;

    public LoadingPanel(int width, int height) {
        this.col = new Color(0, 0, 255);
        setPreferredSize(new Dimension(width, height));
    }

    private synchronized void setRotation(double rotation) {
        this.rotation = rotation;
    }

    private synchronized double getRotation() {
        return rotation;
    }

    private synchronized int getLastSize() {
        return lastSize;
    }

    private synchronized void redrawImage(int size) {
        if (drawTimer != null) {
            drawTimer.cancel();
            drawTimer = null;
        }
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D big = bi.createGraphics();
        big.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        big.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        big.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int br = size / 16;
        if (br < 2) {
            br = 2;
        }
        int border = 2;
        int r = size / 2 - br - border;

        int o = (int) Math.round(Math.PI * 2 * r);
        double max = Math.PI * 2;
        double skip = max / o;

        big.setComposite(AlphaComposite.Src);
        for (int i = 0; i < o; i++) {
            int c = i * 256 / o;
            double alfa = skip * i;
            double x = border + br + r + Math.round(Math.sin(alfa) * r);
            double y = border + br + r + Math.round(Math.cos(alfa) * r);
            big.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), c));
            big.fill(new Ellipse2D.Double(x - br, y - br, 2 * br, 2 * br));
        }
        lastImage = bi;
        lastSize = size;
        drawTimer = new Timer();
        double timeSpin = 1000;
        double delay;
        while ((delay = timeSpin / o) < 10) {
            o--;
        }
        final int segments = o;
        int idelay = (int) Math.round(delay);
        drawTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (isVisible()) {
                    double rot2 = rotation - Math.PI * 2 / segments;
                    if (rot2 < 0) {
                        rot2 += Math.PI * 2;
                    }
                    setRotation(rot2);
                    repaint();
                }
            }
        }, idelay, idelay);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        int size = Math.min(getWidth(), getHeight());
        if (lastImage == null || getLastSize() != size) {
            redrawImage(size);
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform t = AffineTransform.getRotateInstance(getRotation(), size / 2.0, size / 2.0);
        g2.setTransform(t);
        g2.drawImage(lastImage, 0, 0, this);
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible) {
            if (drawTimer != null) {
                drawTimer.cancel();
                drawTimer = null;
            }
        }

        super.setVisible(visible);
    }
}
