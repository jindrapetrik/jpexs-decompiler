/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.ftr;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf1.ActionGotoFrame;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Converts DirectUI movie resources to and from editable SWF movies.
 */
public final class FtrConverter {

    private static final int VERSION = 8;

    private FtrConverter() {
    }

    public static SWF read(InputStream input) throws IOException {
        SWF swf = new SWF("UTF-8");
        swf.version = VERSION;
        SWFInputStream in = new SWFInputStream(swf, Helper.readStream(input));
        int x = readInt(in, "x");
        int y = readInt(in, "y");
        int width = readInt(in, "width");
        int height = readInt(in, "height");
        if (width < 0 || height < 0 || (long) x + width > Integer.MAX_VALUE
                || (long) y + height > Integer.MAX_VALUE) {
            throw new IOException("Invalid FTR stage rectangle");
        }
        swf.displayRect = new RECT(x, x + width, y, y + height);
        // FTR stores a little-endian double, unlike the word-swapped SWF DOUBLE.
        long fpsLow = in.readUI32("frameRateLow");
        long fpsHigh = in.readUI32("frameRateHigh");
        double fps = Double.longBitsToDouble((fpsHigh << 32) | fpsLow);
        if (!Double.isFinite(fps) || fps > 65535 / 256.0) {
            throw new IOException("FTR frame rate cannot be represented in SWF");
        }
        swf.frameRate = fps > 0 ? (float) fps : 30;
        FtrShapes.Reader shapes = new FtrShapes.Reader(swf);
        readTimeline(in, swf, swf, shapes, 0);
        if (in.available() != 0) {
            throw new IOException("Trailing data after FTR timeline at offset " + in.getPos());
        }
        shapes.addImages();
        swf.clearModified();
        return swf;
    }

    public static void write(SWF swf, OutputStream output) throws IOException {
        SWFOutputStream out = new SWFOutputStream(output, VERSION, "UTF-8");
        RECT rect = swf.displayRect;
        out.writeSI32(rect.Xmin);
        out.writeSI32(rect.Ymin);
        out.writeSI32((long) rect.Xmax - rect.Xmin);
        out.writeSI32((long) rect.Ymax - rect.Ymin);
        if (!Float.isFinite(swf.frameRate) || swf.frameRate <= 0) {
            throw new IOException("Invalid SWF frame rate");
        }
        long fps = Double.doubleToLongBits(swf.frameRate);
        out.writeUI32(fps & 0xffffffffL);
        out.writeUI32(fps >>> 32);
        writeTimeline(out, swf, swf, 0);
        out.flush();
    }

    private static void readTimeline(SWFInputStream in, SWF swf, Timelined timeline, FtrShapes.Reader shapes,
            int depth) throws IOException {
        int count = readCount(in, "tagCount", 4);
        int frames = 0;
        for (int i = 0; i < count; i++) {
            long offset = in.getPos();
            long type = in.readUI32("tagType");
            Tag tag;
            switch ((int) type) {
                case ShowFrameTag.ID:
                    tag = new ShowFrameTag(swf);
                    frames++;
                    break;
                case SetBackgroundColorTag.ID:
                    RGBA color = in.readRGBA("background");
                    tag = new SetBackgroundColorTag(swf, new RGB(color.red, color.green, color.blue));
                    break;
                case 2:
                case 22:
                case 32:
                    if (depth != 0) {
                        throw new IOException("Shape definition inside an FTR sprite");
                    }
                    tag = shapes.read(in);
                    break;
                case PlaceObject2Tag.ID:
                    tag = readPlacement(in, swf);
                    break;
                case RemoveObject2Tag.ID:
                    RemoveObject2Tag remove = new RemoveObject2Tag(swf);
                    remove.depth = in.readUI16("depth");
                    tag = remove;
                    break;
                case DefineSpriteTag.ID:
                    if (depth != 0) {
                        throw new IOException("Sprite definition inside an FTR sprite");
                    }
                    DefineSpriteTag sprite = new DefineSpriteTag(swf);
                    sprite.setCharacterId(in.readUI16("spriteId"));
                    shapes.registerId(sprite.getCharacterId());
                    readTimeline(in, swf, sprite, shapes, depth + 1);
                    sprite.hasEndTag = true;
                    tag = sprite;
                    break;
                case DoActionTag.ID:
                    long action = in.readUI32("action");
                    int frame = in.readUI16("frame");
                    if (action != 1) {
                        throw new IOException("Unsupported FTR action " + action + " at offset " + offset);
                    }
                    DoActionTag doAction = new DoActionTag(swf);
                    doAction.setActions(Collections.<Action>singletonList(new ActionGotoFrame(frame, swf.getCharset())));
                    tag = doAction;
                    break;
                default:
                    throw new IOException("Unsupported FTR tag " + type + " at offset " + offset);
            }
            tag.setTimelined(timeline);
            timeline.addTag(tag);
        }
        if (frames > 65535) {
            throw new IOException("Too many frames for SWF");
        }
        timeline.setFrameCount(frames);
    }

