/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.tags;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DoInitActionTag extends Tag implements ASMSource {
    /**
     * Identifier of Sprite
     */
    public int spriteId = 0;
    /**
     * List of actions to perform
     */
    public List<Action> actions = new ArrayList<Action>();
    /**
     * List of ExportAssetsTag used for converting to String
     */
    public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();

    /**
     * Sets actions associated with this object
     *
     * @param actions Action list
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }


    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DoInitActionTag(byte[] data, int version) {
        super(59, data);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            SWFInputStream sis = new SWFInputStream(bais, version);
            spriteId = sis.readUI16();
            actions = sis.readActionList();
        } catch (IOException e) {

        }
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI16(spriteId);
            sos.write(Action.actionsToBytes(actions, true, version));
            sos.close();
        } catch (IOException e) {

        }
        return baos.toByteArray();
    }

    /**
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @return ASM source
     */
    public String getASMSource(int version) {
        return Action.actionsToString(actions, null, version);
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
            if (eat.assets.containsKey(spriteId)) {
                name = ": " + eat.assets.get((Integer) spriteId);
            }
        }
        return "DoInitActionTag (" + spriteId + name + ")";
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
     *
     * @return List of actions
     */
    public List<Action> getActions() {
        return actions;
    }
}
