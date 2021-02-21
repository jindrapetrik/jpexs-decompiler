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
package org.pushingpixels.flamingo.internal.ui.common;

import java.awt.*;
import java.beans.PropertyChangeEvent;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

public class CommandButtonLayoutManagerSmall implements
		CommandButtonLayoutManager {

	@Override
	public int getPreferredIconSize() {
		return 16;
	}

	@Override
	public Dimension getPreferredSize(AbstractCommandButton commandButton) {
		Insets borderInsets = commandButton.getInsets();
		// int bx = borderInsets.left + borderInsets.right;
		int by = borderInsets.top + borderInsets.bottom;
		FontMetrics fm = commandButton.getFontMetrics(commandButton.getFont());

		int layoutHGap = FlamingoUtilities.getHLayoutGap(commandButton);

		boolean hasIcon = (commandButton.getIcon() != null);
		boolean hasPopupIcon = FlamingoUtilities.hasPopupAction(commandButton);

		int prefIconSize = hasIcon ? this.getPreferredIconSize() : 0;

		// start with the left insets
		int width = borderInsets.left;
		// icon?
		if (hasIcon) {
			// padding before the icon
			width += layoutHGap;
			// icon width
			width += prefIconSize;
			// padding after the icon
			width += layoutHGap;
		}
		// popup icon?
		if (hasPopupIcon) {
			// padding before the popup icon
			width += 2 * layoutHGap;
			// text width
			width += 1 + fm.getHeight() / 2;
			// padding after the popup icon
			width += 2 * layoutHGap;
		}

		if (commandButton instanceof JCommandButton) {
			JCommandButton jcb = (JCommandButton) commandButton;
			CommandButtonKind buttonKind = jcb.getCommandButtonKind();
			if (hasIcon && buttonKind.hasAction() && buttonKind.hasPopup()) {
				// space for a vertical separator
				width += new JSeparator(JSeparator.VERTICAL).getPreferredSize().width;
			}
		}

		// right insets
		width += borderInsets.right;

		// and remove the padding before the first and after the last elements
		width -= 2 * layoutHGap;

		return new Dimension(width, by
				+ Math.max(prefIconSize, fm.getAscent() + fm.getDescent()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public Point getKeyTipAnchorCenterPoint(AbstractCommandButton commandButton) {
		Insets ins = commandButton.getInsets();
		int height = commandButton.getHeight();
		ResizableIcon buttonIcon = commandButton.getIcon();
		if (buttonIcon != null) {
			// bottom-right corner of the icon area
			return new Point(ins.left + buttonIcon.getIconWidth(),
					(height + buttonIcon.getIconHeight()) / 2);
		} else {
			// bottom-left corner of the button
			return new Point(ins.left, 3 * height / 4);
		}
	}

	@Override
	public CommandButtonLayoutInfo getLayoutInfo(
			AbstractCommandButton commandButton, Graphics g) {
		CommandButtonLayoutInfo result = new CommandButtonLayoutInfo();

		result.actionClickArea = new Rectangle(0, 0, 0, 0);
		result.popupClickArea = new Rectangle(0, 0, 0, 0);

		Insets ins = commandButton.getInsets();

		result.iconRect = new Rectangle();
		result.popupActionRect = new Rectangle();

		int width = commandButton.getWidth();
		int height = commandButton.getHeight();

		int prefWidth = this.getPreferredSize(commandButton).width;
		int shiftX = 0;
		if (commandButton.getHorizontalAlignment() == SwingConstants.CENTER) {
			if (width > prefWidth) {
				shiftX = (width - prefWidth) / 2;
			}
		}

		ResizableIcon buttonIcon = commandButton.getIcon();

		boolean hasIcon = (buttonIcon != null);
		boolean hasPopupIcon = FlamingoUtilities.hasPopupAction(commandButton);

		boolean ltr = commandButton.getComponentOrientation().isLeftToRight();

		FontMetrics fm = g.getFontMetrics();
		int labelHeight = fm.getAscent() + fm.getDescent();

		JCommandButton.CommandButtonKind buttonKind = (commandButton instanceof JCommandButton) ? ((JCommandButton) commandButton)
				.getCommandButtonKind()
				: JCommandButton.CommandButtonKind.ACTION_ONLY;
		int layoutHGap = FlamingoUtilities.getHLayoutGap(commandButton);

		if (ltr) {
			int x = ins.left + shiftX - layoutHGap;

			// icon
			if (hasIcon) {
				x += layoutHGap;

				int iconHeight = buttonIcon.getIconHeight();
				int iconWidth = buttonIcon.getIconWidth();

				result.iconRect.x = x;
				result.iconRect.y = (height - iconHeight) / 2;
				result.iconRect.width = iconWidth;
				result.iconRect.height = iconHeight;

				x += (iconWidth + layoutHGap);
			}

			if (hasPopupIcon) {
				x += 2 * layoutHGap;

				result.popupActionRect.x = x;
				result.popupActionRect.y = (height - labelHeight) / 2 - 1;
				result.popupActionRect.width = 1 + labelHeight / 2;
				result.popupActionRect.height = labelHeight + 2;
				x += result.popupActionRect.width;

				x += 2 * layoutHGap;
			}

			int xBorderBetweenActionAndPopup = 0;
			int verticalSeparatorWidth = new JSeparator(JSeparator.VERTICAL)
					.getPreferredSize().width;
			// compute the action and popup click areas
			switch (buttonKind) {
			case ACTION_ONLY:
				result.actionClickArea.x = 0;
				result.actionClickArea.y = 0;
				result.actionClickArea.width = width;
				result.actionClickArea.height = height;
				result.isTextInActionArea = true;
				break;
			case POPUP_ONLY:
				result.popupClickArea.x = 0;
				result.popupClickArea.y = 0;
				result.popupClickArea.width = width;
				result.popupClickArea.height = height;
				result.isTextInActionArea = false;
				break;
			case ACTION_AND_POPUP_MAIN_ACTION:
			case ACTION_AND_POPUP_MAIN_POPUP:
				// 1. break after icon if button has icon
				// 2. no break (all popup) if button has no icon
				if (hasIcon) {
					// shift popup action rectangle to the right
					// to accomodate the vertical separator
					result.popupActionRect.x += verticalSeparatorWidth;

					xBorderBetweenActionAndPopup = result.iconRect.x
							+ result.iconRect.width + layoutHGap;

					result.actionClickArea.x = 0;
					result.actionClickArea.y = 0;
					result.actionClickArea.width = xBorderBetweenActionAndPopup;
					result.actionClickArea.height = height;

					result.popupClickArea.x = xBorderBetweenActionAndPopup;
					result.popupClickArea.y = 0;
					result.popupClickArea.width = width
							- xBorderBetweenActionAndPopup;
					result.popupClickArea.height = height;

					result.separatorOrientation = CommandButtonSeparatorOrientation.VERTICAL;
					result.separatorArea = new Rectangle();
					result.separatorArea.x = xBorderBetweenActionAndPopup;
					result.separatorArea.y = 0;
					result.separatorArea.width = verticalSeparatorWidth;
					result.separatorArea.height = height;

					result.isTextInActionArea = true;
				} else {
					result.popupClickArea.x = 0;
					result.popupClickArea.y = 0;
					result.popupClickArea.width = width;
					result.popupClickArea.height = height;

					result.isTextInActionArea = true;
				}
				break;
			}
		} else {
			int x = width - ins.right - shiftX + layoutHGap;

			// icon
			if (hasIcon) {
				x -= layoutHGap;

				int iconHeight = buttonIcon.getIconHeight();
				int iconWidth = buttonIcon.getIconWidth();

				result.iconRect.x = x - iconWidth;
				result.iconRect.y = (height - iconHeight) / 2;
				result.iconRect.width = iconWidth;
				result.iconRect.height = iconHeight;

				x -= (iconWidth + layoutHGap);
			}

			if (hasPopupIcon) {
				x -= 2 * layoutHGap;

				result.popupActionRect.width = 1 + labelHeight / 2;
				result.popupActionRect.x = x - result.popupActionRect.width;
				result.popupActionRect.y = (height - labelHeight) / 2 - 1;
				result.popupActionRect.height = labelHeight + 2;
				x -= result.popupActionRect.width;

				x -= 2 * layoutHGap;
			}

			int xBorderBetweenActionAndPopup = 0;
			int verticalSeparatorWidth = new JSeparator(JSeparator.VERTICAL)
					.getPreferredSize().width;
			// compute the action and popup click areas
			switch (buttonKind) {
			case ACTION_ONLY:
				result.actionClickArea.x = 0;
				result.actionClickArea.y = 0;
				result.actionClickArea.width = width;
				result.actionClickArea.height = height;
				result.isTextInActionArea = true;
				break;
			case POPUP_ONLY:
				result.popupClickArea.x = 0;
				result.popupClickArea.y = 0;
				result.popupClickArea.width = width;
				result.popupClickArea.height = height;
				result.isTextInActionArea = false;
				break;
			case ACTION_AND_POPUP_MAIN_ACTION:
			case ACTION_AND_POPUP_MAIN_POPUP:
				// 1. break after icon if button has icon
				// 2. no break (all popup) if button has no icon
				if (hasIcon) {
					// shift popup action rectangle to the left
					// to accomodate the vertical separator
					result.popupActionRect.x -= verticalSeparatorWidth;

					xBorderBetweenActionAndPopup = result.iconRect.x
							- layoutHGap;

					result.actionClickArea.x = xBorderBetweenActionAndPopup;
					result.actionClickArea.y = 0;
					result.actionClickArea.width = width
							- xBorderBetweenActionAndPopup;
					result.actionClickArea.height = height;

					result.popupClickArea.x = 0;
					result.popupClickArea.y = 0;
					result.popupClickArea.width = xBorderBetweenActionAndPopup;
					result.popupClickArea.height = height;

					result.separatorOrientation = CommandButtonSeparatorOrientation.VERTICAL;
					result.separatorArea = new Rectangle();
					result.separatorArea.x = xBorderBetweenActionAndPopup;
					result.separatorArea.y = 0;
					result.separatorArea.width = verticalSeparatorWidth;
					result.separatorArea.height = height;

					result.isTextInActionArea = true;
				} else {
					result.popupClickArea.x = 0;
					result.popupClickArea.y = 0;
					result.popupClickArea.width = width;
					result.popupClickArea.height = height;

					result.isTextInActionArea = true;
				}
				break;
			}
		}

		return result;
	}
}
