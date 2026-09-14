/*
 * Copyright (C) 2010-2026 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.jpexs.examples.substance;

import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceColorSchemeBundle;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.painter.border.GlassBorderPainter;
import org.pushingpixels.substance.api.painter.decoration.ArcDecorationPainter;
import org.pushingpixels.substance.api.painter.fill.GlassFillPainter;
import org.pushingpixels.substance.api.painter.highlight.ClassicHighlightPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;

/**
 * A small, self-contained example of a custom Substance 6.2 skin.
 */
public final class SampleBlueSkin extends SubstanceSkin {

    public static final String NAME = "Sample Blue";

    private static final String COLOR_SCHEMES =
            "com/jpexs/examples/substance/sample-blue.colorschemes";

    public SampleBlueSkin() {
        ColorSchemes schemes = getColorSchemes(COLOR_SCHEMES);
        SubstanceColorScheme active = schemes.get("Sample Blue Active");
        SubstanceColorScheme enabled = schemes.get("Sample Blue Enabled");
        SubstanceColorScheme disabled = schemes.get("Sample Blue Disabled");
        SubstanceColorScheme selected = schemes.get("Sample Blue Selected");
        SubstanceColorScheme rollover = schemes.get("Sample Blue Rollover");
        SubstanceColorScheme border = schemes.get("Sample Blue Border");
        SubstanceColorScheme header = schemes.get("Sample Blue Header");

        SubstanceColorSchemeBundle defaultBundle =
                new SubstanceColorSchemeBundle(active, enabled, disabled);
        defaultBundle.registerColorScheme(selected,
                ComponentState.SELECTED,
                ComponentState.PRESSED_SELECTED);
        defaultBundle.registerColorScheme(rollover,
                ComponentState.ROLLOVER_UNSELECTED,
                ComponentState.ROLLOVER_SELECTED);
        defaultBundle.registerHighlightColorScheme(selected,
                ComponentState.SELECTED,
                ComponentState.ARMED);
        defaultBundle.registerHighlightColorScheme(rollover,
                ComponentState.ROLLOVER_UNSELECTED,
                ComponentState.ROLLOVER_SELECTED,
                ComponentState.ROLLOVER_ARMED);
        defaultBundle.registerColorScheme(border,
                ColorSchemeAssociationKind.BORDER);
        defaultBundle.registerColorScheme(border,
                ColorSchemeAssociationKind.SEPARATOR);
        registerDecorationAreaSchemeBundle(defaultBundle,
                DecorationAreaType.NONE);

        SubstanceColorSchemeBundle headerBundle =
                new SubstanceColorSchemeBundle(header, header, header);
        headerBundle.registerColorScheme(header, 0.55f,
                ComponentState.DISABLED_UNSELECTED,
                ComponentState.DISABLED_SELECTED);
        registerDecorationAreaSchemeBundle(headerBundle,
                DecorationAreaType.PRIMARY_TITLE_PANE,
                DecorationAreaType.SECONDARY_TITLE_PANE,
                DecorationAreaType.HEADER,
                DecorationAreaType.TOOLBAR,
                DecorationAreaType.FOOTER);

        watermarkScheme = enabled;
        watermark = null;
        buttonShaper = new ClassicButtonShaper();
        fillPainter = new GlassFillPainter();
        borderPainter = new GlassBorderPainter();
        highlightPainter = new ClassicHighlightPainter();
        decorationPainter = new ArcDecorationPainter();
    }

    @Override
    public String getDisplayName() {
        return NAME;
    }
}
