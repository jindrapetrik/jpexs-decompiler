/*
 * Copyright (C) 2010-2026 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.jpexs.examples.substance;

import java.util.Collections;
import java.util.Set;
import org.pushingpixels.substance.api.skin.SkinInfo;
import org.pushingpixels.substance.internal.plugin.SubstanceSkinPlugin;

/**
 * Publishes {@link SampleBlueSkin} through the Substance 6.2 plugin API.
 */
public final class SampleSkinPlugin implements SubstanceSkinPlugin {

    @Override
    public Set<SkinInfo> getSkins() {
        SkinInfo skin = new SkinInfo(
                SampleBlueSkin.NAME, SampleBlueSkin.class.getName());
        return Collections.singleton(skin);
    }

    @Override
    public String getDefaultSkinClassName() {
        // Do not replace the host application's default skin.
        return null;
    }
}
