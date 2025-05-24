/*
 * Copyright (c) 2005-2010 Substance Kirill Grouchnikov. All Rights Reserved.
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
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of
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
package org.pushingpixels.substance.internal.utils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

import org.pushingpixels.lafwidget.animation.effects.GhostPaintingUtils;
import org.pushingpixels.lafwidget.utils.RenderingUtils;
import org.pushingpixels.lafwidget.utils.TrackableThread;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.SubstanceConstants.SubstanceWidgetType;
import org.pushingpixels.substance.api.skin.SkinInfo;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.ui.SubstanceButtonUI;
import org.pushingpixels.substance.internal.ui.SubstanceRootPaneUI;
import org.pushingpixels.substance.internal.utils.filters.NegatedFilter;
import org.pushingpixels.substance.internal.utils.icon.SubstanceIconFactory;
import org.pushingpixels.substance.internal.utils.icon.TransitionAwareIcon;

/**
 * Title pane for <b>Substance</b> look and feel.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceTitlePane extends JComponent {
	/**
	 * PropertyChangeListener added to the JRootPane.
	 */
	private PropertyChangeListener propertyChangeListener;

	/**
	 * JMenuBar, typically renders the system menu items.
	 */
	protected JMenuBar menuBar;

	/**
	 * Action used to close the Window.
	 */
	private Action closeAction;

	/**
	 * Action used to iconify the Frame.
	 */
	private Action iconifyAction;

	/**
	 * Action to restore the Frame size.
	 */
	private Action restoreAction;

	/**
	 * Action to restore the Frame size.
	 */
	private Action maximizeAction;

        /**
	 * Action to set frame as Always on top.
	 */
	private Action alwaysOnTopAction;
        
	/**
	 * Button used to maximize or restore the frame.
	 */
	protected JButton toggleButton;

        /**
	 * Button used to set always on top
	 */
	protected JButton alwaysOnTopButton;
        
	/**
	 * Button used to minimize the frame
	 */
	protected JButton minimizeButton;

	/**
	 * Button used to close the frame.
	 */
	protected JButton closeButton;

	/**
	 * Listens for changes in the state of the Window listener to update the
	 * state of the widgets.
	 */
	private WindowListener windowListener;

	/**
	 * Window we're currently in.
	 */
	protected Window window;

	/**
	 * JRootPane rendering for.
	 */
	protected JRootPane rootPane;

	/**
	 * Buffered Frame.state property. As state isn't bound, this is kept to
	 * determine when to avoid updating widgets.
	 */
	private int state;

	/**
	 * SubstanceRootPaneUI that created us.
	 */
	private SubstanceRootPaneUI rootPaneUI;

	/**
	 * The logfile name for the heap status panel. Can be <code>null</code> - in
	 * this case the {@link HeapStatusThread} will not write heap information.
	 */
	private static String heapStatusLogfileName;

	/**
	 * The heap status panel of <code>this</code> title pane.
	 */
	protected HeapStatusPanel heapStatusPanel;

	/**
	 * The heap status toggle menu item of <code>this</code> title pane.
	 */
	protected JCheckBoxMenuItem heapStatusMenuItem;

	/**
	 * Listens on changes to <code>componentOrientation</code> and
	 * {@link SubstanceLookAndFeel#WINDOW_MODIFIED} properties.
	 */
	protected PropertyChangeListener propertyListener;

	/**
	 * Client property to mark every child to be either leading or trailing. The
	 * value must be one of {@link ExtraComponentKind}.
	 * 
	 * @see #markExtraComponent(JComponent, ExtraComponentKind)
	 * @see #getTitleTextRectangle()
	 */
	protected static final String EXTRA_COMPONENT_KIND = "substancelaf.internal.titlePane.extraComponentKind";

	/**
	 * The application icon to be displayed.
	 */
	protected Image appIcon;

	/**
	 * Enumerates the types of children components.
	 * 
	 * @author Kirill Grouchnikov
	 */
	protected enum ExtraComponentKind {
		/**
		 * Leading child components (left on LTR and right on RTL).
		 */
		LEADING,

		/**
		 * Trailing child components (right on LTR and left on RTL).
		 */
		TRAILING
	}

	/**
	 * Panel that shows heap status and allows running the garbage collector.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class HeapStatusPanel extends JPanel {
		/**
		 * The current heap size in kilobytes.
		 */
		private int currHeapSizeKB;

		/**
		 * The current used portion of heap in kilobytes.
		 */
		private int currTakenHeapSizeKB;

		/**
		 * History of used heap portion (in percents). Each value is in 0.0-1.0
		 * range.
		 */
		private LinkedList<Double> graphValues;

		/**
		 * Creates new heap status panel.
		 */
		public HeapStatusPanel() {
			this.graphValues = new LinkedList<Double>();
			HeapStatusThread.getInstance();
		}

		/**
		 * Updates the values for <code>this</code> heap status panel.
		 * 
		 * @param currHeapSizeKB
		 *            The current heap size in kilobytes.
		 * @param currTakenHeapSizeKB
		 *            The current used portion of heap in kilobytes.
		 */
		public synchronized void updateStatus(int currHeapSizeKB,
				int currTakenHeapSizeKB) {
			this.currHeapSizeKB = currHeapSizeKB;
			this.currTakenHeapSizeKB = currTakenHeapSizeKB;
			double newGraphValue = (double) currTakenHeapSizeKB
					/ (double) currHeapSizeKB;
			this.graphValues.addLast(newGraphValue);
			this.repaint();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public synchronized void paint(Graphics g) {
			Graphics2D graphics = (Graphics2D) g.create();

			SubstanceColorScheme scheme = SubstanceCoreUtilities.getSkin(this)
					.getActiveColorScheme(
							DecorationAreaType.PRIMARY_TITLE_PANE);

			graphics.setColor(scheme.getDarkColor());
			int w = this.getWidth();
			int h = this.getHeight();

			graphics.drawRect(0, 0, w - 1, h - 1);

			graphics.setColor(scheme.getExtraLightColor());
			graphics.fillRect(1, 1, w - 2, h - 2);

			while (this.graphValues.size() > (w - 2))
				this.graphValues.removeFirst();

			int xOff = w - this.graphValues.size() - 1;
			graphics.setColor(scheme.getMidColor());
			int count = 0;
			for (double value : this.graphValues) {
				int valueH = (int) (value * (h - 2));
				graphics.drawLine(xOff + count, h - 1 - valueH, xOff + count,
						h - 2);
				count++;
			}

			graphics.setFont(UIManager.getFont("Panel.font"));
			FontMetrics fm = graphics.getFontMetrics();

			StringBuffer longFormat = new StringBuffer();
			Formatter longFormatter = new Formatter(longFormat);
			longFormatter.format("%.1fMB / %.1fMB",
					this.currTakenHeapSizeKB / 1024.f,
					this.currHeapSizeKB / 1024.f);
			int strW = fm.stringWidth(longFormat.toString());
			int strH = fm.getAscent() + fm.getDescent();

			graphics.setColor(scheme.getForegroundColor());
			RenderingUtils.installDesktopHints(graphics, this);
			if (strW < (w - 5)) {
				graphics.drawString(longFormat.toString(), (w - strW) / 2,
						(h + strH) / 2 - 2);
			} else {
				String shortFormat = (this.currTakenHeapSizeKB / 1024)
						+ "MB / " + (this.currHeapSizeKB / 1024) + "MB";
				strW = fm.stringWidth(shortFormat);
				graphics.drawString(shortFormat, (w - strW) / 2,
						(h + strH) / 2 - 2);
			}

			graphics.dispose();
		}

		/**
		 * Returns the preferred width of this panel.
		 * 
		 * @return Preferred width of this panel.
		 */
		public int getPreferredWidth() {
			BufferedImage dummy = new BufferedImage(1, 1,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = dummy.createGraphics();
			RenderingUtils.installDesktopHints(g2d, this);
			g2d.setFont(UIManager.getFont("Panel.font"));
			FontMetrics fm = g2d.getFontMetrics();
			int result = fm.stringWidth("100.9MB / 200.9MB");
			g2d.dispose();
			return result;
		}
	}

	/**
	 * Thread for heap status panel.
	 */
	public static class HeapStatusThread extends TrackableThread {
		/**
		 * Current heap size in kilobytes.
		 */
		private int heapSizeKB;

		/**
		 * Current used portion of heap in kilobytes.
		 */
		private int takenHeapSizeKB;

		/**
		 * All heap status panels.
		 */
		private static Set<WeakReference<HeapStatusPanel>> panels = new HashSet<WeakReference<HeapStatusPanel>>();

		/**
		 * Single instance of <code>this</code> thread.
		 */
		private static HeapStatusThread instance;

		/**
		 * Formatter object (for logfile).
		 */
		private SimpleDateFormat format;

		/**
		 * Signifies whether a stop request has been issued on <code>this</code>
		 * thread using the {@link #requestStop()} call.
		 */
		private boolean isStopRequested;

		/**
		 * Simple constructor. Defined private for singleton.
		 * 
		 * @see #getInstance()
		 */
		private HeapStatusThread() {
			this.format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
			this.isStopRequested = false;
			this.setName("Substance heap status");
		}

		/**
		 * Gets singleton instance of <code>this</code> thread.
		 * 
		 * @return Singleton instance of <code>this</code> thread.
		 */
		public synchronized static HeapStatusThread getInstance() {
			if (HeapStatusThread.instance == null) {
				HeapStatusThread.instance = new HeapStatusThread();
				HeapStatusThread.instance.start();
			}
			return HeapStatusThread.instance;
		}

		/**
		 * Registers new heap status panel with <code>this</code> thread.
		 * 
		 * @param panel
		 *            Heap statuc panel.
		 */
		public static synchronized void registerPanel(HeapStatusPanel panel) {
			panels.add(new WeakReference<HeapStatusPanel>(panel));
		}

		/**
		 * Unregisters new heap status panel from <code>this</code> thread.
		 * 
		 * @param panel
		 *            Heap statuc panel.
		 */
		public static synchronized void unregisterPanel(HeapStatusPanel panel) {
			for (Iterator<WeakReference<HeapStatusPanel>> it = panels
					.iterator(); it.hasNext();) {
				WeakReference<HeapStatusPanel> ref = it.next();
				HeapStatusPanel currPanel = ref.get();
				if (panel == currPanel) {
					it.remove();
					return;
				}
			}
		}

		/**
		 * Updates the values of heap status.
		 */
		private synchronized void updateHeapCounts() {
			long heapSize = Runtime.getRuntime().totalMemory();
			long heapFreeSize = Runtime.getRuntime().freeMemory();

			this.heapSizeKB = (int) (heapSize / 1024);
			this.takenHeapSizeKB = (int) ((heapSize - heapFreeSize) / 1024);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while (!this.isStopRequested) {
				try {
					// update every 0.5 seconds
					Thread.sleep(500);
				} catch (InterruptedException ie) {
				}
				if (!SubstanceWidgetManager.getInstance().isAllowedAnywhere(
						SubstanceWidgetType.TITLE_PANE_HEAP_STATUS))
					continue;
				this.updateHeapCounts();
				for (Iterator<WeakReference<HeapStatusPanel>> it = panels
						.iterator(); it.hasNext();) {
					WeakReference<HeapStatusPanel> refPanel = it.next();
					HeapStatusPanel panel = refPanel.get();
					if (panel == null) {
						// prune
						it.remove();
						continue;
					}

					panel.updateStatus(this.heapSizeKB, this.takenHeapSizeKB);
				}
				// see if need to put info in log file
				if (SubstanceTitlePane.heapStatusLogfileName != null) {
					PrintWriter pw = null;
					try {
						pw = new PrintWriter(new FileWriter(
								SubstanceTitlePane.heapStatusLogfileName, true));
						pw.println(this.format.format(new Date()) + " "
								+ this.takenHeapSizeKB + "KB / "
								+ this.heapSizeKB + "KB");
					} catch (IOException ioe) {

					} finally {
						if (pw != null) {
							pw.close();
						}
					}
				}
			}
		}

		@Override
		protected void requestStop() {
			this.isStopRequested = true;
			HeapStatusThread.instance = null;
		}
	}

	/**
	 * Creates a new title pane.
	 * 
	 * @param root
	 *            Root pane.
	 * @param ui
	 *            Root pane UI.
	 */
	public SubstanceTitlePane(JRootPane root, SubstanceRootPaneUI ui) {
		this.rootPane = root;
		this.rootPaneUI = ui;

		this.state = -1;

		this.installSubcomponents();
		this.installDefaults();

		this.setLayout(this.createLayout());

		this.setToolTipText(this.getTitle());

		SubstanceLookAndFeel.setDecorationType(this,
				DecorationAreaType.PRIMARY_TITLE_PANE);
		this.setForeground(SubstanceColorUtilities
				.getForegroundColor(SubstanceCoreUtilities.getSkin(this)
						.getBackgroundColorScheme(
								DecorationAreaType.PRIMARY_TITLE_PANE)));
		// SubstanceColorSchemeUtilities
		// .getColorScheme(this, ComponentState.ACTIVE)));
	}

	/**
	 * Uninstalls the necessary state.
	 */
	public void uninstall() {
		this.uninstallListeners();
		this.window = null;

		HeapStatusThread.unregisterPanel(this.heapStatusPanel);

		// Swing bug (?) - the updateComponentTree never gets to the
		// system menu (and in our case we have radio menu items with
		// rollover listeners). Fix for defect 109 - memory leak on skin
		// switch
		if ((this.menuBar != null) && (this.menuBar.getMenuCount() > 0)) {
			this.menuBar.getUI().uninstallUI(this.menuBar);
			SubstanceCoreUtilities.uninstallMenu(this.menuBar.getMenu(0));
		}

		if (this.heapStatusPanel != null) {
			for (MouseListener listener : this.heapStatusPanel
					.getMouseListeners())
				this.heapStatusPanel.removeMouseListener(listener);
			HeapStatusThread.unregisterPanel(this.heapStatusPanel);
			this.remove(this.heapStatusPanel);
		}

		if (this.menuBar != null)
			this.menuBar.removeAll();
		this.removeAll();
	}

	/**
	 * Installs the necessary listeners.
	 */
	private void installListeners() {
		if (this.window != null) {
			this.windowListener = new WindowHandler();
			this.window.addWindowListener(this.windowListener);
			this.propertyChangeListener = new PropertyChangeHandler();
			this.window.addPropertyChangeListener(this.propertyChangeListener);
		}

		// Property change listener for pulsating close button
		// when window has been marked as changed.
		// Fix for defect 109 - memory leak on skin change.
		this.propertyListener = new PropertyChangeListener() {
			@Override
            public void propertyChange(final PropertyChangeEvent evt) {
				if (SubstanceLookAndFeel.WINDOW_MODIFIED.equals(evt
						.getPropertyName())) {
					syncCloseButtonTooltip();
					// if (Boolean.TRUE.equals(evt.getNewValue())) {
					// SubstanceTitlePane.this.closeButton
					// .setToolTipText(SubstanceLookAndFeel
					// .getLabelBundle().getString(
					// "SystemMenu.close")
					// + " ["
					// + SubstanceLookAndFeel
					// .getLabelBundle()
					// .getString(
					// "Tooltip.contentsNotSaved")
					// + "]");
					// } else {
					// SubstanceTitlePane.this.closeButton
					// .setToolTipText(SubstanceLookAndFeel
					// .getLabelBundle().getString(
					// "SystemMenu.close"));
					// }
					// SubstanceTitlePane.this.closeButton.repaint();
				}

				if ("componentOrientation".equals(evt.getPropertyName())) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
                        public void run() {
							if (SubstanceTitlePane.this.menuBar != null) {
								SubstanceTitlePane.this.menuBar
										.applyComponentOrientation((ComponentOrientation) evt
												.getNewValue());
							}
						}
					});
				}
			}
		};
		// Wire it on the frame itself and its root pane.
		this.rootPane.addPropertyChangeListener(this.propertyListener);
		if (this.getFrame() != null)
			this.getFrame().addPropertyChangeListener(this.propertyListener);
	}

	/**
	 * Uninstalls the necessary listeners.
	 */
	private void uninstallListeners() {
		if (this.window != null) {
			this.window.removeWindowListener(this.windowListener);
			this.windowListener = null;
			this.window
					.removePropertyChangeListener(this.propertyChangeListener);
			this.propertyChangeListener = null;
		}

		// Fix for defect 109 - memory leak on skin change.
		this.rootPane.removePropertyChangeListener(this.propertyListener);
		if (this.getFrame() != null)
			this.getFrame().removePropertyChangeListener(this.propertyListener);
		this.propertyListener = null;

	}

	/**
	 * Returns the <code>JRootPane</code> this was created for.
	 */
	@Override
	public JRootPane getRootPane() {
		return this.rootPane;
	}

	/**
	 * Returns the decoration style of the <code>JRootPane</code>.
	 * 
	 * @return Decoration style of the <code>JRootPane</code>.
	 */
	protected int getWindowDecorationStyle() {
		return this.getRootPane().getWindowDecorationStyle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#addNotify()
	 */
	@Override
	public void addNotify() {
		super.addNotify();

		this.uninstallListeners();

		this.window = SwingUtilities.getWindowAncestor(this);
		if (this.window != null) {
			this.setActive(this.window.isActive());
			if (this.window instanceof Frame) {
				this.setState(((Frame) this.window).getExtendedState());
			} else {
				this.setState(0);
			}
			if (this.getComponentCount() == 0) {
				// fix for issue 385 - add the sub-components uninstalled
				// in the removeNotify. This happens when a decorated
				// dialog has been disposed and then reshown.
				this.installSubcomponents();
			}
			this.installListeners();
		}
		this.setToolTipText(this.getTitle());
		this.updateAppIcon();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#removeNotify()
	 */
	@Override
	public void removeNotify() {
		super.removeNotify();

		this.uninstall();
		this.window = null;
	}

	/**
	 * Adds any sub-Components contained in the <code>SubstanceTitlePane</code>.
	 */
	private void installSubcomponents() {
		int decorationStyle = this.getWindowDecorationStyle();
		if (decorationStyle == JRootPane.FRAME) {
			this.createActions();
			this.menuBar = this.createMenuBar();
			if (this.menuBar != null) {
				this.add(this.menuBar);
			}
			this.createButtons();
                        this.add(this.alwaysOnTopButton);
			this.add(this.minimizeButton);
			this.add(this.toggleButton);
			this.add(this.closeButton);

			this.heapStatusPanel = new HeapStatusPanel();
			this.markExtraComponent(this.heapStatusPanel,
					ExtraComponentKind.TRAILING);
			this.add(this.heapStatusPanel);
			boolean isHeapStatusPanelShowing = SubstanceWidgetManager
					.getInstance().isAllowed(rootPane,
							SubstanceWidgetType.TITLE_PANE_HEAP_STATUS);
			this.heapStatusPanel.setVisible(isHeapStatusPanelShowing);
			this.heapStatusPanel.setPreferredSize(new Dimension(80, this
					.getPreferredSize().height));
			this.heapStatusPanel.setToolTipText(SubstanceCoreUtilities
					.getResourceBundle(rootPane).getString(
							"Tooltip.heapStatusPanel"));
			this.heapStatusPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					System.gc();
				}
			});

			HeapStatusThread.registerPanel(this.heapStatusPanel);
		} else {
			if ((decorationStyle == JRootPane.PLAIN_DIALOG)
					|| (decorationStyle == JRootPane.INFORMATION_DIALOG)
					|| (decorationStyle == JRootPane.ERROR_DIALOG)
					|| (decorationStyle == JRootPane.COLOR_CHOOSER_DIALOG)
					|| (decorationStyle == JRootPane.FILE_CHOOSER_DIALOG)
					|| (decorationStyle == JRootPane.QUESTION_DIALOG)
					|| (decorationStyle == JRootPane.WARNING_DIALOG)) {
				this.createActions();
				this.createButtons();
				this.add(this.closeButton);
			}
		}
	}

	/**
	 * Installs the fonts and necessary properties.
	 */
	private void installDefaults() {
		this.setFont(UIManager.getFont("InternalFrame.titleFont", this
				.getLocale()));
	}

	/**
	 * Returns the <code>JMenuBar</code> displaying the appropriate system menu
	 * items.
	 * 
	 * @return <code>JMenuBar</code> displaying the appropriate system menu
	 *         items.
	 */
	protected JMenuBar createMenuBar() {
		this.menuBar = new SubstanceMenuBar();
		this.menuBar.setFocusable(false);
		this.menuBar.setBorderPainted(true);
		this.menuBar.add(this.createMenu());
		this.menuBar.setOpaque(false);
		// support for RTL
		this.menuBar.applyComponentOrientation(this.rootPane
				.getComponentOrientation());

		this.markExtraComponent(this.menuBar, ExtraComponentKind.LEADING);

		return this.menuBar;
	}

	/**
	 * Create the <code>Action</code>s that get associated with the buttons and
	 * menu items.
	 */
	private void createActions() {
		this.closeAction = new CloseAction();
		if (this.getWindowDecorationStyle() == JRootPane.FRAME) {
                        this.alwaysOnTopAction = new AlwaysOnTopAction();
			this.iconifyAction = new IconifyAction();
			this.restoreAction = new RestoreAction();
			this.maximizeAction = new MaximizeAction();
		}
	}

	/**
	 * Returns the <code>JMenu</code> displaying the appropriate menu items for
	 * manipulating the Frame.
	 * 
	 * @return <code>JMenu</code> displaying the appropriate menu items for
	 *         manipulating the Frame.
	 */
	private JMenu createMenu() {
		JMenu menu = new JMenu("");
		menu.setOpaque(false);
		menu.setBackground(null);
		if (this.getWindowDecorationStyle() == JRootPane.FRAME) {
			this.addMenuItems(menu);
		}
		return menu;
	}

	/**
	 * Adds the necessary <code>JMenuItem</code>s to the specified menu.
	 * 
	 * @param menu
	 *            Menu.
	 */
	private void addMenuItems(JMenu menu) {
                menu.add(this.alwaysOnTopAction);
		menu.add(this.restoreAction);

		menu.add(this.iconifyAction);

		if (Toolkit.getDefaultToolkit().isFrameStateSupported(
				Frame.MAXIMIZED_BOTH)) {
			menu.add(this.maximizeAction);
		}

		if (SubstanceCoreUtilities.toShowExtraWidgets(rootPane)) {
			menu.addSeparator();
			JMenu skinMenu = new JMenu(SubstanceCoreUtilities
					.getResourceBundle(rootPane).getString("SystemMenu.skins"));
			Map<String, SkinInfo> allSkins = SubstanceLookAndFeel.getAllSkins();
			for (Map.Entry<String, SkinInfo> skinEntry : allSkins.entrySet()) {
				final String skinClassName = skinEntry.getValue()
						.getClassName();
				JMenuItem jmiSkin = new JMenuItem(skinEntry.getKey());
				jmiSkin.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent e) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
                            public void run() {
								SubstanceLookAndFeel.setSkin(skinClassName);
							}
						});
					}
				});

				skinMenu.add(jmiSkin);
			}
			menu.add(skinMenu);
		}

		menu.addSeparator();

		menu.add(this.closeAction);
	}

	/**
	 * Returns a <code>JButton</code> appropriate for placement on the
	 * TitlePane.
	 * 
	 * @return Title button.
	 */
	private JButton createTitleButton() {
		JButton button = new SubstanceTitleButton();

		button.setFocusPainted(false);
		button.setFocusable(false);
		button.setOpaque(true);

		this.markExtraComponent(button, ExtraComponentKind.TRAILING);

		return button;
	}

	/**
	 * Creates the Buttons that will be placed on the TitlePane.
	 */
	private void createButtons() {
		this.closeButton = this.createTitleButton();
		this.closeButton.setAction(this.closeAction);
		this.closeButton.setText(null);
		this.closeButton.setBorder(null);
		// this.closeButton.setToolTipText(SubstanceLookAndFeel
		// .getLabelBundle().getString(
		// "SystemMenu.close"));

		Icon closeIcon = new TransitionAwareIcon(closeButton,
				new TransitionAwareIcon.Delegate() {
					@Override
                    public Icon getColorSchemeIcon(SubstanceColorScheme scheme) {
						return SubstanceIconFactory
								.getTitlePaneIcon(
										SubstanceIconFactory.IconKind.CLOSE,
										scheme,
										SubstanceCoreUtilities
												.getSkin(rootPane)
												.getBackgroundColorScheme(
														DecorationAreaType.PRIMARY_TITLE_PANE));
					}
				}, "substance.titlePane.closeIcon");
		this.closeButton.setIcon(closeIcon);

		this.closeButton.setFocusable(false);
		this.closeButton.putClientProperty(SubstanceLookAndFeel.FLAT_PROPERTY,
				Boolean.TRUE);

		this.closeButton.putClientProperty(
				SubstanceButtonUI.IS_TITLE_CLOSE_BUTTON, Boolean.TRUE);

		if (this.getWindowDecorationStyle() == JRootPane.FRAME) {
                        this.alwaysOnTopButton = this.createTitleButton();
			this.alwaysOnTopButton.setAction(this.alwaysOnTopAction);
			this.alwaysOnTopButton.setText(null);
			this.alwaysOnTopButton.setBorder(null);

			Icon alwaysOnTopIcon = new TransitionAwareIcon(this.alwaysOnTopButton,
					new TransitionAwareIcon.Delegate() {
						@Override
                        public Icon getColorSchemeIcon(
								SubstanceColorScheme scheme) {
							return getAlwaysOnTopIcon(scheme,
											SubstanceCoreUtilities
													.getSkin(rootPane)
													.getBackgroundColorScheme(
															DecorationAreaType.PRIMARY_TITLE_PANE),
                                                                                        false);
						}
					}, "substance.titlePane.alwaysOnTopIcon");
			this.alwaysOnTopButton.setIcon(alwaysOnTopIcon);

			this.alwaysOnTopButton.setFocusable(false);
			this.alwaysOnTopButton.putClientProperty(
					SubstanceLookAndFeel.FLAT_PROPERTY, Boolean.TRUE);
			this.alwaysOnTopButton.setToolTipText(SubstanceCoreUtilities
					.getResourceBundle(rootPane)
					.getString("SystemMenu.alwaysOnTop"));
                    
                    
			this.minimizeButton = this.createTitleButton();
			this.minimizeButton.setAction(this.iconifyAction);
			this.minimizeButton.setText(null);
			this.minimizeButton.setBorder(null);

			Icon minIcon = new TransitionAwareIcon(this.minimizeButton,
					new TransitionAwareIcon.Delegate() {
						@Override
                        public Icon getColorSchemeIcon(
								SubstanceColorScheme scheme) {
							return SubstanceIconFactory
									.getTitlePaneIcon(
											SubstanceIconFactory.IconKind.MINIMIZE,
											scheme,
											SubstanceCoreUtilities
													.getSkin(rootPane)
													.getBackgroundColorScheme(
															DecorationAreaType.PRIMARY_TITLE_PANE));
						}
					}, "substance.titlePane.minIcon");
			this.minimizeButton.setIcon(minIcon);

			this.minimizeButton.setFocusable(false);
			this.minimizeButton.putClientProperty(
					SubstanceLookAndFeel.FLAT_PROPERTY, Boolean.TRUE);
			this.minimizeButton.setToolTipText(SubstanceCoreUtilities
					.getResourceBundle(rootPane)
					.getString("SystemMenu.iconify"));

			this.toggleButton = this.createTitleButton();
			this.toggleButton.setAction(this.restoreAction);
			this.toggleButton.setBorder(null);
			this.toggleButton.setText(null);

			Icon maxIcon = new TransitionAwareIcon(this.toggleButton,
					new TransitionAwareIcon.Delegate() {
						@Override
                        public Icon getColorSchemeIcon(
								SubstanceColorScheme scheme) {
							return SubstanceIconFactory
									.getTitlePaneIcon(
											SubstanceIconFactory.IconKind.MAXIMIZE,
											scheme,
											SubstanceCoreUtilities
													.getSkin(rootPane)
													.getBackgroundColorScheme(
															DecorationAreaType.PRIMARY_TITLE_PANE));
						}
					}, "substance.titlePane.maxIcon");
			this.toggleButton.setIcon(maxIcon);

			this.toggleButton.setToolTipText(SubstanceCoreUtilities
					.getResourceBundle(rootPane).getString(
							"SystemMenu.maximize"));
			this.toggleButton.setFocusable(false);
			this.toggleButton.putClientProperty(
					SubstanceLookAndFeel.FLAT_PROPERTY, Boolean.TRUE);

		}
		syncCloseButtonTooltip();
	}

	/**
	 * Returns the <code>LayoutManager</code> that should be installed on the
	 * <code>SubstanceTitlePane</code>.
	 * 
	 * @return Layout manager.
	 */
	protected LayoutManager createLayout() {
		return new TitlePaneLayout();
	}

	/**
	 * Updates state dependant upon the Window's active state.
	 * 
	 * @param isActive
	 *            if <code>true</code>, the window is in active state.
	 */
	private void setActive(boolean isActive) {
		this.getRootPane().repaint();
	}

	/**
	 * Sets the state of the Window.
	 * 
	 * @param state
	 *            Window state.
	 */
	private void setState(int state) {
		this.setState(state, false);
	}

	/**
	 * Sets the state of the window. If <code>updateRegardless</code> is true
	 * and the state has not changed, this will update anyway.
	 * 
	 * @param state
	 *            Window state.
	 * @param updateRegardless
	 *            if <code>true</code>, the update is done in any case.
	 */
	private void setState(int state, boolean updateRegardless) {
		Window w = this.getWindow();

		if ((w != null) && (this.getWindowDecorationStyle() == JRootPane.FRAME)) {
			if ((this.state == state) && !updateRegardless) {
				return;
			}
			Frame frame = this.getFrame();

			if (frame != null) {
				final JRootPane rootPane = this.getRootPane();

                                
                                updateAlwaysOnTopButton();
                                
				if (((state & Frame.MAXIMIZED_BOTH) != 0)
						&& ((rootPane.getBorder() == null) || (rootPane
								.getBorder() instanceof UIResource))
						&& frame.isShowing()) {
					rootPane.setBorder(null);
				} else {
					if ((state & Frame.MAXIMIZED_BOTH) == 0) {
						// This is a croak, if state becomes bound, this can
						// be nuked.
						this.rootPaneUI.installBorder(rootPane);
					}
				}
				if (frame.isResizable()) {
					if ((state & Frame.MAXIMIZED_BOTH) != 0) {
						Icon restoreIcon = new TransitionAwareIcon(
								this.toggleButton,
								new TransitionAwareIcon.Delegate() {
									@Override
                                    public Icon getColorSchemeIcon(
											SubstanceColorScheme scheme) {
										return SubstanceIconFactory
												.getTitlePaneIcon(
														SubstanceIconFactory.IconKind.RESTORE,
														scheme,
														SubstanceCoreUtilities
																.getSkin(
																		rootPane)
																.getBackgroundColorScheme(
																		DecorationAreaType.PRIMARY_TITLE_PANE));
									}
								}, "substance.titlePane.restoreIcon");
						this
								.updateToggleButton(this.restoreAction,
										restoreIcon);
						this.toggleButton.setToolTipText(SubstanceCoreUtilities
								.getResourceBundle(rootPane).getString(
										"SystemMenu.restore"));
						this.maximizeAction.setEnabled(false);
						this.restoreAction.setEnabled(true);
					} else {
						Icon maxIcon = new TransitionAwareIcon(
								this.toggleButton,
								new TransitionAwareIcon.Delegate() {
									@Override
                                    public Icon getColorSchemeIcon(
											SubstanceColorScheme scheme) {
										return SubstanceIconFactory
												.getTitlePaneIcon(
														SubstanceIconFactory.IconKind.MAXIMIZE,
														scheme,
														SubstanceCoreUtilities
																.getSkin(
																		rootPane)
																.getBackgroundColorScheme(
																		DecorationAreaType.PRIMARY_TITLE_PANE));
									}
								}, "substance.titlePane.maxIcon");
						this.updateToggleButton(this.maximizeAction, maxIcon);
						this.toggleButton.setToolTipText(SubstanceCoreUtilities
								.getResourceBundle(rootPane).getString(
										"SystemMenu.maximize"));
						this.maximizeAction.setEnabled(true);
						this.restoreAction.setEnabled(false);
					}
					if ((this.toggleButton.getParent() == null)
							|| (this.minimizeButton.getParent() == null)
                                                        || (this.alwaysOnTopButton.getParent() == null)
                                                ) {
                                                this.add(this.alwaysOnTopButton);
						this.add(this.toggleButton);
						this.add(this.minimizeButton);
						this.revalidate();
						this.repaint();
					}
					this.toggleButton.setText(null);
                                        this.alwaysOnTopAction.setEnabled(true);
				} else {
					this.maximizeAction.setEnabled(false);
					this.restoreAction.setEnabled(false);
					if (this.toggleButton.getParent() != null) {
						this.remove(this.toggleButton);
						this.revalidate();
						this.repaint();
					}
				}
			} else {
				// Not contained in a Frame
                                this.alwaysOnTopAction.setEnabled(false);
				this.maximizeAction.setEnabled(false);
				this.restoreAction.setEnabled(false);
				this.iconifyAction.setEnabled(false);
                                this.remove(this.alwaysOnTopButton);
				this.remove(this.toggleButton);
				this.remove(this.minimizeButton);
				this.revalidate();
				this.repaint();
			}
                        this.alwaysOnTopAction.setEnabled(true);
			this.closeAction.setEnabled(true);
			this.state = state;
		}
	}

	/**
	 * Updates the toggle button to contain the Icon <code>icon</code>, and
	 * Action <code>action</code>.
	 * 
	 * @param action
	 *            Action.
	 * @param icon
	 *            Icon.
	 */
	private void updateToggleButton(Action action, Icon icon) {
		this.toggleButton.setAction(action);
		this.toggleButton.setIcon(icon);
		this.toggleButton.setText(null);
	}
        
        private void updateAlwaysOnTopButton() {
            Frame frame = this.getFrame();
            if (frame.isAlwaysOnTop()) {
                Icon alwaysOnTopIconEnabled = new TransitionAwareIcon(this.alwaysOnTopButton,
                    new TransitionAwareIcon.Delegate() {
                            @Override
                    public Icon getColorSchemeIcon(
                                                            SubstanceColorScheme scheme) {
                                                    return getAlwaysOnTopIcon(scheme,
                                                                                    SubstanceCoreUtilities
                                                                                                    .getSkin(rootPane)
                                                                                                    .getBackgroundColorScheme(
                                                                                                                    DecorationAreaType.PRIMARY_TITLE_PANE),
                                                                                    true);
                                            }
                                    }, "substance.titlePane.alwaysOnTopIconEnabled");
                    this.alwaysOnTopButton.setIcon(alwaysOnTopIconEnabled);
            } else {
                Icon alwaysOnTopIcon = new TransitionAwareIcon(this.alwaysOnTopButton,
                    new TransitionAwareIcon.Delegate() {
                            @Override
                    public Icon getColorSchemeIcon(
                                                            SubstanceColorScheme scheme) {
                                                    return getAlwaysOnTopIcon(scheme,
                                                                                    SubstanceCoreUtilities
                                                                                                    .getSkin(rootPane)
                                                                                                    .getBackgroundColorScheme(
                                                                                                                    DecorationAreaType.PRIMARY_TITLE_PANE),
                                                                                    false);
                                            }
                                    }, "substance.titlePane.alwaysOnTopIcon");
                    this.alwaysOnTopButton.setIcon(alwaysOnTopIcon);
            }
        }

	/**
	 * Returns the Frame rendering in. This will return null if the
	 * <code>JRootPane</code> is not contained in a <code>Frame</code>.
	 * 
	 * @return Frame.
	 */
	private Frame getFrame() {
		Window window = this.getWindow();

		if (window instanceof Frame) {
			return (Frame) window;
		}
		return null;
	}

	/**
	 * Returns the <code>Window</code> the <code>JRootPane</code> is contained
	 * in. This will return null if there is no parent ancestor of the
	 * <code>JRootPane</code>.
	 * 
	 * @return Window.
	 */
	private Window getWindow() {
		return this.window;
	}

	/**
	 * Returns the String to display as the title.
	 * 
	 * @return Display title.
	 */
	private String getTitle() {
		Window w = this.getWindow();

		if (w instanceof Frame) {
			return ((Frame) w).getTitle();
		}
		if (w instanceof Dialog) {
			return ((Dialog) w).getTitle();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		// long start = System.nanoTime();
		// As state isn't bound, we need a convenience place to check
		// if it has changed. Changing the state typically changes the
		if (this.getFrame() != null) {
			this.setState(this.getFrame().getExtendedState());
		}
		final JRootPane rootPane = this.getRootPane();
		Window window = this.getWindow();
		boolean leftToRight = (window == null) ? rootPane
				.getComponentOrientation().isLeftToRight() : window
				.getComponentOrientation().isLeftToRight();
		int width = this.getWidth();
		int height = this.getHeight();

		SubstanceSkin skin = SubstanceCoreUtilities.getSkin(rootPane);
		if (skin == null) {
			SubstanceCoreUtilities
					.traceSubstanceApiUsage(this,
							"Substance delegate used when Substance is not the current LAF");
		}
		SubstanceColorScheme scheme = skin
				.getEnabledColorScheme(DecorationAreaType.PRIMARY_TITLE_PANE);

		int xOffset = 0;
		String theTitle = this.getTitle();

		if (theTitle != null) {
			Rectangle titleTextRect = this.getTitleTextRectangle();
			FontMetrics fm = rootPane.getFontMetrics(g.getFont());
			int titleWidth = titleTextRect.width - 20;
			String clippedTitle = SubstanceCoreUtilities.clipString(fm,
					titleWidth, theTitle);
			// show tooltip with full title only if necessary
			if (theTitle.equals(clippedTitle)) {
				this.setToolTipText(null);
			} else {
				this.setToolTipText(theTitle);
			}
			theTitle = clippedTitle;
			if (leftToRight)
				xOffset = titleTextRect.x;
			else
				xOffset = titleTextRect.x + titleTextRect.width
						- fm.stringWidth(theTitle);
		}

		Graphics2D graphics = (Graphics2D) g.create();
		Font font = SubstanceLookAndFeel.getFontPolicy().getFontSet(
				"Substance", null).getWindowTitleFont();
		graphics.setFont(font);

		BackgroundPaintingUtils
				.update(graphics, SubstanceTitlePane.this, false);
		// DecorationPainterUtils.paintDecorationBackground(graphics,
		// SubstanceTitlePane.this, false);

		// draw the title (if needed)
		if (theTitle != null) {
			FontMetrics fm = rootPane.getFontMetrics(graphics.getFont());
			int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();

			SubstanceTextUtilities.paintTextWithDropShadow(this, graphics,
					SubstanceColorUtilities.getForegroundColor(scheme),
					theTitle, width, height, xOffset, yOffset);
		}

		GhostPaintingUtils.paintGhostImages(this, graphics);

		// long end = System.nanoTime();
		// System.out.println(end - start);
		graphics.dispose();
	}

	/**
	 * Computes the rectangle of the title text. This method looks at all the
	 * children components of the title pane, grouping them by leading and
	 * trailing (based on {@link #EXTRA_COMPONENT_KIND} client property). The
	 * title text rectangle is the space between the leading group and the
	 * trailing group.
	 * 
	 * @return Rectangle of the title text.
	 * @throws IllegalStateException
	 *             If at least one child component of this title pane is not
	 *             marked with the {@link #EXTRA_COMPONENT_KIND} client
	 *             property.
	 * @see #markExtraComponent(JComponent, ExtraComponentKind)
	 * @see #EXTRA_COMPONENT_KIND
	 */
	protected Rectangle getTitleTextRectangle() {
		JRootPane rootPane = this.getRootPane();
		Window window = this.getWindow();
		boolean leftToRight = (window == null) ? rootPane
				.getComponentOrientation().isLeftToRight() : window
				.getComponentOrientation().isLeftToRight();

		if (leftToRight) {
			int maxLeadingX = 0;
			int minTrailingX = this.getWidth();

			for (int i = 0; i < this.getComponentCount(); i++) {
				Component child = this.getComponent(i);
				if (!child.isVisible())
					continue;
				if (child instanceof JComponent) {
					ExtraComponentKind kind = (ExtraComponentKind) ((JComponent) child)
							.getClientProperty(EXTRA_COMPONENT_KIND);
					if (kind == null) {
						throw new IllegalStateException("Title pane child "
								+ child.getClass().getName()
								+ " is not marked as leading or trailing");
					}
					if (kind == ExtraComponentKind.LEADING) {
						int cx = child.getX() + child.getWidth();
						if (cx > maxLeadingX)
							maxLeadingX = cx;
					} else {
						int cx = child.getX();
						if (cx < minTrailingX)
							minTrailingX = cx;
					}
				}
			}

			int start = maxLeadingX + 10;
			int end = minTrailingX - 5;
			return new Rectangle(start, 0, end - start, this.getHeight());
		} else {
			int minLeadingX = this.getWidth();
			int maxTrailingX = 0;

			for (int i = 0; i < this.getComponentCount(); i++) {
				Component child = this.getComponent(i);
				if (!child.isVisible())
					continue;
				if (child instanceof JComponent) {
					ExtraComponentKind kind = (ExtraComponentKind) ((JComponent) child)
							.getClientProperty(EXTRA_COMPONENT_KIND);
					if (kind == null) {
						throw new IllegalStateException("Title pane child "
								+ child.getClass().getName()
								+ " is not marked as leading or trailing");
					}
					if (kind == ExtraComponentKind.LEADING) {
						int cx = child.getX();
						if (cx < minLeadingX)
							minLeadingX = cx;
					} else {
						int cx = child.getX() + child.getWidth();
						if (cx > maxTrailingX)
							maxTrailingX = cx;
					}
				}
			}

			int start = maxTrailingX + 5;
			int end = minLeadingX - 10;
			return new Rectangle(start, 0, end - start, this.getHeight());
		}
	}

	/**
	 * Actions used to <code>close</code> the <code>Window</code>.
	 */
	private class CloseAction extends AbstractAction {
		/**
		 * Creates a new close action.
		 */
		public CloseAction() {
			super(SubstanceCoreUtilities.getResourceBundle(rootPane).getString(
					"SystemMenu.close"), SubstanceImageCreator.getCloseIcon(
					SubstanceCoreUtilities.getSkin(rootPane)
							.getActiveColorScheme(
									DecorationAreaType.PRIMARY_TITLE_PANE),
					SubstanceCoreUtilities.getSkin(rootPane)
							.getBackgroundColorScheme(
									DecorationAreaType.PRIMARY_TITLE_PANE)));
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			Window window = SubstanceTitlePane.this.getWindow();

			if (window != null) {
				window.dispatchEvent(new WindowEvent(window,
						WindowEvent.WINDOW_CLOSING));
			}
		}
	}

	/**
	 * Actions used to <code>iconfiy</code> the <code>Frame</code>.
	 */
	private class IconifyAction extends AbstractAction {
		/**
		 * Creates a new iconify action.
		 */
		public IconifyAction() {
			super(
					SubstanceCoreUtilities.getResourceBundle(rootPane)
							.getString("SystemMenu.iconify"),
					SubstanceImageCreator
							.getMinimizeIcon(
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getActiveColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE),
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getBackgroundColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE)));
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			Frame frame = SubstanceTitlePane.this.getFrame();
			if (frame != null) {
				frame.setExtendedState(SubstanceTitlePane.this.state
						| Frame.ICONIFIED);
			}
		}
	}
        
        private class AlwaysOnTopAction extends AbstractAction {
		/**
		 * Creates a new restore action.
		 */
		public AlwaysOnTopAction() {
			super(
					/*SubstanceCoreUtilities.getResourceBundle(rootPane)
							.getString("SystemMenu.restore"),*/
                                        //"Always on top",
                                    SubstanceCoreUtilities.getResourceBundle(rootPane)
							.getString("SystemMenu.alwaysOnTop"),
					SubstanceImageCreator
							.getRestoreIcon(
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getActiveColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE),
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getBackgroundColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE)));
		}

		@Override
                public void actionPerformed(ActionEvent e) {
			Frame frame = SubstanceTitlePane.this.getFrame();

			if (frame == null) {
				return;
			}

			frame.setAlwaysOnTop(!frame.isAlwaysOnTop());       
                        updateAlwaysOnTopButton();
		}
	}

	/**
	 * Actions used to <code>restore</code> the <code>Frame</code>.
	 */
	private class RestoreAction extends AbstractAction {
		/**
		 * Creates a new restore action.
		 */
		public RestoreAction() {
			super(
					SubstanceCoreUtilities.getResourceBundle(rootPane)
							.getString("SystemMenu.restore"),
					SubstanceImageCreator
							.getRestoreIcon(
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getActiveColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE),
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getBackgroundColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE)));
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			Frame frame = SubstanceTitlePane.this.getFrame();

			if (frame == null) {
				return;
			}

			if ((SubstanceTitlePane.this.state & Frame.ICONIFIED) != 0) {
				frame.setExtendedState(SubstanceTitlePane.this.state
						& ~Frame.ICONIFIED);
			} else {
				frame.setExtendedState(SubstanceTitlePane.this.state
						& ~Frame.MAXIMIZED_BOTH);
			}
		}
	}

	/**
	 * Actions used to <code>restore</code> the <code>Frame</code>.
	 */
	private class MaximizeAction extends AbstractAction {
		/**
		 * Creates a new maximize action.
		 */
		public MaximizeAction() {
			super(
					SubstanceCoreUtilities.getResourceBundle(rootPane)
							.getString("SystemMenu.maximize"),
					SubstanceImageCreator
							.getMaximizeIcon(
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getActiveColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE),
									SubstanceCoreUtilities
											.getSkin(rootPane)
											.getEnabledColorScheme(
													DecorationAreaType.PRIMARY_TITLE_PANE)));
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			Frame frame = SubstanceTitlePane.this.getFrame();
			if (frame != null) {
				if (frame instanceof JFrame) {
					SubstanceRootPaneUI rpUI = (SubstanceRootPaneUI) ((JFrame) frame)
							.getRootPane().getUI();
					rpUI.setMaximized();
				}
				frame.setExtendedState(SubstanceTitlePane.this.state
						| Frame.MAXIMIZED_BOTH);
			}
		}
	}

	/**
	 * Class responsible for drawing the system menu. Looks up the image to draw
	 * from the Frame associated with the <code>JRootPane</code>.
	 */
	public class SubstanceMenuBar extends JMenuBar {
		@Override
		public void paint(Graphics g) {
			if (appIcon != null) {
				g.drawImage(appIcon, 0, 0, null);
			} else {
				Icon icon = UIManager.getIcon("InternalFrame.icon");
				if (icon != null) {
					icon.paintIcon(this, g, 0, 0);
				}
			}
		}

		@Override
		public Dimension getMinimumSize() {
			return this.getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension size = super.getPreferredSize();

			int iSize = SubstanceSizeUtils.getTitlePaneIconSize();
			return new Dimension(Math.max(iSize, size.width), Math.max(
					size.height, iSize));
		}
	}

	/**
	 * Layout manager for the title pane.
	 * 
	 * @author Kirill Graphics
	 */
	protected class TitlePaneLayout implements LayoutManager {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String,
		 * java.awt.Component)
		 */
		@Override
        public void addLayoutComponent(String name, Component c) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
		 */
		@Override
        public void removeLayoutComponent(Component c) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
		 */
		@Override
        public Dimension preferredLayoutSize(Container c) {
			int height = this.computeHeight();
			return new Dimension(height, height);
		}

		/**
		 * Computes title pane height.
		 * 
		 * @return Title pane height.
		 */
		private int computeHeight() {
			FontMetrics fm = SubstanceTitlePane.this.rootPane
					.getFontMetrics(SubstanceTitlePane.this.getFont());
			int fontHeight = fm.getHeight();
			fontHeight += 7;
			int iconHeight = 0;
			if (SubstanceTitlePane.this.getWindowDecorationStyle() == JRootPane.FRAME) {
				iconHeight = SubstanceSizeUtils.getTitlePaneIconSize();
			}

			int finalHeight = Math.max(fontHeight, iconHeight);
			return finalHeight;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
		 */
		@Override
        public Dimension minimumLayoutSize(Container c) {
			return this.preferredLayoutSize(c);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
		 */
		@Override
        public void layoutContainer(Container c) {
			boolean leftToRight = (SubstanceTitlePane.this.window == null) ? SubstanceTitlePane.this
					.getRootPane().getComponentOrientation().isLeftToRight()
					: SubstanceTitlePane.this.window.getComponentOrientation()
							.isLeftToRight();

			int w = SubstanceTitlePane.this.getWidth();
			int x;
			int y = 3;
			int spacing;
			int buttonHeight;
			int buttonWidth;

			if ((SubstanceTitlePane.this.closeButton != null)
					&& (SubstanceTitlePane.this.closeButton.getIcon() != null)) {
				buttonHeight = SubstanceTitlePane.this.closeButton.getIcon()
						.getIconHeight();
				buttonWidth = SubstanceTitlePane.this.closeButton.getIcon()
						.getIconWidth();
			} else {
				buttonHeight = SubstanceSizeUtils.getTitlePaneIconSize();
				buttonWidth = SubstanceSizeUtils.getTitlePaneIconSize();
			}

			y = (getHeight() - buttonHeight) / 2;

			// assumes all buttons have the same dimensions
			// these dimensions include the borders

			x = leftToRight ? w : 0;

			spacing = 5;
			x = leftToRight ? spacing : w - buttonWidth - spacing;
			if (SubstanceTitlePane.this.menuBar != null) {
				SubstanceTitlePane.this.menuBar.setBounds(x, y, buttonWidth,
						buttonHeight);
				// System.out.println(menuBar.getBounds());
			}

			x = leftToRight ? w : 0;
			spacing = 3;
			x += leftToRight ? -spacing - buttonWidth : spacing;
			if (SubstanceTitlePane.this.closeButton != null) {
				SubstanceTitlePane.this.closeButton.setBounds(x, y,
						buttonWidth, buttonHeight);
			}

			if (!leftToRight)
				x += buttonWidth;

			if (SubstanceTitlePane.this.getWindowDecorationStyle() == JRootPane.FRAME) {
				if (Toolkit.getDefaultToolkit().isFrameStateSupported(
						Frame.MAXIMIZED_BOTH)) {
					if (SubstanceTitlePane.this.toggleButton.getParent() != null) {
						spacing = 10;
						x += leftToRight ? -spacing - buttonWidth : spacing;
						SubstanceTitlePane.this.toggleButton.setBounds(x, y,
								buttonWidth, buttonHeight);
						if (!leftToRight) {
							x += buttonWidth;
						}
					}
				}

				if ((SubstanceTitlePane.this.minimizeButton != null)
						&& (SubstanceTitlePane.this.minimizeButton.getParent() != null)) {
					spacing = 2;
					x += leftToRight ? -spacing - buttonWidth : spacing;
					SubstanceTitlePane.this.minimizeButton.setBounds(x, y,
							buttonWidth, buttonHeight);
					if (!leftToRight) {
						x += buttonWidth;
					}
				}
                                
                                if ((SubstanceTitlePane.this.alwaysOnTopButton != null)
						&& (SubstanceTitlePane.this.alwaysOnTopButton.getParent() != null)) {
					spacing = 2;
					x += leftToRight ? -spacing - buttonWidth : spacing;
					SubstanceTitlePane.this.alwaysOnTopButton.setBounds(x, y,
							buttonWidth, buttonHeight);
					if (!leftToRight) {
						x += buttonWidth;
					}
				}

				if ((SubstanceTitlePane.this.heapStatusPanel != null)
						&& SubstanceTitlePane.this.heapStatusPanel.isVisible()) {
					spacing = 5;
					x += leftToRight ? (-spacing - SubstanceTitlePane.this.heapStatusPanel
							.getPreferredWidth())
							: spacing;
					SubstanceTitlePane.this.heapStatusPanel.setBounds(x, 1,
							SubstanceTitlePane.this.heapStatusPanel
									.getPreferredWidth(),
							SubstanceTitlePane.this.getHeight() - 3);
				}
			}
			// buttonsWidth = leftToRight ? w - x : x;
		}

	}

	/**
	 * PropertyChangeListener installed on the Window. Updates the necessary
	 * state as the state of the Window changes.
	 */
	private class PropertyChangeHandler implements PropertyChangeListener {
		@Override
        public void propertyChange(PropertyChangeEvent pce) {
			String name = pce.getPropertyName();

			// Frame.state isn't currently bound.
			if ("resizable".equals(name) || "state".equals(name)) {
				Frame frame = SubstanceTitlePane.this.getFrame();

				if (frame != null) {
					SubstanceTitlePane.this.setState(frame.getExtendedState(),
							true);
				}
				if ("resizable".equals(name)) {
					SubstanceTitlePane.this.getRootPane().repaint();
				}
			} else {
				if ("title".equals(name)) {
					SubstanceTitlePane.this.repaint();
					SubstanceTitlePane.this.setToolTipText((String) pce
							.getNewValue());
				} else if ("componentOrientation" == name) {
					revalidate();
					repaint();
				} else if ("iconImage" == name) {
					updateAppIcon();
					revalidate();
					repaint();
				}
			}
		}
	}

	/**
	 * WindowListener installed on the Window, updates the state as necessary.
	 */
	private class WindowHandler extends WindowAdapter {
		@Override
		public void windowActivated(WindowEvent ev) {
			SubstanceTitlePane.this.setActive(true);
		}

		@Override
		public void windowDeactivated(WindowEvent ev) {
			SubstanceTitlePane.this.setActive(false);
		}
	}

	/**
	 * Sets location for heap status logfile. Relevant if
	 * {@link #setCanHaveHeapStatusPanel(boolean)} was called with
	 * <code>true</code>.
	 * 
	 * @param heapStatusLogfileName
	 *            Logfile for the heap status panel.
	 */
	public static void setHeapStatusLogfileName(String heapStatusLogfileName) {
		SubstanceTitlePane.heapStatusLogfileName = heapStatusLogfileName;
	}

	/**
	 * Synchronizes the tooltip of the close button.
	 */
	protected void syncCloseButtonTooltip() {
		if (SubstanceCoreUtilities.isRootPaneModified(this.getRootPane())) {
			this.closeButton.setToolTipText(SubstanceCoreUtilities
					.getResourceBundle(rootPane).getString("SystemMenu.close")
					+ " ["
					+ SubstanceCoreUtilities.getResourceBundle(rootPane)
							.getString("Tooltip.contentsNotSaved") + "]");
		} else {
			this.closeButton.setToolTipText(SubstanceCoreUtilities
					.getResourceBundle(rootPane).getString("SystemMenu.close"));
		}
		this.closeButton.repaint();
	}

	/**
	 * Marks the specified child component with the specified extra component
	 * kind.
	 * 
	 * @param comp
	 *            Child component.
	 * @param kind
	 *            Extra kind.
	 * @see #getTitleTextRectangle()
	 * @see #EXTRA_COMPONENT_KIND
	 */
	protected void markExtraComponent(JComponent comp, ExtraComponentKind kind) {
		comp.putClientProperty(EXTRA_COMPONENT_KIND, kind);
	}

	/**
	 * Updates the application icon.
	 */
	private void updateAppIcon() {
		Window window = getWindow();
		if (window == null) {
			this.appIcon = null;
			return;
		}
		java.util.List<Image> icons = window.getIconImages();

		if (icons.size() == 0) {
			this.appIcon = null;
		} else {
			int prefSize = SubstanceSizeUtils.getTitlePaneIconSize();
			this.appIcon = SubstanceCoreUtilities.getScaledIconImage(icons,
					prefSize, prefSize);
		}
	}

	public AbstractButton getCloseButton() {
		return this.closeButton;
	}
        
        
        public static Icon getAlwaysOnTopIcon(SubstanceColorScheme scheme,
			SubstanceColorScheme backgroundScheme, boolean enabled) {
               int iSize = SubstanceSizeUtils.getTitlePaneIconSize();
		BufferedImage image = SubstanceCoreUtilities
				.getBlankImage(iSize, iSize);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		Color color = SubstanceColorUtilities.getMarkColor(scheme, true);
		graphics.setColor(color);
		//graphics.fillRect(start + 2, end - 2, size, 3);
                Ellipse2D ellipse = new Ellipse2D.Double(iSize / 3, iSize / 3, iSize / 3, iSize / 3);
                if (enabled) {
                    graphics.fill(ellipse);
                }
                graphics.draw(ellipse);
                
                graphics.drawLine(iSize / 2, iSize * 2 / 3, iSize / 2, iSize - 4);

		int fgStrength = SubstanceColorUtilities.getColorBrightness(color
				.getRGB());
		int fgNegativeStrength = SubstanceColorUtilities
				.getColorBrightness(SubstanceColorUtilities
						.getNegativeColor(color.getRGB()));
		int bgStrength = SubstanceColorUtilities
				.getColorBrightness(backgroundScheme.getLightColor().getRGB());
		boolean noEcho = (fgStrength > fgNegativeStrength)
				&& (fgStrength < bgStrength);

		return new ImageIcon(overlayEcho(image,
				noEcho ? 0 : SubstanceColorUtilities.getColorStrength(color),
				1, 1));
	}
        
        private static BufferedImage getNegated(BufferedImage bi) {
		return new NegatedFilter().filter(bi, null);
	}
        
        private static BufferedImage overlayEcho(BufferedImage image,
			float echoAlpha, int offsetX, int offsetY) {
		int width = image.getWidth();
		int height = image.getHeight();

		// blur the original image
		// ConvolveOp convolve = new ConvolveOp(new Kernel(3, 3, new float[] {
		// .4f, .4f, .4f, .4f, .0f, .4f, .4f, .4f, .4f }),
		// ConvolveOp.EDGE_NO_OP, null);
		offsetX = offsetY = 0;
		BufferedImage negated = getNegated(image);
		BufferedImage result = SubstanceCoreUtilities.getBlankImage(width,
				height);
		Graphics2D graphics = (Graphics2D) result.getGraphics().create();
		graphics.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.2f * echoAlpha * echoAlpha
						* echoAlpha));
		graphics.drawImage(negated, offsetX - 1, offsetY - 1, null);
		graphics.drawImage(negated, offsetX + 1, offsetY - 1, null);
		graphics.drawImage(negated, offsetX - 1, offsetY + 1, null);
		graphics.drawImage(negated, offsetX + 1, offsetY + 1, null);
		graphics.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.7f * echoAlpha * echoAlpha
						* echoAlpha));
		graphics.drawImage(negated, offsetX, offsetY - 1, null);
		graphics.drawImage(negated, offsetX, offsetY + 1, null);
		graphics.drawImage(negated, offsetX - 1, offsetY, null);
		graphics.drawImage(negated, offsetX + 1, offsetY, null);

		graphics.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 1.0f));
		graphics.drawImage(image, 0, 0, null);

		graphics.dispose();
		return result;
	}
}
