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
package org.pushingpixels.flamingo.internal.ui.ribbon.appmenu;

import java.awt.*;
import java.beans.PropertyChangeEvent;

import javax.swing.UIManager;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandButtonUI;

/**
 * The main application menu button for {@link JRibbon} component placed in a
 * {@link JRibbonFrame}. This class is for internal use only and is intended for
 * look-and-feel layer customization.
 * 
 * @author Kirill Grouchnikov
 */
public class JRibbonApplicationMenuButton extends JCommandButton {
	private JRibbon ribbon;
	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "RibbonApplicationMenuButtonUI";
    static final int APP_BUTTON_SIZE = Integer.getInteger("peacock.appButtonSize", 24);

	private final static CommandButtonDisplayState APP_MENU_BUTTON_STATE = new CommandButtonDisplayState(
			"Ribbon Application Menu Button", APP_BUTTON_SIZE) {
		@Override
		public org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager createLayoutManager(
				org.pushingpixels.flamingo.api.common.AbstractCommandButton commandButton) {
			return new CommandButtonLayoutManager() {
				@Override
                public int getPreferredIconSize() {
					return APP_BUTTON_SIZE;
				}

				@Override
				public CommandButtonLayoutInfo getLayoutInfo(
						AbstractCommandButton commandButton, Graphics g) {
					CommandButtonLayoutInfo result = new CommandButtonLayoutInfo();
					result.actionClickArea = new Rectangle(0, 0, 0, 0);
					result.popupClickArea = new Rectangle(0, 0, commandButton
							.getWidth(), commandButton.getHeight());
					result.popupActionRect = new Rectangle(0, 0, 0, 0);
					ResizableIcon icon = commandButton.getIcon();
					result.iconRect = new Rectangle(
							(commandButton.getWidth() - icon.getIconWidth()) / 2,
							(commandButton.getHeight() - icon.getIconHeight()) / 2,
							icon.getIconWidth(), icon.getIconHeight());
					result.isTextInActionArea = false;
					return result;
				}

				@Override
				public Dimension getPreferredSize(
						AbstractCommandButton commandButton) {
					return new Dimension(40, 40);
				}

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
				}

				@Override
				public Point getKeyTipAnchorCenterPoint(
						AbstractCommandButton commandButton) {
					// dead center
					return new Point(commandButton.getWidth() / 2,
							commandButton.getHeight() / 2);
				}
			};
		}
	};

	/**
	 * Creates a new application menu button.
	 */
	public JRibbonApplicationMenuButton(JRibbon ribbon) {
		super("", new EmptyResizableIcon(16));
		this.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
		this.setDisplayState(APP_MENU_BUTTON_STATE);
		this.ribbon = ribbon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JButton#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((BasicCommandButtonUI) UIManager.getUI(this));
		} else {
			setUI(BasicRibbonApplicationMenuButtonUI.createUI(this));
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

	public JRibbon getRibbon() {
		return this.ribbon;
	}
}
