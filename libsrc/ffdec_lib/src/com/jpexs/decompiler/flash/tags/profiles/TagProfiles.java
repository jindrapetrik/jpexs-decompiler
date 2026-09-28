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
package com.jpexs.decompiler.flash.tags.profiles;

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
 * Registry of custom tag profiles.
 *
 * @author JPEXS
 */
public final class TagProfiles {

    private static final List<TagProfile> PROFILES = Collections.unmodifiableList(Arrays.asList(
            new BioshockTagProfile()
    ));

    private static final Map<String, TagProfile> PROFILES_BY_ID = new LinkedHashMap<>();

    static {
        for (TagProfile profile : PROFILES) {
            PROFILES_BY_ID.put(profile.getId(), profile);
        }
    }

    private TagProfiles() {
    }

    /**
     * Gets all registered custom tag profiles.
     *
     * @return Registered profiles
     */
    public static List<TagProfile> getProfiles() {
        return PROFILES;
    }

    /**
     * Gets a registered custom tag profile.
     *
     * @param id Profile identifier
     * @return Profile, or null when the identifier is empty or unknown
     */
    public static TagProfile getProfile(String id) {
        return PROFILES_BY_ID.get(id);
    }

    /**
     * Gets profiles which can decode at least one unknown tag in an SWF.
     *
     * @param swf SWF to inspect
     * @return Matching profiles
     */
    public static List<TagProfile> getMatchingProfiles(SWF swf) {
        List<TagProfile> result = new ArrayList<>();
        for (TagProfile profile : PROFILES) {
            if (profile.supportsSwf(swf) && containsSupportedUnknownTag(swf, profile)) {
                result.add(profile);
            }
        }
        return result;
    }

    private static boolean containsSupportedUnknownTag(Timelined timelined, TagProfile profile) {
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
