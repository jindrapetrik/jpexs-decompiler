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
package org.pushingpixels.flamingo.api.ribbon;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.internal.ui.ribbon.BasicRibbonComponentUI;
import org.pushingpixels.flamingo.internal.ui.ribbon.RibbonComponentUI;

/**
 * Wrapper around core and 3rd party Swing controls to allow placing them in the
 * {@link JRibbonBand}.
 * 
 * @author Kirill Grouchnikov
 */
public class JRibbonComponent extends RichToolTipManager.JTrackableComponent {
	/**
	 * Wrapper icon. Can be <code>null</code>.
	 * 
	 * @see #JRibbonComponent(ResizableIcon, String, JComponent)
	 */
	private ResizableIcon icon;

	/**
	 * Wrapper caption. Can be <code>null</code>.
	 * 
	 * @see #JRibbonComponent(ResizableIcon, String, JComponent)
	 */
	private String caption;

	/**
	 * The wrapped component. Is guaranteed to be non <code>null</code>.
	 */
	private JComponent mainComponent;

	/**
	 * Indication whether this wrapper is simple. A simple wrapper has
	 * <code>null</code> {@link #icon} and <code>null</code> {@link #caption}.
	 */
	private boolean isSimpleWrapper;

	/**
	 * The key tip for this wrapper component.
	 * 
	 * @see #setKeyTip(String)
	 * @see #getKeyTip()
	 */
	private String keyTip;

	/**
	 * The rich tooltip for this wrapper component.
	 * 
	 * @see #setRichTooltip(RichTooltip)
	 * @see #getRichTooltip(MouseEvent)
	 */
	private RichTooltip richTooltip;

	/**
	 * The horizontal alignment for this wrapper component.
	 * 
	 * @see #getHorizontalAlignment()
	 * @see #setHorizontalAlignment(HorizontalAlignment)
	 */
	private HorizontalAlignment horizontalAlignment;

	private RibbonElementPriority displayPriority;

	private boolean isResizingAware;

	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "RibbonComponentUI";

	/**
	 * Creates a simple wrapper with no icon and no caption.
	 * 
	 * @param mainComponent
	 *            Wrapped component. Cannot be <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>mainComponent</code> is <code>null</code>.
	 */
	public JRibbonComponent(JComponent mainComponent) {
		if (mainComponent == null)
			throw new IllegalArgumentException(
					"All parameters must be non-null");
		this.mainComponent = mainComponent;
		this.isSimpleWrapper = true;
		this.horizontalAlignment = HorizontalAlignment.LEADING;
		this.isResizingAware = false;
		this.displayPriority = RibbonElementPriority.TOP;

		this.updateUI();
	}

