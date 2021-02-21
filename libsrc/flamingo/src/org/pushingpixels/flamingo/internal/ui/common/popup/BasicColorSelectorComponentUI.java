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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.swing.SwingRepaintCallback;

/**
 * Basic UI for color selector component {@link JColorSelectorComponent}.
 * 
 * @author Kirill Grouchnikov
 */
public class BasicColorSelectorComponentUI extends ColorSelectorComponentUI {
	protected JColorSelectorComponent colorSelectorComponent;

	protected ButtonModel buttonModel;

	protected MouseListener mouseListener;

	protected ChangeListener modelChangeListener;

	protected ActionListener actionListener;

	protected Timeline rolloverTimeline;

	protected float rollover;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent c) {
		return new BasicColorSelectorComponentUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
	 */
	@Override
	public void installUI(JComponent c) {
		this.colorSelectorComponent = (JColorSelectorComponent) c;
		this.buttonModel = new DefaultButtonModel();

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

		this.colorSelectorComponent = null;
	}

	/**
	 * Installs listeners on the associated color selector component.
	 */
	protected void installListeners() {
		this.mouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (!buttonModel.isRollover()) {
					colorSelectorComponent
							.onColorRollover(colorSelectorComponent.getColor());
					rolloverTimeline.play();
				}
				buttonModel.setRollover(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (buttonModel.isRollover()) {
					colorSelectorComponent.onColorRollover(null);
					rolloverTimeline.playReverse();
				}
				buttonModel.setRollover(false);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				buttonModel.setArmed(true);
				buttonModel.setPressed(true);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				buttonModel.setPressed(false);
				buttonModel.setArmed(false);
			}
		};
		this.colorSelectorComponent.addMouseListener(this.mouseListener);

		this.modelChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				colorSelectorComponent.repaint();
			}
		};
		this.buttonModel.addChangeListener(this.modelChangeListener);

		this.actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				colorSelectorComponent.onColorSelected(colorSelectorComponent
						.getColor());

				PopupPanelManager.defaultManager().hidePopups(null);
			}
		};
		this.buttonModel.addActionListener(this.actionListener);
	}

	/**
	 * Uninstalls listeners from the associated color selector component.
	 */
	protected void uninstallListeners() {
		this.buttonModel.removeActionListener(this.actionListener);
		this.actionListener = null;

		this.buttonModel.removeChangeListener(this.modelChangeListener);
		this.modelChangeListener = null;

		this.colorSelectorComponent.removeMouseListener(this.mouseListener);
		this.mouseListener = null;
	}

	/**
	 * Installs defaults on the associated color selector component.
	 */
	protected void installDefaults() {
		this.rolloverTimeline = new Timeline(this);
		this.rolloverTimeline.addPropertyToInterpolate("rollover", 0.0f, 1.0f);
		this.rolloverTimeline.addCallback(new SwingRepaintCallback(
				this.colorSelectorComponent));
		this.rolloverTimeline.setDuration(150);
	}

	/**
	 * Uninstalls defaults from the associated color selector component.
	 */
	protected void uninstallDefaults() {
	}

	/**
	 * Installs subcomponents on the associated color selector component.
	 */
	protected void installComponents() {
	}

	/**
	 * Uninstalls subcomponents from the associated color selector component.
	 */
	protected void uninstallComponents() {
	}

	public void setRollover(float rollover) {
		this.rollover = rollover;
	}

	@Override
	public void update(Graphics g, JComponent c) {
		int w = this.colorSelectorComponent.getWidth();
		int h = this.colorSelectorComponent.getHeight();
		Graphics2D g2d = (Graphics2D) g.create();

		Color fillColor = this.colorSelectorComponent.getColor();
		g2d.setColor(fillColor);
		g2d.fillRect(0, 0, w, h);

		float[] hsb = new float[3];
		Color.RGBtoHSB(fillColor.getRed(), fillColor.getGreen(), fillColor
				.getBlue(), hsb);
		float brightness = hsb[2] * 0.7f;
		g2d.setColor(new Color(brightness, brightness, brightness));
		int ty = this.colorSelectorComponent.isTopOpen() ? 1 : 0;
		int by = this.colorSelectorComponent.isBottomOpen() ? 1 : 0;
		g2d.drawRect(0, -ty, w - 1, h - 1 + ty + by);

		if (this.rollover > 0.0f) {
			g2d.setComposite(AlphaComposite.SrcOver.derive(this.rollover));
			g2d.setColor(new Color(207, 186, 115));
			g2d.drawRect(0, 0, w - 1, h - 1);
			g2d.setColor(new Color(230, 212, 150));
			g2d.drawRect(1, 1, w - 3, h - 3);
		}

		g2d.dispose();
	}
}
