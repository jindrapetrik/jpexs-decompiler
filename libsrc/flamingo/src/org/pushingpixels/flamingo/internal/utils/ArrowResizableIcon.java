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
import java.awt.geom.GeneralPath;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * Helper implementation of {@link ResizableIcon} that draws an arrow.
 * 
 * @author Kirill Grouchnikov
 */
public class ArrowResizableIcon implements ResizableIcon {
	/**
	 * Initial dimension.
	 */
	private Dimension initialDim;

	/**
	 * The current icon width.
	 */
	protected int width;

	/**
	 * The current icon height.
	 */
	protected int height;

	/**
	 * Arrow direction. One of {@link SwingConstants#SOUTH},
	 * {@link SwingConstants#NORTH}, {@link SwingConstants#EAST} or
	 * {@link SwingConstants#WEST}.
	 */
	protected int direction;

	/**
	 * Creates a new arrow resizable icon.
	 * 
	 * @param initialDim
	 *            Initial icon dimension.
	 * @param direction
	 *            Arrow direction. Must be one of {@link SwingConstants#SOUTH},
	 *            {@link SwingConstants#NORTH}, {@link SwingConstants#EAST} or
	 *            {@link SwingConstants#WEST}.
	 */
	public ArrowResizableIcon(Dimension initialDim, int direction) {
		this.initialDim = initialDim;
		this.width = initialDim.width;
		this.height = initialDim.height;
		this.direction = direction;
	}

	/**
	 * Creates a new arrow resizable icon.
	 * 
	 * @param initialDim
	 *            Initial icon dimension.
	 * @param direction
	 *            Arrow direction. Must be one of {@link SwingConstants#SOUTH},
	 *            {@link SwingConstants#NORTH}, {@link SwingConstants#EAST} or
	 *            {@link SwingConstants#WEST}.
	 */
	public ArrowResizableIcon(int initialDim, int direction) {
		this(new Dimension(initialDim, initialDim), direction);
	}

	public void revertToOriginalDimension() {
		this.width = initialDim.width;
		this.height = initialDim.height;
	}

	@Override
    public void setDimension(Dimension newDimension) {
		this.width = newDimension.width;
		this.height = newDimension.height;
	}

	@Override
    public int getIconHeight() {
		return this.height;
	}

	@Override
    public int getIconWidth() {
		return this.width;
	}

	protected boolean toPaintEnabled(Component c) {
		return c.isEnabled();
	}

	@Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D graphics = (Graphics2D) g.create();

		float strokeWidth = this.width / 7.0f;
		if (strokeWidth < 1.0f)
			strokeWidth = 1.0f;
		Stroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER);

		graphics.setStroke(stroke);

		GeneralPath gp = new GeneralPath();
		switch (direction) {
		case SwingUtilities.SOUTH:
			gp.moveTo(0, 2);
			gp.lineTo((float) 0.5 * (width - 1), height - 2);
			gp.lineTo(width - 1, 2);
			break;
		case SwingUtilities.NORTH:
			gp.moveTo(0, height - 2);
			gp.lineTo((float) 0.5 * (width - 1), 2);
			gp.lineTo(width - 1, height - 2);
			break;
		case SwingUtilities.EAST:
			gp.moveTo(2, 0);
			gp.lineTo(width - 2, (float) 0.5 * (height - 1));
			gp.lineTo(2, height - 1);
			break;
		case SwingUtilities.WEST:
			gp.moveTo(width - 2, 0);
			gp.lineTo(2, (float) 0.5 * (height - 1));
			gp.lineTo(width - 2, height - 1);
			break;
		}

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.translate(x, y + 1);
		Color dropColor = this.toPaintEnabled(c) ? new Color(255, 255, 255, 196)
				: new Color(255, 255, 255, 32);
		graphics.setColor(dropColor);
		graphics.draw(gp);

		graphics.translate(0, -1);
		Color arrowColor = this.toPaintEnabled(c) ? Color.black : Color.gray;
		graphics.setColor(arrowColor);
		if (this.width < 9) {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.draw(gp);
		}
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.draw(gp);
		graphics.dispose();
	}

	public static class CommandButtonPopupIcon extends ArrowResizableIcon {

		public CommandButtonPopupIcon(int initialDim, int direction) {
			super(initialDim, direction);
		}

		@Override
		protected boolean toPaintEnabled(Component c) {
			JCommandButton jcb = (JCommandButton) c;
			return jcb.isEnabled() && jcb.getPopupModel().isEnabled();
		}
	}
}