    private static void writeTimeline(SWFOutputStream out, SWF swf, Timelined timeline, int depth) throws IOException {
        int count = 0;
        for (Tag tag : timeline.getTags()) {
            if (!isSwfOnly(tag)) {
                count++;
            }
        }
        out.writeUI32(count);
        for (Tag tag : timeline.getTags()) {
            if (tag instanceof ShapeTag && depth == 0) {
                out.writeUI32(32);
                FtrShapes.write(out, swf, (ShapeTag) tag);
            } else if (tag instanceof DefineSpriteTag && depth == 0) {
                DefineSpriteTag sprite = (DefineSpriteTag) tag;
                out.writeUI32(DefineSpriteTag.ID);
                out.writeUI16(sprite.getCharacterId());
                writeTimeline(out, swf, sprite, depth + 1);
            } else if (tag instanceof SetBackgroundColorTag) {
                out.writeUI32(SetBackgroundColorTag.ID);
                out.writeRGBA(new RGBA(((SetBackgroundColorTag) tag).backgroundColor));
            } else if (tag instanceof ShowFrameTag) {
                out.writeUI32(ShowFrameTag.ID);
            } else if (tag instanceof PlaceObject2Tag) {
                writePlacement(out, swf, (PlaceObject2Tag) tag);
            } else if (tag instanceof PlaceObjectTag) {
                PlaceObjectTag place = (PlaceObjectTag) tag;
                CXFORMWITHALPHA color = place.getColorTransform() == null
                        ? null : new CXFORMWITHALPHA(place.getColorTransform());
                writePlacement(out, swf, new PlaceObject2Tag(swf, false, place.getDepth(), place.getCharacterId(),
                        place.getMatrix(), color, -1, null, -1, null));
            } else if (tag instanceof RemoveObject2Tag || tag instanceof RemoveObjectTag) {
                out.writeUI32(RemoveObject2Tag.ID);
                out.writeUI16(tag instanceof RemoveObject2Tag ? ((RemoveObject2Tag) tag).depth
                        : ((RemoveObjectTag) tag).depth);
            } else if (tag instanceof DoActionTag) {
                writeAction(out, (DoActionTag) tag);
            } else if (!isSwfOnly(tag)) {
                throw new IOException("FTR does not support SWF tag " + tag.getName());
            }
        }
    }

    private static boolean isSwfOnly(Tag tag) throws IOException {
        if (tag instanceof FileAttributesTag) {
            if (((FileAttributesTag) tag).actionScript3) {
                throw new IOException("FTR does not support ActionScript 3");
            }
            return true;
        }
        // Bitmap fills store their images inline in FTR.
        return tag instanceof EndTag || tag instanceof ImageTag;
    }

    private static void writeAction(SWFOutputStream out, DoActionTag tag) throws IOException {
        List<Action> actions;
        try {
            actions = tag.getActions();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while reading SWF actions", ex);
        }
        if (actions.size() != 1 || !(actions.get(0) instanceof ActionGotoFrame)) {
            throw new IOException("FTR only supports a single GotoFrame action per DoAction tag");
        }
        out.writeUI32(DoActionTag.ID);
        out.writeUI32(1);
        out.writeUI16(((ActionGotoFrame) actions.get(0)).frame);
    }

    private static PlaceObject2Tag readPlacement(SWFInputStream in, SWF swf) throws IOException {
        PlaceObject2Tag tag = new PlaceObject2Tag(swf);
        tag.placeFlagHasClipDepth = readFlag(in, "hasClipDepth");
        tag.placeFlagHasColorTransform = readFlag(in, "hasColorTransform");
        tag.placeFlagHasMatrix = readFlag(in, "hasMatrix");
        tag.placeFlagHasCharacter = readFlag(in, "hasCharacter");
        tag.placeFlagMove = readFlag(in, "move");
        tag.depth = in.readUI16("depth");
        tag.characterId = in.readUI16("characterId");
        tag.clipDepth = in.readUI16("clipDepth");
        if (tag.placeFlagHasMatrix) {
            tag.matrix = readMatrix(in);
        }
        if (tag.placeFlagHasColorTransform) {
            int[] mult = new int[4];
            int[] add = new int[4];
            for (int i = 0; i < 4; i++) {
                mult[i] = Math.round(readFloat(in, "colorMultiplier") * 256);
                add[i] = Math.round(readFloat(in, "colorAddition"));
                if (mult[i] < -16384 || mult[i] > 16383 || add[i] < -16384 || add[i] > 16383) {
                    throw new IOException("FTR color transform exceeds SWF limits");
                }
            }
            CXFORMWITHALPHA color = new CXFORMWITHALPHA();
            color.hasMultTerms = color.hasAddTerms = true;
            color.redMultTerm = mult[0];
            color.greenMultTerm = mult[1];
            color.blueMultTerm = mult[2];
            color.alphaMultTerm = mult[3];
            color.redAddTerm = add[0];
            color.greenAddTerm = add[1];
            color.blueAddTerm = add[2];
            color.alphaAddTerm = add[3];
            tag.colorTransform = color;
        }
        return tag;
    }

