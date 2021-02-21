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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.internal.ui.common.popup.BasicCommandPopupMenuUI;
import org.pushingpixels.flamingo.internal.ui.common.popup.PopupPanelUI;

/**
 * Popup menu. Can host any number of command menu buttons added with
 * {@link #addMenuButton(JCommandMenuButton)} separated with optional
 * {@link #addMenuSeparator()}. The
 * {@link #JCommandPopupMenu(JCommandButtonPanel, int, int)} constructor allows
 * placing a scrollable command button panel in the top part of the popup menu.
 * 
 * @author Kirill Grouchnikov
 */
public class JCommandPopupMenu extends JPopupPanel {
	/**
	 * @see #getUIClassID
	 */
	public static final String uiClassID = "CommandPopupMenuUI";

	/**
	 * The main button panel. Can be <code>null</code> if this command popup
	 * menu was created with the {@link #JCommandPopupMenu()} constructor.
	 * 
	 * @see #JCommandPopupMenu(JCommandButtonPanel, int, int)
	 * @see #hasCommandButtonPanel()
	 * @see #getMainButtonPanel()
	 */
	protected JCommandButtonPanel mainButtonPanel;

	/**
	 * Menu components. This list holds:
	 * <ul>
	 * <li>{@link JCommandMenuButton}s added with
	 * {@link #addMenuButton(JCommandMenuButton)}</li>
	 * <li>{@link JCommandToggleMenuButton}s added with
	 * {@link #addMenuButton(JCommandToggleMenuButton)}</li>
	 * <li>{@link Separator}s added with {@link #addMenuSeparator()}</li>
	 * <li>{@link JPanel}s added by the subclasses with
	 * {@link #addMenuPanel(JPanel)}</li>
	 * </ul>
	 * 
	 * @see #addMenuButton(JCommandMenuButton)
	 * @see #addMenuButton(JCommandToggleMenuButton)
	 * @see #addMenuSeparator()
	 * @see #addMenuPanel(JPanel)
	 * @see #getMenuComponents()
	 */
	protected java.util.List<Component> menuComponents;

	/**
	 * Maximum number of button columns visible in the {@link #mainButtonPanel}.
	 * 
	 * @see #JCommandPopupMenu(JCommandButtonPanel, int, int)
	 * @see #getMaxButtonColumns()
	 */
	protected int maxButtonColumns;

	/**
	 * Maximum number of button rows visible in the {@link #mainButtonPanel}.
	 * 
	 * @see #JCommandPopupMenu(JCommandButtonPanel, int, int)
	 * @see #getMaxVisibleButtonRows()
	 */
	protected int maxVisibleButtonRows;

	/**
	 * Maximum number of menu items visible in this menu. If more buttons are
	 * added with the {@link #addMenuButton(JCommandMenuButton)} and
	 * {@link #addMenuButton(JCommandToggleMenuButton)} APIs, the menu part will
	 * show scroller buttons above the first and below the last menu button. If
	 * the value is negative, there is no limitation on how many menu buttons
	 * are shown, and the entire popup menu can overflow the monitor edges.
	 */
	protected int maxVisibleMenuButtons;

	private boolean toDismissOnChildClick;

	/**
	 * Creates an empty popup menu with no button panel.
	 */
	public JCommandPopupMenu() {
		this.menuComponents = new ArrayList<Component>();

		this.maxVisibleMenuButtons = -1;
		this.toDismissOnChildClick = true;
	}

	/**
	 * Creates a popup menu hosting the specified button panel.
	 * 
	 * @param buttonPanel
	 *            Fully constructed button panel.
	 * @param maxButtonColumns
	 *            Maximum number of button columns visible in
	 *            <code>buttonPanel</code>.
	 * @param maxVisibleButtonRows
	 *            Maximum number of button rows visible in
	 *            <code>buttonPanel</code>.
	 */
	public JCommandPopupMenu(JCommandButtonPanel buttonPanel,
			int maxButtonColumns, int maxVisibleButtonRows) {
		this();

		this.mainButtonPanel = buttonPanel;
		this.maxButtonColumns = maxButtonColumns;
		this.maxVisibleButtonRows = maxVisibleButtonRows;

		this.updateUI();
	}

	/**
	 * Adds the specified menu button to this menu.
	 * 
	 * @param menuButton
	 *            Menu button to add.
	 */
	public void addMenuButton(JCommandMenuButton menuButton) {
		menuButton.setHorizontalAlignment(SwingUtilities.LEFT);
		this.menuComponents.add(menuButton);
		this.fireStateChanged();
	}

	/**
	 * Adds the specified toggle menu button to this menu.
	 * 
	 * @param menuButton
	 *            Menu button to add.
	 */
	public void addMenuButton(JCommandToggleMenuButton menuButton) {
		menuButton.setHorizontalAlignment(SwingUtilities.LEFT);
		this.menuComponents.add(menuButton);
		this.fireStateChanged();
	}

	/**
	 * Adds a menu separator to this menu.
	 */
	public void addMenuSeparator() {
		this.menuComponents.add(new JPopupMenu.Separator());
		this.fireStateChanged();
	}

