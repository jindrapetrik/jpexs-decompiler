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
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.JToggleButton.ToggleButtonModel;

import org.pushingpixels.flamingo.api.common.JCommandToggleButton;

/**
 * Extension of the default toggle button model that supports the
 * {@link ActionButtonModel} interface. This is the default core action model
 * set on {@link JCommandToggleButton}s.
 * 
 * @author Kirill Grouchnikov
 */
public class ActionToggleButtonModel extends ToggleButtonModel implements
		ActionButtonModel {
	/**
	 * Indication whether the action is fired on mouse press (as opposed to
	 * mouse release).
	 */
	protected boolean toFireActionOnPress;

	/**
	 * Creates a new model.
	 * 
	 * @param toFireActionOnPress
	 *            If <code>true</code>, the action will be fired on mouse press,
	 *            if <code>false</code>, the action will be fired on mouse
	 *            release.
	 */
	public ActionToggleButtonModel(boolean toFireActionOnPress) {
		this.toFireActionOnPress = toFireActionOnPress;
	}

	@Override
	public boolean isFireActionOnPress() {
		return this.toFireActionOnPress;
	}

	@Override
	public void setFireActionOnPress(boolean toFireActionOnPress) {
		this.toFireActionOnPress = toFireActionOnPress;
	}

	@Override
	public void setPressed(boolean b) {
		if ((isPressed() == b) || !isEnabled()) {
			return;
		}

		if (isArmed()) {
			// change selection prior to firing the action event
			if (!this.isFireActionOnPress()) {
				if (!b) {
					setSelected(!this.isSelected());
				}
			} else {
				if (b) {
					setSelected(!this.isSelected());
				}
			}
		}

		if (b) {
			stateMask |= PRESSED;
		} else {
			stateMask &= ~PRESSED;
		}

		fireStateChanged();

		boolean toFireAction = false;
		if (this.isFireActionOnPress()) {
			toFireAction = isPressed() && isArmed();
		} else {
			toFireAction = !isPressed() && isArmed();
		}

		if (toFireAction) {
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
		}
	}
}
