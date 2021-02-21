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
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorConvertOp;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager.CommandButtonLayoutInfo;
import org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager.CommandButtonSeparatorOrientation;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip.StripOrientation;
import org.pushingpixels.flamingo.api.common.icon.FilteredResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.model.PopupButtonModel;
import org.pushingpixels.flamingo.api.common.popup.*;
import org.pushingpixels.flamingo.internal.utils.*;

/**
 * Basic UI for command button {@link JCommandButton}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicCommandButtonUI extends CommandButtonUI {
	/**
	 * The associated command button.
	 */
	protected AbstractCommandButton commandButton;

	/**
	 * Indication whether the mouse pointer is over the associated command
	 * button.
	 */
	protected boolean isUnderMouse;

	/**
	 * Property change listener.
	 */
	protected PropertyChangeListener propertyChangeListener;

	/**
	 * Tracks user interaction with the command button (including keyboard and
	 * mouse).
	 */
	protected BasicCommandButtonListener basicPopupButtonListener;

	/**
	 * Layout information.
	 */
	protected CommandButtonLayoutManager.CommandButtonLayoutInfo layoutInfo;

	/**
	 * Client property to mark the command button to have square corners. This
	 * client property is for internal use only.
	 */
	public static final String EMULATE_SQUARE_BUTTON = "flamingo.internal.commandButton.ui.emulateSquare";

	/**
	 * Client property to mark the command button to not dispose the popups on
	 * activation.
	 * 
	 * @see #disposePopupsActionListener
	 */
	public static final String DONT_DISPOSE_POPUPS = "flamingo.internal.commandButton.ui.dontDisposePopups";

	/**
	 * This listener disposes all popup panels when button's action is
	 * activated. An example of scenario would be a command button in the popup
	 * panel of an in-ribbon gallery. When this command button is activated, the
	 * associated popup panel is dismissed.
	 * 
	 * @see #DONT_DISPOSE_POPUPS
	 */
	protected ActionListener disposePopupsActionListener;

	/**
	 * Action listener on the popup area.
	 */
	protected PopupActionListener popupActionListener;

	/**
	 * The "expand" action icon.
	 */
	protected ResizableIcon popupActionIcon;

	protected CommandButtonLayoutManager layoutManager;

	/**
	 * Used to provide a LAF-consistent appearance under core LAFs.
	 */
	protected CellRendererPane buttonRendererPane;

	/**
	 * Used to provide a LAF-consistent appearance under core LAFs.
	 */
	protected AbstractButton rendererButton;

	/**
	 * Used to paint the separator between the action and popup areas.
	 */
	protected JSeparator rendererSeparator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicCommandButtonUI();
	}

	/**
	 * Creates a new UI delegate.
	 */
	public BasicCommandButtonUI() {
		// this.toTakeSavedDimension = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.commandButton = (AbstractCommandButton) c;
		installDefaults();
		installComponents();
		installListeners();
		installKeyboardActions();

		this.layoutManager = this.commandButton.getDisplayState()
				.createLayoutManager(this.commandButton);

		this.updateCustomDimension();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
	 */
	@Override
	public void uninstallUI(JComponent c) {
		c.setLayout(null);

		uninstallKeyboardActions();
		uninstallListeners();
		uninstallComponents();
		uninstallDefaults();
		this.commandButton = null;
	}

	/**
	 * Installs defaults on the associated command button.
	 */
	protected void installDefaults() {
		configureRenderer();

		this.updateBorder();
		this.syncDisabledIcon();
	}

	protected void configureRenderer() {
		this.buttonRendererPane = new CellRendererPane();
		this.commandButton.add(buttonRendererPane);
		this.rendererButton = createRendererButton();
		this.rendererButton.setOpaque(false);
		this.rendererSeparator = new JSeparator();
		Font currFont = this.commandButton.getFont();
		if ((currFont == null) || (currFont instanceof UIResource)) {
			this.commandButton.setFont(this.rendererButton.getFont());
		}
		// special handling for Mac OS X native look-and-feel
		this.rendererButton.putClientProperty("JButton.buttonType", "square");
	}

	protected void updateBorder() {
		Border currBorder = this.commandButton.getBorder();
		if ((currBorder == null) || (currBorder instanceof UIResource)) {
			int tb = (int) (this.commandButton.getVGapScaleFactor() * 4);
			int lr = (int) (this.commandButton.getHGapScaleFactor() * 6);
			this.commandButton
					.setBorder(new BorderUIResource.EmptyBorderUIResource(tb,
							lr, tb, lr));
		}
	}

	/**
	 * Creates the renderer button.
	 * 
	 * @return The renderer button.
	 */
	protected AbstractButton createRendererButton() {
		return new JButton("");
	}

	/**
	 * Installs subcomponents on the associated command button.
	 */
	protected void installComponents() {
		this.updatePopupActionIcon();

		ResizableIcon buttonIcon = this.commandButton.getIcon();
		if (buttonIcon instanceof AsynchronousLoading) {
			((AsynchronousLoading) buttonIcon)
					.addAsynchronousLoadListener(new AsynchronousLoadListener() {
						@Override
                        public void completed(boolean success) {
							if (success && (commandButton != null))
								commandButton.repaint();
						}
					});
		}

		if (this.commandButton instanceof JCommandButton) {
			this.popupActionIcon = this.createPopupActionIcon();
		}
	}

	/**
	 * Installs listeners on the associated command button.
	 */
	protected void installListeners() {
		this.basicPopupButtonListener = createButtonListener(this.commandButton);
		if (this.basicPopupButtonListener != null) {
			this.commandButton.addMouseListener(this.basicPopupButtonListener);
			this.commandButton
					.addMouseMotionListener(this.basicPopupButtonListener);
			this.commandButton.addFocusListener(this.basicPopupButtonListener);
			this.commandButton.addChangeListener(this.basicPopupButtonListener);
		}

		this.propertyChangeListener = new PropertyChangeListener() {
			@Override
            public void propertyChange(PropertyChangeEvent evt) {
				if (AbstractButton.ICON_CHANGED_PROPERTY.equals(evt
						.getPropertyName())) {
					Icon newIcon = (Icon) evt.getNewValue();
					if (newIcon instanceof AsynchronousLoading) {
						AsynchronousLoading async = (AsynchronousLoading) newIcon;
						async
								.addAsynchronousLoadListener(new AsynchronousLoadListener() {
									@Override
                                    public void completed(boolean success) {
										if (success) {
											if (commandButton != null) {
												syncIconDimension();
												syncDisabledIcon();
												commandButton.repaint();
											}
										}
									}
								});
						if (!async.isLoading()) {
							syncIconDimension();
							syncDisabledIcon();
							commandButton.repaint();
						}
					} else {
						syncIconDimension();
						syncDisabledIcon();
						commandButton.revalidate();
						commandButton.repaint();
					}
				}
				if ("commandButtonKind".equals(evt.getPropertyName())) {
					updatePopupActionIcon();
				}
				if ("popupOrientationKind".equals(evt.getPropertyName())) {
					updatePopupActionIcon();
				}
				if ("customDimension".equals(evt.getPropertyName())) {
					updateCustomDimension();
				}
				if ("hgapScaleFactor".equals(evt.getPropertyName())) {
					updateBorder();
				}
				if ("vgapScaleFactor".equals(evt.getPropertyName())) {
					updateBorder();
				}

				if ("popupModel".equals(evt.getPropertyName())) {
					// rewire the popup action listener on the new popup model
					PopupButtonModel oldModel = (PopupButtonModel) evt
							.getOldValue();
					PopupButtonModel newModel = (PopupButtonModel) evt
							.getNewValue();

					if (oldModel != null) {
						oldModel.removePopupActionListener(popupActionListener);
						popupActionListener = null;
					}

					if (newModel != null) {
						popupActionListener = createPopupActionListener();
						newModel.addPopupActionListener(popupActionListener);
					}
				}
				if ("displayState".equals(evt.getPropertyName()) || ("enabled".equals(evt.getPropertyName()) && !((Boolean) evt.getNewValue()))) {
					syncIconDimension();
					syncDisabledIcon();

					commandButton.invalidate();
					commandButton.revalidate();
					commandButton.doLayout();
				}

				// pass the event to the layout manager
				if (layoutManager != null) {
					layoutManager.propertyChange(evt);
				}

				if ("componentOrientation".equals(evt.getPropertyName())) {
					updatePopupActionIcon();
					commandButton.repaint();
				}
			}
		};
		this.commandButton
				.addPropertyChangeListener(this.propertyChangeListener);

		this.disposePopupsActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean toDismiss = !Boolean.TRUE.equals(commandButton
						.getClientProperty(DONT_DISPOSE_POPUPS));
				if (toDismiss) {
					JCommandPopupMenu menu = (JCommandPopupMenu) SwingUtilities
							.getAncestorOfClass(JCommandPopupMenu.class,
									commandButton);
					if (menu != null) {
						toDismiss = menu.isToDismissOnChildClick();
					}
				}
				if (toDismiss) {
					if (SwingUtilities.getAncestorOfClass(JPopupPanel.class,
							commandButton) != null) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								// command button may be cleared if the
								// button click resulted in LAF switch
								if (commandButton != null) {
									// clear the active states
									commandButton.getActionModel().setPressed(
											false);
									commandButton.getActionModel().setRollover(
											false);
									commandButton.getActionModel().setArmed(
											false);
								}
							}
						});
					}
					PopupPanelManager.defaultManager().hidePopups(null);
				}
			}
		};
		this.commandButton.addActionListener(this.disposePopupsActionListener);

		if (this.commandButton instanceof JCommandButton) {
			this.popupActionListener = this.createPopupActionListener();
			((JCommandButton) this.commandButton).getPopupModel()
					.addPopupActionListener(this.popupActionListener);
		}

	}

	/**
	 * Creates the icon for the popup area.
	 * 
	 * @return The icon for the popup area.
	 */
	protected ResizableIcon createPopupActionIcon() {
		return FlamingoUtilities
				.getCommandButtonPopupActionIcon((JCommandButton) this.commandButton);
	}

	/**
	 * Creates the button listener for the specified command button.
	 * 
	 * @param b
	 *            Command button.
	 * @return The button listener for the specified command button.
	 */
	protected BasicCommandButtonListener createButtonListener(
			AbstractCommandButton b) {
		return new BasicCommandButtonListener();
	}

	/**
	 * Installs the keyboard actions on the associated command button.
	 */
	protected void installKeyboardActions() {
		if (this.basicPopupButtonListener != null) {
			basicPopupButtonListener.installKeyboardActions(this.commandButton);
		}
	}

	/**
	 * Uninstalls defaults from the associated command button.
	 */
	protected void uninstallDefaults() {
		unconfigureRenderer();
	}

	protected void unconfigureRenderer() {
		if (this.buttonRendererPane != null) {
			this.commandButton.remove(this.buttonRendererPane);
		}
		this.buttonRendererPane = null;
	}

	/**
	 * Uninstalls subcomponents from the associated command button.
	 */
	protected void uninstallComponents() {
	}

	/**
	 * Uninstalls listeners from the associated command button.
	 */
	protected void uninstallListeners() {
		if (this.basicPopupButtonListener != null) {
			this.commandButton
					.removeMouseListener(this.basicPopupButtonListener);
			this.commandButton
					.removeMouseListener(this.basicPopupButtonListener);
			this.commandButton
					.removeMouseMotionListener(this.basicPopupButtonListener);
			this.commandButton
					.removeFocusListener(this.basicPopupButtonListener);
			this.commandButton
					.removeChangeListener(this.basicPopupButtonListener);
		}

		this.commandButton
				.removePropertyChangeListener(this.propertyChangeListener);
		this.propertyChangeListener = null;

		this.commandButton
				.removeActionListener(this.disposePopupsActionListener);
		this.disposePopupsActionListener = null;

		if (this.commandButton instanceof JCommandButton) {
			((JCommandButton) this.commandButton).getPopupModel()
					.removePopupActionListener(this.popupActionListener);
			this.popupActionListener = null;
		}
	}

	/**
	 * Uninstalls the keyboard actions from the associated command button.
	 */
	protected void uninstallKeyboardActions() {
		if (this.basicPopupButtonListener != null) {
			this.basicPopupButtonListener
					.uninstallKeyboardActions(this.commandButton);
		}
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
		g.setFont(FlamingoUtilities.getFont(commandButton, "Ribbon.font",
				"Button.font", "Panel.font"));
		this.layoutInfo = this.layoutManager.getLayoutInfo(this.commandButton,
				g);
		commandButton.putClientProperty("icon.bounds", layoutInfo.iconRect);

		if (this.isPaintingBackground()) {
			this.paintButtonBackground(g, new Rectangle(0, 0, commandButton
					.getWidth(), commandButton.getHeight()));
		}
		// Graphics2D g2d = (Graphics2D) g.create();
		// g2d.setColor(new Color(255, 0, 0, 64));
		// if (getActionClickArea() != null) {
		// g2d.fill(getActionClickArea());
		// }
		// g2d.setColor(new Color(0, 0, 255, 64));
		// if (getPopupClickArea() != null) {
		// g2d.fill(getPopupClickArea());
		// }
		// g2d.dispose();

		if (layoutInfo.iconRect != null) {
			this.paintButtonIcon(g, layoutInfo.iconRect);
		}
		if (layoutInfo.popupActionRect.getWidth() > 0) {
			paintPopupActionIcon(g, layoutInfo.popupActionRect);
		}
		FontMetrics fm = g.getFontMetrics();

		boolean isTextPaintedEnabled = commandButton.isEnabled();
		if (commandButton instanceof JCommandButton) {
			JCommandButton jCommandButton = (JCommandButton) commandButton;
			isTextPaintedEnabled = layoutInfo.isTextInActionArea ? jCommandButton
					.getActionModel().isEnabled()
					: jCommandButton.getPopupModel().isEnabled();
		}

		g.setColor(getForegroundColor(isTextPaintedEnabled));

		if (layoutInfo.textLayoutInfoList != null) {
			for (CommandButtonLayoutManager.TextLayoutInfo mainTextLayoutInfo : layoutInfo.textLayoutInfoList) {
				if (mainTextLayoutInfo.text != null) {
					BasicGraphicsUtils.drawString(g, mainTextLayoutInfo.text,
							-1, mainTextLayoutInfo.textRect.x,
							mainTextLayoutInfo.textRect.y + fm.getAscent());
				}
			}
		}

		if (isTextPaintedEnabled) {
			g.setColor(FlamingoUtilities.getColor(Color.gray,
					"Label.disabledForeground"));
		} else {
			g.setColor(FlamingoUtilities.getColor(Color.gray,
					"Label.disabledForeground").brighter());
		}

		if (layoutInfo.extraTextLayoutInfoList != null) {
			for (CommandButtonLayoutManager.TextLayoutInfo extraTextLayoutInfo : layoutInfo.extraTextLayoutInfoList) {
				if (extraTextLayoutInfo.text != null) {
					BasicGraphicsUtils.drawString(g, extraTextLayoutInfo.text,
							-1, extraTextLayoutInfo.textRect.x,
							extraTextLayoutInfo.textRect.y + fm.getAscent());
				}
			}
		}

		if (this.isPaintingSeparators() && (layoutInfo.separatorArea != null)) {
			if (layoutInfo.separatorOrientation == CommandButtonSeparatorOrientation.HORIZONTAL) {
				this
						.paintButtonHorizontalSeparator(g,
								layoutInfo.separatorArea);
			} else {
				this.paintButtonVerticalSeparator(g, layoutInfo.separatorArea);
			}
		}

		// Graphics2D g2d = (Graphics2D) g.create();
		//
		// g2d.setColor(Color.red);
		// g2d.draw(layoutInfo.iconRect);
		// g2d.setColor(Color.blue);
		// if (layoutInfo.textLayoutInfoList != null) {
		// for (CommandButtonLayoutManager.TextLayoutInfo mainTextLayoutInfo :
		// layoutInfo.textLayoutInfoList) {
		// if (mainTextLayoutInfo.text != null) {
		// g2d.draw(mainTextLayoutInfo.textRect);
		// }
		// }
		// }
		// g2d.setColor(Color.magenta);
		// if (layoutInfo.extraTextLayoutInfoList != null) {
		// for (CommandButtonLayoutManager.TextLayoutInfo extraTextLayoutInfo :
		// layoutInfo.extraTextLayoutInfoList) {
		// if (extraTextLayoutInfo.text != null) {
		// g2d.draw(extraTextLayoutInfo.textRect);
		// }
		// }
		// }
		// g2d.setColor(Color.green);
		// g2d.draw(layoutInfo.popupActionRect);
		// g2d.dispose();
	}

	protected Color getForegroundColor(boolean isTextPaintedEnabled) {
		if (isTextPaintedEnabled) {
			return FlamingoUtilities.getColor(Color.black, "Button.foreground");
		} else {
			return FlamingoUtilities.getColor(Color.gray,
					"Label.disabledForeground");
		}
	}

	/**
	 * Paints the icon of the popup area.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param popupActionRect
	 */
	protected void paintPopupActionIcon(Graphics g, Rectangle popupActionRect) {
		int size = Math.max(popupActionRect.width - 2, 7);
		if (size % 2 == 0)
			size--;
		popupActionIcon.setDimension(new Dimension(size, size));
		popupActionIcon.paintIcon(this.commandButton, g, popupActionRect.x
				+ (popupActionRect.width - size) / 2, popupActionRect.y
				+ (popupActionRect.height - size) / 2);
	}

	/**
	 * Returns the current icon.
	 * 
	 * @return Current icon.
	 */
	protected Icon getIconToPaint() {
		return (toUseDisabledIcon() && this.commandButton.getDisabledIcon() != null) ? this.commandButton
				.getDisabledIcon()
				: this.commandButton.getIcon();
	}

	protected boolean toUseDisabledIcon() {
		// special case for command buttons with POPUP_ONLY kind -
		// check the popup model
		boolean toUseDisabledIcon;
		if (this.commandButton instanceof JCommandButton
				&& ((JCommandButton) this.commandButton).getCommandButtonKind() == JCommandButton.CommandButtonKind.POPUP_ONLY) {
			toUseDisabledIcon = !((JCommandButton) this.commandButton)
					.getPopupModel().isEnabled();
		} else {
			toUseDisabledIcon = !this.commandButton.getActionModel()
					.isEnabled();
		}
		return toUseDisabledIcon;
	}

	/**
	 * Paints command button vertical separator.
	 * 
	 * @param graphics
	 *            Graphics context.
	 * @param separatorArea
	 *            Separator area.
	 */
	protected void paintButtonVerticalSeparator(Graphics graphics,
			Rectangle separatorArea) {
		this.buttonRendererPane.setBounds(0, 0, this.commandButton.getWidth(),
				this.commandButton.getHeight());
		Graphics2D g2d = (Graphics2D) graphics.create();
		this.rendererSeparator.setOrientation(JSeparator.VERTICAL);
		this.buttonRendererPane.paintComponent(g2d, this.rendererSeparator,
				this.commandButton, separatorArea.x, 2, 2, this.commandButton
						.getHeight() - 4, true);
		g2d.dispose();
	}

	/**
	 * Paints command button horizontal separator.
	 * 
	 * @param graphics
	 *            Graphics context.
	 * @param separatorArea
	 *            Separator area.
	 */
	protected void paintButtonHorizontalSeparator(Graphics graphics,
			Rectangle separatorArea) {
		this.buttonRendererPane.setBounds(0, 0, this.commandButton.getWidth(),
				this.commandButton.getHeight());
		Graphics2D g2d = (Graphics2D) graphics.create();
		this.rendererSeparator.setOrientation(JSeparator.HORIZONTAL);
		this.buttonRendererPane.paintComponent(g2d, this.rendererSeparator,
				this.commandButton, 2, separatorArea.y, this.commandButton
						.getWidth() - 4, 2, true);
		g2d.dispose();
	}

	/**
	 * Paints command button background.
	 * 
	 * @param graphics
	 *            Graphics context.
	 * @param toFill
	 *            Rectangle for the background.
	 */
	protected void paintButtonBackground(Graphics graphics, Rectangle toFill) {
		ButtonModel actionModel = this.commandButton.getActionModel();
		PopupButtonModel popupModel = (this.commandButton instanceof JCommandButton) ? ((JCommandButton) this.commandButton)
				.getPopupModel()
				: null;

		// first time - paint the full background passing both models
		this.paintButtonBackground(graphics, toFill, actionModel, popupModel);

		Rectangle actionArea = this.getLayoutInfo().actionClickArea;
		Rectangle popupArea = this.getLayoutInfo().popupClickArea;
		if ((actionArea != null) && !actionArea.isEmpty()) {
			// now overlay the action area with the background matching action
			// model
			Graphics2D graphicsAction = (Graphics2D) graphics.create();
			// System.out.println(actionArea);
			graphicsAction.clip(actionArea);
			float actionAlpha = 0.4f;
			if ((popupModel != null) && !popupModel.isEnabled())
				actionAlpha = 1.0f;
			graphicsAction.setComposite(AlphaComposite.SrcOver
					.derive(actionAlpha));
			// System.out.println(graphicsAction.getClipBounds());
			this.paintButtonBackground(graphicsAction, toFill, actionModel);
			graphicsAction.dispose();
		}
		if ((popupArea != null) && !popupArea.isEmpty()) {
			// now overlay the popup area with the background matching popup
			// model
			Graphics2D graphicsPopup = (Graphics2D) graphics.create();
			// System.out.println(popupArea);
			graphicsPopup.clip(popupArea);
			// System.out.println(graphicsPopup.getClipBounds());
			float popupAlpha = 0.4f;
			if (!actionModel.isEnabled())
				popupAlpha = 1.0f;
			graphicsPopup.setComposite(AlphaComposite.SrcOver
					.derive(popupAlpha));
			this.paintButtonBackground(graphicsPopup, toFill, popupModel);
			graphicsPopup.dispose();
		}
	}

	/**
	 * Paints the background of the command button.
	 * 
	 * @param graphics
	 *            Graphics context.
	 * @param toFill
	 *            Rectangle to fill.
	 * @param modelToUse
	 *            Button models to use for computing the background fill.
	 */
	protected void paintButtonBackground(Graphics graphics, Rectangle toFill,
			ButtonModel... modelToUse) {
		if (modelToUse.length == 0)
			return;
		if ((modelToUse.length == 1) && (modelToUse[0] == null))
			return;

		this.buttonRendererPane.setBounds(toFill.x, toFill.y, toFill.width,
				toFill.height);
		this.rendererButton.setRolloverEnabled(true);
		boolean isEnabled = true;
		boolean isRollover = false;
		boolean isPressed = true;
		boolean isArmed = true;
		boolean isSelected = true;
		for (ButtonModel model : modelToUse) {
			if (model == null)
				continue;
			isEnabled = isEnabled && model.isEnabled();
			isRollover = isRollover || model.isRollover();
			isPressed = isPressed && model.isPressed();
			isArmed = isArmed && model.isArmed();
			isSelected = isSelected && model.isSelected();
			if (model instanceof PopupButtonModel) {
				isRollover = isRollover
						|| ((PopupButtonModel) model).isPopupShowing();
			}
		}
		this.rendererButton.getModel().setEnabled(isEnabled);
		this.rendererButton.getModel().setRollover(isRollover);
		this.rendererButton.getModel().setPressed(isPressed);
		this.rendererButton.getModel().setArmed(isArmed);
		this.rendererButton.getModel().setSelected(isSelected);
		// System.out.println(this.commandButton.getText() + " - e:"
		// + this.rendererButton.getModel().isEnabled() + ", s:"
		// + this.rendererButton.getModel().isSelected() + ", r:"
		// + this.rendererButton.getModel().isRollover() + ", p:"
		// + this.rendererButton.getModel().isPressed() + ", a:"
		// + this.rendererButton.getModel().isArmed());
		Graphics2D g2d = (Graphics2D) graphics.create();

		Color borderColor = FlamingoUtilities.getBorderColor();
		if (Boolean.TRUE.equals(this.commandButton
				.getClientProperty(EMULATE_SQUARE_BUTTON))) {
			this.buttonRendererPane.paintComponent(g2d, this.rendererButton,
					this.commandButton, toFill.x - toFill.width / 2, toFill.y
							- toFill.height / 2, 2 * toFill.width,
					2 * toFill.height, true);
			g2d.setColor(borderColor);
			g2d.drawRect(toFill.x, toFill.y, toFill.width - 1,
					toFill.height - 1);
		} else {
			AbstractCommandButton.CommandButtonLocationOrderKind locationKind = this.commandButton
					.getLocationOrderKind();
			Insets outsets = (this.rendererButton instanceof JToggleButton) ? ButtonSizingUtils
					.getInstance().getToggleOutsets()
					: ButtonSizingUtils.getInstance().getOutsets();
			if (locationKind != null) {
				if (locationKind == AbstractCommandButton.CommandButtonLocationOrderKind.ONLY) {
					this.buttonRendererPane.paintComponent(g2d,
							this.rendererButton, this.commandButton, toFill.x
									- outsets.left, toFill.y - outsets.top,
							toFill.width + outsets.left + outsets.right,
							toFill.height + outsets.top + outsets.bottom, true);
				} else {
					// special case for parent component which is a vertical
					// button strip
					Component parent = this.commandButton.getParent();
					if ((parent instanceof JCommandButtonStrip)
							&& (((JCommandButtonStrip) parent).getOrientation() == StripOrientation.VERTICAL)) {
						switch (locationKind) {
						case FIRST:
							this.buttonRendererPane.paintComponent(g2d,
									this.rendererButton, this.commandButton,
									toFill.x - outsets.left, toFill.y
											- outsets.top, toFill.width
											+ outsets.left + outsets.right,
									2 * toFill.height, true);
							g2d.setColor(borderColor);
							g2d.drawLine(toFill.x + 1, toFill.y + toFill.height
									- 1, toFill.x + toFill.width - 2, toFill.y
									+ toFill.height - 1);
							break;
						case LAST:
							this.buttonRendererPane.paintComponent(g2d,
									this.rendererButton, this.commandButton,
									toFill.x - outsets.left, toFill.y
											- toFill.height, toFill.width
											+ outsets.left + outsets.right, 2
											* toFill.height + outsets.bottom,
									true);
							break;
						case MIDDLE:
							this.buttonRendererPane.paintComponent(g2d,
									this.rendererButton, this.commandButton,
									toFill.x - outsets.left, toFill.y
											- toFill.height, toFill.width
											+ outsets.left + outsets.right,
									3 * toFill.height, true);
							g2d.setColor(borderColor);
							g2d.drawLine(toFill.x + 1, toFill.y + toFill.height
									- 1, toFill.x + toFill.width - 2, toFill.y
									+ toFill.height - 1);
						}
					} else {
						// horizontal
						boolean ltr = this.commandButton
								.getComponentOrientation().isLeftToRight();
						if (locationKind == AbstractCommandButton.CommandButtonLocationOrderKind.MIDDLE) {
							this.buttonRendererPane.paintComponent(g2d,
									this.rendererButton, this.commandButton,
									toFill.x - toFill.width, toFill.y
											- outsets.top, 3 * toFill.width,
									toFill.height + outsets.top
											+ outsets.bottom, true);
							g2d.setColor(borderColor);
							g2d.drawLine(toFill.x + toFill.width - 1,
									toFill.y + 1, toFill.x + toFill.width - 1,
									toFill.y + toFill.height - 2);
						} else {
							boolean curveOnLeft = (ltr && (locationKind == AbstractCommandButton.CommandButtonLocationOrderKind.FIRST))
									|| (!ltr && (locationKind == AbstractCommandButton.CommandButtonLocationOrderKind.LAST));
							if (curveOnLeft) {
								this.buttonRendererPane.paintComponent(g2d,
										this.rendererButton,
										this.commandButton, toFill.x
												- outsets.left, toFill.y
												- outsets.top,
										2 * toFill.width, toFill.height
												+ outsets.top + outsets.bottom,
										true);
								g2d.setColor(borderColor);
								g2d.drawLine(toFill.x + toFill.width - 1,
										toFill.y + 1, toFill.x + toFill.width
												- 1, toFill.y + toFill.height
												- 2);
							} else {
								this.buttonRendererPane.paintComponent(g2d,
										this.rendererButton,
										this.commandButton, toFill.x
												- toFill.width, toFill.y
												- outsets.top, 2 * toFill.width
												+ outsets.right, toFill.height
												+ outsets.top + outsets.bottom,
										true);
							}
						}
					}
				}
			} else {
				this.buttonRendererPane.paintComponent(g2d,
						this.rendererButton, this.commandButton, toFill.x
								- outsets.left, toFill.y - outsets.top,
						toFill.width + outsets.left + outsets.right,
						toFill.height + outsets.top + outsets.bottom, true);
			}
		}
		g2d.dispose();
	}

	/**
	 * Updates the custom dimension.
	 */
	protected void updateCustomDimension() {
		int dimension = this.commandButton.getCustomDimension();

		if (dimension > 0) {
			this.commandButton.getIcon().setDimension(
					new Dimension(dimension, dimension));
			this.commandButton
					.setDisplayState(CommandButtonDisplayState.FIT_TO_ICON);

			this.commandButton.invalidate();
			this.commandButton.revalidate();
			this.commandButton.doLayout();
			this.commandButton.repaint();
		}
	}

	/**
	 * Updates the popup action icon.
	 */
	protected void updatePopupActionIcon() {
		JCommandButton button = (JCommandButton) this.commandButton;
		if (button.getCommandButtonKind().hasPopup()) {
			this.popupActionIcon = this.createPopupActionIcon();
		} else {
			this.popupActionIcon = null;
		}
	}

	/**
	 * Paints the button icon.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param iconRect
	 *            Icon rectangle.
	 */
	protected void paintButtonIcon(Graphics g, Rectangle iconRect) {
		Icon iconToPaint = this.getIconToPaint();
		if ((iconRect == null) || (iconToPaint == null)
				|| (iconRect.width == 0) || (iconRect.height == 0)) {
			return;
		}

		iconToPaint.paintIcon(this.commandButton, g, iconRect.x, iconRect.y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.ComponentUI#getPreferredSize(javax.swing.JComponent)
	 */
	@Override
	public Dimension getPreferredSize(JComponent c) {
		AbstractCommandButton button = (AbstractCommandButton) c;
		return this.layoutManager.getPreferredSize(button);
	}

	@Override
	public CommandButtonLayoutInfo getLayoutInfo() {
		if (this.layoutInfo != null) {
			return this.layoutInfo;
		}
		this.layoutInfo = this.layoutManager.getLayoutInfo(commandButton,
				this.commandButton.getGraphics());
		return this.layoutInfo;
	}

	/**
	 * Returns the layout gap for the visuals of the associated command button.
	 * 
	 * @return The layout gap for the visuals of the associated command button.
	 */
	protected int getLayoutGap() {
		Font font = this.commandButton.getFont();
		if (font == null)
			font = UIManager.getFont("Button.font");
		return (font.getSize() - 4) / 4;
	}

	/**
	 * Returns indication whether the action-popup areas separator is painted.
	 * 
	 * @return <code>true</code> if the action-popup areas separator is painted.
	 */
	protected boolean isPaintingSeparators() {
		PopupButtonModel popupModel = (this.commandButton instanceof JCommandButton) ? ((JCommandButton) this.commandButton)
				.getPopupModel()
				: null;
		boolean isActionRollover = this.commandButton.getActionModel()
				.isRollover();
		boolean isPopupRollover = (popupModel != null)
				&& popupModel.isRollover();
		// Rectangle actionArea = this.getActionClickArea();
		// Rectangle popupArea = this.getPopupClickArea();
		// boolean hasNonEmptyAreas = (actionArea.width * actionArea.height
		// * popupArea.width * popupArea.height > 0);
		return // hasNonEmptyAreas &&
		(isActionRollover || isPopupRollover);
	}

	/**
	 * Returns indication whether the button background is painted.
	 * 
	 * @return <code>true</code> if the button background is painted.
	 */
	protected boolean isPaintingBackground() {
		PopupButtonModel popupModel = (this.commandButton instanceof JCommandButton) ? ((JCommandButton) this.commandButton)
				.getPopupModel()
				: null;
		boolean isActionSelected = this.commandButton.getActionModel()
				.isSelected();
		boolean isPopupSelected = (popupModel != null)
				&& popupModel.isSelected();
		boolean isActionRollover = this.commandButton.getActionModel()
				.isRollover();
		boolean isPopupRollover = (popupModel != null)
				&& popupModel.isRollover();
		boolean isPopupShowing = (popupModel != null)
				&& (popupModel.isPopupShowing());
		boolean isActionArmed = this.commandButton.getActionModel().isArmed();
		boolean isPopupArmed = (popupModel != null) && (popupModel.isArmed());

		return (isActionSelected || isPopupSelected || isActionRollover
				|| isPopupRollover || isPopupShowing || isActionArmed
				|| isPopupArmed || !this.commandButton.isFlat());
	}

	/**
	 * Creates the popup action listener for this command button.
	 * 
	 * @return Popup action listener for this command button.
	 */
	protected PopupActionListener createPopupActionListener() {
		return new PopupActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processPopupAction();
			}
		};
	}

	protected void processPopupAction() {
		boolean wasPopupShowing = false;
		if (this.commandButton instanceof JCommandButton) {
			wasPopupShowing = ((JCommandButton) this.commandButton)
					.getPopupModel().isPopupShowing();
		}

		// dismiss all the popups that are currently showing
		// up until <this> button.
		PopupPanelManager.defaultManager().hidePopups(commandButton);

		if (!(commandButton instanceof JCommandButton))
			return;

		if (wasPopupShowing)
			return;

		JCommandButton jcb = (JCommandButton) this.commandButton;

		// check if the command button has an associated popup
		// panel
		PopupPanelCallback popupCallback = jcb.getPopupCallback();
		final JPopupPanel popupPanel = (popupCallback != null) ? popupCallback
				.getPopupPanel(jcb) : null;
		if (popupPanel != null) {
			popupPanel.applyComponentOrientation(jcb.getComponentOrientation());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
                public void run() {
					if ((commandButton == null) || (popupPanel == null))
						return;

					if (!commandButton.isShowing())
						return;

					popupPanel.doLayout();

					int x = 0;
					int y = 0;

					JPopupPanel.PopupPanelCustomizer customizer = popupPanel
							.getCustomizer();
					boolean ltr = commandButton.getComponentOrientation()
							.isLeftToRight();
					if (customizer == null) {
						switch (((JCommandButton) commandButton)
								.getPopupOrientationKind()) {
						case DOWNWARD:
							if (ltr) {
								x = commandButton.getLocationOnScreen().x;
							} else {
								x = commandButton.getLocationOnScreen().x
										+ commandButton.getWidth()
										- popupPanel.getPreferredSize().width;
							}
							y = commandButton.getLocationOnScreen().y
									+ commandButton.getSize().height;
							break;
						case SIDEWARD:
							if (ltr) {
								x = commandButton.getLocationOnScreen().x
										+ commandButton.getWidth();
							} else {
								x = commandButton.getLocationOnScreen().x
										- popupPanel.getPreferredSize().width;
							}
							y = commandButton.getLocationOnScreen().y
									+ getLayoutInfo().popupClickArea.y;
							break;
						}
					} else {
						Rectangle placementRect = customizer.getScreenBounds();
						// System.out.println(placementRect);
						x = placementRect.x;
						y = placementRect.y;
					}

					// make sure that the popup stays in bounds
					Rectangle scrBounds = commandButton
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
					if (customizer != null) {
						Rectangle placementRect = customizer.getScreenBounds();
						popupPanel.setPreferredSize(new Dimension(
								placementRect.width, placementRect.height));
					}
					Popup popup = PopupFactory.getSharedInstance().getPopup(
							commandButton, popupPanel, x, y);
					// System.out.println("Showing the popup panel");
					PopupPanelManager.defaultManager().addPopup(commandButton,
							popup, popupPanel);
				}
			});
			return;
		}
	}

	protected void syncDisabledIcon() {
		ResizableIcon currDisabledIcon = this.commandButton.getDisabledIcon();
		ResizableIcon icon = this.commandButton.getIcon();
		if ((currDisabledIcon == null)
				|| (currDisabledIcon instanceof UIResource)) {
			if (icon != null) {
				this.commandButton.setDisabledIcon(new ResizableIconUIResource(
						new FilteredResizableIcon(icon, new ColorConvertOp(
								ColorSpace.getInstance(ColorSpace.CS_GRAY),
								null))));
			} else {
				this.commandButton.setDisabledIcon(null);
			}
		} else {
			// disabled icon coming from app code
			if (icon != null) {
				this.commandButton.getDisabledIcon()
						.setDimension(
								new Dimension(icon.getIconWidth(), icon
										.getIconHeight()));
			}
		}
	}

	protected void syncIconDimension() {
		ResizableIcon icon = this.commandButton.getIcon();
		CommandButtonDisplayState commandButtonState = this.commandButton
				.getDisplayState();

		this.layoutManager = commandButtonState
				.createLayoutManager(this.commandButton);

		if (icon == null)
			return;

		int maxHeight = layoutManager.getPreferredIconSize();
		if (maxHeight < 0) {
			maxHeight = this.commandButton.getIcon().getIconHeight();
		}

		if (commandButtonState != CommandButtonDisplayState.FIT_TO_ICON) {
			Dimension newDim = new Dimension(maxHeight, maxHeight);
			icon.setDimension(newDim);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.ui.CommandButtonUI#getKeyTipAnchorCenterPoint()
	 */
	@Override
	public Point getKeyTipAnchorCenterPoint() {
		return this.layoutManager
				.getKeyTipAnchorCenterPoint(this.commandButton);
	}
}
