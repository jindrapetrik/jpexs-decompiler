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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.iggy.IggyDeclStrings;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggyNamedFrame;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggySymbolClass;
import com.jpexs.decompiler.flash.iggy.IggyText;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Iggy to SWF convertor. WIP
 *
 * @author JPEXS
 */
public class IggyToSwfConvertor {

    public static void exportAllSwfsToDir(IggyFile file, File outputDir) throws IOException {
        exportSwfToDir(file, outputDir);
    }

    public static void exportSwfToDir(IggyFile file, File outputDir) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, file.getSwfName()))) {
            exportSwf(file, fos);
        }
    }

    public static void exportSwfToFile(IggyFile file, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            exportSwf(file, fos);
        }
    }

    public static void exportSwf(IggyFile file, OutputStream output) throws IOException {
        SWF swf = getSwf(file);
        swf.saveTo(output);
    }

    private static int makeLengthsTwip(double val) {
        return (int) (val * SWF.unitDivisor);
    }

    public static SWF getSwf(IggyFile file) {
        SWF swf = new SWF();
        swf.compression = SWFCompression.NONE;
        swf.frameCount = (int) file.getSwf().getHdr().getFrameCount();
        swf.frameRate = file.getSwfFrameRate();
        swf.gfx = false;
        swf.setSwfProfile(new com.jpexs.decompiler.flash.profiles.IggySwfProfile());
        swf.displayRect = new RECT(
                makeLengthsTwip(file.getSwfXMin()),
                makeLengthsTwip(file.getSwfXMax()),
                makeLengthsTwip(file.getSwfYMin()),
                makeLengthsTwip(file.getSwfYMax()));
        swf.version = file.getSwfVersion();

        FileAttributesTag fat = new FileAttributesTag(swf);
        fat.actionScript3 = file.getSwf().hasAS3() || !file.getSwf().getAbcBlocks().isEmpty();
        fat.hasMetadata = false;
        fat.useNetwork = false;
        swf.addTag(fat);
        IggySwf iggySwf = file.getSwf();

        long color = iggySwf.getHdr().getBackgroundColor();
        swf.addTag(new SetBackgroundColorTag(swf, new RGB((int) color & 255, (int) (color >>> 8) & 255, (int) (color >>> 16) & 255)));
        DefineSceneAndFrameLabelDataTag labels = new DefineSceneAndFrameLabelDataTag(swf);
        labels.sceneOffsets = new long[iggySwf.getScenes().size()];
        labels.sceneNames = new String[labels.sceneOffsets.length];
        for (int i = 0; i < labels.sceneOffsets.length; i++) {
            IggyNamedFrame scene = iggySwf.getScenes().get(i);
            labels.sceneOffsets[i] = scene.getFrame();
            labels.sceneNames[i] = scene.getName();
        }
        labels.frameNums = new long[iggySwf.getFrameLabels().size()];
        labels.frameNames = new String[labels.frameNums.length];
        for (int i = 0; i < labels.frameNums.length; i++) {
            IggyNamedFrame label = iggySwf.getFrameLabels().get(i);
            labels.frameNums[i] = label.getFrame();
            labels.frameNames[i] = label.getName();
        }
        if (labels.sceneOffsets.length > 0 || labels.frameNums.length > 0) {
            swf.addTag(labels);
        }
        Set<Integer> exportedIds = new HashSet<>();
        IggyImportConvertor.exportImports(swf, iggySwf, exportedIds);
        Map<Integer, Integer> fontIndex2CharId = new HashMap<>();

        for (int fontIndex = 0; fontIndex < iggySwf.getFonts().size(); fontIndex++) {
            IggyFont iggyFont = iggySwf.getFonts().get(fontIndex);
            com.jpexs.decompiler.flash.tags.base.FontTag fontTag = IggyFontConvertor.toSwf(swf, iggyFont);
            fontIndex2CharId.put(fontIndex, fontTag.getCharacterId());
            exportedIds.add(fontTag.getCharacterId());
            fontTag.setModified(true);
            swf.addTag(fontTag);
        }

        Map<Integer, Integer> textIndex2CharId = new HashMap<>();

        for (int textIndex = 0; textIndex < iggySwf.getTexts().size(); textIndex++) {
            IggyText iggyText = iggySwf.getTexts().get(textIndex);
            DefineEditTextTag textTag = convertText(swf, iggyText);
            textIndex2CharId.put(textIndex, textTag.characterID);
            exportedIds.add(textTag.characterID);
            swf.addTag(textTag);
        }

        try {
            IggyAssetConvertor.exportAssets(swf, file, exportedIds);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid Iggy dictionary asset", ex);
        }

        for (Map.Entry<Integer, IggyTimeline> entry : iggySwf.getSpriteTimelines().entrySet()) {
            DefineSpriteTag sprite = new DefineSpriteTag(swf);
            sprite.spriteId = entry.getKey();
            try {
                sprite.frameCount = iggySwf.getTimelineFrameCount(sprite.spriteId);
                for (Tag tag : IggyTimelineConvertor.toSwf(swf, iggySwf, entry.getValue(), sprite.frameCount)) {
                    sprite.addTag(tag);
                }
            } catch (IOException ex) {
                throw new IllegalArgumentException("Invalid Iggy sprite timeline", ex);
            }
            sprite.addTag(new EndTag(swf));
            sprite.setModified(true);
            swf.addTag(sprite);
            exportedIds.add(sprite.spriteId);
        }

        try {
            IggyMovieTagConvertor.exportScalingGrids(swf, iggySwf);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid Iggy scaling grid", ex);
        }

        for (IggyDeclStrings abc : iggySwf.getAbcBlocks()) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try {
                bytes.write(new byte[]{1, 0, 0, 0, 0}); //DoABC flags and empty name
                bytes.write(abc.getData());
                byte[] payload = bytes.toByteArray();
                DoABC2Tag tag = new DoABC2Tag(new SWFInputStream(swf, payload), new ByteArrayRange(payload));
                tag.setModified(true);
                swf.addTag(tag);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Invalid ABC in Iggy movie", ex);
            }
        }
        SymbolClassTag symbols = new SymbolClassTag(swf);
        for (IggySymbolClass symbol : iggySwf.getSymbolClasses()) {
            int id = (int) symbol.getCharacterId();
            if (id == 0 || exportedIds.contains(id)) {
                symbols.tags.add(id);
                symbols.names.add(symbol.getName());
            }
        }
        for (IggyText text : iggySwf.getTexts()) {
            if (text.getSymbolClassName() != null && !symbols.tags.contains(text.getTextIndex())) {
                symbols.tags.add(text.getTextIndex());
                symbols.names.add(text.getSymbolClassName());
            }
        }
        if (!symbols.tags.isEmpty()) {
            swf.addTag(symbols);
        }
        try {
            for (Tag tag : IggyTimelineConvertor.toSwf(swf, iggySwf, iggySwf.getTimeline(), swf.frameCount)) {
                swf.addTag(tag);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid Iggy root timeline", ex);
        }

        swf.addTag(
                new EndTag(swf));
        swf.setModified(
                true);

        return swf;
    }

    /**
     * Convert the editable text properties shared by the x64 and x86 layouts.
     */
    public static DefineEditTextTag convertText(SWF swf, IggyText text) {
        DefineEditTextTag tag = new DefineEditTextTag(swf);
        tag.characterID = text.getTextIndex();
        tag.bounds = new RECT(
                (int) Math.round(text.getPar1() * SWF.unitDivisor),
                (int) Math.round(text.getPar3() * SWF.unitDivisor),
                (int) Math.round(text.getPar2() * SWF.unitDivisor),
                (int) Math.round(text.getPar4() * SWF.unitDivisor));
        int flags = text.getEnum_hex();
        tag.wordWrap = (flags & IggyText.FLAG_WORD_WRAP) != 0;
        tag.multiline = (flags & IggyText.FLAG_MULTILINE) != 0;
        tag.password = (flags & IggyText.FLAG_PASSWORD) != 0;
        tag.readOnly = (flags & IggyText.FLAG_READ_ONLY) != 0;
        tag.autoSize = (flags & IggyText.FLAG_AUTO_SIZE) != 0;
        tag.noSelect = (flags & IggyText.FLAG_NO_SELECT) != 0;
        tag.border = (flags & IggyText.FLAG_BORDER) != 0;
        tag.wasStatic = (flags & IggyText.FLAG_WAS_STATIC) != 0;
        tag.html = (flags & IggyText.FLAG_HTML) != 0;
        tag.useOutlines = (flags & IggyText.FLAG_USE_OUTLINES) != 0;
        tag.hasText = text.getInitialText() != null;
        tag.initialText = text.getInitialText();
        tag.hasFont = text.getFontIndex() != 0;
        tag.fontId = text.getFontIndex();
        tag.hasFontClass = text.getFontClassName() != null;
        tag.fontClass = text.getFontClassName();
        tag.fontHeight = text.getFontHeight(); // Already in twips.
        byte[] rgba = text.getTextColorRgba();
        tag.hasTextColor = true;
        tag.textColor = new RGBA(rgba[0] & 255, rgba[1] & 255, rgba[2] & 255, rgba[3] & 255);
        tag.hasMaxLength = text.getMaxLength() != 0;
        tag.maxLength = text.getMaxLength();
        tag.hasLayout = true;
        tag.align = text.getAlign();
        tag.leftMargin = text.getLeftMargin();
        tag.rightMargin = text.getRightMargin();
        tag.indent = text.getIndent();
        tag.leading = text.getLeading();
        // The DLL consumes and discards the SWF variable name.
        tag.variableName = "";
        tag.setModified(true);
        return tag;
    }

    public static void main(String[] args) {

        if (args.length < 2 || (args[0].isEmpty() || args[1].isEmpty())) {
            System.err.println("Invalid arguments");
            System.err.println("Usage: iggy-extract.bat file.iggy d:/outdir/");
            System.exit(1);
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("FAIL: Input file: " + file.getAbsolutePath() + " does not exist.");
            System.exit(1);
        }
        File outDir = new File(args[1]);
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                System.err.println("FAIL: Cannot create output directory");
                System.exit(1);
            }
        }

        try {
            System.out.print("(1/2) Loading file " + args[0] + "...");
            IggyFile iggyFile = new IggyFile(new File(args[0]));
            System.out.println("OK");
            System.out.print("(2/2) Exporting SWF files to " + args[1] + "...");
            exportAllSwfsToDir(iggyFile, new File(args[1]));
            System.out.println("OK");
            System.out.println("All finished successfully.");
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("FAIL");
            System.err.println("Error while converting: " + ex.getMessage());
            System.exit(1);
        }
    }
}
