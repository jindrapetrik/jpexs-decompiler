/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.tags;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.types.BUTTONRECORD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a button character
 *
 * @author JPEXS
 */
public class DefineButtonTag extends Tag implements ASMSource {
    /**
     * ID for this character
     */
    public int buttonId;
    /**
     * Characters that make up the button
     */
    public List<BUTTONRECORD> characters;
    /**
     * Actions to perform
     */
    //public List<Action> actions;
    public byte[] actionBytes;

    /**
     * List of ExportAssetsTag used for converting to String
     */
    public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();


    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DefineButtonTag(byte[] data, int version, long pos) throws IOException {
        super(7, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        buttonId = sis.readUI16();
        characters = sis.readBUTTONRECORDList(false);
        //actions = sis.readActionList();
        actionBytes = sis.readBytes(sis.available());
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        if (Main.DISABLE_DANGEROUS) return super.getData(version);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Main.DEBUG_COPY) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
        }
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUI16(buttonId);
            sos.writeBUTTONRECORDList(characters, false);
            sos.write(actionBytes);
            //sos.write(Action.actionsToBytes(actions, true, version));
            sos.close();
        } catch (IOException e) {

        }
        return baos.toByteArray();
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        String name = "";
        for (ExportAssetsTag eat : exportAssetsTags) {
            if (eat.assets.containsKey(buttonId)) {
                name = ": " + eat.assets.get((Integer) buttonId);
            }
        }
        return "DefineButtonTag (" + buttonId + name + ")";
    }

    /**
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @return ASM source
     */
    public String getASMSource(int version) {
        List<Action> actions=new ArrayList<Action>();
        try {
            actions = (new SWFInputStream(new ByteArrayInputStream(actionBytes), version)).readActionList();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        return Action.actionsToString(actions, null, version);
    }

    /**
     * Whether or not this object contains ASM source
     *
     * @return True when contains
     */
    public boolean containsSource() {
        return true;
    }


/**
     * Returns actions associated with this object
     * @param version Version
     * @return List of actions
     */
    public List<Action> getActions(int version) {
        try {
            return (new SWFInputStream(new ByteArrayInputStream(actionBytes), version)).readActionList();
        } catch (IOException ex) {
            return new ArrayList<Action>();
        }
    }

    public void setActions(List<Action> actions,int version) {
        actionBytes=Action.actionsToBytes(actions, true, version);
    }

    public byte[] getActionBytes() {
        return actionBytes;
    }

    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes=actionBytes;
    }
}
