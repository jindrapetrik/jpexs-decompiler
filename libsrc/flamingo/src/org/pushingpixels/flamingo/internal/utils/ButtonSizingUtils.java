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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

public class ButtonSizingUtils {
	private static ButtonSizingUtils instance;

	private Insets outsets;

	private Insets toggleOutsets;

	public static synchronized ButtonSizingUtils getInstance() {
		if (instance == null)
			instance = new ButtonSizingUtils();
		return instance;
	}

	private ButtonSizingUtils() {
		this.outsets = this.syncOutsets(new JButton(""));
		this.toggleOutsets = this.syncOutsets(new JToggleButton(""));
		UIManager.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("lookAndFeel".equals(evt.getPropertyName())) {
					outsets = syncOutsets(new JButton(""));
					toggleOutsets = syncOutsets(new JToggleButton(""));
				}
			}
		});
	}

	private Insets syncOutsets(AbstractButton renderer) {
		JPanel panel = new JPanel(null);
		renderer.putClientProperty("JButton.buttonStyle", "square");
		renderer.setFocusable(false);
		renderer.setOpaque(false);
		panel.add(renderer);
		renderer.setBounds(0, 0, 100, 50);

		GraphicsEnvironment e = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice d = e.getDefaultScreenDevice();
		GraphicsConfiguration c = d.getDefaultConfiguration();
		BufferedImage compatibleImage = c.createCompatibleImage(100, 50,
				Transparency.TRANSLUCENT);
		renderer.paint(compatibleImage.getGraphics());

		// analyze top
		int top = 0;
		for (int i = 0; i < 25; i++) {
			int rgba = compatibleImage.getRGB(50, i);
			int alpha = (rgba >>> 24) & 0xFF;
			if (alpha == 255) {
				top = i;
				break;
			}
		}
		// analyze bottom
		int bottom = 0;
		for (int i = 49; i > 25; i--) {
			int rgba = compatibleImage.getRGB(50, i);
			int alpha = (rgba >>> 24) & 0xFF;
			if (alpha == 255) {
				bottom = 49 - i;
				break;
			}
		}
		// analyze left
		int left = 0;
		for (int i = 0; i < 50; i++) {
			int rgba = compatibleImage.getRGB(i, 25);
			int alpha = (rgba >>> 24) & 0xFF;
			if (alpha == 255) {
				left = i;
				break;
			}
		}
		// analyze right
		int right = 0;
		for (int i = 99; i > 50; i--) {
			int rgba = compatibleImage.getRGB(i, 25);
			int alpha = (rgba >>> 24) & 0xFF;
			if (alpha == 255) {
				right = 99 - i;
				break;
			}
		}

		return new Insets(top, left, bottom, right);
	}

	public Insets getOutsets() {
		return new Insets(outsets.top, outsets.left, outsets.bottom,
				outsets.right);
	}

	public Insets getToggleOutsets() {
		return new Insets(toggleOutsets.top, toggleOutsets.left,
				toggleOutsets.bottom, toggleOutsets.right);
	}
}
