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
package org.pushingpixels.flamingo.api.ribbon;

import java.awt.event.ActionListener;

import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;

/**
 * Metadata description for the secondary menu entries of the
 * {@link RibbonApplicationMenu}. The secondary menu entries at runtime are
 * represented by command menu buttons placed in the right panel of the
 * application menu.
 * 
 * @author Kirill Grouchnikov
 */
public class RibbonApplicationMenuEntrySecondary extends
		RibbonApplicationMenuEntry {
	/**
	 * Extra description text for this secondary menu entry.
	 * 
	 * @see #getDescriptionText()
	 * @see #setDescriptionText(String)
	 */
	protected String descriptionText;

	/**
	 * Popup callback for this menu entry. Must be not <code>null</code> if the
	 * menu entry kind has popup part.
	 * 
	 * @see #getPopupCallback()
	 * @see #setPopupCallback(PopupPanelCallback)
	 */
	protected PopupPanelCallback popupCallback;

	/**
	 * Creates the metadata description of a {@link RibbonApplicationMenu}
	 * secondary menu entry.
	 * 
	 * @param icon
	 *            The icon of this menu entry. Must be non-<code>null</code>.
	 * @param text
	 *            The text of this menu entry. Must be non-<code>null</code>.
	 * @param mainActionListener
	 *            The main action listener for this menu entry. If the entry
	 *            kind is {@link CommandButtonKind#POPUP_ONLY}, this listener
	 *            will be ignored.
	 * @param entryKind
	 *            The kind of the command button that will represent this menu
	 *            entry. Must be non- <code>null</code>.
	 */
	public RibbonApplicationMenuEntrySecondary(ResizableIcon icon, String text,
			ActionListener mainActionListener, CommandButtonKind entryKind) {
		super(icon, text, mainActionListener, entryKind);
	}

	/**
	 * Returns the description text of this secondary menu entry.
	 * 
	 * @return The description text of this secondary menu entry.
	 * @see #setDescriptionText(String)
	 */
	public String getDescriptionText() {
		return this.descriptionText;
	}

	/**
	 * Sets the new description text for this secondary menu entry.
	 * 
	 * @param descriptionText
	 *            The new description text for this secondary menu entry.
	 * @see #getDescriptionText()
	 */
	public void setDescriptionText(String descriptionText) {
		this.descriptionText = descriptionText;
	}

	/**
	 * Sets the popup callback for this secondary menu entry.
	 * 
	 * @param popupCallback
	 *            The popup callback for this secondary menu entry.
	 * @see #getPopupCallback()
	 */
	public void setPopupCallback(PopupPanelCallback popupCallback) {
		this.popupCallback = popupCallback;
	}

	/**
	 * Returns the current popup callback of this secondary menu entry.
	 * 
	 * @return The current popup callback of this secondary menu entry.
	 * @see #setPopupCallback(PopupPanelCallback)
	 */
	public PopupPanelCallback getPopupCallback() {
		return popupCallback;
	}
}
