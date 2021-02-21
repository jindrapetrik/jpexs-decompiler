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

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Definition of a layout manager for {@link AbstractCommandButton}s.
 * 
 * @author Kirill Grouchnikov
 */
public interface CommandButtonLayoutManager extends PropertyChangeListener {
	/**
	 * Enumerates the available values for separator orientations.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public enum CommandButtonSeparatorOrientation {
		/**
		 * Vertical separator orientation.
		 */
		VERTICAL,

		/**
		 * Horizontal separator orientation.
		 */
		HORIZONTAL
	}

	/**
	 * Layout information on a single line of text.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public class TextLayoutInfo {
		/**
		 * Text itself.
		 */
		public String text;

		/**
		 * The text rectangle.
		 */
		public Rectangle textRect;
	}

	/**
	 * Layout information on different visual parts of a single command button.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public class CommandButtonLayoutInfo {
		/**
		 * The action area. A mouse click in this area will trigger all
		 * listeners associated with the command button action model
		 * {@link AbstractCommandButton#addActionListener(java.awt.event.ActionListener)}
		 */
		public Rectangle actionClickArea;

		/**
		 * The popup area. A mouse click in this area will trigger the listener
		 * associated with the command button popup model
		 * {@link JCommandButton#setPopupCallback(org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback)}
		 */
		public Rectangle popupClickArea;

		/**
		 * The separator area. If it's not empty, the command button will show a
		 * separator between {@link #actionClickArea} and
		 * {@link #popupClickArea} on mouse rollover - depending on the current
		 * look-and-feel.
		 */
		public Rectangle separatorArea;

		public CommandButtonSeparatorOrientation separatorOrientation;

		/**
		 * Rectangle for the command button icon.
		 */
		public Rectangle iconRect;

		/**
		 * Layout information for the command button text (that can span
		 * multiple lines).
		 */
		public List<TextLayoutInfo> textLayoutInfoList;

		/**
		 * Layout information for the command button extra text (that can span
		 * multiple lines).
		 */
		public List<TextLayoutInfo> extraTextLayoutInfoList;

		/**
		 * Rectangle for the icon associated with the {@link #popupClickArea}.
		 * This icon is usually a single or double arrow indicating that the
		 * command button has a popup area.
		 */
		public Rectangle popupActionRect;

		/**
		 * Indication whether the command button text (rectangles in
		 * {@link #textLayoutInfoList}) belongs in the action area.
		 */
		public boolean isTextInActionArea;
	}

	/**
	 * Returns the preferred size of the specified command button.
	 * 
	 * @param commandButton
	 *            Command button.
	 * @return The preferred size of the specified command button.
	 */
	public Dimension getPreferredSize(AbstractCommandButton commandButton);

	/**
	 * Returns the preferred icon size of command buttons which use this layout
	 * manager.
	 * 
	 * @return The preferred icon size of command buttons which use this layout
	 *         manager.
	 */
	public int getPreferredIconSize();

	/**
	 * Returns the anchor center point of the key tip of the specified command
	 * button.
	 * 
	 * @param commandButton
	 *            Command button.
	 * @return The anchor center point of the key tip of the specified command
	 *         button.
	 */
	public Point getKeyTipAnchorCenterPoint(AbstractCommandButton commandButton);

	/**
	 * Returns the layout information for the specified command button.
	 * 
	 * @param commandButton
	 *            Command button.
	 * @param g
	 *            Graphics context.
	 * @return The layout information for the specified command button.
	 */
	public CommandButtonLayoutInfo getLayoutInfo(
			AbstractCommandButton commandButton, Graphics g);
}
