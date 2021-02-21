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
package org.pushingpixels.flamingo.api.common.popup;

import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.pushingpixels.flamingo.internal.ui.common.popup.BasicPopupPanelUI;
import org.pushingpixels.flamingo.internal.ui.common.popup.PopupPanelUI;

/**
 * Base class for popup panels.
 * 
 * @author Kirill Grouchnikov
 * @see PopupPanelManager#addPopup(javax.swing.JComponent, javax.swing.Popup,
 *      JPopupPanel)
 */
public abstract class JPopupPanel extends JPanel {
	/**
	 * @see #getUIClassID
	 */
	public static final String uiClassID = "PopupPanelUI";

	/**
	 * The customizer for this popup panel. Can be <code>null</code>.
	 * 
	 * @see #getCustomizer()
	 * @see #setCustomizer(PopupPanelCustomizer)
	 */
	protected PopupPanelCustomizer customizer;

	/**
	 * Allows providing custom application logic for computing the screen bounds
	 * of popup panels before they are shown on the screen.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static interface PopupPanelCustomizer {
		/**
		 * Returns the requested screen bounds of the associated popup panel.
		 * 
		 * @return The requested screen bounds of the associated popup panel.
		 */
		public Rectangle getScreenBounds();
	}

	/**
	 * Protected to prevent direct instantiation.
	 */
	protected JPopupPanel() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#getUI()
	 */
	@Override
	public PopupPanelUI getUI() {
		return (PopupPanelUI) ui;
	}

	/**
	 * Sets the look and feel (L&F) object that renders this component.
	 * 
	 * @param ui
	 *            the PopupGalleryUI L&F object
	 */
	protected void setUI(PopupPanelUI ui) {
		super.setUI(ui);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#getUIClassID()
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((PopupPanelUI) UIManager.getUI(this));
		} else {
			setUI(BasicPopupPanelUI.createUI(this));
		}
	}

	/**
	 * Sets the customizer for this popup panel.
	 * 
	 * @param customizer
	 *            The customizer for this popup panel.
	 * @see #getCustomizer()
	 */
	public void setCustomizer(PopupPanelCustomizer customizer) {
		this.customizer = customizer;
	}

	/**
	 * Returns the customizer of this popup panel. Can return <code>null</code>.
	 * 
	 * @return The customizer of this popup panel.
	 * @see #setCustomizer(PopupPanelCustomizer)
	 */
	public PopupPanelCustomizer getCustomizer() {
		return this.customizer;
	}
}
