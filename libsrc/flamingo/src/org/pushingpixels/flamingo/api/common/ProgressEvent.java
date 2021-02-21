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
package org.pushingpixels.flamingo.api.common;

import java.util.EventObject;

/**
 * This event is used to notify interested parties that progress has been made
 * in the event source.
 * 
 * @author Kirill Grouchnikov
 * @see ProgressListener
 */
public class ProgressEvent extends EventObject {
	/**
	 * Minimum value of the available progress range.
	 */
	private int minimum;

	/**
	 * Maximum value of the available progress range.
	 */
	private int maximum;

	/**
	 * Current value of the progress.
	 */
	private int progress;

	/**
	 * Creates a new progress event.
	 * 
	 * @param source
	 *            Event source.
	 * @param min
	 *            Minimum value of the available progress range.
	 * @param max
	 *            Maximum value of the available progress range.
	 * @param progress
	 *            Current value of the progress.
	 */
	public ProgressEvent(Object source, int min, int max, int progress) {
		super(source);
		this.maximum = max;
		this.minimum = min;
		this.progress = progress;
	}

	/**
	 * Returns the maximum value of the available progress range.
	 * 
	 * @return The maximum value of the available progress range.
	 */
	public int getMaximum() {
		return this.maximum;
	}

	/**
	 * Returns the minimum value of the available progress range.
	 * 
	 * @return The minimum value of the available progress range.
	 */
	public int getMinimum() {
		return this.minimum;
	}

	/**
	 * Returns the current value of the progress.
	 * 
	 * @return The current value of the progress.
	 */
	public int getProgress() {
		return this.progress;
	}
}
