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
import java.awt.event.ComponentEvent;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.event.EventListenerList;

import org.pushingpixels.flamingo.api.common.JCommandButton;

/**
 * Manager for showing and hiding {@link JPopupPanel}s.
 * 
 * @author Kirill Grouchnikov
 */
public class PopupPanelManager {
	/**
	 * Listener on showing and hiding the popup panels.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static interface PopupListener extends EventListener {
		/**
		 * Fired when a popup panel has been shown.
		 * 
		 * @param event
		 *            Popup event.
		 */
		void popupShown(PopupEvent event);

		/**
		 * Fired when a popup panel has been hidden.
		 * 
		 * @param event
		 *            Popup event.
		 */
		void popupHidden(PopupEvent event);
	}

	/**
	 * Popup event.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class PopupEvent extends ComponentEvent {
		/**
		 * ID of "popup shown" event.
		 */
		public static final int POPUP_SHOWN = 100;

		/**
		 * ID of "popup hidden" event.
		 */
		public static final int POPUP_HIDDEN = 101;

		/**
		 * The popup originator component.
		 */
		private JComponent popupOriginator;

		/**
		 * Creates a new popup event.
		 * 
		 * @param source
		 *            Event source.
		 * @param id
		 *            Event ID.
		 * @param popupOriginator
		 *            Popup originator component.
		 */
		public PopupEvent(JPopupPanel source, int id, JComponent popupOriginator) {
			super(source, id);
			this.popupOriginator = popupOriginator;
		}

		/**
		 * Returns the popup originator component.
		 * 
		 * @return Popup originator component.
		 */
		public JComponent getPopupOriginator() {
			return this.popupOriginator;
		}
	}

	/**
	 * List of all registered listeners.
	 */
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * The singleton instance of popup panel manager.
	 */
	private static final PopupPanelManager instance = new PopupPanelManager();

	/**
	 * Information on a single showing popup.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class PopupInfo {
		/**
		 * The popup panel.
		 */
		private JPopupPanel popupPanel;

		/**
		 * The originating component.
		 */
		private JComponent popupOriginator;

		/**
		 * Creates a new information object.
		 * 
		 * @param popupOriginator
		 *            The originating component.
		 * @param popupPanel
		 *            The popup panel.
		 */
		public PopupInfo(JComponent popupOriginator, JPopupPanel popupPanel) {
			this.popupOriginator = popupOriginator;
			this.popupPanel = popupPanel;
		}

		/**
		 * Returns the popup panel.
		 * 
		 * @return The popup panel.
		 */
		public JPopupPanel getPopupPanel() {
			return this.popupPanel;
		}

		/**
		 * Returns the originating component.
		 * 
		 * @return The originating component.
		 */
		public JComponent getPopupOriginator() {
			return this.popupOriginator;
		}
	}

	/**
	 * Returns the default popup panel manager.
	 * 
	 * @return a PopupPanelManager object
	 */
	public static PopupPanelManager defaultManager() {
		return instance;
	}

	/**
	 * All currently shown popup panels.
	 */
	protected LinkedList<PopupInfo> shownPath = new LinkedList<PopupInfo>();

	/**
	 * Map of all popup panels and associated {@link Popup} objects.
	 */
	protected Map<JPopupPanel, Popup> popupPanels = new HashMap<JPopupPanel, Popup>();

	/**
	 * Adds new popup to the tracking structures.
	 * 
	 * @param popupOriginator
	 *            The originating component.
	 * @param popup
	 *            The new popup.
	 * @param popupInitiator
	 *            The initiator of the popup.
	 */
	public void addPopup(JComponent popupOriginator, Popup popup,
			JPopupPanel popupInitiator) {
		popupPanels.put(popupInitiator, popup);
		shownPath.addLast(new PopupInfo(popupOriginator, popupInitiator));
		popup.show();
		if (popupOriginator instanceof JCommandButton) {
			((JCommandButton) popupOriginator).getPopupModel().setPopupShowing(
					true);
		}
		this.firePopupShown(popupInitiator, popupOriginator);
	}

	/**
	 * Hides the last shown popup panel.
	 */
	public void hideLastPopup() {
		if (shownPath.size() == 0)
			return;
		PopupInfo last = shownPath.removeLast();
		Popup popup = popupPanels.get(last.popupPanel);
		popup.hide();
		popupPanels.remove(last.popupPanel);
		if (last.popupOriginator instanceof JCommandButton) {
			((JCommandButton) last.popupOriginator).getPopupModel()
					.setPopupShowing(false);
		}

		// KeyTipManager.defaultManager().showChainBefore(last.popupPanel);
		this.firePopupHidden(last.popupPanel, last.popupOriginator);
	}

	/**
	 * Hides all popup panels based on the specified component. We find the
	 * first ancestor of the specified component that is popup panel, and close
	 * all popup panels that were open from that popup panel. If the specified
	 * component is <code>null</code>, all popup panels are closed.
	 * 
	 * @param comp
	 *            Component.
	 */
	public void hidePopups(Component comp) {
		// System.out.println("Hiding all popups");
		// try {
		// throw new Exception();
		// }
		// catch (Exception exc) {
		// exc.printStackTrace(System.out);
		// System.out.println("At " + System.currentTimeMillis() + "\n");
		// }
		boolean foundAndDismissed = false;
		if (comp != null) {
			Component c = comp;
			// find JPopupGallery parent of the component
			while (c != null) {
				if (c instanceof JPopupPanel) {
					foundAndDismissed = true;
					// And close all popups that were opened
					// from the found popup panel
					while (shownPath.size() > 0) {
						if (shownPath.getLast().popupPanel == c)
							return;
						PopupInfo last = shownPath.removeLast();
						Popup popup = popupPanels.get(last.popupPanel);
						popup.hide();
						if (last.popupOriginator instanceof JCommandButton) {
							((JCommandButton) last.popupOriginator)
									.getPopupModel().setPopupShowing(false);
						}
						this.firePopupHidden(last.popupPanel,
								last.popupOriginator);
						popupPanels.remove(last.popupPanel);
					}
				}
				c = c.getParent();
			}
		}
		if (!foundAndDismissed || (comp == null)) {
			while (shownPath.size() > 0) {
				PopupInfo last = shownPath.removeLast();
				Popup popup = popupPanels.get(last.popupPanel);
				popup.hide();
				if (last.popupOriginator instanceof JCommandButton) {
					((JCommandButton) last.popupOriginator).getPopupModel()
							.setPopupShowing(false);
				}
				this.firePopupHidden(last.popupPanel, last.popupOriginator);
				popupPanels.remove(last.popupPanel);
			}
		}
	}

	/**
	 * Returns all currently shown popup panels.
	 * 
	 * @return All currently shown popup panels.
	 */
	public List<PopupInfo> getShownPath() {
		List<PopupInfo> toReturn = new ArrayList<PopupInfo>();
		for (PopupInfo pInfo : this.shownPath)
			toReturn.add(pInfo);
		return toReturn;
	}

	/**
	 * Adds the specified popup listener.
	 * 
	 * @param l
	 *            Listener to add.
	 */
	public void addPopupListener(PopupListener l) {
		this.listenerList.add(PopupListener.class, l);
	}

	/**
	 * Removes the specified popup listener.
	 * 
	 * @param l
	 *            Listener to remove.
	 */
	public void removePopupListener(PopupListener l) {
		this.listenerList.remove(PopupListener.class, l);
	}

	/**
	 * Fires an event on showing the specified popup panel.
	 * 
	 * @param panel
	 *            Popup panel that was shown.
	 * @param popupOriginator
	 *            The originating component.
	 */
	protected void firePopupShown(JPopupPanel panel, JComponent popupOriginator) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		PopupEvent popupEvent = new PopupEvent(panel, PopupEvent.POPUP_SHOWN,
				popupOriginator);
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PopupListener.class) {
				((PopupListener) listeners[i + 1]).popupShown(popupEvent);
			}
		}
	}

	/**
	 * Fires an event on hiding the specified popup panel.
	 * 
	 * @param panel
	 *            Popup panel that was hidden.
	 * @param popupOriginator
	 *            The originating component.
	 */
	protected void firePopupHidden(JPopupPanel panel, JComponent popupOriginator) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		PopupEvent popupEvent = new PopupEvent(panel, PopupEvent.POPUP_HIDDEN,
				popupOriginator);
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PopupListener.class) {
				((PopupListener) listeners[i + 1]).popupHidden(popupEvent);
			}
		}
	}
}
