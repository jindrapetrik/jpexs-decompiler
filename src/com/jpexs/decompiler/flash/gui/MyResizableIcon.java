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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 *
 * @author JPEXS
 */
public class MyResizableIcon implements Icon {

    protected BufferedImage originalImage;

    protected Map<String, BufferedImage> cachedImages = new HashMap<>();

    public MyResizableIcon(BufferedImage originalImage) {
        this.originalImage = originalImage;
        width = originalImage.getWidth();
        height = originalImage.getHeight();
    }

    protected int width;

    protected int height;

    protected BufferedImage image;

    public void setDimension(Dimension dim) {
        setIconSize(dim.width, dim.height);
    }

    public void setIconSize(int renderWidth, int renderHeight) {
        width = renderWidth;
        height = renderHeight;
        String key = renderWidth + ":" + renderHeight;
        if (this.cachedImages.containsKey(key)) {
            image = this.cachedImages.get(key);
            return;
        }

        BufferedImage result = originalImage;
        float scaleX = (float) originalImage.getWidth()
                / (float) renderWidth;
        float scaleY = (float) originalImage.getHeight()
                / (float) renderHeight;

        float scale = Math.max(scaleX, scaleY);
        if (scale > 1.0f) {
            int finalWidth = (int) (originalImage.getWidth() / scale);
            result = FlamingoUtilities.createThumbnail(originalImage,
                    finalWidth);
        }
        cachedImages.put(renderWidth + ":" + renderHeight, result);
        image = result;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (image != null) {
            int dx = (this.width - image.getWidth()) / 2;
            int dy = (this.height - image.getHeight()) / 2;
            g.drawImage(image, x + dx, y + dy, null);
        }
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
