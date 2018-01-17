/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.Color;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ColorSchemeSingleColorQuery;
import org.pushingpixels.substance.api.ColorSchemeTransform;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceColorSchemeBundle;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.painter.border.CompositeBorderPainter;
import org.pushingpixels.substance.api.painter.border.DelegateFractionBasedBorderPainter;
import org.pushingpixels.substance.api.painter.border.FractionBasedBorderPainter;
import org.pushingpixels.substance.api.painter.border.SubstanceBorderPainter;
import org.pushingpixels.substance.api.painter.decoration.FractionBasedDecorationPainter;
import org.pushingpixels.substance.api.painter.fill.FractionBasedFillPainter;
import org.pushingpixels.substance.api.painter.highlight.ClassicHighlightPainter;
import org.pushingpixels.substance.api.painter.overlay.BottomLineOverlayPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;

/**
 *
 * @author JPEXS
 */
public class OceanicSkin extends SubstanceSkin {

    /**
     * Display name for <code>this</code> skin.
     */
    public static final String NAME = "Oceanic";

    /**
     * Creates a new <code>Oceanic</code> skin.
     */
    public OceanicSkin() {
        SubstanceSkin.ColorSchemes colorSchemes = SubstanceSkin
                .getColorSchemes("com/jpexs/decompiler/flash/gui/graphics/oceanic.colorschemes");

        SubstanceColorScheme activeScheme = colorSchemes
                .get("Oceanic Active");
        SubstanceColorScheme enabledScheme = colorSchemes
                .get("Oceanic Enabled");

        SubstanceColorSchemeBundle defaultSchemeBundle = new SubstanceColorSchemeBundle(
                activeScheme, enabledScheme, enabledScheme);
        defaultSchemeBundle.registerColorScheme(enabledScheme, 0.5f,
                ComponentState.DISABLED_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(activeScheme, 0.5f,
                ComponentState.DISABLED_SELECTED);

        SubstanceColorScheme rolloverScheme = colorSchemes
                .get("Oceanic Rollover");
        SubstanceColorScheme rolloverSelectedScheme = colorSchemes
                .get("Oceanic Rollover Selected");
        SubstanceColorScheme selectedScheme = colorSchemes
                .get("Oceanic Selected");
        SubstanceColorScheme pressedScheme = colorSchemes
                .get("Oceanic Pressed");
        SubstanceColorScheme pressedSelectedScheme = colorSchemes
                .get("Oceanic Pressed Selected");

        // register state-specific color schemes on rollovers and selections
        defaultSchemeBundle.registerColorScheme(rolloverScheme,
                ComponentState.ROLLOVER_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(rolloverSelectedScheme,
                ComponentState.ROLLOVER_SELECTED);
        defaultSchemeBundle.registerColorScheme(selectedScheme,
                ComponentState.SELECTED);
        defaultSchemeBundle.registerColorScheme(pressedScheme,
                ComponentState.PRESSED_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(pressedSelectedScheme,
                ComponentState.PRESSED_SELECTED);

        // register state-specific highlight color schemes on rollover and
        // selections
        defaultSchemeBundle.registerHighlightColorScheme(rolloverScheme, 0.8f,
                ComponentState.ROLLOVER_UNSELECTED);
        defaultSchemeBundle.registerHighlightColorScheme(selectedScheme, 0.8f,
                ComponentState.SELECTED);
        defaultSchemeBundle.registerHighlightColorScheme(
                rolloverSelectedScheme, 0.8f, ComponentState.ROLLOVER_SELECTED);
        defaultSchemeBundle.registerHighlightColorScheme(selectedScheme, 0.8f,
                ComponentState.ARMED, ComponentState.ROLLOVER_ARMED);

        // borders and marks
        SubstanceColorScheme borderEnabledScheme = colorSchemes
                .get("Oceanic Border Enabled");
        SubstanceColorScheme borderActiveScheme = colorSchemes
                .get("Oceanic Border Active");
        SubstanceColorScheme borderRolloverScheme = colorSchemes
                .get("Oceanic Border Rollover");
        SubstanceColorScheme borderRolloverSelectedScheme = colorSchemes
                .get("Oceanic Border Rollover Selected");
        SubstanceColorScheme borderSelectedScheme = colorSchemes
                .get("Oceanic Border Selected");
        SubstanceColorScheme borderPressedScheme = colorSchemes
                .get("Oceanic Border Pressed");

        defaultSchemeBundle.registerColorScheme(borderEnabledScheme,
                ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED);
        defaultSchemeBundle.registerColorScheme(borderEnabledScheme,
                ColorSchemeAssociationKind.BORDER,
                ComponentState.DISABLED_SELECTED,
                ComponentState.DISABLED_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(borderActiveScheme,
                ColorSchemeAssociationKind.BORDER, ComponentState.DEFAULT);
        defaultSchemeBundle.registerColorScheme(borderRolloverScheme,
                ColorSchemeAssociationKind.BORDER,
                ComponentState.ROLLOVER_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(borderRolloverSelectedScheme,
                ColorSchemeAssociationKind.BORDER,
                ComponentState.ROLLOVER_SELECTED, ComponentState.ARMED,
                ComponentState.ROLLOVER_ARMED);
        defaultSchemeBundle.registerColorScheme(borderSelectedScheme,
                ColorSchemeAssociationKind.BORDER, ComponentState.SELECTED);
        defaultSchemeBundle.registerColorScheme(borderPressedScheme,
                ColorSchemeAssociationKind.BORDER,
                ComponentState.PRESSED_SELECTED,
                ComponentState.PRESSED_UNSELECTED);

        // tabs and tab borders
        SubstanceColorScheme tabSelectedScheme = colorSchemes
                .get("Oceanic Tab Selected");
        SubstanceColorScheme tabRolloverScheme = colorSchemes
                .get("Oceanic Tab Rollover");
        defaultSchemeBundle.registerColorScheme(tabSelectedScheme,
                ColorSchemeAssociationKind.TAB, ComponentState.SELECTED,
                ComponentState.ROLLOVER_SELECTED,
                ComponentState.PRESSED_SELECTED,
                ComponentState.PRESSED_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(tabRolloverScheme,
                ColorSchemeAssociationKind.TAB,
                ComponentState.ROLLOVER_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(borderEnabledScheme,
                ColorSchemeAssociationKind.TAB_BORDER, ComponentState.SELECTED,
                ComponentState.ROLLOVER_UNSELECTED);
        defaultSchemeBundle.registerColorScheme(rolloverSelectedScheme,
                ColorSchemeAssociationKind.TAB_BORDER,
                ComponentState.ROLLOVER_SELECTED);

        // separator
        SubstanceColorScheme separatorScheme = colorSchemes
                .get("Oceanic Separator");
        defaultSchemeBundle.registerColorScheme(separatorScheme,
                ColorSchemeAssociationKind.SEPARATOR);

        this.registerDecorationAreaSchemeBundle(defaultSchemeBundle,
                DecorationAreaType.NONE);

        this.watermarkScheme = colorSchemes.get("Oceanic Watermark");

        SubstanceColorScheme generalWatermarkScheme = colorSchemes
                .get("Oceanic Header Watermark");

        this.registerAsDecorationArea(generalWatermarkScheme,
                DecorationAreaType.FOOTER, DecorationAreaType.HEADER,
                DecorationAreaType.TOOLBAR);

        SubstanceColorScheme titleWatermarkScheme = colorSchemes
                .get("Oceanic Title Watermark");

        this.registerAsDecorationArea(titleWatermarkScheme,
                DecorationAreaType.GENERAL,
                DecorationAreaType.PRIMARY_TITLE_PANE,
                DecorationAreaType.SECONDARY_TITLE_PANE);

        setSelectedTabFadeStart(0.7);
        setSelectedTabFadeEnd(0.9);

        this.addOverlayPainter(new BottomLineOverlayPainter(
                new ColorSchemeSingleColorQuery() {
            @Override
            public Color query(SubstanceColorScheme scheme) {
                Color fg = scheme.getForegroundColor();
                return new Color(fg.getRed(), fg.getGreen(), fg
                        .getBlue(), 72);
            }
        }), DecorationAreaType.PRIMARY_TITLE_PANE,
                DecorationAreaType.SECONDARY_TITLE_PANE);

        this.buttonShaper = new ClassicButtonShaper();
        this.watermark = null;

        this.fillPainter = new FractionBasedFillPainter("Oceanic",
                new float[]{0.0f, 0.49999f, 0.5f, 1.0f},
                new ColorSchemeSingleColorQuery[]{
                    ColorSchemeSingleColorQuery.ULTRALIGHT,
                    ColorSchemeSingleColorQuery.LIGHT,
                    ColorSchemeSingleColorQuery.ULTRADARK,
                    ColorSchemeSingleColorQuery.EXTRALIGHT});

        FractionBasedBorderPainter outerBorderPainter = new FractionBasedBorderPainter(
                "Oceanic Outer", new float[]{0.0f, 0.5f, 1.0f},
                new ColorSchemeSingleColorQuery[]{
                    ColorSchemeSingleColorQuery.EXTRALIGHT,
                    ColorSchemeSingleColorQuery.DARK,
                    ColorSchemeSingleColorQuery.MID});
        SubstanceBorderPainter innerBorderPainter = new DelegateFractionBasedBorderPainter(
                "Oceanic Inner", outerBorderPainter, new int[]{
                    0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF},
                new ColorSchemeTransform() {
            @Override
            public SubstanceColorScheme transform(
                    SubstanceColorScheme scheme) {
                return scheme.tint(0.8f);
            }
        });
        this.borderPainter = new CompositeBorderPainter("Oceanic",
                outerBorderPainter, innerBorderPainter);

        this.decorationPainter = new FractionBasedDecorationPainter(
                "Oceanic", new float[]{0.0f, 0.1199999f, 0.12f,
                    0.5f, 0.9f, 1.0f}, new ColorSchemeSingleColorQuery[]{
                    ColorSchemeSingleColorQuery.LIGHT,
                    ColorSchemeSingleColorQuery.LIGHT,
                    ColorSchemeSingleColorQuery.ULTRADARK,
                    ColorSchemeSingleColorQuery.MID,
                    ColorSchemeSingleColorQuery.ULTRALIGHT,
                    ColorSchemeSingleColorQuery.LIGHT});
        this.highlightPainter = new ClassicHighlightPainter();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pushingpixels.substance.skin.SubstanceSkin#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return NAME;
    }
}
