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
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a button character
 *
 * @author JPEXS
 */
public class DefineButtonTag extends ButtonTag implements ASMSource {

    /**
     * ID for this character
     */
    @SWFType(BasicType.UI16)
    public int buttonId;
    /**
     * Characters that make up the button
     */
    public List<BUTTONRECORD> characters;
    /**
     * Actions to perform
     */
    //public List<Action> actions;
    @Internal
    public ByteArrayRange actionBytes;
    public static final int ID = 7;

    @Override
    public int getCharacterId() {
        return buttonId;
    }

    private Timeline timeline;

    private boolean isSingleFrameInitialized;
    private boolean isSingleFrame;

    @Override
    public List<BUTTONRECORD> getRecords() {
        return characters;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineButtonTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineButton", data);
        buttonId = sis.readUI16("buttonId");
        characters = sis.readBUTTONRECORDList(false, "characters");
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
        OutputStream os = baos;
        if (Configuration.debugCopy.get()) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(getOriginalData().getRangeData()));
        }
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(buttonId);
            sos.writeBUTTONRECORDList(characters, false);
            sos.write(getActionBytes());
            //sos.write(Action.actionsToBytes(actions, true, version));
            sos.close();
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Converts actions to ASM source
     *
     * @param actions
     * @param writer
     * @return ASM source
     * @throws java.lang.InterruptedException
     */
    @Override
    public GraphTextWriter getASMSource(ScriptExportMode exportMode, GraphTextWriter writer, List<Action> actions) throws InterruptedException {
        if (actions == null) {
            actions = getActions();
        }
        return Action.actionsToString(listeners, 0, actions, swf.version, exportMode, writer);
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
     * @return List of actions
     * @throws java.lang.InterruptedException
     */
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
    public RECT getRect(Set<BoundedTag> added) {
        if (rectCache.contains(this)) {
            return rectCache.get(this);
        }
        RECT rect = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (BUTTONRECORD r : characters) {
            CharacterTag ch = swf.characters.get(r.characterId);
            if (ch instanceof BoundedTag) {
                BoundedTag bt = (BoundedTag) ch;
                if (!added.contains(bt)) {
                    added.add(bt);
                    RECT r2 = bt.getRect(added);
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
    public Timeline getTimeline() {
        if (timeline != null) {
            return timeline;
        }
        timeline = new Timeline(swf, new ArrayList<Tag>(), buttonId, getRect(new HashSet<BoundedTag>()));

        ColorTransform clrTrans = null;
        for (Tag t : swf.tags) {
            if (t instanceof DefineButtonCxformTag) {
                DefineButtonCxformTag cx = (DefineButtonCxformTag) t;
                clrTrans = cx.buttonColorTransform;
            }
        }
        int maxDepth = 0;
        Frame frameUp = new Frame(timeline);
        Frame frameDown = new Frame(timeline);
        Frame frameOver = new Frame(timeline);
        Frame frameHit = new Frame(timeline);
        for (BUTTONRECORD r : this.characters) {

            DepthState layer = new DepthState(swf, null);
            layer.colorTransForm = clrTrans;
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

    @Override
    public Tag getSourceTag() {
        return this;
    }
}
