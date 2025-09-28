/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.ActionTreeOperation;
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
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DoInitAction tag - Instructs Flash Player to perform a list of actions when a
 * sprite is initialized.
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

    @Internal
    private String exportedScriptName = "-";

    @Override
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Constructor
     *
     * @param swf SWF
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
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
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
     * @throws IOException On I/O error
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
     * @param writer Writer
     * @param actions Actions
     * @return ASM source
     * @throws InterruptedException On interrupt
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

        return Action.actionsToSource(requiresUninitializedClassTraitsDetection() ? swf.getUninitializedAs2ClassTraits() : new HashMap<>(), this, actions, getScriptName(), writer, getCharset());
    }

    @Override
    public GraphTextWriter getActionScriptSource(GraphTextWriter writer, ActionList actions, List<ActionTreeOperation> treeOperations) throws InterruptedException {
        if (actions == null) {
            actions = getActions();
        }

        return Action.actionsToSource(requiresUninitializedClassTraitsDetection() ? swf.getUninitializedAs2ClassTraits() : new HashMap<>(), this, actions, getScriptName(), writer, getCharset(), treeOperations);
    }

    private boolean requiresUninitializedClassTraitsDetection() {
        return swf.needsCalculatingAS2UninitializeClassTraits(this);
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
    public Map<String, String> getNameProperties() {
        String exportName = swf == null ? "" : swf.getExportName(spriteId);

        Map<String, String> ret = super.getNameProperties();
        ret.put("sid", "" + spriteId);

        if (exportName == null || exportName.isEmpty()) {
            return ret;
        }
        ret.put("exp", Helper.escapeExportname(getSwf(), exportName, true));
        return ret;
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

    @Override
    public Tag getTag() {
        return null; //?
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        needed.add(spriteId);
    }

    @Override
    public List<GraphTargetItem> getActionsToTree() {
        try {
            return Action.actionsToTree(new LinkedHashSet<>(), requiresUninitializedClassTraitsDetection(), requiresUninitializedClassTraitsDetection() ? swf.getUninitializedAs2ClassTraits() : new HashMap<>(), true, false, getActions(), swf.version, 0, getScriptName(), swf.getCharset());
        } catch (InterruptedException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public String getExportedScriptName() {
        return exportedScriptName;
    }

    @Override
    public void setExportedScriptName(String scriptName) {
        this.exportedScriptName = scriptName;
    }
}