    private static void writePlacement(SWFOutputStream out, SWF swf, PlaceObject2Tag tag) throws IOException {
        if (tag.placeFlagHasClipActions || tag.placeFlagHasName || tag.placeFlagHasRatio) {
            throw new IOException("FTR does not support PlaceObject2 names, ratios or clip actions");
        }
        if (tag.placeFlagHasCharacter && !(swf.getCharacter(tag.characterId) instanceof ShapeTag)
                && !(swf.getCharacter(tag.characterId) instanceof DefineSpriteTag)) {
            throw new IOException("FTR cannot place character " + tag.characterId + " on the timeline");
        }
        out.writeUI32(PlaceObject2Tag.ID);
        out.writeUI8(tag.placeFlagHasClipDepth ? 1 : 0);
        out.writeUI8(tag.placeFlagHasColorTransform ? 1 : 0);
        out.writeUI8(tag.placeFlagHasMatrix ? 1 : 0);
        out.writeUI8(tag.placeFlagHasCharacter ? 1 : 0);
        out.writeUI8(tag.placeFlagMove ? 1 : 0);
        out.writeUI16(tag.depth);
        out.writeUI16(tag.placeFlagHasCharacter ? tag.characterId : 0xffff);
        out.writeUI16(tag.placeFlagHasClipDepth ? tag.clipDepth : 0xffff);
        if (tag.placeFlagHasMatrix) {
            MATRIX matrix = tag.matrix;
            if (matrix == null) {
                throw new IOException("Missing PlaceObject2 matrix");
            }
            writeMatrix(out, new Matrix(matrix));
        }
        if (tag.placeFlagHasColorTransform) {
            CXFORMWITHALPHA color = tag.colorTransform;
            if (color == null) {
                throw new IOException("Missing PlaceObject2 color transform");
            }
            out.writeFLOAT(color.getRedMulti() / 256f);
            out.writeFLOAT(color.getRedAdd());
            out.writeFLOAT(color.getGreenMulti() / 256f);
            out.writeFLOAT(color.getGreenAdd());
            out.writeFLOAT(color.getBlueMulti() / 256f);
            out.writeFLOAT(color.getBlueAdd());
            out.writeFLOAT(color.getAlphaMulti() / 256f);
            out.writeFLOAT(color.getAlphaAdd());
        }
    }

    static MATRIX readMatrix(SWFInputStream in) throws IOException {
        float a = readFloat(in, "m11");
        float b = readFloat(in, "m12");
        float c = readFloat(in, "m21");
        float d = readFloat(in, "m22");
        int x = Math.round(readFloat(in, "dx"));
        int y = Math.round(readFloat(in, "dy"));
        return new Matrix(a, d, b, c, x, y).toMATRIX();
    }

    static void writeMatrix(SWFOutputStream out, Matrix matrix) throws IOException {
        out.writeFLOAT((float) matrix.scaleX);
        out.writeFLOAT((float) matrix.rotateSkew0);
        out.writeFLOAT((float) matrix.rotateSkew1);
        out.writeFLOAT((float) matrix.scaleY);
        out.writeFLOAT((float) matrix.translateX);
        out.writeFLOAT((float) matrix.translateY);
    }

    static int readInt(SWFInputStream in, String name) throws IOException {
        return (int) in.readUI32(name);
    }

    static int readCount(SWFInputStream in, String name, int minimumSize) throws IOException {
        long count = in.readUI32(name);
        if (count > in.available() / minimumSize) {
            throw new IOException("Invalid FTR " + name + " at offset " + in.getPos());
        }
        return (int) count;
    }

    static float readFloat(SWFInputStream in, String name) throws IOException {
        float value = in.readFLOAT(name);
        if (!Float.isFinite(value)) {
            throw new IOException("Invalid FTR " + name + " at offset " + in.getPos());
        }
        return value;
    }

    private static boolean readFlag(SWFInputStream in, String name) throws IOException {
        int value = in.readUI8(name);
        if (value > 1) {
            throw new IOException("Invalid FTR " + name + " flag at offset " + in.getPos());
        }
        return value != 0;
    }
}
