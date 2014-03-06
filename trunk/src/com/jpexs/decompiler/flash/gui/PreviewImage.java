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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.SerializableImage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class PreviewImage extends JPanel {

    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private static final int PREVIEW_SIZE = 150;
    private static final int BORDER_SIZE = 5;
    private Image image;
    private boolean rendering;
    private Tag tag;

    public PreviewImage(Tag tag) {
        this.tag = tag;
        Dimension dim = new Dimension(PREVIEW_SIZE + 2 * BORDER_SIZE, PREVIEW_SIZE + 2 * BORDER_SIZE);
        setMinimumSize(dim);
        setMaximumSize(dim);
        setPreferredSize(dim);
        setSize(dim);
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    private synchronized void renderImageTask(final Tag tag) {
        if (rendering) {
            return;
        }
        rendering = true;
        executor.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                image = renderImage(tag.getSwf(), tag);
                View.execInEventDispatch(new Runnable() {

                    @Override
                    public void run() {
                        // todo: 
                        // call repaint on MainPanel.folderPreviewPanel
                        // this is a hack, but otherwise the preview panel looks crazy sometimes
                        // how to handle it in a better way?
                        // normally repaint() on this panel should be enough
                        Component parent = getParent();
                        if (parent != null) {
                            parent = parent.getParent();
                            if (parent != null) {
                                parent.repaint();
                            }
                        }
                    }
                });
                return null;
            }

        });

    }

    private Image renderImage(SWF swf, Tag tag) {

        double scale = 1;
        int width = 0;
        int height = 0;
        SerializableImage imgSrc = null;
        Matrix m = new Matrix();
        if (tag instanceof ImageTag) {
            imgSrc = ((ImageTag) tag).getImage();
            width = (imgSrc.getWidth());
            height = (imgSrc.getHeight());
        } else if (tag instanceof BoundedTag) {
            BoundedTag boundedTag = (BoundedTag) tag;
            RECT rect = boundedTag.getRect();
            width = (int) (rect.getWidth() / SWF.unitDivisor);
            height = (int) (rect.getHeight() / SWF.unitDivisor);
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
            h = h2;
        } else {
            w = w2;
        }

        scale = (double) w / (double) w1;
        if (w1 <= w2 && h1 <= h2) {
            scale = 1;
        }

        /*if (width > PREVIEW_SIZE * SWF.unitDivisor) {
         scale = PREVIEW_SIZE * SWF.unitDivisor / width;
         }
         if (height > PREVIEW_SIZE * SWF.unitDivisor) {
         scale = Math.min(scale, PREVIEW_SIZE * SWF.unitDivisor / height);
         }*/
        m = m.preConcatenate(Matrix.getScaleInstance(scale));
        width = (int) (scale * width);
        height = (int) (scale * height);
        if (width == 0 || height == 0) {
            return null;
        }

        SerializableImage image = new SerializableImage(width, height, SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();
        if (imgSrc == null) {
            DrawableTag drawable = (DrawableTag) tag;
            drawable.toImage(0, 0, image, m, new ColorTransform());
        } else {
            Graphics2D g = (Graphics2D) image.getGraphics();
            g.setTransform(m.toTransform());
            g.drawImage(imgSrc.getBufferedImage(), 0, 0, null);
        }
        return image.getBufferedImage();
    }

    public static Component createFolderPreviewImage(Tag tag) {
        JPanel pan = new JPanel(new BorderLayout());
        PreviewImage imagePanel = new PreviewImage(tag);
        pan.add(imagePanel, BorderLayout.CENTER);
        String s = tag.getTagName();
        if (tag instanceof CharacterTag) {
            s = s + " (" + ((CharacterTag) tag).getCharacterId() + ")";
        }
        JLabel lab = new JLabel(s);
        lab.setFont(lab.getFont().deriveFont(AffineTransform.getScaleInstance(0.8, 0.8)));
        pan.add(lab, BorderLayout.SOUTH);
        return pan;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) {
            int x = (getWidth() / 2) - (image.getWidth(this) / 2);
            int y = (getHeight() / 2) - (image.getHeight(this) / 2);
            g.drawImage(image, x, y, null);
        } else {
            renderImageTask(tag);
        }
    }
}
