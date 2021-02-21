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

import javax.swing.*;
import javax.swing.event.*;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.model.ActionRepeatableButtonModel;
import org.pushingpixels.flamingo.api.common.model.PopupButtonModel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.internal.ui.common.BasicCommandButtonUI;
import org.pushingpixels.flamingo.internal.ui.common.CommandButtonUI;

/**
 * Command button.
 * 
 * @author Kirill Grouchnikov
 */
public class JCommandButton extends AbstractCommandButton {
	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "CommandButtonUI";

	/**
	 * Associated popup callback. May be <code>null</code>.
	 * 
	 * @see #setPopupCallback(PopupPanelCallback)
	 * @see #getPopupCallback()
	 */
	protected PopupPanelCallback popupCallback;

	/**
	 * The command button kind of this button.
	 * 
	 * @see #setCommandButtonKind(CommandButtonKind)
	 * @see #getCommandButtonKind()
	 */
	protected CommandButtonKind commandButtonKind;

	/**
	 * The popup orientation kind of this button.
	 * 
	 * @see #setPopupOrientationKind(CommandButtonPopupOrientationKind)
	 * @see #getPopupOrientationKind()
	 */
	protected CommandButtonPopupOrientationKind popupOrientationKind;

	/**
	 * Indicates the auto-repeat action mode. When the button is not in the
	 * auto-repeat action mode, the registered action listeners are activated
	 * when the mouse is released (just as with the base {@link AbstractButton}
	 * ). When the button is in auto-repeat mode, the registered action
	 * listeners are activated when the mouse is pressed. In addition, if the
	 * mouse is still pressed after {@link #getAutoRepeatInitialInterval()}, the
	 * action listeners will be activated every
	 * {@link #getAutoRepeatSubsequentInterval()} until the button is disabled
	 * or the mouse is released.
	 * 
	 * @see #autoRepeatInitialInterval
	 * @see #autoRepeatSubsequentInterval
	 * @see #setAutoRepeatAction(boolean)
	 * @see #isAutoRepeatAction()
	 */
	protected boolean isAutoRepeatAction;

	/**
	 * The initial interval for invoking the registered action listeners in the
	 * auto-repeat action mode.
	 * 
	 * @see #isAutoRepeatAction
	 * @see #autoRepeatSubsequentInterval
	 * @see #getAutoRepeatInitialInterval()
	 * @see #setAutoRepeatActionIntervals(int, int)
	 */
	protected int autoRepeatInitialInterval;

	/**
	 * The subsequent interval for invoking the registered action listeners in
	 * the auto-repeat action mode.
	 * 
	 * @see #isAutoRepeatAction
	 * @see #autoRepeatInitialInterval
	 * @see #getAutoRepeatSubsequentInterval()
	 * @see #setAutoRepeatActionIntervals(int, int)
	 */
	protected int autoRepeatSubsequentInterval;

	/**
	 * Indicates that rollover should result in firing the action. Used in
	 * conjunction with the {@link #isAutoRepeatAction} can model quick pan
	 * buttons such as breadcrumb bar scrollers.
	 * 
	 * @see #setFireActionOnRollover(boolean)
	 * @see #isFireActionOnRollover()
	 */
	protected boolean isFireActionOnRollover;

	/**
	 * Popup model of this button.
	 * 
	 * @see #setPopupModel(PopupButtonModel)
	 * @see #getPopupModel()
	 */
	protected PopupButtonModel popupModel;

	/**
	 * Default popup handler for this button.
	 */
	protected PopupHandler popupHandler;

	/**
	 * Rich tooltip for the popup area of this button.
	 * 
	 * @see #setPopupRichTooltip(RichTooltip)
	 * @see #getRichTooltip(MouseEvent)
	 */
	private RichTooltip popupRichTooltip;

	/**
	 * Key tip for the popup area of this button.
	 * 
	 * @see #setPopupKeyTip(String)
	 * @see #getPopupKeyTip()
	 */
	protected String popupKeyTip;

