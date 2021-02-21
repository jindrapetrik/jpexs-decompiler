/*
 * Copyright (c) 2001-2006 JGoodies Karsten Lentzsch. All Rights Reserved.
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
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of 
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
import java.awt.print.PrinterGraphics;
import java.util.*;

/**
 * Utilities to install desktop rendering hints for correctly rasterizing texts.
 * 
 * @author Kirill Grouchnikov
 */
public class RenderingUtils {
	/**
	 * Desktop property name that points to a collection of rendering hints. See
	 * <a href="http://java.sun.com/javase/6/docs/api/java/awt/doc-files/DesktopProperties.html"
	 * >documentation</a> for more details.
	 */
	private static final String PROP_DESKTOPHINTS = "awt.font.desktophints";

	/**
	 * Installs desktop hints on the specified graphics context.
	 * 
	 * @param g2
	 *            Graphics context.
	 * @return Map of old rendering hints.
	 */
	public static Map installDesktopHints(Graphics2D g2) {
		Map oldRenderingHints = null;
		Map desktopHints = desktopHints(g2);
		if (desktopHints != null && !desktopHints.isEmpty()) {
			oldRenderingHints = new HashMap(desktopHints.size());
			RenderingHints.Key key;
			for (Iterator i = desktopHints.keySet().iterator(); i.hasNext();) {
				key = (RenderingHints.Key) i.next();
				oldRenderingHints.put(key, g2.getRenderingHint(key));
			}
			g2.addRenderingHints(desktopHints);
		}
		return oldRenderingHints;
	}

	/**
	 * Returns the desktop hints for the specified graphics context.
	 * 
	 * @param g2
	 *            Graphics context.
	 * @return The desktop hints for the specified graphics context.
	 */
	private static Map desktopHints(Graphics2D g2) {
		if (isPrinting(g2)) {
			return null;
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		GraphicsDevice device = g2.getDeviceConfiguration().getDevice();
		Map desktopHints = (Map) toolkit.getDesktopProperty(PROP_DESKTOPHINTS
				+ '.' + device.getIDstring());
		if (desktopHints == null) {
			desktopHints = (Map) toolkit.getDesktopProperty(PROP_DESKTOPHINTS);
		}
		// It is possible to get a non-empty map but with disabled AA.
		if (desktopHints != null) {
			Object aaHint = desktopHints
					.get(RenderingHints.KEY_TEXT_ANTIALIASING);
			if ((aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
					|| (aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT)) {
				desktopHints = null;
			}
		}
		return desktopHints;
	}

	/**
	 * Checks whether the specified graphics context is a print context.
	 * 
	 * @param g
	 *            Graphics context.
	 * @return <code>true</code> if the specified graphics context is a print
	 *         context.
	 */
	private static boolean isPrinting(Graphics g) {
		return g instanceof PrintGraphics || g instanceof PrinterGraphics;
	}
}
