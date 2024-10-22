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
package org.pushingpixels.flamingo.internal.ui.common.popup;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.ComboPopup;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager.PopupEvent;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.internal.ui.ribbon.JRibbonTaskToggleButton;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuPopupPanel;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;
import org.pushingpixels.flamingo.internal.utils.KeyTipManager;

/**
 * Basic UI for popup panel {@link JPopupPanel}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicPopupPanelUI extends PopupPanelUI {
	/**
	 * The associated popup panel.
	 */
	protected JPopupPanel popupPanel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicPopupPanelUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.popupPanel = (JPopupPanel) c;
		super.installUI(this.popupPanel);
		installDefaults();
		installComponents();
		installListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
	 */
	@Override
	public void uninstallUI(JComponent c) {
		uninstallListeners();
		uninstallComponents();
		uninstallDefaults();
		super.uninstallUI(this.popupPanel);
	}

	/**
	 * Installs default settings for the associated command popup menu.
	 */
	protected void installDefaults() {
		Color bg = this.popupPanel.getBackground();
		if (bg == null || bg instanceof UIResource) {
			this.popupPanel.setBackground(FlamingoUtilities.getColor(
					Color.lightGray, "PopupPanel.background",
					"Panel.background"));
		}

		Border b = this.popupPanel.getBorder();
		if (b == null || b instanceof UIResource) {
			Border toSet = UIManager.getBorder("PopupPanel.border");
			if (toSet == null)
				toSet = new BorderUIResource.CompoundBorderUIResource(
						new LineBorder(FlamingoUtilities.getBorderColor()),
						new EmptyBorder(1, 1, 1, 1));
			this.popupPanel.setBorder(toSet);
		}
		LookAndFeel.installProperty(this.popupPanel, "opaque", Boolean.TRUE);
	}

	/**
	 * Installs listeners on the associated command popup menu.
	 */
	protected void installListeners() {
		initializeGlobalListeners();
	}

	/**
	 * Installs components on the associated command popup menu.
	 */
	protected void installComponents() {
	}

	/**
	 * Uninstalls default settings from the associated command popup menu.
	 */
	protected void uninstallDefaults() {
		LookAndFeel.uninstallBorder(this.popupPanel);
	}

	/**
	 * Uninstalls listeners from the associated command popup menu.
	 */
	protected void uninstallListeners() {
	}

	/**
	 * Uninstalls subcomponents from the associated command popup menu.
	 */
	protected void uninstallComponents() {
	}

	/**
	 * The global listener that tracks the ESC key action on the root panes of
	 * windows that show popup panels.
	 */
	static PopupPanelManager.PopupListener popupPanelManagerListener;

	/**
	 * Initializes the global listeners.
	 */
	protected static synchronized void initializeGlobalListeners() {
		if (popupPanelManagerListener != null) {
			return;
		}

		popupPanelManagerListener = new PopupPanelEscapeDismisser();
		PopupPanelManager.defaultManager().addPopupListener(
				popupPanelManagerListener);

		new WindowTracker();
	}

	/**
	 * This class is used to trace the changes in the shown popup panels and
	 * install ESC key listener on the matching root pane so that the popup
	 * panels can be dismissed with the ESC key.
	 * 
	 * @author Kirill Grouchnikov
	 */
	protected static class PopupPanelEscapeDismisser implements
			PopupPanelManager.PopupListener {
		/**
		 * The currently installed action map on the {@link #tracedRootPane}.
		 */
		private ActionMap newActionMap;

		/**
		 * The currently installed input map on the {@link #tracedRootPane}.
		 */
		private InputMap newInputMap;

		/**
		 * The last shown popup panel sequence.
		 */
		List<PopupPanelManager.PopupInfo> lastPathSelected;

		/**
		 * Currently traced root pane. It is the root pane of the originating
		 * component of the first popup panel in the currently shown sequence of
		 * {@link PopupPanelManager}.
		 */
		private JRootPane tracedRootPane;

		/**
		 * Creates a new tracer for popup panels to be dismissed with ESC key.
		 */
		public PopupPanelEscapeDismisser() {
			PopupPanelManager popupPanelManager = PopupPanelManager
					.defaultManager();
			this.lastPathSelected = popupPanelManager.getShownPath();
			if (this.lastPathSelected.size() != 0) {
				traceRootPane(this.lastPathSelected);
			}
		}

		@Override
		public void popupHidden(PopupEvent event) {
			PopupPanelManager msm = PopupPanelManager.defaultManager();
			List<PopupPanelManager.PopupInfo> p = msm.getShownPath();

			if (lastPathSelected.size() != 0 && p.size() == 0) {
				// if it is the last popup panel to be dismissed, untrace the
				// root pane
				untraceRootPane();
			}

			lastPathSelected = p;
		}

		/**
		 * Removes the installed maps on the currently traced root pane.
		 */
		private void untraceRootPane() {
			if (this.tracedRootPane != null) {
				removeUIActionMap(this.tracedRootPane, this.newActionMap);
				removeUIInputMap(this.tracedRootPane, this.newInputMap);
			}
		}

		@Override
		public void popupShown(PopupEvent event) {
			PopupPanelManager msm = PopupPanelManager.defaultManager();
			List<PopupPanelManager.PopupInfo> p = msm.getShownPath();

			if (lastPathSelected.size() == 0 && p.size() != 0) {
				// if it is the first popup panel to be shown, trace the root
				// panel
				traceRootPane(p);
			}

			lastPathSelected = p;
		}

		/**
		 * Installs the maps on the root pane of the originating component of
		 * the first popup panel of the specified sequence to trace the ESC key
		 * and dismiss the shown popup panels.
		 * 
		 * @param shownPath
		 *            Popup panel sequence.
		 */
		private void traceRootPane(List<PopupPanelManager.PopupInfo> shownPath) {
			JComponent originator = shownPath.get(0).getPopupOriginator();
			this.tracedRootPane = SwingUtilities.getRootPane(originator);

			if (this.tracedRootPane != null) {
				newInputMap = new ComponentInputMapUIResource(tracedRootPane);
				newInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
						"hidePopupPanel");

				newActionMap = new ActionMapUIResource();
				newActionMap.put("hidePopupPanel", new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// Hide the last sequence popup for every ESC keystroke.
						// There is special case - if the keytips are shown
						// for the *second* panel of the app menu popup panel,
						// do not dismiss the popup
						List<PopupPanelManager.PopupInfo> popups = PopupPanelManager
								.defaultManager().getShownPath();
						if (popups.size() > 0) {
							PopupPanelManager.PopupInfo lastPopup = popups
									.get(popups.size() - 1);
							if (lastPopup.getPopupPanel() instanceof JRibbonApplicationMenuPopupPanel) {
								JRibbonApplicationMenuPopupPanel appMenuPopupPanel = (JRibbonApplicationMenuPopupPanel) lastPopup
										.getPopupPanel();
								KeyTipManager.KeyTipChain currentlyShownKeyTipChain = KeyTipManager
										.defaultManager()
										.getCurrentlyShownKeyTipChain();
								if ((currentlyShownKeyTipChain != null)
										&& (currentlyShownKeyTipChain.chainParentComponent == appMenuPopupPanel
												.getPanelLevel2()))
									return;
							}
						}
						PopupPanelManager.defaultManager().hideLastPopup();
					}
				});

				addUIInputMap(tracedRootPane, newInputMap);
				addUIActionMap(tracedRootPane, newActionMap);
			}
		}

		/**
		 * Adds the specified input map to the specified component.
		 * 
		 * @param c
		 *            Component.
		 * @param map
		 *            Input map to add.
		 */
		void addUIInputMap(JComponent c, InputMap map) {
			InputMap lastNonUI = null;
			InputMap parent = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

			while (parent != null && !(parent instanceof UIResource)) {
				lastNonUI = parent;
				parent = parent.getParent();
			}

			if (lastNonUI == null) {
				c.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, map);
			} else {
				lastNonUI.setParent(map);
			}
			map.setParent(parent);
		}

		/**
		 * Adds the specified action map to the specified component.
		 * 
		 * @param c
		 *            Component.
		 * @param map
		 *            Action map to add.
		 */
		void addUIActionMap(JComponent c, ActionMap map) {
			ActionMap lastNonUI = null;
			ActionMap parent = c.getActionMap();

			while (parent != null && !(parent instanceof UIResource)) {
				lastNonUI = parent;
				parent = parent.getParent();
			}

			if (lastNonUI == null) {
				c.setActionMap(map);
			} else {
				lastNonUI.setParent(map);
			}
			map.setParent(parent);
		}

		/**
		 * Removes the specified input map from the specified component.
		 * 
		 * @param c
		 *            Component.
		 * @param map
		 *            Input map to remove.
		 */
		void removeUIInputMap(JComponent c, InputMap map) {
			InputMap im = null;
			InputMap parent = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

			while (parent != null) {
				if (parent == map) {
					if (im == null) {
						c.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, map
								.getParent());
					} else {
						im.setParent(map.getParent());
					}
					break;
				}
				im = parent;
				parent = parent.getParent();
			}
		}

		/**
		 * Removes the specified action map from the specified component.
		 * 
		 * @param c
		 *            Component.
		 * @param map
		 *            Action map to remove.
		 */
		void removeUIActionMap(JComponent c, ActionMap map) {
			ActionMap im = null;
			ActionMap parent = c.getActionMap();

			while (parent != null) {
				if (parent == map) {
					if (im == null) {
						c.setActionMap(map.getParent());
					} else {
						im.setParent(map.getParent());
					}
					break;
				}
				im = parent;
				parent = parent.getParent();
			}
		}
	}

	/**
	 * This class is used to dismiss popup panels on the following events:
	 * 
	 * <ul>
	 * <li>Mouse click outside any shown popup panel.</li>
	 * <li>Closing, iconifying or deactivation of a top-level window.</li>
	 * <li>Any change in the component hierarchy of a top-level window.</li>
	 * </ul>
	 * 
	 * Only one top-level window is tracked at any time. The assumption is that
	 * the {@link PopupPanelManager} only shows popup panels originating from
	 * one top-level window.
	 * 
	 * @author Kirill Grouchnikov
	 */
	protected static class WindowTracker implements
			PopupPanelManager.PopupListener, AWTEventListener,
			ComponentListener, WindowListener {

		/**
		 * The currently tracked window. It is the window of the originating
		 * component of the first popup panel in the currently shown sequence of
		 * {@link PopupPanelManager}.
		 */
		Window grabbedWindow;

		/**
		 * Last selected path in the {@link PopupPanelManager}.
		 */
		List<PopupPanelManager.PopupInfo> lastPathSelected;

		/**
		 * Creates the new window tracker.
		 */
		public WindowTracker() {
			PopupPanelManager popupPanelManager = PopupPanelManager
					.defaultManager();
			popupPanelManager.addPopupListener(this);
			this.lastPathSelected = popupPanelManager.getShownPath();
			if (this.lastPathSelected.size() != 0) {
				grabWindow(this.lastPathSelected);
			}
		}

		/**
		 * Grabs the window of the first popup panel in the specified popup
		 * panel sequence.
		 * 
		 * @param shownPath
		 *            Sequence of the currently shown popup panels.
		 */
		void grabWindow(List<PopupPanelManager.PopupInfo> shownPath) {
			final Toolkit tk = Toolkit.getDefaultToolkit();
			java.security.AccessController
					.doPrivileged(new java.security.PrivilegedAction() {
						@Override
                        public Object run() {
							tk.addAWTEventListener(WindowTracker.this,
									AWTEvent.MOUSE_EVENT_MASK
											| AWTEvent.MOUSE_MOTION_EVENT_MASK
											| AWTEvent.MOUSE_WHEEL_EVENT_MASK
											| AWTEvent.WINDOW_EVENT_MASK);
							return null;
						}
					});

			Component invoker = shownPath.get(0).getPopupOriginator();
			grabbedWindow = invoker instanceof Window ? (Window) invoker
					: SwingUtilities.getWindowAncestor(invoker);
			if (grabbedWindow != null) {
				grabbedWindow.addComponentListener(this);
				grabbedWindow.addWindowListener(this);
			}
		}

		/**
		 * Ungrabs the currently tracked window.
		 */
		void ungrabWindow() {
			final Toolkit tk = Toolkit.getDefaultToolkit();
			// The grab should be removed
			java.security.AccessController
					.doPrivileged(new java.security.PrivilegedAction() {
						@Override
                        public Object run() {
							tk.removeAWTEventListener(WindowTracker.this);
							return null;
						}
					});
			if (grabbedWindow != null) {
				grabbedWindow.removeComponentListener(this);
				grabbedWindow.removeWindowListener(this);
				grabbedWindow = null;
			}
		}

		@Override
		public void popupShown(PopupEvent event) {
			PopupPanelManager msm = PopupPanelManager.defaultManager();
			List<PopupPanelManager.PopupInfo> p = msm.getShownPath();

			if (lastPathSelected.size() == 0 && p.size() != 0) {
				// if it is the first popup panel to be shown, grab its window
				grabWindow(p);
			}

			lastPathSelected = p;
		}

		@Override
		public void popupHidden(PopupEvent event) {
			PopupPanelManager msm = PopupPanelManager.defaultManager();
			List<PopupPanelManager.PopupInfo> p = msm.getShownPath();

			if (lastPathSelected.size() != 0 && p.size() == 0) {
				// if it is the last popup panel to be hidden, ungrab its window
				ungrabWindow();
			}

			lastPathSelected = p;
		}

		@Override
        public void eventDispatched(AWTEvent ev) {
			if (!(ev instanceof MouseEvent)) {
				// We are interested in MouseEvents only
				return;
			}
			MouseEvent me = (MouseEvent) ev;
			final Component src = me.getComponent();
			JPopupPanel popupPanelParent = (JPopupPanel) SwingUtilities
					.getAncestorOfClass(JPopupPanel.class, src);
			switch (me.getID()) {
			case MouseEvent.MOUSE_PRESSED:
				boolean wasCommandButtonPopupShowing = false;
				if (src instanceof JCommandButton) {
					wasCommandButtonPopupShowing = ((JCommandButton) src)
							.getPopupModel().isPopupShowing();
				}

				if (!wasCommandButtonPopupShowing && (popupPanelParent != null)) {
					// close all popups until this parent and return
					PopupPanelManager.defaultManager().hidePopups(
							popupPanelParent);
					return;
				}
				if (src instanceof JRibbonTaskToggleButton) {
					JRibbon ribbon = (JRibbon) SwingUtilities
							.getAncestorOfClass(JRibbon.class, src);
					if ((ribbon != null)
							&& FlamingoUtilities
									.isShowingMinimizedRibbonInPopup(ribbon)) {
						// This will be handled in the action listener installed
						// on ribbon task toggle buttons in BasicRibbonUI.
						// There the ribbon popup will be hidden.
						return;
					}
				}

				// if the popup of command button was showing, it will be hidden
				// in BasicCommandButtonUI.processPopupAction() - via
				// BasicCommandButtonUI.createPopupActionListener().
				if (!wasCommandButtonPopupShowing) {
					// special case - ignore mouse press on an item in a combo popup
					if (SwingUtilities
							.getAncestorOfClass(ComboPopup.class, src) == null) {
						PopupPanelManager.defaultManager().hidePopups(src);
					}
				}

				// pass the event so that it gets processed by the controls
				break;

			case MouseEvent.MOUSE_RELEASED:
				// special case - mouse release on an item in a combo popup
				if (SwingUtilities.getAncestorOfClass(ComboPopup.class, src) != null) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							PopupPanelManager.defaultManager().hidePopups(src);
						}
					});
				}

				// pass the event so that it gets processed by the controls
				break;

			case MouseEvent.MOUSE_WHEEL:
				if (popupPanelParent != null) {
					// close all popups until this parent and return
					PopupPanelManager.defaultManager().hidePopups(
							popupPanelParent);
					return;
				}

				PopupPanelManager.defaultManager().hidePopups(src);
				break;
			}
		}

		/**
		 * Checks whether the specified component lies inside a
		 * {@link JPopupPanel}.
		 * 
		 * @param src
		 *            Component.
		 * @return <code>true</code> if the specified component lies inside a
		 *         {@link JPopupPanel}.
		 */
		boolean isInPopupPanel(Component src) {
			for (Component c = src; c != null; c = c.getParent()) {
				if (c instanceof Applet || c instanceof Window) {
					break;
				} else if (c instanceof JPopupPanel) {
					return true;
				}
			}
			return false;
		}

		@Override
        public void componentResized(ComponentEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void componentMoved(ComponentEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void componentShown(ComponentEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void componentHidden(ComponentEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void windowClosing(WindowEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void windowClosed(WindowEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void windowIconified(WindowEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void windowDeactivated(WindowEvent e) {
			PopupPanelManager.defaultManager().hidePopups(null);
		}

		@Override
        public void windowOpened(WindowEvent e) {
		}

		@Override
        public void windowDeiconified(WindowEvent e) {
		}

		@Override
        public void windowActivated(WindowEvent e) {
		}
	}
}
