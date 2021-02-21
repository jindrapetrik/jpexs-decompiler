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

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.ribbon.*;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies.*;

/**
 * Defines the resize policies for the {@link JRibbonBand}s and
 * {@link JFlowRibbonBand}s.
 * 
 * <p>
 * The resize policy defines a single visual state of the given ribbon band. For
 * every control in the specific ribbon band (command button, gallery etc), the
 * resize policy defines what is its display state.
 * </p>
 * 
 * <p>
 * The resize policies are installed with
 * {@link AbstractRibbonBand#setResizePolicies(java.util.List)} API. The order
 * of the resize policies in this list is important. The first entry in the list
 * must be the most permissive policies that returns the largest value from its
 * {@link #getPreferredWidth(int, int)}. Each successive entry in the list must
 * return the value smaller than its predecessors. The last entry
 * <strong>must</strong> be {@link IconRibbonBandResizePolicy}.
 * </p>
 * 
 * <p>
 * As the ribbon horizontal size is changed (by the user resizing the
 * application window), the ribbon task resize sequencing policy set by
 * {@link RibbonTask#setResizeSequencingPolicy(RibbonBandResizeSequencingPolicy)}
 * determines the order of ribbon bands to shrink / expand. See more details in
 * the documentation of the {@link RibbonBandResizeSequencingPolicy}.
 * </p>
 * 
 * <p>
 * The {@link CoreRibbonResizePolicies} provides a number of built in resize
 * policies that respect the application element priorities passed to
 * {@link JRibbonBand#addCommandButton(org.pushingpixels.flamingo.api.common.AbstractCommandButton, org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority)}
 * and
 * {@link JRibbonBand#addRibbonGallery(String, java.util.List, java.util.Map, int, int, org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority)}
 * APIs. There are three types of built in resize policies:
 * </p>
 * 
 * <ul>
 * <li>Resize policies for the {@link JFlowRibbonBand}s. The {@link FlowTwoRows}
 * and {@link FlowThreeRows} allow placing the flow ribbon band content in two
 * and three rows respectively.</li>
 * <li>Resize policies for the {@link JRibbonBand}s. The
 * {@link BaseCoreRibbonBandResizePolicy} is the base class for these policies.
 * These policies respect the {@link RibbonElementPriority} associated on
 * command buttons and ribbon galleries in {@link #getPreferredWidth(int, int)}
 * and {@link #install(int, int)}. While {@link #install(int, int)} call on a
 * {@link JFlowRibbonBand} only changes the bounds of the flow components, this
 * call on a {@link JRibbonBand} can also change the display state of the
 * command buttons (with
 * {@link AbstractCommandButton#setDisplayState(org.pushingpixels.flamingo.api.common.CommandButtonDisplayState)}
 * ) and the number of visible buttons in the ribbon galleries.</li>
 * <li>The collapsed policy that replaces the entire content of the ribbon band
 * with a single popup button. This is done when there is not enough horizontal
 * space to show the content of the ribbon band under the most restrictive
 * resize policy. Activating the popup button will show the original content
 * under the most permissive resize policy in a popup. This policy is
 * implemented in the {@link IconRibbonBandResizePolicy}.</li>
 * </ul>
 * 
 * <p>
 * In addition to the specific resize policies, the
 * {@link CoreRibbonResizePolicies} provides three core resize policies lists
 * for {@link JRibbonBand}s:
 * </p>
 * 
 * <ul>
 * <li>{@link CoreRibbonResizePolicies#getCorePoliciesPermissive(JRibbonBand)}
 * returns a list that starts with a resize policy that shows all command
 * buttons in the {@link CommandButtonDisplayState#BIG} and ribbon galleries
 * with the largest number of visible buttons, fully utilizing the available
 * screen space.</li>
 * <li>{@link CoreRibbonResizePolicies#getCorePoliciesRestrictive(JRibbonBand)}
 * returns a list that starts with a resize policy that respects the associated
 * ribbon element priority set on the specific components.</li>
 * <li> {@link CoreRibbonResizePolicies#getCorePoliciesNone(JRibbonBand)} returns
 * a list that only has a <code>mirror</code> resize policy that respects the
 * associated ribbon element priority set on the specific components.</li>
 * </ul>
 * 
 * <p>
 * Note that as mentioned above, all the three lists above have the
 * <code>collapsed</code> policy as their last element.
 * </p>
 * 
 * <p>
 * In addition, the
 * {@link CoreRibbonResizePolicies#getCoreFlowPoliciesRestrictive(JFlowRibbonBand, int)}
 * returns a restrictive resize policy for {@link JFlowRibbonBand}s. The list
 * starts with the two-row policy, goes to the three-row policy and then finally
 * to the collapsed policy.
 * </p>
 * 
 * @author Kirill Grouchnikov
 */
public interface RibbonBandResizePolicy {
	/**
	 * Returns the preferred width of the associated ribbon band under the
	 * specified dimensions.
	 * 
	 * @param availableHeight
	 *            The height available for the associated ribbon band.
	 * @param gap
	 *            The inter-component gap.
	 * @return The preferred width of the associated ribbon band under the
	 *         specified dimensions.
	 */
	public int getPreferredWidth(int availableHeight, int gap);

	/**
	 * Installs this resize policy on the associated ribbon band. For
	 * {@link JFlowRibbonBand}s only changes the bounds of the flow components.
	 * For {@link JRibbonBand}s can also change the display state of the command
	 * buttons (with
	 * {@link AbstractCommandButton#setDisplayState(org.pushingpixels.flamingo.api.common.CommandButtonDisplayState)}
	 * ) and the number of visible buttons in the ribbon galleries. Note that
	 * this method is for internal use only and should not be called by the
	 * application code.
	 * 
	 * @param availableHeight
	 *            The height available for the associated ribbon band.
	 * @param gap
	 *            The inter-component gap.
	 */
	public void install(int availableHeight, int gap);
}
