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
package org.pushingpixels.flamingo.internal.utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.event.EventListenerList;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager.PopupInfo;
import org.pushingpixels.flamingo.api.ribbon.*;
import org.pushingpixels.flamingo.internal.ui.ribbon.*;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuPopupPanel;

public class KeyTipManager {
	boolean isShowingKeyTips;

	List<KeyTipChain> keyTipChains;

	protected EventListenerList listenerList;

	protected BlockingQueue<Character> processingQueue;
	protected ProcessingThread processingThread;

	private JRibbonFrame rootOwner;

	private Component focusOwner;

	private static final KeyTipManager instance = new KeyTipManager();

	public interface KeyTipLinkTraversal {
		public KeyTipChain getNextChain();
	}

	public static interface KeyTipListener extends EventListener {
		public void keyTipsShown(KeyTipEvent event);

		public void keyTipsHidden(KeyTipEvent event);
	}

	public static class KeyTipEvent extends AWTEvent {
		public KeyTipEvent(Object source, int id) {
			super(source, id);
		}
	}

	/**
	 * Annotation to mark a command button that shows UI content with associated
	 * keytips on clicking its action area. Can be used to associate keytips
	 * with menu command buttons in the popup menu shown when the ribbon gallery
	 * is expanded.
	 * 
	 * @author Kirill Grouchnikov
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface HasNextKeyTipChain {
	}

	public class KeyTipLink {
		public String keyTipString;

		public JComponent comp;

		public Point prefAnchorPoint;

		public ActionListener onActivated;

		public KeyTipLinkTraversal traversal;

		public boolean enabled;
	}

	public class KeyTipChain {
		private List<KeyTipLink> links;

		public int keyTipLookupIndex;

		public JComponent chainParentComponent;

		private KeyTipLinkTraversal parent;

		public KeyTipChain(JComponent chainParentComponent) {
			this.chainParentComponent = chainParentComponent;
			this.links = new ArrayList<KeyTipLink>();
			this.keyTipLookupIndex = 0;
		}

		public void addLink(KeyTipLink link) {
			this.links.add(link);
		}
	}

	public static KeyTipManager defaultManager() {
		return instance;
	}

	private KeyTipManager() {
		this.isShowingKeyTips = false;
		this.keyTipChains = new ArrayList<KeyTipChain>();
		this.listenerList = new EventListenerList();
		this.processingQueue = new LinkedBlockingQueue<Character>();
		this.processingThread = new ProcessingThread();
		this.processingThread.start();
	}

	public boolean isShowingKeyTips() {
		return !this.keyTipChains.isEmpty();
	}

	public void hideAllKeyTips() {
		if (this.keyTipChains.isEmpty())
			return;
		this.keyTipChains.clear();
		this.fireKeyTipsHidden(rootOwner);
		repaintWindows();

		// try restoring the focus owner if still relevant
		this.tryRestoringFocusOwner();
	}

	private void tryRestoringFocusOwner() {
		if (focusOwner != null) {
			if (focusOwner.isDisplayable() && focusOwner.isShowing()) {
				focusOwner.requestFocus();
			}
		}
	}

	public void showRootKeyTipChain(JRibbonFrame ribbonFrame) {
		if (!this.keyTipChains.isEmpty()) {
			throw new IllegalStateException(
					"Can't call this method when key tip chains are present");
		}

		// store the current focus owner
		focusOwner = FocusManager.getCurrentManager().getFocusOwner();
		// and transfer the focus to the ribbon frame itself. If the focus
		// is cleared, no key events will be dispatched to our window.
		ribbonFrame.requestFocus();

		rootOwner = ribbonFrame;
		final JRibbon ribbon = ribbonFrame.getRibbon();
		// root chain - application menu button,
		// taskbar panel components and task toggle buttons
		KeyTipChain root = new KeyTipChain(ribbon);

		// application menu button
		final JRibbonApplicationMenuButton appMenuButton = FlamingoUtilities
				.getApplicationMenuButton(ribbonFrame);
		if ((appMenuButton != null)
				&& (ribbon.getApplicationMenuKeyTip() != null)) {
			final KeyTipLink appMenuButtonLink = new KeyTipLink();
			appMenuButtonLink.comp = appMenuButton;
			appMenuButtonLink.keyTipString = ribbon.getApplicationMenuKeyTip();
			appMenuButtonLink.prefAnchorPoint = appMenuButton.getUI()
					.getKeyTipAnchorCenterPoint();
			appMenuButtonLink.onActivated = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					appMenuButton.doPopupClick();
				}
			};
			appMenuButtonLink.enabled = true;
			appMenuButtonLink.traversal = new KeyTipLinkTraversal() {
				@Override
				public KeyTipChain getNextChain() {
					// System.out.println("Get next chain");
					// collect key tips of all controls in the relevant popup
					// panel
					List<PopupInfo> popups = PopupPanelManager.defaultManager()
							.getShownPath();
					if (popups.size() > 0) {
						PopupInfo last = popups.get(popups.size() - 1);
						if (last.getPopupOriginator() == appMenuButton) {
							JPopupPanel popupPanel = last.getPopupPanel();
							KeyTipChain chain = new KeyTipChain(popupPanel);
							chain.parent = appMenuButtonLink.traversal;
							populateChain(last.getPopupPanel(), chain);
							// popupPanel.putClientProperty(KEYTIP_MANAGER,
							// KeyTipManager.this);
							return chain;
						}
					}
					return null;
				}
			};
			root.addLink(appMenuButtonLink);
		}

		// taskbar panel components
		for (Component taskbarComp : ribbon.getTaskbarComponents()) {
			if (taskbarComp instanceof AbstractCommandButton) {
				AbstractCommandButton cb = (AbstractCommandButton) taskbarComp;
				KeyTipLink actionLink = getCommandButtonActionLink(cb);
				if (actionLink != null) {
					root.addLink(actionLink);
				}
				if (taskbarComp instanceof JCommandButton) {
					JCommandButton jcb = (JCommandButton) taskbarComp;
					KeyTipLink popupLink = getCommandButtonPopupLink(jcb);
					if (popupLink != null) {
						root.addLink(popupLink);
					}
				}
			}
		}

		// task toggle buttons
		RibbonUI ui = ribbon.getUI();
		if (ui instanceof BasicRibbonUI) {
			for (Map.Entry<RibbonTask, JRibbonTaskToggleButton> ttbEntry : ((BasicRibbonUI) ui)
					.getTaskToggleButtons().entrySet()) {
				final RibbonTask task = ttbEntry.getKey();
				final JRibbonTaskToggleButton taskToggleButton = ttbEntry
						.getValue();
				String keyTip = task.getKeyTip();
				if (keyTip != null) {
					final KeyTipLink taskToggleButtonLink = new KeyTipLink();
					taskToggleButtonLink.comp = taskToggleButton;
					taskToggleButtonLink.keyTipString = keyTip;
					taskToggleButtonLink.prefAnchorPoint = new Point(
							taskToggleButton.getWidth() / 2, taskToggleButton
									.getHeight());
					taskToggleButtonLink.onActivated = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							taskToggleButton.doActionClick();
						}
					};
					taskToggleButtonLink.enabled = true;
					taskToggleButtonLink.traversal = new KeyTipLinkTraversal() {
						@Override
						public KeyTipChain getNextChain() {
							KeyTipChain taskChain = new KeyTipChain(
									taskToggleButton);
							// collect key tips of all controls from all task
							// bands
							for (AbstractRibbonBand band : task.getBands())
								populateChain(band, taskChain);
							taskChain.parent = taskToggleButtonLink.traversal;
							return taskChain;
						}
					};
					root.addLink(taskToggleButtonLink);
				}
			}
		}
		this.keyTipChains.add(root);
		this.fireKeyTipsShown(ribbonFrame);
		ribbonFrame.repaint();
	}

	public Collection<KeyTipLink> getCurrentlyShownKeyTips() {
		if (this.keyTipChains.isEmpty())
			return Collections.emptyList();
		return Collections.unmodifiableCollection(this.keyTipChains
				.get(this.keyTipChains.size() - 1).links);
	}

	public KeyTipChain getCurrentlyShownKeyTipChain() {
		if (this.keyTipChains.isEmpty())
			return null;
		return this.keyTipChains.get(this.keyTipChains.size() - 1);
	}

	public void showPreviousChain() {
		if (this.keyTipChains.isEmpty())
			return;
		this.keyTipChains.remove(this.keyTipChains.size() - 1);
		// was last?
		if (!this.isShowingKeyTips()) {
			// try restoring focus owner
			this.tryRestoringFocusOwner();
		}
		repaintWindows();
	}

	private void addCommandButtonLinks(Component c, KeyTipChain chain) {
		AbstractCommandButton cb = (AbstractCommandButton) c;
		KeyTipLink actionLink = getCommandButtonActionLink(cb);
		if (actionLink != null) {
			chain.addLink(actionLink);
		}
		if (c instanceof JCommandButton) {
			JCommandButton jcb = (JCommandButton) c;
			KeyTipLink popupLink = getCommandButtonPopupLink(jcb);
			if (popupLink != null) {
				chain.addLink(popupLink);
			}
		}
	}

	private void populateChain(final Component c, final KeyTipChain chain) {
		if (c instanceof AbstractCommandButton) {
			Rectangle compBounds = c.getBounds();
			if (c.isVisible() && c.isShowing()) {
				if ((compBounds.height > 0) && (compBounds.width > 0))
					addCommandButtonLinks(c, chain);
				else
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							Rectangle compBounds = c.getBounds();
							if ((compBounds.height > 0)
									&& (compBounds.width > 0))
								addCommandButtonLinks(c, chain);
						}
					});
			}
		}

		if (c instanceof JRibbonComponent) {
			JRibbonComponent rc = (JRibbonComponent) c;
			KeyTipLink link = getRibbonComponentLink(rc);
			if (link != null) {
				chain.addLink(link);
			}
		}

		if (c instanceof Container) {
			Container cont = (Container) c;
			for (int i = 0; i < cont.getComponentCount(); i++) {
				populateChain(cont.getComponent(i), chain);
			}
		}
	}

	private KeyTipLink getCommandButtonActionLink(final AbstractCommandButton cb) {
		if (cb.getActionKeyTip() != null) {
			final KeyTipLink link = new KeyTipLink();
			link.comp = cb;
			link.keyTipString = cb.getActionKeyTip();
			link.prefAnchorPoint = cb.getUI().getKeyTipAnchorCenterPoint();
			link.onActivated = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cb.doActionClick();
				}
			};
			link.enabled = cb.getActionModel().isEnabled();
			if (cb.getClass().isAnnotationPresent(
					KeyTipManager.HasNextKeyTipChain.class)) {
				link.traversal = new KeyTipLinkTraversal() {
					@Override
					public KeyTipChain getNextChain() {
						// collect key tips of all controls in the relevant
						// popup panel
						List<PopupInfo> popups = PopupPanelManager
								.defaultManager().getShownPath();
						if (popups.size() > 0) {
							PopupInfo last = popups.get(popups.size() - 1);
							JPopupPanel popupPanel = last.getPopupPanel();
							KeyTipChain chain = new KeyTipChain(popupPanel);
							populateChain(last.getPopupPanel(), chain);
							chain.parent = link.traversal;
							return chain;
						}
						return null;
					}
				};
			} else {
				link.traversal = null;
			}
			return link;
		}
		return null;
	}

	private KeyTipLink getRibbonComponentLink(final JRibbonComponent rc) {
		if (rc.getKeyTip() != null) {
			KeyTipLink link = new KeyTipLink();
			link.comp = rc;
			link.keyTipString = rc.getKeyTip();
			link.prefAnchorPoint = rc.getUI().getKeyTipAnchorCenterPoint();
			link.onActivated = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JComponent mainComponent = rc.getMainComponent();
					if (mainComponent instanceof AbstractButton) {
						((AbstractButton) mainComponent).doClick();
					} else {
						if (mainComponent instanceof JComboBox) {
							((JComboBox) mainComponent).showPopup();
						} else {
							if (mainComponent instanceof JSpinner) {
								JComponent editor = ((JSpinner) mainComponent)
										.getEditor();
								editor.requestFocusInWindow();
							} else {
								mainComponent.requestFocusInWindow();
							}
						}
					}
				}
			};
			link.enabled = rc.getMainComponent().isEnabled();
			link.traversal = null;
			return link;
		}
		return null;
	}

	private KeyTipLink getCommandButtonPopupLink(final JCommandButton cb) {
		if (cb.getPopupKeyTip() != null) {
			final KeyTipLink link = new KeyTipLink();
			link.comp = cb;
			link.keyTipString = cb.getPopupKeyTip();
			link.prefAnchorPoint = cb.getUI().getKeyTipAnchorCenterPoint();
			link.onActivated = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (cb instanceof JCommandMenuButton) {
						((JCommandMenuButton) cb).doActionRollover();
					}
					cb.doPopupClick();
				}
			};
			link.enabled = cb.getPopupModel().isEnabled();
			link.traversal = new KeyTipLinkTraversal() {
				@Override
				public KeyTipChain getNextChain() {
					// System.out.println("Get next chain");
					// collect key tips of all controls in the relevant popup
					// panel
					List<PopupInfo> popups = PopupPanelManager.defaultManager()
							.getShownPath();
					if (popups.size() > 0) {
						PopupInfo last = popups.get(popups.size() - 1);
						// if (last.getPopupOriginator() == cb) {
						JPopupPanel popupPanel = last.getPopupPanel();
						// special case - application menu
						if (popupPanel instanceof JRibbonApplicationMenuPopupPanel) {
							JRibbonApplicationMenuPopupPanel appMenuPopupPanel = (JRibbonApplicationMenuPopupPanel) popupPanel;
							// check whether there are entries at level 2
							JPanel level1 = appMenuPopupPanel.getPanelLevel1();
							JPanel level2 = appMenuPopupPanel.getPanelLevel2();
							if (level2.getComponentCount() > 0) {
								KeyTipChain chain = new KeyTipChain(level2);
								populateChain(level2, chain);
								chain.parent = link.traversal;
								return chain;
							} else {
								KeyTipChain chain = new KeyTipChain(level1);
								populateChain(level1, chain);
								chain.parent = link.traversal;
								return chain;
							}
						} else {
							KeyTipChain chain = new KeyTipChain(popupPanel);
							populateChain(last.getPopupPanel(), chain);
							chain.parent = link.traversal;
							return chain;
						}
						// popupPanel.putClientProperty(KEYTIP_MANAGER,
						// KeyTipManager.this);
						// }
					}
					return null;
				}
			};
			return link;
		}
		return null;
	}

	public void handleKeyPress(char keyChar) {
		this.processingQueue.add(keyChar);
	}

	private class ProcessingThread extends Thread {
		public ProcessingThread() {
			super();
			this.setName("KeyTipManager processing thread");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			while (true) {
				try {
					final char keyChar = processingQueue.take();
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							processNextKeyPress(keyChar);
						}
					});
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	private void processNextKeyPress(char keyChar) {
		if (this.keyTipChains.isEmpty())
			return;

		KeyTipChain currChain = this.keyTipChains
				.get(this.keyTipChains.size() - 1);
		// go over the key tip links and see if there is an exact match
		for (final KeyTipLink link : currChain.links) {
			String keyTipString = link.keyTipString;
			if ((Character.toLowerCase(keyTipString
					.charAt(currChain.keyTipLookupIndex)) == Character
					.toLowerCase(keyChar))
					&& (keyTipString.length() == (currChain.keyTipLookupIndex + 1))) {
				// exact match
				if (link.enabled) {
					link.onActivated.actionPerformed(new ActionEvent(link.comp,
							ActionEvent.ACTION_PERFORMED, "keyTipActivated"));
					if (link.traversal != null) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								final KeyTipChain next = link.traversal
										.getNextChain();
								if (next != null) {
									KeyTipChain prev = (keyTipChains.isEmpty() ? null
											: keyTipChains.get(keyTipChains
													.size() - 1));
									keyTipChains.add(next);
									repaintWindows();
									if (prev != null) {
										// force repaint of all menu buttons
										for (KeyTipLink link : prev.links) {
											if (link.comp instanceof JCommandMenuButton)
												link.comp.repaint();
										}
									}
								}
							}
						});
					} else {
						// match found and activated, and no further
						// traversal - dismiss all key tip chains
						hideAllKeyTips();
					}
				}
				return;
			}
		}

		// go over the key tip links and look for key tips that have
		// the specified character as the prefix
		if (currChain.keyTipLookupIndex == 0) {
			KeyTipChain secondary = new KeyTipChain(
					currChain.chainParentComponent);
			secondary.keyTipLookupIndex = 1;
			for (KeyTipLink link : currChain.links) {
				String keyTipString = link.keyTipString;
				if ((Character.toLowerCase(keyTipString
						.charAt(currChain.keyTipLookupIndex)) == Character
						.toLowerCase(keyChar))
						&& (keyTipString.length() == 2)) {
					KeyTipLink secondaryLink = new KeyTipLink();
					secondaryLink.comp = link.comp;
					secondaryLink.enabled = link.enabled;
					secondaryLink.keyTipString = link.keyTipString;
					secondaryLink.onActivated = link.onActivated;
					secondaryLink.prefAnchorPoint = link.prefAnchorPoint;
					secondaryLink.traversal = link.traversal;
					secondary.addLink(secondaryLink);
				}
			}
			if (secondary.links.size() > 0) {
				this.keyTipChains.add(secondary);
			}
			repaintWindows();
			return;
		}
	}

	private void repaintWindows() {
		for (Window window : Window.getWindows()) {
			window.repaint();
		}
		List<PopupInfo> popups = PopupPanelManager.defaultManager()
				.getShownPath();
		for (PopupPanelManager.PopupInfo popup : popups) {
			JPopupPanel popupPanel = popup.getPopupPanel();
			popupPanel.paintImmediately(new Rectangle(0, 0, popupPanel
					.getWidth(), popupPanel.getHeight()));
		}
	}

	public void addKeyTipListener(KeyTipListener keyTipListener) {
		this.listenerList.add(KeyTipListener.class, keyTipListener);
	}

	public void removeKeyTipListener(KeyTipListener keyTipListener) {
		this.listenerList.remove(KeyTipListener.class, keyTipListener);
	}

	protected void fireKeyTipsShown(JRibbonFrame ribbonFrame) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		KeyTipEvent e = new KeyTipEvent(ribbonFrame, 0);
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == KeyTipListener.class) {
				((KeyTipListener) listeners[i + 1]).keyTipsShown(e);
			}
		}
	}

	protected void fireKeyTipsHidden(JRibbonFrame ribbonFrame) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		KeyTipEvent e = new KeyTipEvent(ribbonFrame, 0);
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == KeyTipListener.class) {
				((KeyTipListener) listeners[i + 1]).keyTipsHidden(e);
			}
		}
	}

	public void refreshCurrentChain() {
		KeyTipChain curr = this.keyTipChains.get(this.keyTipChains.size() - 1);
		if (curr.parent == null)
			return;
		KeyTipChain refreshed = curr.parent.getNextChain();
		this.keyTipChains.remove(this.keyTipChains.size() - 1);
		this.keyTipChains.add(refreshed);
		repaintWindows();
	}
}