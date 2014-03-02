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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.flamingo.api.common.model.PopupButtonModel;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.BasicRibbonApplicationMenuButtonUI;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;
import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.lafwidget.animation.effects.GhostPaintingUtils;
import org.pushingpixels.lafwidget.animation.effects.GhostingListener;
import org.pushingpixels.substance.api.painter.border.SubstanceBorderPainter;
import org.pushingpixels.substance.api.painter.fill.SubstanceFillPainter;
import org.pushingpixels.substance.flamingo.common.ui.ActionPopupTransitionAwareUI;
import org.pushingpixels.substance.flamingo.utils.CommandButtonBackgroundDelegate;
import org.pushingpixels.substance.flamingo.utils.CommandButtonVisualStateTracker;
import org.pushingpixels.substance.flamingo.utils.RibbonApplicationMenuButtonBackgroundDelegate;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

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
    private boolean buttonResized = false;

    public MyResizableIcon getClickIcon() {
        return clickIcon;
    }

    public MyRibbonApplicationMenuButtonUI() {
        super();
        hoverIcon = View.getMyResizableIcon("buttonicon_hover_256");
        normalIcon = View.getMyResizableIcon("buttonicon_256");
        clickIcon = View.getMyResizableIcon("buttonicon_down_256");
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
        if (regular != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            regular.paintIcon(this.applicationMenuButton, g2d, 0, 0);
            g2d.dispose();
        }
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
