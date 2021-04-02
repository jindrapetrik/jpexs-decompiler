/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFHeader;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.decompiler.graph.CompilationException;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class PreviewExporter {

    // play morph shape in 2 second(s)
    public static final int MORPH_SHAPE_ANIMATION_LENGTH = 2;

    public static final int MORPH_SHAPE_ANIMATION_FRAME_RATE = 30;

    private void updateProgressBar(int xmin, int ymin, SWF swf, SWFOutputStream sos2, int width, int height, int progressBarHeight, int currentFrame, int totalFrames) throws IOException {
        Matrix m = new Matrix();
        m.translate(xmin, ymin + height - progressBarHeight * 20);
        m.scale(width * currentFrame / totalFrames, progressBarHeight * 20);
        new PlaceObject2Tag(swf, true, 2, -1, m.toMATRIX(), null, -1, null, -1, null).writeTag(sos2);
    }

    private void addControls(int xmin, int ymin, int progressBarHeight, SWF swf, SWFOutputStream sos2, int width, int numFrames, int height) throws IOException {
        int progressBarShapeId = swf.getNextCharacterId();
        int overVideoButtonId = progressBarShapeId + 1;
        int progressBarButtonId = overVideoButtonId + 1;

        Color progressBarColor = Color.red;
        DefineShapeTag dsh = new DefineShapeTag(swf);
        dsh.shapeBounds = new RECT(0, 20, 0, 20 * progressBarHeight);
        dsh.shapeId = progressBarShapeId;
        dsh.shapes.fillStyles.fillStyles = new FILLSTYLE[1];
        dsh.shapes.fillStyles.fillStyles[0] = new FILLSTYLE();
        dsh.shapes.fillStyles.fillStyles[0].fillStyleType = FILLSTYLE.SOLID;
        dsh.shapes.fillStyles.fillStyles[0].color = new RGB(progressBarColor);
        dsh.shapes.shapeRecords.clear();
        StyleChangeRecord scr = new StyleChangeRecord();
        scr.stateFillStyle0 = true;
        scr.fillStyle0 = 1;
        scr.stateMoveTo = true;
        scr.moveDeltaX = 0;
        scr.moveDeltaY = 0;
        dsh.shapes.shapeRecords.add(scr);
        StraightEdgeRecord ser;
        ser = new StraightEdgeRecord();
        ser.vertLineFlag = true;
        ser.deltaY = 1;
        dsh.shapes.shapeRecords.add(ser);
        ser = new StraightEdgeRecord();
        ser.deltaX = 1;
        dsh.shapes.shapeRecords.add(ser);
        ser = new StraightEdgeRecord();
        ser.vertLineFlag = true;
        ser.deltaY = -1;
        dsh.shapes.shapeRecords.add(ser);
        ser = new StraightEdgeRecord();
        ser.deltaX = -1;
        dsh.shapes.shapeRecords.add(ser);
        dsh.shapes.shapeRecords.add(new EndShapeRecord());

        dsh.writeTag(sos2);

        DefineButton2Tag overVideoButton = new DefineButton2Tag(swf);
        overVideoButton.buttonId = overVideoButtonId;

        BUTTONRECORD br;
        br = new BUTTONRECORD();
        br.buttonStateUp = false;
        br.buttonStateDown = false;
        br.buttonStateOver = false;
        br.buttonStateHitTest = true;
        br.characterId = progressBarShapeId;
        br.placeDepth = 1;
        br.colorTransform = new CXFORMWITHALPHA();
        br.placeMatrix = new MATRIX();
        overVideoButton.characters.add(br);

        BUTTONCONDACTION bca;
        ActionScript2Parser ap;

        bca = new BUTTONCONDACTION(swf, overVideoButton);
        bca.isLast = true;
        ap = new ActionScript2Parser(swf, bca);
        try {
            bca.setActions(ap.actionsFromString("on(press){"
                    + "stopped = !stopped; if(stopped) {stop();}else{play();}"
                    + "}"));
        } catch (CompilationException ex) {
            Logger.getLogger(PreviewExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ActionParseException ex) {
            Logger.getLogger(PreviewExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        overVideoButton.actions.add(bca);

        overVideoButton.writeTag(sos2);

        DefineButton2Tag progressBarButton = new DefineButton2Tag(swf);
        progressBarButton.buttonId = progressBarButtonId;
        progressBarButton.characters.add(br);

        bca = new BUTTONCONDACTION(swf, overVideoButton);
        bca.isLast = true;

        ap = new ActionScript2Parser(swf, bca);
        try {
            bca.setActions(ap.actionsFromString("on(press){"
                    + "var f = Math.round(_root._xmouse*" + numFrames + "/" + (width / SWF.unitDivisor) + ");"
                    + "if(stopped){"
                    + "gotoAndStop(f);"
                    + "}else{"
                    + "gotoAndPlay(f);"
                    + "}"
                    + "}"));
        } catch (CompilationException ex) {
            Logger.getLogger(PreviewExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ActionParseException ex) {
            Logger.getLogger(PreviewExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        progressBarButton.actions.add(bca);

        progressBarButton.writeTag(sos2);

        Matrix m;

        m = new Matrix();
        m.translate(xmin, ymin + height - progressBarHeight * 20);
        m.scale(1, progressBarHeight * 20);

        new PlaceObject2Tag(swf, false, 2, progressBarShapeId, m.toMATRIX(), null, -1, null, -1, null).writeTag(sos2);

        m = new Matrix();
        m.scale(width, height - progressBarHeight * 20);

        new PlaceObject2Tag(swf, false, 3, overVideoButtonId, m.toMATRIX(), null, -1, null, -1, null).writeTag(sos2);

        m = new Matrix();
        m.translate(xmin, ymin + height - progressBarHeight * 20);
        m.scale(width, progressBarHeight * 20);

        new PlaceObject2Tag(swf, false, 4, progressBarButtonId, m.toMATRIX(), null, -1, null, -1, null).writeTag(sos2);

        DoActionTag doAction;
        doAction = new DoActionTag(swf);
        ap = new ActionScript2Parser(swf, doAction);
        try {
            doAction.setActions(ap.actionsFromString("var stopped = false;"));
        } catch (CompilationException ex) {
            Logger.getLogger(PreviewExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ActionParseException ex) {
            Logger.getLogger(PreviewExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        doAction.writeTag(sos2);
    }

    public SWFHeader exportSwf(OutputStream os, TreeItem treeItem, Color backgroundColor, int fontPageNum, boolean showControls) throws IOException, ActionParseException {
        SWF swf = treeItem.getSwf();

        int frameCount = 1;
        float frameRate = swf.frameRate;
        HashMap<Integer, VideoFrameTag> videoFrames = new HashMap<>();
        if (treeItem instanceof DefineVideoStreamTag) {
            DefineVideoStreamTag vs = (DefineVideoStreamTag) treeItem;
            SWF.populateVideoFrames(vs.getCharacterId(), swf.getTags(), videoFrames);
            frameCount = videoFrames.size();
        }

        List<SoundStreamBlockTag> soundFrames = new ArrayList<>();
        if (treeItem instanceof SoundStreamHeadTypeTag) {
            soundFrames = ((SoundStreamHeadTypeTag) treeItem).getBlocks();
            frameCount = soundFrames.size();
        }

        if ((treeItem instanceof DefineMorphShapeTag) || (treeItem instanceof DefineMorphShape2Tag)) {
            frameRate = MORPH_SHAPE_ANIMATION_FRAME_RATE;
            frameCount = (int) (MORPH_SHAPE_ANIMATION_LENGTH * frameRate);
        }

        if (treeItem instanceof DefineSoundTag) {
            frameCount = 1;
        }

        if (treeItem instanceof DefineSpriteTag) {
            frameCount = ((DefineSpriteTag) treeItem).frameCount;
        }

        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            SWFOutputStream sos2 = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
            RECT outrect = new RECT(swf.displayRect);

            RECT treeItemBounds = null;
            if (treeItem instanceof FontTag) {
                outrect.Xmin = 0;
                outrect.Ymin = 0;
                outrect.Xmax = FontTag.PREVIEWSIZE * 20;
                outrect.Ymax = FontTag.PREVIEWSIZE * 20;
            } else if (treeItem instanceof BoundedTag) {
                treeItemBounds = ((BoundedTag) treeItem).getRect();
            } else if (treeItem instanceof Frame) {
                treeItemBounds = ((Frame) treeItem).timeline.timelined.getRect();
            }

            if (showControls) {
                outrect = new RECT(treeItemBounds);
            } else {
                if (treeItemBounds != null) {
                    if (outrect.getWidth() < treeItemBounds.getWidth()) {
                        outrect.Xmax += treeItemBounds.getWidth() - outrect.getWidth();
                    }

                    if (outrect.getHeight() < treeItemBounds.getHeight()) {
                        outrect.Ymax += treeItemBounds.getHeight() - outrect.getHeight();
                    }
                }
            }

            if (!(treeItem instanceof DefineVideoStreamTag) && !(treeItem instanceof MorphShapeTag)) {
                showControls = false;
            }

            int progressBarHeight = 20;
            if (showControls) {
                outrect.Ymax += progressBarHeight * 20;
            }

            int width = outrect.getWidth();
            int height = outrect.getHeight();

            sos2.writeRECT(outrect);
            sos2.writeFIXED8(frameRate);
            sos2.writeUI16(frameCount); //framecnt

            FileAttributesTag fa = swf.getFileAttributes();
            if (fa != null) {
                fa.writeTag(sos2);
            }

            SetBackgroundColorTag setBgColorTag = swf.getBackgroundColor();
            if (setBgColorTag == null && backgroundColor != null) {
                setBgColorTag = new SetBackgroundColorTag(swf, new RGB(backgroundColor));
            }

            if (setBgColorTag != null) {
                setBgColorTag.writeTag(sos2);
            }

            if (treeItem instanceof Frame) {
                Frame fn = (Frame) treeItem;
                Timelined parent = fn.timeline.timelined;

                Set<Integer> doneCharacters = new HashSet<>();
                for (Tag t : parent.getTags()) {
                    if (t instanceof FileAttributesTag || t instanceof SetBackgroundColorTag) {
                        continue;
                    }

                    if (t instanceof DoActionTag || t instanceof DoInitActionTag) {
                        // todo: Maybe DoABC tags should be removed, too
                        continue;
                    }

                    Set<Integer> needed = new HashSet<>();
                    t.getNeededCharactersDeep(needed);
                    for (int n : needed) {
                        if (!doneCharacters.contains(n)) {
                            writeTag(swf.getCharacter(n), sos2);
                            doneCharacters.add(n);
                        }
                    }

                    //if (t instanceof ShowFrameTag || t instanceof PlaceObjectTypeTag || t instanceof RemoveTag) {
                    //    continue;
                    //}
                    if (t instanceof CharacterTag) {
                        int characterId = ((CharacterTag) t).getCharacterId();
                        doneCharacters.add(characterId);
                        writeTag(t, sos2);
                    }
                }

                RECT r = parent.getRect();
                for (Map.Entry<Integer, DepthState> value : fn.layers.entrySet()) {
                    PlaceObjectTypeTag pot = value.getValue().toPlaceObjectTag(value.getKey());
                    MATRIX mat = new MATRIX(pot.getMatrix());
                    mat.translateX += width / 2 - r.getWidth() / 2;
                    mat.translateY += height / 2 - r.getHeight() / 2;
                    pot.setMatrix(mat);
                    pot.writeTag(sos2);
                }

                new ShowFrameTag(swf).writeTag(sos2);
            } else {
                boolean isSprite = false;
                if (treeItem instanceof DefineSpriteTag) {
                    isSprite = true;
                }
                int chtId = -1;
                if (treeItem instanceof CharacterTag) {
                    chtId = ((CharacterTag) treeItem).getCharacterId();
                }

                if (treeItem instanceof DefineBitsTag) {
                    JPEGTablesTag jtt = swf.getJtt();
                    if (jtt != null) {
                        jtt.writeTag(sos2);
                    }
                } else if (treeItem instanceof AloneTag) {
                } else {
                    Set<Integer> needed = new HashSet<>();
                    ((Tag) treeItem).getNeededCharactersDeep(needed);
                    for (int n : needed) {
                        if (isSprite && chtId == n) {
                            continue;
                        }

                        CharacterTag characterTag = swf.getCharacter(n);
                        if (characterTag instanceof DefineBitsTag) {
                            JPEGTablesTag jtt = swf.getJtt();
                            if (jtt != null) {
                                jtt.writeTag(sos2);
                            }
                        }

                        writeTag(characterTag, sos2);
                    }
                }

                writeTag((Tag) treeItem, sos2);

                MATRIX mat = new MATRIX();
                mat.hasRotate = false;
                mat.hasScale = false;
                mat.translateX = 0;
                mat.translateY = 0;
                int rxmin = 0;
                int rymin = 0;
                if (treeItem instanceof BoundedTag) {
                    RECT r = ((BoundedTag) treeItem).getRect();
                    rxmin = r.Xmin;
                    rymin = r.Ymin;
                    /*mat.translateX = -r.Xmin;
                    mat.translateY = -r.Ymin;*/
                    mat.translateX = mat.translateX + width / 2 - r.getWidth() / 2;
                    mat.translateY = mat.translateY + (showControls ? height - progressBarHeight * 20 : height) / 2 - r.getHeight() / 2;
                } else {
                    mat.translateX = width / 4;
                    mat.translateY = height / 4;
                }
                if (treeItem instanceof FontTag) {
                    FontTag ft = (FontTag) classicTag((Tag) treeItem);

                    int countGlyphsTotal = ft.getGlyphShapeTable().size();
                    int countGlyphs = Math.min(SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW, countGlyphsTotal);
                    int fontId = ft.getFontId();
                    int cols = (int) Math.ceil(Math.sqrt(countGlyphs));
                    int rows = (int) Math.ceil(((float) countGlyphs) / ((float) cols));
                    if (rows == 0) {
                        rows = 1;
                        cols = 1;
                    }
                    int x = 0;
                    int y = 0;
                    int firstGlyphIndex = fontPageNum * SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW;
                    countGlyphs = Math.min(SHAPERECORD.MAX_CHARACTERS_IN_FONT_PREVIEW, countGlyphsTotal - firstGlyphIndex);
                    List<SHAPE> shapes = ft.getGlyphShapeTable();
                    int maxw = 0;
                    for (int f = firstGlyphIndex; f < firstGlyphIndex + countGlyphs; f++) {
                        RECT b = shapes.get(f).getBounds();
                        if (b.Xmin == Integer.MAX_VALUE) {
                            continue;
                        }
                        if (b.Ymin == Integer.MAX_VALUE) {
                            continue;
                        }
                        int w = (int) (b.getWidth() / ft.getDivider());
                        if (w > maxw) {
                            maxw = w;
                        }
                        x++;
                    }

                    x = 0;

                    int BORDER = 3 * 20;

                    int textHeight = height / rows;

                    while (maxw * textHeight / 1024.0 > width / cols - 2 * BORDER) {
                        textHeight--;
                    }

                    MATRIX tmat = new MATRIX();
                    for (int f = firstGlyphIndex; f < firstGlyphIndex + countGlyphs; f++) {
                        if (x >= cols) {
                            x = 0;
                            y++;
                        }
                        List<TEXTRECORD> rec = new ArrayList<>();
                        TEXTRECORD tr = new TEXTRECORD();

                        RECT b = shapes.get(f).getBounds();
                        int xmin = b.Xmin == Integer.MAX_VALUE ? 0 : (int) (b.Xmin / ft.getDivider());
                        xmin *= textHeight / 1024.0;
                        int ymin = b.Ymin == Integer.MAX_VALUE ? 0 : (int) (b.Ymin / ft.getDivider());
                        ymin *= textHeight / 1024.0;
                        int w = (int) (b.getWidth() / ft.getDivider());
                        w *= textHeight / 1024.0;
                        int h = (int) (b.getHeight() / ft.getDivider());
                        h *= textHeight / 1024.0;

                        tr.fontId = fontId;
                        tr.styleFlagsHasFont = true;
                        tr.textHeight = textHeight;
                        tr.xOffset = -xmin;
                        tr.yOffset = 0;
                        tr.styleFlagsHasXOffset = true;
                        tr.styleFlagsHasYOffset = true;
                        tr.glyphEntries = new ArrayList<>(1);
                        tr.styleFlagsHasColor = true;
                        tr.textColor = new RGB(0, 0, 0);
                        GLYPHENTRY ge = new GLYPHENTRY();

                        double ga = ft.getGlyphAdvance(f);
                        int cw = ga == -1 ? w : (int) (ga / ft.getDivider() * textHeight / 1024.0);

                        ge.glyphAdvance = 0;
                        ge.glyphIndex = f;
                        tr.glyphEntries.add(ge);
                        rec.add(tr);

                        tmat.translateX = x * width / cols + width / cols / 2 - w / 2;
                        tmat.translateY = y * height / rows + height / rows / 2;
                        new DefineTextTag(swf, 999 + f, new RECT(0, cw, ymin, ymin + h), new MATRIX(), rec).writeTag(sos2);
                        new PlaceObject2Tag(swf, false, 1 + f, 999 + f, tmat, null, 0, null, -1, null).writeTag(sos2);
                        x++;
                    }
                    new ShowFrameTag(swf).writeTag(sos2);
                } else if ((treeItem instanceof DefineMorphShapeTag) || (treeItem instanceof DefineMorphShape2Tag)) {
                    if (showControls) {
                        addControls(rxmin, rymin, progressBarHeight, swf, sos2, width, 65536, height);
                    }
                    new PlaceObject2Tag(swf, false, 1, chtId, mat, null, 0, null, -1, null).writeTag(sos2);
                    new ShowFrameTag(swf).writeTag(sos2);
                    for (int ratio = 0; ratio < 65536; ratio += 65536 / frameCount) {
                        new PlaceObject2Tag(swf, true, 1, chtId, mat, null, ratio, null, -1, null).writeTag(sos2);
                        if (showControls) {
                            updateProgressBar(rxmin, rymin, swf, sos2, width, height, progressBarHeight, ratio, 65536);
                        }
                        new ShowFrameTag(swf).writeTag(sos2);
                    }
                } else if (treeItem instanceof SoundStreamHeadTypeTag) {
                    for (SoundStreamBlockTag blk : soundFrames) {
                        blk.writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);
                    }
                } else if (treeItem instanceof DefineSoundTag) {
                    ExportAssetsTag ea = new ExportAssetsTag(swf);
                    DefineSoundTag ds = (DefineSoundTag) treeItem;
                    ea.tags.add(ds.soundId);
                    ea.names.add("my_define_sound");
                    ea.writeTag(sos2);
                    List<Action> actions;
                    DoActionTag doa;

                    doa = new DoActionTag(swf, null);
                    actions = ASMParser.parse(0, false,
                            "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\"\n"
                            + "Push \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\" 0.0 \"Sound\"\n"
                            + "NewObject\n"
                            + "SetMember\n"
                            + "Push \"my_define_sound\" 1 \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\"\n"
                            + "GetMember\n"
                            + "Push \"attachSound\"\n"
                            + "CallMethod\n"
                            + "Pop\n"
                            + "Stop", swf.version, false);
                    doa.setActions(actions);
                    doa.writeTag(sos2);
                    new ShowFrameTag(swf).writeTag(sos2);

                    actions = ASMParser.parse(0, false,
                            "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"start\"\n"
                            + "StopSounds\n"
                            + "Push \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\" 0.0 \"Sound\"\n"
                            + "NewObject\n"
                            + "SetMember\n"
                            + "Push \"my_define_sound\" 1 \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\"\n"
                            + "GetMember\n"
                            + "Push \"attachSound\"\n"
                            + "CallMethod\n"
                            + "Pop\n"
                            + "Push 9999 0.0 2 \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\"\n"
                            + "GetMember\n"
                            + "Push \"start\"\n"
                            + "CallMethod\n"
                            + "Pop\n"
                            + "Stop", swf.version, false);
                    doa.setActions(actions);
                    doa.writeTag(sos2);
                    new ShowFrameTag(swf).writeTag(sos2);

                    actions = ASMParser.parse(0, false,
                            "ConstantPool \"_root\" \"my_sound\" \"Sound\" \"my_define_sound\" \"attachSound\" \"onSoundComplete\" \"start\" \"execParam\"\n"
                            + "StopSounds\n"
                            + "Push \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\" 0.0 \"Sound\"\n"
                            + "NewObject\n"
                            + "SetMember\n"
                            + "Push \"my_define_sound\" 1 \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\"\n"
                            + "GetMember\n"
                            + "Push \"attachSound\"\n"
                            + "CallMethod\n"
                            + "Pop\n"
                            + "Push \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\"\n"
                            + "GetMember\n"
                            + "Push \"onSoundComplete\"\n"
                            + "DefineFunction2 \"\" 0 2 false true true false true false true false false  {\n"
                            + "Push 0.0 register1 \"my_sound\"\n"
                            + "GetMember\n"
                            + "Push \"start\"\n"
                            + "CallMethod\n"
                            + "Pop\n"
                            + "}\n"
                            + "SetMember\n"
                            + "Push \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"execParam\"\n"
                            + "GetMember\n"
                            + "Push 1 \"_root\"\n"
                            + "GetVariable\n"
                            + "Push \"my_sound\"\n"
                            + "GetMember\n"
                            + "Push \"start\"\n"
                            + "CallMethod\n"
                            + "Pop\n"
                            + "Stop", swf.version, false);
                    doa.setActions(actions);
                    doa.writeTag(sos2);
                    new ShowFrameTag(swf).writeTag(sos2);

                    actions = ASMParser.parse(0, false,
                            "StopSounds\n"
                            + "Stop", swf.version, false);
                    doa.setActions(actions);
                    doa.writeTag(sos2);
                    new ShowFrameTag(swf).writeTag(sos2);

                    new ShowFrameTag(swf).writeTag(sos2);
                } else if (treeItem instanceof DefineVideoStreamTag) {
                    List<VideoFrameTag> frs = new ArrayList<>(videoFrames.values());
                    Collections.sort(frs, new Comparator<VideoFrameTag>() {
                        @Override
                        public int compare(VideoFrameTag o1, VideoFrameTag o2) {
                            return o1.frameNum - o2.frameNum;
                        }
                    });
                    if (showControls) {
                        addControls(rxmin, rymin, progressBarHeight, swf, sos2, width, videoFrames.size(), height);
                    }
                    new PlaceObject2Tag(swf, false, 1, chtId, mat, null, -1, null, -1, null).writeTag(sos2);
                    boolean first = true;
                    int ratio = 0;
                    for (VideoFrameTag f : frs) {
                        if (!first) {
                            ratio++;
                            new PlaceObject2Tag(swf, true, 1, -1, null, null, ratio, null, -1, null).writeTag(sos2);
                            if (showControls) {
                                updateProgressBar(rxmin, rymin, swf, sos2, width, height, progressBarHeight, ratio, videoFrames.size());
                            }
                        }
                        f.writeTag(sos2);
                        new ShowFrameTag(swf).writeTag(sos2);
                        first = false;
                    }
                } else if (treeItem instanceof DefineSpriteTag) {
                    DefineSpriteTag s = (DefineSpriteTag) treeItem;
                    Tag lastTag = null;
                    for (Tag t : s.getTags()) {
                        if (t instanceof EndTag) {
                            break;
                        } else if (t instanceof PlaceObjectTypeTag) {
                            PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                            MATRIX m = pt.getMatrix();
                            MATRIX m2 = new Matrix(m).preConcatenate(new Matrix(mat)).toMATRIX();
                            pt.writeTagWithMatrix(sos2, m2);
                            lastTag = t;
                        } else {
                            t.writeTag(sos2);
                            lastTag = t;
                        }
                    }
                    if (!s.getTags().isEmpty() && (lastTag != null) && (!(lastTag instanceof ShowFrameTag))) {
                        new ShowFrameTag(swf).writeTag(sos2);
                    }
                } else {
                    new PlaceObject2Tag(swf, false, 1, chtId, mat, null, 0, null, -1, null).writeTag(sos2);
                    new ShowFrameTag(swf).writeTag(sos2);
                }

            } // not showframe

            new EndTag(swf).writeTag(sos2);
            data = baos.toByteArray();
        }

        SWFHeader result = new SWFHeader();
        result.version = Math.max(10, swf.version);
        result.frameRate = frameRate;

        SWFOutputStream sos = new SWFOutputStream(os, result.version);
        sos.write("FWS".getBytes());
        sos.write(swf.version);
        result.fileSize = sos.getPos() + data.length + 4;
        sos.writeUI32(result.fileSize);
        sos.write(data);
        os.flush();

        return result;
    }

    private static Tag classicTag(Tag t) {
        if (t instanceof DefineCompactedFont) {
            return ((DefineCompactedFont) t).toClassicFont();
        }
        return t;
    }

    private static void writeTag(Tag t, SWFOutputStream sos) throws IOException {
        t = classicTag(t);

        t.writeTag(sos);
        if (t instanceof CharacterIdTag) {
            List<CharacterIdTag> chIdTags = t.getSwf().getCharacterIdTags(((CharacterIdTag) t).getCharacterId());
            if (chIdTags != null) {
                for (CharacterIdTag chIdTag : chIdTags) {
                    if (!(chIdTag instanceof PlaceObjectTypeTag || chIdTag instanceof RemoveTag)) {
                        ((Tag) chIdTag).writeTag(sos);
                    }
                }
            }
        }
    }
}
