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
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionUnknown;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry of custom SWF profiles.
 *
 * @author JPEXS
 */
public final class SwfProfiles {

    private static final List<SwfProfile> PROFILES = Collections.unmodifiableList(Arrays.asList(
            new BioshockSwfProfile(),
            new CastleCrashersSwfProfile(),
            new IggySwfProfile()
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
     * Gets profiles which can decode at least one unknown tag or action in an SWF.
     *
     * @param swf SWF to inspect
     * @return Matching profiles
     */
    public static List<SwfProfile> getMatchingProfiles(SWF swf) {
        List<SwfProfile> result = new ArrayList<>();
        for (SwfProfile profile : PROFILES) {
            if (profile.supportsSwf(swf) && (containsSupportedUnknownTag(swf, profile)
                    || containsSupportedUnknownAction(swf, profile))) {
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

    private static boolean containsSupportedUnknownAction(SWF swf, SwfProfile profile) {
        List<Integer> supportedActions = profile.getSupportedActions();
        if (supportedActions.isEmpty()) {
            return false;
        }
        for (ASMSource source : swf.getASMs(false).values()) {
            try {
                SWFInputStream sis = new SWFInputStream(swf, source.getActionBytes().getRangeData());
                if (containsSupportedUnknownAction(sis, sis.available(), supportedActions)) {
                    return true;
                }
            } catch (IOException ex) {
                Logger.getLogger(SwfProfiles.class.getName()).log(Level.FINE, "Cannot inspect actions for SWF profiles", ex);
            }
        }
        return false;
    }

    private static boolean containsSupportedUnknownAction(SWFInputStream sis, long end, List<Integer> supportedActions) throws IOException {
        while (sis.getPos() < end) {
            long pos = sis.getPos();
            // Read the original bytecode: ActionListReader replaces long unknown actions with jumps.
            Action action = sis.readAction();
            if (action == null || action instanceof ActionEnd) {
                return false;
            }
            long nextPos = pos + action.getTotalActionLength();
            if (nextPos > end) {
                return false;
            }
            if (action instanceof ActionUnknown && supportedActions.contains(action.getActionCode())) {
                return true;
            }
            if (action instanceof GraphSourceItemContainer) {
                for (long size : ((GraphSourceItemContainer) action).getContainerSizes()) {
                    long containerEnd = nextPos + size;
                    if (containerEnd > end) {
                        return false;
                    }
                    sis.seek(nextPos);
                    if (containsSupportedUnknownAction(sis, containerEnd, supportedActions)) {
                        return true;
                    }
                    nextPos = containerEnd;
                }
            }
            sis.seek(nextPos);
        }
        return false;
    }
}
