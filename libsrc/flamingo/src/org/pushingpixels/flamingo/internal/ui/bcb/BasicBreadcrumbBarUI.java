/*
 * Copyright (c) 2003-2010 Flamingo Kirill Grouchnikov
 * and <a href="http://www.topologi.com">Topologi</a>. 
 * Contributed by <b>Rick Jelliffe</b> of <b>Topologi</b> 
 * in January 2006. in All Rights Reserved.
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
 *  o Neither the name of Flamingo Kirill Grouchnikov Topologi nor the names of 
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
package org.pushingpixels.flamingo.internal.ui.bcb;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.pushingpixels.flamingo.api.bcb.*;
import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonPopupOrientationKind;
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.model.PopupButtonModel;
import org.pushingpixels.flamingo.api.common.popup.*;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 * Basic UI for breadcrumb bar ({@link JBreadcrumbBar}).
 * 
 * @author Topologi
 * @author Kirill Grouchnikov
 * @author Pawel Hajda
 */
public class BasicBreadcrumbBarUI extends BreadcrumbBarUI {
	/**
	 * The associated breadcrumb bar.
	 */
	protected JBreadcrumbBar breadcrumbBar;

	protected JPanel mainPanel;

	protected JScrollablePanel<JPanel> scrollerPanel;

	protected ComponentListener componentListener;

	protected JCommandButton dummy;

	/**
	 * Contains the item path.
	 */
	protected LinkedList modelStack;

	protected LinkedList<JCommandButton> buttonStack;

	protected BreadcrumbPathListener pathListener;

	private AtomicInteger atomicCounter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicBreadcrumbBarUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.breadcrumbBar = (JBreadcrumbBar) c;

		this.modelStack = new LinkedList();
		this.buttonStack = new LinkedList<JCommandButton>();

		installDefaults(this.breadcrumbBar);
		installComponents(this.breadcrumbBar);
		installListeners(this.breadcrumbBar);

		c.setLayout(createLayoutManager());

		if (this.breadcrumbBar.getCallback() != null) {
			SwingWorker<List<StringValuePair>, Void> worker = new SwingWorker<List<StringValuePair>, Void>() {
				@Override
				protected List<StringValuePair> doInBackground()
						throws Exception {
					return breadcrumbBar.getCallback().getPathChoices(null);
				}

				@Override
				protected void done() {
					try {
						pushChoices(new BreadcrumbItemChoices(null, get()));
					} catch (Exception exc) {
					}
				}
			};
			worker.execute();
		}

		this.dummy = new JCommandButton("Dummy", new EmptyResizableIcon(16));
		this.dummy.setDisplayState(CommandButtonDisplayState.MEDIUM);
		this.dummy
				.setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
	 */
	@Override
	public void uninstallUI(JComponent c) {
		c.setLayout(null);

		uninstallListeners((JBreadcrumbBar) c);
		uninstallComponents((JBreadcrumbBar) c);
		uninstallDefaults((JBreadcrumbBar) c);
		this.breadcrumbBar = null;
	}

	protected void installDefaults(JBreadcrumbBar bar) {
		Font currFont = bar.getFont();
		if ((currFont == null) || (currFont instanceof UIResource)) {
			Font font = FlamingoUtilities.getFont(null, "BreadcrumbBar.font",
					"Button.font", "Panel.font");
			bar.setFont(font);
		}
	}

	protected void installComponents(JBreadcrumbBar bar) {
		this.mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.mainPanel.setOpaque(false);
		this.scrollerPanel = new JScrollablePanel<JPanel>(this.mainPanel,
				JScrollablePanel.ScrollType.HORIZONTALLY);

		bar.add(this.scrollerPanel, BorderLayout.CENTER);
	}

