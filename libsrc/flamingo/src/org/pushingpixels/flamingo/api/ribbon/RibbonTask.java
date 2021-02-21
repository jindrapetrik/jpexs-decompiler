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

import java.util.*;

import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizeSequencingPolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizeSequencingPolicy;

/**
 * Single ribbon task in {@link JRibbon}. This is a logical entity that groups
 * {@link AbstractRibbonBand} components.
 * 
 * @author Kirill Grouchnikov
 */
public class RibbonTask {
	/**
	 * The associated ribbon.
	 */
	private JRibbon ribbon;

	/**
	 * List of all bands.
	 */
	private ArrayList<AbstractRibbonBand<?>> bands;

	/**
	 * The title of this task.
	 */
	private String title;

	/**
	 * The group that this band belongs to. For regular ribbon bands this field
	 * is <code>null</code>.
	 */
	private RibbonContextualTaskGroup contextualGroup;

	/**
	 * The current resize sequencing policy.
	 */
	private RibbonBandResizeSequencingPolicy resizeSequencingPolicy;

	/**
	 * The key tip for the task button of this task.
	 */
	private String keyTip;

	/**
	 * Creates a ribbon task that contains the specified bands.
	 * 
	 * @param title
	 *            Ribbon task title.
	 * @param bands
	 *            Bands to add to the ribbon task.
	 */
	public RibbonTask(String title, AbstractRibbonBand<?>... bands) {
		if ((bands == null) || (bands.length == 0)) {
			throw new IllegalArgumentException("Cannot have empty ribbon task");
		}
		this.title = title;
		this.bands = new ArrayList<AbstractRibbonBand<?>>();
		for (AbstractRibbonBand<?> band : bands) {
			band.setRibbonTask(this);
			this.bands.add(band);
		}
		this.resizeSequencingPolicy = new CoreRibbonResizeSequencingPolicies.RoundRobin(
				this);
	}

	/**
	 * Returns the number of bands in <code>this</code> task.
	 * 
	 * @return Number of bands in <code>this</code> task.
	 * @see #getBand(int)
	 * @see #getBands()
	 */
	public int getBandCount() {
		return this.bands.size();
	}

	/**
	 * Returns band at the specified index from <code>this</code> task.
	 * 
	 * @param index
	 *            Band index.
	 * @return Band at the specified index.
	 * @see #getBandCount()
	 * @see #getBands()
	 */
	public AbstractRibbonBand<?> getBand(int index) {
		return this.bands.get(index);
	}

	/**
	 * Returns the title of this task.
	 * 
	 * @return The title of this task.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Sets the contextual task group for this ribbon task. This method is
	 * package protected and is for internal use only.
	 * 
	 * @param contextualGroup
	 *            The contextual task group for this ribbon task.
	 * @see #getContextualGroup()
	 */
	void setContextualGroup(RibbonContextualTaskGroup contextualGroup) {
		if (this.contextualGroup != null) {
			throw new IllegalStateException(
					"The task already belongs to another contextual task group");
		}
		this.contextualGroup = contextualGroup;
	}

	/**
	 * Returns the contextual task group for this ribbon task. Will return
	 * <code>null</code> for general ribbon tasks.
	 * 
	 * @return The contextual task group for this ribbon task.
	 */
	public RibbonContextualTaskGroup getContextualGroup() {
		return this.contextualGroup;
	}

	/**
	 * Returns an unmodifiable view on the ribbon bands of this task.
	 * 
	 * @return Unmodifiable view on the ribbon bands of this task.
	 * @see #getBandCount()
	 * @see #getBand(int)
	 */
	public List<AbstractRibbonBand<?>> getBands() {
		return Collections.unmodifiableList(this.bands);
	}

	/**
	 * Changes the title of this ribbon task.
	 * 
	 * @param title
	 *            The new title for this ribbon task.
	 */
	public void setTitle(String title) {
		this.title = title;
		if (this.ribbon != null)
			this.ribbon.fireStateChanged();
	}

	/**
	 * Associates this ribbon task with the specified ribbon. This method is
	 * package protected and is for internal use only.
	 * 
	 * @param ribbon
	 *            The associated ribbon.
	 */
	void setRibbon(JRibbon ribbon) {
		if (this.ribbon != null) {
			throw new IllegalStateException(
					"The task already belongs to another ribbon");
		}
		this.ribbon = ribbon;
	}

	/**
	 * Returns the current resize sequencing policy of this ribbon task.
	 * 
	 * @return The current resize sequencing policy of this ribbon task.
	 * @see #setResizeSequencingPolicy(RibbonBandResizeSequencingPolicy)
	 */
	public RibbonBandResizeSequencingPolicy getResizeSequencingPolicy() {
		return this.resizeSequencingPolicy;
	}

	/**
	 * Sets the specified parameter as the new resize sequencing policy of this
	 * ribbon task.
	 * 
	 * @param resizeSequencingPolicy
	 *            The new resize sequencing policy of this ribbon task.
	 * @see #getResizeSequencingPolicy()
	 */
	public void setResizeSequencingPolicy(
			RibbonBandResizeSequencingPolicy resizeSequencingPolicy) {
		this.resizeSequencingPolicy = resizeSequencingPolicy;
	}

	/**
	 * Returns the key tip for the task button of this task.
	 * 
	 * @return The key tip for the task button of this task.
	 * @see #setKeyTip(String)
	 */
	public String getKeyTip() {
		return this.keyTip;
	}

	/**
	 * Sets the specified parameter to be the new key tip for the task button of
	 * this task.
	 * 
	 * @param keyTip
	 *            The new key tip for the task button of this task.
	 */
	public void setKeyTip(String keyTip) {
		this.keyTip = keyTip;
	}
}
