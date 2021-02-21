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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.JSeparator;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

public class CommandButtonLayoutManagerBigFixedLandscape implements
		CommandButtonLayoutManager {

	@Override
	public int getPreferredIconSize() {
		return 32;
	}

	@Override
	public Dimension getPreferredSize(AbstractCommandButton commandButton) {
		Insets borderInsets = commandButton.getInsets();
		int bx = borderInsets.left + borderInsets.right;
		int by = borderInsets.top + borderInsets.bottom;
		FontMetrics fm = commandButton.getFontMetrics(commandButton.getFont());
		JSeparator jsep = new JSeparator(JSeparator.VERTICAL);
		int layoutVGap = FlamingoUtilities.getVLayoutGap(commandButton);

		// icon, label
		int fillTitleWidth = fm.stringWidth(commandButton.getText());

		int widthFull = Math.max(this.getPreferredIconSize(), fillTitleWidth);

		int heightFull = by + this.getPreferredIconSize() + layoutVGap
				+ jsep.getPreferredSize().width;
		if (commandButton.getText() != null) {
			heightFull += fm.getHeight();
		}

		widthFull = Math.max(widthFull, heightFull * 5 / 4);
		return new Dimension(bx + widthFull, heightFull);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public Point getKeyTipAnchorCenterPoint(AbstractCommandButton commandButton) {
		// center of the bottom edge
		return new Point(commandButton.getWidth() / 2, commandButton
				.getHeight());
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

		int x = ins.left;
		int y = ins.top;

		FontMetrics fm = g.getFontMetrics();
		int labelHeight = fm.getAscent() + fm.getDescent();

		JCommandButton.CommandButtonKind buttonKind = (commandButton instanceof JCommandButton) ? ((JCommandButton) commandButton)
				.getCommandButtonKind()
				: JCommandButton.CommandButtonKind.ACTION_ONLY;

		result.isTextInActionArea = false;
		if (buttonKind == JCommandButton.CommandButtonKind.ACTION_ONLY) {
			result.actionClickArea.x = 0;
			result.actionClickArea.y = 0;
			result.actionClickArea.width = width;
			result.actionClickArea.height = height;
			result.isTextInActionArea = true;
		}
		if (buttonKind == JCommandButton.CommandButtonKind.POPUP_ONLY) {
			result.popupClickArea.x = 0;
			result.popupClickArea.y = 0;
			result.popupClickArea.width = width;
			result.popupClickArea.height = height;
		}

		JSeparator jsep = new JSeparator(JSeparator.VERTICAL);
		// int layoutGap = FlamingoUtilities.getLayoutGap(commandButton);

		ResizableIcon buttonIcon = commandButton.getIcon();

		if (commandButton.getText() == null) {
			y = ins.top
					+ (height - ins.top - ins.bottom - buttonIcon
							.getIconHeight()) / 2;
		}
		result.iconRect.x = (width - buttonIcon.getIconWidth()) / 2;
		result.iconRect.y = y;
		result.iconRect.width = buttonIcon.getIconWidth();
		result.iconRect.height = buttonIcon.getIconHeight();
		y += buttonIcon.getIconHeight();

		y += jsep.getPreferredSize().width;

		TextLayoutInfo lineLayoutInfo = new TextLayoutInfo();
		lineLayoutInfo.text = commandButton.getText();
		lineLayoutInfo.textRect = new Rectangle();

		int labelWidth = (int) fm.getStringBounds(commandButton.getText(), g)
				.getWidth();

		lineLayoutInfo.textRect.x = ins.left
				+ (width - labelWidth - ins.left - ins.right) / 2;
		lineLayoutInfo.textRect.y = y;
		lineLayoutInfo.textRect.width = labelWidth;
		lineLayoutInfo.textRect.height = labelHeight;

		result.textLayoutInfoList = new ArrayList<TextLayoutInfo>();
		result.textLayoutInfoList.add(lineLayoutInfo);

		return result;
	}

}
