/*
 * Copyright (c) 2005 JGoodies Karsten Lentzsch. All Rights Reserved.
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

package org.pushingpixels.substance.internal.contrib.jgoodies.looks.common;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * Does all the magic for getting popups with drop shadows. It adds the drop
 * shadow border to the Popup, in <code>#show</code> it snapshots the screen
 * background as needed, and in <code>#hide</code> it cleans up all changes made
 * before.
 * 
 * @author Andrej Golovnin
 * 
 * @see com.jgoodies.looks.common.ShadowPopupBorder
 * @see com.jgoodies.looks.common.ShadowPopupFactory
 */
public final class ShadowPopup extends Popup {

	/**
	 * Max number of items to store in the cache.
	 */
	private static final int MAX_CACHE_SIZE = 5;

	/**
	 * The cache to use for ShadowPopups.
	 */
	private static List cache;

	/**
	 * The singleton instance used to draw all borders.
	 */
	private static final Border SHADOW_BORDER = ShadowPopupBorder.getInstance();

	/**
	 * The size of the drop shadow.
	 */
	private static final int SHADOW_SIZE = 5;

	/**
	 * Indicates whether we can make snapshots from screen or not.
	 */
	private static boolean canSnapshot = true;

	/**
	 * The component mouse coordinates are relative to, may be null.
	 */
	private Component owner;

	/**
	 * The contents of the popup.
	 */
	private Component contents;

	/**
	 * The desired x and y location of the popup.
	 */
	private int x, y;

	/**
	 * The real popup. The #show() and #hide() methods will delegate all calls
	 * to these popup.
	 */
	private Popup popup;

	/**
	 * The border of the contents' parent replaced by SHADOW_BORDER.
	 */
	private Border oldBorder;

	/**
	 * The old value of the opaque property of the contents' parent.
	 */
	private boolean oldOpaque;

	/**
	 * The heavy weight container of the popup contents, may be null.
	 */
	private Container heavyWeightContainer;

	/**
	 * Returns a previously used <code>ShadowPopup</code>, or a new one if none
	 * of the popups have been recycled.
	 */
	static Popup getInstance(Component owner, Component contents, int x, int y,
			Popup delegate) {
		ShadowPopup result;
		synchronized (ShadowPopup.class) {
			if (cache == null) {
				cache = new ArrayList(MAX_CACHE_SIZE);
			}
			if (cache.size() > 0) {
				result = (ShadowPopup) cache.remove(0);
			} else {
				result = new ShadowPopup();
			}
		}
		result.reset(owner, contents, x, y, delegate);
		return result;
	}

	/**
	 * Recycles the ShadowPopup.
	 */
	private static void recycle(ShadowPopup popup) {
		synchronized (ShadowPopup.class) {
			if (cache.size() < MAX_CACHE_SIZE) {
				cache.add(popup);
			}
		}
	}

	public static boolean canSnapshot() {
		return canSnapshot;
	}

	/**
	 * Hides and disposes of the <code>Popup</code>. Once a <code>Popup</code>
	 * has been disposed you should no longer invoke methods on it. A
	 * <code>dispose</code>d <code>Popup</code> may be reclaimed and later used
	 * based on the <code>PopupFactory</code>. As such, if you invoke methods on
	 * a <code>disposed</code> <code>Popup</code>, indeterminate behavior will
	 * result.
	 * <p>
	 * 
	 * In addition to the superclass behavior, we reset the stored horizontal
	 * and vertical drop shadows - if any.
	 */
	@Override
	public void hide() {
		if (contents == null)
			return;

		final JComponent parent = (JComponent) contents.getParent();
		popup.hide();
		if (parent.getBorder() == SHADOW_BORDER) {
			parent.setBorder(oldBorder);
			parent.setOpaque(oldOpaque);
			oldBorder = null;
			if (heavyWeightContainer != null) {
				parent.putClientProperty(
						ShadowPopupFactory.PROP_HORIZONTAL_BACKGROUND, null);
				parent.putClientProperty(
						ShadowPopupFactory.PROP_VERTICAL_BACKGROUND, null);
				heavyWeightContainer = null;
			}
		}
		owner = null;
		contents = null;
		popup = null;
		recycle(ShadowPopup.this);
		// }
		// });
	}

	/**
	 * Makes the <code>Popup</code> visible. If the popup has a heavy-weight
	 * container, we try to snapshot the background. If the <code>Popup</code>
	 * is currently visible, it remains visible.
	 */
	@Override
	public void show() {
		if (heavyWeightContainer != null) {
			snapshot();
		}
		popup.show();
	}

	/**
	 * Reinitializes this ShadowPopup using the given parameters.
	 * 
	 * @param owner
	 *            component mouse coordinates are relative to, may be null
	 * @param contents
	 *            the contents of the popup
	 * @param x
	 *            the desired x location of the popup
	 * @param y
	 *            the desired y location of the popup
	 * @param popup
	 *            the popup to wrap
	 */
	private void reset(Component owner, Component contents, int x, int y,
			Popup popup) {
		this.owner = owner;
		this.contents = contents;
		this.popup = popup;
		this.x = x;
		this.y = y;
		if (owner instanceof JComboBox) {
			return;
		}
		for (Container p = contents.getParent(); p != null; p = p.getParent()) {
			if ((p instanceof JWindow) || (p instanceof Panel)) {
				// Workaround for the gray rect problem.
				p.setBackground(contents.getBackground());
				heavyWeightContainer = p;
				break;
			}
		}
		JComponent parent = (JComponent) contents.getParent();
		oldOpaque = parent.isOpaque();
		oldBorder = parent.getBorder();
		parent.setOpaque(false);
		parent.setBorder(SHADOW_BORDER);
		// Pack it because we have changed the border.
		if (heavyWeightContainer != null) {
			heavyWeightContainer.setSize(heavyWeightContainer
					.getPreferredSize());
		} else {
			parent.setSize(parent.getPreferredSize());
		}
	}

