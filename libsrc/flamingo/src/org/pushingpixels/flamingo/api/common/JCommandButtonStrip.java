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

import java.awt.Component;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pushingpixels.flamingo.internal.ui.common.BasicCommandButtonStripUI;
import org.pushingpixels.flamingo.internal.ui.common.CommandButtonStripUI;

/**
 * Button strip component. Provides visual appearance of a strip. The buttons in
 * the strip are either drawn horizontally with no horizontal space between them
 * or drawn vertically with no vertical space between them.
 * 
 * @author Kirill Grouchnikov
 */
public class JCommandButtonStrip extends JComponent {
	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "CommandButtonStripUI";

	/**
	 * Element state for the buttons in this button strip. Default state is
	 * {@link CommandButtonDisplayState#SMALL}.
	 */
	protected CommandButtonDisplayState displayState;

	/**
	 * Scale factor for horizontal gaps.
	 * 
	 * @see #setVGapScaleFactor(double)
	 */
	protected double hgapScaleFactor;

	/**
	 * Scale factor for vertical gaps.
	 * 
	 * @see #setVGapScaleFactor(double)
	 */
	protected double vgapScaleFactor;

	/**
	 * Button strip orientation.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public enum StripOrientation {
		/**
		 * Horizontal strip orientation.
		 */
		HORIZONTAL,

