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

import java.awt.Insets;
import java.util.*;

import javax.swing.JComponent;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.ribbon.*;
import org.pushingpixels.flamingo.internal.ui.ribbon.*;

/**
 * The core resize policies. Provides a number of built in resize policies that
 * respect the application element priorities passed to
 * {@link JRibbonBand#addCommandButton(org.pushingpixels.flamingo.api.common.AbstractCommandButton, org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority)}
 * and
 * {@link JRibbonBand#addRibbonGallery(String, java.util.List, java.util.Map, int, int, org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority)}
 * APIs. There are three types of built in resize policies: </p>
 * 
 * <ul>
 * <li>Resize policies for the {@link JFlowRibbonBand}s. The {@link FlowTwoRows}
 * and {@link FlowThreeRows} allow placing the flow ribbon band content in two
 * and three rows respectively.</li>
 * <li>Resize policies for the {@link JRibbonBand}s. The
 * {@link BaseCoreRibbonBandResizePolicy} is the base class for these policies.
 * These policies respect the {@link RibbonElementPriority} associated on
 * command buttons and ribbon galleries in
 * {@link RibbonBandResizePolicy#getPreferredWidth(int, int)} and
 * {@link RibbonBandResizePolicy#install(int, int)}. While
 * {@link RibbonBandResizePolicy#install(int, int)} call on a
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
 * In addition to the specific resize policies, this class provides three core
 * resize policies lists for {@link JRibbonBand}s:
 * </p>
 * 
 * <ul>
 * <li>{@link #getCorePoliciesPermissive(JRibbonBand)} returns a list that
 * starts with a resize policy that shows all command buttons in the
 * {@link CommandButtonDisplayState#BIG} and ribbon galleries with the largest
 * number of visible buttons, fully utilizing the available screen space.</li>
 * <li>{@link #getCorePoliciesRestrictive(JRibbonBand)} returns a list that
 * starts with a resize policy that respects the associated ribbon element
 * priority set on the specific components.</li>
 * <li> {@link #getCorePoliciesNone(JRibbonBand)} returns a list that only has a
 * <code>mirror</code> resize policy that respects the associated ribbon element
 * priority set on the specific components.</li>
 * </ul>
 * 
 * <p>
 * Note that as mentioned above, all the three lists above have the
 * <code>collapsed</code> policy as their last element.
 * </p>
 * 
 * <p>
 * In addition, the
 * {@link #getCoreFlowPoliciesRestrictive(JFlowRibbonBand, int)} returns a
 * restrictive resize policy for {@link JFlowRibbonBand}s. The list starts with
 * the two-row policy, goes to the three-row policy and then finally to the
 * collapsed policy.
 * </p>
 * 
 * @author Kirill Grouchnikov
 */
public class CoreRibbonResizePolicies {
	/**
	 * Maps the element priority associated with a ribbon band component to the
	 * element priority assigned by the specific resize policy.
	 * 
	 * @author Kirill Grouchnikov
	 */
	static interface Mapping {
		/**
		 * Maps the element priority associated with a ribbon band component to
		 * the element priority assigned by the specific resize policy.
		 * 
		 * @param priority
		 *            The element priority associated with a ribbon band
		 *            component
		 * @return The element priority assigned by the specific resize policy.
		 */
		RibbonElementPriority map(RibbonElementPriority priority);
	}

