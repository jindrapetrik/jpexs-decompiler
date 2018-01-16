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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.BasicRibbonApplicationMenuPopupPanelUI;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.internal.painter.HighlightPainterUtils;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;
import org.pushingpixels.substance.internal.utils.border.SubstanceBorder;

/**
 *
 * @author JPEXS
 */
public class MyRibbonApplicationMenuPopupPanelUI extends BasicRibbonApplicationMenuPopupPanelUI {

    public static ComponentUI createUI(JComponent c) {
        return new MyRibbonApplicationMenuPopupPanelUI();
    }

    @Override
    protected void installComponents() {
        super.installComponents();
        Border newBorder = new CompoundBorder(new SubstanceBorder(new Insets(2,
                2, 2, 2)), new Border() {
            @Override
            public boolean isBorderOpaque() {
                return true;
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(18, 0, 0, 0);
            }

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y,
                    int width, int height) {
                SubstanceColorScheme bgFillScheme = SubstanceColorSchemeUtilities
                        .getColorScheme(c,
                                ColorSchemeAssociationKind.HIGHLIGHT,
                                ComponentState.ENABLED);
                SubstanceColorScheme bgBorderScheme = SubstanceColorSchemeUtilities
                        .getColorScheme(c,
                                ColorSchemeAssociationKind.HIGHLIGHT_BORDER,
                                ComponentState.ENABLED);
                HighlightPainterUtils.paintHighlight(g, null, c, new Rectangle(
                        x, y, width, height), 0.0f, null, bgFillScheme,
                        bgBorderScheme);

                // draw the application menu button
                JRibbonApplicationMenuButton rendererButton = new JRibbonApplicationMenuButton(
                        applicationMenuPopupPanel.getAppMenuButton()
                        .getRibbon());

                JRibbonApplicationMenuButton appMenuButton = applicationMenuPopupPanel
                        .getAppMenuButton();
                rendererButton.applyComponentOrientation(appMenuButton
                        .getComponentOrientation());

                rendererButton.setPopupKeyTip(appMenuButton.getPopupKeyTip());
                rendererButton.getPopupModel().setPopupShowing(true);
                rendererButton.setDisplayState(appMenuButton.getDisplayState());

                rendererButton.getPopupModel().setRollover(false);
                rendererButton.getPopupModel().setPressed(true);
                rendererButton.getPopupModel().setArmed(true);

                CellRendererPane buttonRendererPane = new CellRendererPane();
                Point buttonLoc = appMenuButton.getLocationOnScreen();
                Point panelLoc = c.getLocationOnScreen();

                buttonRendererPane.setBounds(panelLoc.x - buttonLoc.x,
                        panelLoc.y - buttonLoc.y, appMenuButton.getWidth(),
                        appMenuButton.getHeight());

                buttonRendererPane.paintComponent(g, rendererButton,
                        (Container) c, -panelLoc.x + buttonLoc.x, -panelLoc.y
                        + buttonLoc.y, appMenuButton.getWidth(),
                        appMenuButton.getHeight(), true);
                /*g.setColor(Color.red);
                         g.fillRect(0, 0, width,height);*/
            }
        });
        this.applicationMenuPopupPanel.setBorder(newBorder);

        this.panelLevel2.setBorder(new Border() {
            @Override
            public Insets getBorderInsets(Component c) {
                boolean ltr = c.getComponentOrientation().isLeftToRight();
                return new Insets(0, ltr ? 1 : 0, 0, ltr ? 0 : 1);
            }

            @Override
            public boolean isBorderOpaque() {
                return true;
            }

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y,
                    int width, int height) {
                int componentFontSize = SubstanceSizeUtils
                        .getComponentFontSize(null);
                int borderDelta = (int) Math.floor(SubstanceSizeUtils
                        .getBorderStrokeWidth(componentFontSize) / 2.0);
                float borderThickness = SubstanceSizeUtils
                        .getBorderStrokeWidth(componentFontSize);

                Graphics2D g2d = (Graphics2D) g.create();
                SubstanceColorScheme scheme = SubstanceColorSchemeUtilities
                        .getColorScheme(applicationMenuPopupPanel,
                                ColorSchemeAssociationKind.BORDER,
                                ComponentState.ENABLED);
                g2d.setColor(scheme.getMidColor());
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_NORMALIZE);
                int joinKind = BasicStroke.JOIN_ROUND;
                int capKind = BasicStroke.CAP_BUTT;
                g2d.setStroke(new BasicStroke(borderThickness, capKind,
                        joinKind));

                boolean ltr = applicationMenuPopupPanel
                        .getComponentOrientation().isLeftToRight();
                int lineX = ltr ? borderDelta : c.getWidth() - borderDelta - 1;
                g2d.drawLine(lineX, borderDelta, lineX, height - 1 - 2
                        * borderDelta);

                g2d.dispose();
            }
        });
        this.mainPanel.setBorder(new SubstanceBorder());
    }
}
