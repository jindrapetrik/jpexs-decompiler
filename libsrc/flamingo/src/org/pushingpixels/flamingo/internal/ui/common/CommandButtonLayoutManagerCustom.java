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

import javax.swing.JSeparator;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

public class CommandButtonLayoutManagerCustom extends
		CommandButtonLayoutManagerBig {

	public CommandButtonLayoutManagerCustom(AbstractCommandButton commandButton) {
		super(commandButton);
	}

	@Override
	public int getPreferredIconSize() {
		return -1;
	}

	@Override
	public Dimension getPreferredSize(AbstractCommandButton commandButton) {
		Insets borderInsets = commandButton.getInsets();
		int bx = borderInsets.left + borderInsets.right;
		int by = borderInsets.top + borderInsets.bottom;
		FontMetrics fm = commandButton.getFontMetrics(commandButton.getFont());
		JSeparator jsep = new JSeparator(JSeparator.HORIZONTAL);
		int layoutHGap = FlamingoUtilities.getHLayoutGap(commandButton);
		int layoutVGap = FlamingoUtilities.getVLayoutGap(commandButton);

		int title1Width = (this.titlePart1 == null) ? 0 : fm
				.stringWidth(this.titlePart1);
		int title2Width = (this.titlePart2 == null) ? 0 : fm
				.stringWidth(this.titlePart2);

		ResizableIcon icon = commandButton.getIcon();
		int iconWidth = (icon == null) ? 0 : icon.getIconWidth();
		int width = Math.max(iconWidth, Math.max(title1Width, title2Width
				+ 4
				* layoutHGap
				+ jsep.getPreferredSize().width
				+ (FlamingoUtilities.hasPopupAction(commandButton) ? 1 + fm
						.getHeight() / 2 : 0)));

		boolean hasIcon = (commandButton.getIcon() != null);
		boolean hasText = (this.titlePart1 != null);
		boolean hasPopupIcon = FlamingoUtilities.hasPopupAction(commandButton);

		// start height with the top inset
		int height = borderInsets.top;
		// icon?
		if (hasIcon) {
			// padding above the icon
			height += layoutVGap;
			// icon height
			height += icon.getIconHeight();
			// padding below the icon
			height += layoutVGap;
		}
		// text?
		if (hasText) {
			// padding above the text
			height += layoutVGap;
			// text height - two lines
			height += 2 * (fm.getAscent() + fm.getDescent());
			// padding below the text
			height += layoutVGap;
		}
		// popup icon (no text)?
		if (!hasText && hasPopupIcon) {
			// padding above the popup icon
			height += layoutVGap;
			// popup icon height - one line of text
			height += fm.getHeight();
			// padding below the popup icon
			height += layoutVGap;
		}

		if (hasPopupIcon) {
			// space for a horizontal separator
			height += new JSeparator(JSeparator.HORIZONTAL).getPreferredSize().height;
		}

		// bottom insets
		height += borderInsets.bottom;

		// and remove the padding above the first and below the last elements
		height -= 2 * layoutVGap;

		return new Dimension(bx + width, height);
	}
}
