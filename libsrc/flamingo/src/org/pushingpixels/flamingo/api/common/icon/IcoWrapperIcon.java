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
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

import org.pushingpixels.flamingo.api.common.AsynchronousLoadListener;
import org.pushingpixels.flamingo.api.common.AsynchronousLoading;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 * Helper class to load image planes from .ICO files.
 * 
 * @author Kirill Grouchnikov
 */
abstract class IcoWrapperIcon implements Icon, AsynchronousLoading {
	/**
	 * The input stream of the original image.
	 */
	protected InputStream icoInputStream;

	/**
	 * Image planes of the original ICO image.
	 */
	protected Map<Integer, BufferedImage> icoPlaneMap;

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
	 * Create a new SVG icon.
	 * 
	 * @param inputStream
	 *            The input stream to read the SVG document from.
	 * @param w
	 *            The width of the icon.
	 * @param h
	 *            The height of the icon.
	 */
	public IcoWrapperIcon(InputStream inputStream, int w, int h) {
		this.icoInputStream = inputStream;
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
				synchronized (icoInputStream) {
					if (icoPlaneMap == null) {
						// read original ICO image
						Ico ico = new Ico(icoInputStream);
						icoPlaneMap = new TreeMap<Integer, BufferedImage>();

						Set<Integer> widths = new HashSet<Integer>();
						for (int i = 0; i < ico.getNumImages(); i++) {
							BufferedImage icoPlane = ico.getImage(i);
							widths.add(icoPlane.getWidth());
						}

						for (int width : widths) {
							// find the ico plane with the largest color count
							BufferedImage bestMatch = null;
							int bestColorCount = -1;
							for (int i = 0; i < ico.getNumImages(); i++) {
								BufferedImage icoPlane = ico.getImage(i);
								if (icoPlane.getWidth() != width)
									continue;
								int icoPlaneColorCount = ico.getNumColors(i);
								if (icoPlaneColorCount == 0) {
									bestMatch = icoPlane;
									bestColorCount = 0;
								} else {
									if (bestColorCount == 0)
										continue;
									if (icoPlaneColorCount > bestColorCount) {
										bestMatch = icoPlane;
										bestColorCount = icoPlaneColorCount;
									}
								}
							}
							icoPlaneMap.put(width, bestMatch);
						}
					}
				}

				// find the best match
				int indexOfBestMatch = -1;
				int bestMatchWidth = -1;
				for (Map.Entry<Integer, BufferedImage> icoPlaneMapEntry : icoPlaneMap
						.entrySet()) {
					BufferedImage icoPlane = icoPlaneMapEntry.getValue();
					int icoPlaneWidth = icoPlane.getWidth();
					if (icoPlaneWidth > renderWidth) {
						// check if the ICO plane width is closer
						// to the required width than the best match so far
						if (bestMatchWidth < 0) {
							bestMatchWidth = icoPlaneWidth;
						} else {
							if (bestMatchWidth > icoPlaneWidth) {
								bestMatchWidth = icoPlaneWidth;
							}
						}
					}
				}

				// if at this point the best match is not found, it
				// means that the requested width is bigger than
				// any of the ICO planes. Take the biggest ICO plane
				// available
				if (indexOfBestMatch < 0) {
					for (Map.Entry<Integer, BufferedImage> icoPlaneMapEntry : icoPlaneMap
							.entrySet()) {
						BufferedImage icoPlane = icoPlaneMapEntry.getValue();
						int icoPlaneWidth = icoPlane.getWidth();
						if (icoPlaneWidth > bestMatchWidth) {
							bestMatchWidth = icoPlaneWidth;
						}
					}
				}

				BufferedImage bestMatchPlane = icoPlaneMap.get(bestMatchWidth);
				BufferedImage result = bestMatchPlane;
				float scaleX = (float) bestMatchPlane.getWidth()
						/ (float) renderWidth;
				float scaleY = (float) bestMatchPlane.getHeight()
						/ (float) renderHeight;

				float scale = Math.max(scaleX, scaleY);
				if (scale > 1.0f) {
					int finalWidth = (int) (bestMatchPlane.getWidth() / scale);
					result = FlamingoUtilities.createThumbnail(bestMatchPlane,
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

/**
 * The code below is copyrighted by Jeff Friesen and first appeared on <a
 * href="<a href="http://www.informit.com">InformIT.com</a> at <a
 * href="http://www.informit.com/articles/article.aspx?p=1186882">this
 * location</a>. This code is licensed under BSD license and can be reused as
 * long as the credit is given to Jeff Friesen, InformIT.com and the original
 * URL above.
 * 
 * @author Jeff Friesen
 */
class Ico {
	private final static int FDE_OFFSET = 6; // first directory entry offset
	private final static int DE_LENGTH = 16; // directory entry length

	private final static int BMIH_LENGTH = 40; // BITMAPINFOHEADER length

	private byte[] icoimage = new byte[0]; // new byte [0] facilitates read()

	private int numImages;

	private BufferedImage[] bi;

	private int[] colorCount;

	public Ico(File file) throws BadIcoResException, IOException {
		this(file.getAbsolutePath());
	}

	public Ico(InputStream is) throws BadIcoResException, IOException {
		try {
			read(is);
			parseICOImage();
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
			}
		}
	}

	public Ico(String filename) throws BadIcoResException, IOException {
		this(new FileInputStream(filename));
	}

	public Ico(URL url) throws BadIcoResException, IOException {
		this(url.openStream());
	}

	public BufferedImage getImage(int index) {
		if (index < 0 || index >= numImages)
			throw new IllegalArgumentException("index out of range");

		return bi[index];
	}

	public int getNumColors(int index) {
		if (index < 0 || index >= numImages)
			throw new IllegalArgumentException("index out of range");

		return colorCount[index];
	}

	public int getNumImages() {
		return numImages;
	}

	private int calcScanlineBytes(int width, int bitCount) {
		// Calculate minimum number of double-words required to store width
		// pixels where each pixel occupies bitCount bits. XOR and AND bitmaps
		// are stored such that each scanline is aligned on a double-word
		// boundary.

		return (((width * bitCount) + 31) / 32) * 4;
	}

	private void parseICOImage() throws BadIcoResException, IOException {
		// Check resource type field.

		if (icoimage[2] != 1 || icoimage[3] != 0)
			throw new BadIcoResException("Not an ICO resource");

		numImages = ubyte(icoimage[5]);
		numImages <<= 8;
		numImages |= icoimage[4];

		bi = new BufferedImage[numImages];

		colorCount = new int[numImages];

		for (int i = 0; i < numImages; i++) {
			int width = ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH]);

			int height = ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 1]);

			colorCount[i] = ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 2]);

			int bytesInRes = ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 11]);
			bytesInRes <<= 8;
			bytesInRes |= ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 10]);
			bytesInRes <<= 8;
			bytesInRes |= ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 9]);
			bytesInRes <<= 8;
			bytesInRes |= ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 8]);

			int imageOffset = ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 15]);
			imageOffset <<= 8;
			imageOffset |= ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 14]);
			imageOffset <<= 8;
			imageOffset |= ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 13]);
			imageOffset <<= 8;
			imageOffset |= ubyte(icoimage[FDE_OFFSET + i * DE_LENGTH + 12]);

			if (icoimage[imageOffset] == 40 && icoimage[imageOffset + 1] == 0
					&& icoimage[imageOffset + 2] == 0
					&& icoimage[imageOffset + 3] == 0) {
				// BITMAPINFOHEADER detected

				int _width = ubyte(icoimage[imageOffset + 7]);
				_width <<= 8;
				_width |= ubyte(icoimage[imageOffset + 6]);
				_width <<= 8;
				_width |= ubyte(icoimage[imageOffset + 5]);
				_width <<= 8;
				_width |= ubyte(icoimage[imageOffset + 4]);

				// If width is 0 (for 256 pixels or higher), _width contains
				// actual width.

				if (width == 0)
					width = _width;

				int _height = ubyte(icoimage[imageOffset + 11]);
				_height <<= 8;
				_height |= ubyte(icoimage[imageOffset + 10]);
				_height <<= 8;
				_height |= ubyte(icoimage[imageOffset + 9]);
				_height <<= 8;
				_height |= ubyte(icoimage[imageOffset + 8]);

				// If height is 0 (for 256 pixels or higher), _height contains
				// actual height times 2.

				if (height == 0)
					height = _height >> 1; // Divide by 2.

				int planes = ubyte(icoimage[imageOffset + 13]);
				planes <<= 8;
				planes |= ubyte(icoimage[imageOffset + 12]);

				int bitCount = ubyte(icoimage[imageOffset + 15]);
				bitCount <<= 8;
				bitCount |= ubyte(icoimage[imageOffset + 14]);

				// If colorCount [i] is 0, the number of colors is determined
				// from the planes and bitCount values. For example, the number
				// of colors is 256 when planes is 1 and bitCount is 8. Leave
				// colorCount [i] set to 0 when planes is 1 and bitCount is 32.

				if (colorCount[i] == 0) {
					if (planes == 1) {
						if (bitCount == 1)
							colorCount[i] = 2;
						else if (bitCount == 4)
							colorCount[i] = 16;
						else if (bitCount == 8)
							colorCount[i] = 256;
						else if (bitCount != 32)
							colorCount[i] = (int) Math.pow(2, bitCount);
					} else
						colorCount[i] = (int) Math.pow(2, bitCount * planes);
				}

				bi[i] = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);

				// Parse image to image buffer.

				int colorTableOffset = imageOffset + BMIH_LENGTH;

				if (colorCount[i] == 2) {
					int xorImageOffset = colorTableOffset + 2 * 4;

					int scanlineBytes = calcScanlineBytes(width, 1);
					int andImageOffset = xorImageOffset + scanlineBytes
							* height;

					int[] masks = { 128, 64, 32, 16, 8, 4, 2, 1 };

					for (int row = 0; row < height; row++)
						for (int col = 0; col < width; col++) {
							int index;

							if ((ubyte(icoimage[xorImageOffset + row
									* scanlineBytes + col / 8]) & masks[col % 8]) != 0)
								index = 1;
							else
								index = 0;

							int rgb = 0;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4
									+ 2]));
							rgb <<= 8;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4
									+ 1]));
							rgb <<= 8;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4]));

							if ((ubyte(icoimage[andImageOffset + row
									* scanlineBytes + col / 8]) & masks[col % 8]) != 0)
								bi[i].setRGB(col, height - 1 - row, rgb);
							else
								bi[i].setRGB(col, height - 1 - row,
										0xff000000 | rgb);
						}
				} else if (colorCount[i] == 16) {
					int xorImageOffset = colorTableOffset + 16 * 4;

					int scanlineBytes = calcScanlineBytes(width, 4);
					int andImageOffset = xorImageOffset + scanlineBytes
							* height;

					int[] masks = { 128, 64, 32, 16, 8, 4, 2, 1 };

					for (int row = 0; row < height; row++)
						for (int col = 0; col < width; col++) {
							int index;
							if ((col & 1) == 0) // even
							{
								index = ubyte(icoimage[xorImageOffset + row
										* scanlineBytes + col / 2]);
								index >>= 4;
							} else {
								index = ubyte(icoimage[xorImageOffset + row
										* scanlineBytes + col / 2]) & 15;
							}

							int rgb = 0;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4
									+ 2]));
							rgb <<= 8;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4
									+ 1]));
							rgb <<= 8;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4]));

							if ((ubyte(icoimage[andImageOffset + row
									* calcScanlineBytes(width, 1) + col / 8]) & masks[col % 8]) != 0)
								bi[i].setRGB(col, height - 1 - row, rgb);
							else
								bi[i].setRGB(col, height - 1 - row,
										0xff000000 | rgb);
						}
				} else if (colorCount[i] == 256) {
					int xorImageOffset = colorTableOffset + 256 * 4;

					int scanlineBytes = calcScanlineBytes(width, 8);
					int andImageOffset = xorImageOffset + scanlineBytes
							* height;

					int[] masks = { 128, 64, 32, 16, 8, 4, 2, 1 };

					for (int row = 0; row < height; row++)
						for (int col = 0; col < width; col++) {
							int index;
							index = ubyte(icoimage[xorImageOffset + row
									* scanlineBytes + col]);

							int rgb = 0;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4
									+ 2]));
							rgb <<= 8;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4
									+ 1]));
							rgb <<= 8;
							rgb |= (ubyte(icoimage[colorTableOffset + index * 4]));

							if ((ubyte(icoimage[andImageOffset + row
									* calcScanlineBytes(width, 1) + col / 8]) & masks[col % 8]) != 0)
								bi[i].setRGB(col, height - 1 - row, rgb);
							else
								bi[i].setRGB(col, height - 1 - row,
										0xff000000 | rgb);
						}
				} else if (colorCount[i] == 0) {
					int scanlineBytes = calcScanlineBytes(width, 32);

					for (int row = 0; row < height; row++)
						for (int col = 0; col < width; col++) {
							int rgb = ubyte(icoimage[colorTableOffset + row
									* scanlineBytes + col * 4 + 3]);
							rgb <<= 8;
							rgb |= ubyte(icoimage[colorTableOffset + row
									* scanlineBytes + col * 4 + 2]);
							rgb <<= 8;
							rgb |= ubyte(icoimage[colorTableOffset + row
									* scanlineBytes + col * 4 + 1]);
							rgb <<= 8;
							rgb |= ubyte(icoimage[colorTableOffset + row
									* scanlineBytes + col * 4]);

							bi[i].setRGB(col, height - 1 - row, rgb);
						}
				}
			} else if (ubyte(icoimage[imageOffset]) == 0x89
					&& icoimage[imageOffset + 1] == 0x50
					&& icoimage[imageOffset + 2] == 0x4e
					&& icoimage[imageOffset + 3] == 0x47
					&& icoimage[imageOffset + 4] == 0x0d
					&& icoimage[imageOffset + 5] == 0x0a
					&& icoimage[imageOffset + 6] == 0x1a
					&& icoimage[imageOffset + 7] == 0x0a) {
				// PNG detected

				ByteArrayInputStream bais;
				bais = new ByteArrayInputStream(icoimage, imageOffset,
						bytesInRes);
				bi[i] = ImageIO.read(bais);
			} else
				throw new BadIcoResException("BITMAPINFOHEADER or PNG "
						+ "expected");
		}

		icoimage = null; // This array can now be garbage collected.
	}

	private void read(InputStream is) throws IOException {
		int bytesToRead;
		while ((bytesToRead = is.available()) != 0) {
			byte[] icoimage2 = new byte[icoimage.length + bytesToRead];
			System.arraycopy(icoimage, 0, icoimage2, 0, icoimage.length);
			is.read(icoimage2, icoimage.length, bytesToRead);
			icoimage = icoimage2;
		}
	}

	private int ubyte(byte b) {
		return (b < 0) ? 256 + b : b; // Convert byte to unsigned byte.
	}
}

class BadIcoResException extends Exception {
	public BadIcoResException(String message) {
		super(message);
	}
}
