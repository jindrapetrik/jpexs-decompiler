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

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.profiles.BioshockSwfProfile;
import com.jpexs.decompiler.flash.profiles.CastleCrashersSwfProfile;
import com.jpexs.decompiler.flash.profiles.SwfProfile;
import com.jpexs.decompiler.flash.profiles.SwfProfiles;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.profiles.bioshock.BioshockImage;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests custom SWF profile selection during SWF parsing.
 *
 * @author JPEXS
 */
public class SwfProfileTest {

    @Test
    public void testBioshockTagIsDecodedOnlyWithProfile() throws Exception {
        byte[] swfData = createSwfWithTag512();
        String titlePrefix = "swf-profile-test-" + UUID.randomUUID();

        SWF standardSwf = new SWF(new ByteArrayInputStream(swfData), titlePrefix + "-standard.swf", titlePrefix + "-standard.swf", false);
        assertTrue(standardSwf.getTags().get(0) instanceof UnknownTag);
        List<SwfProfile> matchingProfiles = SwfProfiles.getMatchingProfiles(standardSwf);
        assertEquals(matchingProfiles.size(), 1);
        assertEquals(matchingProfiles.get(0).getId(), BioshockSwfProfile.ID);

        String profiledTitle = titlePrefix + "-bioshock.swf";
        SWF profiledSwf = new SWF(new ByteArrayInputStream(swfData), profiledTitle, profiledTitle, false, "WINDOWS-1252", SwfProfiles.getProfile(BioshockSwfProfile.ID));
        assertEquals(profiledSwf.getSwfProfile().getId(), BioshockSwfProfile.ID);
        assertTrue(profiledSwf.getTags().get(0) instanceof BioshockImage);
    }

    @Test
    public void testNullProfileUsesStandardTags() throws Exception {
        byte[] swfData = createSwfWithTag512();
        String title = "swf-profile-null-test-" + UUID.randomUUID() + ".swf";
        SWF standardSwf = new SWF(new ByteArrayInputStream(swfData), title, title, false, "WINDOWS-1252", null);
        assertEquals(standardSwf.getSwfProfile(), null);
        assertTrue(standardSwf.getTags().get(0) instanceof UnknownTag);
    }

    @DataProvider(name = "profileActions")
    public Object[][] profileActions() {
        return new Object[][]{
            {"CC_ToBoolean"},
            {"CC_PushGetVariable \"_root\""},
            {"CC_PushGetVariable2 \"_root\", \"value\""},
            {"CC_PushGetMember \"value\""},
            {"CC_PushSetMember \"value\", true"},
            {"CC_PushDefineLocal \"value\", true"},
            {"DefineFunction \"f\", 0 {\nCC_ToBoolean\n}"},
            {"DefineFunction2 \"f\", 0, 1, false, false, false, false, false, false, false, false, false {\nCC_PushGetMember \"value\"\n}"},
            {"With {\nCC_ToBoolean\n}"},
            {"Try \"e\" {\nPush true\n} Catch {\nCC_ToBoolean\n} Finally {\nPush false\n}"}
        };
    }

    @Test(dataProvider = "profileActions")
    public void testUnknownActionsMatchProfile(String source) throws Exception {
        byte[] bytes = Action.actionsToBytes(ASMParser.parse(0, true, source, 6, true, "UTF-8",
                SwfProfiles.getProfile(CastleCrashersSwfProfile.ID)), true, 6);
        SWF swf = createSwfWithActions(bytes);
        List<SwfProfile> matchingProfiles = SwfProfiles.getMatchingProfiles(swf);
        assertEquals(matchingProfiles.size(), 1);
        assertEquals(matchingProfiles.get(0).getId(), CastleCrashersSwfProfile.ID);
    }

    @DataProvider(name = "nonMatchingActions")
    public Object[][] nonMatchingActions() {
        return new Object[][]{
            {new byte[]{0x06, 0}}, // Standard Play action.
            {new byte[]{0x71, 0}}, // Unknown, but unsupported by any profile.
            {new byte[]{(byte) 0x96, 3, 0, 0, 0x70, 0, 0}}, // 0x70 is part of a Push string.
            {new byte[]{(byte) 0xA5, 1, 0, 0x70, 0}}, // 0x70 is an unknown action's payload.
            {new byte[]{0, 0x70}}, // Data after ActionEnd is not an action.
            {new byte[]{(byte) 0xA0, 5, 0, 0}}, // Truncated action.
            {new byte[]{(byte) 0x96}} // Truncated action header.
        };
    }