	/**
	 * Enumerates the available command button kinds.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static enum CommandButtonKind {
		/**
		 * Command button that has only action area.
		 */
		ACTION_ONLY(true, false),

		/**
		 * Command button that has only popup area.
		 */
		POPUP_ONLY(false, true),

		/**
		 * Command button that has both action and popup areas, with the main
		 * text click activating the action.
		 */
		ACTION_AND_POPUP_MAIN_ACTION(true, true),

		/**
		 * Command button that has both action and popup areas, with the main
		 * text click activating the popup.
		 */
		ACTION_AND_POPUP_MAIN_POPUP(true, true);

		/**
		 * <code>true</code> if the command button kind has an action.
		 */
		private boolean hasAction;

		/**
		 * <code>true</code> if the command button kind has a popup.
		 */
		private boolean hasPopup;

		/**
		 * Constructs a new command button kind.
		 * 
		 * @param hasAction
		 *            Indicates whether the command button kind has an action.
		 * @param hasPopup
		 *            Indicates whether the command button kind has a popup.
		 */
		private CommandButtonKind(boolean hasAction, boolean hasPopup) {
			this.hasAction = hasAction;
			this.hasPopup = hasPopup;
		}

		/**
		 * Returns indication whether this command button kind has an action.
		 * 
		 * @return <code>true</code> if the command button kind has an action,
		 *         <code>false</code> otherwise.
		 */
		public boolean hasAction() {
			return hasAction;
		}

