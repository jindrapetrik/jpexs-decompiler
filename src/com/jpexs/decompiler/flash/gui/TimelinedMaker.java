/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.PreviewExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage2;
import com.jpexs.decompiler.flash.tags.gfx.DefineSubImage;
import com.jpexs.decompiler.flash.tags.gfx.enums.IdType;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class TimelinedMaker {

    public static SWF makeTimelinedImage(ImageTag imageTag) {
        SWF swf = new SWF();
        swf.gfx = imageTag.getSwf().gfx;
        swf.version = imageTag.getSwf().version;
        int w = (int) (imageTag.getImageDimension().getWidth() * SWF.unitDivisor);
        int h = (int) (imageTag.getImageDimension().getHeight() * SWF.unitDivisor);
        swf.displayRect = new RECT(0, w, 0, h);
        swf.frameCount = 1;
        swf.frameRate = 1;
        swf.setFile(imageTag.getSwf().getFile()); //DefineSubImage calculates relative paths from it
        try {

            JPEGTablesTag jpegTablesTag = null;
            if (imageTag instanceof DefineBitsTag) {
                jpegTablesTag = imageTag.getSwf().getJtt();
            }
            Set<Integer> needed = new LinkedHashSet<>();
            imageTag.getNeededCharacters(needed, swf);

            List<CharacterTag> neededCopies = new ArrayList<>();
            for (int n : needed) {
                CharacterTag ct = imageTag.getSwf().getCharacter(n);
                if (ct != null) {
                    ct = (CharacterTag) ct.cloneTag();
                    ct.setSwf(swf);
                    neededCopies.add(ct);
                }
            }
            if (imageTag instanceof DefineSubImage) {
                DefineExternalImage2 dei2 = (DefineExternalImage2) imageTag.getSwf().getExternalImage2(((DefineSubImage) imageTag).imageId);
                if (dei2 != null) {
                    dei2 = (DefineExternalImage2) dei2.cloneTag();
                    dei2.setSwf(swf);
                    neededCopies.add(dei2);
                }
            }

            ImageTag imageTagCopy = (ImageTag) imageTag.cloneTag();
            imageTagCopy.setSwf(swf);
            int imageCharId = imageTag.getCharacterId();
            if ((imageTag instanceof DefineExternalImage2) && (((DefineExternalImage2) imageTag).idType != IdType.IDTYPE_NONE)) {
                imageCharId = swf.getNextCharacterId();
                imageTagCopy.characterID = imageCharId;
            }
            DefineShape2Tag shapeTag = new DefineShape2Tag(swf);
            int shapeCharId = imageCharId + 1;
            shapeTag.shapeId = shapeCharId;
            shapeTag.shapeBounds = new RECT(swf.displayRect);

            SHAPEWITHSTYLE shapeData = new SHAPEWITHSTYLE();
            FILLSTYLEARRAY fillStyleArray = new FILLSTYLEARRAY();
            FILLSTYLE[] fillStyles = new FILLSTYLE[1];
            FILLSTYLE fillStyle = new FILLSTYLE();
            fillStyle.bitmapId = imageCharId;
            fillStyle.inShape3 = false;
            fillStyle.fillStyleType = FILLSTYLE.CLIPPED_BITMAP;
            fillStyle.bitmapMatrix = Matrix.getScaleInstance(SWF.unitDivisor).toMATRIX();
            fillStyles[0] = fillStyle;
            fillStyleArray.fillStyles = fillStyles;
            shapeData.fillStyles = fillStyleArray;
            shapeData.lineStyles = new LINESTYLEARRAY();

            List<SHAPERECORD> shapeRecords = new ArrayList<>();

            StyleChangeRecord scr = new StyleChangeRecord();
            scr.stateFillStyle0 = true;
            scr.fillStyle0 = 1;
            shapeRecords.add(scr);

            StyleChangeRecord scr2 = new StyleChangeRecord();
            scr2.stateMoveTo = true;
            scr2.moveDeltaX = 0;
            scr2.moveDeltaY = 0;
            scr2.calculateBits();
            shapeRecords.add(scr2);

            StraightEdgeRecord ser1 = new StraightEdgeRecord();
            ser1.vertLineFlag = true;
            ser1.deltaY = h;
            ser1.calculateBits();
            shapeRecords.add(ser1);

            StraightEdgeRecord ser2 = new StraightEdgeRecord();
            ser2.deltaX = w;
            shapeRecords.add(ser2);

            StraightEdgeRecord ser3 = new StraightEdgeRecord();
            ser3.vertLineFlag = true;
            ser3.deltaY = -h;
            shapeRecords.add(ser3);

            StraightEdgeRecord ser4 = new StraightEdgeRecord();
            ser4.deltaX = -w;
            shapeRecords.add(ser4);

            shapeRecords.add(new EndShapeRecord());

            shapeData.shapeRecords = shapeRecords;

            shapeData.numFillBits = 1;
            shapeData.numLineBits = 0;

            shapeTag.shapes = shapeData;

            PlaceObjectTag placeTag = new PlaceObjectTag(swf, shapeCharId, 1, new Matrix().toMATRIX(), null);

            ShowFrameTag showFrameTag = new ShowFrameTag(swf);

            EndTag endTag = new EndTag(swf);

            if (jpegTablesTag != null) {
                swf.addTag(jpegTablesTag);
            }
            for (CharacterTag neededCopy : neededCopies) {
                swf.addTag(neededCopy);
            }
            swf.addTag(imageTagCopy);
            swf.addTag(shapeTag);
            swf.addTag(placeTag);
            swf.addTag(showFrameTag);
            swf.addTag(endTag);

        } catch (InterruptedException | IOException ex) {
            //ignore
        }
        return swf;
    }

    public static Timelined makeTimelined(final Tag tag) {
        if (tag instanceof ImageTag) {
            return makeTimelinedImage((ImageTag) tag);
        }
        return makeTimelined(tag, -1);
    }

    public static Timelined makeTimelined(final Tag tag, final int fontFrameNum) {
        int chId = ((CharacterIdTag) tag).getCharacterId();
        if (chId == -1) {
            chId = 0;
        }
        final int fChId = chId;
        return new Timelined() {
            private Timeline tim;

            @Override
            public Timeline getTimeline() {
                if (tim == null) {
                    Timeline timeline = new Timeline(tag.getSwf(), this, fChId, getRect());
                    initTimeline(timeline);
                    tim = timeline;
                }

                return tim;
            }

            @Override
            public void resetTimeline() {
                if (tim != null) {
                    tim.reset(tag.getSwf(), this, ((CharacterTag) tag).getCharacterId(), getRect());
                    initTimeline(tim);
                }
            }

            private void initTimeline(Timeline timeline) {
                if (tag instanceof MorphShapeTag) {
                    timeline.frameRate = PreviewExporter.MORPH_SHAPE_ANIMATION_FRAME_RATE;
                    int framesCnt = (int) (timeline.frameRate * PreviewExporter.MORPH_SHAPE_ANIMATION_LENGTH);
                    for (int i = 0; i < framesCnt; i++) {
                        Frame f = new Frame(timeline, i);
                        DepthState ds = new DepthState(tag.getSwf(), f, f);
                        ds.characterId = fChId;
                        ds.matrix = new MATRIX();
                        ds.ratio = i * 65535 / framesCnt;
                        f.layers.put(1, ds);
                        f.layersChanged = true;
                        timeline.addFrame(f);
                    }
                    Frame f = new Frame(timeline, framesCnt);
                    DepthState ds = new DepthState(tag.getSwf(), f, f);
                    ds.depth = 1;
                    ds.characterId = fChId;
                    ds.matrix = new MATRIX();
                    ds.ratio = 65535;
                    f.layers.put(1, ds);
                    f.layersChanged = true;
                    timeline.addFrame(f);
                } else if (tag instanceof FontTag) {
                    int pageCount = PreviewPanel.getFontPageCount((FontTag) tag);
                    int frame = fontFrameNum;
                    if (frame < 0 || frame >= pageCount) {
                        frame = 0;
                    }
                    //TODO: make this static texts instead of FontTag as drawable.
                    //We do not want to draw fonts directly added to stage as
                    //Fonts are really added to stage in some corner cases like for vertical text.
                    Frame f = new Frame(timeline, 0);
                    DepthState ds = new DepthState(tag.getSwf(), f, f);
                    ds.depth = 1;
                    ds.characterId = fChId;
                    ds.matrix = new MATRIX();
                    f.layers.put(1, ds);
                    f.layersChanged = true;
                    timeline.addFrame(f);
                    timeline.fontFrameNum = frame;
                } else if (tag instanceof SoundTag) {
                    //empty
                } else {
                    Frame f = new Frame(timeline, 0);
                    DepthState ds = new DepthState(tag.getSwf(), f, f);
                    ds.depth = 1;
                    ds.characterId = fChId;
                    ds.matrix = new MATRIX();
                    f.layers.put(1, ds);
                    timeline.addFrame(f);
                }
                timeline.displayRect = getRect();
            }

            @Override
            public RECT getRect() {
                return getRect(new HashSet<>());
            }

            @Override
            public RECT getRect(Set<BoundedTag> added) {
                if (!(tag instanceof BoundedTag)) {
                    return new RECT(0, 1, 0, 1);
                }
                BoundedTag bt = (BoundedTag) tag;
                if (!added.contains(bt)) {
                    return bt.getRect(added);
                }
                return new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
            }

            @Override
            public int hashCode() {
                return tag.hashCode();
            }

            @Override
            public void setModified(boolean value) {
            }

            @Override
            public boolean isModified() {
                return false;
            }

            @Override
            public ReadOnlyTagList getTags() {
                return ReadOnlyTagList.EMPTY;
            }

            @Override
            public void removeTag(int index) {
            }

            @Override
            public void removeTag(Tag tag) {
            }

            @Override
            public void addTag(Tag tag) {
            }

            @Override
            public void addTag(int index, Tag tag) {
            }

            @Override
            public void replaceTag(int index, Tag newTag) {
            }

            @Override
            public void replaceTag(Tag oldTag, Tag newTag) {
            }

            @Override
            public int indexOfTag(Tag tag) {
                return -1;
            }

            @Override
            public RECT getRectWithStrokes() {
                return getRect();
            }

            @Override
            public void setFrameCount(int frameCount) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getFrameCount() {
                return getTimeline().getFrameCount();
            }

            @Override
            public SWF getSwf() {
                return tag.getSwf();
            }
        };
    }
}
