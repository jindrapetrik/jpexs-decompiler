/*
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
package org.pushingpixels.flamingo.internal.ui.ribbon;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.*;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand.RibbonGalleryPopupCallback;

/**
 * In-ribbon gallery. This class is for internal use only and should not be
 * directly used by the applications.
 * 
 * @author Kirill Grouchnikov
 * @see JRibbonBand#addRibbonGallery(String, List, Map, int, int,
 *      RibbonElementPriority)
 */
public class JRibbonGallery extends JComponent {
	/**
	 * The buttons of <code>this</code> gallery.
	 */
	protected List<JCommandToggleButton> buttons;

	/**
	 * Button group for ensuring that only one button is selected.
	 */
	protected CommandToggleButtonGroup buttonSelectionGroup;

	/**
	 * The current display priority of <code>this</code> in-ribbon gallery.
	 */
	protected RibbonElementPriority displayPriority;

	/**
	 * Preferred widths for each possible display state (set in the user code
	 * according to design preferences).
	 */
	protected Map<RibbonElementPriority, Integer> preferredVisibleIconCount;

	/**
	 * Gallery button groups.
	 */
	protected List<StringValuePair<List<JCommandToggleButton>>> buttonGroups;

	/**
	 * Preferred maximum number of button columns for the popup panel.
	 */
	protected int preferredPopupMaxButtonColumns;

	/**
	 * Preferred maximum number of visible button rows for the popup panel.
	 */
	protected int preferredPopupMaxVisibleButtonRows;

	/**
	 * Indication whether the ribbon gallery is showing the popup panel.
	 */
	protected boolean isShowingPopupPanel;

	protected RibbonGalleryPopupCallback popupCallback;

	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "RibbonGalleryUI";

	private String expandKeyTip;

	private CommandButtonDisplayState buttonDisplayState;

