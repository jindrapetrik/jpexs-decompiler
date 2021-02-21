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

import java.util.concurrent.ExecutorService;

/**
 * This interface is used for asynchronously-loaded contents. For example, the
 * {@link SvgBatikResizableIcon} uses the {@link ExecutorService} to load the
 * SVG image in the background. When the image is loaded, the component that
 * contains this image ({@link JCommandButton} for example) is notified to
 * repaint itself.
 * 
 * @author Kirill Grouchnikov.
 */
public interface AsynchronousLoading {
	/**
	 * Adds listener on the asynchronous loading events.
	 * 
	 * @param l
	 * 		Listener to add.
	 */
	public void addAsynchronousLoadListener(AsynchronousLoadListener l);

	/**
	 * Removes listener on the asynchronous loading events.
	 * 
	 * @param l
	 * 		Listener to remove.
	 */
	public void removeAsynchronousLoadListener(AsynchronousLoadListener l);

	/**
	 * Returns indication whether the content is still loading.
	 * 
	 * @return <code>true</code> if the content is still loading,
	 * 	<code>false</code> otherwise.
	 */
	public boolean isLoading();
}
