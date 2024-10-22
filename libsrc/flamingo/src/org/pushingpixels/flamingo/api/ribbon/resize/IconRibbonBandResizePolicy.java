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
package org.pushingpixels.flamingo.api.ribbon.resize;

import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;
import org.pushingpixels.flamingo.internal.ui.ribbon.RibbonBandUI;

/**
 * Special resize policy that is used for collapsed ribbon bands. When there is
 * not enough horizontal space to show the ribbon band content under the most
 * restrictive {@link RibbonBandResizePolicy}, the entire ribbon band content is
 * replaced by a single popup button. Activating the popup button will show the
 * original content under the most permissive resize policy in a popup.
 * 
 * <p>
 * An instance of this policy <strong>must</strong> appear exactly once in the
 * list passed to {@link AbstractRibbonBand#setResizePolicies(java.util.List)},
 * and it <strong>must</strong> be the last entry in that list.
 * </p>
 * 
 * @author Kirill Grouchnikov
 */
public class IconRibbonBandResizePolicy extends
		BaseRibbonBandResizePolicy<AbstractBandControlPanel> {
	/**
	 * Creates a new collapsed resize policy.
	 * 
	 * @param controlPanel
	 *            The control panel of the associated ribbon band.
	 */
	public IconRibbonBandResizePolicy(AbstractBandControlPanel controlPanel) {
		super(controlPanel);
	}

	@Override
	public int getPreferredWidth(int availableHeight, int gap) {
		AbstractRibbonBand ribbonBand = this.controlPanel.getRibbonBand();
		RibbonBandUI ui = ribbonBand.getUI();
		return ui.getPreferredCollapsedWidth();
	}

	@Override
	public void install(int availableHeight, int gap) {
	}
}
