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

import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * Basic metadata for entries in the ribbon application menu.
 * 
 * <p>
 * At runtime, the application menu entries are implemented as
 * {@link JCommandMenuButton}, but the application code does not operate on that
 * level. Instead, the application code creates metadata-driven description of
 * the ribbon application menu, and that description is used to create and
 * populate the "real" controls of the application menu popup.
 * </p>
 * 
 * @author Kirill Grouchnikov
 * @see RibbonApplicationMenu
 * @see JRibbon#setApplicationMenu(RibbonApplicationMenu)
 */
abstract class RibbonApplicationMenuEntry {
	/**
	 * The menu icon.
	 */
	protected ResizableIcon icon;

	/**
	 * The menu icon for disabled state. Optional, can be <code>null</code>.
	 */
	protected ResizableIcon disabledIcon;

	/**
	 * The menu text.
	 */
	protected String text;

	/**
	 * The main action listener for this menu entry.
	 */
	protected ActionListener mainActionListener;

	/**
	 * The kind of the command button that represents this menu entry.
	 */
	protected CommandButtonKind entryKind;

	/**
	 * Enabled state of this menu.
	 */
	protected boolean isEnabled;

	/**
	 * Optional key tip for the action area of the command button that
	 * represents this menu entry.
	 */
	protected String actionKeyTip;

	/**
	 * Optional key tip for the popup area of the command button that represents
	 * this menu entry.
	 */
	protected String popupKeyTip;

	/**
	 * Creates the basic metadata description of a {@link RibbonApplicationMenu}
	 * menu entry.
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
	public RibbonApplicationMenuEntry(ResizableIcon icon, String text,
			ActionListener mainActionListener, CommandButtonKind entryKind) {
		super();
		this.icon = icon;
		this.text = text;
		this.mainActionListener = mainActionListener;
		this.entryKind = entryKind;
		this.isEnabled = true;
	}

	/**
	 * Returns the icon of this application menu entry.
	 * 
	 * @return The icon of this application menu entry.
	 */
	public ResizableIcon getIcon() {
		return this.icon;
	}

	/**
	 * Returns the text of this application menu entry.
	 * 
	 * @return The text of this application menu entry.
	 * @see #setText(String)
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Sets the new text for this application menu entry.
	 * 
	 * @param text
	 *            The new text for this application menu entry.
	 * @see #getText()
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Returns the main action listener associated with this application menu
	 * entry.
	 * 
	 * @return The main action listener associated with this application menu
	 *         entry.
	 */
	public ActionListener getMainActionListener() {
		return this.mainActionListener;
	}

	/**
	 * Returns the kind of the command button that represents this menu entry.
	 * 
	 * @return The kind of the command button that represents this menu entry.
	 */
	public CommandButtonKind getEntryKind() {
		return this.entryKind;
	}

	/**
	 * Sets the enabled state of the command button that represents this menu
	 * entry.
	 * 
	 * @param isEnabled
	 *            If <code>true</code>, the command button that represents this
	 *            menu entry will be enabled, if <code>false</code>, the command
	 *            button will be disabled.
	 * @see #isEnabled
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Returns the enabled state of the command button that represents this menu
	 * entry.
	 * 
	 * @return <code>true</code> if the command button that represents this menu
	 *         entry is enabled, <code>false</code> otherwise.
	 */
	public boolean isEnabled() {
		return this.isEnabled;
	}

	/**
	 * Returns the key tip for the action area of the command button that
	 * represents this menu entry.
	 * 
	 * @return The key tip for the action area of the command button that
	 *         represents this menu entry.
	 * @see #setActionKeyTip(String)
	 * @see #getPopupKeyTip()
	 */
	public String getActionKeyTip() {
		return this.actionKeyTip;
	}

	/**
	 * Sets the new value for the key tip for the action area of the command
	 * button that represents this menu entry.
	 * 
	 * @param actionKeyTip
	 *            The new value for the key tip for the action area of the
	 *            command button that represents this menu entry.
	 * @see #getActionKeyTip()
	 * @see #setPopupKeyTip(String)
	 */
	public void setActionKeyTip(String actionKeyTip) {
		this.actionKeyTip = actionKeyTip;
	}

	/**
	 * Returns the key tip for the popup area of the command button that
	 * represents this menu entry.
	 * 
	 * @return The key tip for the popup area of the command button that
	 *         represents this menu entry.
	 * @see #setPopupKeyTip(String)
	 * @see #getActionKeyTip()
	 */
	public String getPopupKeyTip() {
		return this.popupKeyTip;
	}

	/**
	 * Sets the new value for the key tip for the popup area of the command
	 * button that represents this menu entry.
	 * 
	 * @param popupKeyTip
	 *            The new value for the key tip for the popup area of the
	 *            command button that represents this menu entry.
	 * @see #getPopupKeyTip()
	 * @see #setActionKeyTip(String)
	 */
	public void setPopupKeyTip(String popupKeyTip) {
		this.popupKeyTip = popupKeyTip;
	}

	/**
	 * Returns the disabled icon for the command button that represents this
	 * menu entry.
	 * 
	 * @return The disabled icon for the command button that represents this
	 *         menu entry.
	 * @see #setDisabledIcon(ResizableIcon)
	 */
	public ResizableIcon getDisabledIcon() {
		return this.disabledIcon;
	}

	/**
	 * Sets the disabled icon for the command button that represents this menu
	 * entry.
	 * 
	 * @param disabledIcon
	 *            The disabled icon for the command button that represents this
	 *            menu entry.
	 * @see #getDisabledIcon()
	 */
	public void setDisabledIcon(ResizableIcon disabledIcon) {
		this.disabledIcon = disabledIcon;
	}
}
