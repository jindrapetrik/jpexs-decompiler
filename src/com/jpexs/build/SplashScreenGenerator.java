/*
 * Copyright (C) 2010-2026 JPEXS
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
package com.jpexs.build;

import com.jpexs.decompiler.flash.helpers.BMPFile;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Generates BMP splashscreen with version info for inclusion in ffdec.exe
 *
 * @author JPEXS
 */
public class SplashScreenGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Expected version parameter");
        }
        String ver = args[0];
        BufferedImage img = ImageIO.read(new File("graphics/splash3.png"));
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0, 100, 255, 128));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        int sw = g.getFontMetrics().stringWidth(ver);
        int x = 190;
        int y = 170;
        int w = 220;
        int h = 25;
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawString(ver, x + w / 2 - sw / 2, y + h);

        BMPFile.saveBitmap(img, new File("build/splash.bmp"));
    }
}
