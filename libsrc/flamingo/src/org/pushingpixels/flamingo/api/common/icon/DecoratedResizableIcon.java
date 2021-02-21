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
import java.util.ArrayList;

import org.pushingpixels.flamingo.api.common.AsynchronousLoadListener;
import org.pushingpixels.flamingo.api.common.AsynchronousLoading;

/**
 * Implementation of {@link ResizableIcon} that adds decorations to a main icon.
 * 
 * @author Kirill Grouchnikov
 */
public class DecoratedResizableIcon implements ResizableIcon,
		AsynchronousLoading {
	/**
	 * The main delegate icon.
	 */
	protected ResizableIcon delegate;

	/**
	 * List of icon decorators.
	 */
	protected java.util.List<IconDecorator> decorators;

	/**
	 * Icon decorator interface.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static interface IconDecorator {
		/**
		 * Paints the icon decoration.
		 * 
		 * @param c
		 *            Component.
		 * @param g
		 *            Graphics context.
		 * @param mainIconX
		 *            X position of main icon painting.
		 * @param mainIconY
		 *            Y position of main icon painting.
		 * @param mainIconWidth
		 *            Width of main icon.
		 * @param mainIconHeight
		 *            Height of main icon.
		 */
		public void paintIconDecoration(Component c, Graphics g, int mainIconX,
				int mainIconY, int mainIconWidth, int mainIconHeight);
	}

	/**
	 * Creates a new decorated icon.
	 * 
	 * @param delegate
	 *            The main icon.
	 * @param decorators
	 *            Icon decorators.
	 */
	public DecoratedResizableIcon(ResizableIcon delegate,
			IconDecorator... decorators) {
		this.delegate = delegate;
		this.decorators = new ArrayList<IconDecorator>();
		if (decorators != null) {
			for (IconDecorator decorator : decorators) {
				this.decorators.add(decorator);
			}
		}
	}

	/**
	 * Creates a new decorated icon with no decorators. Decorators can be added
	 * later with {@link #addIconDecorator(IconDecorator)}.
	 * 
	 * @param delegate
	 *            Main icon.
	 */
	public DecoratedResizableIcon(ResizableIcon delegate) {
		this(delegate, (IconDecorator) null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
    public int getIconHeight() {
		return this.delegate.getIconHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
    public int getIconWidth() {
		return this.delegate.getIconWidth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
	 */
	@Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
		this.delegate.paintIcon(c, g, x, y);
		for (IconDecorator decorator : this.decorators) {
			decorator.paintIconDecoration(c, g, x, y, this.delegate
					.getIconWidth(), this.delegate.getIconHeight());
		}
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
		this.delegate.setDimension(newDimension);
	}

	/**
	 * Adds the specified decorator to the end of the decorator sequence. If the
	 * specified decorator already exists, it is not moved to the end of the
	 * sequence.
	 * 
	 * @param decorator
	 *            Decorator to add.
	 */
	public void addIconDecorator(IconDecorator decorator) {
		if (this.decorators.contains(decorator))
			return;
		this.decorators.add(decorator);
	}

	/**
	 * Removes the specified decorator.
	 * 
	 * @param decorator
	 *            Decorator to remove.
	 */
	public void removeIconDecorator(IconDecorator decorator) {
		this.decorators.remove(decorator);
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
		if (this.delegate instanceof AsynchronousLoading) {
			((AsynchronousLoading) this.delegate)
					.addAsynchronousLoadListener(l);
		}
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
		if (this.delegate instanceof AsynchronousLoading) {
			((AsynchronousLoading) this.delegate)
					.removeAsynchronousLoadListener(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.common.AsynchronousLoading#isLoading()
	 */
	@Override
	public synchronized boolean isLoading() {
		if (this.delegate instanceof AsynchronousLoading) {
			if (((AsynchronousLoading) this.delegate).isLoading())
				return true;
		}
		for (IconDecorator decorator : this.decorators) {
			if (decorator instanceof AsynchronousLoading) {
				if (((AsynchronousLoading) decorator).isLoading())
					return true;
			}
		}
		return false;
	}
}
