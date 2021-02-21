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

import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback;

/**
 * Metadata description of the application menu of the {@link JRibbon}
 * component. The ribbon application menu has three parts:
 * 
 * <pre>
 * +-------------------------------------+
 * |           |                         |
 * |           |                         |
 * |  primary  |        secondary        |
 * |   area    |           area          |        
 * |           |                         |
 * |           |                         |
 * |-------------------------------------|
 * |            footer area              |
 * +-------------------------------------+
 * </pre>
 * 
 * <p>
 * The entries in the primary area are always visible. The secondary area
 * entries are shown based on the currently active element in the primary area.
 * There are three different types of primary entries:
 * </p>
 * 
 * <ul>
 * <li>Associated {@link ActionListener} passed to the constructor of the
 * {@link RibbonApplicationMenuEntryPrimary}. When this entry is armed (with
 * mouse rollover or via keyboard navigation), the contents of the secondary
 * area are cleared. The <code>Quit</code> menu item is an example of such a
 * primary menu entry.</li>
 * <li>Associated {@link PrimaryRolloverCallback} set by the
 * {@link RibbonApplicationMenuEntryPrimary#setRolloverCallback(PrimaryRolloverCallback)}
 * . When this entry is armed (with mouse rollover or via keyboard navigation),
 * the contents of the secondary area are populated by the application callback
 * implementation of
 * {@link PrimaryRolloverCallback#menuEntryActivated(javax.swing.JPanel)}. The
 * <code>Open</code> menu item is an example of such a primary menu entry,
 * showing a list of recently opened files.</li>
 * <li>Associated list of {@link RibbonApplicationMenuEntrySecondary}s added
 * with the
 * {@link RibbonApplicationMenuEntryPrimary#addSecondaryMenuGroup(String, RibbonApplicationMenuEntrySecondary...)}
 * API. When this entry is armed (with mouse rollover or via keyboard
 * navigation), the secondary area shows menu buttons for the registered
 * secondary menu entries. The <code>Save As</code> menu item is an example of
 * such a primary menu item, showing a list of default save formats.</li>
 * </ul>
 * 
 * <p>
 * At runtime, the application menu entries are implemented as
 * {@link JCommandMenuButton}, but the application code does not operate on that
 * level. Instead, the application code creates metadata-driven description of
 * the ribbon application menu, and that description is used to create and
 * populate the "real" controls of the application menu popup.
 * </p>
 * 
 * <p>
 * Note that once a {@link RibbonApplicationMenu} is set on the {@link JRibbon}
 * with the {@link JRibbon#setApplicationMenu(RibbonApplicationMenu)}, its
 * contents cannot be changed. An {@link IllegalStateException} will be thrown
 * from {@link #addMenuEntry(RibbonApplicationMenuEntryPrimary)} and
 * {@link #addFooterEntry(RibbonApplicationMenuEntryFooter)}.
 * </p>
 * 
 * @author Kirill Grouchnikov
 */
public class RibbonApplicationMenu {
	/**
	 * Indicates whether this ribbon application menu has been set on the
	 * {@link JRibbon} with the
	 * {@link JRibbon#setApplicationMenu(RibbonApplicationMenu)}. Once that API
	 * is called, the contents of this menu cannot be changed. An
	 * {@link IllegalStateException} will be thrown from
	 * {@link #addMenuEntry(RibbonApplicationMenuEntryPrimary)} and
	 * {@link #addFooterEntry(RibbonApplicationMenuEntryFooter)}.
	 * 
	 * @see #setFrozen()
	 * @see #addMenuEntry(RibbonApplicationMenuEntryPrimary)
	 * @see #addFooterEntry(RibbonApplicationMenuEntryFooter)
	 */
	private boolean isFrozen;

	/**
	 * Primary menu entries.
	 */
	private List<List<RibbonApplicationMenuEntryPrimary>> primaryEntries;

	/**
	 * Footer menu entries.
	 */
	private List<RibbonApplicationMenuEntryFooter> footerEntries;

	/**
	 * The default callback to be called when:
	 * 
	 * <ul>
	 * <li>The ribbon application menu is first shown.</li>
	 * <li>The currently active (rollover) primary application menu entry has no
	 * secondary menu entries and no associated rollover callback.
	 * </ul>
	 */
	private PrimaryRolloverCallback defaultCallback;

	/**
	 * Creates an empty ribbon application menu.
	 */
	public RibbonApplicationMenu() {
		this.primaryEntries = new ArrayList<List<RibbonApplicationMenuEntryPrimary>>();
		this.primaryEntries
				.add(new ArrayList<RibbonApplicationMenuEntryPrimary>());
		this.footerEntries = new ArrayList<RibbonApplicationMenuEntryFooter>();
	}

