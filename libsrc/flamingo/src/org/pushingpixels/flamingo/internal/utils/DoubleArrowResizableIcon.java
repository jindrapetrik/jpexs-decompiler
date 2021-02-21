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

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * Helper implementation of {@link ResizableIcon} that draws a double arrow.
 * 
 * @author Kirill Grouchnikov
 */
public class DoubleArrowResizableIcon implements ResizableIcon {
	/**
	 * Initial dimension.
	 */
	private Dimension initialDim;

	/**
	 * The width of the rendered image.
	 */
	protected int width;

	/**
	 * The height of the rendered image.
	 */
	protected int height;

	/**
	 * Arrow direction. One of {@link SwingConstants#SOUTH},
	 * {@link SwingConstants#NORTH}, {@link SwingConstants#EAST} or
	 * {@link SwingConstants#WEST}.
	 */
	protected int direction;

	/**
	 * Creates a new double arrow resizable icon.
	 * 
	 * @param initialDim
	 *            Initial icon dimension.
	 * @param direction
	 *            Arrow direction. Currently only {@link SwingConstants#SOUTH}
	 *            is supported.
	 */
	public DoubleArrowResizableIcon(Dimension initialDim, int direction) {
		this.initialDim = initialDim;
		this.width = initialDim.width;
		this.height = initialDim.height;
		this.direction = direction;
	}

	/**
	 * Creates a new double arrow resizable icon.
	 * 
	 * @param initialDim
	 *            Initial icon dimension.
	 * @param direction
	 *            Arrow direction. Currently only {@link SwingConstants#SOUTH}
	 *            is supported.
	 */
	public DoubleArrowResizableIcon(int initialDim, int direction) {
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
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Color arrowColor = this.toPaintEnabled(c) ? Color.black : Color.gray;
		graphics.setColor(arrowColor);
		Stroke stroke = new BasicStroke(this.width / 8.0f,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

		graphics.setStroke(stroke);
		graphics.translate(x, y);
		GeneralPath gp = new GeneralPath();
		int arrowHeight = height / 2;
		int arrowWidth = width / 2;
		int deltaH = (height + 2) / 3;
		int deltaW = (width + 2) / 3;
		switch (direction) {
		case SwingUtilities.NORTH:
			gp.moveTo(0, height - 1);
			gp.lineTo((float) 0.5 * (width - 1), height - 1 - arrowHeight);
			gp.lineTo(width - 1, height - 1);

			gp.moveTo(0, height - 1 - deltaH);
			gp.lineTo((float) 0.5 * (width - 1), height - 1 - arrowHeight
					- deltaH);
			gp.lineTo(width - 1, height - 1 - deltaH);

			break;

		case SwingUtilities.SOUTH:
			gp.moveTo(0, 0);
			gp.lineTo((float) 0.5 * (width - 1), arrowHeight);
			gp.lineTo(width - 1, 0);

			gp.moveTo(0, deltaH);
			gp.lineTo((float) 0.5 * (width - 1), arrowHeight + deltaH);
			gp.lineTo(width - 1, deltaH);

			break;

		case SwingUtilities.EAST:
			gp.moveTo(0, 0);
			gp.lineTo(arrowWidth, (float) 0.5 * (height - 1));
			gp.lineTo(0, height - 1);

			gp.moveTo(deltaW, 0);
			gp.lineTo(arrowWidth + deltaW, (float) 0.5 * (height - 1));
			gp.lineTo(deltaW, height - 1);

			break;

		case SwingUtilities.WEST:
			gp.moveTo(width - 1, 0);
			gp.lineTo(width - 1 - arrowWidth, (float) 0.5 * (height - 1));
			gp.lineTo(width - 1, height - 1);

			gp.moveTo(width - 1 - deltaW, 0);
			gp.lineTo(width - 1 - arrowWidth - deltaW, (float) 0.5
					* (height - 1));
			gp.lineTo(width - 1 - deltaW, height - 1);

			break;

		}
		graphics.draw(gp);
		graphics.dispose();
	}
}
