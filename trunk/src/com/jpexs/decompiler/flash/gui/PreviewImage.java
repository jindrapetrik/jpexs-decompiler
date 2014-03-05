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
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class PreviewImage extends JPanel {

    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private static final int PREVIEW_SIZE = 100;
    private Image image;
    private boolean rendering;
    private Tag tag;

    public PreviewImage(Tag tag) {
        this.tag = tag; 
        Dimension dim = new Dimension(PREVIEW_SIZE, PREVIEW_SIZE);
        setMinimumSize(dim);
        setMaximumSize(dim);
        setPreferredSize(dim);
        setSize(PREVIEW_SIZE, PREVIEW_SIZE);
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
                        Component parent = getParent();
                        if (parent != null) {
                            parent.repaint();
                        }
                    }
                });
                return null;
            }
            
        });

    }

    private Image renderImage(SWF swf, Tag tag) {
        BoundedTag boundedTag = (BoundedTag) tag;
        RECT rect = boundedTag.getRect(swf.characters, new Stack<Integer>());
        Matrix m = new Matrix();
        double scale = 1;
        if (rect.getWidth() > PREVIEW_SIZE * SWF.unitDivisor) {
            scale = PREVIEW_SIZE * SWF.unitDivisor / rect.getWidth();
        }
        if (rect.getHeight() > PREVIEW_SIZE * SWF.unitDivisor) {
            scale = Math.min(scale, PREVIEW_SIZE * SWF.unitDivisor / rect.getHeight());
        }
        m.translate(-rect.Xmin, -rect.Ymin);
        m = m.preConcatenate(Matrix.getScaleInstance(scale));
        int width = (int) (scale * rect.getWidth() / SWF.unitDivisor);
        int height = (int) (scale * rect.getHeight() / SWF.unitDivisor);
        if (width == 0 || height == 0) {
            return null;
        }
        SerializableImage image = new SerializableImage(width, height, SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();
        DrawableTag drawable = (DrawableTag) tag;
        drawable.toImage(0, 0, swf.tags, swf.characters, new Stack<Integer>(), image, m, new ColorTransform());
        return image.getBufferedImage();
    }

    public static PreviewImage createFolderPreviewImage(Tag tag) {
        PreviewImage imagePanel = new PreviewImage(tag);
        return imagePanel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        } else {
            renderImageTask(tag);
        }
    }
}
