/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.Layer;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.ReReadableInputStream;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a button character
 *
 * @author JPEXS
 */
public class DefineButtonTag extends CharacterTag implements ASMSource, BoundedTag, ButtonTag {

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
    public static final int ID = 7;

    @Override
    public int getCharacterId() {
        return buttonId;
    }
    private long hdrSize;

    @Override
    public List<BUTTONRECORD> getRecords() {
        return characters;
    }

    /**
     * Constructor
     *
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DefineButtonTag(SWF swf, byte data[], int version, long pos) throws IOException {
        super(swf, ID, "DefineButton", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        buttonId = sis.readUI16();
        characters = sis.readBUTTONRECORDList(false);
        //actions = sis.readActionList();
        hdrSize = sis.getPos();
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
        if (Configuration.DISABLE_DANGEROUS) {
            return super.getData(version);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Configuration.DEBUG_COPY) {
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
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @return ASM source
     */
    @Override
    public String getASMSource(int version, boolean hex) {
        return Action.actionsToString(listeners, 0, getActions(version), null, version, hex, getPos() + hdrSize, toString()/*FIXME?*/);
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
     * Returns actions associated with this object
     *
     * @param version Version
     * @return List of actions
     */
    @Override
    public List<Action> getActions(int version) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int prevLength = 0;
            if (previousTag != null) {
                byte prevData[] = previousTag.getData(version);
                baos.write(prevData);
                prevLength = prevData.length;
            }
            baos.write(actionBytes);
            ReReadableInputStream rri = new ReReadableInputStream(new ByteArrayInputStream(baos.toByteArray()));
            rri.setPos(prevLength);

            boolean deobfuscate = Configuration.getConfig("autoDeobfuscate", true);
            List<Action> list = SWFInputStream.readActionList(listeners, 0, getPos() + hdrSize - prevLength, rri, version, prevLength, -1, toString()/*FIXME?*/);
            if (deobfuscate) {
                list = Action.removeNops(0, list, version, getPos() + hdrSize, toString()/*FIXME?*/);
            }
            return list;
        } catch (Exception ex) {
            Logger.getLogger(DoActionTag.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<>();
        }
    }

    @Override
    public void setActions(List<Action> actions, int version) {
        actionBytes = Action.actionsToBytes(actions, true, version);
    }

    @Override
    public byte[] getActionBytes() {
        return actionBytes;
    }

    @Override
    public void setActionBytes(byte[] actionBytes) {
        this.actionBytes = actionBytes;
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> needed = new HashSet<>();
        for (BUTTONRECORD r : characters) {
            needed.add(r.characterId);
        }
        return needed;
    }
    private static Cache rectCache = Cache.getInstance(true);

    @Override
    public RECT getRect(HashMap<Integer, CharacterTag> allCharacters, Stack<Integer> visited) {
        if (rectCache.contains(this)) {
            return (RECT) rectCache.get(this);
        }
        if (visited.contains(buttonId)) {
            return new RECT();
        }
        visited.push(buttonId);
        RECT rect = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (BUTTONRECORD r : characters) {
            CharacterTag ch = allCharacters.get(r.characterId);
            if (ch instanceof BoundedTag) {
                if (visited.contains(ch.getCharacterId())) {
                    continue;
                }
                RECT r2 = ((BoundedTag) ch).getRect(allCharacters, visited);
                MATRIX mat = r.placeMatrix;
                if (mat != null) {
                    r2 = mat.apply(r2);
                }
                rect.Xmin = Math.min(r2.Xmin, rect.Xmin);
                rect.Ymin = Math.min(r2.Ymin, rect.Ymin);
                rect.Xmax = Math.max(r2.Xmax, rect.Xmax);
                rect.Ymax = Math.max(r2.Ymax, rect.Ymax);
            }
        }
        visited.pop();
        rectCache.put(this, rect);
        return rect;
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
    public boolean trackAsMenu() {
        return false;
    }

    @Override
    public BufferedImage toImage(int frame, List<Tag> tags, RECT displayRect, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        if (visited.contains(buttonId)) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        }
        visited.push(buttonId);
        HashMap<Integer, Layer> layers = new HashMap<>();
        int maxDepth = 0;
        for (BUTTONRECORD r : this.characters) {
            if (r.buttonStateUp) {
                Layer layer = new Layer();
                layer.colorTransFormAlpha = r.colorTransform;
                layer.blendMode = r.blendMode;
                layer.filters = r.filterList;
                layer.matrix = r.placeMatrix;
                layer.characterId = r.characterId;
                if (r.placeDepth > maxDepth) {
                    maxDepth = r.placeDepth;
                }
                layers.put(r.placeDepth, layer);
            }
        }
        visited.pop();
        displayRect = getRect(characters, visited);
        visited.push(buttonId);
        BufferedImage ret = SWF.frameToImage(buttonId, maxDepth, layers, new Color(0, 0, 0, 0), characters, 1, tags, tags, displayRect, visited);
        visited.pop();
        return ret;
    }

    @Override
    public Point getImagePos(int frame, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        RECT r = getRect(characters, visited);
        return new Point(r.Xmin / 20, r.Ymin / 20);
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public String getActionSourcePrefix() {
        return "";
    }

    @Override
    public String getActionSourceSuffix() {
        return "";
    }

    @Override
    public int getActionSourceIndent() {
        return 0;
    }
}
