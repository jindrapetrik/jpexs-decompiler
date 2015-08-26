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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.flamingo.api.common.model.PopupButtonModel;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.BasicRibbonApplicationMenuButtonUI;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;
import org.pushingpixels.lafwidget.animation.effects.GhostingListener;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.flamingo.common.ui.ActionPopupTransitionAwareUI;
import org.pushingpixels.substance.flamingo.utils.CommandButtonVisualStateTracker;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;

/**
 * UI for {@link JRibbonApplicationMenuButton} components in <b>Substance</b>
 * look and feel.
 *
 * @author Kirill Grouchnikov
 */
/**
 *
 * @author JPEXS
 */
public class MyRibbonApplicationMenuButtonUI extends BasicRibbonApplicationMenuButtonUI implements
        ActionPopupTransitionAwareUI {

    private MyResizableIcon hoverIcon = null;

    private MyResizableIcon clickIcon = null;

    private MyResizableIcon normalIcon = null;

    private MyResizableIcon clearIcon = null;

    private final boolean buttonResized = false;

    public MyResizableIcon getClickIcon() {
        return clickIcon;
    }

    public static MyResizableIcon[] getIcons() {
        MyResizableIcon clearIcon = View.getMyResizableIcon("buttonicon_clear_256");
        return new MyResizableIcon[]{
            clearIcon,
            //normal
            new MyResizableIcon(clearIcon.originalImage) {

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
                    g2.setPaint(new RadialGradientPaint(getIconWidth() / 2.0f, getIconHeight() / 2.0f, getIconWidth() / 2.0f, new float[]{0.32f, 0.84f, 1f}, new Color[]{
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.HIGHLIGHT, ComponentState.ENABLED).shiftBackground(Color.white, 0.5).getUltraLightColor(),
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getMidColor(),
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED).getUltraDarkColor()
                    }, MultipleGradientPaint.CycleMethod.NO_CYCLE));
                    Shape s = new Ellipse2D.Double(x, y, getIconWidth(), getIconHeight());
                    g2.fill(s);
                    g2.setPaint(skin.getEnabledColorScheme(DecorationAreaType.PRIMARY_TITLE_PANE).getMidColor());
                    super.paintIcon(c, g, x, y);
                }
            },
            //hover
            new MyResizableIcon(clearIcon.originalImage) {

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
                    g2.setPaint(new RadialGradientPaint(getIconWidth() / 2, getIconHeight() / 2, getIconWidth() / 2, new float[]{0.32f, 0.84f, 1f}, new Color[]{
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.HIGHLIGHT, ComponentState.ROLLOVER_UNSELECTED)/*.shiftBackground(Color.white, 0.8)*/.getUltraLightColor(),
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_UNSELECTED).getMidColor(),
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.BORDER, ComponentState.ROLLOVER_UNSELECTED)/*.shiftBackground(new Color(0x7c, 0x7c, 0x7c), 0.8)*/.getUltraDarkColor()
                    }, MultipleGradientPaint.CycleMethod.NO_CYCLE));
                    Shape s = new Ellipse2D.Double(x, y, getIconWidth(), getIconHeight());
                    g2.fill(s);
                    super.paintIcon(c, g, x, y);
                }
            },
            //click
            new MyResizableIcon(clearIcon.originalImage) {

                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
                    g2.setPaint(new RadialGradientPaint(getIconWidth() / 2, getIconHeight() / 2, getIconWidth() / 2, new float[]{0.2f, 0.5f, 0.8f}, new Color[]{
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getUltraLightColor(),
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getMidColor(),
                        skin.getColorScheme(DecorationAreaType.SECONDARY_TITLE_PANE, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).shiftBackground(Color.black, 0.7).getUltraDarkColor()
                    }, MultipleGradientPaint.CycleMethod.NO_CYCLE));
                    Shape s = new Ellipse2D.Double(x, y, getIconWidth(), getIconHeight());
                    g2.fill(s);
                    AffineTransform origt = g2.getTransform();
                    AffineTransform t = (AffineTransform) origt.clone();
                    t.translate(-getIconWidth() / 2, -getIconHeight() / 2);
                    t.scale(0.8, 0.8);
                    t.translate(getIconWidth() / 2 + getIconWidth() / 4, getIconHeight() / 2 + getIconHeight() / 4);
                    g2.setTransform(t);
                    g2.setPaint(Color.BLACK);
                    super.paintIcon(c, g, x, y);
                    g2.setTransform(origt);
                }
            }

        };
    }

    public MyRibbonApplicationMenuButtonUI() {
        super();
        MyResizableIcon[] icons = getIcons();
        clearIcon = icons[0];
        normalIcon = icons[1];
        hoverIcon = icons[2];
        clickIcon = icons[3];

    }

    /**
     * Model change listener for ghost image effects.
     */
    private GhostingListener substanceModelChangeListener;

    /**
     * Tracker for visual state transitions.
     */
    protected CommandButtonVisualStateTracker substanceVisualStateTracker;

    public static ComponentUI createUI(JComponent c) {
        return new MyRibbonApplicationMenuButtonUI();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jvnet.flamingo.common.ui.BasicCommandButtonUI#installListeners()
     */
    @Override
    protected void installListeners() {
        super.installListeners();

        this.substanceVisualStateTracker = new CommandButtonVisualStateTracker();
        this.substanceVisualStateTracker.installListeners(this.commandButton);

        this.substanceModelChangeListener = new GhostingListener(
                this.commandButton, this.commandButton.getActionModel());
        this.substanceModelChangeListener.registerListeners();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#uninstallListeners()
     */
    @Override
    protected void uninstallListeners() {
        this.substanceVisualStateTracker.uninstallListeners(this.commandButton);
        this.substanceVisualStateTracker = null;

        this.substanceModelChangeListener.unregisterListeners();
        this.substanceModelChangeListener = null;

        super.uninstallListeners();
    }

    private void updateIcons(JComponent c) {

        Dimension dim = c.getPreferredSize();
        hoverIcon.setDimension(dim);
        clickIcon.setDimension(dim);
        normalIcon.setDimension(dim);

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.plaf.basic.BasicButtonUI#paint(java.awt.Graphics,
     * javax.swing.JComponent)
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        JRibbonApplicationMenuButton b = (JRibbonApplicationMenuButton) c;

        updateIcons(c);

        this.layoutInfo = this.layoutManager.getLayoutInfo(this.commandButton, g);
        commandButton.putClientProperty("icon.bounds", layoutInfo.iconRect);

        Graphics2D g2d = (Graphics2D) g.create();

        // Paint the icon
        Icon icon = getCurrentIcon(b);

        if (icon != null) {
            paintButtonIcon(g2d);
        }

        g2d.dispose();
    }

    private Icon getCurrentIcon(JRibbonApplicationMenuButton button) {
        PopupButtonModel mod = button.getPopupModel();
        if (mod.isPopupShowing()) {
            if (clickIcon != null) {
                return clickIcon;
            }
        }
        if (mod.isRollover()) {
            if (hoverIcon != null) {
                return hoverIcon;
            }
        }
        if (normalIcon != null) {
            return normalIcon;
        }
        return button.getIcon();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#paintButtonIcon(java
     * .awt.Graphics, java.awt.Rectangle)
     */
    protected void paintButtonIcon(Graphics g) {
        Icon regular = getCurrentIcon(this.applicationMenuButton);
        if (regular == null) {
            return;
        }
        //SubstanceLookAndFeel.getCurrentSkin().getActiveColorScheme(DecorationAreaType.GENERAL);

        Graphics2D g2d = (Graphics2D) g.create();
        regular.paintIcon(this.applicationMenuButton, g2d, 0, 0);
        g2d.dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jvnet.flamingo.ribbon.ui.appmenu.BasicRibbonApplicationMenuButtonUI
     * #update(java.awt.Graphics, javax.swing.JComponent)
     */
    @Override
    public void update(Graphics g, JComponent c) {
        this.paint(g, c);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jvnet.substance.SubstanceButtonUI#contains(javax.swing.JComponent,
     * int, int)
     */
    @Override
    public boolean contains(JComponent c, int x, int y) {
        // allow clicking anywhere in the area (around the button
        // round outline as well) to activate the button.
        return (x >= 0) && (x < c.getWidth()) && (y >= 0)
                && (y < c.getHeight());
    }

    @Override
    public StateTransitionTracker getActionTransitionTracker() {
        return this.substanceVisualStateTracker
                .getActionStateTransitionTracker();
    }

    @Override
    public StateTransitionTracker getPopupTransitionTracker() {
        return this.substanceVisualStateTracker
                .getPopupStateTransitionTracker();
    }

    @Override
    public StateTransitionTracker getTransitionTracker() {
        return this.substanceVisualStateTracker
                .getPopupStateTransitionTracker();
    }

    @Override
    public boolean isInside(MouseEvent me) {
        return true;
    }
}
