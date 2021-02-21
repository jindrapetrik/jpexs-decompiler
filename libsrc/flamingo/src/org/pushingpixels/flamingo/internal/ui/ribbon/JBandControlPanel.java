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

import java.util.*;

import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.ribbon.*;

/**
 * Control panel of a single {@link JRibbonBand}. This class is for internal use
 * only and should not be directly used by the applications.
 * 
 * @author Kirill Grouchnikov
 */
public class JBandControlPanel extends AbstractBandControlPanel implements
		UIResource {
	public static class ControlPanelGroup {
		public String groupTitle;

		/**
		 * Indication whether <code>this</code> control panel has galleries.
		 */
		private boolean hasGalleries;

		/**
		 * Number of galleries in <code>this</code> control panel.
		 */
		private int galleryCount;

		/**
		 * Mapping from priority to galleries.
		 */
		private Map<RibbonElementPriority, List<JRibbonGallery>> ribbonGalleries;

		/**
		 * Mapping from gallery to priority.
		 */
		private Map<JRibbonGallery, RibbonElementPriority> ribbonGalleriesPriorities;

		/**
		 * Mapping from priority to ribbon buttons.
		 */
		private Map<RibbonElementPriority, List<AbstractCommandButton>> ribbonButtons;

		/**
		 * Mapping from ribbon button to priority.
		 */
		private Map<AbstractCommandButton, RibbonElementPriority> ribbonButtonsPriorities;

		private List<JRibbonComponent> coreComps;

		private Map<JRibbonComponent, Integer> coreCompRowSpans;

		public ControlPanelGroup(String groupTitle) {
			this.groupTitle = groupTitle;
			this.ribbonButtons = new HashMap<RibbonElementPriority, java.util.List<AbstractCommandButton>>();
			this.ribbonButtonsPriorities = new HashMap<AbstractCommandButton, RibbonElementPriority>();
			this.ribbonGalleries = new HashMap<RibbonElementPriority, java.util.List<JRibbonGallery>>();
			this.ribbonGalleriesPriorities = new HashMap<JRibbonGallery, RibbonElementPriority>();
			this.hasGalleries = false;
			this.galleryCount = 0;
			this.coreComps = new ArrayList<JRibbonComponent>();
			this.coreCompRowSpans = new HashMap<JRibbonComponent, Integer>();
		}

		public String getGroupTitle() {
			return groupTitle;
		}

		public void setGroupTitle(String newTitle) {
			if ((this.groupTitle == null) && (newTitle != null)) {
				throw new IllegalArgumentException(
						"Cannot set a title for an unnamed group");
			}
			if ((this.groupTitle != null) && (newTitle == null)) {
				throw new IllegalArgumentException(
						"Cannot remove a title from a named group");
			}
			this.groupTitle = newTitle;
		}

		public boolean isCoreContent() {
			return !this.coreComps.isEmpty();
		}

		/**
		 * Adds a new ribbon button to <code>this</code> control panel.
		 * 
		 * @param ribbonButton
		 *            Ribbon button to add.
		 * @param priority
		 *            Ribbon button priority.
		 */
		public synchronized void addCommandButton(
				AbstractCommandButton ribbonButton,
				RibbonElementPriority priority) {
			if (this.groupTitle != null) {
				throw new UnsupportedOperationException(
						"Can't add command buttons to ribbon band group with non-null title");
			}
			if (this.isCoreContent()) {
				throw new UnsupportedOperationException(
						"Ribbon band groups do not support mixing JRibbonComponents and custom Flamingo components");
			}
			if (!this.ribbonButtons.containsKey(priority)) {
				this.ribbonButtons.put(priority,
						new LinkedList<AbstractCommandButton>());
			}
			List<AbstractCommandButton> al = this.ribbonButtons.get(priority);
			al.add(ribbonButton);

			this.ribbonButtonsPriorities.put(ribbonButton, priority);

			ribbonButton.setDisplayState(CommandButtonDisplayState.BIG);
		}

		/**
		 * Adds a new in-ribbon gallery to <code>this</code> control panel.
		 * 
		 * @param ribbonGallery
		 *            Ribbon gallery to add.
		 * @param priority
		 *            Ribbon gallery priority.
		 */
		public synchronized void addRibbonGallery(JRibbonGallery ribbonGallery,
				RibbonElementPriority priority) {
			if (this.groupTitle != null) {
				throw new UnsupportedOperationException(
						"Can't add galleries to ribbon band group with non-null title");
			}
			if (this.isCoreContent()) {
				throw new UnsupportedOperationException(
						"Ribbon band groups do not support mixing JRibbonComponents and custom Flamingo components");
			}
			// check the name
			if (!this.ribbonGalleries.containsKey(priority)) {
				this.ribbonGalleries.put(priority,
						new LinkedList<JRibbonGallery>());
			}
			List<JRibbonGallery> al = this.ribbonGalleries.get(priority);
			al.add(ribbonGallery);

			this.ribbonGalleriesPriorities.put(ribbonGallery, priority);

			ribbonGallery.setDisplayPriority(RibbonElementPriority.TOP);

			this.hasGalleries = true;
			this.galleryCount++;
		}

		/**
		 * Sets new priority of a ribbon button in <code>this</code> control
		 * panel.
		 * 
		 * @param ribbonButton
		 *            Gallery button.
		 * @param newPriority
		 *            New priority for the specified ribbon button.
		 */
		public synchronized void setPriority(JCommandButton ribbonButton,
				RibbonElementPriority newPriority) {
			RibbonElementPriority oldPriority = this.ribbonButtonsPriorities
					.get(ribbonButton);
			if (newPriority == oldPriority)
				return;

			this.ribbonButtons.get(oldPriority).remove(ribbonButton);

			if (!this.ribbonButtons.containsKey(newPriority)) {
				this.ribbonButtons.put(newPriority,
						new ArrayList<AbstractCommandButton>());
			}
			this.ribbonButtons.get(newPriority).add(ribbonButton);
		}

		/**
		 * Sets new priority of an in-ribbon gallery in <code>this</code>
		 * control panel.
		 * 
		 * @param ribbonGallery
		 *            In-ribbon gallery.
		 * @param newPriority
		 *            New priority for the specified in-ribbon gallery.
		 */
		public synchronized void setPriority(JRibbonGallery ribbonGallery,
				RibbonElementPriority newPriority) {
			RibbonElementPriority oldPriority = this.ribbonGalleriesPriorities
					.get(ribbonGallery);
			if (newPriority == oldPriority)
				return;

			this.ribbonGalleries.get(oldPriority).remove(ribbonGallery);

			if (!this.ribbonGalleries.containsKey(newPriority)) {
				this.ribbonGalleries.put(newPriority,
						new ArrayList<JRibbonGallery>());
			}
			this.ribbonGalleries.get(newPriority).add(ribbonGallery);
		}

		public void addRibbonComponent(JRibbonComponent comp, int rowSpan) {
			if (!this.ribbonButtonsPriorities.isEmpty()
					|| !this.ribbonGalleries.isEmpty()) {
				throw new UnsupportedOperationException(
						"Ribbon band groups do not support mixing JRibbonComponents and custom Flamingo components");
			}
			comp.setOpaque(false);
			this.coreComps.add(comp);
			this.coreCompRowSpans.put(comp, rowSpan);
		}

		/**
		 * Retrieves all ribbon buttons of specified priority from
		 * <code>this</code> control panel.
		 * 
		 * @param priority
		 *            Priority.
		 * @return All ribbon buttons of specified priority from
		 *         <code>this</code> control panel.
		 */
		public List<AbstractCommandButton> getRibbonButtons(
				RibbonElementPriority priority) {
			List<AbstractCommandButton> result = this.ribbonButtons
					.get(priority);
			if (result == null)
				return EMPTY_GALLERY_BUTTONS_LIST;
			return result;
		}

		/**
		 * Retrieves all in-ribbon galleries of specified priority from
		 * <code>this</code> control panel.
		 * 
		 * @param priority
		 *            Priority.
		 * @return All in-ribbon galleries of specified priority from
		 *         <code>this</code> control panel.
		 */
		public List<JRibbonGallery> getRibbonGalleries(
				RibbonElementPriority priority) {
			List<JRibbonGallery> result = this.ribbonGalleries.get(priority);
			if (result == null)
				return EMPTY_RIBBON_GALLERIES_LIST;
			return result;
		}

		/**
		 * Returns indication whether <code>this</code> control panel has
		 * in-ribbon galleries.
		 * 
		 * @return <code>true</code> if <code>this</code> control panel has
		 *         in-ribbon galleries, <code>false</code> otherwise.
		 */
		public boolean hasRibbonGalleries() {
			return this.hasGalleries;
		}

		/**
		 * Returns the number of in-ribbon galleries in <code>this</code>
		 * control panel.
		 * 
		 * @return Number of in-ribbon galleries in <code>this</code> control
		 *         panel.
		 */
		public int getRibbonGalleriesCount() {
			return this.galleryCount;
		}

		public List<JRibbonComponent> getRibbonComps() {
			return this.coreComps;
		}

		public Map<JRibbonComponent, Integer> getRibbonCompsRowSpans() {
			return this.coreCompRowSpans;
		}
	}

	/**
	 * Maps from gallery name to gallery.
	 */
	private Map<String, JRibbonGallery> galleryNameMap;

	private LinkedList<ControlPanelGroup> controlPanelGroups;

	/**
	 * Empty list of buttons.
	 */
	public static final List<AbstractCommandButton> EMPTY_GALLERY_BUTTONS_LIST = new LinkedList<AbstractCommandButton>();

	/**
	 * Empty list of galleries.
	 */
	public static final List<JRibbonGallery> EMPTY_RIBBON_GALLERIES_LIST = new LinkedList<JRibbonGallery>();

	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "BandControlPanelUI";

	/**
	 * Creates a control panel for specified ribbon band.
	 * 
	 */
	public JBandControlPanel() {
		super();

		this.controlPanelGroups = new LinkedList<ControlPanelGroup>();
		this.galleryNameMap = new HashMap<String, JRibbonGallery>();
	}

	/**
	 * Sets the new UI delegate.
	 * 
	 * @param ui
	 *            New UI delegate.
	 */
	public void setUI(BandControlPanelUI ui) {
		super.setUI(ui);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((BandControlPanelUI) UIManager.getUI(this));
		} else {
			setUI(new BasicBandControlPanelUI());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#getUI()
	 */
	@Override
	public BandControlPanelUI getUI() {
		return (BandControlPanelUI) ui;
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

	/**
	 * Adds a new ribbon button to <code>this</code> control panel.
	 * 
	 * @param ribbonButton
	 *            Ribbon button to add.
	 * @param priority
	 *            Ribbon button priority.
	 */
	public synchronized void addCommandButton(
			AbstractCommandButton ribbonButton, RibbonElementPriority priority) {
		if (this.controlPanelGroups.size() == 0)
			this.startGroup();

		this.controlPanelGroups.getLast().addCommandButton(ribbonButton,
				priority);

		super.add(ribbonButton);
	}

	/**
	 * Adds a new in-ribbon gallery to <code>this</code> control panel.
	 * 
	 * @param ribbonGallery
	 *            Ribbon gallery to add.
	 * @param priority
	 *            Ribbon gallery priority.
	 */
	public synchronized void addRibbonGallery(JRibbonGallery ribbonGallery,
			RibbonElementPriority priority) {
		// check the name
		String galleryName = ribbonGallery.getName();
		if ((galleryName == null) || (galleryName.isEmpty())) {
			throw new IllegalArgumentException(
					"Ribbon gallery name null or empty");
		}
		if (this.galleryNameMap.containsKey(galleryName)) {
			throw new IllegalArgumentException(
					"Another riboon gallery with the same name already exists");
		}

		if (this.controlPanelGroups.size() == 0)
			this.startGroup();

		this.controlPanelGroups.getLast().addRibbonGallery(ribbonGallery,
				priority);

		this.galleryNameMap.put(galleryName, ribbonGallery);

		super.add(ribbonGallery);
	}

	/**
	 * Sets new priority of a ribbon button in <code>this</code> control panel.
	 * 
	 * @param ribbonButton
	 *            Gallery button.
	 * @param newPriority
	 *            New priority for the specified ribbon button.
	 */
	public synchronized void setPriority(JCommandButton ribbonButton,
			RibbonElementPriority newPriority) {
		if (this.controlPanelGroups.size() == 0)
			this.startGroup();

		this.controlPanelGroups.getLast()
				.setPriority(ribbonButton, newPriority);
	}

	/**
	 * Sets new priority of an in-ribbon gallery in <code>this</code> control
	 * panel.
	 * 
	 * @param ribbonGallery
	 *            In-ribbon gallery.
	 * @param newPriority
	 *            New priority for the specified in-ribbon gallery.
	 */
	public synchronized void setPriority(JRibbonGallery ribbonGallery,
			RibbonElementPriority newPriority) {
		if (this.controlPanelGroups.size() == 0)
			this.startGroup();

		this.controlPanelGroups.getLast().setPriority(ribbonGallery,
				newPriority);
	}

	public void addRibbonComponent(JRibbonComponent comp) {
		this.addRibbonComponent(comp, 1);
	}

	public void addRibbonComponent(JRibbonComponent comp, int rowSpan) {
		if (this.controlPanelGroups.size() == 0)
			this.startGroup();

		this.controlPanelGroups.getLast().addRibbonComponent(comp, rowSpan);
		super.add(comp);
	}

	public List<ControlPanelGroup> getControlPanelGroups() {
		return Collections.unmodifiableList(this.controlPanelGroups);
	}

	public int getControlPanelGroupCount() {
		if (this.controlPanelGroups == null)
			return 1;
		return this.controlPanelGroups.size();
	}

	public String getControlPanelGroupTitle(int controlPanelGroupIndex) {
		if (this.controlPanelGroups == null)
			return null;
		return this.controlPanelGroups.get(controlPanelGroupIndex).groupTitle;
	}

	public int startGroup() {
		return this.startGroup(null);
	}

	public int startGroup(String groupTitle) {
		ControlPanelGroup controlPanelGroup = new ControlPanelGroup(groupTitle);
		this.controlPanelGroups.addLast(controlPanelGroup);
		this.fireChanged();
		return this.controlPanelGroups.size() - 1;
	}

	public void setGroupTitle(int groupIndex, String groupTitle) {
		this.controlPanelGroups.get(groupIndex).setGroupTitle(groupTitle);
		this.fireChanged();
	}

	/**
	 * Returns the ribbon gallery based on its name.
	 * 
	 * @param galleryName
	 *            Ribbon gallery name.
	 * @return Ribbon gallery.
	 */
	public JRibbonGallery getRibbonGallery(String galleryName) {
		return this.galleryNameMap.get(galleryName);
	}

	public void addChangeListener(ChangeListener l) {
		this.listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		this.listenerList.remove(ChangeListener.class, l);
	}

	protected void fireChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		ChangeEvent ce = new ChangeEvent(this);
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1]).stateChanged(ce);
			}
		}
	}

	public List<JRibbonComponent> getRibbonComponents(int groupIndex) {
		return Collections.unmodifiableList(this.controlPanelGroups.get(
				groupIndex).getRibbonComps());
	}
}
