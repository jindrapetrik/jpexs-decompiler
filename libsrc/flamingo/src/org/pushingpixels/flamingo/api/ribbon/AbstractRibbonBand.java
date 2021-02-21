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

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.resize.*;
import org.pushingpixels.flamingo.internal.ui.ribbon.*;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

/**
 * Ribbon band. Is part of a logical {@link RibbonTask}. This is an abstract
 * base class for two types of ribbon bands - flow in {@link JFlowRibbonBand}
 * and general in {@link JRibbonBand}.
 * 
 * <p>
 * This class provides the following common functionality:
 * </p>
 * <ul>
 * <li>Tracking the available and current resize policies.</li>
 * <li>Tracking the collapsed state of the ribbon band - when there is not
 * enough horizontal space to show this panel under the smallest resize setting
 * (see {@link RibbonBandResizePolicy} and {@link CoreRibbonResizePolicies}) -
 * the band content is replaced by one collapsed button. When that button is
 * activated, the original ribbon band content is shown in a popup panel.</li>
 * <li>Associating key tip and rich tooltip with the expand button of the ribbon
 * band.</li>
 * <li>Associating key tip with the collapsed button of the ribbon band.</li>
 * </ul>
 * 
 * @author Kirill Grouchnikov
 * @param <T>
 *            Class parameter that specifies the type of band control panel
 *            implementation.
 */
