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
package org.pushingpixels.flamingo.internal.utils;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;

import javax.swing.*;

import org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonPopupOrientationKind;

public class KeyTipRenderingUtilities {
	private static int INSETS = 3;

	public static Dimension getPrefSize(FontMetrics fm, String keyTip) {
		int prefWidth = fm.stringWidth(keyTip) + 2 * INSETS + 1;
		int prefHeight = fm.getHeight() + INSETS - 1;
		return new Dimension(prefWidth, prefHeight);
	}

	public static void renderKeyTip(Graphics g, Container c, Rectangle rect,
			String keyTip, boolean toPaintEnabled) {
		CellRendererPane buttonRendererPane = new CellRendererPane();
		JButton rendererButton = new JButton("");
		rendererButton.setEnabled(toPaintEnabled);

		buttonRendererPane.setBounds(rect.x, rect.y, rect.width, rect.height);
		Graphics2D g2d = (Graphics2D) g.create();

		g2d.setComposite(AlphaComposite.SrcOver.derive(toPaintEnabled ? 1.0f
				: 0.5f));

		Shape clip = g2d.getClip();
		RoundRectangle2D.Double roundRect = new RoundRectangle2D.Double(rect.x,
				rect.y, rect.width - 1, rect.height - 1, 6, 6);
		g2d.clip(roundRect);
		buttonRendererPane.paintComponent(g2d, rendererButton, c, rect.x
				- rect.width / 2, rect.y - rect.height / 2, 2 * rect.width,
				2 * rect.height, true);
		g2d.setClip(clip);

		g2d.setColor(FlamingoUtilities.getBorderColor());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.draw(roundRect);

		g2d.setColor(FlamingoUtilities.getColor(Color.black,
				"Button.foreground"));
		Font font = UIManager.getFont("Button.font");
		font = font.deriveFont(font.getSize() + 1.0f);
		g2d.setFont(font);
		int strWidth = g2d.getFontMetrics().stringWidth(keyTip);
		//int strHeight = g2d.getFontMetrics().getHeight();

		g2d.translate(rect.x, rect.y);
		LineMetrics lineMetrics = g2d.getFontMetrics().getLineMetrics(keyTip, g2d);
		int strHeight = (int)lineMetrics.getHeight();
		g2d.drawString(keyTip, (rect.width - strWidth + 1) / 2,
				(rect.height + strHeight) / 2
						- g2d.getFontMetrics().getDescent() + 1);

		g2d.dispose();
	}

	public static void renderMenuButtonKeyTips(Graphics g,
			JCommandMenuButton menuButton,
			CommandButtonLayoutManager layoutManager) {
		Collection<KeyTipManager.KeyTipLink> currLinks = KeyTipManager
				.defaultManager().getCurrentlyShownKeyTips();
		if (currLinks == null)
			return;

		boolean found = false;
		for (KeyTipManager.KeyTipLink link : currLinks) {
			found = (link.comp == menuButton);
			if (found)
				break;
		}

		if (!found)
			return;

		// System.out.println("Painting key tip for " + menuButton.getText());

		String actionKeyTip = menuButton.getActionKeyTip();
		String popupKeyTip = menuButton.getPopupKeyTip();
		CommandButtonLayoutManager.CommandButtonLayoutInfo layoutInfo = layoutManager
				.getLayoutInfo(menuButton, g);
		Point prefCenter = menuButton.getUI().getKeyTipAnchorCenterPoint();
		if ((layoutInfo.iconRect.width > 0) && (actionKeyTip != null)) {
			Dimension pref = KeyTipRenderingUtilities.getPrefSize(g
					.getFontMetrics(), actionKeyTip);
			KeyTipRenderingUtilities.renderKeyTip(g, menuButton, new Rectangle(
					prefCenter.x - pref.width / 2, Math.min(prefCenter.y
							- pref.height / 2, layoutInfo.actionClickArea.y
							+ layoutInfo.actionClickArea.height - pref.height),
					pref.width, pref.height), actionKeyTip, menuButton
					.getActionModel().isEnabled());
		}
		if ((layoutInfo.popupClickArea.width > 0) && (popupKeyTip != null)) {
			Dimension pref = KeyTipRenderingUtilities.getPrefSize(g
					.getFontMetrics(), popupKeyTip);
			if (menuButton.getPopupOrientationKind() == CommandButtonPopupOrientationKind.SIDEWARD) {
				if (menuButton.getCommandButtonKind() != CommandButtonKind.POPUP_ONLY) {
					// vertically aligned with the action keytip along
					// the right edge
					KeyTipRenderingUtilities.renderKeyTip(g, menuButton,
							new Rectangle(layoutInfo.popupClickArea.x
									+ layoutInfo.popupClickArea.width
									- pref.width - 4, Math.min(prefCenter.y
									- pref.height / 2,
									layoutInfo.actionClickArea.y
											+ layoutInfo.actionClickArea.height
											- pref.height), pref.width,
									pref.height), popupKeyTip, menuButton
									.getPopupModel().isEnabled());
				} else {
					KeyTipRenderingUtilities
							.renderKeyTip(
									g,
									menuButton,
									new Rectangle(
											prefCenter.x - pref.width / 2,
											Math
													.min(
															prefCenter.y
																	- pref.height
																	/ 2,
															layoutInfo.popupClickArea.y
																	+ layoutInfo.popupClickArea.height
																	- pref.height),
											pref.width, pref.height),
									popupKeyTip, menuButton.getPopupModel()
											.isEnabled());
				}
			} else {
				// horizontally centered along the bottom edge
				KeyTipRenderingUtilities
						.renderKeyTip(
								g,
								menuButton,
								new Rectangle(
										(layoutInfo.popupClickArea.x
												+ layoutInfo.popupClickArea.width - pref.width) / 2,
										layoutInfo.popupClickArea.y
												+ layoutInfo.popupClickArea.height
												- pref.height, pref.width,
										pref.height), popupKeyTip, menuButton
										.getPopupModel().isEnabled());
			}
		}
	}
}
