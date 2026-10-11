package com.jpexs.decompiler.flash.profiles;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionPushDefineLocal;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionPushGetMember;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionPushGetVariable;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionPushGetVariable2;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionPushSetMember;
import com.jpexs.decompiler.flash.action.profile.castlecrashers.ActionToBoolean;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.profiles.castlecrashers.PixlImage;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Castle Crashers game SWF profile
 *
 * @author JPEXS
 */
public class CastleCrashersSwfProfile implements SwfProfile {

    public static final String ID = "castle_crashers";

    public static final String NAME = "Castle Crashers";

    private static final Map<Integer, Class<? extends Tag>> SUPPORTED_TAGS = new LinkedHashMap<>();

    static {
        SUPPORTED_TAGS.put(PixlImage.ID, PixlImage.class);
        SUPPORTED_TAGS.put(PixlImage.ID_INTERNAL, PixlImage.class);
    }

    public CastleCrashersSwfProfile() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Tag decodeTag(SWFInputStream sis, int tagId, ByteArrayRange data) throws IOException {
        if ((tagId == PixlImage.ID || tagId == PixlImage.ID_INTERNAL) && sis.available() > 0) {
            return new PixlImage(tagId, sis, data);
        }
        return null;
    }

    @Override
    public Map<Integer, Class<? extends Tag>> getSupportedTags() {
        return SUPPORTED_TAGS;
    }

    @Override
    public boolean supportsTagId(int tagId) {
        return SUPPORTED_TAGS.containsKey(tagId);
    }

    @Override
    public Class<? extends Tag> getTagIdClass(int tagId) {
        return SUPPORTED_TAGS.get(tagId);
    }

    @Override
    public boolean supportsSwf(SWF swf) {
        return true;
    }

    @Override
    public boolean prefersAvm1DefineFunction2() {
        return true;
    }

    @Override
    public List<Integer> getSupportedActions() {
        return Arrays.asList(0x70, 0xA0, 0xA1, 0xA2, 0xA3, 0xA4);
    }

    @Override
    public Action parseAction(String instructionName, FlasmLexer lexer, List<String> constantPool, int version, String charset) throws IOException, ActionParseException {
        if (instructionName.compareToIgnoreCase("CC_ToBoolean") == 0) {
            return new ActionToBoolean();
        }
        if (instructionName.compareToIgnoreCase("CC_PushGetVariable") == 0) {
            return new ActionPushGetVariable(lexer, constantPool, charset);
        }
        if (instructionName.compareToIgnoreCase("CC_PushGetVariable2") == 0) {
            return new ActionPushGetVariable2(lexer, constantPool, charset);
        }
        if (instructionName.compareToIgnoreCase("CC_PushGetMember") == 0) {
            return new ActionPushGetMember(lexer, constantPool, charset);
        }
        if (instructionName.compareToIgnoreCase("CC_PushSetMember") == 0) {
            return new ActionPushSetMember(lexer, constantPool, charset);
        }
        if (instructionName.compareToIgnoreCase("CC_PushDefineLocal") == 0) {
            return new ActionPushDefineLocal(lexer, constantPool, charset);
        }
        return null;
    }

    @Override
    public Action readAction(int actionCode, int actionLength, SWFInputStream sis, String charset) throws IOException {
        switch (actionCode) {
            case 0x70:
                return new ActionToBoolean();
            case 0xA0:
                return new ActionPushGetVariable(actionLength, sis, charset);
            case 0xA1:
                return new ActionPushGetVariable2(actionLength, sis, charset);
            case 0xA2:
                return new ActionPushGetMember(actionLength, sis, charset);
            case 0xA3:
                return new ActionPushSetMember(actionLength, sis, charset);
            case 0xA4:
                return new ActionPushDefineLocal(actionLength, sis, charset);
        }
        return null;
    }
}