	protected void installListeners(final JBreadcrumbBar bar) {
		this.atomicCounter = new AtomicInteger(0);

		this.componentListener = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateComponents();
			}
		};
		bar.addComponentListener(this.componentListener);

		this.pathListener = new BreadcrumbPathListener() {
			private SwingWorker<Void, Object> pathChangeWorker;

			@Override
			public void breadcrumbPathEvent(BreadcrumbPathEvent event) {
				final int indexOfFirstChange = event.getIndexOfFirstChange();

				if ((this.pathChangeWorker != null)
						&& !this.pathChangeWorker.isDone()) {
					this.pathChangeWorker.cancel(true);
				}
				this.pathChangeWorker = new SwingWorker<Void, Object>() {
					@Override
					protected Void doInBackground() throws Exception {
						atomicCounter.incrementAndGet();

						synchronized (BasicBreadcrumbBarUI.this) {
							// remove stack elements after the first change
							if (indexOfFirstChange == 0) {
								modelStack.clear();
							} else {
								int toLeave = indexOfFirstChange * 2 + 1;
								while (modelStack.size() > toLeave)
									modelStack.removeLast();
							}
						}

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								updateComponents();
							}
						});

						if (indexOfFirstChange == 0) {
							List<StringValuePair> rootChoices = breadcrumbBar
									.getCallback().getPathChoices(null);
							BreadcrumbItemChoices bic = new BreadcrumbItemChoices(
									null, rootChoices);
							if (!this.isCancelled()) {
								publish(bic);
							}
						}

						List<BreadcrumbItem> items = breadcrumbBar.getModel()
								.getItems();
						if (items != null) {
							for (int itemIndex = indexOfFirstChange; itemIndex < items
									.size(); itemIndex++) {
								if (this.isCancelled())
									break;

								BreadcrumbItem item = items.get(itemIndex);
								publish(item);

								// now check if it has any children
								List<BreadcrumbItem> subPath = new ArrayList<BreadcrumbItem>();
								for (int j = 0; j <= itemIndex; j++) {
									subPath.add(items.get(j));
								}
								BreadcrumbItemChoices bic = new BreadcrumbItemChoices(
										item, breadcrumbBar.getCallback()
												.getPathChoices(subPath));
								if ((bic.getChoices() != null)
										&& (bic.getChoices().length > 0)) {
									// add the selector - the current item has
									// children
									publish(bic);
								}
							}
						}
						return null;
					}

					@Override
					protected void process(List<Object> chunks) {
						if (chunks != null) {
							for (Object chunk : chunks) {
								if (this.isCancelled()
										|| atomicCounter.get() > 1)
									break;

								if (chunk instanceof BreadcrumbItemChoices) {
									pushChoices((BreadcrumbItemChoices) chunk,
											false);
								}
								if (chunk instanceof BreadcrumbItem) {
									pushChoice((BreadcrumbItem) chunk, false);
								}
							}
						}
						updateComponents();
					}

					@Override
					protected void done() {
						atomicCounter.decrementAndGet();
					}
				};
				pathChangeWorker.execute();
			}
		};
		this.breadcrumbBar.getModel().addPathListener(this.pathListener);
	}

	protected void uninstallDefaults(JBreadcrumbBar bar) {
	}

	protected void uninstallComponents(JBreadcrumbBar bar) {
		this.mainPanel.removeAll();
		this.buttonStack.clear();

		bar.remove(this.scrollerPanel);
	}

	protected void uninstallListeners(JBreadcrumbBar bar) {
		bar.removeComponentListener(this.componentListener);
		this.componentListener = null;

		this.breadcrumbBar.getModel().removePathListener(this.pathListener);
		this.pathListener = null;
	}

	/**
	 * Invoked by <code>installUI</code> to create a layout manager object to
	 * manage the {@link JBreadcrumbBar}.
	 * 
	 * @return a layout manager object
	 * 
	 * @see BreadcrumbBarLayout
	 */
	protected LayoutManager createLayoutManager() {
		return new BreadcrumbBarLayout();
	}

	/**
	 * Layout for the breadcrumb bar.
	 * 
	 * @author Kirill Grouchnikov
	 * @author Topologi
	 */
	protected class BreadcrumbBarLayout implements LayoutManager {
		/**
		 * Creates new layout manager.
		 */
		public BreadcrumbBarLayout() {
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
			// The height of breadcrumb bar is
			// computed based on the preferred height of a command
			// button in MEDIUM state.
			int buttonHeight = dummy.getPreferredSize().height;

			Insets ins = c.getInsets();
			return new Dimension(c.getWidth(), buttonHeight + ins.top
					+ ins.bottom);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
		 */
		@Override
        public Dimension minimumLayoutSize(Container c) {
			int buttonHeight = dummy.getPreferredSize().height;

			return new Dimension(10, buttonHeight);
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
			scrollerPanel.setBounds(0, 0, width, height);
		}
	}

	protected synchronized void updateComponents() {
		if (!this.breadcrumbBar.isVisible())
			return;

		this.mainPanel.removeAll();
		buttonStack.clear();
		// update the ui
		for (int i = 0; i < modelStack.size(); i++) {
			Object element = modelStack.get(i);
			if (element instanceof BreadcrumbItemChoices) {
				BreadcrumbItemChoices bic = (BreadcrumbItemChoices) element;
				if (buttonStack.isEmpty()) {
					JCommandButton button = new JCommandButton("");
					button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
					configureBreadcrumbButton(button);
					configurePopupAction(button, bic);
					configurePopupRollover(button);
					buttonStack.add(button);
				} else {
					JCommandButton button = buttonStack.getLast();
					int oldW = button.getPreferredSize().width;
					button
							.setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
					configurePopupAction(button, bic);
					configurePopupRollover(button);
				}
			} else if (element instanceof BreadcrumbItem) {
				BreadcrumbItem bi = (BreadcrumbItem) element;

				JCommandButton button = new JCommandButton(bi.getKey());
				configureBreadcrumbButton(button);
				configureMainAction(button, bi);
				final Icon icon = bi.getIcon();
				if (icon != null) {
					button.setIcon(new ResizableIcon() {
						int iw = icon.getIconWidth();
						int ih = icon.getIconHeight();

						@Override
						public void paintIcon(Component c, Graphics g, int x,
								int y) {
							int dx = (iw - icon.getIconWidth()) / 2;
							int dy = (ih - icon.getIconHeight()) / 2;
							icon.paintIcon(c, g, x + dx, y + dy);
						}

						@Override
						public int getIconWidth() {
							return iw;
						}

						@Override
						public int getIconHeight() {
							return ih;
						}

						@Override
						public void setDimension(Dimension newDimension) {
							iw = newDimension.width;
							ih = newDimension.height;
						}
					});
				}

				if (i > 0) {
					BreadcrumbItemChoices lastBic = (BreadcrumbItemChoices) modelStack
							.get(i - 1);
					BreadcrumbItem[] choices = lastBic.getChoices();
					if (choices != null) {
						for (int j = 0; j < choices.length; j++) {
							if (bi.getKey().equals(choices[j].getKey())) {
								lastBic.setSelectedIndex(j);
								break;
							}
						}
					}
				}

				buttonStack.addLast(button);
			}
		}

		for (JCommandButton jcb : buttonStack) {
			this.mainPanel.add(jcb);
		}

		this.scrollerPanel.revalidate();
		this.scrollerPanel.repaint();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// scroll to the last element in the breadcrumb bar
				scrollerPanel.scrollToIfNecessary(
						mainPanel.getPreferredSize().width, 0);
				scrollerPanel.repaint();
			}
		});
	}

	private void configureMainAction(JCommandButton button,
			final BreadcrumbItem bi) {
		button.getActionModel().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						BreadcrumbBarModel barModel = breadcrumbBar.getModel();
						int itemIndex = barModel.indexOf(bi);
						int toLeave = (itemIndex < 0) ? 0 : itemIndex + 1;
						barModel.setCumulative(true);
						while (barModel.getItemCount() > toLeave) {
							barModel.removeLast();
						}
						barModel.setCumulative(false);
					}
				});
			}
		});
	}

	private void configurePopupAction(JCommandButton button,
			final BreadcrumbItemChoices bic) {
		button.setPopupCallback(new PopupPanelCallback() {
			@Override
			public JPopupPanel getPopupPanel(JCommandButton commandButton) {
				JCommandPopupMenu popup = new JCommandPopupMenu();
				for (int i = 0; i < bic.getChoices().length; i++) {
					final BreadcrumbItem bi = bic.getChoices()[i];

					JCommandMenuButton menuButton = new JCommandMenuButton(bi
							.getKey(), null);
					final Icon icon = bi.getIcon();
					if (icon != null) {
						menuButton.setIcon(new ResizableIcon() {
							int iw = icon.getIconWidth();
							int ih = icon.getIconHeight();

							@Override
							public void paintIcon(Component c, Graphics g,
									int x, int y) {
								int dx = (iw - icon.getIconWidth()) / 2;
								int dy = (ih - icon.getIconHeight()) / 2;
								icon.paintIcon(c, g, x + dx, y + dy);
							}

							@Override
							public int getIconWidth() {
								return iw;
							}

							@Override
							public int getIconHeight() {
								return ih;
							}

							@Override
							public void setDimension(Dimension newDimension) {
								iw = newDimension.width;
								ih = newDimension.height;
							}
						});
					}
					if (i == bic.getSelectedIndex()) {
						menuButton.setFont(menuButton.getFont().deriveFont(
								Font.BOLD));
					}

					final int biIndex = i;
					menuButton.getActionModel().addActionListener(
							new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
                                        public void run() {
											if (bi == null)
												return;

											BreadcrumbBarModel barModel = breadcrumbBar
													.getModel();
											barModel.setCumulative(true);
											int itemIndex = barModel
													.indexOf(bic.getAncestor());
											int toLeave = ((bic.getAncestor() == null) || (itemIndex < 0)) ? 0
													: itemIndex + 1;
											while (barModel.getItemCount() > toLeave) {
												barModel.removeLast();
											}
											barModel.addLast(bi);

											bic.setSelectedIndex(biIndex);

											barModel.setCumulative(false);
										}
									});
								}
							});

					popup.addMenuButton(menuButton);
					menuButton.setCursor(Cursor
							.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				popup.setMaxVisibleMenuButtons(10);
				return popup;
			}
		});
	}

	private void configurePopupRollover(final JCommandButton button) {
		button.getPopupModel().addChangeListener(new ChangeListener() {
			boolean rollover = button.getPopupModel().isRollover();

			@Override
			public void stateChanged(ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						boolean isRollover = button.getPopupModel()
								.isRollover();
						if (isRollover == rollover)
							return;

						if (isRollover) {
							// does any *other* button show popup?
							for (JCommandButton bcbButton : buttonStack) {
								if (bcbButton == button)
									continue;

								if (bcbButton.getPopupModel().isPopupShowing()) {
									// scroll to view
									scrollerPanel.scrollToIfNecessary(button
											.getBounds().x, button.getWidth());
									// simulate click on the popup area
									// of *this* button
									button.doPopupClick();
								}
							}
						}

						rollover = isRollover;
					}
				});
			}
		});
	}

	private void configureBreadcrumbButton(final JCommandButton button) {
		button.setDisplayState(CommandButtonDisplayState.MEDIUM);
		button
				.setPopupOrientationKind(CommandButtonPopupOrientationKind.SIDEWARD);
		button.setHGapScaleFactor(0.75);
		button.getPopupModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PopupButtonModel model = button.getPopupModel();
				boolean displayDownwards = model.isRollover()
						|| model.isPopupShowing();
				CommandButtonPopupOrientationKind popupOrientationKind = displayDownwards ? CommandButtonPopupOrientationKind.DOWNWARD
						: CommandButtonPopupOrientationKind.SIDEWARD;
				button.setPopupOrientationKind(popupOrientationKind);
			}
		});
	}

	/**
	 * Pushes a choice to the top position of the stack. If the current top is
	 * already a {@link BreadcrumbItemChoices}, replace it.
	 * 
	 * @param bic
	 *            The choice item to push.
	 * @return The item that has been pushed.
	 */
	protected Object pushChoices(BreadcrumbItemChoices bic) {
		return pushChoices(bic, true);
	}

	/**
	 * Pushes a choice to the top position of the stack. If the current top is
	 * already a {@link BreadcrumbItemChoices}, replace it.
	 * 
	 * @param bic
	 *            The choice item to push.
	 * @param toUpdateUI
	 *            Indication whether the bar should be repainted.
	 * @return The item that has been pushed.
	 */
	protected synchronized Object pushChoices(BreadcrumbItemChoices bic,
			boolean toUpdateUI) {
		if (bic == null)
			return null;
		if (modelStack.size() % 2 == 1) {
			modelStack.pop();
		}
		modelStack.addLast(bic);
		if (toUpdateUI) {
			updateComponents();
		}
		return bic;
	}

	/**
	 * Pushes an item to the top position of the stack. If the current top is
	 * already a {@link BreadcrumbItemChoices}, replace it.
	 * 
	 * @param bi
	 *            The item to push.
	 * @param toUpdateUI
	 *            Indication whether the bar should be repainted.
	 * @return The item that has been pushed.
	 */
	protected synchronized Object pushChoice(BreadcrumbItem bi,
			boolean toUpdateUI) {
		assert (bi != null);
		Object result;
		// synchronized (stack) {
		if (!modelStack.isEmpty() && modelStack.size() % 2 == 0) {
			modelStack.pop();
		}
		bi.setIndex(modelStack.size());
		modelStack.addLast(bi);
		// }
		return bi;
	}
}