/*
 * Copyright (c) 2003-2010 Flamingo Kirill Grouchnikov
 * and <a href="http://www.topologi.com">Topologi</a>. 
 * Contributed by <b>Rick Jelliffe</b> of <b>Topologi</b> 
 * in January 2006. in All Rights Reserved.
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
 *  o Neither the name of Flamingo Kirill Grouchnikov Topologi nor the names of 
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
package org.pushingpixels.flamingo.api.bcb;

import javax.swing.Icon;

/**
 * A single item in the {@link JBreadcrumbBar} model.
 * 
 * @param <T>
 *            Type of associated data.
 */
public final class BreadcrumbItem<T> {
	/**
	 * Display key for this item.
	 */
	protected String key;

	/**
	 * Data value for this item.
	 */
	protected T data;

	/**
	 * The index of <code>this</code> item.
	 */
	private int index = 0;

	/**
	 * The optional icon.
	 */
	private Icon icon;

	/**
	 * Creates a new item.
	 * 
	 * @param key
	 *            Item key.
	 * @param data
	 *            Item data.
	 */
	public BreadcrumbItem(String key, T data) {
		this.key = key;
		this.data = data;
	}

	/**
	 * Creates a new item.
	 * 
	 * @param s
	 *            String that will be used for display purposes.
	 */
	public BreadcrumbItem(String s) {
		this(s, null);
	}

	public String getKey() {
		return key;
	}

	public T getData() {
		return data;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Returns the index of <code>this</code> item.
	 * 
	 * @return The index of <code>this</code> item.
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Sets the index of <code>this</code> item.
	 * 
	 * @param index
	 *            The new index of <code>this</code> item.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getKey() + ":" + getData();
	}

	/**
	 * Returns the icon of <code>this</code> item.
	 * 
	 * @return The icon of <code>this</code> item.
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Sets the new icon on <code>this</code> item.
	 * 
	 * @param icon
	 *            The new icon for <code>this</code> item.
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}
}
