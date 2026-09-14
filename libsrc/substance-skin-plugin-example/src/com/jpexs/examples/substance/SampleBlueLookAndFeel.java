/*
 * Copyright (C) 2010-2026 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.jpexs.examples.substance;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * Optional adapter for applications that prefer UIManager.setLookAndFeel().
 */
public final class SampleBlueLookAndFeel extends SubstanceLookAndFeel {

    public SampleBlueLookAndFeel() {
        super(new SampleBlueSkin());
    }
}
