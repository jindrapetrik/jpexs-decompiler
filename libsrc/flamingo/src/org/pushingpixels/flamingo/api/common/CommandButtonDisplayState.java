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

import org.pushingpixels.flamingo.internal.ui.common.*;

/**
 * Display state for command buttons. This class provides a number of core
 * display states, and it is possible to create additional custom states by
 * using the protected constructor and implementing the relevant abstract
 * methods.
 * 
 * @author Kirill Grouchnikov
 */
public abstract class CommandButtonDisplayState {
	/**
	 * Fit to icon state.
	 */
	public static final CommandButtonDisplayState FIT_TO_ICON = new CommandButtonDisplayState(
			"Fit to icon", -1) {
		@Override
		public CommandButtonLayoutManager createLayoutManager(
				AbstractCommandButton commandButton) {
			return new CommandButtonLayoutManagerCustom(commandButton);
		}
	};

	/**
	 * Big state.
	 */
	public static final CommandButtonDisplayState BIG = new CommandButtonDisplayState(
			"Big", 32) {
		@Override
		public CommandButtonLayoutManager createLayoutManager(
				AbstractCommandButton commandButton) {
			return new CommandButtonLayoutManagerBig(commandButton);
		}
	};

	/**
	 * Tile state.
	 */
	public static final CommandButtonDisplayState TILE = new CommandButtonDisplayState(
			"Tile", 32) {
		@Override
		public CommandButtonLayoutManager createLayoutManager(
				AbstractCommandButton arg0) {
			return new CommandButtonLayoutManagerTile();
		}
	};

	/**
	 * Medium state.
	 */
	public static final CommandButtonDisplayState MEDIUM = new CommandButtonDisplayState(
			"Medium", 16) {
		@Override
		public CommandButtonLayoutManager createLayoutManager(
				AbstractCommandButton arg0) {
			return new CommandButtonLayoutManagerMedium();
		}
	};

	/**
	 * Small state.
	 */
	public static final CommandButtonDisplayState SMALL = new CommandButtonDisplayState(
			"Small", 16) {
		@Override
		public CommandButtonLayoutManager createLayoutManager(
				AbstractCommandButton arg0) {
			return new CommandButtonLayoutManagerSmall();
		}
	};

	/**
	 * Preferred icon size for this state.
	 * 
	 * @see #CommandButtonDisplayState(String, int)
	 * @see #getPreferredIconSize()
	 */
	int preferredIconSize;

	/**
	 * Display name for this state.
	 * 
	 * @see #CommandButtonDisplayState(String, int)
	 * @see #getDisplayName()
	 */
	String displayName;

	/**
	 * Creates a new element state.
	 * 
	 * @param displayName
	 *            Display name.
	 * @param preferredIconSize
	 *            Preferred icon size.
	 */
	protected CommandButtonDisplayState(String displayName,
			int preferredIconSize) {
		this.displayName = displayName;
		this.preferredIconSize = preferredIconSize;
	}

	/**
	 * Returns the display name for this state.
	 * 
	 * @return The display name for this state.
	 * @see #CommandButtonDisplayState(String, int)
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Returns the preferred icon size for this state.
	 * 
	 * @return The preferred icon size for this state.
	 * @see #CommandButtonDisplayState(String, int)
	 */
	public int getPreferredIconSize() {
		return this.preferredIconSize;
	}

	/**
	 * Creates a layout manager for the specified button.
	 * 
	 * @param commandButton
	 *            Command button.
	 * @return A layout manager for the specified button.
	 */
	public abstract CommandButtonLayoutManager createLayoutManager(
			AbstractCommandButton commandButton);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getDisplayName();
	}
}
