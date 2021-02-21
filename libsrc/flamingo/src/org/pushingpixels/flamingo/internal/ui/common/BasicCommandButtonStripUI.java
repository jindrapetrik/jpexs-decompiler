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

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip.StripOrientation;

/**
 * Basic UI for button strip {@link JCommandButtonStrip}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicCommandButtonStripUI extends CommandButtonStripUI {
	/**
	 * The associated button strip.
	 */
	protected JCommandButtonStrip buttonStrip;

	protected ChangeListener changeListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicCommandButtonStripUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.buttonStrip = (JCommandButtonStrip) c;
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

		c.setLayout(null);

		this.buttonStrip = null;
	}

	/**
	 * Installs listeners on the associated button strip.
	 */
	protected void installListeners() {
		this.changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (buttonStrip.getButtonCount() == 1) {
					buttonStrip
							.getButton(0)
							.setLocationOrderKind(
									AbstractCommandButton.CommandButtonLocationOrderKind.ONLY);
				} else {
					buttonStrip
							.getButton(0)
							.setLocationOrderKind(
									AbstractCommandButton.CommandButtonLocationOrderKind.FIRST);
					for (int i = 1; i < buttonStrip.getButtonCount() - 1; i++) {
						buttonStrip
								.getButton(i)
								.setLocationOrderKind(
										AbstractCommandButton.CommandButtonLocationOrderKind.MIDDLE);

					}
					buttonStrip
							.getButton(buttonStrip.getButtonCount() - 1)
							.setLocationOrderKind(
									AbstractCommandButton.CommandButtonLocationOrderKind.LAST);
				}
			}
		};
		this.buttonStrip.addChangeListener(this.changeListener);
	}

	/**
	 * Uninstalls listeners from the associated button strip.
	 */
	protected void uninstallListeners() {
		this.buttonStrip.removeChangeListener(this.changeListener);
		this.changeListener = null;
	}

	/**
	 * Installs defaults on the associated button strip.
	 */
	protected void installDefaults() {
		this.buttonStrip.setBorder(new EmptyBorder(0, 0, 0, 0));
	}

	/**
	 * Uninstalls defaults from the associated button strip.
	 */
	protected void uninstallDefaults() {
	}

	/**
	 * Installs subcomponents on the associated button strip.
	 */
	protected void installComponents() {
		this.buttonStrip.setLayout(createLayoutManager());
	}

	/**
	 * Uninstalls subcomponents from the associated ribbon.
	 */
	protected void uninstallComponents() {
	}

	/**
	 * Invoked by <code>installUI</code> to create a layout manager object to
	 * manage the {@link JCommandButtonStrip}.
	 * 
	 * @return a layout manager object
	 */
	protected LayoutManager createLayoutManager() {
		return new ButtonStripLayout();
	}

	/**
	 * Layout for the button strip.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private class ButtonStripLayout implements LayoutManager {
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
			int width = 0;
			int height = 0;
			if (buttonStrip.getOrientation() == StripOrientation.HORIZONTAL) {
				for (int i = 0; i < buttonStrip.getButtonCount(); i++) {
					width += buttonStrip.getButton(i).getPreferredSize().width;
					height = Math.max(height, buttonStrip.getButton(i)
							.getPreferredSize().height);
				}
			} else {
				for (int i = 0; i < buttonStrip.getButtonCount(); i++) {
					height += buttonStrip.getButton(i).getPreferredSize().height;
					width = Math.max(width, buttonStrip.getButton(i)
							.getPreferredSize().width);
				}
			}
			Insets ins = c.getInsets();
			// System.out.println(ins + ":" + width + ":" + height);
			return new Dimension(width + ins.left + ins.right, height + ins.top
					+ ins.bottom);
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
			if (buttonStrip.getButtonCount() == 0)
				return;
			Insets ins = c.getInsets();
			int height = c.getHeight() - ins.top - ins.bottom;
			int width = c.getWidth() - ins.left - ins.right;
			if (buttonStrip.getOrientation() == StripOrientation.HORIZONTAL) {
				int totalPreferredWidth = 0;
				for (int i = 0; i < buttonStrip.getButtonCount(); i++) {
					AbstractCommandButton currButton = buttonStrip.getButton(i);
					totalPreferredWidth += currButton.getPreferredSize().width;
				}
				int deltaX = (width - totalPreferredWidth)
						/ buttonStrip.getButtonCount();
				if (buttonStrip.getComponentOrientation().isLeftToRight()) {
					int x = ins.left;
					for (int i = 0; i < buttonStrip.getButtonCount(); i++) {
						AbstractCommandButton currButton = buttonStrip
								.getButton(i);
						currButton.setBounds(x, ins.top, currButton
								.getPreferredSize().width
								+ deltaX, height);
						x += (currButton.getPreferredSize().width + deltaX);
					}
				} else {
					int x = c.getWidth() - ins.right;
					for (int i = 0; i < buttonStrip.getButtonCount(); i++) {
						AbstractCommandButton currButton = buttonStrip
								.getButton(i);
						int buttonWidth = currButton.getPreferredSize().width
								+ deltaX;
						currButton.setBounds(x - buttonWidth, ins.top,
								buttonWidth, height);
						x -= buttonWidth;
					}
				}
			} else {
				int totalPreferredHeight = 0;
				for (int i = 0; i < buttonStrip.getButtonCount(); i++) {
					AbstractCommandButton currButton = buttonStrip.getButton(i);
					totalPreferredHeight += currButton.getPreferredSize().height;
				}
				float deltaY = (float) (height - totalPreferredHeight)
						/ (float) buttonStrip.getButtonCount();
				float y = ins.top;
				for (int i = 0; i < buttonStrip.getButtonCount(); i++) {
					AbstractCommandButton currButton = buttonStrip.getButton(i);
					float buttonHeight = (currButton.getPreferredSize().height + deltaY);
					currButton.setBounds(ins.left, (int) y, width, (int) Math
							.ceil(buttonHeight));
					y += buttonHeight;
				}
			}
		}
	}
}