	/**
	 * Adds a menu panel to this menu.
	 * 
	 * @param menuPanel
	 *            Menu panel to add.
	 */
	protected void addMenuPanel(JPanel menuPanel) {
		if (this.maxVisibleMenuButtons > 0) {
			throw new IllegalStateException(
					"This method is not supported on menu that contains a command button panel");
		}
		this.menuComponents.add(menuPanel);
		this.fireStateChanged();
	}

	/**
	 * Returns indication whether this menu has a command button panel.
	 * 
	 * @return <code>true</code> if this menu has a command button panel,
	 *         <code>false</code> otherwise.
	 * @see #getMainButtonPanel()
	 */
	public boolean hasCommandButtonPanel() {
		return (this.mainButtonPanel != null);
	}

	/**
	 * Returns the command button panel of this menu. Can return
	 * <code>null</code>.
	 * 
	 * @return The command button panel of this menu.
	 * @see #hasCommandButtonPanel()
	 */
	public JCommandButtonPanel getMainButtonPanel() {
		return this.mainButtonPanel;
	}

	/**
	 * Returns an unmodifiable list of all the menu components. Can return
	 * <code>null</code>.
	 * 
	 * @return An unmodifiable list of all the menu components
	 */
	public java.util.List<Component> getMenuComponents() {
		if (this.menuComponents == null)
			return null;
		return Collections.unmodifiableList(this.menuComponents);
	}

	/**
	 * Returns the maximum number of button columns visible in the command
	 * button panel of this menu. If this menu has been created with the
	 * {@link #JCommandPopupMenu()} constructor, zero is returned.
	 * 
	 * @return The maximum number of button columns visible in the command
	 *         button panel of this menu.
	 * @see #JCommandPopupMenu(JCommandButtonPanel, int, int)
	 * @see #getMaxVisibleButtonRows()
	 */
	public int getMaxButtonColumns() {
		return this.maxButtonColumns;
	}

	/**
	 * Returns the maximum number of button rows visible in the command button
	 * panel of this menu. If this menu has been created with the
	 * {@link #JCommandPopupMenu()} constructor, zero is returned.
	 * 
	 * @return The maximum number of button rows visible in the command button
	 *         panel of this menu.
	 * @see #JCommandPopupMenu(JCommandButtonPanel, int, int)
	 * @see #getMaxButtonColumns()
	 */
	public int getMaxVisibleButtonRows() {
		return this.maxVisibleButtonRows;
	}

	/**
	 * Returns the maximum number of menu items visible in this menu.
	 * 
	 * @return The maximum number of menu items visible in this menu. If the
	 *         value is negative, there is no limitation on how many menu
	 *         buttons are shown, and the entire popup menu can overflow the
	 *         monitor edges.
	 */
	public int getMaxVisibleMenuButtons() {
		return this.maxVisibleMenuButtons;
	}

	/**
	 * Sets the maximum number of menu items visible in this menu. If the value
	 * is negative, there is no limitation on how many menu buttons are shown,
	 * and the entire popup menu can overflow the monitor edges.
	 * 
	 * @param maxVisibleMenuButtons
	 *            The new value for the maximum number of menu items visible in
	 *            this menu.
	 */
	public void setMaxVisibleMenuButtons(int maxVisibleMenuButtons) {
		for (Component menuComp : this.menuComponents) {
			if (menuComp instanceof JPanel) {
				throw new IllegalStateException(
						"This method is not supported on menus with panels");
			}
		}

		int old = this.maxVisibleMenuButtons;
		this.maxVisibleMenuButtons = maxVisibleMenuButtons;

		if (old != this.maxVisibleMenuButtons) {
			this.firePropertyChange("maxVisibleMenuButtons", old,
					this.maxVisibleMenuButtons);
		}
	}

	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((PopupPanelUI) UIManager.getUI(this));
		} else {
			setUI(BasicCommandPopupMenuUI.createUI(this));
		}
	}

	/**
	 * Adds the specified change listener to track changes to this popup menu.
	 * 
	 * @param l
	 *            Change listener to add.
	 * @see #removeChangeListener(ChangeListener)
	 */
	public void addChangeListener(ChangeListener l) {
		this.listenerList.add(ChangeListener.class, l);
	}

	/**
	 * Removes the specified change listener from tracking changes to this popup
	 * menu.
	 * 
	 * @param l
	 *            Change listener to remove.
	 * @see #addChangeListener(ChangeListener)
	 */
	public void removeChangeListener(ChangeListener l) {
		this.listenerList.remove(ChangeListener.class, l);
	}

	/**
	 * Notifies all registered listener that the state of this popup menu has
	 * changed.
	 */
	protected void fireStateChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = this.listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		ChangeEvent event = new ChangeEvent(this);
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1]).stateChanged(event);
			}
		}
	}

	public boolean isToDismissOnChildClick() {
		return toDismissOnChildClick;
	}

	public void setToDismissOnChildClick(boolean toDismissOnChildClick) {
		this.toDismissOnChildClick = toDismissOnChildClick;
	}
}
