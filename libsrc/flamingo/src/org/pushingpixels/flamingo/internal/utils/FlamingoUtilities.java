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
package org.pushingpixels.flamingo.internal.utils;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.*;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.flamingo.api.ribbon.*;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;
import org.pushingpixels.flamingo.internal.ui.ribbon.JRibbonTaskToggleButton;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;

/**
 * Helper utilities for Flamingo project. This class is for internal use only.
 * 
 * @author Kirill Grouchnikov
 */
public class FlamingoUtilities {
	/**
	 * Gets the component font.
	 * 
	 * @param comp
	 *            Component.
	 * @param keys
	 *            {@link UIManager} keys.
	 * @return If the component is not <code>null</code>, its font is returned.
	 *         Otherwise the first entry in {@link UIManager} which is a
	 *         {@link Font} is returned.
	 */
	public static FontUIResource getFont(Component comp, String... keys) {
		if (comp != null) {
			Font compFont = comp.getFont();
			if ((compFont != null) && !(compFont instanceof UIResource)) {
				return new FontUIResource(compFont);
			}
		}
		for (String key : keys) {
			Font font = UIManager.getFont(key);
			if (font != null) {
				if (font instanceof UIResource)
					return (FontUIResource) font;
				else
					return new FontUIResource(font);
			}
		}
		return null;
	}

	/**
	 * Gets the color based on the specified {@link UIManager} keys.
	 * 
	 * @param defaultColor
	 *            Default color to return if none of the {@link UIManager} keys
	 *            are present.
	 * @param keys
	 *            {@link UIManager} keys.
	 * @return The first entry in {@link UIManager} which is a color. If none,
	 *         then the default color is returned.
	 */
	public static Color getColor(Color defaultColor, String... keys) {
		for (String key : keys) {
			Color color = UIManager.getColor(key);
			if (color != null)
				return color;
		}
		return new ColorUIResource(defaultColor);
	}

	/**
	 * Returns a ribbon band expand icon.
	 * 
	 * @return Ribbon band expand icon.
	 */
	public static ResizableIcon getRibbonBandExpandIcon(
			AbstractRibbonBand ribbonBand) {
		boolean ltr = ribbonBand.getComponentOrientation().isLeftToRight();
		return new ArrowResizableIcon(9, ltr ? SwingConstants.EAST
				: SwingConstants.WEST);
	}

	/**
	 * Returns a popup action icon for the specific command button.
	 */
	public static ResizableIcon getCommandButtonPopupActionIcon(
			JCommandButton commandButton) {
		JCommandButton.CommandButtonPopupOrientationKind popupOrientationKind = ((JCommandButton) commandButton)
				.getPopupOrientationKind();
		switch (popupOrientationKind) {
		case DOWNWARD:
			return new ArrowResizableIcon.CommandButtonPopupIcon(9,
					SwingConstants.SOUTH);
		case SIDEWARD:
			return new ArrowResizableIcon.CommandButtonPopupIcon(
					9,
					commandButton.getComponentOrientation().isLeftToRight() ? SwingConstants.EAST
							: SwingConstants.WEST);
		}
		return null;
	}

