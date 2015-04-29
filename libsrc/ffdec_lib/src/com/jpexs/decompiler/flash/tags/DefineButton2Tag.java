/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
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
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Extends the capabilities of DefineButton by allowing any state transition to
 * trigger actions
 *
 * @author JPEXS
 */
public class DefineButton2Tag extends ButtonTag implements ASMSourceContainer {

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

    private static final Cache<DefineButton2Tag, RECT> rectCache = Cache.getInstance(true, true, "rect_button2");

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
     */
    public DefineButton2Tag(SWF swf) {
        super(swf, ID, "DefineButton2", null);
        buttonId = swf.getNextCharacterId();
        characters = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineButton2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineButton2", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        buttonId = sis.readUI16("buttonId");
        reserved = (int) sis.readUB(7, "reserved");
        trackAsMenu = sis.readUB(1, "trackAsMenu") == 1;
        int actionOffset = sis.readUI16("actionOffset");
        characters = sis.readBUTTONRECORDList(true, "characters");
        if (actionOffset > 0) {
            actions = sis.readBUTTONCONDACTIONList(swf, this, "actions");
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
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    @Override
    public List<BUTTONCONDACTION> getSubItems() {
        return actions;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        for (BUTTONRECORD r : characters) {
            needed.add(r.characterId);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (int i = 0; i < characters.size(); i++) {
            BUTTONRECORD character = characters.get(i);
            if (character.characterId == oldCharacterId) {
                character.characterId = newCharacterId;
                modified = true;
            }
        }
        if (modified) {
            setModified(true);
        }
        return modified;
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

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        if (rectCache.contains(this)) {
            return rectCache.get(this);
        }
        RECT rect = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (BUTTONRECORD r : characters) {
            CharacterTag ch = swf.getCharacter(r.characterId);
            if (ch instanceof BoundedTag) {
                BoundedTag bt = (BoundedTag) ch;
                if (!added.contains(bt)) {
                    added.add(bt);
                    RECT r2 = bt.getRect(added);
                    added.remove(bt);
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
        }
        rectCache.put(this, rect);
        return rect;
    }

    public static void clearCache() {
        rectCache.clear();
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

        timeline = new Timeline(swf, this, new ArrayList<Tag>(), buttonId, getRect());
        initTimeline(timeline);
        return timeline;
    }

    @Override
    public void resetTimeline() {
        if (timeline != null) {
            timeline.reset(swf, this, new ArrayList<Tag>(), buttonId, getRect());
            initTimeline(timeline);
        }
    }

    private void initTimeline(Timeline timeline) {
        int maxDepth = 0;
        Frame frameUp = new Frame(timeline, 0);
        Frame frameDown = new Frame(timeline, 0);
        Frame frameOver = new Frame(timeline, 0);
        Frame frameHit = new Frame(timeline, 0);
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

        timeline.addFrame(frameUp);

        if (frameOver.layers.isEmpty()) {
            frameOver = frameUp;
        }

        timeline.addFrame(frameOver);

        if (frameDown.layers.isEmpty()) {
            frameDown = frameOver;
        }

        timeline.addFrame(frameDown);

        if (frameHit.layers.isEmpty()) {
            frameHit = frameUp;
        }

        timeline.addFrame(frameHit);
    }

    @Override
    public void setCharacterId(int characterId) {
        this.buttonId = characterId;
    }
}
