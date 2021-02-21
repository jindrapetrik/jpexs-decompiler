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
package org.pushingpixels.flamingo.api.common.model;

import javax.swing.ButtonModel;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.PopupActionListener;

/**
 * Model for the popup area of {@link JCommandButton} component.
 * 
 * @author Kirill Grouchnikov
 */
public interface PopupButtonModel extends ButtonModel {
	/**
	 * Adds an <code>PopupActionListener</code> to the model.
	 * 
	 * @param l
	 *            the listener to add
	 */
	void addPopupActionListener(PopupActionListener l);

	/**
	 * Removes an <code>PopupActionListener</code> from the model.
	 * 
	 * @param l
	 *            the listener to remove
	 */
	void removePopupActionListener(PopupActionListener l);

	/**
	 * Sets indication on the visibility status of the associated popup.
	 * 
	 * @param flag
	 *            The visibility status of the associated popup.
	 */
	void setPopupShowing(boolean flag);

	/**
	 * Returns indication whether the associated popup is showing.
	 * 
	 * @return <code>true</code> if the associated popup is showing,
	 *         <code>false</code> otherwise.
	 */
	boolean isPopupShowing();
}