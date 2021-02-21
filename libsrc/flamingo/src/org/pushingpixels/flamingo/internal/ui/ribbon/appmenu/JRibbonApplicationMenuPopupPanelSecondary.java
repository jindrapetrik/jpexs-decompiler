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

import javax.swing.SwingUtilities;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonPopupOrientationKind;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class JRibbonApplicationMenuPopupPanelSecondary extends
		JCommandButtonPanel {
	protected static final CommandButtonDisplayState MENU_TILE_LEVEL_2 = new CommandButtonDisplayState(
			"Ribbon application menu tile level 2", 32) {
		@Override
		public CommandButtonLayoutManager createLayoutManager(
				AbstractCommandButton commandButton) {
			return new CommandButtonLayoutManagerMenuTileLevel2();
		}
	};

	public JRibbonApplicationMenuPopupPanelSecondary(
			RibbonApplicationMenuEntryPrimary primaryMenuEntry) {
		super(MENU_TILE_LEVEL_2);
		this.setMaxButtonColumns(1);

		int groupCount = primaryMenuEntry.getSecondaryGroupCount();
		for (int i = 0; i < groupCount; i++) {
			String groupDesc = primaryMenuEntry.getSecondaryGroupTitleAt(i);
			this.addButtonGroup(groupDesc);

			for (final RibbonApplicationMenuEntrySecondary menuEntry : primaryMenuEntry
					.getSecondaryGroupEntries(i)) {
				JCommandMenuButton commandButton = new JCommandMenuButton(
						menuEntry.getText(), menuEntry.getIcon());
				commandButton.setExtraText(menuEntry.getDescriptionText());
				commandButton.setCommandButtonKind(menuEntry.getEntryKind());
				commandButton.addActionListener(menuEntry
						.getMainActionListener());
				commandButton.setDisplayState(MENU_TILE_LEVEL_2);
				commandButton.setHorizontalAlignment(SwingUtilities.LEADING);
				commandButton
						.setPopupOrientationKind(CommandButtonPopupOrientationKind.SIDEWARD);
				commandButton.setEnabled(menuEntry.isEnabled());
				commandButton.setPopupCallback(menuEntry.getPopupCallback());
				commandButton.setActionKeyTip(menuEntry.getActionKeyTip());
				commandButton.setPopupKeyTip(menuEntry.getPopupKeyTip());
				if (menuEntry.getDisabledIcon() != null) {
					commandButton.setDisabledIcon(menuEntry.getDisabledIcon());
				}
				this.addButtonToLastGroup(commandButton);
			}
		}
	}
}
