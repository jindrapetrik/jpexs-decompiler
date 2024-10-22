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
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizeSequencingPolicies.CollapseFromLast;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizeSequencingPolicies.RoundRobin;

/**
 * Defines the resize sequencing policies for {@link RibbonTask}s.
 * 
 * <p>
 * The resize sequencing policy defines which ribbon band will be chosen next
 * when the ribbon is shrunk / expanded. It is installed with the
 * {@link RibbonTask#setResizeSequencingPolicy(RibbonBandResizeSequencingPolicy)}
 * .
 * </p>
 * 
 * <p>
 * The {@link CoreRibbonResizeSequencingPolicies} provides two built in resize
 * sequencing policies:
 * </p>
 * 
 * <ul>
 * <li>{@link RoundRobin} under which the ribbon bands are being collapsed in a
 * cyclic fashion, distributing the collapsed pixels between the different
 * bands.</li>
 * <li>{@link CollapseFromLast} under which the ribbon bands are being collapsed
 * from right to left.</li>
 * </ul>
 * 
 * @author Kirill Grouchnikov
 */
public interface RibbonBandResizeSequencingPolicy {
	/**
	 * Resets this policy. Note that this method is for internal use only and
	 * should not be called by the application code.
	 */
	public void reset();

	/**
	 * Returns the next ribbon band for collapse.
	 * 
	 * @return The next ribbon band for collapse.
	 */
	public AbstractRibbonBand next();
}
