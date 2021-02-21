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
import java.awt.color.ColorSpace;
import java.awt.image.ColorConvertOp;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

import org.pushingpixels.flamingo.api.common.HorizontalAlignment;
import org.pushingpixels.flamingo.api.common.icon.FilteredResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class BasicRibbonComponentUI extends RibbonComponentUI {
	/**
	 * The associated ribbon component.
	 */
	protected JRibbonComponent ribbonComponent;

	protected JLabel captionLabel;

	protected PropertyChangeListener propertyChangeListener;

	protected ResizableIcon disabledIcon;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicRibbonComponentUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.ribbonComponent = (JRibbonComponent) c;
		installDefaults();
		installComponents();
		installListeners();
		c.setLayout(createLayoutManager());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
	 */
	@Override
	public void uninstallUI(JComponent c) {
		c.setLayout(null);
		uninstallListeners();
		uninstallDefaults();
		uninstallComponents();
	}

	/**
	 * Installs default parameters on the associated ribbon component.
	 */
	protected void installDefaults() {
		if (!this.ribbonComponent.isSimpleWrapper()) {
			ResizableIcon icon = this.ribbonComponent.getIcon();
			if (icon != null) {
				icon.setDimension(new Dimension(16, 16));
				this.disabledIcon = createDisabledIcon();
			}
		}

		this.ribbonComponent.getMainComponent().setOpaque(false);
		this.ribbonComponent.setOpaque(false);
	}

	/**
	 * Installs subcomponents on the associated ribbon component.
	 */
	protected void installComponents() {
		this.captionLabel = new JLabel(this.ribbonComponent.getCaption());
		this.captionLabel.setEnabled(this.ribbonComponent.isEnabled());
		this.ribbonComponent.add(this.captionLabel);

		JComponent mainComponent = this.ribbonComponent.getMainComponent();
		this.ribbonComponent.add(mainComponent);
	}

	/**
	 * Installs listeners on the associated ribbon component.
	 */
	protected void installListeners() {
		this.propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("enabled".equals(evt.getPropertyName())) {
					boolean isEnabled = (Boolean) evt.getNewValue();
					ribbonComponent.getMainComponent().setEnabled(isEnabled);
					if (!ribbonComponent.isSimpleWrapper()) {
						captionLabel.setEnabled(isEnabled);
					}
					ribbonComponent.repaint();
				}
				if ("caption".equals(evt.getPropertyName())) {
					captionLabel.setText((String) evt.getNewValue());
				}
				if ("displayPriority".equals(evt.getPropertyName())) {
					ribbonComponent.revalidate();
					ribbonComponent.doLayout();
				}
			}
		};
		this.ribbonComponent
				.addPropertyChangeListener(this.propertyChangeListener);

	}

	/**
	 * Uninstalls default parameters from the associated ribbon component.
	 */
	protected void uninstallDefaults() {
	}

	/**
	 * Uninstalls components from the associated ribbon component.
	 */
	protected void uninstallComponents() {
		JComponent mainComponent = this.ribbonComponent.getMainComponent();
		this.ribbonComponent.remove(mainComponent);

		this.ribbonComponent.remove(this.captionLabel);
		this.captionLabel = null;
	}

	/**
	 * Uninstalls listeners from the associated ribbon component.
	 */
	protected void uninstallListeners() {
		this.ribbonComponent
				.removePropertyChangeListener(this.propertyChangeListener);
		this.propertyChangeListener = null;
	}

	@Override
	public Point getKeyTipAnchorCenterPoint() {
		if (this.ribbonComponent.isSimpleWrapper()) {
			return new Point(
					this.ribbonComponent.getMainComponent().getX() + 10,
					this.ribbonComponent.getHeight());
		} else {
			return new Point(this.captionLabel.getX(), this.ribbonComponent
					.getHeight());
		}
	}

	protected LayoutManager createLayoutManager() {
		return new ExtComponentLayout();
	}

	protected class ExtComponentLayout implements LayoutManager {
		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			Insets ins = ribbonComponent.getInsets();
			JComponent mainComponent = ribbonComponent.getMainComponent();
			Dimension minMain = mainComponent.getMinimumSize();

			int width = ins.left;
			int height = minMain.height;
			if (isIconVisible(ribbonComponent.getDisplayPriority())) {
				ResizableIcon icon = ribbonComponent.getIcon();
				if (icon != null) {
					width += (icon.getIconWidth() + getLayoutGap());
					height = Math.max(height, icon.getIconHeight());
				}
			}
			if (isCaptionVisible(ribbonComponent.getDisplayPriority())) {
				Dimension minCaption = captionLabel.getMinimumSize();
				width += (minCaption.width + getLayoutGap());
				height = Math.max(height, minCaption.height);
			}
			width += minMain.width;
			width += ins.right;
			height += (ins.top + ins.bottom);

			return new Dimension(width, height);
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return getPreferredSize(ribbonComponent.getDisplayPriority());
		}

		@Override
		public void layoutContainer(Container parent) {
			JRibbonComponent ribbonComp = (JRibbonComponent) parent;
			Insets ins = ribbonComp.getInsets();
			int gap = getLayoutGap();
			int availableHeight = ribbonComp.getHeight() - ins.top - ins.bottom;
			int availableWidth = ribbonComp.getWidth() - ins.left - ins.right;

			HorizontalAlignment horizAlignment = ribbonComp
					.getHorizontalAlignment();
			JComponent mainComp = ribbonComp.getMainComponent();
			Dimension prefMainDim = mainComp.getPreferredSize();
			int prefMainWidth = prefMainDim.width;
			int finalHeight = Math.min(prefMainDim.height, availableHeight);
			boolean ltr = ribbonComp.getComponentOrientation().isLeftToRight();

			if (ribbonComp.isSimpleWrapper()) {
				int finalMainWidth = Math.min(availableWidth, prefMainWidth);
				int offset = availableWidth - prefMainWidth;
				int topMain = ins.top + (availableHeight - finalHeight) / 2;
				int x = ltr ? ins.left : ribbonComp.getWidth() - ins.right;
				switch (horizAlignment) {
				case LEADING:
					if (ltr) {
						mainComp.setBounds(x, topMain, finalMainWidth,
								finalHeight);
					} else {
						mainComp.setBounds(x - finalMainWidth, topMain,
								finalMainWidth, finalHeight);
					}
					break;
				case TRAILING:
					if (ltr) {
						mainComp.setBounds(x + offset, topMain, finalMainWidth,
								finalHeight);
					} else {
						mainComp.setBounds(x - finalMainWidth - offset,
								topMain, finalMainWidth, finalHeight);
					}
					break;
				case CENTER:
					if (ltr) {
						mainComp.setBounds(x + offset / 2, topMain,
								finalMainWidth, finalHeight);
					} else {
						mainComp.setBounds(x - finalMainWidth - offset / 2,
								topMain, finalMainWidth, finalHeight);
					}
					break;
				case FILL:
					if (ltr) {
						mainComp.setBounds(x, topMain, availableWidth,
								finalHeight);
					} else {
						mainComp.setBounds(x - availableWidth, topMain,
								availableWidth, finalHeight);
					}
					break;
				}
				mainComp.doLayout();
			} else {
				int x = ltr ? ins.left : ribbonComp.getWidth() - ins.right;
				if (isIconVisible(ribbonComponent.getDisplayPriority())) {
					if (ribbonComp.getIcon() != null) {
						// icon is painted separately
						int iconW = ribbonComp.getIcon().getIconWidth();
						x = ltr ? x + iconW + gap : x - iconW - gap;
					}
				}

				if (isCaptionVisible(ribbonComponent.getDisplayPriority())) {
					captionLabel.setVisible(true);
					Dimension prefCaptionDim = captionLabel.getPreferredSize();
					if (ltr) {
						captionLabel
								.setBounds(
										x,
										ins.top
												+ (availableHeight - prefCaptionDim.height)
												/ 2, prefCaptionDim.width,
										prefCaptionDim.height);
						x += (prefCaptionDim.width + gap);
					} else {
						captionLabel
								.setBounds(
										x - prefCaptionDim.width,
										ins.top
												+ (availableHeight - prefCaptionDim.height)
												/ 2, prefCaptionDim.width,
										prefCaptionDim.height);
						x -= (prefCaptionDim.width + gap);
					}
				} else {
					captionLabel.setVisible(false);
				}

				int topMain = ins.top + (availableHeight - finalHeight) / 2;
				int finalMainWidth = ltr ? Math.min(ribbonComp.getWidth()
						- ins.right - x, prefMainWidth) : Math.min(
						x - ins.left, prefMainWidth);
				int offset = ltr ? ribbonComp.getWidth() - ins.right - x
						- prefMainWidth : x - prefMainWidth - ins.left;

				switch (horizAlignment) {
				case LEADING:
					if (ltr) {
						mainComp.setBounds(x, topMain, finalMainWidth,
								finalHeight);
					} else {
						mainComp.setBounds(x - finalMainWidth, topMain,
								finalMainWidth, finalHeight);
					}
					break;
				case TRAILING:
					if (ltr) {
						mainComp.setBounds(x + offset, topMain, finalMainWidth,
								finalHeight);
					} else {
						mainComp.setBounds(x - finalMainWidth - offset,
								topMain, finalMainWidth, finalHeight);
					}
					break;
				case CENTER:
					if (ltr) {
						mainComp.setBounds(x + offset / 2, topMain,
								finalMainWidth, finalHeight);
					} else {
						mainComp.setBounds(x - finalMainWidth - offset / 2,
								topMain, finalMainWidth, finalHeight);
					}
					break;
				case FILL:
					if (ltr) {
						mainComp.setBounds(x, topMain, ribbonComp.getWidth()
								- ins.right - x, finalHeight);
					} else {
						mainComp.setBounds(ins.left, topMain, x - ins.left,
								finalHeight);
					}
					break;
				}
				mainComp.doLayout();
			}
		}
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		JRibbonComponent ribbonComp = (JRibbonComponent) c;
		if (isIconVisible(this.ribbonComponent.getDisplayPriority())) {
			Insets ins = ribbonComp.getInsets();
			ResizableIcon icon = ribbonComp.isEnabled() ? ribbonComp.getIcon()
					: this.disabledIcon;
			if (icon != null) {
				int availableHeight = ribbonComp.getHeight() - ins.top
						- ins.bottom;
				int iconY = Math.max(0, ins.top
						+ (availableHeight - icon.getIconHeight()) / 2);
				if (ribbonComp.getComponentOrientation().isLeftToRight()) {
					paintIcon(g, ribbonComp, icon, ins.left, iconY);
				} else {
					paintIcon(g, ribbonComp, icon, ribbonComp.getWidth()
							- ins.right - icon.getIconWidth(), iconY);
				}
			}
		}
	}

	protected void paintIcon(Graphics g, JRibbonComponent ribbonComp,
			Icon icon, int x, int y) {
		icon.paintIcon(ribbonComp, g, x, y);
	}

	protected int getLayoutGap() {
		return 4;
	}

	protected ResizableIcon createDisabledIcon() {
		return new FilteredResizableIcon(this.ribbonComponent.getIcon(),
				new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),
						null));
	}

	protected boolean isIconVisible(RibbonElementPriority displayPriority) {
		if (this.ribbonComponent.isSimpleWrapper())
			return false;
		return (displayPriority == RibbonElementPriority.TOP)
				|| (displayPriority == RibbonElementPriority.MEDIUM);
	}

	protected boolean isCaptionVisible(RibbonElementPriority displayPriority) {
		if (this.ribbonComponent.isSimpleWrapper())
			return false;
		return (displayPriority == RibbonElementPriority.TOP);
	}

	@Override
	public Dimension getPreferredSize(RibbonElementPriority priority) {
		Insets ins = ribbonComponent.getInsets();
		JComponent mainComponent = ribbonComponent.getMainComponent();
		Dimension prefMain = mainComponent.getPreferredSize();

		int width = ins.left;
		int height = prefMain.height;
		if (isIconVisible(priority)) {
			ResizableIcon icon = ribbonComponent.getIcon();
			if (icon != null) {
				width += (icon.getIconWidth() + getLayoutGap());
				height = Math.max(height, icon.getIconHeight());
			}
		}
		if (isCaptionVisible(priority)) {
			Dimension prefCaption = captionLabel.getPreferredSize();
			width += (prefCaption.width + getLayoutGap());
			height = Math.max(height, prefCaption.height);
		}
		width += prefMain.width;
		width += ins.right;
		height += (ins.top + ins.bottom);

		return new Dimension(width, height);
	}
}
