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
import java.awt.geom.GeneralPath;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 * Basic UI delegate for the {@link JCommandToggleMenuButton} component.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicCommandToggleMenuButtonUI extends BasicCommandToggleButtonUI {
	public static ComponentUI createUI(JComponent c) {
		return new BasicCommandToggleMenuButtonUI();
	}

	@Override
	protected void paintButtonIcon(Graphics g, Rectangle iconRect) {
		boolean isSelected = this.commandButton.getActionModel().isSelected();
		if (isSelected) {
			Color selectionColor = FlamingoUtilities.getColor(Color.blue
					.darker(), "Table.selectionBackground", "textHighlight");
			Rectangle extended = new Rectangle(iconRect.x - 1, iconRect.y - 1,
					iconRect.width + 1, iconRect.height + 1);
			g.setColor(selectionColor);
			g.fillRect(extended.x, extended.y, extended.width, extended.height);
			g.setColor(selectionColor.darker());
			g.drawRect(extended.x, extended.y, extended.width, extended.height);
		}
		super.paintButtonIcon(g, iconRect);
		// does it actually have an icon?
		Icon iconToPaint = this.getIconToPaint();
		if (isSelected && (iconToPaint == null)) {
			// draw a checkmark
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(getForegroundColor(this.commandButton.getActionModel()
					.isEnabled()));

			int iw = iconRect.width;
			int ih = iconRect.height;
			GeneralPath path = new GeneralPath();

			path.moveTo(0.2f * iw, 0.5f * ih);
			path.lineTo(0.42f * iw, 0.8f * ih);
			path.lineTo(0.8f * iw, 0.2f * ih);
			g2d.translate(iconRect.x, iconRect.y);
			Stroke stroke = new BasicStroke((float) 0.1 * iw,
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g2d.setStroke(stroke);
			g2d.draw(path);

			g2d.dispose();
		}
	}

	@Override
	protected boolean isPaintingBackground() {
		boolean isActionRollover = this.commandButton.getActionModel()
				.isRollover();

		return (isActionRollover || !this.commandButton.isFlat());
	}
}
