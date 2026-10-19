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
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.iggy.IggyDeclStrings;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyFont;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyText;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.helpers.MemoryInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Converts SWF to Iggy.
 *
 * @author JPEXS
 */
public class SwfToIggyConvertor {

    /**
     * Convert a SWF without using an existing Iggy file as a template.
     *
     * @param swf Source movie
     * @param variant Target pointer width and root layout
     * @param swfName Movie/library name
     * @return A fully materialized, editable native container
     * @throws IOException If the SWF cannot be represented in this layout
     */
    public static IggyFile createIggy(SWF swf, IggyFile.Variant variant, String swfName) throws IOException {
        validateNewMovie(swf);
        IggyFile file = IggyFile.create(variant, swfName);
        file.getHeader().setSwfVersion(swf.version);
        updateIggy(file.getSwf(), swf);
        file.updateFlashEntry();
        MemoryDataStream bytes = new MemoryDataStream();
        file.writeToDataStream(bytes);
        // Establish the generated definitions as the baseline for subsequent edits.
        return new IggyFile(new MemoryDataStream(bytes.getAllBytes()));
    }

    /**
     * Convert with warnings for omitted unsupported tags, leaving the source
     * intact. A null warning handler selects the strict conversion behavior.
     */
    public static IggyFile createIggy(SWF swf, IggyFile.Variant variant, String swfName,
            Consumer<String> warningHandler) throws IOException {
        if (warningHandler == null) {
            return createIggy(swf, variant, swfName);
        }
        validateAlchemy(swf);
        // Keep warning retries attached to the tags owned by the input filter.
        swf = IggyDefinitionConvertor.expandSymbolAliases(null, swf);
        IggySwfInputFilter input = new IggySwfInputFilter(swf, variant, warningHandler);
        while (true) {
            try {
                return createIggy(input.getSwf(), variant, swfName);
            } catch (IggyUnsupportedTagException ex) {
                input.skip(ex.getTag(), ex.getMessage());
            }
        }
    }

    /**
     * Convert to the default 64-bit layout.
     */
    public static IggyFile createIggy(SWF swf, String swfName) throws IOException {
        return createIggy(swf, IggyFile.Variant.X64, swfName);
    }

    /**
     * Convert and save without opening the output file until conversion
     * succeeds.
     */
    public static void exportIggyToFile(SWF swf, File output, IggyFile.Variant variant, String swfName) throws IOException {
        createIggy(swf, variant, swfName).saveTo(output);
    }

    /**
     * Convert and save, reporting unsupported tags instead of aborting.
     */
    public static void exportIggyToFile(SWF swf, File output, IggyFile.Variant variant, String swfName,
            Consumer<String> warningHandler) throws IOException {
        createIggy(swf, variant, swfName, warningHandler).saveTo(output);
    }

    static boolean supportsNewTag(Tag tag) {
        if (tag instanceof CharacterTag) {
            try {
                IggyDefinitionConvertor.type((CharacterTag) tag);
                // CFF fonts have no supported native representation.
                return tag.getId() != 91;
            } catch (IggyUnsupportedTagException ex) {
                return false;
            }
        }
        switch (tag.getId()) {
            case 0:
            case 1:
            case 4:
            case 5:
            case 8: // JPEGTables are incorporated into each DefineBits JPEG.
            case 9:
            case 13:
            case 15:
            case 23:
            case 26:
            case 28:
            case 43:
            case 57:
            case 62:
            case 69:
            case 70:
            case 71:
            case 72:
            case 76:
            case 78:
            case 82:
            case 86:
            case 89:
                return true;
            default:
                return false;
        }
    }