	/**
	 * Creates a thumbnail of the specified width.
	 * 
	 * @param image
	 *            The original image.
	 * @param requestedThumbWidth
	 *            The width of the resulting thumbnail.
	 * @return Thumbnail of the specified width.
	 * @author Romain Guy
	 */
	public static BufferedImage createThumbnail(BufferedImage image,
			int requestedThumbWidth) {
		float ratio = (float) image.getWidth() / (float) image.getHeight();
		int width = image.getWidth();
		BufferedImage thumb = image;

		do {
			width /= 2;
			if (width < requestedThumbWidth) {
				width = requestedThumbWidth;
			}

			BufferedImage temp = new BufferedImage(width,
					(int) (width / ratio), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = temp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(thumb, 0, 0, temp.getWidth(), temp.getHeight(), null);
			g2.dispose();

			thumb = temp;
		} while (width != requestedThumbWidth);

		return thumb;
	}

	/**
	 * Returns the outline of the ribbon border.
	 * 
	 * @param startX
	 *            The starting X of the ribbon area.
	 * @param endX
	 *            The ending X of the ribbon area.
	 * @param startSelectedX
	 *            The starting X of the toggle tab button of the selected task.
	 * @param endSelectedX
	 *            The ending X of the toggle tab button of the selected task.
	 * @param topY
	 *            The top Y of the ribbon area.
	 * @param bandTopY
	 *            The top Y of the ribbon band area.
	 * @param bottomY
	 *            The bottom Y of the ribbon area.
	 * @param radius
	 *            Corner radius.
	 * @return The outline of the ribbon border.
	 */
	public static GeneralPath getRibbonBorderOutline(int startX, int endX,
			int startSelectedX, int endSelectedX, int topY, int bandTopY,
			int bottomY, float radius) {
		int height = bottomY - topY;
		GeneralPath result = new GeneralPath();
		float radius3 = (float) (radius / (1.5 * Math.pow(height, 0.5)));

		// start in the top left corner at the end of the curve
		result.moveTo(startX + radius, bandTopY);

		// move to the bottom start of the selected tab and curve up
		result.lineTo(startSelectedX - radius, bandTopY);
		// result.quadTo(startSelectedX - radius3, bandTopY - radius3,
		// startSelectedX, bandTopY - radius);

		// move to the top start of the selected tab and curve right
		// result.lineTo(startSelectedX, topY + radius);
		// result.quadTo(startSelectedX + radius3, topY + radius3,
		// startSelectedX
		// + radius, topY);

		// move to the top end of the selected tab and curve down
		// result.lineTo(endSelectedX - radius - 1, topY);
		// result.quadTo(endSelectedX + radius3 - 1, topY + radius3,
		// endSelectedX - 1, topY + radius);

		// move to the bottom end of the selected tab and curve right
		// result.lineTo(endSelectedX - 1, bandTopY - radius);
		// result.quadTo(endSelectedX + radius3 - 1, bandTopY - radius3,
		// endSelectedX + radius - 1, bandTopY);
		result.moveTo(endSelectedX + radius - 1, bandTopY);

		// move to the top right corner and curve down
		result.lineTo(endX - radius - 1, bandTopY);
		result.quadTo(endX - radius3 - 1, bandTopY + radius3, endX - 1,
				bandTopY + radius);

		// move to the bottom right corner and curve left
		result.lineTo(endX - 1, bottomY - radius - 1);
		result.quadTo(endX - radius3 - 1, bottomY - 1 - radius3, endX - radius
				- 1, bottomY - 1);

		// move to the bottom left corner and curve up
		result.lineTo(startX + radius, bottomY - 1);
		result.quadTo(startX + radius3, bottomY - 1 - radius3, startX, bottomY
				- radius - 1);

		// move to the top left corner and curve right
		result.lineTo(startX, bandTopY + radius);
		result.quadTo(startX + radius3, bandTopY + radius3, startX + radius,
				bandTopY);

		return result;
	}

	/**
	 * Returns the clip area of a task toggle button in ribbon component.
	 * 
	 * @param width
	 *            Toggle tab button width.
	 * @param height
	 *            Toggle tab button height.
	 * @param radius
	 *            Toggle tab button corner radius.
	 * @return Clip area of a toggle tab button in ribbon component.
	 */
	public static GeneralPath getRibbonTaskToggleButtonOutline(int width,
			int height, float radius) {
		GeneralPath result = new GeneralPath();
		float radius3 = (float) (radius / (1.5 * Math.pow(height, 0.5)));

		// start at the bottom left
		result.moveTo(0, height);

		// move to the top start and curve right
		result.lineTo(0, radius);
		result.quadTo(radius3, radius3, radius, 0);

		// move to the top end and curve down
		result.lineTo(width - radius - 1, 0);
		result.quadTo(width + radius3 - 1, radius3, width - 1, radius);

		// move to the bottom right end
		result.lineTo(width - 1, height);

		// move to the bottom left end
		result.lineTo(0, height);

		return result;
	}

	/**
	 * Returns the outline of in-ribbon gallery.
	 * 
	 * @param startX
	 *            Start X of the in-ribbon gallery.
	 * @param endX
	 *            End X of the in-ribbon gallery.
	 * @param topY
	 *            Top Y of the in-ribbon gallery.
	 * @param bottomY
	 *            Bottom Y of the in-ribbon gallery.
	 * @param radius
	 *            Corner radius.
	 * @return The outline of in-ribbon gallery.
	 */
	public static GeneralPath getRibbonGalleryOutline(int startX, int endX,
			int topY, int bottomY, float radius) {

		int height = bottomY - topY;
		GeneralPath result = new GeneralPath();
		float radius3 = (float) (radius / (1.5 * Math.pow(height, 0.5)));

		// start in the top left corner at the end of the curve
		result.moveTo(startX + radius, topY);

		// move to the top right corner and curve down
		result.lineTo(endX - radius - 1, topY);
		result.quadTo(endX - radius3 - 1, topY + radius3, endX - 1, topY
				+ radius);

		// move to the bottom right corner and curve left
		result.lineTo(endX - 1, bottomY - radius - 1);
		result.quadTo(endX - radius3 - 1, bottomY - 1 - radius3, endX - radius
				- 1, bottomY - 1);

		// move to the bottom left corner and curve up
		result.lineTo(startX + radius, bottomY - 1);
		result.quadTo(startX + radius3, bottomY - 1 - radius3, startX, bottomY
				- radius - 1);

		// move to the top left corner and curve right
		result.lineTo(startX, topY + radius);
		result.quadTo(startX + radius3, topY + radius3, startX + radius, topY);

		return result;
	}

	/**
	 * Clips string based on specified font metrics and available width (in
	 * pixels). Returns the clipped string, which contains the beginning and the
	 * end of the input string separated by ellipses (...) in case the string is
	 * too long to fit into the specified width, and the original string
	 * otherwise.
	 * 
	 * @param metrics
	 *            Font metrics.
	 * @param availableWidth
	 *            Available width in pixels.
	 * @param fullText
	 *            String to clip.
	 * @return The clipped string, which contains the beginning and the end of
	 *         the input string separated by ellipses (...) in case the string
	 *         is too long to fit into the specified width, and the original
	 *         string otherwise.
	 */
	public static String clipString(FontMetrics metrics, int availableWidth,
			String fullText) {

		if (metrics.stringWidth(fullText) <= availableWidth)
			return fullText;

		String ellipses = "...";
		int ellipsesWidth = metrics.stringWidth(ellipses);
		if (ellipsesWidth > availableWidth)
			return "";

		String starter = "";

		int w = fullText.length();
		String prevText = "";
		for (int i = 0; i < w; i++) {
			String newStarter = starter + fullText.charAt(i);
			String newText = newStarter + ellipses;
			if (metrics.stringWidth(newText) <= availableWidth) {
				starter = newStarter;
				prevText = newText;
				continue;
			}
			return prevText;
		}
		return fullText;
	}

	/**
	 * Retrieves transparent image of specified dimension.
	 * 
	 * @param width
	 *            Image width.
	 * @param height
	 *            Image height.
	 * @return Transparent image of specified dimension.
	 */
	public static BufferedImage getBlankImage(int width, int height) {
		GraphicsEnvironment e = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice d = e.getDefaultScreenDevice();
		GraphicsConfiguration c = d.getDefaultConfiguration();
		BufferedImage compatibleImage = c.createCompatibleImage(width, height,
				Transparency.TRANSLUCENT);
		return compatibleImage;
	}

	/**
	 * Returns the alpha version of the specified color.
	 * 
	 * @param color
	 *            Original color.
	 * @param alpha
	 *            Alpha channel value.
	 * @return Alpha version of the specified color.
	 */
	public static Color getAlphaColor(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),
				alpha);
	}

	public static int getHLayoutGap(AbstractCommandButton commandButton) {
		Font font = commandButton.getFont();
		if (font == null)
			font = UIManager.getFont("Button.font");
		return (int) Math.ceil(commandButton.getHGapScaleFactor()
				* (font.getSize() - 4) / 4);
	}

	public static int getVLayoutGap(AbstractCommandButton commandButton) {
		Font font = commandButton.getFont();
		if (font == null)
			font = UIManager.getFont("Button.font");
		return (int) Math.ceil(commandButton.getVGapScaleFactor()
				* (font.getSize() - 4) / 4);
	}

	public static boolean hasPopupAction(AbstractCommandButton commandButton) {
		if (commandButton instanceof JCommandButton) {
			JCommandButton jcb = (JCommandButton) commandButton;
			return jcb.getCommandButtonKind().hasPopup();
		}
		return false;
	}

	public static void updateRibbonFrameIconImages(JRibbonFrame ribbonFrame) {
		JRibbonApplicationMenuButton appMenuButton = getApplicationMenuButton(ribbonFrame);
		if (appMenuButton == null) {
			return;
		}

		ResizableIcon appIcon = ribbonFrame.getApplicationIcon();
		if (appIcon != null) {
			appMenuButton.setIcon(appIcon);
		}
	}

	public static JRibbonApplicationMenuButton getApplicationMenuButton(
			Component comp) {
		if (comp instanceof JRibbonApplicationMenuButton)
			return (JRibbonApplicationMenuButton) comp;
		if (comp instanceof Container) {
			Container cont = (Container) comp;
			for (int i = 0; i < cont.getComponentCount(); i++) {
				JRibbonApplicationMenuButton result = getApplicationMenuButton(cont
						.getComponent(i));
				if ((result != null) && result.isVisible())
					return result;
			}
		}
		return null;
	}

	public static void renderSurface(Graphics g, Container c, Rectangle rect,
			boolean toSimulateRollover, boolean hasTopBorder,
			boolean hasBottomBorder) {
		CellRendererPane buttonRendererPane = new CellRendererPane();
		JButton rendererButton = new JButton("");
		rendererButton.getModel().setRollover(toSimulateRollover);

		buttonRendererPane.setBounds(rect.x, rect.y, rect.width, rect.height);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.clipRect(rect.x, rect.y, rect.width, rect.height);
		buttonRendererPane.paintComponent(g2d, rendererButton, c, rect.x
				- rect.width / 2, rect.y - rect.height / 2, 2 * rect.width,
				2 * rect.height, true);

		g2d.setColor(FlamingoUtilities.getBorderColor());
		if (hasTopBorder) {
			g2d.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
		}
		if (hasBottomBorder) {
			g2d.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width
					- 1, rect.y + rect.height - 1);
		}
		g2d.dispose();
	}

	/**
	 * Returns lighter version of the specified color.
	 * 
	 * @param color
	 *            Color.
	 * @param diff
	 *            Difference factor (values closer to 1.0 will produce results
	 *            closer to white color).
	 * @return Lighter version of the specified color.
	 */
	public static Color getLighterColor(Color color, double diff) {
		int r = color.getRed() + (int) (diff * (255 - color.getRed()));
		int g = color.getGreen() + (int) (diff * (255 - color.getGreen()));
		int b = color.getBlue() + (int) (diff * (255 - color.getBlue()));
		return new Color(r, g, b);
	}

	public static Color getBorderColor() {
		return FlamingoUtilities.getColor(Color.gray,
				"TextField.inactiveForeground", "Button.disabledText",
				"ComboBox.disabledForeground");
	}

	public static boolean isShowingMinimizedRibbonInPopup(JRibbon ribbon) {
		List<PopupPanelManager.PopupInfo> popups = PopupPanelManager
				.defaultManager().getShownPath();
		if (popups.size() == 0)
			return false;

		for (PopupPanelManager.PopupInfo popup : popups) {
			JComponent originator = popup.getPopupOriginator();
			if (originator instanceof JRibbonTaskToggleButton) {
				return (ribbon == SwingUtilities.getAncestorOfClass(
						JRibbon.class, originator));
			}
		}
		return false;
	}

	public static boolean isShowingMinimizedRibbonInPopup(
			JRibbonTaskToggleButton taskToggleButton) {
		List<PopupPanelManager.PopupInfo> popups = PopupPanelManager
				.defaultManager().getShownPath();
		if (popups.size() == 0)
			return false;

		for (PopupPanelManager.PopupInfo popup : popups) {
			JComponent originator = popup.getPopupOriginator();
			if (originator == taskToggleButton)
				return true;
		}
		return false;
	}

	public static void checkResizePoliciesConsistency(
			AbstractRibbonBand ribbonBand) {
		Insets ins = ribbonBand.getInsets();
		AbstractBandControlPanel controlPanel = ribbonBand.getControlPanel();
		if (controlPanel == null)
			return;
		int height = controlPanel.getPreferredSize().height
				+ ribbonBand.getUI().getBandTitleHeight() + ins.top
				+ ins.bottom;
		List<RibbonBandResizePolicy> resizePolicies = ribbonBand
				.getResizePolicies();
		checkResizePoliciesConsistencyBase(ribbonBand);
		for (int i = 0; i < (resizePolicies.size() - 1); i++) {
			RibbonBandResizePolicy policy1 = resizePolicies.get(i);
			RibbonBandResizePolicy policy2 = resizePolicies.get(i + 1);
			int width1 = policy1.getPreferredWidth(height, 4);
			int width2 = policy2.getPreferredWidth(height, 4);
			if (width1 < width2) {
				// create the trace message
				StringBuilder builder = new StringBuilder();
				builder.append("Inconsistent preferred widths\n");
				builder.append("Ribbon band '" + ribbonBand.getTitle()
						+ "' has the following resize policies\n");
				for (int j = 0; j < resizePolicies.size(); j++) {
					RibbonBandResizePolicy policy = resizePolicies.get(j);
					int width = policy.getPreferredWidth(height, 4);
					builder.append("\t" + policy.getClass().getName()
							+ " with preferred width " + width + "\n");
				}
				builder.append(policy1.getClass().getName()
						+ " with pref width " + width1
						+ " is followed by resize policy "
						+ policy2.getClass().getName()
						+ " with larger pref width\n");

				throw new IllegalStateException(builder.toString());
			}
		}
	}

	public static void checkResizePoliciesConsistencyBase(
			AbstractRibbonBand ribbonBand) {
		List<RibbonBandResizePolicy> resizePolicies = ribbonBand
				.getResizePolicies();
		if (resizePolicies.size() == 0) {
			throw new IllegalStateException("Resize policy list is empty");
		}
		for (int i = 0; i < resizePolicies.size(); i++) {
			RibbonBandResizePolicy policy = resizePolicies.get(i);
			boolean isIcon = policy instanceof IconRibbonBandResizePolicy;
			if (isIcon && (i < (resizePolicies.size() - 1))) {
				throw new IllegalStateException(
						"Icon resize policy must be the last in the list");
			}
		}
	}
}
