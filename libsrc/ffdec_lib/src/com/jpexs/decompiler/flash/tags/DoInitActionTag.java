/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ConstantPoolTooBigException;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 6)
public class DoInitActionTag extends Tag implements CharacterIdTag, ASMSource {

    public static final int ID = 59;

    public static final String NAME = "DoInitAction";

    /**
     * Identifier of Sprite
     */
    @SWFType(BasicType.UI16)
    public int spriteId = 0;

    /**
     * List of actions to perform
     */
    @HideInRawEdit
    public ByteArrayRange actionBytes;

    @Internal
    private String scriptName = "-";

    @Override
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DoInitActionTag(SWF swf) {
        super(swf, ID, NAME, null);
        actionBytes = ByteArrayRange.EMPTY;
    }

    @Override
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DoInitActionTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        spriteId = sis.readUI16("spriteId");
        actionBytes = sis.readByteRangeEx(sis.available(), "actionBytes", DumpInfoSpecialType.ACTION_BYTES, sis.getPos());
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(spriteId);
        sos.write(getActionBytes());
        //sos.write(Action.actionsToBytes(actions, true, version));
    }

    /**
     * Whether or not this object contains ASM source
     *
     * @return True when contains
     */
    @Override
    public boolean containsSource() {
        return true;
    }

    /**
     * Converts actions to ASM source
     *
     * @param exportMode PCode or hex?
     * @param writer
     * @param actions
     * @return ASM source
     * @throws java.lang.InterruptedException
     */
    @Override
    public GraphTextWriter getASMSource(ScriptExportMode exportMode, GraphTextWriter writer, ActionList actions) throws InterruptedException {
        if (actions == null) {
            actions = getActions();
        }
        return Action.actionsToString(listeners, 0, actions, swf.version, exportMode, writer);
    }

    @Override
    public GraphTextWriter getActionScriptSource(GraphTextWriter writer, ActionList actions) throws InterruptedException {
        if (actions == null) {
            actions = getActions();
        }

        return Action.actionsToSource(this, actions, getScriptName(), writer);
    }

    @Override
    public ActionList getActions() throws InterruptedException {
        return SWF.getCachedActionList(this, listeners);
    }

    @Override
    public void setActions(List<Action> actions) {
        actionBytes = Action.actionsToByteArrayRange(actions, true, swf.version);
    }

    @Override
    public ByteArrayRange getActionBytes() {
        return actionBytes;
    }

    @Override
    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes = new ByteArrayRange(actionBytes);
        SWF.uncache(this);
    }

    @Override
    public void setConstantPools(List<List<String>> constantPools) throws ConstantPoolTooBigException {
        Action.setConstantPools(this, constantPools, false);
    }

    @Override
    public void setModified() {
        setModified(true);
    }

    @Override
    public GraphTextWriter getActionBytesAsHex(GraphTextWriter writer) {
        return Helper.byteArrayToHexWithHeader(writer, actionBytes.getRangeData());
    }

    @Override
    public int getCharacterId() {
        return spriteId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.spriteId = characterId;
    }

    List<DisassemblyListener> listeners = new ArrayList<>();

    @Override
    public void addDisassemblyListener(DisassemblyListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeDisassemblyListener(DisassemblyListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getExportFileName() {
        String expName = swf == null ? "" : swf.getExportName(spriteId);
        if (expName == null || expName.isEmpty()) {
            return super.getExportFileName();
        }
        String[] pathParts = expName.contains(".") ? expName.split("\\.") : new String[]{expName};
        return pathParts[pathParts.length - 1];
    }

    @Override
    public String getName() {
        String expName = swf == null ? "" : swf.getExportName(spriteId);
        if (expName == null || expName.isEmpty()) {
            return super.getName();
        }
        String[] pathParts = expName.contains(".") ? expName.split("\\.") : new String[]{expName};
        return pathParts[pathParts.length - 1];
    }

    @Override
    public GraphTextWriter getActionSourcePrefix(GraphTextWriter writer) {
        return writer;
    }

    @Override
    public GraphTextWriter getActionSourceSuffix(GraphTextWriter writer) {
        return writer;
    }

    @Override
    public int getPrefixLineCount() {
        return 0;
    }

    @Override
    public String removePrefixAndSuffix(String source) {
        return source;
    }

    @Override
    public Tag getSourceTag() {
        return this;
    }

    @Override
    public void setSourceTag(Tag t) {
        //nothing
    }
}