		/**
		 * Vertical strip orientation.
		 */
		VERTICAL
	}

	/**
	 * Orientation of <code>this</code> strip.
	 * 
	 * @see #getOrientation()
	 */
	private StripOrientation orientation;

	/**
	 * Creates an empty horizontally-oriented strip.
	 */
	public JCommandButtonStrip() {
		this(StripOrientation.HORIZONTAL);
	}

	/**
	 * Creates an empty strip.
	 * 
	 * @param orientation
	 *            Orientation for this strip.
	 */
	public JCommandButtonStrip(StripOrientation orientation) {
		this.orientation = orientation;
		this.displayState = CommandButtonDisplayState.SMALL;
		switch (orientation) {
		case HORIZONTAL:
			this.hgapScaleFactor = 0.75;
			this.vgapScaleFactor = 1.0;
			break;
		case VERTICAL:
			this.hgapScaleFactor = 1.0;
			this.vgapScaleFactor = 0.75;
			break;
		}
		this.setOpaque(false);
		updateUI();
	}

	/**
	 * Sets the display state for the buttons in this button strip. This method
	 * must be called <em>before</em> adding the first command button. The
	 * default state is {@link CommandButtonDisplayState#SMALL}.
	 * 
	 * @param elementState
	 *            New element state for the buttons in this button strip.
	 */
	public void setDisplayState(CommandButtonDisplayState elementState) {
		if (this.getComponentCount() > 0) {
			throw new IllegalStateException(
					"Can't call this method after buttons have been already added");
		}
		this.displayState = elementState;
	}

	/**
	 * Sets the horizontal gap scale factor for the buttons in this button
	 * strip. This method must be called <em>before</em> adding the first
	 * command button.
	 * 
	 * <p>
	 * The default horizontal gap scale factor for horizontally oriented strips
	 * is 0.75. The default horizontal gap scale factor for vertically oriented
	 * strips is 1.0.
	 * </p>
	 * 
	 * @param hgapScaleFactor
	 *            New horizontal gap scale factor for the buttons in this button
	 *            strip.
	 * @see #setVGapScaleFactor(double)
	 */
	public void setHGapScaleFactor(double hgapScaleFactor) {
		if (this.getComponentCount() > 0) {
			throw new IllegalStateException(
					"Can't call this method after buttons have been already added");
		}
		this.hgapScaleFactor = hgapScaleFactor;
	}

	/**
	 * Sets the vertical gap scale factor for the buttons in this button strip.
	 * This method must be called <em>before</em> adding the first command
	 * button.
	 * 
	 * <p>
	 * The default vertical gap scale factor for vertically oriented strips is
	 * 0.75. The default vertical gap scale factor for horizontally oriented
	 * strips is 1.0.
	 * </p>
	 * 
	 * @param vgapScaleFactor
	 *            New vertical gap scale factor for the buttons in this button
	 *            strip.
	 * @see #setHGapScaleFactor(double)
	 */
	public void setVGapScaleFactor(double vgapScaleFactor) {
		if (this.getComponentCount() > 0) {
			throw new IllegalStateException(
					"Can't call this method after buttons have been already added");
		}
		this.vgapScaleFactor = vgapScaleFactor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#add(java.awt.Component, java.lang.Object, int)
	 */
	@Override
	public void add(Component comp, Object constraints, int index) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
	 */
	@Override
	public void add(Component comp, Object constraints) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#add(java.awt.Component, int)
	 */
	@Override
	public Component add(Component comp, int index) {
		if (!(comp instanceof AbstractCommandButton))
			throw new UnsupportedOperationException();
		this.configureCommandButton((AbstractCommandButton) comp);
		return super.add(comp, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#add(java.awt.Component)
	 */
	@Override
	public Component add(Component comp) {
		if (!(comp instanceof AbstractCommandButton))
			throw new UnsupportedOperationException();
		try {
			this.configureCommandButton((AbstractCommandButton) comp);
			Component result = super.add(comp);
			return result;
		} finally {
			this.fireStateChanged();
		}
	}

	/**
	 * Configures the specified command button.
	 * 
	 * @param button
	 *            Command button to configure.
	 */
	private void configureCommandButton(AbstractCommandButton button) {
		button.setDisplayState(this.displayState);
		button.setHGapScaleFactor(this.hgapScaleFactor);
		button.setVGapScaleFactor(this.vgapScaleFactor);
		button.setFlat(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#add(java.lang.String, java.awt.Component)
	 */
	@Override
	public Component add(String name, Component comp) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the new UI delegate.
	 * 
	 * @param ui
	 *            New UI delegate.
	 */
	public void setUI(CommandButtonStripUI ui) {
		super.setUI(ui);
	}

	/**
	 * Resets the UI property to a value from the current look and feel.
	 * 
	 * @see JComponent#updateUI
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((CommandButtonStripUI) UIManager.getUI(this));
		} else {
			setUI(BasicCommandButtonStripUI.createUI(this));
		}
	}

	/**
	 * Returns the UI object which implements the L&F for this component.
	 * 
	 * @return a <code>ButtonStripUI</code> object
	 * @see #setUI(org.pushingpixels.flamingo.internal.ui.common.CommandButtonStripUI)
	 */
	public CommandButtonStripUI getUI() {
		return (CommandButtonStripUI) ui;
	}

	/**
	 * Returns the name of the UI class that implements the L&F for this
	 * component.
	 * 
	 * @return the string "ButtonStripUI"
	 * @see JComponent#getUIClassID
	 * @see UIDefaults#getUI(javax.swing.JComponent)
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	/**
	 * Returns the number of buttons in <code>this</code> strip.
	 * 
	 * @return Number of buttons in <code>this</code> strip.
	 * @see #getButton(int)
	 */
	public int getButtonCount() {
		return this.getComponentCount();
	}

	/**
	 * Returns the specified button component of <code>this</code> strip.
	 * 
	 * @param index
	 *            Button index.
	 * @return The matching button.
	 * @see #getButtonCount()
	 */
	public AbstractCommandButton getButton(int index) {
		return (AbstractCommandButton) this.getComponent(index);
	}

	/**
	 * Checks whether the specified button is the first button in
	 * <code>this</code> strip.
	 * 
	 * @param button
	 *            Button to check.
	 * @return <code>true</code> if the specified button is the first button in
	 *         <code>this</code> strip, <code>false</code> otherwise.
	 * @see #isLast(AbstractCommandButton)
	 */
	public boolean isFirst(AbstractCommandButton button) {
		return (button == this.getButton(0));
	}

	/**
	 * Checks whether the specified button is the last button in
	 * <code>this</code> strip.
	 * 
	 * @param button
	 *            Button to check.
	 * @return <code>true</code> if the specified button is the last button in
	 *         <code>this</code> strip, <code>false</code> otherwise.
	 * @see #isFirst(AbstractCommandButton)
	 */
	public boolean isLast(AbstractCommandButton button) {
		return (button == this.getButton(this.getButtonCount() - 1));
	}

	/**
	 * Returns the orientation of <code>this</code> strip.
	 * 
	 * @return Orientation of <code>this</code> strip.
	 */
	public StripOrientation getOrientation() {
		return orientation;
	}

	/**
	 * Adds the specified change listener to track changes to this command
	 * button strip.
	 * 
	 * @param l
	 *            Change listener to add.
	 * @see #removeChangeListener(ChangeListener)
	 */
	public void addChangeListener(ChangeListener l) {
		this.listenerList.add(ChangeListener.class, l);
	}

	/**
	 * Removes the specified change listener from tracking changes to this
	 * command button strip.
	 * 
	 * @param l
	 *            Change listener to remove.
	 * @see #addChangeListener(ChangeListener)
	 */
	public void removeChangeListener(ChangeListener l) {
		this.listenerList.remove(ChangeListener.class, l);
	}

	/**
	 * Notifies all registered listener that the state of this command button
	 * strip has changed.
	 */
	protected void fireStateChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = this.listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		ChangeEvent event = new ChangeEvent(this);
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1]).stateChanged(event);
			}
		}
	}

}
