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

import com.jpexs.decompiler.flash.SWFLimitedInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.Cache;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends the capabilities of DefineButton by allowing any state transition to
 * trigger actions
 *
 * @author JPEXS
 */
public class DefineButton2Tag extends ButtonTag implements Container {

    /**
     * ID for this character
     */
    @SWFType(BasicType.UI16)
    public int buttonId;

    @Reserved
    @SWFType(value = BasicType.UB, count = 7)
    public int reserved;

    /**
     * Track as menu button
     */
    public boolean trackAsMenu;
    /**
     * Characters that make up the button
     */
    public List<BUTTONRECORD> characters;
    /**
     * Actions to execute at particular button events
     */
    public List<BUTTONCONDACTION> actions = new ArrayList<>();
    public static final int ID = 34;

    private Timeline timeline;

    private boolean isSingleFrameInitialized;
    private boolean isSingleFrame;

    @Override
    public int getCharacterId() {
        return buttonId;
    }

    @Override
    public List<BUTTONRECORD> getRecords() {
        return characters;
    }

    /**
     * Constructor
     *
     * @param swf
     * @param headerData
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public DefineButton2Tag(SWFLimitedInputStream sis, long pos, int length) throws IOException {
        super(sis.swf, ID, "DefineButton2", pos, length);
        buttonId = sis.readUI16();
        reserved = (int) sis.readUB(7);
        trackAsMenu = sis.readUB(1) == 1;
        int actionOffset = sis.readUI16();
        characters = sis.readBUTTONRECORDList(true);
        if (actionOffset > 0) {
            actions = sis.readBUTTONCONDACTIONList(swf, this);
        }
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Configuration.debugCopy.get()) {
            ByteArrayInputStream bais = new ByteArrayInputStream(getOriginalData());
            os = new CopyOutputStream(os, bais);
        }
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(buttonId);
            sos.writeUB(7, reserved);
            sos.writeUB(1, trackAsMenu ? 1 : 0);

            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            try (SWFOutputStream sos2 = new SWFOutputStream(baos2, getVersion())) {
                sos2.writeBUTTONRECORDList(characters, true);
            }
            byte[] brdata = baos2.toByteArray();
            if ((actions == null) || (actions.isEmpty())) {
                sos.writeUI16(0);
            } else {
                sos.writeUI16(2 + brdata.length);
            }
            sos.write(brdata);
            sos.writeBUTTONCONDACTIONList(actions);
            sos.close();
        } catch (IOException e) {
            Logger.getLogger(DefineButton2Tag.class.getName()).log(Level.SEVERE, null, e);
        }
        return baos.toByteArray();
    }

    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    @Override
    public List<ContainerItem> getSubItems() {
        List<ContainerItem> ret = new ArrayList<>();
        ret.addAll(actions);
        return ret;
    }

    /**
     * Returns number of sub-items
     *
     * @return Number of sub-items
     */
    @Override
    public int getItemCount() {
        return actions.size();
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        for (BUTTONRECORD r : characters) {
            needed.add(r.characterId);
        }
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (int i = 0; i < characters.size(); i++) {
            if (characters.get(i).characterId == characterId) {
                characters.remove(i);
                modified = true;
                i--;
            }
        }
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    private static final Cache<RECT> rectCache = Cache.getInstance(true);

    @Override
    public RECT getRect() {
        if (rectCache.contains(this)) {
            return (RECT) rectCache.get(this);
        }
        RECT rect = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (BUTTONRECORD r : characters) {
            CharacterTag ch = swf.characters.get(r.characterId);
            if (ch instanceof BoundedTag) {
                RECT r2 = ((BoundedTag) ch).getRect();
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
        rectCache.put(this, rect);
        return rect;
    }

    @Override
    public boolean trackAsMenu() {
        return trackAsMenu;
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        if (!isSingleFrameInitialized) {
            initialiteIsSingleFrame();
        }
        return isSingleFrame;
    }

    private synchronized void initialiteIsSingleFrame() {
        if (!isSingleFrameInitialized) {
            isSingleFrame = getTimeline().isSingleFrame();
            isSingleFrameInitialized = true;
        }
    }

    @Override
    public Timeline getTimeline() {
        if (timeline != null) {
            return timeline;
        }
        timeline = new Timeline(swf, new ArrayList<Tag>(), buttonId, getRect());

        int maxDepth = 0;
        Frame frameUp = new Frame(timeline);
        Frame frameDown = new Frame(timeline);
        Frame frameOver = new Frame(timeline);
        Frame frameHit = new Frame(timeline);
        for (BUTTONRECORD r : this.characters) {

            DepthState layer = new DepthState(swf, null);
            layer.colorTransForm = r.colorTransform;
            layer.blendMode = r.blendMode;
            layer.filters = r.filterList;
            layer.matrix = r.placeMatrix;
            layer.characterId = r.characterId;
            if (r.placeDepth > maxDepth) {
                maxDepth = r.placeDepth;
            }

            if (r.buttonStateUp) {
                frameUp.layers.put(r.placeDepth, new DepthState(layer, frameUp, false));
            }
            if (r.buttonStateDown) {
                frameDown.layers.put(r.placeDepth, new DepthState(layer, frameDown, false));
            }
            if (r.buttonStateOver) {
                frameOver.layers.put(r.placeDepth, new DepthState(layer, frameOver, false));
            }
            if (r.buttonStateHitTest) {
                frameHit.layers.put(r.placeDepth, new DepthState(layer, frameHit, false));
            }

        }
        timeline.frames.add(frameUp);
        if (frameOver.layers.isEmpty()) {
            frameOver = frameUp;
        }
        timeline.frames.add(frameOver);
        if (frameDown.layers.isEmpty()) {
            frameDown = frameOver;
        }
        timeline.frames.add(frameDown);
        if (frameHit.layers.isEmpty()) {
            frameHit = frameUp;
        }
        timeline.frames.add(frameHit);
        return timeline;
    }

    @Override
    public void resetTimeline() {
        timeline = null;
    }
}