    private static void validateFrameCount(Iterable<Tag> tags, int frameCount) throws IOException {
        int frames = 0;
        for (Tag tag : tags) {
            if (tag.getId() == 1) {
                frames++;
            }
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) tag;
                validateFrameCount(sprite.getTags(), sprite.frameCount);
            }
        }
        if (frameCount < 0 || frameCount > 65535 || frames > frameCount) {
            throw new IOException("Invalid timeline frame count in a new Iggy");
        }
    }

    private static void validateAlchemy(SWF swf) throws IOException {
        int abcIndex = 0;
        for (Tag tag : IggyDefinitionConvertor.tags(swf.getTags())) {
            if (!(tag instanceof ABCContainerTag)) {
                continue;
            }
            for (MethodBody body : ((ABCContainerTag) tag).getABC().bodies) {
                // Decode a separate copy: getCode() caches and normalizes the
                // source body, including branches in unreachable instructions.
                try (ABCInputStream input = new ABCInputStream(new MemoryInputStream(body.getCodeBytes()))) {
                    for (AVM2Instruction instruction : new AVM2Code(input, body).code) {
                        if (instruction.definition.hasFlag(AVM2InstructionFlag.DOMAIN_MEMORY)) {
                            throw new IOException("Cannot convert SWF to IGGY: Alchemy instruction "
                                    + instruction.definition.instructionName + " in " + tag.getName()
                                    + " (ABC " + abcIndex + ", method " + body.method_info
                                    + ", offset " + instruction.getAddress() + ") is not supported");
                        }
                    }
                }
            }
            abcIndex++;
        }
    }

    private static void validateNewMovie(SWF swf) throws IOException {
        if (swf == null || swf.displayRect == null || !Float.isFinite(swf.frameRate) || swf.frameRate <= 0
                || swf.frameCount < 0 || swf.frameCount > 65535) {
            throw new IOException("Invalid movie properties for a new Iggy");
        }
        validateFrameCount(swf.getTags(), swf.frameCount);
        List<Tag> tags = IggyDefinitionConvertor.tags(swf.getTags());
        Set<Integer> available = new HashSet<>();
        for (Tag tag : tags) {
            if (tag instanceof CharacterTag) {
                available.add(((CharacterTag) tag).getCharacterId());
            }
            if (tag instanceof ImportTag) {
                available.addAll(((ImportTag) tag).getAssets().keySet());
            }
        }
        for (Tag tag : tags) {
            Set<Integer> needed = new HashSet<>();
            tag.getNeededCharacters(needed, new HashSet<String>(), swf);
            needed.remove(0);
            needed.removeAll(available);
            if (!needed.isEmpty()) {
                throw new IOException("Missing character in a new Iggy: " + needed.iterator().next());
            }
            if (tag instanceof CharacterTag) {
                // The definition converter performs the detailed type and ID validation.
                continue;
            }
            if (supportsNewTag(tag)) {
                continue;
            }
            switch (tag.getId()) {
                case 24: // Protect
                case 41: // ProductInfo
                case 58: // EnableDebugger
                case 63: // DebugID
                case 64: // EnableDebugger2
                case 65: // ScriptLimits
                case 77: // Metadata
                    // These SWF authoring/player settings have no native Iggy table.
                    break;
                default:
                    throw new IggyUnsupportedTagException(tag, "Unsupported tag in a new Iggy: " + tag.getName());
            }
        }
    }

    /**
     * Standalone conversion: input.swf output.iggy ["32 legacy"|32|64].
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2 || args.length > 3) {
            throw new IOException("Usage: SwfToIggyConvertor input.swf output.iggy [\"32 legacy\"|32|64]");
        }
        IggyFile.Variant variant = IggyFile.Variant.X64;
        if (args.length == 3) {
            variant = IggyFile.Variant.fromString(args[2]);
        }
        File input = new File(args[0]);
        SWF swf;
        try (InputStream stream = new FileInputStream(input)) {
            swf = new SWF(stream, input.getAbsolutePath(), input.getName(), false);
        }
        exportIggyToFile(swf, new File(args[1]), variant, input.getName(), warning -> System.err.println("Warning: " + warning));
    }

    /**
     * Update a container, including an explicitly changed SWF version. An
     * unchanged fallback version preserves an unknown original header word.
     *
     * @param file Target container
     * @param swf Edited movie
     * @throws IOException If the movie cannot be represented
     */
    public static void updateIggy(IggyFile file, SWF swf) throws IOException {
        int previousVersion = file.getSwfVersion();
        updateIggy(file.getSwf(), swf);
        if (swf.version != previousVersion) {
            file.getHeader().setSwfVersion(swf.version);
        }
    }

    public static void updateIggy(IggySwf iggySwf, SWF swf) throws IOException {
        validateAlchemy(swf);
        swf = IggyDefinitionConvertor.expandSymbolAliases(iggySwf, swf);
        IggyDefinitionConvertor.validate(iggySwf, swf);
        IggyImportConvertor.updateImports(iggySwf, swf);
        IggyAssetConvertor.updateAssets(iggySwf, swf);
        IggyDefinitionConvertor.update(iggySwf, swf);
        IggyMovieTagConvertor.update(iggySwf, swf);
        // Compare before changing movie properties so the original frame count remains available.
        IggyTimelineConvertor.update(swf, iggySwf, 0, swf.getTags(), swf.frameCount);
        Set<Integer> sprites = new HashSet<>();
        for (Tag tag : IggyDefinitionConvertor.tags(swf.getTags())) {
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) tag;
                if (!sprites.add(sprite.spriteId)) {
                    throw new IOException("Duplicate Iggy sprite ID");
                }
                IggyTimelineConvertor.update(swf, iggySwf, sprite.spriteId, sprite.getTags(), sprite.frameCount);
            }
        }
        if (!sprites.equals(iggySwf.getSpriteIds())) {
            throw new IOException("Missing original Iggy sprite definitions");
        }
        long color = iggySwf.getHdr().getBackgroundColor();
        for (Tag tag : swf.getTags()) {
            if (tag instanceof SetBackgroundColorTag) {
                SetBackgroundColorTag background = (SetBackgroundColorTag) tag;
                color = (color & 0xff000000L) | background.backgroundColor.red
                        | (long) background.backgroundColor.green << 8 | (long) background.backgroundColor.blue << 16;
            }
        }
        iggySwf.getHdr().setMovieProperties(Math.round(swf.displayRect.Xmin / SWF.unitDivisor), Math.round(swf.displayRect.Ymin / SWF.unitDivisor),
                Math.round(swf.displayRect.Xmax / SWF.unitDivisor), Math.round(swf.displayRect.Ymax / SWF.unitDivisor), swf.frameRate, swf.frameCount, color);
        IggyFontConvertor.updateFonts(iggySwf, swf);
        List<DefineEditTextTag> textTags = new ArrayList<>();
        List<ABCContainerTag> abcTags = new ArrayList<>();

        for (Tag t : IggyDefinitionConvertor.tags(swf.getTags())) {
            if (t instanceof DefineEditTextTag) {
                textTags.add((DefineEditTextTag) t);
            }
            if (t instanceof DoABC2Tag || t instanceof DoABCTag) {
                abcTags.add((ABCContainerTag) t);
            }
        }
        for (DefineEditTextTag textTag : textTags) {
            IggyText text = null;
            for (IggyText original : iggySwf.getTexts()) {
                if (original.getTextIndex() == textTag.characterID) {
                    text = original;
                    break;
                }
            }
            boolean added = text == null;
            if (added) {
                text = IggyText.create(iggySwf.is64(), textTag.characterID);
            }
            updateIggyText(text, textTag);
            if (added) {
                iggySwf.replaceDefinition(text);
            }
        }
        for (int i = 0; i < abcTags.size(); i++) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            abcTags.get(i).getABC().saveTo(bytes);
            if (i < iggySwf.getAbcBlocks().size()) {
                iggySwf.getAbcBlocks().get(i).setData(bytes.toByteArray());
            } else {
                iggySwf.getAbcBlocks().add(new IggyDeclStrings(bytes.toByteArray()));
            }
        }
        while (iggySwf.getAbcBlocks().size() > abcTags.size()) {
            iggySwf.getAbcBlocks().remove(iggySwf.getAbcBlocks().size() - 1);
        }
    }

    public static void updateIggyText(IggyText iggyText, DefineEditTextTag textTag) {
        iggyText.setBounds((float) (textTag.bounds.Xmin / SWF.unitDivisor),
                (float) (textTag.bounds.Ymin / SWF.unitDivisor), (float) (textTag.bounds.Xmax / SWF.unitDivisor),
                (float) (textTag.bounds.Ymax / SWF.unitDivisor));
        int flags = iggyText.getEnum_hex() & ~IggyText.SWF_BEHAVIOR_MASK;
        flags |= textTag.wordWrap ? IggyText.FLAG_WORD_WRAP : 0;
        flags |= textTag.multiline ? IggyText.FLAG_MULTILINE : 0;
        flags |= textTag.password ? IggyText.FLAG_PASSWORD : 0;
        flags |= textTag.readOnly ? IggyText.FLAG_READ_ONLY : 0;
        flags |= textTag.autoSize ? IggyText.FLAG_AUTO_SIZE : 0;
        flags |= textTag.noSelect ? IggyText.FLAG_NO_SELECT : 0;
        flags |= textTag.border ? IggyText.FLAG_BORDER : 0;
        flags |= textTag.wasStatic ? IggyText.FLAG_WAS_STATIC : 0;
        flags |= textTag.html ? IggyText.FLAG_HTML : 0;
        flags |= textTag.useOutlines ? IggyText.FLAG_USE_OUTLINES : 0;
        iggyText.setEnum_hex(flags);
        iggyText.setInitialText(textTag.hasText ? textTag.initialText : null);
        iggyText.setFontIndex(textTag.hasFont ? textTag.fontId : 0);
        iggyText.setFontClassName(textTag.hasFontClass ? textTag.fontClass : null);
        // SWF omits height when neither font reference is present. Retain that latent Iggy value.
        if (textTag.hasFont || textTag.hasFontClass) {
            iggyText.setFontHeight(textTag.fontHeight);
        }
        iggyText.setTextColorRgba(textTag.hasTextColor
                ? new byte[]{(byte) textTag.textColor.red, (byte) textTag.textColor.green,
                    (byte) textTag.textColor.blue, (byte) textTag.textColor.alpha}
                : new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
        iggyText.setMaxLength(textTag.hasMaxLength ? textTag.maxLength : 0);
        iggyText.setAlign(textTag.hasLayout ? textTag.align : 0);
        iggyText.setLeftMargin(textTag.hasLayout ? textTag.leftMargin : 0);
        iggyText.setRightMargin(textTag.hasLayout ? textTag.rightMargin : 0);
        iggyText.setIndent(textTag.hasLayout ? textTag.indent : 0);
        iggyText.setLeading(textTag.hasLayout ? textTag.leading : 0);
    }

    public static void updateIggyFont(IggyFont iggyFont, DefineFont2Tag fontTag) {
        try {
            IggyFontConvertor.updateFont(iggyFont, fontTag);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid Iggy font", ex);
        }
    }

    public static IggyFont createIggyFont(DefineFont2Tag fontTag) {
        byte[] commonHeader = new byte[28];
        commonHeader[12] = 1; // Null relative class-name pointer at native offset 16.
        IggyFont font = new IggyFont(IggyFont.ID, fontTag.fontID, commonHeader, 0,
                0, 0, 0, 0, 0, new float[5], 0, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 0, new byte[48], new byte[48],
                0, 0, 0, 0, 0, 0, 0, 0, "", new ArrayList<>(), new ArrayList<>(), null, null, null);
        updateIggyFont(font, fontTag);
        return font;
    }
}
