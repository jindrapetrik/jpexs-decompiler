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
package com.jpexs.decompiler.flash;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.profiles.BioshockTagProfile;
import com.jpexs.decompiler.flash.tags.profiles.TagProfile;
import com.jpexs.decompiler.flash.tags.profiles.TagProfiles;
import com.jpexs.decompiler.flash.tags.profiles.bioshock.BioshockImage;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import org.testng.annotations.Test;

/**
 * Tests custom tag profile selection during SWF parsing.
 *
 * @author JPEXS
 */
public class TagProfileTest {

    @Test
    public void testBioshockTagIsDecodedOnlyWithProfile() throws Exception {
        byte[] swfData = createSwfWithTag512();
        String titlePrefix = "tag-profile-test-" + UUID.randomUUID();

        SWF standardSwf = new SWF(new ByteArrayInputStream(swfData), titlePrefix + "-standard.swf", titlePrefix + "-standard.swf", false);
        assertTrue(standardSwf.getTags().get(0) instanceof UnknownTag);
        List<TagProfile> matchingProfiles = TagProfiles.getMatchingProfiles(standardSwf);
        assertEquals(matchingProfiles.size(), 1);
        assertEquals(matchingProfiles.get(0).getId(), BioshockTagProfile.ID);

        String profiledTitle = titlePrefix + "-bioshock.swf";
        SWF profiledSwf = new SWF(new ByteArrayInputStream(swfData), profiledTitle, profiledTitle, false, "WINDOWS-1252", TagProfiles.getProfile(BioshockTagProfile.ID));
        assertEquals(profiledSwf.getTagProfile().getId(), BioshockTagProfile.ID);
        assertTrue(profiledSwf.getTags().get(0) instanceof BioshockImage);
    }

    @Test
    public void testNullProfileUsesStandardTags() throws Exception {
        byte[] swfData = createSwfWithTag512();
        String title = "tag-profile-null-test-" + UUID.randomUUID() + ".swf";
        SWF standardSwf = new SWF(new ByteArrayInputStream(swfData), title, title, false, "WINDOWS-1252", null);
        assertEquals(standardSwf.getTagProfile(), null);
        assertTrue(standardSwf.getTags().get(0) instanceof UnknownTag);
    }

    private byte[] createSwfWithTag512() throws Exception {
        SWF swf = new SWF();
        UnknownTag tag = new UnknownTag(swf, BioshockImage.ID);
        tag.unknownData = new ByteArrayRange(new byte[]{
            1, 0,       // character ID
            4, 0,       // width
            4, 0,       // height
            4, 0, 0, 0, // DXT5 format
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
        });
        swf.addTag(tag);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        return output.toByteArray();
    }
}