public abstract class AbstractRibbonBand<T extends AbstractBandControlPanel>
		extends JComponent {
	/**
	 * The UI class ID string.
	 */
	public static final String uiClassID = "RibbonBandUI";

	/**
	 * The ribbon task of this ribbon band.
	 */
	RibbonTask ribbonTask;

	/**
	 * Band title.
	 * 
	 * @see #getTitle()
	 * @see #setTitle(String)
	 */
	private String title;

	/**
	 * Optional <code>expand</code> action listener. If present, the title pane
	 * shows button with plus sign. The action listener on the button will be
	 * <code>this</code> listener.
	 * 
	 * @see #getExpandActionListener()
	 * @see #AbstractRibbonBand(String, ResizableIcon, ActionListener,
	 *      AbstractBandControlPanel)
	 */
	private ActionListener expandActionListener;

	/**
	 * Band control panel. When there is not enough horizontal space to show
	 * this panel under the smallest resize setting, the control panel is hidden
	 * and a collapsed button is shown. When this collapsed button is activated,
	 * it shows the {@link #popupRibbonBand} in a popup panel. The collapsed
	 * button itself is implemented as a part of the UI delegate in
	 * {@link BasicRibbonBandUI}.
	 * 
	 * @see #popupRibbonBand
	 * @see #icon
	 */
	protected T controlPanel;

	/**
	 * Ribbon band shown in a popup panel when this ribbon band is in a
	 * collapsed state.
	 * 
	 * @see #controlPanel
	 * @see #getPopupRibbonBand()
	 * @see #setPopupRibbonBand(AbstractRibbonBand)
	 */
	private AbstractRibbonBand popupRibbonBand;

	/**
	 * Icon for the collapsed state. Is set on the button that represents the
	 * collapsed state of this band. The collapsed button itself is implemented
	 * as a part of the UI delegate in {@link BasicRibbonBandUI}.
	 * 
	 * @see #getIcon()
	 * @see #setIcon(ResizableIcon)
	 */
	private ResizableIcon icon;

	/**
	 * The current resize policy for this band. Must be one of the policies in
	 * the {@link #resizePolicies} list.
	 * 
	 * @see #resizePolicies
	 * @see #setCurrentResizePolicy(RibbonBandResizePolicy)
	 * @see #getCurrentResizePolicy()
	 */
	private RibbonBandResizePolicy currResizePolicy;

	/**
	 * The list of available resize policies.
	 * 
	 * @see #currResizePolicy
	 * @see #setResizePolicies(List)
	 * @see #getResizePolicies()
	 * @see #getCurrentResizePolicy()
	 */
	protected List<RibbonBandResizePolicy> resizePolicies;

	/**
	 * The key tip for the ribbon band expand button. Is relevant only when
	 * {@link #expandActionListener} is not <code>null</code>.
	 * 
	 * @see #setExpandButtonKeyTip(String)
	 * @see #getExpandButtonKeyTip()
	 */
	private String expandButtonKeyTip;

	/**
	 * The rich tooltip for the ribbon band expand button. Is relevant only when
	 * {@link #expandActionListener} is not <code>null</code>.
	 * 
	 * @see #setExpandButtonRichTooltip(RichTooltip)
	 * @see #getExpandButtonRichTooltip()
	 */
	private RichTooltip expandButtonRichTooltip;

	/**
	 * The key tip for the collapsed button which is shown when there is not
	 * enough horizontal space to show the ribbon band content under the most
	 * restrictive resize policy. The collapsed button itself is implemented as
	 * a part of the UI delegate in {@link BasicRibbonBandUI}.
	 * 
	 * @see #setCollapsedStateKeyTip(String)
	 * @see #getCollapsedStateKeyTip()
	 */
	private String collapsedStateKeyTip;

	/**
	 * Creates a new ribbon band.
	 * 
	 * @param title
	 *            Band title.
	 * @param icon
	 *            Associated icon (for collapsed state).
	 * @param expandActionListener
	 *            Expand action listener (can be <code>null</code>).
	 * @param controlPanel
	 *            The control panel of this ribbon band.
	 */
	public AbstractRibbonBand(String title, ResizableIcon icon,
			ActionListener expandActionListener, T controlPanel) {
		super();
		this.title = title;
		this.icon = icon;
		this.expandActionListener = expandActionListener;

		this.controlPanel = controlPanel;
		this.controlPanel.setRibbonBand(this);
		this.add(this.controlPanel);

		updateUI();
	}

	/**
	 * Returns a clone of this ribbon band.
	 * 
	 * @return A clone of this ribbon band.
	 */
	public abstract AbstractRibbonBand<T> cloneBand();

	/**
	 * Returns the UI object which implements the L&F for this component.
	 * 
	 * @return a <code>RibbonBandUI</code> object
	 * @see #setUI(RibbonBandUI)
	 */
	public RibbonBandUI getUI() {
		return (RibbonBandUI) ui;
	}

	/**
	 * Sets the new UI delegate.
	 * 
	 * @param ui
	 *            New UI delegate.
	 */
	public void setUI(RibbonBandUI ui) {
		super.setUI(ui);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((RibbonBandUI) UIManager.getUI(this));
		} else {
			setUI(new BasicRibbonBandUI());
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
     * Returns the icon for the collapsed state.
     *
     * @return The icon for the collapsed state.
     * @see #AbstractRibbonBand(String, ResizableIcon, ActionListener,
     *      AbstractBandControlPanel)
     */
    public ResizableIcon getIcon() {
        return this.icon;
    }

    /**
     * Changes the icon for the collapsed state of this ribbon band. Fires a <code>icon</code>
     * property change event.
     *
     * @param icon
     *            The new icon for the collapsed state.
     * @see #AbstractRibbonBand(String, ResizableIcon, ActionListener,
     *      AbstractBandControlPanel)
     * @see #getIcon()
     */
    public void setIcon(ResizableIcon icon) {
        ResizableIcon old = this.icon;
        this.icon = icon;
        this.firePropertyChange("icon", old, this.icon);
    }

	/**
	 * Returns the title of <code>this</code> band.
	 *
	 * @return Title of <code>this</code> band.
	 * @see #setTitle(String)
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Changes the title of this ribbon band. Fires a <code>title</code>
	 * property change event.
	 * 
	 * @param title
	 *            The new title for this ribbon band.
	 * @see #AbstractRibbonBand(String, ResizableIcon, ActionListener,
	 *      AbstractBandControlPanel)
	 * @see #getTitle()
	 */
	public void setTitle(String title) {
		String old = this.title;
		this.title = title;
		this.firePropertyChange("title", old, this.title);
	}

	/**
	 * Returns the expand action listener of <code>this</code> ribbon band. The
	 * result may be <code>null</code>.
	 * 
	 * @return Expand action listener of <code>this</code> ribbon band.
	 * @see #AbstractRibbonBand(String, ResizableIcon, ActionListener,
	 *      AbstractBandControlPanel)
	 * @see #setExpandActionListener(ActionListener)
	 */
	public ActionListener getExpandActionListener() {
		return this.expandActionListener;
	}

	/**
	 * Sets the specified action listener to be activated when the user clicks
	 * the expand button on this ribbon band. Passing <code>null</code> will
	 * remove the expand button from this ribbon band.
	 * 
	 * @param expandActionListener
	 *            Expand action listener for this ribbon band.
	 * @see #getExpandActionListener()
	 */
	public void setExpandActionListener(ActionListener expandActionListener) {
		ActionListener old = this.expandActionListener;
		this.expandActionListener = expandActionListener;
		this.firePropertyChange("expandActionListener", old,
				this.expandActionListener);
	}

	/**
	 * Returns the control panel of <code>this</code> ribbon band. The result
	 * may be <code>null</code>.
	 * 
	 * @return Control panel of <code>this</code> ribbon band.
	 * @see #AbstractRibbonBand(String, ResizableIcon, ActionListener,
	 *      AbstractBandControlPanel)
	 * @see #setControlPanel(AbstractBandControlPanel)
	 */
	public T getControlPanel() {
		return this.controlPanel;
	}

	/**
	 * Sets the control panel of <code>this</code> ribbon band. The parameter
	 * may be <code>null</code>. This method is for internal use only.
	 * 
	 * @param controlPanel
	 *            The new control panel for <code>this</code> ribbon band. May
	 *            be <code>null</code>.
	 * @see #AbstractRibbonBand(String, ResizableIcon, ActionListener,
	 *      AbstractBandControlPanel)
	 * @see #getControlPanel()
	 */
	public void setControlPanel(T controlPanel) {
		if (controlPanel == null) {
			this.remove(this.controlPanel);
		} else {
			this.add(controlPanel);
			controlPanel.applyComponentOrientation(this
					.getComponentOrientation());
		}
		this.controlPanel = controlPanel;
	}

	/**
	 * Returns the ribbon band shown in a popup panel when this ribbon band is
	 * in a collapsed state. This method is for internal use only and should not
	 * be called by the application code.
	 * 
	 * @return The ribbon band shown in a popup panel when this ribbon band is
	 *         in a collapsed state.
	 * @see #setPopupRibbonBand(AbstractRibbonBand)
	 */
	public AbstractRibbonBand getPopupRibbonBand() {
		return this.popupRibbonBand;
	}

	/**
	 * Sets the specified parameter to be the ribbon band shown in a popup panel
	 * when this ribbon band is in a collapsed state. This method is for
	 * internal use only and should not be called by the application code.
	 * 
	 * @param popupRibbonBand
	 *            The ribbon band to be shown in a popup panel when this ribbon
	 *            band is in a collapsed state.
	 */
	public void setPopupRibbonBand(AbstractRibbonBand popupRibbonBand) {
		this.popupRibbonBand = popupRibbonBand;
		if (this.popupRibbonBand != null) {
			popupRibbonBand.applyComponentOrientation(this
					.getComponentOrientation());
		}
	}

	/**
	 * Returns the current resize policy of this ribbon band.
	 * 
	 * @return The current resize policy of this ribbon band.
	 */
	public RibbonBandResizePolicy getCurrentResizePolicy() {
		return currResizePolicy;
	}

	/**
	 * Sets the specified parameter to be the current resize policy of this
	 * ribbon band. This method is for internal use only and should not be
	 * called by the application code.
	 * 
	 * @param resizePolicy
	 *            The new resize policy for this ribbon band.
	 * @see #getCurrentResizePolicy()
	 * @see #getResizePolicies()
	 */
	public void setCurrentResizePolicy(RibbonBandResizePolicy resizePolicy) {
		this.currResizePolicy = resizePolicy;
	}

	/**
	 * Returns an unmodifiable list of available resize policies of this ribbon
	 * band.
	 * 
	 * @return An unmodifiable list of available resize policies of this ribbon
	 *         band.
	 */
	public List<RibbonBandResizePolicy> getResizePolicies() {
		return Collections.unmodifiableList(this.resizePolicies);
	}

	/**
	 * Sets the specified parameter as the available resize policies of this
	 * ribbon band. The order of the resize policies in this list is important.
	 * The first entry in the list must be the most permissive policies that
	 * returns the largest value from its
	 * {@link RibbonBandResizePolicy#getPreferredWidth(int, int)}. Each
	 * successive entry in the list must return the value smaller than its
	 * predecessors. If {@link IconRibbonBandResizePolicy} is in the list, it
	 * <strong>must</strong> be the last entry.
	 * 
	 * @param resizePolicies
	 *            The new available resize policies of this ribbon band.
	 */
	public void setResizePolicies(List<RibbonBandResizePolicy> resizePolicies) {
		this.resizePolicies = Collections.unmodifiableList(resizePolicies);
		if (this.ribbonTask != null) {
			FlamingoUtilities.checkResizePoliciesConsistency(this);
		}
	}

	/**
	 * Returns the key tip for the expand button of this ribbon band.
	 * 
	 * @return The key tip for the expand button of this ribbon band.
	 * @see #setExpandButtonKeyTip(String)
	 */
	public String getExpandButtonKeyTip() {
		return this.expandButtonKeyTip;
	}

	/**
	 * Changes the key tip for the expand button of this ribbon band. Fires an
	 * <code>expandButtonKeyTip</code> property change event.
	 * 
	 * @param expandButtonKeyTip
	 *            The new key tip for the expand button of this ribbon band.
	 * @see #getExpandButtonKeyTip()
	 */
	public void setExpandButtonKeyTip(String expandButtonKeyTip) {
		String old = this.expandButtonKeyTip;
		this.expandButtonKeyTip = expandButtonKeyTip;
		this.firePropertyChange("expandButtonKeyTip", old,
				this.expandButtonKeyTip);
	}

	/**
	 * Returns the rich tooltip for the expand button of this ribbon band.
	 * 
	 * @return The rich tooltip for the expand button of this ribbon band.
	 * @see #setExpandButtonRichTooltip(RichTooltip)
	 */
	public RichTooltip getExpandButtonRichTooltip() {
		return this.expandButtonRichTooltip;
	}

	/**
	 * Changes the rich tooltip for the expand button of this ribbon band. Fires
	 * an <code>expandButtonRichTooltip</code> property change event.
	 * 
	 * @param expandButtonRichTooltip
	 *            The new rich tooltip for the expand button of this ribbon
	 *            band.
	 * @see #getExpandButtonRichTooltip()
	 */
	public void setExpandButtonRichTooltip(RichTooltip expandButtonRichTooltip) {
		RichTooltip old = this.expandButtonRichTooltip;
		this.expandButtonRichTooltip = expandButtonRichTooltip;
		this.firePropertyChange("expandButtonRichTooltip", old,
				this.expandButtonRichTooltip);
	}

	/**
	 * Returns the key tip for the collapsed button which is shown when there is
	 * not enough horizontal space to show the ribbon band content under the
	 * most restrictive resize policy.
	 * 
	 * @return The key tip for the collapsed button of this ribbon band.
	 * @see #setCollapsedStateKeyTip(String)
	 */
	public String getCollapsedStateKeyTip() {
		return this.collapsedStateKeyTip;
	}

	/**
	 * Changes the key tip for the collapsed button which is shown when there is
	 * not enough horizontal space to show the ribbon band content under the
	 * most restrictive resize policy. Fires a <code>collapsedStateKeyTip</code>
	 * property change event.
	 * 
	 * @param collapsedStateKeyTip
	 *            The new key tip for the collapsed button of this ribbon band.
	 * @see #getCollapsedStateKeyTip()
	 */
	public void setCollapsedStateKeyTip(String collapsedStateKeyTip) {
		String old = this.collapsedStateKeyTip;
		this.collapsedStateKeyTip = collapsedStateKeyTip;
		this.firePropertyChange("collapsedStateKeyTip", old,
				this.collapsedStateKeyTip);
	}

	/**
	 * Associates this ribbon band with the specified ribbon task.
	 * 
	 * @param ribbonTask
	 *            Ribbon task.
	 * @throws IllegalArgumentException
	 *             When this ribbon band has already been associated with a
	 *             ribbon task.
	 */
	void setRibbonTask(RibbonTask ribbonTask) {
		if (this.ribbonTask != null) {
			throw new IllegalArgumentException(
					"Ribbon band cannot be added to more than one ribbon task");
		}
		this.ribbonTask = ribbonTask;
		FlamingoUtilities.checkResizePoliciesConsistency(this);
	}
}
