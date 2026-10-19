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
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract SWF profile.
 *
 * @author JPEXS
 */
public abstract class AbstractSwfProfile implements SwfProfile {

    protected final String id;
    protected final String name;

    public AbstractSwfProfile(String id, String name) {
        this.id = id;
        this.name = name;        
    }        

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override    
    public Tag decodeTag(SWFInputStream sis, int tagId, ByteArrayRange data) throws IOException {
        return null;
    }

    @Override
    public List<Integer> getSupportedActions() {
        return new ArrayList<>();
    }

    @Override
    public Map<Integer, Class<? extends Tag>> getSupportedTags() {
        return new HashMap<>();
    }

    @Override
    public Class<? extends Tag> getTagIdClass(int tagId) {
        return null;
    }

    @Override
    public boolean supportsTagId(int tagId) {
        return false;
    }
        
    @Override
    public Action parseAction(String instructionName, FlasmLexer lexer, List<String> constantPool, int version, String charset) throws IOException, ActionParseException {
        return null;
    }

    @Override
    public Action readAction(int actionCode, int actionLength, SWFInputStream sis, String charset) throws IOException {
        return null;
    }

    @Override
    public boolean supportsSwf(SWF swf) {
        return true;
    }

    @Override
    public boolean prefersAvm1DefineFunction2() {
        return false;
    }

    @Override
    public boolean prefersNoVariablesInAvm1Functions() {
        return false;
    }       
}