	/**
	 * Creates new in-ribbon gallery.
	 */
	public JRibbonGallery() {
		this.buttons = new ArrayList<JCommandToggleButton>();
		this.buttonSelectionGroup = new CommandToggleButtonGroup();
		this.buttonSelectionGroup
				.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (CommandToggleButtonGroup.SELECTED_PROPERTY
								.equals(evt.getPropertyName())) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									firePropertyChange("selectedButton", evt
											.getOldValue(), evt.getNewValue());
								}
							});
						}
					}
				});

		this.preferredVisibleIconCount = new HashMap<RibbonElementPriority, Integer>();
		// Initialize with some values. Application should provide real
		// widths using setPreferredWidth.
		for (RibbonElementPriority state : RibbonElementPriority.values())
			this.preferredVisibleIconCount.put(state, 100);

		this.isShowingPopupPanel = false;
		this.buttonDisplayState = JRibbonBand.BIG_FIXED_LANDSCAPE;

		this.updateUI();
	}

	/**
	 * Sets the new UI delegate.
	 * 
	 * @param ui
	 *            New UI delegate.
	 */
	public void setUI(RibbonGalleryUI ui) {
		super.setUI(ui);
	}

	/**
	 * Resets the UI property to a value from the current look and feel.
	 * 
	 * @see JComponent#updateUI
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((RibbonGalleryUI) UIManager.getUI(this));
		} else {
			setUI(new BasicRibbonGalleryUI());
		}
		//
		// if (this.popupPanel != null)
		// SwingUtilities.updateComponentTreeUI(this.popupPanel);
	}

	/**
	 * Returns the UI object which implements the L&F for this component.
	 * 
	 * @return a <code>RibbonGalleryUI</code> object
	 * @see #setUI(RibbonGalleryUI)
	 */
	public RibbonGalleryUI getUI() {
		return (RibbonGalleryUI) ui;
	}

	/**
	 * Returns the name of the UI class that implements the L&F for this
	 * component.
	 * 
	 * @return the string "RibbonGalleryUI"
	 * @see JComponent#getUIClassID
	 * @see UIDefaults#getUI(javax.swing.JComponent)
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	/**
	 * Adds new gallery button to <code>this</code> in-ribbon gallery.
	 * 
	 * @param buttonGroup
	 *            Button group.
	 * @param button
	 *            Gallery button to add.
	 */
	private void addGalleryButton(
			StringValuePair<List<JCommandToggleButton>> buttonGroup,
			JCommandToggleButton button) {
		String buttonGroupName = buttonGroup.getKey();
		// find the index to add
		int indexToAdd = 0;
		for (int i = 0; i < this.buttonGroups.size(); i++) {
			StringValuePair<List<JCommandToggleButton>> buttonGroupPair = this.buttonGroups
					.get(i);
			String currGroupName = buttonGroupPair.getKey();
			indexToAdd += buttonGroupPair.getValue().size();
			if ((currGroupName == null) && (buttonGroupName == null)) {
				break;
			}
			if (currGroupName.compareTo(buttonGroupName) == 0) {
				break;
			}
		}
		// System.out.println("Added " + button.getText() + " at " +
		// indexToAdd);
		this.buttons.add(indexToAdd, button);
		this.buttonSelectionGroup.add(button);
		buttonGroup.getValue().add(button);
		button.setDisplayState(this.buttonDisplayState);

		super.add(button);
	}

	/**
	 * Removes an existing gallery button from <code>this</code> in-ribbon
	 * gallery.
	 * 
	 * @param button
	 *            Gallery button to remove.
	 */
	private void removeGalleryButton(JCommandToggleButton button) {
		this.buttons.remove(button);
		this.buttonSelectionGroup.remove(button);

		super.remove(button);
	}

	/**
	 * Set preferred width of <code>this</code> in-ribbon gallery for the
	 * specified display state.
	 * 
	 * @param state
	 *            Display state.
	 * @param visibleButtonCount
	 *            Preferred width for the specified state.
	 */
	public void setPreferredVisibleButtonCount(RibbonElementPriority state,
			int visibleButtonCount) {
		this.preferredVisibleIconCount.put(state, visibleButtonCount);
	}

	/**
	 * Returns the preferred width of <code>this</code> in-ribbon gallery for
	 * the specified display state.
	 * 
	 * @param state
	 *            Display state.
	 * @param availableHeight
	 *            Available height in pixels.
	 * @return The preferred width of <code>this</code> in-ribbon gallery for
	 *         the specified display state.
	 */
	public int getPreferredWidth(RibbonElementPriority state,
			int availableHeight) {
		int preferredVisibleButtonCount = this.preferredVisibleIconCount
				.get(state);

		BasicRibbonGalleryUI ui = (BasicRibbonGalleryUI) this.getUI();
		return ui.getPreferredWidth(preferredVisibleButtonCount,
				availableHeight);
	}

	/**
	 * Sets new display priority for <code>this</code> in-ribbon gallery.
	 * 
	 * @param displayPriority
	 *            New display priority for <code>this</code> in-ribbon gallery.
	 */
	public void setDisplayPriority(RibbonElementPriority displayPriority) {
		this.displayPriority = displayPriority;
	}

	/**
	 * Returns the current display priority for <code>this</code> in-ribbon
	 * gallery.
	 * 
	 * @return The current display priority for <code>this</code> in-ribbon
	 *         gallery.
	 */
	public RibbonElementPriority getDisplayPriority() {
		return this.displayPriority;
	}

	/**
	 * Returns the number of button groups in <code>this</code> in-ribbon
	 * gallery.
	 * 
	 * @return The number of button groups in <code>this</code> in-ribbon
	 *         gallery.
	 */
	public int getButtonGroupCount() {
		return this.buttonGroups.size();
	}

	/**
	 * Returns the list of buttons in the specified button group.
	 * 
	 * @param buttonGroupName
	 *            Button group name.
	 * @return The list of buttons in the specified button group.
	 */
	public List<JCommandToggleButton> getButtonGroup(String buttonGroupName) {
		for (StringValuePair<List<JCommandToggleButton>> group : this.buttonGroups) {
			if (group.getKey().compareTo(buttonGroupName) == 0)
				return group.getValue();
		}
		return null;
	}

	/**
	 * Returns the number of gallery buttons in <code>this</code> in-ribbon
	 * gallery.
	 * 
	 * @return The number of gallery buttons in <code>this</code> in-ribbon
	 *         gallery.
	 */
	public int getButtonCount() {
		return this.buttons.size();
	}

	/**
	 * Returns the gallery button at specified index.
	 * 
	 * @param index
	 *            Gallery button index.
	 * @return Gallery button at specified index.
	 */
	public JCommandToggleButton getButtonAt(int index) {
		return this.buttons.get(index);
	}

	/**
	 * Returns the currently selected gallery button.
	 * 
	 * @return The currently selected gallery button.
	 */
	public JCommandToggleButton getSelectedButton() {
		return this.buttonSelectionGroup.getSelected();
	}

	/**
	 * Sets new value for the currently selected gallery button.
	 * 
	 * @param selectedButton
	 *            New value for the currently selected gallery button.
	 */
	public void setSelectedButton(JCommandToggleButton selectedButton) {
		this.buttonSelectionGroup.setSelected(selectedButton, true);
	}

	/**
	 * Returns the associated popup gallery.
	 * 
	 * @return The associated popup gallery.
	 */
	public JCommandButtonPanel getPopupButtonPanel() {
		JCommandButtonPanel buttonPanel = new JCommandButtonPanel(
				this.buttonDisplayState);
		buttonPanel.setMaxButtonColumns(this.preferredPopupMaxButtonColumns);
		buttonPanel.setToShowGroupLabels(true);
		for (StringValuePair<List<JCommandToggleButton>> buttonGroupEntry : this.buttonGroups) {
			String groupName = buttonGroupEntry.getKey();
			if (groupName == null) {
				buttonPanel.setToShowGroupLabels(false);
			}
			buttonPanel.addButtonGroup(groupName);
			for (JCommandToggleButton button : buttonGroupEntry.getValue()) {
				// set the button to visible (the gallery hides the buttons
				// that don't fit the front row).
				button.setVisible(true);
				buttonPanel.addButtonToLastGroup(button);
			}
		}
		// just to make sure that the button panel will not try to add
		// the buttons to its own button group
		buttonPanel.setSingleSelectionMode(true);
		return buttonPanel;
	}

	/**
	 * Sets indication whether the popup panel is showing.
	 * 
	 * @param isShowingPopupPanel
	 *            Indication whether the popup panel is showing.
	 */
	public void setShowingPopupPanel(boolean isShowingPopupPanel) {
		this.isShowingPopupPanel = isShowingPopupPanel;

		if (!isShowingPopupPanel) {
			// populate the ribbon gallery back
			for (StringValuePair<List<JCommandToggleButton>> buttonGroupEntry : this.buttonGroups) {
				for (JCommandToggleButton button : buttonGroupEntry.getValue()) {
					button.setDisplayState(this.buttonDisplayState);
					this.add(button);
				}
			}
			// and layout
			this.doLayout();
		}
	}

	/**
	 * Returns indication whether the popup panel is showing.
	 * 
	 * @return <code>true</code> if the popup panel is showing,
	 *         <code>false</code> otherwise.
	 */
	public boolean isShowingPopupPanel() {
		return this.isShowingPopupPanel;
	}

	/**
	 * Sets the button groups for this ribbon gallery.
	 * 
	 * @param buttons
	 *            Button groups.
	 */
	public void setGroupMapping(
			List<StringValuePair<List<JCommandToggleButton>>> buttons) {
		this.buttonGroups = new ArrayList<StringValuePair<List<JCommandToggleButton>>>();
		boolean hasGroupWithNullTitle = false;
		for (StringValuePair<List<JCommandToggleButton>> buttonGroupPair : buttons) {
			if (buttonGroupPair.getKey() == null) {
				if (hasGroupWithNullTitle) {
					throw new IllegalArgumentException(
							"Can't have more than one ribbon gallery group with null name");
				}
				hasGroupWithNullTitle = true;
			}

			// create the list of buttons for this group
			List<JCommandToggleButton> buttonGroupCopy = new ArrayList<JCommandToggleButton>();
			// add it to the groups list
			StringValuePair<List<JCommandToggleButton>> buttonGroupInfo = new StringValuePair<List<JCommandToggleButton>>(
					buttonGroupPair.getKey(), buttonGroupCopy);
			this.buttonGroups.add(buttonGroupInfo);
			// add all the buttons to the control
			for (JCommandToggleButton button : buttonGroupPair.getValue()) {
				this.addGalleryButton(buttonGroupInfo, button);
			}
		}
	}

	/**
	 * Adds toggle command buttons to the specified button group in this ribbon
	 * gallery.
	 * 
	 * @param buttonGroupName
	 *            Button group name.
	 * @param buttons
	 *            Toggle command buttons to add to the specified button group.
	 */
	public void addRibbonGalleryButtons(String buttonGroupName,
			JCommandToggleButton... buttons) {
		for (StringValuePair<List<JCommandToggleButton>> buttonGroup : this.buttonGroups) {
			if (buttonGroup.getKey().compareTo(buttonGroupName) == 0) {
				for (JCommandToggleButton button : buttons) {
					// buttonGroup.getValue().add(button);
					this.addGalleryButton(buttonGroup, button);
				}
				return;
			}
		}
		this.revalidate();
		this.doLayout();
	}

	/**
	 * Removes the specified toggle command buttons from this ribbon gallery.
	 * 
	 * @param buttons
	 *            Toggle command buttons to remove from this gallery.
	 */
	public void removeRibbonGalleryButtons(JCommandToggleButton... buttons) {
		for (StringValuePair<List<JCommandToggleButton>> buttonGroup : this.buttonGroups) {
			for (Iterator<JCommandToggleButton> it = buttonGroup.getValue()
					.iterator(); it.hasNext();) {
				JCommandToggleButton currButtonInGroup = it.next();
				for (JCommandToggleButton toRemove : buttons) {
					if (toRemove == currButtonInGroup) {
						it.remove();
						this.removeGalleryButton(toRemove);
					}
				}
			}
		}
		this.revalidate();
		this.doLayout();
	}

	/**
	 * Sets the preferred dimension of the popup panel.
	 * 
	 * @param preferredPopupMaxButtonColumns
	 *            Preferred maximum number of button columns for the popup
	 *            panel.
	 * @param preferredPopupMaxVisibleButtonRows
	 *            Preferred maximum number of visible button rows for the popup
	 *            panel.
	 */
	public void setPreferredPopupPanelDimension(
			int preferredPopupMaxButtonColumns,
			int preferredPopupMaxVisibleButtonRows) {
		this.preferredPopupMaxButtonColumns = preferredPopupMaxButtonColumns;
		this.preferredPopupMaxVisibleButtonRows = preferredPopupMaxVisibleButtonRows;
	}

	public void setPopupCallback(RibbonGalleryPopupCallback popupCallback) {
		this.popupCallback = popupCallback;
	}

	public RibbonGalleryPopupCallback getPopupCallback() {
		return popupCallback;
	}

	public int getPreferredPopupMaxButtonColumns() {
		return preferredPopupMaxButtonColumns;
	}

	public int getPreferredPopupMaxVisibleButtonRows() {
		return preferredPopupMaxVisibleButtonRows;
	}

	public void setExpandKeyTip(String expandKeyTip) {
		String old = this.expandKeyTip;
		this.expandKeyTip = expandKeyTip;
		this.firePropertyChange("expandKeyTip", old, this.expandKeyTip);
	}

	public String getExpandKeyTip() {
		return expandKeyTip;
	}

	public CommandButtonDisplayState getButtonDisplayState() {
		return this.buttonDisplayState;
	}

	public void setButtonDisplayState(
			CommandButtonDisplayState buttonDisplayState) {
		if (this.getButtonCount() > 0) {
			throw new IllegalStateException(
					"Cannot change button display state on ribbon gallery with existing buttons");
		}
		boolean isSupported = (buttonDisplayState == JRibbonBand.BIG_FIXED)
				|| (buttonDisplayState == CommandButtonDisplayState.SMALL)
				|| (buttonDisplayState == JRibbonBand.BIG_FIXED_LANDSCAPE);
		if (!isSupported) {
			throw new IllegalArgumentException("Display state "
					+ buttonDisplayState.getDisplayName()
					+ " is not supported in ribbon galleries");
		}
		if (!buttonDisplayState.equals(this.buttonDisplayState)) {
			CommandButtonDisplayState old = this.buttonDisplayState;
			this.buttonDisplayState = buttonDisplayState;

			for (JCommandToggleButton button : this.buttons)
				button.setDisplayState(buttonDisplayState);

			this.firePropertyChange("buttonDisplayState", old,
					this.buttonDisplayState);
		}
	}
}
