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

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.geom.GeneralPath;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.JLabel;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.painter.border.StandardBorderPainter;
import org.pushingpixels.substance.internal.utils.SubstanceOutlineUtilities;

/**
 *
 * @author JPEXS
 */
public class HeaderLabel extends JLabel {

    public HeaderLabel(String text) {
        super(text);
        //setBorder(BorderFactory.createRaisedBevelBorder());


        /*setBorder(new Border() {

         @Override
         public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
         g.setColor(Color.gray);
         g.drawLine(0, 0,width-1, 0);
         g.drawLine(0, 0, 0, height-1);
         g.setColor(Color.darkGray);
         g.drawLine(width-1, 0, width-1, height-1);
         g.drawLine(0, height-1, width-1, height-1);
         }

         @Override
         public Insets getBorderInsets(Component c) {
         return new Insets(2, 2, 2, 2);
         }

         @Override
         public boolean isBorderOpaque() {
         return false;
         }
         });*/
    }

    @Override
    public void paint(Graphics g) {
        if (Configuration.useRibbonInterface.get()) {
            SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
            g.setColor(skin.getColorScheme(DecorationAreaType.HEADER, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor());
        } else {
            g.setColor(SystemColor.control);
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        if (Configuration.useRibbonInterface.get()) {
            StandardBorderPainter borderPainter = new StandardBorderPainter();

            Set<SubstanceConstants.Side> straightSides = EnumSet.of(SubstanceConstants.Side.BOTTOM);
            int dy = 0;
            float cornerRadius = 5f;
            int borderThickness = 1;
            int borderInsets = 0;
            GeneralPath contourInner = borderPainter.isPaintingInnerContour() ? SubstanceOutlineUtilities.getBaseOutline(getWidth(), getHeight() + dy,
                    cornerRadius - borderThickness, straightSides, borderThickness + borderInsets)
                    : null;

            GeneralPath contour = SubstanceOutlineUtilities.getBaseOutline(getWidth(),
                    getHeight() + dy, cornerRadius, straightSides, borderInsets);

            SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
            borderPainter.paintBorder(g, this, getWidth(), getHeight() + dy,
                    contour, contourInner, skin.getColorScheme(DecorationAreaType.HEADER, ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED));
            g.setColor(skin.getColorScheme(DecorationAreaType.HEADER, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getForegroundColor());
        } else {
            g.setColor(SystemColor.controlText);
        }

        JLabel lab = new JLabel(getText(), JLabel.CENTER);
        lab.setSize(getSize());
        lab.paint(g);
    }
}
