/*
 *  Copyright (C) 2010-2014 PEXS
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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
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

    private ImageWrapperResizableIcon hoverIcon = null;
    private ImageWrapperResizableIcon clickIcon = null;
    private ImageWrapperResizableIcon normalIcon = null;

    public void setNormalIcon(ImageWrapperResizableIcon normalIcon) {
        this.normalIcon = normalIcon;
    }

    public ImageWrapperResizableIcon getHoverIcon() {
        return hoverIcon;
    }

    public ImageWrapperResizableIcon getClickIcon() {
        return clickIcon;
    }

    public ImageWrapperResizableIcon getNormalIcon() {
        return normalIcon;
    }

    public void setHoverIcon(ImageWrapperResizableIcon hoverIcon) {
        this.hoverIcon = hoverIcon;
    }

    public void setClickIcon(ImageWrapperResizableIcon clickIcon) {
        this.clickIcon = clickIcon;
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.plaf.basic.BasicButtonUI#paint(java.awt.Graphics,
     * javax.swing.JComponent)
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        JRibbonApplicationMenuButton b = (JRibbonApplicationMenuButton) c;

        if (hoverIcon != null) {
            hoverIcon.setPreferredSize(b.getSize());
        }
        if (clickIcon != null) {
            clickIcon.setPreferredSize(b.getSize());
        }
        if (normalIcon != null) {
            normalIcon.setPreferredSize(b.getSize());
        }

        this.layoutInfo = this.layoutManager.getLayoutInfo(this.commandButton,
                g);
        commandButton.putClientProperty("icon.bounds", layoutInfo.iconRect);

        Graphics2D g2d = (Graphics2D) g.create();
        SubstanceFillPainter fillPainter = SubstanceCoreUtilities
                .getFillPainter(commandButton);
        SubstanceBorderPainter borderPainter = SubstanceCoreUtilities
                .getBorderPainter(commandButton);
        BufferedImage fullAlphaBackground = RibbonApplicationMenuButtonBackgroundDelegate
                .getFullAlphaBackground(b, fillPainter, borderPainter,
                        commandButton.getWidth(), commandButton.getHeight());
        g2d.drawImage(fullAlphaBackground, 0, 0, null);

        // Paint the icon
        ResizableIcon icon = getCurrentIcon(b);
        /*if(icon instanceof ImageWrapperResizableIcon){
         ImageWrapperResizableIcon iw=(ImageWrapperResizableIcon)icon;
         iw.setPreferredSize(b.getSize());
         iw.setDimension(b.getSize());                    
         }*/

        if (icon != null) {
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            Rectangle iconRect = new Rectangle((c.getWidth() - iconWidth) / 2,
                    (c.getHeight() - iconHeight) / 2, iconWidth, iconHeight);
            paintButtonIcon(g2d, iconRect);
        }

        g2d.dispose();
    }

    private ResizableIcon getCurrentIcon(JRibbonApplicationMenuButton button) {
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
    @Override
    protected void paintButtonIcon(Graphics g, Rectangle iconRect) {
        Icon regular = this.applicationMenuButton.isEnabled() ? getCurrentIcon(this.applicationMenuButton)
                : this.applicationMenuButton.getDisabledIcon();
        if (regular == null) {
            return;
        }
        boolean useThemed = SubstanceCoreUtilities
                .useThemedDefaultIcon(this.applicationMenuButton);

        if (regular != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            GhostPaintingUtils.paintGhostIcon(g2d, this.applicationMenuButton,
                    regular, iconRect);
            g2d.setComposite(LafWidgetUtilities.getAlphaComposite(
                    this.applicationMenuButton, g));

            if (!useThemed) {
                regular.paintIcon(this.applicationMenuButton, g2d, iconRect.x,
                        iconRect.y);
            } else {
                CommandButtonBackgroundDelegate.paintThemedCommandButtonIcon(
                        g2d, iconRect, this.applicationMenuButton, regular,
                        this.applicationMenuButton.getPopupModel(),
                        this.substanceVisualStateTracker
                        .getPopupStateTransitionTracker());
            }
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
