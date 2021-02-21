/*
 * Copyright (c) 2005-2010 Flamingo Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.flamingo.internal.ui.ribbon;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager.PopupEvent;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandToggleButtonUI;
import org.pushingpixels.flamingo.internal.utils.*;

/**
 * Basic UI for toggle button of ribbon tasks {@link JRibbonTaskToggleButton}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicRibbonTaskToggleButtonUI extends BasicCommandToggleButtonUI {
	protected PopupPanelManager.PopupListener popupListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicRibbonTaskToggleButtonUI();
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		Font f = this.commandButton.getFont();
		if (f == null || f instanceof UIResource) {
			this.commandButton.setFont(FlamingoUtilities.getFont(null,
					"Ribbon.font", "Button.font", "Panel.font"));
		}

		Border border = this.commandButton.getBorder();
		if (border == null || border instanceof UIResource) {
			Border toInstall = UIManager
					.getBorder("RibbonTaskToggleButton.border");
			if (toInstall == null)
				toInstall = new BorderUIResource.EmptyBorderUIResource(1, 12,
						1, 12);
			this.commandButton.setBorder(toInstall);
		}

		this.commandButton.setFlat(true);
		this.commandButton.setOpaque(false);
	}

	@Override
	protected void installListeners() {
		super.installListeners();

		this.popupListener = new PopupPanelManager.PopupListener() {
			@Override
			public void popupShown(PopupEvent event) {
				if (event.getSource() == commandButton) {
					commandButton.getActionModel()
							.setSelected(isTaskSelected());
				}
			}

			@Override
			public void popupHidden(PopupEvent event) {
				if (event.getSource() == commandButton) {
					commandButton.getActionModel()
							.setSelected(isTaskSelected());
				}
			}

			private boolean isTaskSelected() {
				JRibbon ribbon = (JRibbon) SwingUtilities.getAncestorOfClass(
						JRibbon.class, commandButton);
				if (ribbon == null)
					return false;

				return ribbon.getSelectedTask() == ((JRibbonTaskToggleButton) commandButton)
						.getRibbonTask();
			}
		};
		PopupPanelManager.defaultManager().addPopupListener(this.popupListener);
	}

	@Override
	protected void uninstallListeners() {
		PopupPanelManager.defaultManager().removePopupListener(
				this.popupListener);
		this.popupListener = null;

		super.uninstallListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#update(java.awt.Graphics,
	 * javax.swing.JComponent)
	 */
	@Override
	public void update(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g.create();
		RenderingUtils.installDesktopHints(g2d);
		this.layoutInfo = this.layoutManager.getLayoutInfo(this.commandButton,
				g);
		this.paintButtonBackground(g2d, new Rectangle(0, 0, c.getWidth(), c
				.getHeight() + 10));
		this.paintText(g2d);
		g2d.dispose();
	}

	protected void paintText(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		String toPaint = this.commandButton.getText();

		// compute the insets
		int fullInsets = this.commandButton.getInsets().left;
		int pw = this.getPreferredSize(this.commandButton).width;
		int mw = this.getMinimumSize(this.commandButton).width;
		int w = this.commandButton.getWidth();
		int h = this.commandButton.getHeight();
		int insets = fullInsets - (pw - w) * (fullInsets - 2) / (pw - mw);

		// and the text rectangle
		Rectangle textRect = new Rectangle(insets,
				1 + (h - fm.getHeight()) / 2, w - 2 * insets, fm.getHeight());

		// show the first characters that fit into the available text rectangle
		while (true) {
			if (toPaint.length() == 0)
				break;
			int strWidth = fm.stringWidth(toPaint);
			if (strWidth <= textRect.width)
				break;
			toPaint = toPaint.substring(0, toPaint.length() - 1);
		}
		BasicGraphicsUtils.drawString(g, toPaint, -1, textRect.x, textRect.y
				+ fm.getAscent());
	}

	/**
	 * Paints the button background.
	 * 
	 * @param graphics
	 *            Graphics context.
	 * @param toFill
	 *            Rectangle to fill.
	 */
	@Override
	protected void paintButtonBackground(Graphics graphics, Rectangle toFill) {
		JRibbon ribbon = (JRibbon) SwingUtilities.getAncestorOfClass(
				JRibbon.class, this.commandButton);

		this.buttonRendererPane.setBounds(toFill.x, toFill.y, toFill.width,
				toFill.height);
		ButtonModel model = this.rendererButton.getModel();
		model.setEnabled(this.commandButton.isEnabled());
		model.setSelected(false);
		// System.out.println(toggleTabButton.getText() + ":"
		// + toggleTabButton.isSelected());

		// selected task toggle button should not have any background if
		// the ribbon is minimized and it is not shown in a popup
		boolean displayAsSelected = this.commandButton.getActionModel()
				.isSelected();
		model.setRollover(displayAsSelected
				|| this.commandButton.getActionModel().isRollover());
		model.setPressed(false);
		if (model.isRollover()) {
			Graphics2D g2d = (Graphics2D) graphics.create();
			// partial translucency if it is not selected
			if (!this.commandButton.getActionModel().isSelected()) {
				g2d.setComposite(AlphaComposite.SrcOver.derive(0.4f));
			}
			g2d.translate(toFill.x, toFill.y);

			Color contextualGroupHueColor = ((JRibbonTaskToggleButton) this.commandButton)
					.getContextualGroupHueColor();
			boolean isContextualTask = (contextualGroupHueColor != null);
			if (!isContextualTask) {
				Shape clip = g2d.getClip();
				g2d.clip(FlamingoUtilities.getRibbonTaskToggleButtonOutline(
						toFill.width, toFill.height, 2));
				this.buttonRendererPane.paintComponent(g2d,
						this.rendererButton, this.commandButton, toFill.x
								- toFill.width / 2, toFill.y - toFill.height
								/ 2, 2 * toFill.width, 2 * toFill.height, true);
				g2d.setColor(FlamingoUtilities.getBorderColor().darker());
				g2d.setClip(clip);
				g2d.draw(FlamingoUtilities.getRibbonTaskToggleButtonOutline(
						toFill.width, toFill.height + 1, 2));
			} else {
				// draw to an offscreen image, colorize and draw the colorized
				// image
				BufferedImage offscreen = FlamingoUtilities.getBlankImage(
						toFill.width, toFill.height);
				Graphics2D offscreenGraphics = offscreen.createGraphics();
				Shape clip = g2d.getClip();
				offscreenGraphics.clip(FlamingoUtilities
						.getRibbonTaskToggleButtonOutline(toFill.width,
								toFill.height, 2));
				this.buttonRendererPane.paintComponent(offscreenGraphics,
						this.rendererButton, this.commandButton, toFill.x
								- toFill.width / 2, toFill.y - toFill.height
								/ 2, 2 * toFill.width, 2 * toFill.height, true);
				offscreenGraphics.setColor(FlamingoUtilities.getBorderColor()
						.darker());
				offscreenGraphics.setClip(clip);
				offscreenGraphics.draw(FlamingoUtilities
						.getRibbonTaskToggleButtonOutline(toFill.width,
								toFill.height + 1, 2));
				offscreenGraphics.dispose();

				ColorShiftFilter filter = new ColorShiftFilter(
						contextualGroupHueColor,
						RibbonContextualTaskGroup.HUE_ALPHA);
				BufferedImage colorized = filter.filter(offscreen, null);
				g2d.drawImage(colorized, 0, 0, null);
			}
			g2d.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicButtonUI#getPreferredSize(javax.swing.JComponent
	 * )
	 */
	@Override
	public Dimension getPreferredSize(JComponent c) {
		JRibbonTaskToggleButton b = (JRibbonTaskToggleButton) c;

		Icon icon = b.getIcon();
		String text = b.getText();

		Font font = b.getFont();
		FontMetrics fm = b.getFontMetrics(font);

		Rectangle iconR = new Rectangle();
		Rectangle textR = new Rectangle();
		Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

		SwingUtilities.layoutCompoundLabel(b, fm, text, icon,
				SwingUtilities.CENTER, b.getHorizontalAlignment(),
				SwingUtilities.CENTER, SwingUtilities.CENTER, viewR, iconR,
				textR, (text == null ? 0 : 6));

		Rectangle r = iconR.union(textR);

		Insets insets = b.getInsets();
		r.width += insets.left + insets.right;
		r.height += insets.top + insets.bottom;

		return r.getSize();
	}

	@Override
	public Dimension getMinimumSize(JComponent c) {
		JRibbonTaskToggleButton b = (JRibbonTaskToggleButton) c;

		Icon icon = b.getIcon();
		String text = "Www";

		Font font = b.getFont();
		FontMetrics fm = b.getFontMetrics(font);

		Rectangle iconR = new Rectangle();
		Rectangle textR = new Rectangle();
		Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

		SwingUtilities.layoutCompoundLabel(b, fm, text, icon,
				SwingUtilities.CENTER, b.getHorizontalAlignment(),
				SwingUtilities.CENTER, SwingUtilities.CENTER, viewR, iconR,
				textR, (text == null ? 0 : 6));

		Rectangle r = iconR.union(textR);

		Insets insets = b.getInsets();
		r.width += 4;
		r.height += insets.top + insets.bottom;

		return r.getSize();
	}
}