		/**
		 * Returns indication whether this command button kind has a popup.
		 * 
		 * @return <code>true</code> if the command button kind has a popup,
		 *         <code>false</code> otherwise.
		 */
		public boolean hasPopup() {
			return hasPopup;
		}
	}

	/**
	 * Orientation kind for the popup.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static enum CommandButtonPopupOrientationKind {
		/**
		 * Indicates that the popup should be displayed below the button.
		 */
		DOWNWARD,

		/**
		 * Indicates that the popup should be displayed to the side of the
		 * button.
		 */
		SIDEWARD
	}

	/**
	 * Extension of the default button model that supports the
	 * {@link PopupButtonModel} interface.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private static class DefaultPopupButtonModel extends DefaultButtonModel
			implements PopupButtonModel {
		/**
		 * Timer for the auto-repeat action mode.
		 */
		protected Timer autoRepeatTimer;

		/**
		 * Identifies the "popup showing" bit in the bitmask, which indicates
		 * that the visibility status of the associated popup.
		 */
		public final static int POPUP_SHOWING = 1 << 8;

		/**
		 * Creates a new default popup button model.
		 */
		public DefaultPopupButtonModel() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jvnet.flamingo.common.PopupButtonModel#addPopupActionListener
		 * (org.jvnet.flamingo.common.PopupActionListener)
		 */
		@Override
		public void addPopupActionListener(PopupActionListener l) {
			listenerList.add(PopupActionListener.class, l);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jvnet.flamingo.common.PopupButtonModel#removePopupActionListener
		 * (org.jvnet.flamingo.common.PopupActionListener)
		 */
		@Override
		public void removePopupActionListener(PopupActionListener l) {
			listenerList.remove(PopupActionListener.class, l);
		}

		/**
		 * Notifies all listeners that have registered interest for notification
		 * on this event type.
		 * 
		 * @param e
		 *            the <code>ActionEvent</code> to deliver to listeners
		 * @see EventListenerList
		 */
		protected void firePopupActionPerformed(ActionEvent e) {
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == PopupActionListener.class) {
					((PopupActionListener) listeners[i + 1]).actionPerformed(e);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.DefaultButtonModel#setPressed(boolean)
		 */
		@Override
		public void setPressed(boolean b) {
			if ((isPressed() == b) || !isEnabled()) {
				return;
			}

			if (b) {
				stateMask |= PRESSED;
			} else {
				stateMask &= ~PRESSED;
			}

			if (isPressed() && isArmed()) {
				// fire the popup action on button press and not on button
				// release - like the comboboxes
				int modifiers = 0;
				AWTEvent currentEvent = EventQueue.getCurrentEvent();
				if (currentEvent instanceof InputEvent) {
					modifiers = ((InputEvent) currentEvent).getModifiers();
				} else if (currentEvent instanceof ActionEvent) {
					modifiers = ((ActionEvent) currentEvent).getModifiers();
				}
				firePopupActionPerformed(new ActionEvent(this,
						ActionEvent.ACTION_PERFORMED, getActionCommand(),
						EventQueue.getMostRecentEventTime(), modifiers));
			}

			fireStateChanged();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.jvnet.flamingo.common.PopupButtonModel#isPopupShowing()
		 */
		@Override
		public boolean isPopupShowing() {
			return (stateMask & POPUP_SHOWING) != 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jvnet.flamingo.common.PopupButtonModel#setPopupShowing(boolean)
		 */
		@Override
		public void setPopupShowing(boolean b) {
			// System.out.println(this.isPopupShowing() + "-->" + b);
			if (this.isPopupShowing() == b) {
				return;
			}

			if (b) {
				stateMask |= POPUP_SHOWING;
			} else {
				stateMask &= ~POPUP_SHOWING;
			}

			fireStateChanged();
		}
	}

	/**
	 * Creates a new command button with empty text
	 * 
	 * @param icon
	 *            Button icon.
	 */
	public JCommandButton(ResizableIcon icon) {
		this(null, icon);
	}

	/**
	 * Creates a new command button without an icon.
	 * 
	 * @param title
	 *            Button title. May contain any number of words.
	 */
	public JCommandButton(String title) {
		this(title, null);
	}

	/**
	 * Creates a new command button.
	 * 
	 * @param title
	 *            Button title. May contain any number of words.
	 * @param icon
	 *            Button icon.
	 */
	public JCommandButton(String title, ResizableIcon icon) {
		super(title, icon);

		this.setActionModel(new ActionRepeatableButtonModel(this));

		// important - handler creation must be done before setting
		// the popup model so that it can be registered to track the
		// changes
		this.popupHandler = new PopupHandler();
		this.setPopupModel(new DefaultPopupButtonModel());

		this.commandButtonKind = CommandButtonKind.ACTION_ONLY;
		this.popupOrientationKind = CommandButtonPopupOrientationKind.DOWNWARD;
		// this.displayState = CommandButtonDisplayState.CUSTOM;
		this.isAutoRepeatAction = false;
		this.autoRepeatInitialInterval = 500;
		this.autoRepeatSubsequentInterval = 100;

		this.updateUI();
	}

	/**
	 * Returns the command button kind of this button.
	 * 
	 * @return Command button kind of this button.
	 * @see #setCommandButtonKind(CommandButtonKind)
	 */
	public CommandButtonKind getCommandButtonKind() {
		return this.commandButtonKind;
	}

	/**
	 * Sets the kind for this button. Fires a <code>commandButtonKind</code>
	 * property change event.
	 * 
	 * @param commandButtonKind
	 *            The new button kind.
	 * @see #getCommandButtonKind()
	 */
	public void setCommandButtonKind(CommandButtonKind commandButtonKind) {
		CommandButtonKind old = this.commandButtonKind;
		this.commandButtonKind = commandButtonKind;
		if (old != this.commandButtonKind) {
			firePropertyChange("commandButtonKind", old, this.commandButtonKind);
		}
	}

	/**
	 * Returns the popup orientation kind of this button.
	 * 
	 * @return Popup orientation kind of this button.
	 * @see #setPopupOrientationKind(CommandButtonPopupOrientationKind)
	 */
	public CommandButtonPopupOrientationKind getPopupOrientationKind() {
		return this.popupOrientationKind;
	}

	/**
	 * Sets the popup orientation for this button. Fires a
	 * <code>popupOrientationKind</code> property change event.
	 * 
	 * @param popupOrientationKind
	 *            The new popup orientation kind.
	 * @see #getPopupOrientationKind()
	 */
	public void setPopupOrientationKind(
			CommandButtonPopupOrientationKind popupOrientationKind) {
		CommandButtonPopupOrientationKind old = this.popupOrientationKind;
		this.popupOrientationKind = popupOrientationKind;
		if (old != this.popupOrientationKind) {
			firePropertyChange("popupOrientationKind", old,
					this.popupOrientationKind);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((CommandButtonUI) UIManager.getUI(this));
		} else {
			setUI(BasicCommandButtonUI.createUI(this));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getUIClassID()
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	/**
	 * Returns the associated popup callback.
	 * 
	 * @return The associated popup callback.
	 * @see #setPopupCallback(PopupPanelCallback)
	 */
	public PopupPanelCallback getPopupCallback() {
		return this.popupCallback;
	}

	/**
	 * Sets new popup callback for <code>this</code> button.
	 * 
	 * @param popupCallback
	 *            New popup callback for <code>this</code> button.
	 * @see #getPopupCallback()
	 */
	public void setPopupCallback(PopupPanelCallback popupCallback) {
		this.popupCallback = popupCallback;
	}

	/**
	 * Sets the auto-repeat action indication.
	 * 
	 * @param isAutoRepeatAction
	 *            If <code>true</code>, pressing the button will activate
	 *            auto-repeat action mode. When the button is not in the
	 *            auto-repeat action mode, the registered action listeners are
	 *            activated when the mouse is released (just as with the base
	 *            {@link AbstractButton}). When the button is in auto-repeat
	 *            mode, the registered action listeners are activated when the
	 *            mouse is pressed. In addition, is the mouse is still pressed
	 *            after {@link #getAutoRepeatInitialInterval()}, the action
	 *            listeners will be activated every
	 *            {@link #getAutoRepeatSubsequentInterval()} until the button is
	 *            disabled or the mouse is released.
	 * @see #setAutoRepeatActionIntervals(int, int)
	 * @see #isAutoRepeatAction()
	 */
	public void setAutoRepeatAction(boolean isAutoRepeatAction) {
		this.isAutoRepeatAction = isAutoRepeatAction;
	}

	/**
	 * Sets the intervals for the auto-repeat action mode.
	 * 
	 * @param initial
	 *            The initial interval for invoking the registered action
	 *            listeners in the auto-repeat action mode.
	 * @param subsequent
	 *            The subsequent interval for invoking the registered action
	 *            listeners in the auto-repeat action mode.
	 * @see #setAutoRepeatAction(boolean)
	 * @see #isAutoRepeatAction()
	 * @see #getAutoRepeatInitialInterval()
	 * @see #getAutoRepeatSubsequentInterval()
	 */
	public void setAutoRepeatActionIntervals(int initial, int subsequent) {
		this.autoRepeatInitialInterval = initial;
		this.autoRepeatSubsequentInterval = subsequent;
	}

	/**
	 * Returns indication whether the button is in auto-repeat action mode.
	 * 
	 * @return <code>true</code> if the button is in auto-repeat action mode,
	 *         <code>false</code> otherwise.
	 * @see #setAutoRepeatAction(boolean)
	 * @see #setAutoRepeatActionIntervals(int, int)
	 * @see #getAutoRepeatInitialInterval()
	 * @see #getAutoRepeatSubsequentInterval()
	 */
	public boolean isAutoRepeatAction() {
		return this.isAutoRepeatAction;
	}

	/**
	 * Returns the initial interval for invoking the registered action listeners
	 * in the auto-repeat action mode.
	 * 
	 * @return The initial interval for invoking the registered action listeners
	 *         in the auto-repeat action mode.
	 * @see #setAutoRepeatActionIntervals(int, int)
	 * @see #setAutoRepeatAction(boolean)
	 * @see #isAutoRepeatAction()
	 * @see #getAutoRepeatSubsequentInterval()
	 */
	public int getAutoRepeatInitialInterval() {
		return autoRepeatInitialInterval;
	}

	/**
	 * Returns the subsequent interval for invoking the registered action
	 * listeners in the auto-repeat action mode.
	 * 
	 * @return The subsequent interval for invoking the registered action
	 *         listeners in the auto-repeat action mode.
	 * @see #setAutoRepeatActionIntervals(int, int)
	 * @see #setAutoRepeatAction(boolean)
	 * @see #isAutoRepeatAction()
	 * @see #getAutoRepeatInitialInterval()
	 */
	public int getAutoRepeatSubsequentInterval() {
		return autoRepeatSubsequentInterval;
	}

	/**
	 * Sets action-on-rollover mode. When this mode is on, button will fire
	 * action events when it gets rollover (instead of press). Combine with
	 * {@link #setAutoRepeatAction(boolean)} passing <code>true</code> to get
	 * auto-repeat action fired on rollover (useful for quicker manipulation of
	 * scroller buttons, for example).
	 * 
	 * @param isFireActionOnRollover
	 *            If <code>true</code>, the button is moved into the
	 *            action-on-rollover mode.
	 * @see #isFireActionOnRollover()
	 */
	public void setFireActionOnRollover(boolean isFireActionOnRollover) {
		this.isFireActionOnRollover = isFireActionOnRollover;
	}

	/**
	 * Returns indication whether this button is in action-on-rollover mode.
	 * 
	 * @return <code>true</code> if this button is in action-on-rollover mode,
	 *         <code>false</code> otherwise.
	 * @see #setFireActionOnRollover(boolean)
	 */
	public boolean isFireActionOnRollover() {
		return this.isFireActionOnRollover;
	}

	/**
	 * Returns the popup model of this button.
	 * 
	 * @return The popup model of this button.
	 * @see #setPopupModel(PopupButtonModel)
	 */
	public PopupButtonModel getPopupModel() {
		return this.popupModel;
	}

	/**
	 * Sets the new popup model for this button. Fires a <code>popupModel</code>
	 * property change event.
	 * 
	 * @param newModel
	 *            The new popup model for this button.
	 * @see #getPopupModel()
	 */
	public void setPopupModel(PopupButtonModel newModel) {

		PopupButtonModel oldModel = getPopupModel();

		if (oldModel != null) {
			oldModel.removeChangeListener(this.popupHandler);
			oldModel.removeActionListener(this.popupHandler);
		}

		this.popupModel = newModel;

		if (newModel != null) {
			newModel.addChangeListener(this.popupHandler);
			newModel.addActionListener(this.popupHandler);
		}

		firePropertyChange("popupModel", oldModel, newModel);
		if (newModel != oldModel) {
			revalidate();
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.common.AbstractCommandButton#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean b) {
		if (!b && popupModel.isRollover()) {
			popupModel.setRollover(false);
		}
		super.setEnabled(b);
		popupModel.setEnabled(b);
	}

	/**
	 * Default popup handler.
	 * 
	 * @author Kirill Grouchnikov
	 */
	class PopupHandler implements PopupActionListener, ChangeListener {
		@Override
        public void stateChanged(ChangeEvent e) {
			fireStateChanged();
			repaint();
		}

		@Override
        public void actionPerformed(ActionEvent event) {
			firePopupActionPerformed(event);
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * <code>event</code> parameter.
	 * 
	 * @param event
	 *            the <code>ActionEvent</code> object
	 * @see EventListenerList
	 */
	protected void firePopupActionPerformed(ActionEvent event) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		ActionEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PopupActionListener.class) {
				// Lazily create the event:
				if (e == null) {
					String actionCommand = event.getActionCommand();
					e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
							actionCommand, event.getWhen(), event
									.getModifiers());
				}
				((PopupActionListener) listeners[i + 1]).actionPerformed(e);
			}
		}
	}

	@Override
	boolean hasRichTooltips() {
		return super.hasRichTooltips() || (this.popupRichTooltip != null);
	}

	/**
	 * Sets the rich tooltip for the popup area of this button.
	 * 
	 * @param richTooltip
	 *            Rich tooltip for the popup area of this button.
	 * @see #getRichTooltip(MouseEvent)
	 * @see #setActionRichTooltip(RichTooltip)
	 */
	public void setPopupRichTooltip(RichTooltip richTooltip) {
		this.popupRichTooltip = richTooltip;
		RichToolTipManager richToolTipManager = RichToolTipManager
				.sharedInstance();
		if (this.hasRichTooltips()) {
			richToolTipManager.registerComponent(this);
		} else {
			richToolTipManager.unregisterComponent(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.AbstractCommandButton#getRichTooltip(java.awt
	 * .event.MouseEvent)
	 */
	@Override
	public RichTooltip getRichTooltip(MouseEvent event) {
		CommandButtonUI ui = this.getUI();
		if (ui.getLayoutInfo().actionClickArea.contains(event.getPoint()))
			return super.getRichTooltip(event);
		if (ui.getLayoutInfo().popupClickArea.contains(event.getPoint()))
			return this.popupRichTooltip;
		return null;
	}

	/**
	 * Returns the key tip for the popup area of this button.
	 * 
	 * @return The key tip for the popup area of this button.
	 * @see #setPopupKeyTip(String)
	 * @see #getActionKeyTip()
	 */
	public String getPopupKeyTip() {
		return this.popupKeyTip;
	}

	/**
	 * Sets the key tip for the popup area of this button. Fires a
	 * <code>popupKeyTip</code> property change event.
	 * 
	 * @param popupKeyTip
	 *            The key tip for the popup area of this button.
	 * @see #getPopupKeyTip()
	 * @see #setActionKeyTip(String)
	 */
	public void setPopupKeyTip(String popupKeyTip) {
		if (!canHaveBothKeyTips() && (popupKeyTip != null)
				&& (this.actionKeyTip != null)) {
			throw new IllegalArgumentException(
					"Action *and* popup keytips are not supported at the same time");
		}

		String old = this.popupKeyTip;
		this.popupKeyTip = popupKeyTip;
		this.firePropertyChange("popupKeyTip", old, this.popupKeyTip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.common.AbstractCommandButton#setActionKeyTip(java.
	 * lang.String)
	 */
	@Override
	public void setActionKeyTip(String actionKeyTip) {
		if (!canHaveBothKeyTips() && (popupKeyTip != null)
				&& (this.actionKeyTip != null)) {
			throw new IllegalArgumentException(
					"Action *and* popup keytips are not supported at the same time");
		}

		super.setActionKeyTip(actionKeyTip);
	}

	/**
	 * Returns indication whether key tips can be installed on both action and
	 * popup areas of this button. This method is for internal use only.
	 * 
	 * @return <code>true</code> if key tips can be installed on both action and
	 *         popup areas of this button, <code>false</code> otherwise.
	 */
	boolean canHaveBothKeyTips() {
		return false;
	}

	/**
	 * Programmatically perform a "click" on the popup area. This does the same
	 * thing as if the user had pressed and released the popup area of the
	 * button.
	 */
	public void doPopupClick() {
		Dimension size = getSize();
		PopupButtonModel popupModel = this.getPopupModel();
		popupModel.setArmed(true);
		popupModel.setPressed(true);
		paintImmediately(new Rectangle(0, 0, size.width, size.height));
		try {
			Thread.sleep(100);
		} catch (InterruptedException ie) {
		}
		popupModel.setPressed(false);
		popupModel.setArmed(false);
		popupModel.setPopupShowing(true);
		paintImmediately(new Rectangle(0, 0, size.width, size.height));
	}
}
