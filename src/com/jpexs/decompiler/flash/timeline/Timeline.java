/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.SerializableImage;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class Timeline {

    public List<Frame> frames = new ArrayList<>();
    public int id;
    public SWF swf;
    public RECT displayRect;
    public int frameRate;
    public List<Tag> tags;

    public int getMaxDepth() {
        int max_depth = 0;
        for (Frame f : frames) {
            for (int depth : f.layers.keySet()) {
                if (depth > max_depth) {
                    max_depth = depth;
                }
                if (f.layers.get(depth).clipDepth > max_depth) {
                    max_depth = f.layers.get(depth).clipDepth;
                }
            }
        }
        return max_depth;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public Timeline(SWF swf) {
        this(swf, swf.tags, 0, swf.displayRect);
    }

    public Timeline(SWF swf, List<Tag> tags, int id, RECT displayRect) {
        this.id = id;
        this.swf = swf;
        this.displayRect = displayRect;
        this.frameRate = swf.frameRate;
        this.tags = tags;
        Frame frame = new Frame(this);
        boolean tagAdded = false;
        for (Tag t : tags) {
            if (ShowFrameTag.isNestedTagType(t.getId())) {
                frame.innerTags.add(t);
                tagAdded = true;
            }
            if (t instanceof StartSoundTag) {
                frame.sounds.add(((StartSoundTag) t).soundId);
                tagAdded = true;
            }
            if (t instanceof StartSound2Tag) {
                frame.soundClasses.add(((StartSound2Tag) t).soundClassName);
                tagAdded = true;
            }
            if (t instanceof SetBackgroundColorTag) {
                frame.backgroundColor = ((SetBackgroundColorTag) t).backgroundColor;
                tagAdded = true;
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int depth = po.getDepth();
                if (!frame.layers.containsKey(depth)) {
                    frame.layers.put(depth, new DepthState(swf, frame));
                }
                DepthState fl = frame.layers.get(depth);
                int characterId = po.getCharacterId();
                if (characterId != -1) {
                    fl.characterId = characterId;
                }
                if (po.flagMove()) {
                    MATRIX matrix2 = po.getMatrix();
                    if (matrix2 != null) {
                        fl.matrix = matrix2;
                    }
                    String instanceName2 = po.getInstanceName();
                    if (instanceName2 != null) {
                        fl.instanceName = instanceName2;
                    }
                    ColorTransform colorTransForm2 = po.getColorTransform();
                    if (colorTransForm2 != null) {
                        fl.colorTransForm = colorTransForm2;
                    }

                    CLIPACTIONS clipActions2 = po.getClipActions();
                    if (clipActions2 != null) {
                        fl.clipActions = clipActions2;
                    }
                    if (po.cacheAsBitmap()) {
                        fl.cacheAsBitmap = true;
                    }
                    int blendMode2 = po.getBlendMode();
                    if (blendMode2 > 0) {
                        fl.blendMode = blendMode2;
                    }
                    List<FILTER> filters2 = po.getFilters();
                    if (filters2 != null) {
                        fl.filters = filters2;
                    }
                    int ratio2 = po.getRatio();
                    if (ratio2 > -1) {
                        fl.ratio = ratio2;
                    }
                    int clipDepth2 = po.getClipDepth();
                    if (clipDepth2 > -1) {
                        fl.clipDepth = clipDepth2;
                    }
                } else {
                    fl.matrix = po.getMatrix();
                    fl.instanceName = po.getInstanceName();
                    fl.colorTransForm = po.getColorTransform();
                    fl.cacheAsBitmap = po.cacheAsBitmap();
                    fl.blendMode = po.getBlendMode();
                    fl.filters = po.getFilters();
                    fl.ratio = po.getRatio();
                    fl.clipActions = po.getClipActions();
                    fl.clipDepth = po.getClipDepth();
                }
                fl.key = true;
                tagAdded = true;
            }
            if (t instanceof RemoveTag) {
                RemoveTag r = (RemoveTag) t;
                int depth = r.getDepth();
                frame.layers.remove(depth);
                tagAdded = true;
            }
            if (t instanceof DoActionTag) {
                frame.action = (DoActionTag) t;
                tagAdded = true;
            }
            if (t instanceof ShowFrameTag) {
                frames.add(frame);
                frame = new Frame(frame);
                tagAdded = false;
            }
        }
        if (tagAdded) {
            frames.add(frame);
        }
    }

    public void getNeededCharacters(Set<Integer> usedCharacters) {
        for (int i = 0; i < getFrameCount(); i++) {
            getNeededCharacters(i, usedCharacters);
        }
    }
    
    public void getNeededCharacters(List<Integer> frames, Set<Integer> usedCharacters) {
        for (int frame = 0; frame < frames.size(); frame++) {
            getNeededCharacters(frame, usedCharacters);
        }
    }
    
    public void getNeededCharacters(int frame, Set<Integer> usedCharacters) {
        Frame frameObj = frames.get(frame);
        for (int depth : frameObj.layers.keySet()) {
            DepthState layer = frameObj.layers.get(depth);
            if (layer.characterId != -1) {
                if (!swf.characters.containsKey(layer.characterId)) {
                    continue;
                }
                usedCharacters.add(layer.characterId);
                swf.characters.get(layer.characterId).getNeededCharactersDeep(usedCharacters);
            }
        }
    }
    
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof CharacterIdTag && ((CharacterIdTag) t).getCharacterId() == characterId) {
                tags.remove(i);
                i--;
                modified = true;
            }
        }
        return modified;
    }

    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SWF.frameToImage(this, frame, time, stateUnderCursor, mouseButton, image, transformation, colorTransform);
    }

    public String toHtmlCanvas(double unitDivisor, List<Integer> frames) {
        return SWF.framesToHtmlCanvas(unitDivisor, this, frames, 0, null, 0, displayRect, new ColorTransform(), null);
    }

    public void getSounds(int frame, int time, DepthState stateUnderCursor, int mouseButton, List<Integer> sounds, List<String> soundClasses) {
        Frame fr = this.frames.get(frame);
        sounds.addAll(fr.sounds);
        soundClasses.addAll(fr.soundClasses);
        int maxDepth = this.getMaxDepth();
        for (int d = maxDepth; d >= 0; d--) {
            DepthState ds = fr.layers.get(d);
            if (ds != null) {
                CharacterTag c = swf.characters.get(ds.characterId);
                if (c instanceof Timelined) {
                    int frameCount = ((Timelined) c).getTimeline().frames.size();
                    if (frameCount == 0) {
                        continue;
                    }
                    int dframe = (time + ds.time) % frameCount;
                    if (c instanceof ButtonTag) {
                        ButtonTag bt = (ButtonTag) c;
                        dframe = ButtonTag.FRAME_UP;
                        if (stateUnderCursor == ds) {
                            if (mouseButton > 0) {
                                dframe = ButtonTag.FRAME_DOWN;
                            } else {
                                dframe = ButtonTag.FRAME_OVER;
                            }
                        }
                    }
                    ((Timelined) c).getTimeline().getSounds(dframe, time + ds.time, stateUnderCursor, mouseButton, sounds, soundClasses);
                }
            }
        }
    }

    public void getObjectsOutlines(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation, List<DepthState> objs, List<Shape> outlines) {
        Frame fr = this.frames.get(frame);
        Stack<Clip> clips = new Stack<>();
        int maxDepth = this.getMaxDepth();
        for (int d = maxDepth; d >= 0; d--) {
            Clip currentClip = null;
            for (int i = clips.size() - 1; i >= 0; i--) {
                Clip cl = clips.get(i);
                if (cl.depth <= d) {
                    clips.remove(i);
                }
            }
            if (!clips.isEmpty()) {
                currentClip = clips.peek();
            }
            DepthState ds = fr.layers.get(d);
            if (ds == null) {
                continue;
            }
            if (!ds.isVisible) {
                continue;
            }
            CharacterTag c = swf.characters.get(ds.characterId);
            if (c instanceof DrawableTag) {
                Matrix m = new Matrix(ds.matrix);
                m = m.preConcatenate(transformation);

                int dframe = 0;
                if (c instanceof Timelined) {
                    int frameCount = ((Timelined) c).getTimeline().frames.size();
                    if (frameCount == 0) {
                        return;
                    }
                    dframe = ds.time % frameCount;
                    if (c instanceof ButtonTag) {
                        ButtonTag bt = (ButtonTag) c;
                        dframe = ButtonTag.FRAME_HITTEST;
                        /*dframe = ButtonTag.FRAME_UP;
                         if (stateUnderCursor == ds) {
                         if (mouseButton > 0) {
                         dframe = ButtonTag.FRAME_DOWN;
                         } else {
                         dframe = ButtonTag.FRAME_OVER;
                         }
                         }*/
                    }
                }
                Shape cshape = ((DrawableTag) c).getOutline(dframe, ds.time + time, ds.ratio, stateUnderCursor, mouseButton, m);

                Area addArea = new Area(cshape);
                if (currentClip != null) {
                    Area a = new Area(new Rectangle(displayRect.Xmin, displayRect.Ymin, displayRect.getWidth(), displayRect.getHeight()));
                    a.subtract(new Area(currentClip.shape));
                    addArea.subtract(a);
                }
                if (ds.clipDepth > -1) {
                    Clip clip = new Clip(addArea, ds.clipDepth);
                    clips.push(clip);
                } else {
                    objs.add(ds);
                    outlines.add(addArea);
                }
                if (c instanceof Timelined) {
                    ((Timelined) c).getTimeline().getObjectsOutlines(dframe, time + ds.time, ds.ratio, stateUnderCursor, mouseButton, m, objs, outlines);
                }
            }
        }
    }

    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        Frame fr = this.frames.get(frame);
        Area area = new Area();
        Stack<Clip> clips = new Stack<>();
        int maxDepth = this.getMaxDepth();
        for (int d = maxDepth; d >= 0; d--) {
            Clip currentClip = null;
            for (int i = clips.size() - 1; i >= 0; i--) {
                Clip cl = clips.get(i);
                if (cl.depth <= d) {
                    clips.remove(i);
                }
            }
            if (!clips.isEmpty()) {
                currentClip = clips.peek();
            }
            DepthState ds = fr.layers.get(d);
            if (ds == null) {
                continue;
            }
            if (!ds.isVisible) {
                continue;
            }
            CharacterTag c = swf.characters.get(ds.characterId);
            if (c instanceof DrawableTag) {
                Matrix m = new Matrix(ds.matrix);
                m = m.preConcatenate(transformation);

                int dframe = 0;
                if (c instanceof Timelined) {
                    dframe = (time + ds.time) % ((Timelined) c).getTimeline().frames.size();
                    if (c instanceof ButtonTag) {
                        ButtonTag bt = (ButtonTag) c;
                        dframe = ButtonTag.FRAME_UP;
                        if (stateUnderCursor == ds) {
                            if (mouseButton > 0) {
                                dframe = ButtonTag.FRAME_DOWN;
                            } else {
                                dframe = ButtonTag.FRAME_OVER;
                            }
                        }
                    }
                }
                Shape cshape = ((DrawableTag) c).getOutline(dframe, time + ds.time, ds.ratio, stateUnderCursor, mouseButton, m);

                Area addArea = new Area(cshape);
                if (currentClip != null) {
                    Area a = new Area(new Rectangle(displayRect.Xmin, displayRect.Ymin, displayRect.getWidth(), displayRect.getHeight()));
                    a.subtract(new Area(currentClip.shape));
                    addArea.subtract(a);
                }
                if (ds.clipDepth > -1) {
                    Clip clip = new Clip(addArea, ds.clipDepth);
                    clips.push(clip);
                } else {
                    area.add(addArea);
                }
            }
        }
        return area;
    }

    public boolean isSingleFrame() {
        for (int i = 0; i < frames.size(); i++) {
            if (!isSingleFrame(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSingleFrame(int frame) {
        Frame frameObj = frames.get(frame);
        int maxDepth = this.getMaxDepth();
        for (int i = 1; i <= maxDepth; i++) {
            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!swf.characters.containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }
            CharacterTag character = swf.characters.get(layer.characterId);
            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;
                if (!drawable.isSingleFrame()) {
                    return false;
                }
            }
        }

        return true;
    }
}
