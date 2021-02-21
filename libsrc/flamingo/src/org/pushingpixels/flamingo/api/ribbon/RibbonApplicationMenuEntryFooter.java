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

/**
 * Metadata description for the footer entries of the
 * {@link RibbonApplicationMenu}. The footer entries at runtime are represented
 * by {@link CommandButtonKind#ACTION_ONLY} command buttons placed in a
 * right-aligned row along the bottom edge of the ribbon application menu.
 * 
 * @author Kirill Grouchnikov
 */
public class RibbonApplicationMenuEntryFooter extends
		RibbonApplicationMenuEntry {
	/**
	 * Creates the metadata description of a {@link RibbonApplicationMenu}
	 * footer menu entry.
	 * 
	 * @param icon
	 *            The icon of this footer menu entry. Must be non-
	 *            <code>null</code>.
	 * @param text
	 *            The text of this footer menu entry. Must be non-
	 *            <code>null</code>.
	 * @param mainActionListener
	 *            The main action listener for this footer menu entry. While
	 *            this can be <code>null</code>, clicking on the matching button
	 *            will have no effect.
	 */
	public RibbonApplicationMenuEntryFooter(ResizableIcon icon, String text,
			ActionListener mainActionListener) {
		super(icon, text, mainActionListener, CommandButtonKind.ACTION_ONLY);
	}
}
