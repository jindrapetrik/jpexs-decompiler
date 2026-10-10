package com.jpexs.decompiler.flash.profiles;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Castle Crashers game SWF profile
 * @author JPEXS
 */
public class CastleCrashersSwfProfile implements SwfProfile {
    public static final String ID = "castle_crashers";
    
    public static final String NAME = "Castle Crashers";
    
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
        return null;
    }

    @Override
    public Map<Integer, Class<? extends Tag>> getSupportedTags() {
        return new HashMap<>();
    }

    @Override
    public boolean supportsTagId(int tagId) {
        return false;
    }

    @Override
    public Class<? extends Tag> getTagIdClass(int tagId) {
        return null;
    }

    @Override
    public boolean supportsSwf(SWF swf) {
        return swf.version == 6;
    }    

    @Override
    public boolean prefersAvm1DefineFunction2() {
        return true;
    }        
}