	/**
	 * Adds the specified primary menu entry.
	 * 
	 * @param entry
	 *            Primary menu entry to add.
	 * @throws IllegalStateException
	 *             if this ribbon application menu has already been set on the
	 *             {@link JRibbon} with the
	 *             {@link JRibbon#setApplicationMenu(RibbonApplicationMenu)}.
	 * @see #getPrimaryEntries()
	 * @see #addFooterEntry(RibbonApplicationMenuEntryFooter)
	 */
	public synchronized void addMenuEntry(
			RibbonApplicationMenuEntryPrimary entry) {
		if (this.isFrozen) {
			throw new IllegalStateException(
					"Cannot add menu entries after the menu has been set on the ribbon");
		}
		this.primaryEntries.get(this.primaryEntries.size() - 1).add(entry);
	}

	public synchronized void addMenuSeparator() {
		if (this.isFrozen) {
			throw new IllegalStateException(
					"Cannot add menu entries after the menu has been set on the ribbon");
		}
		this.primaryEntries
				.add(new ArrayList<RibbonApplicationMenuEntryPrimary>());
	}

	/**
	 * Returns an unmodifiable list of all primary menu entries of this
	 * application menu. The result is guaranteed to be non-<code>null</code>.
	 * 
	 * @return An unmodifiable list of all primary menu entries of this
	 *         application menu.
	 * @see #addMenuEntry(RibbonApplicationMenuEntryPrimary)
	 * @see #getFooterEntries()
	 */
	public List<List<RibbonApplicationMenuEntryPrimary>> getPrimaryEntries() {
		return Collections.unmodifiableList(this.primaryEntries);
	}

	/**
	 * Adds the specified footer menu entry.
	 * 
	 * @param entry
	 *            Footer menu entry to add.
	 * @throws IllegalStateException
	 *             if this ribbon application menu has already been set on the
	 *             {@link JRibbon} with the
	 *             {@link JRibbon#setApplicationMenu(RibbonApplicationMenu)}.
	 * @see #getFooterEntries()
	 * @see #addMenuEntry(RibbonApplicationMenuEntryPrimary)
	 */
	public synchronized void addFooterEntry(
			RibbonApplicationMenuEntryFooter entry) {
		if (this.isFrozen) {
			throw new IllegalStateException(
					"Cannot add footer entries after the menu has been set on the ribbon");
		}
		this.footerEntries.add(entry);
	}

	/**
	 * Returns an unmodifiable list of all footer menu entries of this
	 * application menu. The result is guaranteed to be non-<code>null</code>.
	 * 
	 * @return An unmodifiable list of all footer menu entries of this
	 *         application menu.
	 * @see #addFooterEntry(RibbonApplicationMenuEntryFooter)
	 * @see #getPrimaryEntries()
	 */
	public List<RibbonApplicationMenuEntryFooter> getFooterEntries() {
		return Collections.unmodifiableList(this.footerEntries);
	}

	/**
	 * Sets the default callback to be called when:
	 * 
	 * <ul>
	 * <li>The ribbon application menu is first shown.</li>
	 * <li>The currently active (rollover) primary application menu entry has no
	 * secondary menu entries and no associated rollover callback.
	 * </ul>
	 * 
	 * @param defaultCallback
	 *            Default callback.
	 */
	public void setDefaultCallback(PrimaryRolloverCallback defaultCallback) {
		this.defaultCallback = defaultCallback;
	}

	/**
	 * Returns the default callback of this ribbon application menu.
	 * 
	 * @return The default callback of this ribbon application menu.
	 * @see #setDefaultCallback(org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback)
	 */
	public PrimaryRolloverCallback getDefaultCallback() {
		return defaultCallback;
	}

	/**
	 * Marks this application menu as frozen. Subsequent calls to
	 * {@link #addMenuEntry(RibbonApplicationMenuEntryPrimary)} and
	 * {@link #addFooterEntry(RibbonApplicationMenuEntryFooter)} will throw an
	 * {@link IllegalStateException}.
	 * 
	 * @see #addMenuEntry(RibbonApplicationMenuEntryPrimary)
	 * @see #addFooterEntry(RibbonApplicationMenuEntryFooter)
	 * @see JRibbon#setApplicationMenu(RibbonApplicationMenu)
	 */
	synchronized void setFrozen() {
		this.isFrozen = true;
		if (this.primaryEntries.get(this.primaryEntries.size() - 1).isEmpty()) {
			this.primaryEntries.remove(this.primaryEntries.size() - 1);
		}
	}
}
