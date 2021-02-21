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
package org.pushingpixels.flamingo.api.common.icon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pushingpixels.flamingo.api.common.AsynchronousLoading;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 * Implementation of {@link ResizableIcon} that allows applying a
 * {@link BufferedImageOp} on another icon.
 * 
 * @author Kirill Grouchnikov
 */
public class FilteredResizableIcon implements ResizableIcon {
	/**
	 * Image cache to speed up rendering.
	 */
	protected Map<String, BufferedImage> cachedImages;

	/**
	 * The main (pre-filtered) icon.
	 */
	protected ResizableIcon delegate;

	/**
	 * Filter operation.
	 */
	protected BufferedImageOp operation;

	/**
	 * Creates a new filtered icon.
	 * 
	 * @param delegate
	 *            The main (pre-filtered) icon.
	 * @param operation
	 *            Filter operation.
	 */
	public FilteredResizableIcon(ResizableIcon delegate,
			BufferedImageOp operation) {
		super();
		this.delegate = delegate;
		this.operation = operation;
		this.cachedImages = new LinkedHashMap<String, BufferedImage>() {
			@Override
			protected boolean removeEldestEntry(
					java.util.Map.Entry<String, BufferedImage> eldest) {
				return size() > 5;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
    public int getIconHeight() {
		return delegate.getIconHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
    public int getIconWidth() {
		return delegate.getIconWidth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.icon.ResizableIcon#setDimension(java.awt.Dimension
	 * )
	 */
	@Override
    public void setDimension(Dimension newDimension) {
		delegate.setDimension(newDimension);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// check cache
		String key = this.getIconWidth() + ":" + this.getIconHeight();
		if (!this.cachedImages.containsKey(key)) {
			// check if loading
			if (this.delegate instanceof AsynchronousLoading) {
				AsynchronousLoading asyncDelegate = (AsynchronousLoading) this.delegate;
				// if the delegate is still loading - do nothing
				if (asyncDelegate.isLoading())
					return;
			}
			BufferedImage offscreen = FlamingoUtilities.getBlankImage(this
					.getIconWidth(), this.getIconHeight());
			Graphics2D g2d = offscreen.createGraphics();
			this.delegate.paintIcon(c, g2d, 0, 0);
			g2d.dispose();
			BufferedImage filtered = this.operation.filter(offscreen, null);
			this.cachedImages.put(key, filtered);
		}
		g.drawImage(this.cachedImages.get(key), x, y, null);
	}
}
