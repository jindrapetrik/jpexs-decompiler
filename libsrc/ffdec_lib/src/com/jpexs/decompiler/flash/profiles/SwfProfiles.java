/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.profiles;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of custom SWF profiles.
 *
 * @author JPEXS
 */
public final class SwfProfiles {

    private static final List<SwfProfile> PROFILES = Collections.unmodifiableList(Arrays.asList(
            new BioshockSwfProfile()
    ));

    private static final Map<String, SwfProfile> PROFILES_BY_ID = new LinkedHashMap<>();

    static {
        for (SwfProfile profile : PROFILES) {
            PROFILES_BY_ID.put(profile.getId(), profile);
        }
    }

    private SwfProfiles() {
    }

    /**
     * Gets all registered custom SWF profiles.
     *
     * @return Registered profiles
     */
    public static List<SwfProfile> getProfiles() {
        return PROFILES;
    }

    /**
     * Gets a registered custom SWF profile.
     *
     * @param id Profile identifier
     * @return Profile, or null when the identifier is empty or unknown
     */
    public static SwfProfile getProfile(String id) {
        return PROFILES_BY_ID.get(id);
    }

    /**
     * Gets profiles which can decode at least one unknown tag in an SWF.
     *
     * @param swf SWF to inspect
     * @return Matching profiles
     */
    public static List<SwfProfile> getMatchingProfiles(SWF swf) {
        List<SwfProfile> result = new ArrayList<>();
        for (SwfProfile profile : PROFILES) {
            if (profile.supportsSwf(swf) && containsSupportedUnknownTag(swf, profile)) {
                result.add(profile);
            }
        }
        return result;
    }

    private static boolean containsSupportedUnknownTag(Timelined timelined, SwfProfile profile) {
        for (Tag tag : timelined.getTags()) {
            if (tag instanceof UnknownTag && profile.supportsTagId(tag.getId())) {
                return true;
            }
            if (tag instanceof Timelined && containsSupportedUnknownTag((Timelined) tag, profile)) {
                return true;
            }
        }
        return false;
    }
}