    @Test(dataProvider = "nonMatchingActions")
    public void testActionsDoNotMatchProfile(byte[] bytes) throws Exception {
        assertTrue(SwfProfiles.getMatchingProfiles(createSwfWithActions(bytes)).isEmpty());
    }

    @Test
    public void testUnknownActionsRequireSupportedSwfVersion() throws Exception {
        SWF swf = createSwfWithActions(new byte[]{0x70, 0});
        swf.version = 7;
        assertTrue(SwfProfiles.getMatchingProfiles(swf).isEmpty());
    }

    @Test
    public void testKnownProfileActionsDoNotMatchProfile() throws Exception {
        SWF swf = createSwfWithActions(new byte[]{0x70, 0});
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        SWF profiledSwf = new SWF(new ByteArrayInputStream(output.toByteArray()), false, "UTF-8",
                SwfProfiles.getProfile(CastleCrashersSwfProfile.ID));
        assertTrue(SwfProfiles.getMatchingProfiles(profiledSwf).isEmpty());
    }

    @Test
    public void testActionAfterFunctionEndMatchesProfile() throws Exception {
        SWF swf = createSwfWithActions(new byte[]{(byte) 0x9B, 5, 0, 0, 0, 0, 1, 0, 0, 0x70, 0});
        assertEquals(SwfProfiles.getMatchingProfiles(swf).get(0).getId(), CastleCrashersSwfProfile.ID);
    }

    @DataProvider(name = "actionLocations")
    public Object[][] actionLocations() {
        return new Object[][]{{"sprite"}, {"init"}, {"button"}, {"button2"}, {"clip"}};
    }

    @Test(dataProvider = "actionLocations")
    public void testUnknownActionsInOtherSourcesMatchProfile(String location) throws Exception {
        SWF swf = new SWF();
        swf.version = 6;
        byte[] bytes = new byte[]{0x70, 0};
        switch (location) {
            case "sprite":
                DefineSpriteTag sprite = new DefineSpriteTag(swf);
                DoActionTag action = new DoActionTag(swf);
                action.setActionBytes(bytes);
                sprite.addTag(action);
                sprite.addTag(new ShowFrameTag(swf));
                sprite.frameCount = 1;
                swf.addTag(sprite);
                break;
            case "init":
                DoInitActionTag init = new DoInitActionTag(swf);
                init.setActionBytes(bytes);
                swf.addTag(init);
                break;
            case "button":
                DefineButtonTag button = new DefineButtonTag(swf);
                button.setActionBytes(bytes);
                swf.addTag(button);
                break;
            case "button2":
                DefineButton2Tag button2 = new DefineButton2Tag(swf);
                BUTTONCONDACTION condition = new BUTTONCONDACTION(swf, button2);
                condition.condOverUpToOverDown = true;
                condition.setActionBytes(bytes);
                button2.actions.add(condition);
                swf.addTag(button2);
                break;
            case "clip":
                CLIPACTIONS clipActions = new CLIPACTIONS();
                PlaceObject2Tag place = new PlaceObject2Tag(swf, false, 1, -1, null, null, -1, null, -1, clipActions);
                CLIPACTIONRECORD record = new CLIPACTIONRECORD(swf, place);
                record.eventFlags.clipEventLoad = true;
                record.setActionBytes(bytes);
                clipActions.clipActionRecords.add(record);
                swf.addTag(place);
                break;
            default:
                throw new IllegalArgumentException(location);
        }
        swf.addTag(new ShowFrameTag(swf));
        List<SwfProfile> matchingProfiles = SwfProfiles.getMatchingProfiles(swf);
        assertEquals(matchingProfiles.size(), 1);
        assertEquals(matchingProfiles.get(0).getId(), CastleCrashersSwfProfile.ID);
    }

    private SWF createSwfWithActions(byte[] bytes) throws Exception {
        SWF swf = new SWF();
        swf.version = 6;
        DoActionTag tag = new DoActionTag(swf);
        tag.setActionBytes(bytes);
        swf.addTag(tag);
        swf.addTag(new ShowFrameTag(swf));
        return swf;
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
