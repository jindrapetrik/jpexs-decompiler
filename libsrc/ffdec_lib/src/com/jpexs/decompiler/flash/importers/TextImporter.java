/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.TextImportErrorHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class TextImporter {

    private final MissingCharacterHandler missingCharacterHandler;

    private final TextImportErrorHandler errorHandler;

    public TextImporter(MissingCharacterHandler missingCharacterHandler, TextImportErrorHandler errorHandler) {
        this.missingCharacterHandler = missingCharacterHandler;
        this.errorHandler = errorHandler;
    }

    private Map<Integer, String[]> splitTextRecords(String texts) {
        String[] textsArr = texts.split(Helper.newLine + Configuration.textExportSingleFileSeparator.get() + Helper.newLine);
        String recordSeparator = Helper.newLine + Configuration.textExportSingleFileRecordSeparator.get() + Helper.newLine;
        Map<Integer, String[]> result = new HashMap<>();
        for (String text : textsArr) {
            String[] textArr = text.split(Helper.newLine, 2);
            String idLine = textArr[0];
            if (idLine.startsWith("ID:")) {
                int id = Integer.parseInt(idLine.substring(3).trim());
                String[] records = textArr[1].split(recordSeparator);
                result.put(id, records);
            } else if (errorHandler.handle(null)) {
                return null;
            }
        }
        return result;
    }

    public void importTextsSingleFile(File textsFile, SWF swf) {
        String texts = Helper.readTextFile(textsFile.getPath());
        Map<Integer, String[]> records = splitTextRecords(texts);
        boolean ignoreLetterSpacing = Configuration.resetLetterSpacingOnTextImport.get();
        if (records != null) {
            for (int characterId : records.keySet()) {
                TextTag textTag = swf.getText(characterId);
                if (textTag != null) {
                    String[] currentRecords = records.get(characterId);
                    String text = textTag.getFormattedText(ignoreLetterSpacing).text;
                    if (!saveText(textTag, text, currentRecords)) {
                        return;
                    }
                }
            }
        }
    }

    public void importTextsSingleFileFormatted(File textsFile, SWF swf) {
        String texts = Helper.readTextFile(textsFile.getPath());
        Map<Integer, String[]> records = splitTextRecords(texts);
        if (records != null) {
            for (int characterId : records.keySet()) {
                TextTag textTag = swf.getText(characterId);
                if (textTag != null) {
                    String[] currentRecords = records.get(characterId);
                    if (!saveText(textTag, currentRecords[0], null)) {
                        return;
                    }
                }
            }
        }
    }

    public void importTextsMultipleFiles(String folder, SWF swf) {
        File textsFolder = new File(Path.combine(folder, TextExportSettings.EXPORT_FOLDER_NAME));
        String[] files = textsFolder.list(new FilenameFilter() {

            private final Pattern pat = Pattern.compile("\\d+\\.txt", Pattern.CASE_INSENSITIVE);

            @Override
            public boolean accept(File dir, String name) {

                return pat.matcher(name).matches();
            }
        });

        if (files == null) {
            return;
        }

        for (String fileName : files) {
            String texts = Helper.readTextFile(Path.combine(textsFolder.getPath(), fileName));
            int characterId = Integer.parseInt(fileName.split("\\.")[0]);
            TextTag textTag = swf.getText(characterId);
            if (!importText(textTag, texts)) {
                return;
            }
        }
    }

    public boolean importText(TextTag textTag, String newText) {
        String recordSeparator = Helper.newLine + Configuration.textExportSingleFileRecordSeparator.get() + Helper.newLine;
        boolean formatted = !newText.contains(recordSeparator) && newText.startsWith("[" + Helper.newLine);
        boolean ignoreLetterSpacing = Configuration.resetLetterSpacingOnTextImport.get();
        if (!formatted) {
            String[] records = newText.split(recordSeparator);
            if (textTag != null) {
                String text = textTag.getFormattedText(ignoreLetterSpacing).text;
                if (!saveText(textTag, text, records)) {
                    return false;
                }
            }
        } else if (textTag != null) {
            if (!saveText(textTag, newText, null)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Stores the new text to the text tag
     *
     * @param textTag
     * @param formattedText
     * @param texts
     * @return If false the processing should be interrupted
     */
    private boolean saveText(TextTag textTag, String formattedText, String[] texts) {
        try {
            if (textTag.setFormattedText(missingCharacterHandler, formattedText, texts)) {
                return true;
            }

            return !errorHandler.handle(textTag);
        } catch (TextParseException ex) {
            return !errorHandler.handle(textTag, ex.text, ex.line);
        }
    }

    public static int getTextTagType(String format) {
        int res = 0;
        switch (format) {
            case "text":
                res = DefineTextTag.ID;
                break;
            case "text2":
                res = DefineText2Tag.ID;
                break;
            case "edittext":
                res = DefineEditTextTag.ID;
                break;
        }

        return res;
    }
}
