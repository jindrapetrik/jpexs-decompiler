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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager.PopupEvent;
import org.pushingpixels.flamingo.api.ribbon.*;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizeSequencingPolicy;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandButtonUI;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;
import org.pushingpixels.flamingo.internal.utils.*;

/**
 * Basic UI for ribbon {@link JRibbon}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicRibbonUI extends RibbonUI {
	/**
	 * Client property marking the ribbon component to indicate whether the task
	 * bar and contextual task group headers should be shown on the title pane
	 * of the window. This is only relevant for the {@link JRibbonFrame}.
	 */
	public static final String IS_USING_TITLE_PANE = "ribbon.internal.isUsingTitlePane";

	private static final String JUST_MINIMIZED = "ribbon.internal.justMinimized";

	/**
	 * The associated ribbon.
	 */
	protected JRibbon ribbon;

	/**
	 * Mouse wheel listener to switch between ribbon tasks.
	 */
	// protected MouseWheelListener mouseWheelListener;
	/**
	 * Taskbar panel.
	 */
	protected JPanel taskBarPanel;

	protected JScrollablePanel<BandHostPanel> bandScrollablePanel;

	protected JScrollablePanel<TaskToggleButtonsHostPanel> taskToggleButtonsScrollablePanel;

	protected JRibbonApplicationMenuButton applicationMenuButton;

	protected JCommandButton helpButton;

	/**
	 * Map of toggle buttons of all tasks.
	 */
	protected Map<RibbonTask, JRibbonTaskToggleButton> taskToggleButtons;

	/**
	 * Button group for task toggle buttons.
	 */
	protected CommandToggleButtonGroup taskToggleButtonGroup;

	/**
	 * Change listener.
	 */
	protected ChangeListener ribbonChangeListener;

	/**
	 * Property change listener.
	 */
	protected PropertyChangeListener propertyChangeListener;

	protected ContainerListener ribbonContainerListener;

	protected ComponentListener ribbonComponentListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicRibbonUI();
	}

	/**
	 * Creates a new basic ribbon UI delegate.
	 */
	public BasicRibbonUI() {
		this.taskToggleButtons = new HashMap<RibbonTask, JRibbonTaskToggleButton>();
		this.taskToggleButtonGroup = new CommandToggleButtonGroup();
		this.taskToggleButtonGroup.setAllowsClearingSelection(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.ribbon = (JRibbon) c;
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

		this.ribbon = null;
	}

	/**
	 * Installs listeners on the associated ribbon.
	 */
	protected void installListeners() {
		// this.mouseWheelListener = new MouseWheelListener() {
		// public void mouseWheelMoved(MouseWheelEvent e) {
		// handleMouseWheelEvent(e);
		// }
		// };
		// this.taskToggleButtonsScrollablePanel.getView().addMouseWheelListener(
		// this.mouseWheelListener);
		//
		this.ribbonChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				syncRibbonState();
			}
		};
		this.ribbon.addChangeListener(this.ribbonChangeListener);

		this.propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("selectedTask".equals(evt.getPropertyName())) {
					RibbonTask old = (RibbonTask) evt.getOldValue();
					final RibbonTask curr = (RibbonTask) evt.getNewValue();
					if ((old != null) && (taskToggleButtons.get(old) != null)) {
						taskToggleButtons.get(old).getActionModel()
								.setSelected(false);
					}
					if ((curr != null) && (taskToggleButtons.get(curr) != null)) {
						taskToggleButtons.get(curr).getActionModel()
								.setSelected(true);
					}

					if (isShowingScrollsForTaskToggleButtons()
							&& (curr != null)) {
						// scroll selected task as necessary so that it's
						// visible
						JRibbonTaskToggleButton toggleButton = taskToggleButtons
								.get(curr);
						if (toggleButton != null) {
							scrollAndRevealTaskToggleButton(toggleButton);
						}
					}

					// Special case for showing key tips of ribbon tasks.
					// When a ribbon task is selected with a key tip, its
					// showing and layout is deferred as a separate Runnable
					// on EDT. When the key chain for that task is created,
					// the command buttons are not at their final size yet
					// and no key tips are shown.
					// Here we schedule yet another Runnable
					// to recompute all keytips if the
					// originator is a task toggle button.
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							KeyTipManager ktm = KeyTipManager.defaultManager();
							if (ktm.isShowingKeyTips()) {
								KeyTipManager.KeyTipChain chain = ktm
										.getCurrentlyShownKeyTipChain();
								if (chain.chainParentComponent == taskToggleButtons
										.get(curr)) {
									ktm.refreshCurrentChain();
								}
							}
						}
					});
				}
				if ("applicationMenuRichTooltip".equals(evt.getPropertyName())) {
					syncApplicationMenuTips();
				}
				if ("applicationMenuKeyTip".equals(evt.getPropertyName())) {
					syncApplicationMenuTips();
				}
				if ("applicationMenu".equals(evt.getPropertyName())) {
					ribbon.revalidate();
					ribbon.doLayout();
					ribbon.repaint();
					Window windowAncestor = SwingUtilities
							.getWindowAncestor(ribbon);
					if (windowAncestor instanceof JRibbonFrame) {
						FlamingoUtilities
								.updateRibbonFrameIconImages((JRibbonFrame) windowAncestor);
					}
				}
				if ("minimized".equals(evt.getPropertyName())) {
					PopupPanelManager.defaultManager().hidePopups(null);
					ribbon.revalidate();
					ribbon.doLayout();
					ribbon.repaint();
				}
			}
		};
		this.ribbon.addPropertyChangeListener(this.propertyChangeListener);

		this.ribbonContainerListener = new ContainerAdapter() {
			@Override
			public void componentAdded(ContainerEvent e) {
				if (isUsingTitlePane())
					return;
				Component added = e.getComponent();
				if (added != applicationMenuButton) {
					ribbon.setComponentZOrder(applicationMenuButton, ribbon
							.getComponentCount() - 1);
				}
			}
		};
		this.ribbon.addContainerListener(this.ribbonContainerListener);

		this.ribbonComponentListener = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				KeyTipManager.defaultManager().hideAllKeyTips();
			}
		};
		this.ribbon.addComponentListener(this.ribbonComponentListener);
	}

	/**
	 * Uninstalls listeners from the associated ribbon.
	 */
	protected void uninstallListeners() {
		// this.taskToggleButtonsScrollablePanel.getView()
		// .removeMouseWheelListener(this.mouseWheelListener);
		// this.mouseWheelListener = null;
		//
		this.ribbon.removeChangeListener(this.ribbonChangeListener);
		this.ribbonChangeListener = null;

		this.ribbon.removePropertyChangeListener(this.propertyChangeListener);
		this.propertyChangeListener = null;

		this.ribbon.removeContainerListener(this.ribbonContainerListener);
		this.ribbonContainerListener = null;

		this.ribbon.removeComponentListener(this.ribbonComponentListener);
		this.ribbonComponentListener = null;
	}

	/**
	 * Installs defaults on the associated ribbon.
	 */
	protected void installDefaults() {
		Border b = this.ribbon.getBorder();
		if (b == null || b instanceof UIResource) {
			Border toSet = UIManager.getBorder("Ribbon.border");
			if (toSet == null)
				toSet = new BorderUIResource.EmptyBorderUIResource(1, 2, 1, 2);
			this.ribbon.setBorder(toSet);
		}
	}

	/**
	 * Uninstalls defaults from the associated ribbon.
	 */
	protected void uninstallDefaults() {
	}

	/**
	 * Installs subcomponents on the associated ribbon.
	 */
	protected void installComponents() {
		// taskbar panel
		this.taskBarPanel = new TaskbarPanel();
		this.taskBarPanel.setName("JRibbon Task Bar");
		this.taskBarPanel.setLayout(createTaskbarLayoutManager());
		this.ribbon.add(this.taskBarPanel);

		// band scrollable panel
		BandHostPanel bandHostPanel = createBandHostPanel();
		bandHostPanel.setLayout(createBandHostPanelLayoutManager());
		this.bandScrollablePanel = new JScrollablePanel<BandHostPanel>(
				bandHostPanel, JScrollablePanel.ScrollType.HORIZONTALLY);
		this.bandScrollablePanel.setScrollOnRollover(false);
		this.ribbon.add(this.bandScrollablePanel);

		// task toggle buttons scrollable panel
		TaskToggleButtonsHostPanel taskToggleButtonsHostPanel = createTaskToggleButtonsHostPanel();
		taskToggleButtonsHostPanel
				.setLayout(createTaskToggleButtonsHostPanelLayoutManager());
		this.taskToggleButtonsScrollablePanel = new JScrollablePanel<TaskToggleButtonsHostPanel>(
				taskToggleButtonsHostPanel,
				JScrollablePanel.ScrollType.HORIZONTALLY);
		this.taskToggleButtonsScrollablePanel.setScrollOnRollover(false);
		this.taskToggleButtonsScrollablePanel
				.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						// need to repaint the entire ribbon since scrolling
						// the task toggle buttons affects the contour outline
						// of the ribbon
						ribbon.repaint();
					}
				});
		this.ribbon.add(this.taskToggleButtonsScrollablePanel);

		this.ribbon.setLayout(createLayoutManager());

		this.syncRibbonState();

		this.applicationMenuButton = new JRibbonApplicationMenuButton(
				this.ribbon);
		this.syncApplicationMenuTips();
		this.ribbon.add(applicationMenuButton);
		Window windowAncestor = SwingUtilities.getWindowAncestor(this.ribbon);
		if (windowAncestor instanceof JRibbonFrame) {
			FlamingoUtilities
					.updateRibbonFrameIconImages((JRibbonFrame) windowAncestor);
		}
	}

	protected LayoutManager createTaskToggleButtonsHostPanelLayoutManager() {
		return new TaskToggleButtonsHostPanelLayout();
	}

	protected TaskToggleButtonsHostPanel createTaskToggleButtonsHostPanel() {
		return new TaskToggleButtonsHostPanel();
	}

	protected BandHostPanel createBandHostPanel() {
		return new BandHostPanel();
	}

	protected LayoutManager createBandHostPanelLayoutManager() {
		return new BandHostPanelLayout();
	}

	/**
	 * Uninstalls subcomponents from the associated ribbon.
	 */
	protected void uninstallComponents() {
		this.taskBarPanel.removeAll();
		this.taskBarPanel.setLayout(null);
		this.ribbon.remove(this.taskBarPanel);

		BandHostPanel bandHostPanel = this.bandScrollablePanel.getView();
		bandHostPanel.removeAll();
		bandHostPanel.setLayout(null);
		this.ribbon.remove(this.bandScrollablePanel);

		TaskToggleButtonsHostPanel taskToggleButtonsHostPanel = this.taskToggleButtonsScrollablePanel
				.getView();
		taskToggleButtonsHostPanel.removeAll();
		taskToggleButtonsHostPanel.setLayout(null);
		this.ribbon.remove(this.taskToggleButtonsScrollablePanel);

		this.ribbon.remove(this.applicationMenuButton);
		if (this.helpButton != null)
			this.ribbon.remove(this.helpButton);

		this.ribbon.setLayout(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#update(java.awt.Graphics,
	 * javax.swing.JComponent)
	 */
	@Override
	public void update(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g.create();
		RenderingUtils.installDesktopHints(g2d);
		super.update(g2d, c);
		g2d.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics,
	 * javax.swing.JComponent)
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		this.paintBackground(g);

		if (!ribbon.isMinimized()) {
			Insets ins = c.getInsets();
			int extraHeight = getTaskToggleButtonHeight();
			if (!this.isUsingTitlePane())
				extraHeight += getTaskbarHeight();
			this.paintTaskArea(g, 0, ins.top + extraHeight, c.getWidth(), c
					.getHeight()
					- extraHeight - ins.top - ins.bottom);
		} else {
			this.paintMinimizedRibbonSeparator(g);
		}
	}

	protected void paintMinimizedRibbonSeparator(Graphics g) {
		Color borderColor = FlamingoUtilities.getBorderColor();
		g.setColor(borderColor);
		Insets ins = ribbon.getInsets();
		g.drawLine(0, ribbon.getHeight() - ins.bottom, ribbon.getWidth(),
				ribbon.getHeight() - ins.bottom);
	}

	/**
	 * Paints the ribbon background.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	protected void paintBackground(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();

		g2d.setColor(FlamingoUtilities.getColor(Color.lightGray,
				"Panel.background"));
		g2d.fillRect(0, 0, this.ribbon.getWidth(), this.ribbon.getHeight());

		g2d.dispose();
	}

	/**
	 * Paints the task border.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param x
	 *            Left X of the tasks band bounds.
	 * @param y
	 *            Top Y of the tasks band bounds.
	 * @param width
	 *            Width of the tasks band bounds.
	 * @param height
	 *            Height of the tasks band bounds.
	 */
	protected void paintTaskArea(Graphics g, int x, int y, int width, int height) {
		if (ribbon.getTaskCount() == 0)
			return;

		JRibbonTaskToggleButton selectedTaskButton = this.taskToggleButtons
				.get(this.ribbon.getSelectedTask());
		Rectangle selectedTaskButtonBounds = selectedTaskButton.getBounds();
		Point converted = SwingUtilities.convertPoint(selectedTaskButton
				.getParent(), selectedTaskButtonBounds.getLocation(),
				this.ribbon);
		// System.out.println("Painted " + selectedTaskButtonBounds.x + "->" +
		// converted.x);
		Rectangle taskToggleButtonsViewportBounds = taskToggleButtonsScrollablePanel
				.getView().getParent().getBounds();
		taskToggleButtonsViewportBounds.setLocation(SwingUtilities
				.convertPoint(taskToggleButtonsScrollablePanel,
						taskToggleButtonsViewportBounds.getLocation(),
						this.ribbon));
		int startSelectedX = Math.max(converted.x + 1,
				(int) taskToggleButtonsViewportBounds.getMinX());
		startSelectedX = Math.min(startSelectedX,
				(int) taskToggleButtonsViewportBounds.getMaxX());
		int endSelectedX = Math.min(converted.x
				+ selectedTaskButtonBounds.width - 1,
				(int) taskToggleButtonsViewportBounds.getMaxX());
		endSelectedX = Math.max(endSelectedX,
				(int) taskToggleButtonsViewportBounds.getMinX());
		Shape outerContour = FlamingoUtilities.getRibbonBorderOutline(x + 1, x
				+ width - 3, startSelectedX, endSelectedX, converted.y, y, y
				+ height, 2);

		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(FlamingoUtilities.getBorderColor());
		g2d.draw(outerContour);

		// check whether the currently selected task is a contextual task
		RibbonTask selected = this.ribbon.getSelectedTask();
		RibbonContextualTaskGroup contextualGroup = selected
				.getContextualGroup();
		if (contextualGroup != null) {
			// paint a small gradient directly below the task area
			Insets ins = this.ribbon.getInsets();
			int topY = ins.top + getTaskbarHeight();
			int bottomY = topY + 5;
			Color hueColor = contextualGroup.getHueColor();
			Paint paint = new GradientPaint(0, topY, FlamingoUtilities
					.getAlphaColor(hueColor,
							(int) (255 * RibbonContextualTaskGroup.HUE_ALPHA)),
					0, bottomY, FlamingoUtilities.getAlphaColor(hueColor, 0));
			g2d.setPaint(paint);
			g2d.clip(outerContour);
			g2d.fillRect(0, topY, width, bottomY - topY + 1);
		}

		g2d.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.ribbon.ui.RibbonUI#getContextualGroupTabBounds(org
	 * .jvnet.flamingo.ribbon.RibbonContextualTaskGroup)
	 */
	@Override
	public Rectangle getContextualTaskGroupBounds(
			RibbonContextualTaskGroup group) {
		Rectangle rect = null;
		for (int j = 0; j < group.getTaskCount(); j++) {
			JRibbonTaskToggleButton button = taskToggleButtons.get(group
					.getTask(j));
			if (rect == null)
				rect = button.getBounds();
			else
				rect = rect.union(button.getBounds());
		}
		int buttonGap = getTabButtonGap();
		Point location = SwingUtilities.convertPoint(
				taskToggleButtonsScrollablePanel.getView(), rect.getLocation(),
				ribbon);
		return new Rectangle(location.x - buttonGap / 3, location.y - 1,
				rect.width + buttonGap * 2 / 3 - 1, rect.height + 1);
	}

	/**
	 * Returns the layout gap for the bands in the associated ribbon.
	 * 
	 * @return The layout gap for the bands in the associated ribbon.
	 */
	protected int getBandGap() {
		return 2;
	}

	/**
	 * Returns the layout gap for the tab buttons in the associated ribbon.
	 * 
	 * @return The layout gap for the tab buttons in the associated ribbon.
	 */
	protected int getTabButtonGap() {
		return 6;
	}

	/**
	 * Invoked by <code>installUI</code> to create a layout manager object to
	 * manage the {@link JRibbon}.
	 * 
	 * @return a layout manager object
	 */
	protected LayoutManager createLayoutManager() {
		return new RibbonLayout();
	}

	/**
	 * Invoked by <code>installUI</code> to create a layout manager object to
	 * manage the {@link JRibbon} taskbar.
	 * 
	 * @return a layout manager object
	 */
	protected LayoutManager createTaskbarLayoutManager() {
		return new TaskbarLayout();
	}

	/**
	 * Returns the height of the taskbar area.
	 * 
	 * @return The height of the taskbar area.
	 */
	public int getTaskbarHeight() {
		return 24;
	}

	/**
	 * Returns the height of the task toggle button area.
	 * 
	 * @return The height of the task toggle button area.
	 */
	public int getTaskToggleButtonHeight() {
		return 22;
	}

	/**
	 * Layout for the ribbon.
	 * 
	 * @author Kirill Grouchnikov
	 */
	protected class RibbonLayout implements LayoutManager {
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
			Insets ins = c.getInsets();
			int maxPrefBandHeight = 0;
			boolean isRibbonMinimized = ribbon.isMinimized();
			if (!isRibbonMinimized) {
				if (ribbon.getTaskCount() > 0) {
					RibbonTask selectedTask = ribbon.getSelectedTask();
					for (AbstractRibbonBand<?> ribbonBand : selectedTask
							.getBands()) {
						int bandPrefHeight = ribbonBand.getPreferredSize().height;
						Insets bandInsets = ribbonBand.getInsets();
						maxPrefBandHeight = Math.max(maxPrefBandHeight,
								bandPrefHeight + bandInsets.top
										+ bandInsets.bottom);
					}
				}
			}

			int extraHeight = getTaskToggleButtonHeight();
			if (!isUsingTitlePane())
				extraHeight += getTaskbarHeight();
			int prefHeight = maxPrefBandHeight + extraHeight + ins.top
					+ ins.bottom;
			// System.out.println("Ribbon pref = " + prefHeight);
			return new Dimension(c.getWidth(), prefHeight);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
		 */
		@Override
        public Dimension minimumLayoutSize(Container c) {
			// go over all ribbon bands and sum the width
			// of ribbon buttons (of collapsed state)
			Insets ins = c.getInsets();
			int width = 0;
			int maxMinBandHeight = 0;
			int gap = getBandGap();

			int extraHeight = getTaskToggleButtonHeight();
			if (!isUsingTitlePane())
				extraHeight += getTaskbarHeight();

			if (ribbon.getTaskCount() > 0) {
				boolean isRibbonMinimized = ribbon.isMinimized();
				// minimum is when all the tasks are collapsed
				RibbonTask selectedTask = ribbon.getSelectedTask();
				for (AbstractRibbonBand ribbonBand : selectedTask.getBands()) {
					int bandPrefHeight = ribbonBand.getMinimumSize().height;
					Insets bandInsets = ribbonBand.getInsets();
					RibbonBandUI bandUI = ribbonBand.getUI();
					width += bandUI.getPreferredCollapsedWidth();
					if (!isRibbonMinimized) {
						maxMinBandHeight = Math.max(maxMinBandHeight,
								bandPrefHeight + bandInsets.top
										+ bandInsets.bottom);
					}
				}
				// add inter-band gaps
				width += gap * (selectedTask.getBandCount() - 1);
			} else {
				// fix for issue 44 (empty ribbon)
				width = 50;
			}
			return new Dimension(width, maxMinBandHeight + extraHeight
					+ ins.top + ins.bottom);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
		 */
		@Override
        public void layoutContainer(Container c) {
			// System.out.println("Ribbon real = " + c.getHeight());

			Insets ins = c.getInsets();
			int tabButtonGap = getTabButtonGap();

			boolean ltr = ribbon.getComponentOrientation().isLeftToRight();

			// the top row - task bar components
			int width = c.getWidth();
			int taskbarHeight = getTaskbarHeight();
			int y = ins.top;

			boolean isUsingTitlePane = isUsingTitlePane();
			// handle taskbar only if it is not marked
			if (!isUsingTitlePane) {
				taskBarPanel.removeAll();
				for (Component regComp : ribbon.getTaskbarComponents()) {
					taskBarPanel.add(regComp);
				}
				// taskbar takes all available width
				taskBarPanel.setBounds(ins.left, ins.top, width - ins.left
						- ins.right, taskbarHeight);
				y += taskbarHeight;
			} else {
				taskBarPanel.setBounds(0, 0, 0, 0);
			}

			int taskToggleButtonHeight = getTaskToggleButtonHeight();

			int x = ltr ? ins.left : width - ins.right;
			// the application menu button
			int appMenuButtonSize = taskbarHeight + taskToggleButtonHeight;
			if (!isUsingTitlePane) {
				applicationMenuButton
						.setVisible(ribbon.getApplicationMenu() != null);
				if (ribbon.getApplicationMenu() != null) {
					if (ltr) {
						applicationMenuButton.setBounds(x, ins.top,
								appMenuButtonSize, appMenuButtonSize);
					} else {
						applicationMenuButton.setBounds(x - appMenuButtonSize,
								ins.top, appMenuButtonSize, appMenuButtonSize);
					}
				}
			} else {
				applicationMenuButton.setVisible(false);
			}
			x = ltr ? x + 2 : x - 2;
			if (FlamingoUtilities.getApplicationMenuButton(SwingUtilities
					.getWindowAncestor(ribbon)) != null) {
				x = ltr ? x + appMenuButtonSize : x - appMenuButtonSize;
			}

			// the help button
			if (helpButton != null) {
				Dimension preferred = helpButton.getPreferredSize();
				if (ltr) {
					helpButton.setBounds(width - ins.right - preferred.width,
							y, preferred.width, preferred.height);
				} else {
					helpButton.setBounds(ins.left, y, preferred.width,
							preferred.height);
				}
			}

			// task buttons
			if (ltr) {
				int taskButtonsWidth = (helpButton != null) ? (helpButton
						.getX()
						- tabButtonGap - x) : (c.getWidth() - ins.right - x);
				taskToggleButtonsScrollablePanel.setBounds(x, y,
						taskButtonsWidth, taskToggleButtonHeight);
			} else {
				int taskButtonsWidth = (helpButton != null) ? (x - tabButtonGap
						- helpButton.getX() - helpButton.getWidth())
						: (x - ins.left);
				taskToggleButtonsScrollablePanel.setBounds(
						x - taskButtonsWidth, y, taskButtonsWidth,
						taskToggleButtonHeight);
			}

			TaskToggleButtonsHostPanel taskToggleButtonsHostPanel = taskToggleButtonsScrollablePanel
					.getView();
			int taskToggleButtonsHostPanelMinWidth = taskToggleButtonsHostPanel
					.getMinimumSize().width;
			taskToggleButtonsHostPanel.setPreferredSize(new Dimension(
					taskToggleButtonsHostPanelMinWidth,
					taskToggleButtonsScrollablePanel.getBounds().height));
			taskToggleButtonsScrollablePanel.doLayout();

			y += taskToggleButtonHeight;

			int extraHeight = taskToggleButtonHeight;
			if (!isUsingTitlePane)
				extraHeight += taskbarHeight;

			if (bandScrollablePanel.getParent() == ribbon) {
				if (!ribbon.isMinimized() && (ribbon.getTaskCount() > 0)) {
					// y += ins.top;
					Insets bandInsets = (ribbon.getSelectedTask()
							.getBandCount() == 0) ? new Insets(0, 0, 0, 0)
							: ribbon.getSelectedTask().getBand(0).getInsets();
					bandScrollablePanel.setBounds(1 + ins.left, y
							+ bandInsets.top, c.getWidth() - 2 * ins.left - 2
							* ins.right - 1, c.getHeight() - extraHeight
							- ins.top - ins.bottom - bandInsets.top
							- bandInsets.bottom);
					// System.out.println("Scrollable : "
					// + bandScrollablePanel.getBounds());
					BandHostPanel bandHostPanel = bandScrollablePanel.getView();
					int bandHostPanelMinWidth = bandHostPanel.getMinimumSize().width;
					bandHostPanel.setPreferredSize(new Dimension(
							bandHostPanelMinWidth, bandScrollablePanel
									.getBounds().height));
					bandScrollablePanel.doLayout();
					bandHostPanel.doLayout();
				} else {
					bandScrollablePanel.setBounds(0, 0, 0, 0);
				}
			}
		}
	}

	/**
	 * Layout for the task bar.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private class TaskbarLayout implements LayoutManager {
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
			Insets ins = c.getInsets();
			int pw = 0;
			int gap = getBandGap();
			for (Component regComp : ribbon.getTaskbarComponents()) {
				pw += regComp.getPreferredSize().width;
				pw += gap;
			}
			return new Dimension(pw + ins.left + ins.right, getTaskbarHeight()
					+ ins.top + ins.bottom);
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
			Insets ins = c.getInsets();
			int gap = getBandGap();

			boolean ltr = c.getComponentOrientation().isLeftToRight();

			if (ltr) {
				int x = ins.left + 1;
				if (applicationMenuButton.isVisible()) {
					x += (applicationMenuButton.getX() + applicationMenuButton
							.getWidth());
				}

				for (Component regComp : ribbon.getTaskbarComponents()) {
					int pw = regComp.getPreferredSize().width;
					regComp.setBounds(x, ins.top + 1, pw, c.getHeight()
							- ins.top - ins.bottom - 2);
					x += (pw + gap);
				}
			} else {
				int x = c.getWidth() - ins.right - 1;
				if (applicationMenuButton.isVisible()) {
					x = applicationMenuButton.getX() - 1;
				}

				for (Component regComp : ribbon.getTaskbarComponents()) {
					int pw = regComp.getPreferredSize().width;
					regComp.setBounds(x - pw, ins.top + 1, pw, c.getHeight()
							- ins.top - ins.bottom - 2);
					x -= (pw + gap);
				}
			}
		}
	}

	/**
	 * The taskbar panel that holds the {@link JRibbon#getTaskbarComponents()}.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private class TaskbarPanel extends JPanel {
		/**
		 * Creates the new taskbar panel.
		 */
		public TaskbarPanel() {
			super();
			this.setOpaque(false);
			this.setBorder(new EmptyBorder(1, 0, 1, 0));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			Shape contour = getOutline(this);

			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			RenderingUtils.installDesktopHints(g2d);

			if (contour != null) {
				g2d.setComposite(AlphaComposite.SrcOver.derive(0.6f));
				g2d.setColor(FlamingoUtilities.getColor(Color.lightGray
						.brighter(), "Panel.background"));
				g2d.fill(contour);
				g2d.setColor(FlamingoUtilities.getBorderColor().darker());
				g2d.draw(contour);
			}

			boolean ltr = getComponentOrientation().isLeftToRight();
			int maxX = 0;
			int minX = getWidth();
			if (this.getComponentCount() == 0) {
				maxX = 1;
				minX = getWidth() - 1;
				if (applicationMenuButton.isVisible()) {
					maxX += applicationMenuButton.getX()
							+ applicationMenuButton.getWidth();
					minX = applicationMenuButton.getX() - 1;
				}
			} else {
				for (int i = 0; i < this.getComponentCount(); i++) {
					Component taskBarComp = this.getComponent(i);
					maxX = Math.max(maxX, taskBarComp.getX()
							+ taskBarComp.getWidth());
					minX = Math.min(minX, taskBarComp.getX());
				}
			}
			int height = getHeight();
			if (ltr) {
				g2d.drawLine(maxX, height - 1, getWidth(), height - 1);
			} else {
				g2d.drawLine(0, height - 1, minX, height - 1);
			}

			int contourMaxX = (contour != null) ? (int) contour.getBounds2D()
					.getMaxX() + 6 : 6;
			int contourMinX = (contour != null) ? (int) contour.getBounds2D()
					.getMinX() - 6 : 6;

			// contextual task group headers
			if (!isShowingScrollsForTaskToggleButtons()) {
				g2d.setComposite(AlphaComposite.SrcOver);
				// the taskbar panel is not at the zero X coordinate of the
				// ribbon
				g2d.translate(-this.getBounds().x, 0);
				for (int i = 0; i < ribbon.getContextualTaskGroupCount(); i++) {
					RibbonContextualTaskGroup taskGroup = ribbon
							.getContextualTaskGroup(i);
					if (!ribbon.isVisible(taskGroup))
						continue;
					Rectangle taskGroupBounds = getContextualTaskGroupBounds(taskGroup);

					Color hueColor = taskGroup.getHueColor();
					Paint paint = new GradientPaint(
							0,
							0,
							FlamingoUtilities.getAlphaColor(hueColor, 0),
							0,
							height,
							FlamingoUtilities
									.getAlphaColor(
											hueColor,
											(int) (255 * RibbonContextualTaskGroup.HUE_ALPHA)));
					// translucent gradient paint
					g2d.setPaint(paint);
					int startX = ltr ? taskGroupBounds.x : Math.min(
							contourMinX, taskGroupBounds.x);
					int width = ltr ? taskGroupBounds.x + taskGroupBounds.width
							- startX : Math.min(taskGroupBounds.x
							+ taskGroupBounds.width, contourMinX)
							- startX;

					if (width > 0) {
						g2d.fillRect(startX, 0, width, height);
						// and a solid line at the bottom
						g2d.setColor(hueColor);
						g2d.drawLine(startX + 1, height - 1, startX + width,
								height - 1);

						// task group title
						g2d.setColor(FlamingoUtilities.getColor(Color.black,
								"Button.foreground"));
						FontMetrics fm = this.getFontMetrics(ribbon.getFont());
						int yOffset = (height + fm.getHeight()) / 2
								- fm.getDescent();
						int availableTextWidth = width - 10;
						String titleToShow = taskGroup.getTitle();
						if (fm.stringWidth(titleToShow) > availableTextWidth) {
							while (true) {
								if (titleToShow.length() == 0)
									break;
								if (fm.stringWidth(titleToShow + "...") <= availableTextWidth)
									break;
								titleToShow = titleToShow.substring(0,
										titleToShow.length() - 1);
							}
							titleToShow += "...";
						}
						if (ltr) {
							BasicGraphicsUtils.drawString(g2d, titleToShow, -1,
									startX + 5, yOffset);
						} else {
							BasicGraphicsUtils.drawString(g2d, titleToShow, -1,
									startX + width - 5
											- fm.stringWidth(titleToShow),
									yOffset);
						}

						// separator lines
						Color color = FlamingoUtilities.getBorderColor();
						g2d.setPaint(new GradientPaint(0, 0, FlamingoUtilities
								.getAlphaColor(color, 0), 0, height, color));
						// left line
						g2d.drawLine(startX, 0, startX, height);
						// right line
						g2d.drawLine(startX + width, 0, startX + width, height);
					}
				}
			}

			g2d.dispose();

		}

		/**
		 * Returns the outline of this taskbar panel.
		 * 
		 * @param taskbarPanel
		 *            the taskbar panel to outline
		 * @return The outline of this taskbar panel.
		 */
		protected Shape getOutline(TaskbarPanel taskbarPanel) {
			double height = this.getHeight() - 1;
			boolean ltr = taskbarPanel.getComponentOrientation()
					.isLeftToRight();
			if (this.getComponentCount() == 0) {
				if (applicationMenuButton.isVisible()) {
					// no taskbar components
					if (ltr) {
						int x = 1;
						if (applicationMenuButton.isVisible()) {
							x += applicationMenuButton.getX()
									+ applicationMenuButton.getWidth();
						}
						return new Arc2D.Double(x - 1 - 2 * height, 0,
								2 * height, 2 * height, 0, 90, Arc2D.OPEN);
					} else {
						int x = taskbarPanel.getWidth() - 1;
						if (applicationMenuButton.isVisible()) {
							x = applicationMenuButton.getX() - 1;
						}
						return new Arc2D.Double(x + 1, 0, 2 * height,
								2 * height, 90, 90, Arc2D.OPEN);
					}
				} else {
					return null;
				}
			} else {
				int minX = this.getWidth();
				int maxX = 0;
				for (int i = 0; i < this.getComponentCount(); i++) {
					Component taskBarComp = this.getComponent(i);
					minX = Math.min(minX, taskBarComp.getX());
					maxX = Math.max(maxX, taskBarComp.getX()
							+ taskBarComp.getWidth());
				}

				float radius = (float) height / 2.0f;

				GeneralPath outline = new GeneralPath();

				if (ltr) {
					// top left corner
					if (applicationMenuButton.isVisible()) {
						outline.moveTo(minX + 5 - 2 * radius, 0);
					} else {
						outline.moveTo(minX - 1, 0);
					}
					// top right corner
					outline.lineTo(maxX, 0);
					// right arc
					outline.append(new Arc2D.Double(maxX - radius, 0, height,
							height, 90, -180, Arc2D.OPEN), true);
					// bottom left corner
					outline.lineTo(minX - 1, height);
					if (applicationMenuButton.isVisible()) {
						// left arc
						outline.append(new Arc2D.Double(minX - 1 - 2 * height,
								0, 2 * height, 2 * height, 0, 90, Arc2D.OPEN),
								true);
					} else {
						outline.lineTo(minX - 1, 0);
					}
				} else {
					// top right corner
					if (applicationMenuButton.isVisible()) {
						outline.moveTo(maxX - 5 + 2 * radius, 0);
					} else {
						outline.moveTo(maxX - 1, 0);
					}
					// top left corner
					outline.lineTo(minX, 0);
					// left arc
					outline.append(new Arc2D.Double(minX - radius, 0, height,
							height, 90, 180, Arc2D.OPEN), true);
					// bottom right corner
					outline.lineTo(maxX - 1, height);
					if (applicationMenuButton.isVisible()) {
						outline.append(new Arc2D.Double(maxX - 1, 0,
								2 * height, 2 * height, 180, -90, Arc2D.OPEN),
								true);
					} else {
						outline.lineTo(maxX + 1, 0);
					}

				}

				return outline;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			Dimension result = super.getPreferredSize();
			return new Dimension(result.width + result.height / 2,
					result.height);
		}
	}

	protected static class BandHostPanel extends JPanel {
	}

	/**
	 * Layout for the band host panel.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private class BandHostPanelLayout implements LayoutManager {
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
			// Insets ins = c.getInsets();
			int maxPrefBandHeight = 0;
			if (ribbon.getTaskCount() > 0) {
				RibbonTask selectedTask = ribbon.getSelectedTask();
				for (AbstractRibbonBand<?> ribbonBand : selectedTask.getBands()) {
					int bandPrefHeight = ribbonBand.getPreferredSize().height;
					Insets bandInsets = ribbonBand.getInsets();
					maxPrefBandHeight = Math
							.max(maxPrefBandHeight, bandPrefHeight
									+ bandInsets.top + bandInsets.bottom);
				}
			}

			return new Dimension(c.getWidth(), maxPrefBandHeight);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
		 */
		@Override
        public Dimension minimumLayoutSize(Container c) {
			// go over all ribbon bands and sum the width
			// of ribbon buttons (of collapsed state)
			// Insets ins = c.getInsets();
			int width = 0;
			int maxMinBandHeight = 0;
			int gap = getBandGap();

			// minimum is when all the tasks are collapsed
			RibbonTask selectedTask = ribbon.getSelectedTask();
			// System.out.println(selectedTask.getTitle() + " min width");
			for (AbstractRibbonBand ribbonBand : selectedTask.getBands()) {
				int bandPrefHeight = ribbonBand.getMinimumSize().height;
				Insets bandInsets = ribbonBand.getInsets();
				RibbonBandUI bandUI = ribbonBand.getUI();
				int preferredCollapsedWidth = bandUI
						.getPreferredCollapsedWidth()
						+ bandInsets.left + bandInsets.right;
				width += preferredCollapsedWidth;
				// System.out.println("\t" + ribbonBand.getTitle() + ":" +
				// preferredCollapsedWidth);
				maxMinBandHeight = Math.max(maxMinBandHeight, bandPrefHeight
				// + bandInsets.top + bandInsets.bottom
						);
			}
			// add inter-band gaps
			width += gap * (selectedTask.getBandCount() + 1);
			// System.out.println("\t" + gap + "*" +
			// (selectedTask.getBandCount() + 1));

			// System.out.println(selectedTask.getTitle() + " min width:" +
			// width);

			// System.out.println("Returning min height of " +
			// maxMinBandHeight);

			return new Dimension(width, maxMinBandHeight);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
		 */
		@Override
        public void layoutContainer(Container c) {
			// System.err.println("Layout of band host panel " + c.getWidth() +
			// ":" + c.getHeight());
			int bandGap = getBandGap();

			// the top row - task bar components
			int x = 0;
			int y = 0;

			RibbonTask selectedTask = ribbon.getSelectedTask();
			if (selectedTask == null)
				return;

			// check that the resize policies are still consistent
			for (AbstractRibbonBand<?> band : selectedTask.getBands()) {
				FlamingoUtilities.checkResizePoliciesConsistency(band);
			}

			// start with the most "permissive" resize policy for each band
			for (AbstractRibbonBand<?> band : selectedTask.getBands()) {
				List<RibbonBandResizePolicy> policies = band
						.getResizePolicies();
				RibbonBandResizePolicy last = policies.get(0);
				band.setCurrentResizePolicy(last);
			}

			int availableBandHeight = c.getHeight();
			int availableWidth = c.getWidth();
			if (selectedTask.getBandCount() > 0) {
				RibbonBandResizeSequencingPolicy resizeSequencingPolicy = selectedTask
						.getResizeSequencingPolicy();
				resizeSequencingPolicy.reset();
				AbstractRibbonBand<?> currToTakeFrom = resizeSequencingPolicy
						.next();
				while (true) {
					// check whether all bands have the current resize
					// policy as their last (most restrictive) registered policy
					boolean noMore = true;
					for (AbstractRibbonBand<?> band : selectedTask.getBands()) {
						RibbonBandResizePolicy currentResizePolicy = band
								.getCurrentResizePolicy();
						List<RibbonBandResizePolicy> resizePolicies = band
								.getResizePolicies();
						if (currentResizePolicy != resizePolicies
								.get(resizePolicies.size() - 1)) {
							noMore = false;
							break;
						}
					}
					if (noMore)
						break;

					// get the current preferred width of the bands
					int totalWidth = 0;
					// System.out.println("Iteration");
					for (AbstractRibbonBand<?> ribbonBand : selectedTask
							.getBands()) {
						RibbonBandResizePolicy currentResizePolicy = ribbonBand
								.getCurrentResizePolicy();

						Insets ribbonBandInsets = ribbonBand.getInsets();
						AbstractBandControlPanel controlPanel = ribbonBand
								.getControlPanel();
						if (controlPanel == null) {
							controlPanel = ribbonBand.getPopupRibbonBand()
									.getControlPanel();
						}
						Insets controlPanelInsets = controlPanel.getInsets();
						int controlPanelGap = controlPanel.getUI()
								.getLayoutGap();
						int ribbonBandHeight = availableBandHeight
								- ribbonBandInsets.top
								- ribbonBandInsets.bottom;
						int availableHeight = ribbonBandHeight
								- ribbonBand.getUI().getBandTitleHeight();
						if (controlPanel != null) {
							availableHeight = availableHeight
									- controlPanelInsets.top
									- controlPanelInsets.bottom;
						}
						int preferredWidth = currentResizePolicy
								.getPreferredWidth(availableHeight,
										controlPanelGap)
								+ ribbonBandInsets.left
								+ ribbonBandInsets.right;
						totalWidth += preferredWidth + bandGap;
						// System.out.println("\t"
						// + ribbonBand.getTitle()
						// + ":"
						// + currentResizePolicy.getClass()
						// .getSimpleName() + ":" + preferredWidth
						// + " under " + availableHeight + " with "
						// + controlPanel.getComponentCount()
						// + " children");
					}
					// System.out.println("\t:Total:" + totalWidth + "("
					// + availableWidth + ")");
					// System.out.println("\n");
					if (totalWidth < availableWidth)
						break;

					// try to take from the currently rotating band
					List<RibbonBandResizePolicy> policies = currToTakeFrom
							.getResizePolicies();
					int currPolicyIndex = policies.indexOf(currToTakeFrom
							.getCurrentResizePolicy());
					if (currPolicyIndex == (policies.size() - 1)) {
						// nothing to take
					} else {
						currToTakeFrom.setCurrentResizePolicy(policies
								.get(currPolicyIndex + 1));
					}
					currToTakeFrom = resizeSequencingPolicy.next();
				}
			}

			boolean ltr = c.getComponentOrientation().isLeftToRight();
			x = ltr ? 1 : c.getWidth() - 1;
			// System.out.println("Will get [" + availableWidth + "]:");
			for (AbstractRibbonBand<?> ribbonBand : selectedTask.getBands()) {
				Insets ribbonBandInsets = ribbonBand.getInsets();
				RibbonBandResizePolicy currentResizePolicy = ribbonBand
						.getCurrentResizePolicy();
				AbstractBandControlPanel controlPanel = ribbonBand
						.getControlPanel();
				if (controlPanel == null) {
					controlPanel = ribbonBand.getPopupRibbonBand()
							.getControlPanel();
				}
				Insets controlPanelInsets = controlPanel.getInsets();
				int controlPanelGap = controlPanel.getUI().getLayoutGap();
				int ribbonBandHeight = availableBandHeight;
				// - ribbonBandInsets.top - ribbonBandInsets.bottom;
				int availableHeight = ribbonBandHeight - ribbonBandInsets.top
						- ribbonBandInsets.bottom
						- ribbonBand.getUI().getBandTitleHeight();
				if (controlPanelInsets != null) {
					availableHeight = availableHeight - controlPanelInsets.top
							- controlPanelInsets.bottom;
				}

				int requiredBandWidth = currentResizePolicy.getPreferredWidth(
						availableHeight, controlPanelGap)
						+ ribbonBandInsets.left + ribbonBandInsets.right;

				if (ltr) {
					ribbonBand.setBounds(x, y, requiredBandWidth,
							ribbonBandHeight);
				} else {
					ribbonBand.setBounds(x - requiredBandWidth, y,
							requiredBandWidth, ribbonBandHeight);
				}

				// System.out.println("\t" + ribbonBand.getTitle() + ":"
				// + currentResizePolicy.getClass().getSimpleName() + ":"
				// + requiredBandWidth + " under " + ribbonBandHeight);

				if (ribbonBand.getHeight() > 0) {
					ribbonBand.doLayout();
				}

				if (ltr) {
					x += (requiredBandWidth + bandGap);
				} else {
					x -= (requiredBandWidth + bandGap);
				}

			}
			// System.out.println();
		}
	}

	protected class TaskToggleButtonsHostPanel extends JPanel {
		public static final String IS_SQUISHED = "flamingo.internal.ribbon.taskToggleButtonsHostPanel.isSquished";

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			this.paintContextualTaskGroupsOutlines(g);
			if (Boolean.TRUE.equals(this.getClientProperty(IS_SQUISHED))) {
				this.paintTaskOutlines(g);
			}
		}

		protected void paintTaskOutlines(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();
			Color color = FlamingoUtilities.getBorderColor();
			Paint paint = new GradientPaint(0, 0, FlamingoUtilities
					.getAlphaColor(color, 0), 0, getHeight(), color);
			g2d.setPaint(paint);

			Set<RibbonTask> tasksWithTrailingSeparators = new HashSet<RibbonTask>();
			// add all regular tasks except the last
			for (int i = 0; i < ribbon.getTaskCount() - 1; i++) {
				RibbonTask task = ribbon.getTask(i);
				tasksWithTrailingSeparators.add(task);
				// System.out.println("Added " + task.getTitle());
			}
			// add all tasks of visible contextual groups except last task in
			// each group
			for (int i = 0; i < ribbon.getContextualTaskGroupCount(); i++) {
				RibbonContextualTaskGroup group = ribbon
						.getContextualTaskGroup(i);
				if (ribbon.isVisible(group)) {
					for (int j = 0; j < group.getTaskCount() - 1; j++) {
						RibbonTask task = group.getTask(j);
						tasksWithTrailingSeparators.add(task);
						// System.out.println("Added " + task.getTitle());
					}
				}
			}

			for (RibbonTask taskWithTrailingSeparator : tasksWithTrailingSeparators) {
				JRibbonTaskToggleButton taskToggleButton = taskToggleButtons
						.get(taskWithTrailingSeparator);
				Rectangle bounds = taskToggleButton.getBounds();
				int x = bounds.x + bounds.width + getTabButtonGap() / 2 - 1;
				g2d.drawLine(x, 0, x, getHeight());
				// System.out.println(taskWithTrailingSeparator.getTitle() + ":"
				// + x);
			}

			g2d.dispose();
		}

		/**
		 * Paints the outline of the contextual task groups.
		 * 
		 * @param g
		 *            Graphics context.
		 */
		protected void paintContextualTaskGroupsOutlines(Graphics g) {
			for (int i = 0; i < ribbon.getContextualTaskGroupCount(); i++) {
				RibbonContextualTaskGroup group = ribbon
						.getContextualTaskGroup(i);
				if (!ribbon.isVisible(group))
					continue;
				// go over all the tasks in this group and compute the union
				// of bounds of the matching tab buttons
				Rectangle rect = getContextualTaskGroupBounds(group);
				rect.setLocation(SwingUtilities.convertPoint(ribbon, rect
						.getLocation(), taskToggleButtonsScrollablePanel
						.getView()));
				this.paintContextualTaskGroupOutlines(g, group, rect);
			}
		}

		/**
		 * Paints the outline of the specified contextual task group.
		 * 
		 * @param g
		 *            Graphics context.
		 * @param group
		 *            Contextual task group.
		 * @param groupBounds
		 *            Contextual task group bounds.
		 */
		protected void paintContextualTaskGroupOutlines(Graphics g,
				RibbonContextualTaskGroup group, Rectangle groupBounds) {
			Graphics2D g2d = (Graphics2D) g.create();
			Color color = FlamingoUtilities.getBorderColor();

			Paint paint = new GradientPaint(0, groupBounds.y, color, 0,
					groupBounds.y + groupBounds.height, FlamingoUtilities
							.getAlphaColor(color, 0));
			g2d.setPaint(paint);
			// left line
			int x = groupBounds.x;
			g2d.drawLine(x, groupBounds.y, x, groupBounds.y
					+ groupBounds.height);
			// right line
			x = groupBounds.x + groupBounds.width;
			g2d.drawLine(x, groupBounds.y, x, groupBounds.y
					+ groupBounds.height);

			g2d.dispose();
		}

		// @Override
		// protected void paintComponent(Graphics g) {
		// //g.setColor(new Color(255, 200, 200));
		// //g.fillRect(0, 0, getWidth(), getHeight());
		// // g.setColor(Color.blue.darker());
		// // g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		// //System.err.println(System.currentTimeMillis() + ": tt-repaint");
		// }
		//		
		// @Override
		// protected void paintBorder(Graphics g) {
		// }
		//
		// @Override
		// public void setBounds(int x, int y, int width, int height) {
		// System.out.println("Host : " + x + ":" + y + ":" + width + ":"
		// + height);
		// super.setBounds(x, y, width, height);
		// }
	}

	/**
	 * Layout for the band host panel.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private class TaskToggleButtonsHostPanelLayout implements LayoutManager {
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
			int tabButtonGap = getTabButtonGap();
			int taskToggleButtonHeight = getTaskToggleButtonHeight();

			int totalTaskButtonsWidth = 0;
			List<RibbonTask> visibleTasks = getCurrentlyShownRibbonTasks();
			for (RibbonTask task : visibleTasks) {
				JRibbonTaskToggleButton tabButton = taskToggleButtons.get(task);
				int pw = tabButton.getPreferredSize().width;
				totalTaskButtonsWidth += (pw + tabButtonGap);
			}

			return new Dimension(totalTaskButtonsWidth, taskToggleButtonHeight);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
		 */
		@Override
        public Dimension minimumLayoutSize(Container c) {
			int tabButtonGap = getTabButtonGap();
			int taskToggleButtonHeight = getTaskToggleButtonHeight();

			int totalTaskButtonsWidth = 0;
			List<RibbonTask> visibleTasks = getCurrentlyShownRibbonTasks();
			for (RibbonTask task : visibleTasks) {
				JRibbonTaskToggleButton tabButton = taskToggleButtons.get(task);
				int pw = tabButton.getMinimumSize().width;
				totalTaskButtonsWidth += (pw + tabButtonGap);
			}

			return new Dimension(totalTaskButtonsWidth, taskToggleButtonHeight);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
		 */
		@Override
        public void layoutContainer(Container c) {
			int y = 0;
			int tabButtonGap = getTabButtonGap();
			int taskToggleButtonHeight = getTaskToggleButtonHeight();

			int totalPrefWidth = 0;
			int totalMinWidth = 0;
			List<RibbonTask> visibleTasks = getCurrentlyShownRibbonTasks();
			Map<JRibbonTaskToggleButton, Integer> diffMap = new HashMap<JRibbonTaskToggleButton, Integer>();
			int totalDiff = 0;
			for (RibbonTask task : visibleTasks) {
				JRibbonTaskToggleButton tabButton = taskToggleButtons.get(task);
				int pw = tabButton.getPreferredSize().width;
				int mw = tabButton.getMinimumSize().width;
				diffMap.put(tabButton, pw - mw);
				totalDiff += (pw - mw);
				totalPrefWidth += pw;
				totalMinWidth += mw;
			}
			totalPrefWidth += tabButtonGap * visibleTasks.size();
			totalMinWidth += tabButtonGap * visibleTasks.size();

			boolean ltr = c.getComponentOrientation().isLeftToRight();

			// do we have enough width?
			if (totalPrefWidth <= c.getWidth()) {
				// compute bounds for the tab buttons
				int x = ltr ? 0 : c.getWidth();
				for (RibbonTask task : visibleTasks) {
					JRibbonTaskToggleButton tabButton = taskToggleButtons
							.get(task);
					int pw = tabButton.getPreferredSize().width;
					if (ltr) {
						tabButton.setBounds(x, y + 1, pw,
								taskToggleButtonHeight - 1);
						x += (pw + tabButtonGap);
					} else {
						tabButton.setBounds(x - pw, y + 1, pw,
								taskToggleButtonHeight - 1);
						x -= (pw + tabButtonGap);
					}
					tabButton.setActionRichTooltip(null);
				}
				((JComponent) c).putClientProperty(
						TaskToggleButtonsHostPanel.IS_SQUISHED, null);
			} else {
				if (totalMinWidth > c.getWidth()) {
					throw new IllegalStateException(
							"Available width not enough to host minimized task tab buttons");
				}
				int x = ltr ? 0 : c.getWidth();
				// how much do we need to take from each toggle button?
				int toDistribute = totalPrefWidth - c.getWidth() + 2;
				for (RibbonTask task : visibleTasks) {
					JRibbonTaskToggleButton tabButton = taskToggleButtons
							.get(task);
					int pw = tabButton.getPreferredSize().width;
					int delta = (toDistribute * diffMap.get(tabButton) / totalDiff);
					int finalWidth = pw - delta;
					if (ltr) {
						tabButton.setBounds(x, y + 1, finalWidth,
								taskToggleButtonHeight - 1);
						x += (finalWidth + tabButtonGap);
					} else {
						tabButton.setBounds(x - finalWidth, y + 1, finalWidth,
								taskToggleButtonHeight - 1);
						x -= (finalWidth + tabButtonGap);
					}
					// show the tooltip with the full title
					RichTooltip tooltip = new RichTooltip();
					tooltip.setTitle(task.getTitle());
					tabButton.setActionRichTooltip(tooltip);
				}
				((JComponent) c).putClientProperty(
						TaskToggleButtonsHostPanel.IS_SQUISHED, Boolean.TRUE);
			}
		}
	}

	protected void syncRibbonState() {
		// remove all existing ribbon bands
		BandHostPanel bandHostPanel = this.bandScrollablePanel.getView();
		bandHostPanel.removeAll();

		// remove all the existing task toggle buttons
		TaskToggleButtonsHostPanel taskToggleButtonsHostPanel = this.taskToggleButtonsScrollablePanel
				.getView();
		taskToggleButtonsHostPanel.removeAll();

		// remove the help button
		if (this.helpButton != null) {
			this.ribbon.remove(this.helpButton);
			this.helpButton = null;
		}

		// go over all visible ribbon tasks and create a toggle button
		// for each one of them
		List<RibbonTask> visibleTasks = this.getCurrentlyShownRibbonTasks();
		final RibbonTask selectedTask = this.ribbon.getSelectedTask();
		for (final RibbonTask task : visibleTasks) {
			final JRibbonTaskToggleButton taskToggleButton = new JRibbonTaskToggleButton(
					task);
			taskToggleButton.setKeyTip(task.getKeyTip());
			// wire listener to select the task when the button is
			// selected
			taskToggleButton.addActionListener(new ActionListener() {
				@Override
                public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
                        public void run() {
							scrollAndRevealTaskToggleButton(taskToggleButton);

							ribbon.setSelectedTask(task);

							// System.out.println("Button click on "
							// + task.getTitle() + ", ribbon minimized? "
							// + ribbon.isMinimized());

							if (ribbon.isMinimized()) {
								if (Boolean.TRUE.equals(ribbon
										.getClientProperty(JUST_MINIMIZED))) {
									ribbon.putClientProperty(JUST_MINIMIZED,
											null);
									return;
								}

								// special case - do we have this task currently
								// shown in a popup?
								List<PopupPanelManager.PopupInfo> popups = PopupPanelManager
										.defaultManager().getShownPath();
								if (popups.size() > 0) {
									for (PopupPanelManager.PopupInfo popup : popups) {
										if (popup.getPopupOriginator() == taskToggleButton) {
											// hide all popups and return (hides
											// the task popup and does not
											// show any additional popup).
											PopupPanelManager.defaultManager()
													.hidePopups(null);
											return;
										}
									}
								}

								PopupPanelManager.defaultManager().hidePopups(
										null);
								ribbon.remove(bandScrollablePanel);

								int prefHeight = bandScrollablePanel.getView()
										.getPreferredSize().height;
								Insets ins = ribbon.getInsets();
								prefHeight += ins.top + ins.bottom;
								AbstractRibbonBand band = (ribbon
										.getSelectedTask().getBandCount() > 0) ? ribbon
										.getSelectedTask().getBand(0)
										: null;
								if (band != null) {
									Insets bandIns = band.getInsets();
									prefHeight += bandIns.top + bandIns.bottom;
								}

								// System.out.println(prefHeight
								// + ":"
								// + bandScrollablePanel.getView()
								// .getComponentCount());

								JPopupPanel popupPanel = new BandHostPopupPanel(
										bandScrollablePanel, new Dimension(
												ribbon.getWidth(), prefHeight));

								int x = ribbon.getLocationOnScreen().x;
								int y = ribbon.getLocationOnScreen().y
										+ ribbon.getHeight();

								// make sure that the popup stays in
								// bounds
								Rectangle scrBounds = ribbon
										.getGraphicsConfiguration().getBounds();
								int pw = popupPanel.getPreferredSize().width;
								if ((x + pw) > (scrBounds.x + scrBounds.width)) {
									x = scrBounds.x + scrBounds.width - pw;
								}
								int ph = popupPanel.getPreferredSize().height;
								if ((y + ph) > (scrBounds.y + scrBounds.height)) {
									y = scrBounds.y + scrBounds.height - ph;
								}

								// get the popup and show it
								popupPanel.setPreferredSize(new Dimension(
										ribbon.getWidth(), prefHeight));
								Popup popup = PopupFactory.getSharedInstance()
										.getPopup(taskToggleButton, popupPanel,
												x, y);
								PopupPanelManager.PopupListener tracker = new PopupPanelManager.PopupListener() {
									@Override
									public void popupShown(PopupEvent event) {
										JComponent originator = event
												.getPopupOriginator();
										if (originator instanceof JRibbonTaskToggleButton) {
											bandScrollablePanel.doLayout();
											bandScrollablePanel.repaint();
										}
									}

									@Override
									public void popupHidden(PopupEvent event) {
										JComponent originator = event
												.getPopupOriginator();
										if (originator instanceof JRibbonTaskToggleButton) {
											ribbon.add(bandScrollablePanel);
											PopupPanelManager.defaultManager()
													.removePopupListener(this);
											ribbon.revalidate();
											ribbon.doLayout();
											ribbon.repaint();
										}
									}
								};
								PopupPanelManager.defaultManager()
										.addPopupListener(tracker);
								PopupPanelManager.defaultManager().addPopup(
										taskToggleButton, popup, popupPanel);
							}
						}
					});
				}
			});
			// wire listener to toggle ribbon minimization on double
			// mouse click
			taskToggleButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if ((ribbon.getSelectedTask() == task)
							&& (e.getClickCount() == 2)) {
						boolean wasMinimized = ribbon.isMinimized();
						ribbon.setMinimized(!wasMinimized);
						if (!wasMinimized) {
							// fix for issue 69 - mark the ribbon as
							// "just minimized" to prevent the action handler
							// of the toggle button to show the ribbon in
							// popup mode
							ribbon.putClientProperty(JUST_MINIMIZED,
									Boolean.TRUE);
						}
					}
				}
			});
			// set the background hue color on the tab buttons
			// of tasks in contextual groups
			if (task.getContextualGroup() != null) {
				taskToggleButton.setContextualGroupHueColor(task
						.getContextualGroup().getHueColor());
			}

			taskToggleButton.putClientProperty(
					BasicCommandButtonUI.DONT_DISPOSE_POPUPS, Boolean.TRUE);

			this.taskToggleButtonGroup.add(taskToggleButton);
			taskToggleButtonsHostPanel.add(taskToggleButton);
			this.taskToggleButtons.put(task, taskToggleButton);
		}

		JRibbonTaskToggleButton toSelect = this.taskToggleButtons
				.get(selectedTask);
		if (toSelect != null) {
			toSelect.getActionModel().setSelected(true);
		}

		for (int i = 0; i < this.ribbon.getTaskCount(); i++) {
			RibbonTask task = this.ribbon.getTask(i);
			for (AbstractRibbonBand band : task.getBands()) {
				bandHostPanel.add(band);
				band.setVisible(selectedTask == task);
			}
		}
		for (int i = 0; i < this.ribbon.getContextualTaskGroupCount(); i++) {
			RibbonContextualTaskGroup taskGroup = this.ribbon
					.getContextualTaskGroup(i);
			for (int j = 0; j < taskGroup.getTaskCount(); j++) {
				RibbonTask task = taskGroup.getTask(j);
				for (AbstractRibbonBand band : task.getBands()) {
					bandHostPanel.add(band);
					band.setVisible(selectedTask == task);
				}
			}
		}

		ActionListener helpListener = this.ribbon.getHelpActionListener();
		if (helpListener != null) {
			this.helpButton = new JCommandButton("", this.ribbon.getHelpIcon());
			this.helpButton.setDisplayState(CommandButtonDisplayState.SMALL);
			this.helpButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
			this.helpButton.getActionModel().addActionListener(helpListener);
			this.ribbon.add(this.helpButton);
		}

		this.ribbon.revalidate();
		this.ribbon.repaint();
	}

	/**
	 * Returns the list of currently shown ribbon tasks. This method is for
	 * internal use only.
	 * 
	 * @return The list of currently shown ribbon tasks.
	 */
	protected List<RibbonTask> getCurrentlyShownRibbonTasks() {
		List<RibbonTask> result = new ArrayList<RibbonTask>();

		// add all regular tasks
		for (int i = 0; i < this.ribbon.getTaskCount(); i++) {
			RibbonTask task = this.ribbon.getTask(i);
			result.add(task);
		}
		// add all tasks of visible contextual groups
		for (int i = 0; i < this.ribbon.getContextualTaskGroupCount(); i++) {
			RibbonContextualTaskGroup group = this.ribbon
					.getContextualTaskGroup(i);
			if (this.ribbon.isVisible(group)) {
				for (int j = 0; j < group.getTaskCount(); j++) {
					RibbonTask task = group.getTask(j);
					result.add(task);
				}
			}
		}

		return result;
	}

	protected boolean isUsingTitlePane() {
		return Boolean.TRUE.equals(ribbon
				.getClientProperty(IS_USING_TITLE_PANE));
	}

	protected void syncApplicationMenuTips() {
		this.applicationMenuButton.setPopupRichTooltip(this.ribbon
				.getApplicationMenuRichTooltip());
		this.applicationMenuButton.setPopupKeyTip(this.ribbon
				.getApplicationMenuKeyTip());
	}

	@Override
	public boolean isShowingScrollsForTaskToggleButtons() {
		return this.taskToggleButtonsScrollablePanel.isShowingScrollButtons();
	}

	@Override
	public boolean isShowingScrollsForBands() {
		return this.bandScrollablePanel.isShowingScrollButtons();
	}

	public Map<RibbonTask, JRibbonTaskToggleButton> getTaskToggleButtons() {
		return Collections.unmodifiableMap(taskToggleButtons);
	}

	protected static class BandHostPopupPanel extends JPopupPanel {
		/**
		 * The main component of <code>this</code> popup panel. Can be
		 * <code>null</code>.
		 */
		// protected Component component;
		public BandHostPopupPanel(Component component, Dimension originalSize) {
			// this.component = component;
			this.setLayout(new BorderLayout());
			this.add(component, BorderLayout.CENTER);
			// System.out.println("Popup dim is " + originalSize);
			this.setPreferredSize(originalSize);
			this.setSize(originalSize);
		}
	}

	@Override
	public void handleMouseWheelEvent(MouseWheelEvent e) {
		// no mouse wheel scrolling when the ribbon is minimized
		if (ribbon.isMinimized())
			return;

		// get the visible tasks
		final List<RibbonTask> visibleTasks = getCurrentlyShownRibbonTasks();
		if (visibleTasks.size() == 0)
			return;

		int delta = e.getWheelRotation();
		if (delta == 0)
			return;

		// find the index of the currently selected task
		int currSelectedTaskIndex = visibleTasks.indexOf(ribbon
				.getSelectedTask());

		// compute the next task
		if (!ribbon.getComponentOrientation().isLeftToRight())
			delta = -delta;
		int newSelectedTaskIndex = currSelectedTaskIndex
				+ ((delta > 0) ? 1 : -1);
		if (newSelectedTaskIndex < 0)
			return;
		if (newSelectedTaskIndex >= visibleTasks.size())
			return;

		final int indexToSet = newSelectedTaskIndex;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
            public void run() {
				ribbon
						.setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
				ribbon.setSelectedTask(visibleTasks.get(indexToSet));
				ribbon.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}

	protected void scrollAndRevealTaskToggleButton(
			final JRibbonTaskToggleButton taskToggleButton) {
		// scroll the viewport of the scrollable panel
		// so that the button is fully viewed.
		Point loc = SwingUtilities.convertPoint(taskToggleButton.getParent(),
				taskToggleButton.getLocation(),
				taskToggleButtonsScrollablePanel.getView());
		taskToggleButtonsScrollablePanel.scrollToIfNecessary(loc.x,
				taskToggleButton.getWidth());
	}
}