	/**
	 * Returns a list that starts with a resize policy that shows all command
	 * buttons in the {@link CommandButtonDisplayState#BIG} and ribbon galleries
	 * with the largest number of visible buttons. The last entry is the
	 * {@link IconRibbonBandResizePolicy}.
	 * 
	 * @param ribbonBand
	 *            Ribbon band.
	 * @return The permissive list of core ribbon band resize policies.
	 */
	public static List<RibbonBandResizePolicy> getCorePoliciesPermissive(
			JRibbonBand ribbonBand) {
		List<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();
		result.add(new CoreRibbonResizePolicies.None(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.Low2Mid(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.Mid2Mid(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.Mirror(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.Mid2Low(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.High2Mid(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.High2Low(ribbonBand
				.getControlPanel()));
		result
				.add(new IconRibbonBandResizePolicy(ribbonBand
						.getControlPanel()));
		return result;
	}

	/**
	 * Returns a list that starts with a resize policy that respects the
	 * associated ribbon element priority set on the specific components. The
	 * last entry is the {@link IconRibbonBandResizePolicy}.
	 * 
	 * @param ribbonBand
	 *            Ribbon band.
	 * @return The restrictive list of core ribbon band resize policies.
	 */
	public static List<RibbonBandResizePolicy> getCorePoliciesRestrictive(
			JRibbonBand ribbonBand) {
		List<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();
		result.add(new CoreRibbonResizePolicies.Mirror(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.Mid2Low(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.High2Mid(ribbonBand
				.getControlPanel()));
		result.add(new CoreRibbonResizePolicies.High2Low(ribbonBand
				.getControlPanel()));
		result
				.add(new IconRibbonBandResizePolicy(ribbonBand
						.getControlPanel()));
		return result;
	}

	/**
	 * Returns a list that only has a <code>mirror</code> resize policy that
	 * respects the associated ribbon element priority set on the specific
	 * components. The last entry is the {@link IconRibbonBandResizePolicy}.
	 * 
	 * @param ribbonBand
	 *            Ribbon band.
	 * @return The mirror list of core ribbon band resize policies.
	 */
	public static List<RibbonBandResizePolicy> getCorePoliciesNone(
			JRibbonBand ribbonBand) {
		List<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();
		result.add(new CoreRibbonResizePolicies.Mirror(ribbonBand
				.getControlPanel()));
		result
				.add(new IconRibbonBandResizePolicy(ribbonBand
						.getControlPanel()));
		return result;
	}

	/**
	 * The base class for mapping-based core resize policies.
	 * 
	 * @author Kirill Grouchnikov
	 */
	protected static abstract class BaseCoreRibbonBandResizePolicy extends
			BaseRibbonBandResizePolicy<JBandControlPanel> {
		/**
		 * The element priority mapping.
		 */
		protected Mapping mapping;

		/**
		 * Creates a new resize policy.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 * @param mapping
		 *            The element priority mapping.
		 */
		protected BaseCoreRibbonBandResizePolicy(
				JBandControlPanel controlPanel, Mapping mapping) {
			super(controlPanel);
			this.mapping = mapping;
		}

		/**
		 * Returns the total width of the specified buttons.
		 * 
		 * @param gap
		 *            Inter component gap.
		 * @param bigButtons
		 *            List of buttons in big display state.
		 * @param mediumButtons
		 *            List of buttons in medium display state.
		 * @param smallButtons
		 *            List of buttons in small display state.
		 * @return Total width of the specified buttons.
		 */
		protected int getWidth(int gap,
				java.util.List<AbstractCommandButton> bigButtons,
				java.util.List<AbstractCommandButton> mediumButtons,
				java.util.List<AbstractCommandButton> smallButtons) {
			int result = 0;
			boolean hasLeadingContent = false;
			for (AbstractCommandButton top : bigButtons) {
				if (hasLeadingContent) {
					result += gap;
				}
				result += getPreferredWidth(top, RibbonElementPriority.TOP);
				hasLeadingContent = true;
			}

			int medSize = mediumButtons.size();
			if (medSize > 0) {
				// try to move buttons from low to med to make
				// threesomes.
				while (((mediumButtons.size() % 3) != 0)
						&& (smallButtons.size() > 0)) {
					AbstractCommandButton low = smallButtons.remove(0);
					mediumButtons.add(low);
				}
			}

			// at this point, mediumButtons list contains either
			// threesomes, or there are no buttons in lowButtons.
			int index3 = 0;
			int maxWidth3 = 0;
			for (AbstractCommandButton medium : mediumButtons) {
				int medWidth = getPreferredWidth(medium,
						RibbonElementPriority.MEDIUM);
				maxWidth3 = Math.max(maxWidth3, medWidth);
				index3++;

				if (index3 == 3) {
					// last button in threesome
					index3 = 0;
					if (hasLeadingContent) {
						result += gap;
					}
					result += maxWidth3;
					hasLeadingContent = true;
					maxWidth3 = 0;
				}
			}
			// at this point, maxWidth3 may be non-zero. We can safely
			// add it, since in this case there will be no buttons
			// left in mediumButtons
			if (maxWidth3 > 0) {
				if (hasLeadingContent) {
					result += gap;
				}
				result += maxWidth3;
				hasLeadingContent = true;
			}

			index3 = 0;
			maxWidth3 = 0;
			for (AbstractCommandButton low : smallButtons) {
				int lowWidth = getPreferredWidth(low, RibbonElementPriority.LOW);
				maxWidth3 = Math.max(maxWidth3, lowWidth);
				index3++;

				if (index3 == 3) {
					// last button in threesome
					index3 = 0;
					if (hasLeadingContent) {
						result += gap;
					}
					result += maxWidth3;
					hasLeadingContent = true;
					maxWidth3 = 0;
				}
			}
			// at this point, maxWidth3 may be non-zero. We can safely
			// add it, since in this case there will be no buttons left
			if (maxWidth3 > 0) {
				if (hasLeadingContent) {
					result += gap;
				}
				result += maxWidth3;
				hasLeadingContent = true;
			}

			return result;
		}

		/**
		 * Returns the preferred width of the specified command button under the
		 * specified display priority.
		 * 
		 * @param button
		 *            Command button.
		 * @param buttonDisplayPriority
		 *            Button display priority.
		 * @return The preferred width of the specified command button under the
		 *         specified display priority.
		 */
		private int getPreferredWidth(AbstractCommandButton button,
				RibbonElementPriority buttonDisplayPriority) {
			CommandButtonDisplayState displayState = null;
			switch (buttonDisplayPriority) {
			case TOP:
				displayState = CommandButtonDisplayState.BIG;
				break;
			case MEDIUM:
				displayState = CommandButtonDisplayState.MEDIUM;
				break;
			case LOW:
				displayState = CommandButtonDisplayState.SMALL;
				break;
			}
			return displayState.createLayoutManager(button).getPreferredSize(
					button).width;
		}

		@Override
		public int getPreferredWidth(int availableHeight, int gap) {
			int result = 0;

			Insets ins = this.controlPanel.getInsets();

			for (JBandControlPanel.ControlPanelGroup controlPanelGroup : this.controlPanel
					.getControlPanelGroups()) {
				boolean isCoreContent = controlPanelGroup.isCoreContent();
				if (isCoreContent) {
					List<JRibbonComponent> ribbonComps = controlPanelGroup
							.getRibbonComps();
					Map<JRibbonComponent, Integer> ribbonCompRowSpans = controlPanelGroup
							.getRibbonCompsRowSpans();
					// if a group has a title, then the core components in that
					// group start from the second row
					int startRowIndex = (controlPanelGroup.getGroupTitle() == null) ? 0
							: 1;
					int rowIndex = startRowIndex;
					int maxWidthInCurrColumn = 0;
					for (int i = 0; i < ribbonComps.size(); i++) {
						JRibbonComponent ribbonComp = ribbonComps.get(i);
						int rowSpan = ribbonCompRowSpans.get(ribbonComp);

						// do we need to start a new column?
						int nextRowIndex = rowIndex + rowSpan;
						if (nextRowIndex > 3) {
							result += maxWidthInCurrColumn;
							result += gap;
							maxWidthInCurrColumn = 0;
							rowIndex = startRowIndex;
						}

						RibbonElementPriority targetPriority = RibbonElementPriority.TOP;
						if (ribbonComp.isResizingAware()) {
							targetPriority = this.mapping
									.map(RibbonElementPriority.TOP);
						}
						int prefWidth = ribbonComp.getUI().getPreferredSize(
								targetPriority).width;
						maxWidthInCurrColumn = Math.max(maxWidthInCurrColumn,
								prefWidth);
						rowIndex += rowSpan;
					}
					if ((rowIndex > 0) && (rowIndex <= 3)) {
						result += maxWidthInCurrColumn;
						result += gap;
					}
				} else {
					int galleryAvailableHeight = availableHeight - ins.top
							- ins.bottom;
					// ribbon galleries
					result += this.getPreferredGalleryWidth(controlPanelGroup,
							galleryAvailableHeight, gap);

					// ribbon buttons
					result += this.getPreferredButtonWidth(controlPanelGroup,
							gap);
				}

				result += gap * 3 / 2;
			}
			// no gap after the last group
			result -= gap * 3 / 2;

			// control panel insets
			result += ins.left + ins.right;
			result += gap;

			return result;
		}

		/**
		 * Returns the preferred width of all the buttons in the specified
		 * control panel group.
		 * 
		 * @param controlPanelGroup
		 *            A single control panel group in the associated ribbon
		 *            band.
		 * @param gap
		 *            Inter component gap.
		 * @return The preferred width of all the buttons in the specified
		 *         control panel group.
		 */
		protected int getPreferredButtonWidth(
				JBandControlPanel.ControlPanelGroup controlPanelGroup, int gap) {
			Map<RibbonElementPriority, List<AbstractCommandButton>> mapped = new HashMap<RibbonElementPriority, List<AbstractCommandButton>>();
			for (RibbonElementPriority rep : RibbonElementPriority.values()) {
				mapped.put(rep, new ArrayList<AbstractCommandButton>());
			}

			for (RibbonElementPriority elementPriority : RibbonElementPriority
					.values()) {
				// map the priority
				RibbonElementPriority mappedPriority = mapping
						.map(elementPriority);
				for (AbstractCommandButton button : controlPanelGroup
						.getRibbonButtons(elementPriority)) {
					// and add the button to a list based on the mapped priority
					mapped.get(mappedPriority).add(button);
				}
			}

			// at this point, the lists in the 'mapped' contain the buttons
			// grouped by the priority computed by the resize policy.
			return this.getWidth(gap, mapped.get(RibbonElementPriority.TOP),
					mapped.get(RibbonElementPriority.MEDIUM), mapped
							.get(RibbonElementPriority.LOW));

		}

		/**
		 * Returns the preferred width of all the ribbon galleries in the
		 * specified control panel group.
		 * 
		 * @param controlPanelGroup
		 *            A single control panel group in the associated ribbon
		 *            band.
		 * @param galleryAvailableHeight
		 *            Available height for the ribbon galleries.
		 * @param gap
		 *            Inter component gap.
		 * @return The preferred width of all the ribbon galleries in the
		 *         specified control panel group.
		 */
		private int getPreferredGalleryWidth(
				JBandControlPanel.ControlPanelGroup controlPanelGroup,
				int galleryAvailableHeight, int gap) {
			int result = 0;
			for (RibbonElementPriority elementPriority : RibbonElementPriority
					.values()) {
				// map the priority
				RibbonElementPriority mappedPriority = mapping
						.map(elementPriority);
				// go over all galleries registered with the specific priority
				for (JRibbonGallery gallery : controlPanelGroup
						.getRibbonGalleries(elementPriority))
					// and take the preferred width under the mapped priority
					result += (gallery.getPreferredWidth(mappedPriority,
							galleryAvailableHeight) + gap);
			}

			return result;
		}

		@Override
		public void install(int availableHeight, int gap) {
			for (JBandControlPanel.ControlPanelGroup controlPanelGroup : this.controlPanel
					.getControlPanelGroups()) {
				boolean isCoreContent = controlPanelGroup.isCoreContent();
				if (isCoreContent) {
					List<JRibbonComponent> ribbonComps = controlPanelGroup
							.getRibbonComps();
					for (int i = 0; i < ribbonComps.size(); i++) {
						JRibbonComponent ribbonComp = ribbonComps.get(i);
						RibbonElementPriority targetPriority = RibbonElementPriority.TOP;
						if (ribbonComp.isResizingAware()) {
							targetPriority = this.mapping
									.map(RibbonElementPriority.TOP);
						}
						ribbonComp.setDisplayPriority(targetPriority);
					}
				} else {
					// set the display priority for the galleries
					for (RibbonElementPriority elementPriority : RibbonElementPriority
							.values()) {
						// map the priority
						RibbonElementPriority mappedPriority = mapping
								.map(elementPriority);
						// go over all galleries registered with the specific
						// priority
						for (JRibbonGallery gallery : controlPanelGroup
								.getRibbonGalleries(elementPriority))
							// and set the display priority based on the
							// specific
							// resize policy
							gallery.setDisplayPriority(mappedPriority);
					}

					// set the display priority for the buttons
					Map<RibbonElementPriority, List<AbstractCommandButton>> mapped = new HashMap<RibbonElementPriority, List<AbstractCommandButton>>();
					for (RibbonElementPriority rep : RibbonElementPriority
							.values()) {
						mapped.put(rep, new ArrayList<AbstractCommandButton>());
					}

					for (RibbonElementPriority elementPriority : RibbonElementPriority
							.values()) {
						// map the priority
						RibbonElementPriority mappedPriority = mapping
								.map(elementPriority);
						for (AbstractCommandButton button : controlPanelGroup
								.getRibbonButtons(elementPriority)) {
							// and add the button to a list based on the mapped
							// priority
							mapped.get(mappedPriority).add(button);
						}
					}

					// start from the top priority
					for (AbstractCommandButton big : mapped
							.get(RibbonElementPriority.TOP)) {
						big.setDisplayState(CommandButtonDisplayState.BIG);
					}

					// next - medium priority
					if (mapped.get(RibbonElementPriority.MEDIUM).size() > 0) {
						// try to move buttons from small to medium to make
						// threesomes.
						while (((mapped.get(RibbonElementPriority.MEDIUM)
								.size() % 3) != 0)
								&& (mapped.get(RibbonElementPriority.LOW)
										.size() > 0)) {
							AbstractCommandButton low = mapped.get(
									RibbonElementPriority.LOW).get(0);
							mapped.get(RibbonElementPriority.LOW).remove(low);
							mapped.get(RibbonElementPriority.MEDIUM).add(low);
						}
					}
					for (AbstractCommandButton medium : mapped
							.get(RibbonElementPriority.MEDIUM)) {
						medium
								.setDisplayState(CommandButtonDisplayState.MEDIUM);
					}

					// finally - low priority
					for (AbstractCommandButton low : mapped
							.get(RibbonElementPriority.LOW)) {
						low.setDisplayState(CommandButtonDisplayState.SMALL);
					}
				}
			}
		}
	}

	/**
	 * Core resize policy that maps all {@link RibbonElementPriority}s to
	 * {@link RibbonElementPriority#TOP}.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static final class None extends BaseCoreRibbonBandResizePolicy {
		/**
		 * Creates the new resize policy of type <code>NONE</code>.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public None(JBandControlPanel controlPanel) {
			super(controlPanel, new Mapping() {
				@Override
				public RibbonElementPriority map(RibbonElementPriority priority) {
					return RibbonElementPriority.TOP;
				}
			});
		}
	}

	/**
	 * Core resize policy that maps:
	 * 
	 * <ul>
	 * <li>{@link RibbonElementPriority#TOP} ->
	 * {@link RibbonElementPriority#TOP}</li>
	 * <li>{@link RibbonElementPriority#MEDIUM} ->
	 * {@link RibbonElementPriority#TOP}</li>
	 * <li>{@link RibbonElementPriority#LOW} ->
	 * {@link RibbonElementPriority#MEDIUM}</li>
	 * </ul>
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static final class Low2Mid extends BaseCoreRibbonBandResizePolicy {
		/**
		 * Creates the new resize policy of type <code>LOW2MID</code>.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public Low2Mid(JBandControlPanel controlPanel) {
			super(controlPanel, new Mapping() {
				@Override
				public RibbonElementPriority map(RibbonElementPriority priority) {
					switch (priority) {
					case TOP:
						return RibbonElementPriority.TOP;
					case MEDIUM:
						return RibbonElementPriority.TOP;
					case LOW:
						return RibbonElementPriority.MEDIUM;
					}
					return null;
				}
			});
		}
	}

	/**
	 * Core resize policy that maps:
	 * 
	 * <ul>
	 * <li>{@link RibbonElementPriority#TOP} ->
	 * {@link RibbonElementPriority#TOP}</li>
	 * <li>{@link RibbonElementPriority#MEDIUM} ->
	 * {@link RibbonElementPriority#MEDIUM}</li>
	 * <li>{@link RibbonElementPriority#LOW} ->
	 * {@link RibbonElementPriority#MEDIUM}</li>
	 * </ul>
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static final class Mid2Mid extends BaseCoreRibbonBandResizePolicy {
		/**
		 * Creates the new resize policy of type <code>MID2MID</code>.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public Mid2Mid(JBandControlPanel controlPanel) {
			super(controlPanel, new Mapping() {
				@Override
				public RibbonElementPriority map(RibbonElementPriority priority) {
					switch (priority) {
					case TOP:
						return RibbonElementPriority.TOP;
					case MEDIUM:
						return RibbonElementPriority.MEDIUM;
					case LOW:
						return RibbonElementPriority.MEDIUM;
					}
					return null;
				}
			});
		}
	}

	/**
	 * Mirror core resize policy that maps the values of
	 * {@link RibbonElementPriority}s to themselves.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static final class Mirror extends BaseCoreRibbonBandResizePolicy {
		/**
		 * Creates the new resize policy of type <code>MIRROR</code>.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public Mirror(JBandControlPanel controlPanel) {
			super(controlPanel, new Mapping() {
				@Override
				public RibbonElementPriority map(RibbonElementPriority priority) {
					return priority;
				}
			});
		}
	}

	/**
	 * Core resize policy that maps:
	 * 
	 * <ul>
	 * <li>{@link RibbonElementPriority#TOP} ->
	 * {@link RibbonElementPriority#TOP}</li>
	 * <li>{@link RibbonElementPriority#MEDIUM} ->
	 * {@link RibbonElementPriority#LOW}</li>
	 * <li>{@link RibbonElementPriority#LOW} ->
	 * {@link RibbonElementPriority#LOW}</li>
	 * </ul>
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static final class Mid2Low extends BaseCoreRibbonBandResizePolicy {
		/**
		 * Creates the new resize policy of type <code>MID2LOW</code>.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public Mid2Low(JBandControlPanel controlPanel) {
			super(controlPanel, new Mapping() {
				@Override
				public RibbonElementPriority map(RibbonElementPriority priority) {
					switch (priority) {
					case TOP:
						return RibbonElementPriority.TOP;
					case MEDIUM:
						return RibbonElementPriority.LOW;
					case LOW:
						return RibbonElementPriority.LOW;
					}
					return null;
				}
			});
		}
	}

	/**
	 * Core resize policy that maps:
	 * 
	 * <ul>
	 * <li>{@link RibbonElementPriority#TOP} ->
	 * {@link RibbonElementPriority#MEDIUM}</li>
	 * <li>{@link RibbonElementPriority#MEDIUM} ->
	 * {@link RibbonElementPriority#LOW}</li>
	 * <li>{@link RibbonElementPriority#LOW} ->
	 * {@link RibbonElementPriority#LOW}</li>
	 * </ul>
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static final class High2Mid extends BaseCoreRibbonBandResizePolicy {
		/**
		 * Creates the new resize policy of type <code>HIGH2MID</code>.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public High2Mid(JBandControlPanel controlPanel) {
			super(controlPanel, new Mapping() {
				@Override
				public RibbonElementPriority map(RibbonElementPriority priority) {
					switch (priority) {
					case TOP:
						return RibbonElementPriority.MEDIUM;
					case MEDIUM:
						return RibbonElementPriority.LOW;
					case LOW:
						return RibbonElementPriority.LOW;
					}
					return null;
				}
			});
		}
	}

	/**
	 * Core resize policy that maps all {@link RibbonElementPriority}s to
	 * {@link RibbonElementPriority#LOW}.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static final class High2Low extends BaseCoreRibbonBandResizePolicy {
		/**
		 * Creates the new resize policy of type <code>HIGH2LOW</code>.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public High2Low(JBandControlPanel controlPanel) {
			super(controlPanel, new Mapping() {
				@Override
				public RibbonElementPriority map(RibbonElementPriority priority) {
					return RibbonElementPriority.LOW;
				}
			});
		}
	}

	/**
	 * Core resize policy for {@link JFlowRibbonBand} that places the content in
	 * two rows.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class FlowTwoRows extends
			BaseRibbonBandResizePolicy<JFlowBandControlPanel> {
		/**
		 * Creates a new two-row resize policy for {@link JFlowRibbonBand}s.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public FlowTwoRows(JFlowBandControlPanel controlPanel) {
			super(controlPanel);
		}

		@Override
		public int getPreferredWidth(int availableHeight, int gap) {
			int compCount = controlPanel.getFlowComponents().size();
			int[] widths = new int[compCount];
			int index = 0;
			int currBestResult = 0;
			for (JComponent flowComp : controlPanel.getFlowComponents()) {
				int pref = flowComp.getPreferredSize().width;
				widths[index++] = pref;
				currBestResult += (pref + gap);
			}

			// need to find the inflection point that results in
			// lowest value for max length of two sub-sequences
			for (int inflectionIndex = 0; inflectionIndex < (compCount - 1); inflectionIndex++) {
				int w1 = 0;
				for (int index1 = 0; index1 <= inflectionIndex; index1++) {
					w1 += widths[index1] + gap;
				}
				int w2 = 0;
				for (int index2 = inflectionIndex + 1; index2 < compCount; index2++) {
					w2 += widths[index2] + gap;
				}

				int width = Math.max(w1, w2);
				if (width < currBestResult)
					currBestResult = width;
			}

			return currBestResult;
		}

		@Override
		public void install(int availableHeight, int gap) {
		}
	}

	/**
	 * Core resize policy for {@link JFlowRibbonBand} that places the content in
	 * three rows.
	 * 
	 * @author Kirill Grouchnikov
	 */
	public static class FlowThreeRows extends
			BaseRibbonBandResizePolicy<JFlowBandControlPanel> {
		/**
		 * Creates a new three-row resize policy for {@link JFlowRibbonBand}s.
		 * 
		 * @param controlPanel
		 *            The control panel of the associated ribbon band.
		 */
		public FlowThreeRows(JFlowBandControlPanel controlPanel) {
			super(controlPanel);
		}

		@Override
		public int getPreferredWidth(int availableHeight, int gap) {
			int compCount = controlPanel.getFlowComponents().size();
			int[] widths = new int[compCount];
			int index = 0;
			int currBestResult = 0;
			for (JComponent flowComp : controlPanel.getFlowComponents()) {
				int pref = flowComp.getPreferredSize().width;
				widths[index++] = pref;
				currBestResult += (pref + gap);
			}

			// need to find the inflection points that results in
			// lowest value for max length of three sub-sequences
			for (int inflectionIndex1 = 0; inflectionIndex1 < (compCount - 2); inflectionIndex1++) {
				for (int inflectionIndex2 = inflectionIndex1 + 1; inflectionIndex2 < (compCount - 1); inflectionIndex2++) {
					int w1 = 0;
					for (int index1 = 0; index1 <= inflectionIndex1; index1++) {
						w1 += widths[index1] + gap;
					}
					int w2 = 0;
					for (int index2 = inflectionIndex1 + 1; index2 <= inflectionIndex2; index2++) {
						w2 += widths[index2] + gap;
					}
					int w3 = 0;
					for (int index3 = inflectionIndex2 + 1; index3 < compCount; index3++) {
						w3 += widths[index3] + gap;
					}

					int width = Math.max(Math.max(w1, w2), w3);
					if (width < currBestResult)
						currBestResult = width;
				}
			}

			return currBestResult;
		}

		@Override
		public void install(int availableHeight, int gap) {
		}
	}

	/**
	 * Returns a list that has {@link FlowTwoRows} policy followed by the
	 * {@link FlowThreeRows} resize policy. The last entry is the
	 * {@link IconRibbonBandResizePolicy}.
	 * 
	 * @param ribbonBand
	 *            Ribbon band.
	 * @param stepsToRepeat
	 *            The number of times each one of the {@link FlowTwoRows} /
	 *            {@link FlowThreeRows} should appear consecutively in the
	 *            returned list.
	 * @return The restrictive list of core ribbon band resize policies.
	 */
	public static List<RibbonBandResizePolicy> getCoreFlowPoliciesRestrictive(
			JFlowRibbonBand ribbonBand, int stepsToRepeat) {
		List<RibbonBandResizePolicy> result = new ArrayList<RibbonBandResizePolicy>();
		for (int i = 0; i < stepsToRepeat; i++) {
			result.add(new FlowTwoRows(ribbonBand.getControlPanel()));
		}
		for (int i = 0; i < stepsToRepeat; i++) {
			result.add(new FlowThreeRows(ribbonBand.getControlPanel()));
		}
		result
				.add(new IconRibbonBandResizePolicy(ribbonBand
						.getControlPanel()));
		return result;
	}

}