	/**
	 * Creates a wrapper with an icon and a caption.
	 * 
	 * @param icon
	 *            Wrapper icon. Can be <code>null</code>.
	 * @param caption
	 *            Wrapper caption. Cannot be <code>null</code>.
	 * @param mainComponent
	 *            Wrapped component. Cannot be <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>caption</code> or <code>mainComponent</code> is
	 *             <code>null</code>.
	 */
	public JRibbonComponent(ResizableIcon icon, String caption,
			JComponent mainComponent) {
		if (caption == null)
			throw new IllegalArgumentException("Caption must be non-null");
		if (mainComponent == null)
			throw new IllegalArgumentException(
					"Main component must be non-null");
		this.icon = icon;
		this.caption = caption;
		this.mainComponent = mainComponent;
		this.isSimpleWrapper = false;
		this.horizontalAlignment = HorizontalAlignment.TRAILING;

		this.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI(UIManager.getUI(this));
		} else {
			setUI(BasicRibbonComponentUI.createUI(this));
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
	 * Returns the UI object which implements the L&F for this component.
	 * 
	 * @return a <code>RibbonUI</code> object
	 * @see #setUI(javax.swing.plaf.ComponentUI)
	 */
	public RibbonComponentUI getUI() {
		return (RibbonComponentUI) ui;
	}

	/**
	 * Returns the wrapper icon of this wrapper component. Can return
	 * <code>null</code>.
	 * 
	 * @return The wrapper icon of this wrapper component.
	 * @see #JRibbonComponent(ResizableIcon, String, JComponent)
	 */
	public ResizableIcon getIcon() {
		return this.icon;
	}

	/**
	 * Returns the caption of this wrapper component. Can return
	 * <code>null</code>.
	 * 
	 * @return The caption of this wrapper component.
	 * @see #JRibbonComponent(ResizableIcon, String, JComponent)
	 */
	public String getCaption() {
		return this.caption;
	}

	/**
	 * Sets new value for the caption of this wrapper component.
	 * 
	 * @param caption
	 *            The new caption.
	 */
	public void setCaption(String caption) {
		if (this.isSimpleWrapper) {
			throw new IllegalArgumentException(
					"Cannot set caption on a simple component");
		}
		if (caption == null) {
			throw new IllegalArgumentException("Caption must be non-null");
		}

		String old = this.caption;
		this.caption = caption;
		this.firePropertyChange("caption", old, this.caption);
	}

	/**
	 * Returns the wrapped component of this wrapper component. The result is
	 * guaranteed to be non <code>null</code>.
	 * 
	 * @return The wrapped component of this wrapper component.
	 */
	public JComponent getMainComponent() {
		return this.mainComponent;
	}

	/**
	 * Returns indication whether this wrapper is simple.
	 * 
	 * @return <code>true</code> if both {@link #getIcon()} and
	 *         {@link #getCaption()} return <code>null</code>,
	 *         <code>false</code> otherwise.
	 */
	public boolean isSimpleWrapper() {
		return this.isSimpleWrapper;
	}

	/**
	 * Returns the key tip for this wrapper component.
	 * 
	 * @return The key tip for this wrapper component.
	 * @see #setKeyTip(String)
	 */
	public String getKeyTip() {
		return this.keyTip;
	}

	/**
	 * Sets the specified string to be the key tip for this wrapper component.
	 * Fires a <code>keyTip</code> property change event.
	 * 
	 * @param keyTip
	 *            The new key tip for this wrapper component.
	 */
	public void setKeyTip(String keyTip) {
		String old = this.keyTip;
		this.keyTip = keyTip;
		this.firePropertyChange("keyTip", old, this.keyTip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.common.RichToolTipManager.JTrackableComponent#
	 * getRichTooltip(java.awt.event.MouseEvent)
	 */
	@Override
	public RichTooltip getRichTooltip(MouseEvent mouseEvent) {
		return this.richTooltip;
	}

	/**
	 * Sets the rich tooltip for this wrapper component.
	 * 
	 * @param richTooltip
	 * @see #getRichTooltip(MouseEvent)
	 */
	public void setRichTooltip(RichTooltip richTooltip) {
		this.richTooltip = richTooltip;
		RichToolTipManager richToolTipManager = RichToolTipManager
				.sharedInstance();
		if (richTooltip != null) {
			richToolTipManager.registerComponent(this);
		} else {
			richToolTipManager.unregisterComponent(this);
		}
	}

	/**
	 * Returns the horizontal alignment for this wrapper component.
	 * 
	 * @return The horizontal alignment for this wrapper component.
	 * @see #setHorizontalAlignment(HorizontalAlignment)
	 */
	public HorizontalAlignment getHorizontalAlignment() {
		return this.horizontalAlignment;
	}

	/**
	 * Sets the specified parameter to be the horizontal alignment for this
	 * wrapper component.
	 * 
	 * @param horizontalAlignment
	 *            The new horizontal alignment for this wrapper component.
	 * @see #getHorizontalAlignment()
	 */
	public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	public RibbonElementPriority getDisplayPriority() {
		return this.displayPriority;
	}

	public void setDisplayPriority(RibbonElementPriority displayPriority) {
		RibbonElementPriority old = this.displayPriority;
		this.displayPriority = displayPriority;
		if (old != displayPriority) {
			this.firePropertyChange("displayPriority", old,
					this.displayPriority);
		}
	}

	public boolean isResizingAware() {
		return this.isResizingAware;
	}

	public void setResizingAware(boolean isResizingAware) {
		this.isResizingAware = isResizingAware;
	}
}
