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
import java.util.*;

import javax.swing.JPanel;

import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

/**
 * Metadata description for the primary menu entries of the
 * {@link RibbonApplicationMenu}. The primary menu entries at runtime are
 * represented by command menu buttons placed in the left panel of the
 * application menu.
 * 
 * <p>
 * There are three different types of primary entries:
 * </p>
 * 
 * <ul>
 * <li>Associated {@link ActionListener} passed to the
 * {@link RibbonApplicationMenuEntryPrimary#RibbonApplicationMenuEntryPrimary(org.pushingpixels.flamingo.api.common.icon.ResizableIcon, String, java.awt.event.ActionListener, org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind)}
 * . When this entry is armed (with mouse rollover or via keyboard navigation),
 * the contents of the secondary area are cleared. The <code>Quit</code> menu
 * item is an example of such a primary menu entry.</li>
 * <li>Associated {@link PrimaryRolloverCallback} set by the
 * {@link #setRolloverCallback(PrimaryRolloverCallback)} . When this entry is
 * armed (with mouse rollover or via keyboard navigation), the contents of the
 * secondary area are populated by the application callback implementation of
 * {@link PrimaryRolloverCallback#menuEntryActivated(javax.swing.JPanel)}. The
 * <code>Open</code> menu item is an example of such a primary menu entry,
 * showing a list of recently opened files.</li>
 * <li>Associated list of {@link RibbonApplicationMenuEntrySecondary}s added
 * with the
 * {@link #addSecondaryMenuGroup(String, RibbonApplicationMenuEntrySecondary...)}
 * API. When this entry is armed (with mouse rollover or via keyboard
 * navigation), the secondary area shows menu buttons for the registered
 * secondary menu entries. The <code>Save As</code> menu item is an example of
 * such a primary menu item, showing a list of default save formats.</li>
 * </ul>
 * 
 * @author Kirill Grouchnikov
 */
public class RibbonApplicationMenuEntryPrimary extends
		RibbonApplicationMenuEntry {
	/**
	 * An optional rollover callback. It allows the application to place custom
	 * content in the secondary panel of the {@link RibbonApplicationMenu} when
	 * this primary menu entry is activated.
	 * 
	 * @see #setRolloverCallback(PrimaryRolloverCallback)
	 * @see #getRolloverCallback()
	 */
	protected PrimaryRolloverCallback rolloverCallback;

	/**
	 * Callback that allows application code to provide custom content on the
	 * secondary panel of the {@link RibbonApplicationMenu}.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static interface PrimaryRolloverCallback {
		/**
		 * Called when the matching primary menu item is activated.
		 * 
		 * @param targetPanel
		 *            The secondary panel of the {@link RibbonApplicationMenu}.
		 *            Note that the application code <strong>must not</strong>
		 *            change the parent hierarchy of this panel.
		 */
		public void menuEntryActivated(JPanel targetPanel);
	}

	/**
	 * List of titles for all menu groups.
	 */
	protected List<String> groupTitles;

	/**
	 * List of all menu groups.
	 */
	protected List<List<RibbonApplicationMenuEntrySecondary>> groupEntries;

	/**
	 * Creates the metadata description of a {@link RibbonApplicationMenu}
	 * primary menu entry.
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
	public RibbonApplicationMenuEntryPrimary(ResizableIcon icon, String text,
			ActionListener mainActionListener, CommandButtonKind entryKind) {
		super(icon, text, mainActionListener, entryKind);
		this.groupTitles = new ArrayList<String>();
		this.groupEntries = new ArrayList<List<RibbonApplicationMenuEntrySecondary>>();
	}

	/**
	 * Adds a titled group of secondary menu entries.
	 * 
	 * @param groupTitle
	 *            The title of the group.
	 * @param entries
	 *            The secondary menu entries belonging to this group.
	 * @return The index of the newly added menu group.
	 * @see #getSecondaryGroupCount()
	 * @see #getSecondaryGroupTitleAt(int)
	 * @see #getSecondaryGroupEntries(int)
	 */
	public synchronized int addSecondaryMenuGroup(String groupTitle,
			RibbonApplicationMenuEntrySecondary... entries) {
		this.groupTitles.add(groupTitle);
		List<RibbonApplicationMenuEntrySecondary> entryList = new ArrayList<RibbonApplicationMenuEntrySecondary>();
		this.groupEntries.add(entryList);
		for (RibbonApplicationMenuEntrySecondary entry : entries) {
			entryList.add(entry);
		}
		return this.groupTitles.size() - 1;
	}

	/**
	 * Returns the number of secondary menu groups of this primary menu entry.
	 * 
	 * @return The number of secondary menu groups of this primary menu entry.
	 * @see #addSecondaryMenuGroup(String,
	 *      RibbonApplicationMenuEntrySecondary...)
	 * @see #getSecondaryGroupTitleAt(int)
	 * @see #getSecondaryGroupEntries(int)
	 */
	public int getSecondaryGroupCount() {
		return this.groupTitles.size();
	}

	/**
	 * Returns the title of the secondary menu group at the specified index.
	 * 
	 * @param groupIndex
	 *            The index of a secondary menu group.
	 * @return The title of the secondary menu group at the specified index.
	 * @see #addSecondaryMenuGroup(String,
	 *      RibbonApplicationMenuEntrySecondary...)
	 * @see #getSecondaryGroupCount()
	 * @see #getSecondaryGroupEntries(int)
	 */
	public String getSecondaryGroupTitleAt(int groupIndex) {
		return this.groupTitles.get(groupIndex);
	}

	/**
	 * Returns an unmodifiable list of menu entries of the secondary menu group
	 * at the specified index.
	 * 
	 * @param groupIndex
	 *            The index of a secondary menu group.
	 * @return An unmodifiable list of menu entries of the secondary menu group
	 *         at the specified index.
	 * @see #addSecondaryMenuGroup(String,
	 *      RibbonApplicationMenuEntrySecondary...)
	 * @see #getSecondaryGroupCount()
	 * @see #getSecondaryGroupTitleAt(int)
	 */
	public List<RibbonApplicationMenuEntrySecondary> getSecondaryGroupEntries(
			int groupIndex) {
		return Collections.unmodifiableList(this.groupEntries.get(groupIndex));
	}

	/**
	 * Sets the rollover callback that allows the application to place custom
	 * content in the secondary panel of the {@link RibbonApplicationMenu} when
	 * this primary menu entry is activated.
	 * 
	 * @param rolloverCallback
	 *            The new rollover callback for populating the secondary panel
	 *            of the {@link RibbonApplicationMenu}.
	 * @see #getRolloverCallback()
	 */
	public void setRolloverCallback(PrimaryRolloverCallback rolloverCallback) {
		this.rolloverCallback = rolloverCallback;
	}

	/**
	 * Returns the current application callback that allows placing custom
	 * content in the secondary panel of the {@link RibbonApplicationMenu} when
	 * this primary menu entry is activated.
	 * 
	 * @return The current rollover callback for populating the secondary panel
	 *         of the {@link RibbonApplicationMenu}.
	 * @see #setRolloverCallback(PrimaryRolloverCallback)
	 */
	public PrimaryRolloverCallback getRolloverCallback() {
		return rolloverCallback;
	}

	/**
	 * Changes the title of the specified group.
	 * 
	 * @param groupIndex
	 *            Group index.
	 * @param newTitle
	 *            New title for the specified group.
	 */
	public synchronized void setSecondaryGroupTitle(int groupIndex,
			String newTitle) {
		this.groupTitles.set(groupIndex, newTitle);
	}
}
