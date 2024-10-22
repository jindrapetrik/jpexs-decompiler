/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Image importer.
 *
 * @author JPEXS
 */
public class ImageImporter extends TagImporter {

    /**
     * Imports image.
     * @param it Image tag
     * @param newData New data
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public Tag importImage(ImageTag it, byte[] newData) throws IOException {
        return importImage(it, newData, 0);
    }

    /**
     * Imports image.
     * @param it Image tag
     * @param newData New data
     * @param tagType 0 = can change for defineBits, -1 = detect based on data
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public Tag importImage(ImageTag it, byte[] newData, int tagType) throws IOException {
        if (newData.length >= 2 && newData[0] == 'B' && newData[1] == 'M') {
            BufferedImage b = ImageHelper.read(newData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageHelper.write(b, ImageFormat.PNG, baos);
            newData = baos.toByteArray();
        }

        if (tagType == 0) {
            if (it instanceof DefineBitsTag) {
                // DefineBits tag should be imported as DefineBitsJPEG2 tag
                tagType = DefineBitsJPEG2Tag.ID;
            } else {
                tagType = it.getId();
            }
        }
        if (tagType == -1) {
            if (newData.length >= 4
                    && newData[0] == (byte) 0xff
                    && newData[1] == (byte) 0xd8
                    && newData[2] == (byte) 0xff
                    && newData[3] == (byte) 0xe0) {
                tagType = DefineBitsJPEG2Tag.ID;
            } else {
                tagType = DefineBitsLosslessTag.ID;
            }
        }

        if (it.getId() == tagType) {
            it.setImage(newData);
        } else {
            SWF swf = it.getSwf();
            ImageTag imageTag;
            ByteArrayRange range = it.getOriginalRange();
            int characterId = it.getCharacterId();
            switch (tagType) {
                case DefineBitsJPEG2Tag.ID: {
                    imageTag = new DefineBitsJPEG2Tag(swf, range, characterId, newData);
                    break;
                }
                case DefineBitsJPEG3Tag.ID: {
                    imageTag = new DefineBitsJPEG3Tag(swf, range, characterId, newData);
                    break;
                }
                case DefineBitsJPEG4Tag.ID: {
                    imageTag = new DefineBitsJPEG4Tag(swf, range, characterId, newData);
                    break;
                }
                case DefineBitsLosslessTag.ID: {
                    DefineBitsLosslessTag losslessTag = new DefineBitsLosslessTag(swf, range, characterId);
                    losslessTag.setImage(newData);
                    imageTag = losslessTag;
                    break;
                }
                case DefineBitsLossless2Tag.ID: {
                    DefineBitsLossless2Tag lossless2Tag = new DefineBitsLossless2Tag(swf, range, characterId);
                    lossless2Tag.setImage(newData);
                    imageTag = lossless2Tag;
                    break;
                }
                default:
                    throw new Error("Unsupported image type tag.");
            }

            imageTag.setModified(true);
            it.getTimelined().replaceTag(it, imageTag);
            imageTag.setTimelined(it.getTimelined());
            swf.updateCharacters();
            swf.resetTimelines(swf);
            return imageTag;
        }

        return null;
    }

    /**
     * Imports image alpha.
     * @param it Image tag
     * @param newData New data
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public Tag importImageAlpha(ImageTag it, byte[] newData) throws IOException {

        try {
            BufferedImage img = ImageHelper.read(newData);
            int width = img.getWidth();
            int height = img.getHeight();
            byte[] data = new byte[width * height];
            int[] imgData = img.getRGB(0, 0, width, height, null, 0, width);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int alpha = (imgData[y * width + x] >> 24) & 0xff;
                    data[y * width + x] = (byte) alpha;
                }
            }

            newData = data;
        } catch (IOException ex) {
            //ignored
        }

        if (it instanceof DefineBitsJPEG3Tag) {
            ((DefineBitsJPEG3Tag) it).setImageAlpha(newData);
        } else if (it instanceof DefineBitsJPEG4Tag) {
            ((DefineBitsJPEG4Tag) it).setImageAlpha(newData);
        }
        return null;
    }

    /**
     * Converts image.
     * @param it Image tag
     * @param tagType 0 = can change for defineBits, -1 = detect based on data
     * @throws IOException On I/O error
     */
    public void convertImage(ImageTag it, int tagType) throws IOException {
        importImage(it, Helper.readStream(it.getConvertedImageData()), tagType);
    }

    /**
     * Gets image tag type.
     * @param format Format
     * @return Image tag type
     */
    public static int getImageTagType(String format) {
        int res = 0;
        switch (format) {
            case "lossless":
                res = DefineBitsLosslessTag.ID;
                break;
            case "lossless2":
                res = DefineBitsLossless2Tag.ID;
                break;
            case "jpeg2":
                res = DefineBitsJPEG2Tag.ID;
                break;
            case "jpeg3":
                res = DefineBitsJPEG3Tag.ID;
                break;
            case "jpeg4":
                res = DefineBitsJPEG4Tag.ID;
                break;
        }

        return res;
    }

