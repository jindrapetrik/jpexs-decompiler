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

import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Implementation of {@link ResizableIcon} interface that wraps image files.
 * 
 * @author Kirill Grouchnikov
 */
public class ImageWrapperResizableIcon extends ImageWrapperIcon implements
		ResizableIcon {
	/**
	 * Returns the icon for the specified URL.
	 * 
	 * @param image
	 *            Image.
	 * @param initialDim
	 *            Initial dimension of the icon.
	 * @return Icon instance.
	 */
	public static ImageWrapperResizableIcon getIcon(Image image,
			Dimension initialDim) {
		return new ImageWrapperResizableIcon(image, initialDim);
	}

	/**
	 * Returns the icon for the specified URL.
	 * 
	 * @param location
	 *            Icon URL.
	 * @param initialDim
	 *            Initial dimension of the icon.
	 * @return Icon instance.
	 */
	public static ImageWrapperResizableIcon getIcon(URL location,
			Dimension initialDim) {
		try {
			return new ImageWrapperResizableIcon(location.openStream(),
					initialDim);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the icon for the specified input stream.
	 * 
	 * @param inputStream
	 *            Icon input stream.
	 * @param initialDim
	 *            Initial dimension of the icon.
	 * @return Icon instance.
	 */
	public static ImageWrapperResizableIcon getIcon(InputStream inputStream,
			Dimension initialDim) {
		return new ImageWrapperResizableIcon(inputStream, initialDim);
	}

	/**
	 * Creates a new image-based resizable icon.
	 * 
	 * @param image
	 *            Image.
	 * @param initialDim
	 *            Initial dimension of the icon.
	 */
	private ImageWrapperResizableIcon(Image image, Dimension initialDim) {
		super(image, initialDim.width, initialDim.height);
	}

	/**
	 * Creates a new image-based resizable icon.
	 * 
	 * @param inputStream
	 *            Input stream with the image content.
	 * @param initialDim
	 *            Initial dimension of the icon.
	 */
	private ImageWrapperResizableIcon(InputStream inputStream,
			final Dimension initialDim) {
		super(inputStream, initialDim.width, initialDim.height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.icon.ResizableIcon#setDimension(java.awt.Dimension
	 * )
	 */
	@Override
    public void setDimension(Dimension dim) {
		this.setPreferredSize(dim);
	}
}
