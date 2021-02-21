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
package org.pushingpixels.flamingo.internal.ui.common;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JScrollablePanel;
import org.pushingpixels.flamingo.api.common.JScrollablePanel.ScrollType;
import org.pushingpixels.flamingo.internal.utils.DoubleArrowResizableIcon;

/**
 * Basic UI for scrollable panel {@link JScrollablePanel}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicScrollablePanelUI extends ScrollablePanelUI {
	/**
	 * The associated scrollable panel.
	 */
	protected JScrollablePanel scrollablePanel;

	private JPanel viewport;

	private JCommandButton leadingScroller;

	private JCommandButton trailingScroller;

	private int viewOffset;

	private MouseWheelListener mouseWheelListener;

	private PropertyChangeListener propertyChangeListener;

	private ComponentListener componentListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicScrollablePanelUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.scrollablePanel = (JScrollablePanel) c;
		super.installUI(this.scrollablePanel);
		installDefaults();
		installComponents();
		installListeners();
	}

	protected void installListeners() {
		this.mouseWheelListener = new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (scrollablePanel.getScrollType() != JScrollablePanel.ScrollType.VERTICALLY) {
					return;
				}

				int scrollAmount = 8 * e.getScrollAmount()
						* e.getWheelRotation();
				viewOffset += scrollAmount;
				syncScrolling();
			}
		};
		this.scrollablePanel.addMouseWheelListener(this.mouseWheelListener);

		this.propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("scrollOnRollover".equals(evt.getPropertyName())) {
					boolean isScrollOnRollover = (Boolean) evt.getNewValue();
					leadingScroller.setFireActionOnRollover(isScrollOnRollover);
					trailingScroller
							.setFireActionOnRollover(isScrollOnRollover);
				}
			}
		};
		this.scrollablePanel
				.addPropertyChangeListener(this.propertyChangeListener);

		if (this.scrollablePanel.getView() != null) {
			this.componentListener = new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					scrollablePanel.doLayout();
				}
			};
			this.scrollablePanel.getView().addComponentListener(
					this.componentListener);

		}
	}

	protected void installComponents() {
		this.viewport = new JPanel(new LayoutManager() {
			@Override
			public void addLayoutComponent(String name, Component comp) {
			}

			@Override
			public void removeLayoutComponent(Component comp) {
			}

			@Override
			public Dimension preferredLayoutSize(Container parent) {
				return new Dimension(10, 10);
			}

			@Override
			public Dimension minimumLayoutSize(Container parent) {
				return preferredLayoutSize(parent);
			}

			@Override
			public void layoutContainer(Container parent) {
				JComponent view = scrollablePanel.getView();
				if (scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY) {
					int viewWidth = view.getPreferredSize().width;
					int availWidth = parent.getWidth();

					int offsetX = -viewOffset;
					view.setBounds(offsetX, 0, Math.max(viewWidth, availWidth),
							parent.getHeight());
				} else {
					int viewHeight = view.getPreferredSize().height;
					int availHeight = parent.getHeight();

					int offsetY = -viewOffset;
					view.setBounds(0, offsetY, parent.getWidth(), Math.max(
							viewHeight, availHeight));
				}
			}
		});
		JComponent view = scrollablePanel.getView();
		if (view != null) {
			this.viewport.add(view);
		}
		this.scrollablePanel.add(this.viewport);

		this.leadingScroller = this.createLeadingScroller();
		this.configureLeftScrollerButtonAction();
		this.scrollablePanel.add(this.leadingScroller);

		this.trailingScroller = this.createTrailingScroller();
		this.configureRightScrollerButtonAction();
		this.scrollablePanel.add(this.trailingScroller);
	}

	protected void installDefaults() {
		this.scrollablePanel.setLayout(new ScrollablePanelLayout());
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
		super.uninstallUI(this.scrollablePanel);
	}

	protected void uninstallDefaults() {
	}

	protected void uninstallComponents() {
		this.scrollablePanel.remove(this.viewport);
		this.scrollablePanel.remove(this.leadingScroller);
		this.scrollablePanel.remove(this.trailingScroller);
	}

	protected void uninstallListeners() {
		this.scrollablePanel
				.removePropertyChangeListener(this.propertyChangeListener);
		this.propertyChangeListener = null;

		this.scrollablePanel.removeMouseWheelListener(this.mouseWheelListener);
		this.mouseWheelListener = null;

		if (this.scrollablePanel.getView() != null) {
			this.scrollablePanel.getView().removeComponentListener(
					this.componentListener);
			this.componentListener = null;
		}
	}

	protected JCommandButton createLeadingScroller() {
		JCommandButton b = new JCommandButton(
				null,
				new DoubleArrowResizableIcon(
						new Dimension(9, 9),
						this.scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY ? SwingConstants.WEST
								: SwingConstants.NORTH));

		b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		b.setFocusable(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.putClientProperty(BasicCommandButtonUI.EMULATE_SQUARE_BUTTON,
				Boolean.TRUE);
		b.putClientProperty(BasicCommandButtonUI.DONT_DISPOSE_POPUPS,
				Boolean.TRUE);
		return b;
	}

	protected JCommandButton createTrailingScroller() {
		JCommandButton b = new JCommandButton(
				null,
				new DoubleArrowResizableIcon(
						new Dimension(9, 9),
						this.scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY ? SwingConstants.EAST
								: SwingConstants.SOUTH));

		b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		b.setFocusable(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.putClientProperty(BasicCommandButtonUI.EMULATE_SQUARE_BUTTON,
				Boolean.TRUE);
		b.putClientProperty(BasicCommandButtonUI.DONT_DISPOSE_POPUPS,
				Boolean.TRUE);
		return b;
	}

	private void syncScrolling() {
		this.scrollablePanel.doLayout();
	}

	public void removeScrollers() {
		if (this.leadingScroller.getParent() == this.scrollablePanel) {
			this.scrollablePanel.remove(this.leadingScroller);
			this.scrollablePanel.remove(this.trailingScroller);
			syncScrolling();
			this.scrollablePanel.revalidate();
			this.scrollablePanel.repaint();
		}
	}

	private void addScrollers() {
		this.scrollablePanel.add(this.leadingScroller);
		this.scrollablePanel.add(this.trailingScroller);
		this.scrollablePanel.revalidate();
		JComponent view = this.scrollablePanel.getView();
		view.setPreferredSize(view.getMinimumSize());
		view.setSize(view.getMinimumSize());
		this.scrollablePanel.doLayout();

		this.scrollablePanel.repaint();
	}

	protected void configureLeftScrollerButtonAction() {
		this.leadingScroller.setAutoRepeatAction(true);
		this.leadingScroller.setAutoRepeatActionIntervals(200, 50);
		this.leadingScroller.setFireActionOnRollover(this.scrollablePanel
				.isScrollOnRollover());
		this.leadingScroller.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewOffset -= 12;
				syncScrolling();
			}
		});
	}

	protected void configureRightScrollerButtonAction() {
		this.trailingScroller.setAutoRepeatAction(true);
		this.trailingScroller.setAutoRepeatActionIntervals(200, 50);
		this.trailingScroller.setFireActionOnRollover(this.scrollablePanel
				.isScrollOnRollover());
		this.trailingScroller.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viewOffset += 12;
				syncScrolling();
			}
		});
	}

	@Override
	public void scrollToIfNecessary(int startPosition, int span) {
		if (this.scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY) {
			if (this.scrollablePanel.getComponentOrientation().isLeftToRight()) {
				revealRightEdge(startPosition, span);
				revealLeftEdge(startPosition);
			} else {
				revealLeftEdge(startPosition);
				revealRightEdge(startPosition, span);
			}
		} else {
			revealBottomEdge(startPosition, span);
			revealTopEdge(startPosition);
		}
	}

	private void revealLeftEdge(int x) {
		if (x < viewOffset) {
			// left edge is not visible
			viewOffset = x - 5;
			syncScrolling();
		}
	}

	private void revealRightEdge(int x, int width) {
		if ((x + width) > (viewOffset + viewport.getWidth())) {
			// right edge is not visible
			viewOffset = x + width - viewport.getWidth() + 5;
			syncScrolling();
		}
	}

	private void revealTopEdge(int y) {
		if (y < viewOffset) {
			// top edge is not visible
			viewOffset = y - 5;
			syncScrolling();
		}
	}

	private void revealBottomEdge(int y, int height) {
		if ((y + height) > (viewOffset + viewport.getHeight())) {
			// bottom edge is not visible
			viewOffset = y + height - viewport.getHeight() + 5;
			syncScrolling();
		}
	}

	@Override
	public boolean isShowingScrollButtons() {
		return (this.leadingScroller.isVisible());
	}

	/**
	 * Layout for the scrollable panel.
	 * 
	 * @author Kirill Grouchnikov
	 * @author Topologi
	 */
	protected class ScrollablePanelLayout implements LayoutManager {
		/**
		 * Creates new layout manager.
		 */
		public ScrollablePanelLayout() {
		}

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
			if (scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY) {
				return new Dimension(c.getWidth(), 21);
			} else {
				return new Dimension(21, c.getHeight());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
		 */
		@Override
        public Dimension minimumLayoutSize(Container c) {
			if (scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY) {
				return new Dimension(10, 21);
			} else {
				return new Dimension(21, 10);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
		 */
		@Override
        public void layoutContainer(Container c) {
			int width = c.getWidth();
			int height = c.getHeight();

			Insets ins = c.getInsets();

			JComponent view = scrollablePanel.getView();
			Dimension viewPrefSize = view.getPreferredSize();

			// System.out.println(width + "*" + height + " - "
			// + viewPrefSize.width + "*" + viewPrefSize.height);

			if (scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY) {
				boolean shouldShowScrollerButtons = (viewPrefSize.width > width);

				leadingScroller.setVisible(shouldShowScrollerButtons);
				trailingScroller.setVisible(shouldShowScrollerButtons);

				int scrollPanelWidth = shouldShowScrollerButtons ? width
						- ins.left - ins.right
						- leadingScroller.getPreferredSize().width
						- trailingScroller.getPreferredSize().width - 4 : width
						- ins.left - ins.right;
				int x = ins.left;
				if (shouldShowScrollerButtons) {
					int spw = leadingScroller.getPreferredSize().width;
					leadingScroller.setBounds(x, ins.top, spw, height - ins.top
							- ins.bottom);
					x += spw + 2;
				}
				viewport.setBounds(x, ins.top, scrollPanelWidth, height
						- ins.top - ins.bottom);

				int viewPreferredWidth = view.getPreferredSize().width;
				if (viewOffset < 0) {
					viewOffset = 0;
				}
				if ((viewPreferredWidth > 0)
						&& (viewOffset + scrollPanelWidth > viewPreferredWidth)) {
					viewOffset = Math.max(0, viewPreferredWidth
							- scrollPanelWidth);
				}
				viewport.doLayout();

				x += scrollPanelWidth + 2;
				if (shouldShowScrollerButtons) {
					int spw = trailingScroller.getPreferredSize().width;
					trailingScroller.setBounds(x, ins.top, spw, height
							- ins.top - ins.bottom);
				}
			} else {
				boolean shouldShowScrollerButtons = (viewPrefSize.height > height);

				leadingScroller.setVisible(shouldShowScrollerButtons);
				trailingScroller.setVisible(shouldShowScrollerButtons);

				int scrollPanelHeight = shouldShowScrollerButtons ? height
						- ins.top - ins.bottom
						- leadingScroller.getPreferredSize().height
						- trailingScroller.getPreferredSize().height - 4
						: height - ins.top - ins.bottom;
				int y = ins.top;
				if (shouldShowScrollerButtons) {
					int sph = leadingScroller.getPreferredSize().height;
					leadingScroller.setBounds(ins.left, y, width - ins.left
							- ins.right, sph);
					y += sph + 2;
				}
				viewport.setBounds(ins.left, y, width - ins.left - ins.right,
						scrollPanelHeight);

				int viewPreferredHeight = view.getPreferredSize().height;
				if (viewOffset < 0) {
					viewOffset = 0;
				}
				if ((viewPreferredHeight > 0)
						&& (viewOffset + scrollPanelHeight > viewPreferredHeight)) {
					viewOffset = Math.max(0, viewPreferredHeight
							- scrollPanelHeight);
				}
				viewport.doLayout();

				y += scrollPanelHeight + 2;
				if (shouldShowScrollerButtons) {
					int sph = trailingScroller.getPreferredSize().height;
					trailingScroller.setBounds(ins.left, y, width - ins.left
							- ins.right, sph);
				}
			}

			if (scrollablePanel.getScrollType() == ScrollType.HORIZONTALLY) {
				trailingScroller
						.setEnabled((viewOffset + viewport.getWidth()) < view
								.getWidth());
			} else {
				trailingScroller
						.setEnabled((viewOffset + viewport.getHeight()) < view
								.getHeight());
			}
			leadingScroller.setEnabled(viewOffset > 0);
		}
	}

}
