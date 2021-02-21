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
package org.pushingpixels.flamingo.internal.ui.common.popup;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.pushingpixels.flamingo.api.common.popup.JColorSelectorPopupMenu;

public class JColorSelectorComponent extends JComponent {
	private Color color;

	private List<JColorSelectorPopupMenu.ColorSelectorCallback> colorChooserCallbacks;

	private boolean isTopOpen;

	private boolean isBottomOpen;

	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "ColorSelectorComponentUI";

	public JColorSelectorComponent(Color color,
			JColorSelectorPopupMenu.ColorSelectorCallback colorChooserCallback) {
		this.setOpaque(true);
		this.color = color;
		this.colorChooserCallbacks = new ArrayList<JColorSelectorPopupMenu.ColorSelectorCallback>();
		this.colorChooserCallbacks.add(colorChooserCallback);

		this.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JButton#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((ColorSelectorComponentUI) UIManager.getUI(this));
		} else {
			setUI(BasicColorSelectorComponentUI.createUI(this));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JButton#getUIClassID()
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	public Color getColor() {
		return this.color;
	}

	public synchronized void addColorSelectorCallback(
			JColorSelectorPopupMenu.ColorSelectorCallback callback) {
		this.colorChooserCallbacks.add(callback);
	}

	public synchronized void onColorSelected(Color selected) {
		for (JColorSelectorPopupMenu.ColorSelectorCallback callback : this.colorChooserCallbacks) {
			callback.onColorSelected(selected);
		}
	}

	public synchronized void onColorRollover(Color rollover) {
		for (JColorSelectorPopupMenu.ColorSelectorCallback callback : this.colorChooserCallbacks) {
			callback.onColorRollover(rollover);
		}
	}

	public void setTopOpen(boolean isTopOpen) {
		this.isTopOpen = isTopOpen;
	}

	public void setBottomOpen(boolean isBottomOpen) {
		this.isBottomOpen = isBottomOpen;
	}

	public boolean isTopOpen() {
		return this.isTopOpen;
	}

	public boolean isBottomOpen() {
		return this.isBottomOpen;
	}
}
