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
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

import org.pushingpixels.flamingo.api.common.AsynchronousLoadListener;
import org.pushingpixels.flamingo.api.common.AsynchronousLoading;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 * Helper class to load images and expose them as icons of dynamic size.
 * 
 * @author Kirill Grouchnikov
 */
abstract class ImageWrapperIcon implements Icon, AsynchronousLoading {
	/**
	 * The original image.
	 */
	protected BufferedImage originalImage;

	/**
	 * The input stream of the original image.
	 */
	protected InputStream imageInputStream;

	/**
	 * The input stream of the original image.
	 */
	protected Image image;

	/**
	 * Contains all precomputed images.
	 */
	protected Map<String, BufferedImage> cachedImages;

	/**
	 * The width of the current image.
	 */
	protected int width;

	/**
	 * The height of the current image.
	 */
	protected int height;

	/**
	 * The listeners.
	 */
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * Create a new image-wrapper icon.
	 * 
	 * @param inputStream
	 *            The input stream to read the image from.
	 * @param w
	 *            The width of the icon.
	 * @param h
	 *            The height of the icon.
	 */
	public ImageWrapperIcon(InputStream inputStream, int w, int h) {
		this.imageInputStream = inputStream;
		this.width = w;
		this.height = h;
		this.listenerList = new EventListenerList();
		this.cachedImages = new LinkedHashMap<String, BufferedImage>() {
			@Override
			protected boolean removeEldestEntry(
					Map.Entry<String, BufferedImage> eldest) {
				return size() > 5;
			};
		};
		this.renderImage(this.width, this.height);
	}

	/**
	 * Create a new image-wrapper icon.
	 * 
	 * @param image
	 *            The original image.
	 * @param w
	 *            The width of the icon.
	 * @param h
	 *            The height of the icon.
	 */
	public ImageWrapperIcon(Image image, int w, int h) {
		this.imageInputStream = null;
		this.image = image;
		this.width = w;
		this.height = h;
		this.listenerList = new EventListenerList();
		this.cachedImages = new LinkedHashMap<String, BufferedImage>() {
			@Override
			protected boolean removeEldestEntry(
					Map.Entry<String, BufferedImage> eldest) {
				return size() > 5;
			};
		};
		this.renderImage(this.width, this.height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.AsynchronousLoading#addAsynchronousLoadListener
	 * (org.jvnet.flamingo.common.AsynchronousLoadListener)
	 */
	@Override
    public void addAsynchronousLoadListener(AsynchronousLoadListener l) {
		this.listenerList.add(AsynchronousLoadListener.class, l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.AsynchronousLoading#removeAsynchronousLoadListener
	 * (org.jvnet.flamingo.common.AsynchronousLoadListener)
	 */
	@Override
    public void removeAsynchronousLoadListener(AsynchronousLoadListener l) {
		this.listenerList.remove(AsynchronousLoadListener.class, l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
    public int getIconWidth() {
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
    public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
	 */
	@Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
		BufferedImage image = this.cachedImages.get(this.getIconWidth() + ":"
				+ this.getIconHeight());
		if (image != null) {
			int dx = (this.width - image.getWidth()) / 2;
			int dy = (this.height - image.getHeight()) / 2;
			g.drawImage(image, x + dx, y + dy, null);
		}
	}

	/**
	 * Sets the preferred size for <code>this</code> icon. The rendering is
	 * scheduled automatically.
	 * 
	 * @param dim
	 *            Preferred size.
	 */
	public synchronized void setPreferredSize(Dimension dim) {
		if ((dim.width == this.width) && (dim.height == this.height))
			return;
		this.width = dim.width;
		this.height = dim.height;

		this.renderImage(this.width, this.height);
	}

	/**
	 * Renders the image.
	 * 
	 * @param renderWidth
	 *            Requested rendering width.
	 * @param renderHeight
	 *            Requested rendering height.
	 */
	protected synchronized void renderImage(final int renderWidth,
			final int renderHeight) {
		String key = renderWidth + ":" + renderHeight;
		if (this.cachedImages.containsKey(key)) {
			fireAsyncCompleted(true);
			return;
		}

		SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {
			@Override
			protected BufferedImage doInBackground() throws Exception {
				if (imageInputStream != null) {
					synchronized (imageInputStream) {
						if (originalImage == null) {
							// read original image
							originalImage = ImageIO.read(imageInputStream);
						}
					}
				} else {
					GraphicsEnvironment e = GraphicsEnvironment
							.getLocalGraphicsEnvironment();
					GraphicsDevice d = e.getDefaultScreenDevice();
					GraphicsConfiguration c = d.getDefaultConfiguration();
					originalImage = c.createCompatibleImage(image
							.getWidth(null), image.getHeight(null),
							Transparency.TRANSLUCENT);
					Graphics g = originalImage.getGraphics();
					g.drawImage(image, 0, 0, null);
					g.dispose();
				}

				BufferedImage result = originalImage;
				float scaleX = (float) originalImage.getWidth()
						/ (float) renderWidth;
				float scaleY = (float) originalImage.getHeight()
						/ (float) height;

				float scale = Math.max(scaleX, scaleY);
				if (scale > 1.0f) {
					int finalWidth = (int) (originalImage.getWidth() / scale);
					result = FlamingoUtilities.createThumbnail(originalImage,
							finalWidth);
				}

				return result;
			}

			@Override
			protected void done() {
				try {
					BufferedImage bufferedImage = get();
					cachedImages.put(renderWidth + ":" + renderHeight,
							bufferedImage);
					fireAsyncCompleted(true);
				} catch (Exception exc) {
					fireAsyncCompleted(false);
				}
			}
		};
		worker.execute();
	}

	/**
	 * Fires the asynchronous load event.
	 * 
	 * @param event
	 *            Event object.
	 */
	protected void fireAsyncCompleted(Boolean event) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == AsynchronousLoadListener.class) {
				((AsynchronousLoadListener) listeners[i + 1]).completed(event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.common.AsynchronousLoading#isLoading()
	 */
	@Override
	public synchronized boolean isLoading() {
		BufferedImage image = this.cachedImages.get(this.getIconWidth() + ":"
				+ this.getIconHeight());
		return (image == null);
	}
}
