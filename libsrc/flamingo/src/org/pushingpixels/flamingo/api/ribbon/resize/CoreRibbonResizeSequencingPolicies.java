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

import java.util.List;

import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

/**
 * The core resize sequencing policies. Provides the following:
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
public class CoreRibbonResizeSequencingPolicies {
	/**
	 * The round robin resize sequencing policy. Under this policy the ribbon
	 * bands are being collapsed in a cyclic fashion, distributing the collapsed
	 * pixels between the different bands.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class RoundRobin extends BaseRibbonBandResizeSequencingPolicy {
		/**
		 * The index of the next ribbon task for collapsing.
		 */
		int nextIndex;

		/**
		 * Creates a new round robin resize sequencing policy for the specified
		 * task.
		 * 
		 * @param ribbonTask
		 *            Ribbon task.
		 */
		public RoundRobin(RibbonTask ribbonTask) {
			super(ribbonTask);
		}

		@Override
		public void reset() {
			this.nextIndex = this.ribbonTask.getBandCount() - 1;
		}

		@Override
		public AbstractRibbonBand next() {
			AbstractRibbonBand result = this.ribbonTask.getBand(this.nextIndex);
			this.nextIndex--;
			if (this.nextIndex < 0)
				this.nextIndex = this.ribbonTask.getBandCount() - 1;
			return result;
		}
	}

	/**
	 * The collapse from last resize sequencing policy. Under this policy the
	 * ribbon bands are being collapsed from right to left.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class CollapseFromLast extends
			BaseRibbonBandResizeSequencingPolicy {
		/**
		 * The index of the next ribbon task for collapsing.
		 */
		int nextIndex;

		/**
		 * Creates a new collapse from last resize sequencing policy for the
		 * specified task.
		 * 
		 * @param ribbonTask
		 *            Ribbon task.
		 */
		public CollapseFromLast(RibbonTask ribbonTask) {
			super(ribbonTask);
		}

		@Override
		public void reset() {
			this.nextIndex = this.ribbonTask.getBandCount() - 1;
		}

		@Override
		public AbstractRibbonBand next() {
			AbstractRibbonBand result = this.ribbonTask.getBand(this.nextIndex);

			// check whether the current resize policy on the returned ribbon
			// band is the last
			List<RibbonBandResizePolicy> resizePolicies = result
					.getResizePolicies();
			if (result.getCurrentResizePolicy() == resizePolicies
					.get(resizePolicies.size() - 1)) {
				this.nextIndex--;
				if (this.nextIndex < 0)
					this.nextIndex = 0;
			}
			return result;
		}
	}
}
