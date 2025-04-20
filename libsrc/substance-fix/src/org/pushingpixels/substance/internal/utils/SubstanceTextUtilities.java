/*
 * Copyright (c) 2005-2010 Substance Kirill Grouchnikov. All Rights Reserved.
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
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of
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
package org.pushingpixels.substance.internal.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.text.JTextComponent;

import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.lafwidget.text.LockBorder;
import org.pushingpixels.lafwidget.utils.RenderingUtils;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.watermark.SubstanceWatermark;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.TransitionAwareUI;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.utils.border.SubstanceTextComponentBorder;

/**
 * Text-related utilities. This class if for internal use only.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceTextUtilities {
	public static final String ENFORCE_FG_COLOR = "substancelaf.internal.textUtilities.enforceFgColor";

	/**
	 * Paints text with drop shadow.
	 * 
	 * @param c
	 *            Component.
	 * @param g
	 *            Graphics context.
	 * @param foregroundColor
	 *            Foreground color.
	 * @param text
	 *            Text to paint.
	 * @param width
	 *            Text rectangle width.
	 * @param height
	 *            Text rectangle height.
	 * @param xOffset
	 *            Text rectangle X offset.
	 * @param yOffset
	 *            Text rectangle Y offset.
	 */
	public static void paintTextWithDropShadow(JComponent c, Graphics g,
			Color foregroundColor, String text, int width, int height,
			int xOffset, int yOffset) {
		Graphics2D graphics = (Graphics2D) g.create();
		RenderingUtils.installDesktopHints(graphics, c);

		// blur the text shadow
		BufferedImage blurred = SubstanceCoreUtilities.getBlankImage(width,
				height);
		Graphics2D gBlurred = (Graphics2D) blurred.getGraphics();
		gBlurred.setFont(graphics.getFont());
		gBlurred.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		// Color neg =
		// SubstanceColorUtilities.getNegativeColor(foregroundColor);
		float luminFactor = SubstanceColorUtilities
				.getColorStrength(foregroundColor);
		gBlurred.setColor(SubstanceColorUtilities
				.getNegativeColor(foregroundColor));
		ConvolveOp convolve = new ConvolveOp(new Kernel(3, 3, new float[] {
				.02f, .05f, .02f, .05f, .02f, .05f, .02f, .05f, .02f }),
				ConvolveOp.EDGE_NO_OP, null);
		gBlurred.drawString(text, xOffset, yOffset - 1);
		blurred = convolve.filter(blurred, null);

		graphics.setComposite(LafWidgetUtilities.getAlphaComposite(c,
				luminFactor, g));
		graphics.drawImage(blurred, 0, 0, null);
		graphics.setComposite(LafWidgetUtilities.getAlphaComposite(c, g));

		FontMetrics fm = graphics.getFontMetrics();
		SubstanceTextUtilities.paintText(graphics, c, new Rectangle(xOffset,
				yOffset - fm.getAscent(), width - xOffset, fm.getHeight()),
				text, -1, graphics.getFont(), foregroundColor, graphics
						.getClipBounds());

		graphics.dispose();
	}

	/**
	 * Paints the specified text.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param comp
	 *            Component.
	 * @param textRect
	 *            Text rectangle.
	 * @param text
	 *            Text to paint.
	 * @param mnemonicIndex
	 *            Mnemonic index.
	 * @param font
	 *            Font to use.
	 * @param color
	 *            Color to use.
	 * @param clip
	 *            Optional clip. Can be <code>null</code>.
	 * @param transform
	 *            Optional transform to apply. Can be <code>null</code>.
	 */
	private static void paintText(Graphics g, JComponent comp,
			Rectangle textRect, String text, int mnemonicIndex,
			java.awt.Font font, java.awt.Color color, java.awt.Rectangle clip,
			java.awt.geom.AffineTransform transform) {
		if ((text == null) || (text.length() == 0))
			return;

		Graphics2D g2d = (Graphics2D) g.create();
		// workaroundBug6576507(g2d);
		// RenderingUtils.installDesktopHints(g2d);

		g2d.setFont(font);
		g2d.setColor(color);
		// fix for issue 420 - call clip() instead of setClip() to
		// respect the currently set clip shape
		if (clip != null)
			g2d.clip(clip);
		if (transform != null)
			g2d.transform(transform);
		BasicGraphicsUtils.drawStringUnderlineCharAt(g2d, text, mnemonicIndex,
				textRect.x, textRect.y + g2d.getFontMetrics().getAscent());
		g2d.dispose();
	}

	/**
	 * Paints the specified text.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param comp
	 *            Component.
	 * @param textRect
	 *            Text rectangle.
	 * @param text
	 *            Text to paint.
	 * @param mnemonicIndex
	 *            Mnemonic index.
	 * @param font
	 *            Font to use.
	 * @param color
	 *            Color to use.
	 * @param clip
	 *            Optional clip. Can be <code>null</code>.
	 */
	public static void paintText(Graphics g, JComponent comp,
			Rectangle textRect, String text, int mnemonicIndex,
			java.awt.Font font, java.awt.Color color, java.awt.Rectangle clip) {
		SubstanceTextUtilities.paintText(g, comp, textRect, text,
				mnemonicIndex, font, color, clip, null);
	}

	/**
	 * Paints the specified vertical text.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param comp
	 *            Component.
	 * @param textRect
	 *            Text rectangle.
	 * @param text
	 *            Text to paint.
	 * @param mnemonicIndex
	 *            Mnemonic index.
	 * @param font
	 *            Font to use.
	 * @param color
	 *            Color to use.
	 * @param clip
	 *            Optional clip. Can be <code>null</code>.
	 * @param isFromBottomToTop
	 *            If <code>true</code>, the text will be painted from bottom to
	 *            top, otherwise the text will be painted from top to bottom.
	 */
	public static void paintVerticalText(Graphics g, JComponent comp,
			Rectangle textRect, String text, int mnemonicIndex,
			java.awt.Font font, java.awt.Color color, java.awt.Rectangle clip,
			boolean isFromBottomToTop) {
		if ((text == null) || (text.length() == 0))
			return;

		AffineTransform at = null;

		if (!isFromBottomToTop) {
			at = AffineTransform.getTranslateInstance(textRect.x
					+ textRect.width, textRect.y);
			at.rotate(Math.PI / 2);
		} else {
			at = AffineTransform.getTranslateInstance(textRect.x, textRect.y
					+ textRect.height);
			at.rotate(-Math.PI / 2);
		}
		Rectangle newRect = new Rectangle(0, 0, textRect.width, textRect.height);

		SubstanceTextUtilities.paintText(g, comp, newRect, text, mnemonicIndex,
				font, color, clip, at);
	}

	/**
	 * Paints the text of the specified button.
	 * 
	 * @param g
	 *            Graphic context.
	 * @param button
	 *            Button
	 * @param textRect
	 *            Text rectangle
	 * @param text
	 *            Text to paint
	 * @param mnemonicIndex
	 *            Mnemonic index.
	 */
	public static void paintText(Graphics g, AbstractButton button,
			Rectangle textRect, String text, int mnemonicIndex) {
		paintText(g, button, button.getModel(), textRect, text, mnemonicIndex);
	}

	/**
	 * Paints the text of the specified button.
	 * 
	 * @param g
	 *            Graphic context.
	 * @param button
	 *            Button
	 * @param model
	 *            Button model.
	 * @param textRect
	 *            Text rectangle
	 * @param text
	 *            Text to paint
	 * @param mnemonicIndex
	 *            Mnemonic index.
	 */
	public static void paintText(Graphics g, AbstractButton button,
			ButtonModel model, Rectangle textRect, String text,
			int mnemonicIndex) {
		TransitionAwareUI transitionAwareUI = (TransitionAwareUI) button
				.getUI();
		StateTransitionTracker stateTransitionTracker = transitionAwareUI
				.getTransitionTracker();

		float buttonAlpha = SubstanceColorSchemeUtilities.getAlpha(button,
				ComponentState.getState(button));

		if (button instanceof JMenuItem) {
			paintMenuItemText(g, (JMenuItem) button, textRect, text,
					mnemonicIndex, stateTransitionTracker.getModelStateInfo(),
					buttonAlpha);
		} else {
			paintText(g, button, textRect, text, mnemonicIndex,
					stateTransitionTracker.getModelStateInfo(), buttonAlpha);
		}
	}

	/**
	 * Paints the specified text.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param component
	 *            Component.
	 * @param textRect
	 *            Text rectangle.
	 * @param text
	 *            Text to paint.
	 * @param mnemonicIndex
	 *            Mnemonic index.
	 * @param state
	 *            Component state.
	 * @param prevState
	 *            Component previous state.
	 * @param textAlpha
	 *            Alpha channel for painting the text.
	 */
	public static void paintText(Graphics g, JComponent component,
			Rectangle textRect, String text, int mnemonicIndex,
			ComponentState state, float textAlpha) {
		Color fgColor = getForegroundColor(component, text, state, textAlpha);

		SubstanceTextUtilities.paintText(g, component, textRect, text,
				mnemonicIndex, component.getFont(), fgColor, null);
	}

	public static void paintText(Graphics g, JComponent component,
			Rectangle textRect, String text, int mnemonicIndex,
			StateTransitionTracker.ModelStateInfo modelStateInfo,
			float textAlpha) {
		Color fgColor = getForegroundColor(component, text, modelStateInfo,
				textAlpha);

		SubstanceTextUtilities.paintText(g, component, textRect, text,
				mnemonicIndex, component.getFont(), fgColor, null);
	}

	public static void paintMenuItemText(Graphics g, JMenuItem menuItem,
			Rectangle textRect, String text, int mnemonicIndex,
			StateTransitionTracker.ModelStateInfo modelStateInfo,
			float textAlpha) {
		Color fgColor = getMenuComponentForegroundColor(menuItem, text,
				modelStateInfo, textAlpha);

		SubstanceTextUtilities.paintText(g, menuItem, textRect, text,
				mnemonicIndex, menuItem.getFont(), fgColor, null);
	}

	/**
	 * Returns the foreground color for the specified component.
	 * 
	 * @param component
	 *            Component.
	 * @param text
	 *            Text. If empty or <code>null</code>, the result is
	 *            <code>null</code>.
	 * @param state
	 *            Component state.
	 * @param textAlpha
	 *            Alpha channel for painting the text. If value is less than
	 *            1.0, the result is an opaque color which is an interpolation
	 *            between the "real" foreground color and the background color
	 *            of the component. This is done to ensure that native text
	 *            rasterization will be performed on 6u10+ on Windows.
	 * @return The foreground color for the specified component.
	 */
	public static Color getForegroundColor(JComponent component, String text,
			ComponentState state, float textAlpha) {
		if ((text == null) || (text.length() == 0))
			return null;

		boolean toEnforceFgColor = (SwingUtilities.getAncestorOfClass(
				CellRendererPane.class, component) != null)
				|| Boolean.TRUE.equals(component
						.getClientProperty(ENFORCE_FG_COLOR));

		Color fgColor = toEnforceFgColor ? component.getForeground()
				: SubstanceColorSchemeUtilities
						.getColorScheme(component, state).getForegroundColor();

		// System.out.println(text + ":" + prevState.name() + "->" +
		// state.name() + ":" + fgColor);
		if (textAlpha < 1.0f) {
			Color bgFillColor = SubstanceColorUtilities
					.getBackgroundFillColor(component);
			fgColor = SubstanceColorUtilities.getInterpolatedColor(fgColor,
					bgFillColor, textAlpha);
		}
		return fgColor;
	}

	/**
	 * Returns the foreground color for the specified component.
	 * 
	 * @param component
	 *            Component.
	 * @param text
	 *            Text. If empty or <code>null</code>, the result is
	 *            <code>null</code>.
	 * @param textAlpha
	 *            Alpha channel for painting the text. If value is less than
	 *            1.0, the result is an opaque color which is an interpolation
	 *            between the "real" foreground color and the background color
	 *            of the component. This is done to ensure that native text
	 *            rasterization will be performed on 6u10 on Windows.
	 * @return The foreground color for the specified component.
	 */
	public static Color getForegroundColor(JComponent component, String text,
			StateTransitionTracker.ModelStateInfo modelStateInfo,
			float textAlpha) {
		if ((text == null) || (text.length() == 0))
			return null;

		boolean toEnforceFgColor = (SwingUtilities.getAncestorOfClass(
				CellRendererPane.class, component) != null)
				|| Boolean.TRUE.equals(component
						.getClientProperty(ENFORCE_FG_COLOR));

		Color fgColor = null;
		if (toEnforceFgColor) {
			fgColor = component.getForeground();
		} else {
			fgColor = SubstanceColorUtilities.getForegroundColor(component,
					modelStateInfo);
		}

		// System.out.println(text + ":" + prevState.name() + "->" +
		// state.name() + ":" + fgColor);
		if (textAlpha < 1.0f) {
			Color bgFillColor = SubstanceColorUtilities
					.getBackgroundFillColor(component);
			fgColor = SubstanceColorUtilities.getInterpolatedColor(fgColor,
					bgFillColor, textAlpha);
		}
		return fgColor;
	}

	/**
	 * Returns the foreground color for the specified menu component.
	 * 
	 * @param menuComponent
	 *            Menu component.
	 * @param text
	 *            Text. If empty or <code>null</code>, the result is
	 *            <code>null</code>.
	 * @param modelStateInfo
	 *            Model state info for the specified component.
	 * @param textAlpha
	 *            Alpha channel for painting the text. If value is less than
	 *            1.0, the result is an opaque color which is an interpolation
	 *            between the "real" foreground color and the background color
	 *            of the component. This is done to ensure that native text
	 *            rasterization will be performed on 6u10 on Windows.
	 * @return The foreground color for the specified component.
	 */
	public static Color getMenuComponentForegroundColor(Component menuComponent,
			String text, StateTransitionTracker.ModelStateInfo modelStateInfo,
			float textAlpha) {
		if ((text == null) || (text.length() == 0))
			return null;

		Color fgColor = SubstanceColorUtilities
				.getMenuComponentForegroundColor(menuComponent, modelStateInfo);

		if (textAlpha < 1.0f) {
			Color bgFillColor = SubstanceColorUtilities
					.getBackgroundFillColor(menuComponent);
			fgColor = SubstanceColorUtilities.getInterpolatedColor(fgColor,
					bgFillColor, textAlpha);
		}
		return fgColor;
	}

	/**
	 * Paints background of the specified text component.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param comp
	 *            Component.
	 */
	public static void paintTextCompBackground(Graphics g, JComponent comp) {
		Color backgroundFillColor = getTextBackgroundFillColor(comp);

		boolean toPaintWatermark = (SubstanceLookAndFeel.getCurrentSkin(comp)
				.getWatermark() != null)
				&& (SubstanceCoreUtilities.toDrawWatermark(comp) || !comp
						.isOpaque());
		paintTextCompBackground(g, comp, backgroundFillColor, toPaintWatermark);
	}

	public static Color getTextBackgroundFillColor(JComponent comp) {
		Color backgroundFillColor = SubstanceColorUtilities
				.getBackgroundFillColor(comp);
		JTextComponent componentForTransitions = SubstanceCoreUtilities
				.getTextComponentForTransitions(comp);

		if (componentForTransitions != null) {
			ComponentUI ui = componentForTransitions.getUI();
			if (ui instanceof TransitionAwareUI) {
				TransitionAwareUI trackable = (TransitionAwareUI) ui;
				StateTransitionTracker stateTransitionTracker = trackable
						.getTransitionTracker();

				Color outerTextComponentBorderColor = SubstanceColorUtilities
						.getOuterTextComponentBorderColor(backgroundFillColor);
				outerTextComponentBorderColor = SubstanceColorUtilities
						.getInterpolatedColor(outerTextComponentBorderColor,
								backgroundFillColor, 0.6);

				float selectionStrength = stateTransitionTracker
						.getFacetStrength(ComponentStateFacet.SELECTION);
				float rolloverStrength = stateTransitionTracker
						.getFacetStrength(ComponentStateFacet.ROLLOVER);
				backgroundFillColor = SubstanceColorUtilities
						.getInterpolatedColor(outerTextComponentBorderColor,
								backgroundFillColor, Math.max(
										selectionStrength, rolloverStrength));
			}
		}
		return backgroundFillColor;
	}

	/**
	 * Paints background of the specified text component.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param comp
	 *            Component.
	 * @param backgr
	 *            Background color.
	 * @param toOverlayWatermark
	 *            If <code>true</code>, this method will paint the watermark
	 *            overlay on top of the background fill.
	 */
	private static void paintTextCompBackground(Graphics g, JComponent comp,
			Color backgr, boolean toOverlayWatermark) {
		Graphics2D g2d = (Graphics2D) g.create();

		int componentFontSize = SubstanceSizeUtils.getComponentFontSize(comp);
		int borderDelta = (int) Math.floor(SubstanceSizeUtils
				.getBorderStrokeWidth(componentFontSize));
		Border compBorder = comp.getBorder();

		if (compBorder instanceof LockBorder) {
			compBorder = ((LockBorder) compBorder).getOriginalBorder();
		}
		boolean isSubstanceBorder = compBorder instanceof SubstanceTextComponentBorder;

		if (!isSubstanceBorder) {
			Border border = compBorder;
			while (border instanceof CompoundBorder) {
				Border outer = ((CompoundBorder) border).getOutsideBorder();
				if (outer instanceof SubstanceTextComponentBorder) {
					isSubstanceBorder = true;
					break;
				}
				Border inner = ((CompoundBorder) border).getInsideBorder();
				if (inner instanceof SubstanceTextComponentBorder) {
					isSubstanceBorder = true;
					break;
				}
				border = inner;
			}
		}

		Shape contour = isSubstanceBorder ? SubstanceOutlineUtilities
				.getBaseOutline(
						comp.getWidth(),
						comp.getHeight(),
						Math
								.max(
										0,
										2.0f
												* SubstanceSizeUtils
														.getClassicButtonCornerRadius(componentFontSize)
												- borderDelta), null,
						borderDelta)
				: new Rectangle(0, 0, comp.getWidth(), comp.getHeight());

		BackgroundPaintingUtils.update(g, comp, false);
		SubstanceWatermark watermark = SubstanceCoreUtilities.getSkin(comp)
				.getWatermark();
		if (watermark != null) {
			watermark.drawWatermarkImage(g2d, comp, 0, 0, comp.getWidth(), comp
					.getHeight());
		}
		g2d.setColor(backgr);
		g2d.fill(contour);

		if (toOverlayWatermark) {
			if (watermark != null) {
				g2d.clip(contour);
				watermark.drawWatermarkImage(g2d, comp, 0, 0, comp.getWidth(),
						comp.getHeight());
			}
		}

		g2d.dispose();
	}
}
