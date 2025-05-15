/*
 * Copyright (C) 2024 JPEXS
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
package com.jpexs.decompiler.flash.gui.colordialog;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.UIManager;

/**
 *
 * @author JPEXS
 */
class MyRecentSwatchPanel extends MySwatchPanel {

    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesRecentSwatchSize", getLocale());
        numSwatches = new Dimension(5, 7);
        gap = new Dimension(1, 1);
    }

    protected void initColors() {
        Color defaultRecentColor = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor", getLocale());
        int numColors = numSwatches.width * numSwatches.height;
        colors = new Color[numColors];
        String recentColorsStr = Configuration.recentColors.get();
        for (int i = 0; i < numColors; i++) {
            colors[i] = defaultRecentColor;
        }
        Pattern hexColorPattern = Pattern.compile("^#(?<a>[a-fA-F0-9]{2})(?<r>[a-fA-F0-9]{2})(?<g>[a-fA-F0-9]{2})(?<b>[a-fA-F0-9]{2})$");
        if (!recentColorsStr.isEmpty()) {
            String[] colorsHex = recentColorsStr.split(",", -1);
            for (int i = 0; i < colorsHex.length; i++) {
                Matcher m = hexColorPattern.matcher(colorsHex[i]);
                if (m.matches()) {
                    colors[i] = new Color(Integer.parseInt(m.group("r"), 16), Integer.parseInt(m.group("g"), 16), Integer.parseInt(m.group("b"), 16), Integer.parseInt(m.group("a"), 16));
                }
            }
        }
    }

    public void setMostRecentColor(Color c) {
        if (colors[0].equals(c)) {
            return;
        }
        System.arraycopy(colors, 0, colors, 1, colors.length - 1);
        colors[0] = c;

        List<String> colorsAsStr = new ArrayList<>();
        for (int i = 0; i < colors.length; i++) {
            colorsAsStr.add(new RGBA(colors[i]).toHexARGB());
        }
        Configuration.recentColors.set(String.join(",", colorsAsStr));

        repaint();
    }
}