	/**
	 * The 'scratch pad' objects used to calculate dirty regions of the screen
	 * snapshots.
	 * 
	 * @see #snapshot()
	 */
	private static final Point point = new Point();

	private static final Rectangle rect = new Rectangle();

	/**
	 * Snapshots the background. The snapshots are stored as client properties
	 * of the contents' parent. The next time the border is drawn, this
	 * background will be used.
	 * <p>
	 * 
	 * Uses a robot on the default screen device to capture the screen region
	 * under the drop shadow. Does <em>not</em> use the window's device, because
	 * that may be an outdated device (due to popup reuse) and the robot's
	 * origin seems to be adjusted with the default screen device.
	 * 
	 * @see #show()
	 * @see com.jgoodies.looks.common.ShadowPopupBorder
	 */
	private void snapshot() {
		try {
			Robot robot = new Robot(); // uses the default screen device

			Dimension size = heavyWeightContainer.getPreferredSize();
			int width = size.width;
			int height = size.height;

			rect.setBounds(x, y + height - SHADOW_SIZE, width, SHADOW_SIZE);
			BufferedImage hShadowBg = robot.createScreenCapture(rect);

			rect.setBounds(x + width - SHADOW_SIZE, y, SHADOW_SIZE, height
					- SHADOW_SIZE);
			BufferedImage vShadowBg = robot.createScreenCapture(rect);

			JComponent parent = (JComponent) contents.getParent();
			parent.putClientProperty(
					ShadowPopupFactory.PROP_HORIZONTAL_BACKGROUND, hShadowBg);
			parent.putClientProperty(
					ShadowPopupFactory.PROP_VERTICAL_BACKGROUND, vShadowBg);

			Container layeredPane = getLayeredPane();
			if (layeredPane == null) {
				// This could happen if owner is null.
				return;
			}

			int layeredPaneWidth = layeredPane.getWidth();
			int layeredPaneHeight = layeredPane.getHeight();

			point.x = x;
			point.y = y;
			SwingUtilities.convertPointFromScreen(point, layeredPane);

			// If needed paint dirty region of the horizontal snapshot.
			rect.x = point.x;
			rect.y = point.y + height - SHADOW_SIZE;
			rect.width = width;
			rect.height = SHADOW_SIZE;

			if ((rect.x + rect.width) > layeredPaneWidth) {
				rect.width = layeredPaneWidth - rect.x;
			}
			if ((rect.y + rect.height) > layeredPaneHeight) {
				rect.height = layeredPaneHeight - rect.y;
			}
			if (!rect.isEmpty()) {
				Graphics g = hShadowBg.createGraphics();
				g.translate(-rect.x, -rect.y);
				g.setClip(rect);
				if (layeredPane instanceof JComponent) {
					JComponent c = (JComponent) layeredPane;
					boolean doubleBuffered = c.isDoubleBuffered();
					c.setDoubleBuffered(false);
					c.paintAll(g);
					c.setDoubleBuffered(doubleBuffered);
				} else {
					layeredPane.paintAll(g);
				}
				g.dispose();
			}

			// If needed paint dirty region of the vertical snapshot.
			rect.x = point.x + width - SHADOW_SIZE;
			rect.y = point.y;
			rect.width = SHADOW_SIZE;
			rect.height = height - SHADOW_SIZE;

			if ((rect.x + rect.width) > layeredPaneWidth) {
				rect.width = layeredPaneWidth - rect.x;
			}
			if ((rect.y + rect.height) > layeredPaneHeight) {
				rect.height = layeredPaneHeight - rect.y;
			}
			if (!rect.isEmpty()) {
				Graphics g = vShadowBg.createGraphics();
				g.translate(-rect.x, -rect.y);
				g.setClip(rect);
				if (layeredPane instanceof JComponent) {
					JComponent c = (JComponent) layeredPane;
					boolean doubleBuffered = c.isDoubleBuffered();
					c.setDoubleBuffered(false);
					c.paintAll(g);
					c.setDoubleBuffered(doubleBuffered);
				} else {
					layeredPane.paintAll(g);
				}
				g.dispose();
			}
		} catch (AWTException e) {
			canSnapshot = false;
		} catch (SecurityException e) {
			canSnapshot = false;
		}
	}

	/**
	 * @return the top level layered pane which contains the owner.
	 */
	private Container getLayeredPane() {
		// The code below is copied from PopupFactory#LightWeightPopup#show()
		Container parent = null;
		if (owner != null) {
			parent = owner instanceof Container ? (Container) owner : owner
					.getParent();
		}
		// Try to find a JLayeredPane and Window to add
		for (Container p = parent; p != null; p = p.getParent()) {
			if (p instanceof JRootPane) {
				if (p.getParent() instanceof JInternalFrame) {
					continue;
				}
				parent = ((JRootPane) p).getLayeredPane();
				// Continue, so that if there is a higher JRootPane, we'll
				// pick it up.
			} else if (p instanceof Window) {
				if (parent == null) {
					parent = p;
				}
				break;
			} /*JPEXS:else if (p instanceof JApplet) {
				// Painting code stops at Applets, we don't want
				// to add to a Component above an Applet otherwise
				// you'll never see it painted.
				break;
			}*/
		}
		return parent;
	}

}