    /**
     * Bulk import images.
     * @param imagesDir Images directory
     * @param swf SWF
     * @param printOut Print out
     * @return Number of imported images
     */
    public int bulkImport(File imagesDir, SWF swf, boolean printOut) {
        int count = 0;
        Map<Integer, CharacterTag> characters = swf.getCharacters(false);
        List<String> extensions = Arrays.asList("png", "jpg", "jpeg", "gif", "bmp");
        List<String> alphaExtensions = Arrays.asList("png");
        File[] allFiles = imagesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String nameLower = name.toLowerCase();
                if (nameLower.endsWith(".alpha.png")) {
                    return false;
                }

                for (String ext : extensions) {
                    if (nameLower.endsWith("." + ext)) {
                        return true;
                    }
                }
                return false;
            }
        });

        File[] alphaFiles = imagesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String nameLower = name.toLowerCase();
                for (String ext : alphaExtensions) {
                    if (nameLower.endsWith(".alpha." + ext)) {
                        return true;
                    }
                }
                return false;
            }
        });
        for (int characterId : characters.keySet()) {
            CharacterTag tag = characters.get(characterId);
            if (tag instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) tag;
                if (!imageTag.importSupported()) {
                    continue;
                }
                List<File> existingFilesForImageTag = new ArrayList<>();
                List<File> existingAlphaFilesForImageTag = new ArrayList<>();
                List<String> classNameExpectedFileNames = new ArrayList<>();
                for (String className : imageTag.getClassNames()) {
                    classNameExpectedFileNames.add(Helper.makeFileName(className));
                }

                for (File f : allFiles) {
                    if (f.getName().startsWith("" + characterId + ".") || f.getName().startsWith("" + characterId + "_")) {
                        existingFilesForImageTag.add(f);
                    } else {
                        String nameNoExt = f.getName();
                        if (nameNoExt.contains(".")) {
                            nameNoExt = nameNoExt.substring(0, nameNoExt.lastIndexOf("."));
                        }
                        if (classNameExpectedFileNames.contains(nameNoExt)) {
                            existingFilesForImageTag.add(f);
                        }
                    }
                }
                for (File f : alphaFiles) {
                    if (f.getName().startsWith("" + characterId + ".") || f.getName().startsWith("" + characterId + "_")) {
                        existingAlphaFilesForImageTag.add(f);
                    } else {
                        String nameNoExt = f.getName().substring(0, f.getName().length() - ".alpha.png".length());
                        if (classNameExpectedFileNames.contains(nameNoExt)) {
                            existingAlphaFilesForImageTag.add(f);
                        }
                    }
                }
                existingFilesForImageTag.sort(new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        String ext1 = o1.getName().substring(o1.getName().lastIndexOf(".") + 1);
                        String ext2 = o2.getName().substring(o2.getName().lastIndexOf(".") + 1);
                        int ret = extensions.indexOf(ext1) - extensions.indexOf(ext2);
                        if (ret == 0) {
                            return o1.getName().compareTo(o2.getName());
                        }
                        return ret;
                    }
                });

                existingAlphaFilesForImageTag.sort(new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        String ext1 = o1.getName().substring(o1.getName().lastIndexOf(".") + 1);
                        String ext2 = o2.getName().substring(o2.getName().lastIndexOf(".") + 1);
                        int ret = alphaExtensions.indexOf(ext1) - alphaExtensions.indexOf(ext2);
                        if (ret == 0) {
                            return o1.getName().compareTo(o2.getName());
                        }
                        return ret;
                    }
                });

                if (!existingFilesForImageTag.isEmpty()) {
                    if (existingFilesForImageTag.size() > 1) {
                        Logger.getLogger(ImageImporter.class.getName()).log(Level.WARNING, "Multiple matching files for image tag {0} exists, {1} selected", new Object[]{characterId, existingFilesForImageTag.get(0).getName()});
                    }

                    File sourceFile = existingFilesForImageTag.get(0);
                    if (printOut) {
                        System.out.println("Importing character " + characterId + " from file " + sourceFile.getName());
                    }

                    try {
                        importImage(imageTag, Helper.readFile(sourceFile.getPath()));
                        count++;
                    } catch (IOException ex) {
                        Logger.getLogger(ImageImporter.class.getName()).log(Level.WARNING, "Cannot import image " + characterId + " from file " + sourceFile.getName(), ex);
                    }
                }
                if (!existingAlphaFilesForImageTag.isEmpty()) {
                    if (existingAlphaFilesForImageTag.size() > 1) {
                        Logger.getLogger(ImageImporter.class.getName()).log(Level.WARNING, "Multiple matching files for image alpha tag {0} exists, {1} selected", new Object[]{characterId, existingAlphaFilesForImageTag.get(0).getName()});
                    }
                    File sourceFile = existingAlphaFilesForImageTag.get(0);
                    if (printOut) {
                        System.out.println("Importing character " + characterId + " alpha from file " + sourceFile.getName());
                    }
                    try {
                        importImageAlpha(imageTag, Helper.readFile(sourceFile.getPath()));
                    } catch (IOException ex) {
                        Logger.getLogger(ImageImporter.class.getName()).log(Level.WARNING, "Cannot import image " + characterId + " alpha from file " + sourceFile.getName(), ex);
                    }
                }

                if (CancellableWorker.isInterrupted()) {
                    break;
                }
            }
        }
        return count;
    }
}
