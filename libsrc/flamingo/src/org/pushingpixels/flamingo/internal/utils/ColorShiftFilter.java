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

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Image filter that shifts the colors of the original image.
 * 
 * @author Kirill Grouchnikov
 */
public class ColorShiftFilter extends AbstractFilter {
	/**
	 * Red component of the shift color.
	 */
	int rShift;

	/**
	 * Green component of the shift color.
	 */
	int gShift;

	/**
	 * Blue component of the shift color.
	 */
	int bShift;

	/**
	 * Shift amount in 0.0-1.0 range.
	 */
	double hueShiftAmount;

	/**
	 * Creates a new color shift filter.
	 * 
	 * @param shiftColor
	 *            Shift color.
	 * @param shiftAmount
	 *            Shift amount in 0.0-1.0 range.
	 */
	public ColorShiftFilter(Color shiftColor, double shiftAmount) {
		this.rShift = shiftColor.getRed();
		this.gShift = shiftColor.getGreen();
		this.bShift = shiftColor.getBlue();
		this.hueShiftAmount = shiftAmount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.image.BufferedImageOp#filter(java.awt.image.BufferedImage,
	 * java.awt.image.BufferedImage)
	 */
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, null);
		}

		int width = src.getWidth();
		int height = src.getHeight();

		int[] pixels = new int[width * height];
		getPixels(src, 0, 0, width, height, pixels);
		shiftColor(pixels);
		setPixels(dst, 0, 0, width, height, pixels);

		return dst;
	}

	/**
	 * Color-shifts all the pixels in the specified pixel array.
	 * 
	 * @param pixels
	 *            Pixel array for color-shifting.
	 */
	private void shiftColor(int[] pixels) {
		for (int i = 0; i < pixels.length; i++) {
			int argb = pixels[i];
			int r = (argb >>> 16) & 0xFF;
			int g = (argb >>> 8) & 0xFF;
			int b = (argb >>> 0) & 0xFF;

			int nr = (int) (this.hueShiftAmount * this.rShift + (1.0 - this.hueShiftAmount)
					* r);
			int ng = (int) (this.hueShiftAmount * this.gShift + (1.0 - this.hueShiftAmount)
					* g);
			int nb = (int) (this.hueShiftAmount * this.bShift + (1.0 - this.hueShiftAmount)
					* b);

			pixels[i] = (argb & 0xFF000000) | nr << 16 | ng << 8 | nb;
		}
	}
}
