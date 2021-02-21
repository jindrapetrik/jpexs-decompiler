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
package org.pushingpixels.flamingo.api.common;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.internal.ui.common.JRichTooltipPanel;

public class RichToolTipManager extends MouseAdapter implements
		MouseMotionListener {
	private Timer initialDelayTimer;

	private Timer dismissTimer;

	private RichTooltip richTooltip;

	private JTrackableComponent insideComponent;

	private MouseEvent mouseEvent;

	final static RichToolTipManager sharedInstance = new RichToolTipManager();

	private Popup tipWindow;

	private JRichTooltipPanel tip;

	private boolean tipShowing = false;

	private static final String TRACKED_FOR_RICH_TOOLTIP = "flamingo.internal.trackedForRichTooltip";

	public static abstract class JTrackableComponent extends JComponent {
		public abstract RichTooltip getRichTooltip(MouseEvent mouseEvent);
	}

	RichToolTipManager() {
		initialDelayTimer = new Timer(750, new InitialDelayTimerAction());
		initialDelayTimer.setRepeats(false);
		dismissTimer = new Timer(20000, new DismissTimerAction());
		dismissTimer.setRepeats(false);
	}

	/**
	 * Specifies the initial delay value.
	 * 
	 * @param milliseconds
	 *            the number of milliseconds to delay (after the cursor has
	 *            paused) before displaying the tooltip
	 * @see #getInitialDelay
	 */
	public void setInitialDelay(int milliseconds) {
		initialDelayTimer.setInitialDelay(milliseconds);
	}

	/**
	 * Returns the initial delay value.
	 * 
	 * @return an integer representing the initial delay value, in milliseconds
	 * @see #setInitialDelay(int)
	 */
	public int getInitialDelay() {
		return initialDelayTimer.getInitialDelay();
	}

	/**
	 * Specifies the dismissal delay value.
	 * 
	 * @param milliseconds
	 *            the number of milliseconds to delay before taking away the
	 *            tooltip
	 * @see #getDismissDelay
	 */
	public void setDismissDelay(int milliseconds) {
		dismissTimer.setInitialDelay(milliseconds);
	}

	/**
	 * Returns the dismissal delay value.
	 * 
	 * @return an integer representing the dismissal delay value, in
	 *         milliseconds
	 * @see #setDismissDelay(int)
	 */
	public int getDismissDelay() {
		return dismissTimer.getInitialDelay();
	}

	void showTipWindow(MouseEvent mouseEvent) {
		if (insideComponent == null || !insideComponent.isShowing())
			return;
		Dimension size;
		Point screenLocation = insideComponent.getLocationOnScreen();
		Point location = new Point();
		GraphicsConfiguration gc;
		gc = insideComponent.getGraphicsConfiguration();
		Rectangle sBounds = gc.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		// Take into account screen insets, decrease viewport
		sBounds.x += screenInsets.left;
		sBounds.y += screenInsets.top;
		sBounds.width -= (screenInsets.left + screenInsets.right);
		sBounds.height -= (screenInsets.top + screenInsets.bottom);

		// Just to be paranoid
		hideTipWindow();

		tip = new JRichTooltipPanel(insideComponent.getRichTooltip(mouseEvent));
		tip
				.applyComponentOrientation(insideComponent
						.getComponentOrientation());
		size = tip.getPreferredSize();

		AbstractRibbonBand<?> ribbonBand = (AbstractRibbonBand<?>) SwingUtilities
				.getAncestorOfClass(AbstractRibbonBand.class, insideComponent);
		boolean ltr = tip.getComponentOrientation().isLeftToRight();
		boolean isInRibbonBand = (ribbonBand != null);
		if (isInRibbonBand) {
			// display directly below or above ribbon band
			location.x = ltr ? screenLocation.x : screenLocation.x
					+ insideComponent.getWidth() - size.width;
			Point bandLocationOnScreen = ribbonBand.getLocationOnScreen();
			location.y = bandLocationOnScreen.y + ribbonBand.getHeight() + 4;
			if ((location.y + size.height) > (sBounds.y + sBounds.height)) {
				location.y = bandLocationOnScreen.y - size.height;
			}
		} else {
			// display directly below or above it
			location.x = ltr ? screenLocation.x : screenLocation.x
					+ insideComponent.getWidth() - size.width;
			location.y = screenLocation.y + insideComponent.getHeight();
			if ((location.y + size.height) > (sBounds.y + sBounds.height)) {
				location.y = screenLocation.y - size.height;
			}
		}

		// Tweak the X location to not overflow the screen
		if (location.x < sBounds.x) {
			location.x = sBounds.x;
		} else if (location.x - sBounds.x + size.width > sBounds.width) {
			location.x = sBounds.x + Math.max(0, sBounds.width - size.width);
		}

		PopupFactory popupFactory = PopupFactory.getSharedInstance();
		tipWindow = popupFactory.getPopup(insideComponent, tip, location.x,
				location.y);
		tipWindow.show();

		dismissTimer.start();
		tipShowing = true;
	}

	void hideTipWindow() {
		if (tipWindow != null) {
			tipWindow.hide();
			tipWindow = null;
			tipShowing = false;
			tip = null;
			dismissTimer.stop();
		}
	}

	/**
	 * Returns a shared <code>ToolTipManager</code> instance.
	 * 
	 * @return a shared <code>ToolTipManager</code> object
	 */
	public static RichToolTipManager sharedInstance() {
		return sharedInstance;
	}

	/**
	 * Registers a component for tooltip management.
	 * <p>
	 * This will register key bindings to show and hide the tooltip text only if
	 * <code>component</code> has focus bindings. This is done so that
	 * components that are not normally focus traversable, such as
	 * <code>JLabel</code>, are not made focus traversable as a result of
	 * invoking this method.
	 * 
	 * @param comp
	 *            a <code>JComponent</code> object to add
	 * @see JComponent#isFocusTraversable
	 */
	public void registerComponent(JTrackableComponent comp) {
		if (Boolean.TRUE.equals(comp
				.getClientProperty(TRACKED_FOR_RICH_TOOLTIP)))
			return;
		comp.addMouseListener(this);
		// commandButton.addMouseMotionListener(moveBeforeEnterListener);
		comp.putClientProperty(TRACKED_FOR_RICH_TOOLTIP, Boolean.TRUE);
	}

	/**
	 * Removes a component from tooltip control.
	 * 
	 * @param comp
	 *            a <code>JComponent</code> object to remove
	 */
	public void unregisterComponent(JTrackableComponent comp) {
		comp.removeMouseListener(this);
		comp.putClientProperty(TRACKED_FOR_RICH_TOOLTIP, null);
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		initiateToolTip(event);
	}

	private void initiateToolTip(MouseEvent event) {
		JTrackableComponent component = (JTrackableComponent) event.getSource();
		// component.removeMouseMotionListener(moveBeforeEnterListener);

		Point location = event.getPoint();
		// ensure tooltip shows only in proper place
		if (location.x < 0 || location.x >= component.getWidth()
				|| location.y < 0 || location.y >= component.getHeight()) {
			return;
		}

		// do not show tooltips on components in popup panels that are not
		// in the last shown one
		List<PopupPanelManager.PopupInfo> popups = PopupPanelManager
				.defaultManager().getShownPath();
		if (popups.size() > 0) {
			JPopupPanel popupPanel = popups.get(popups.size() - 1)
					.getPopupPanel();
			boolean ignore = true;
			Component c = component;
			while (c != null) {
				if (c == popupPanel) {
					ignore = false;
					break;
				}
				c = c.getParent();
			}
			if (ignore)
				return;
		}

		if (insideComponent != null) {
			initialDelayTimer.stop();
		}
		// A component in an unactive internal frame is sent two
		// mouseEntered events, make sure we don't end up adding
		// ourselves an extra time.
		component.removeMouseMotionListener(this);
		component.addMouseMotionListener(this);

		insideComponent = component;
		mouseEvent = event;
		initialDelayTimer.start();
	}

	@Override
	public void mouseExited(MouseEvent event) {
		initialDelayTimer.stop();
		if (insideComponent != null) {
			insideComponent.removeMouseMotionListener(this);
		}
		insideComponent = null;
		richTooltip = null;
		mouseEvent = null;
		hideTipWindow();
	}

	@Override
	public void mousePressed(MouseEvent event) {
		hideTipWindow();
		initialDelayTimer.stop();
		insideComponent = null;
		mouseEvent = null;
	}

	@Override
	public void mouseDragged(MouseEvent event) {
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		if (tipShowing) {
			checkForTipChange(event);
		} else {
			// Lazily lookup the values from within insideTimerAction
			insideComponent = (JTrackableComponent) event.getSource();
			mouseEvent = event;
			richTooltip = null;
			initialDelayTimer.restart();
		}
	}

	private void checkForTipChange(MouseEvent event) {
		JTrackableComponent component = (JTrackableComponent) event.getSource();
		RichTooltip newTooltip = component.getRichTooltip(event);

		// is it different?
		boolean isDifferent = (richTooltip != newTooltip);
		if (isDifferent) {
			hideTipWindow();
			if (newTooltip != null) {
				richTooltip = newTooltip;
				initialDelayTimer.restart();
			}
		}
	}

	protected class InitialDelayTimerAction implements ActionListener {
		@Override
        public void actionPerformed(ActionEvent e) {
			if (insideComponent != null && insideComponent.isShowing()) {
				// Lazy lookup
				if (richTooltip == null && mouseEvent != null) {
					richTooltip = insideComponent.getRichTooltip(mouseEvent);
				}
				if (richTooltip != null) {
					boolean showRichTooltip = true;
					// check that no visible popup is originating in this
					// component
					for (PopupPanelManager.PopupInfo pi : PopupPanelManager
							.defaultManager().getShownPath()) {
						if (pi.getPopupOriginator() == insideComponent) {
							showRichTooltip = false;
							break;
						}
					}

					if (showRichTooltip) {
						showTipWindow(mouseEvent);
					}
				} else {
					insideComponent = null;
					richTooltip = null;
					mouseEvent = null;
					hideTipWindow();
				}
			}
		}
	}

	protected class DismissTimerAction implements ActionListener {
		@Override
        public void actionPerformed(ActionEvent e) {
			hideTipWindow();
			initialDelayTimer.stop();
			insideComponent = null;
			mouseEvent = null;
		}
	}
}
