/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoInitActionTag extends CharacterIdTag implements ASMSource {

    /**
     * Identifier of Sprite
     */
    @SWFType(BasicType.UI16)
    public int spriteId = 0;
    /**
     * List of actions to perform
     */
    //public List<Action> actions = new ArrayList<Action>();
    @Internal
    public ByteArrayRange actionBytes;
    public static final int ID = 59;

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DoInitActionTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DoInitAction", data);
        spriteId = sis.readUI16("spriteId");
        int pos = (int) sis.getPos();
        byte[] bytes = sis.readBytesEx(sis.available(), "actionBytes");
        actionBytes = new ByteArrayRange(data.array, pos, bytes.length);
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, getVersion());
        try {
            sos.writeUI16(spriteId);
            sos.write(getActionBytes());
            //sos.write(Action.actionsToBytes(actions, true, version));
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
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
    public ActionList getActions() throws InterruptedException {
        try {
            int prevLength = actionBytes.pos;
            SWFInputStream rri = new SWFInputStream(swf, actionBytes.array);
            if (prevLength != 0) {
                rri.seek(prevLength);
            }
            ActionList list = ActionListReader.readActionListTimeout(listeners, rri, getVersion(), prevLength, prevLength + actionBytes.length, toString()/*FIXME?*/);
            return list;
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger(DoActionTag.class.getName()).log(Level.SEVERE, null, ex);
            return new ActionList();
        }
    }

    @Override
    public void setActions(List<Action> actions) {
        byte[] bytes = Action.actionsToBytes(actions, true, swf.version);
        actionBytes = new ByteArrayRange(bytes, 0, bytes.length);
    }

    @Override
    public byte[] getActionBytes() {
        return actionBytes.getRangeData();
    }

    @Override
    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes = new ByteArrayRange(actionBytes);
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
        String expName = getExportName();
        if ((expName == null) || expName.isEmpty()) {
            return super.toString();
        }
        String[] pathParts;
        if (expName.contains(".")) {
            pathParts = expName.split("\\.");
        } else {
            pathParts = new String[]{expName};
        }
        return pathParts[pathParts.length - 1];
    }

    @Override
    public String toString() {
        String expName = getExportName();
        if ((expName == null) || expName.isEmpty()) {
            return super.toString();
        }
        String[] pathParts;
        if (expName.contains(".")) {
            pathParts = expName.split("\\.");
        } else {
            pathParts = new String[]{expName};
        }
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

}
