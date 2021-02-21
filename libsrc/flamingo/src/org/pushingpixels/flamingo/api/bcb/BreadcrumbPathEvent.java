/*
 * Copyright (c) 2003-2010 Flamingo Kirill Grouchnikov
 * and <a href="http://www.topologi.com">Topologi</a>. 
 * Contributed by <b>Rick Jelliffe</b> of <b>Topologi</b> 
 * in January 2006. 
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

/**
 * Event on the breadcrumb bar path.
 */
public class BreadcrumbPathEvent {
	/**
	 * The object that fired <code>this</code> event.
	 */
	private Object src;

	/**
	 * The index of the first path item that has changed.
	 */
	private int indexOfFirstChange;

	/**
	 * Creates a new breadcrumb bar path event.
	 * 
	 * @param src
	 *            Event source.
	 * @param indexOfFirstChange
	 *            The index of the first path item that has changed.
	 */
	public BreadcrumbPathEvent(Object src, int indexOfFirstChange) {
		this.src = src;
		this.indexOfFirstChange = indexOfFirstChange;
	}

	/**
	 * Returns the index of the first path item that has changed.
	 * 
	 * @return The index of the first path item that has changed.
	 */
	public int getIndexOfFirstChange() {
		return this.indexOfFirstChange;
	}

	/**
	 * Returns the source of <code>this</code> event.
	 * 
	 * @return The source of <code>this</code> event.
	 */
	public Object getSource() {
		return this.src;
	}
}
