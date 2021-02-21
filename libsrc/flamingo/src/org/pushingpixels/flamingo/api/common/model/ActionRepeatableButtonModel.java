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
package org.pushingpixels.flamingo.api.common.model;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.*;

import javax.swing.DefaultButtonModel;
import javax.swing.Timer;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;

/**
 * Extension of the default button model that supports the
 * {@link ActionButtonModel} interface and repeated invocation of action
 * listeners on mouse rollover. This is the default core action model set on
 * {@link JCommandButton}s.
 * 
 * @author Kirill Grouchnikov
 */
public class ActionRepeatableButtonModel extends DefaultButtonModel implements
		ActionButtonModel {
	/**
	 * The button behind the model.
	 */
	private JCommandButton commandButton;

	/**
	 * Timer for the auto-repeat action mode.
	 */
	protected Timer autoRepeatTimer;

	/**
	 * Indication whether the action is fired on mouse press (as opposed to
	 * mouse release).
	 */
	protected boolean toFireActionOnPress;

	/**
	 * Creates a new button model.
	 * 
	 * @param commandButton
	 *            The associated command button.
	 */
	public ActionRepeatableButtonModel(JCommandButton commandButton) {
		super();
		this.commandButton = commandButton;
		this.toFireActionOnPress = false;
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

		boolean toFireFirstAction = isArmed();
		// if the button is in auto-repeat action mode, the action
		// starts firing on press-down and not on press-up
		if (commandButton.isAutoRepeatAction() || isFireActionOnPress())
			toFireFirstAction = isPressed() && toFireFirstAction;
		else
			toFireFirstAction = !isPressed() && toFireFirstAction;

		// no action on popup only command buttons
		if (commandButton.getCommandButtonKind() == CommandButtonKind.POPUP_ONLY)
			toFireFirstAction = false;

		if (this.commandButton.isFireActionOnRollover()) {
			// the action is invoked on rollover
			toFireFirstAction = false;
		}

		int modifiers = 0;
		AWTEvent currentEvent = EventQueue.getCurrentEvent();
		if (currentEvent instanceof InputEvent) {
			modifiers = ((InputEvent) currentEvent).getModifiers();
		} else if (currentEvent instanceof ActionEvent) {
			modifiers = ((ActionEvent) currentEvent).getModifiers();
		}

		if (toFireFirstAction) {
			fireActionPerformed(new ActionEvent(this,
					ActionEvent.ACTION_PERFORMED, getActionCommand(),
					EventQueue.getMostRecentEventTime(), modifiers));
			if (commandButton.isAutoRepeatAction()) {
				// start timer
				this.startActionTimer(modifiers);
			}
		}

		// we need to stop timer when the non-action-on-rollover button
		// gets pressed=false and it is in auto-repeat mode
		if (!this.commandButton.isFireActionOnRollover()) {
			if (this.commandButton.isAutoRepeatAction() && !b) {
				this.stopActionTimer();
			}
		}

		fireStateChanged();
	}

	@Override
	public void setRollover(boolean b) {
		if ((isRollover() == b) || !isEnabled()) {
			return;
		}

		if (b) {
			stateMask |= ROLLOVER;
		} else {
			stateMask &= ~ROLLOVER;
		}

		if (this.commandButton.isFireActionOnRollover()) {
			if (b
					&& !this.isActionTimerRunning()
					&& (this.commandButton.getCommandButtonKind() != CommandButtonKind.POPUP_ONLY)) {
				// action-on-rollover non-popup-only button that gained
				// rollover and the action timer is not running - fire the
				// first event and start the action timer.
				int modifiers = 0;
				AWTEvent currentEvent = EventQueue.getCurrentEvent();
				if (currentEvent instanceof InputEvent) {
					modifiers = ((InputEvent) currentEvent).getModifiers();
				} else if (currentEvent instanceof ActionEvent) {
					modifiers = ((ActionEvent) currentEvent).getModifiers();
				}

				fireActionPerformed(new ActionEvent(this,
						ActionEvent.ACTION_PERFORMED, getActionCommand(),
						EventQueue.getMostRecentEventTime(), modifiers));
				if (commandButton.isAutoRepeatAction()) {
					// start timer
					this.startActionTimer(modifiers);
				}
			}

			if (!b) {
				this.stopActionTimer();
			}
		}

		fireStateChanged();
	}

	/**
	 * Stop the action timer.
	 */
	private void stopActionTimer() {
		if (this.autoRepeatTimer != null)
			this.autoRepeatTimer.stop();
	}

	/**
	 * Checks whether the action timer is running.
	 * 
	 * @return <code>true</code> if the action timer is running,
	 *         <code>false</code> otherwise.
	 */
	private boolean isActionTimerRunning() {
		if (this.autoRepeatTimer == null)
			return false;
		return this.autoRepeatTimer.isRunning();
	}

	/**
	 * Starts the action timer, passing the specified modifiers to the action
	 * event that will be fired in a loop.
	 * 
	 * @param modifiers
	 *            Modifiers for the action event to be fired.
	 */
	private void startActionTimer(final int modifiers) {
		this.autoRepeatTimer = new Timer(this.commandButton
				.getAutoRepeatSubsequentInterval(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isEnabled() || !commandButton.isVisible()
						|| !commandButton.isDisplayable()) {
					// stop the timer when the button becomes
					// disabled, invisible or undisplayable
					autoRepeatTimer.stop();
					return;
				}
				fireActionPerformed(new ActionEvent(this,
						ActionEvent.ACTION_PERFORMED, getActionCommand(),
						EventQueue.getMostRecentEventTime(), modifiers));
			}
		});
		this.autoRepeatTimer.setInitialDelay(this.commandButton
				.getAutoRepeatInitialInterval());
		this.autoRepeatTimer.start();
	}

	@Override
	public boolean isFireActionOnPress() {
		return this.toFireActionOnPress;
	}

	@Override
	public void setFireActionOnPress(boolean toFireActionOnPress) {
		this.toFireActionOnPress = toFireActionOnPress;
	}
}