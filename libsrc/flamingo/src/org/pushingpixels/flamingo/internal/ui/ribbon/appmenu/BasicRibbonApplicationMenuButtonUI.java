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
package org.pushingpixels.flamingo.internal.ui.ribbon.appmenu;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.*;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.*;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandButtonUI;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 * Basic UI for ribbon application menu button
 * {@link JRibbonApplicationMenuButton}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicRibbonApplicationMenuButtonUI extends BasicCommandButtonUI {
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicRibbonApplicationMenuButtonUI();
	}

	/**
	 * The associated application menu button.
	 */
	protected JRibbonApplicationMenuButton applicationMenuButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicButtonUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.applicationMenuButton = (JRibbonApplicationMenuButton) c;
		super.installUI(c);
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();

		Border border = this.commandButton.getBorder();
		if (border == null || border instanceof UIResource) {
			Border toInstall = UIManager
					.getBorder("RibbonApplicationMenuButton.border");
			if (toInstall == null)
				toInstall = new BorderUIResource.EmptyBorderUIResource(4, 4, 4,
						4);
			this.commandButton.setBorder(toInstall);
		}

		this.commandButton.setOpaque(false);
	}

	@Override
	protected void configureRenderer() {
		this.buttonRendererPane = new CellRendererPane();
		this.commandButton.add(buttonRendererPane);
		this.rendererButton = new JButton("");
	}

	@Override
	protected void unconfigureRenderer() {
		this.commandButton.remove(this.buttonRendererPane);
		this.buttonRendererPane = null;
		this.rendererButton = null;
	}

	@Override
	protected void installComponents() {
		super.installComponents();

		final JRibbonApplicationMenuButton appMenuButton = (JRibbonApplicationMenuButton) this.commandButton;
		appMenuButton.setPopupCallback(new PopupPanelCallback() {
			@Override
			public JPopupPanel getPopupPanel(final JCommandButton commandButton) {
				JRibbonFrame ribbonFrame = (JRibbonFrame) SwingUtilities
						.getWindowAncestor(commandButton);
				final JRibbon ribbon = ribbonFrame.getRibbon();
				RibbonApplicationMenu ribbonMenu = ribbon.getApplicationMenu();
				final JRibbonApplicationMenuPopupPanel menuPopupPanel = new JRibbonApplicationMenuPopupPanel(
						appMenuButton, ribbonMenu);
				menuPopupPanel.applyComponentOrientation(appMenuButton
						.getComponentOrientation());
				menuPopupPanel
						.setCustomizer(new JPopupPanel.PopupPanelCustomizer() {
							@Override
							public Rectangle getScreenBounds() {
								boolean ltr = commandButton
										.getComponentOrientation()
										.isLeftToRight();

								int pw = menuPopupPanel.getPreferredSize().width;
								int x = ltr ? ribbon.getLocationOnScreen().x
										: ribbon.getLocationOnScreen().x
												+ ribbon.getWidth() - pw;
								int y = commandButton.getLocationOnScreen().y
										+ commandButton.getSize().height / 2
										+ 2;

								// make sure that the menu popup stays
								// in bounds
								Rectangle scrBounds = commandButton
										.getGraphicsConfiguration().getBounds();
								if ((x + pw) > (scrBounds.x + scrBounds.width)) {
									x = scrBounds.x + scrBounds.width - pw;
								}
								int ph = menuPopupPanel.getPreferredSize().height;
								if ((y + ph) > (scrBounds.y + scrBounds.height)) {
									y = scrBounds.y + scrBounds.height - ph;
								}

								return new Rectangle(
										x,
										y,
										menuPopupPanel.getPreferredSize().width,
										menuPopupPanel.getPreferredSize().height);
							}
						});
				return menuPopupPanel;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.BasicCommandButtonUI#paint(java.awt.Graphics
	 * , javax.swing.JComponent)
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g.create();
		// g2d.setColor(Color.red);
		// g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
		Insets ins = c.getInsets();
		// System.out.println(c.getWidth() + ":" + c.getHeight());
		this.paintButtonBackground(g2d, new Rectangle(ins.left, ins.top, c
				.getWidth()
				- ins.left - ins.right, c.getHeight() - ins.top - ins.bottom));

		this.layoutInfo = this.layoutManager.getLayoutInfo(this.commandButton,
				g);
		commandButton.putClientProperty("icon.bounds", layoutInfo.iconRect);

		this.paintButtonIcon(g2d, layoutInfo.iconRect);

		g2d.dispose();
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
		// graphics.setColor(Color.red);
		// graphics.fillRect(toFill.x, toFill.y, toFill.width,
		// toFill.height);
		// if (true)
		// return;
		//
		// System.out.println(toFill);
		this.buttonRendererPane.setBounds(toFill.x, toFill.y, toFill.width,
				toFill.height);
		ButtonModel model = this.rendererButton.getModel();
		model.setEnabled(true);
		model.setSelected(this.applicationMenuButton.getPopupModel()
				.isSelected());
		model.setRollover(this.applicationMenuButton.getPopupModel()
				.isRollover());
		model.setPressed(this.applicationMenuButton.getPopupModel().isPressed()
				|| this.applicationMenuButton.getPopupModel().isPopupShowing());
		model.setArmed(this.applicationMenuButton.getActionModel().isArmed());

		Graphics2D g2d = (Graphics2D) graphics.create();
		g2d.translate(toFill.x, toFill.y);

		Shape clip = g2d.getClip();
		g2d.clip(new Ellipse2D.Double(0, 0, toFill.width, toFill.height));
		this.rendererButton.setBorderPainted(false);
		this.buttonRendererPane.paintComponent(g2d, this.rendererButton,
				this.applicationMenuButton, -toFill.width / 2,
				-toFill.height / 2, 2 * toFill.width, 2 * toFill.height, true);
		g2d.setColor(FlamingoUtilities.getBorderColor().darker());
		g2d.setClip(clip);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.draw(new Ellipse2D.Double(0, 0, toFill.width, toFill.height));
		g2d.dispose();
	}
}
