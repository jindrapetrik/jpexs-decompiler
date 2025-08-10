/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.importers.svg.SvgImporter;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import dev.matrixlab.webp4j.WebPCodec;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
 * Shape importer.
 *
 * @author JPEXS
 */
public class ShapeImporter {

    /**
     * Imports an image to a shape tag.
     *
     * @param st Shape tag
     * @param newData New image data
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public Tag importImage(ShapeTag st, byte[] newData) throws IOException {
        return importImage((Tag) st, newData, 0, true);
    }

    /**
     * Imports an image to a morph shape tag.
     *
     * @param mst Morph shape tag
     * @param newData New image data
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public Tag importImage(MorphShapeTag mst, byte[] newData) throws IOException {
        return importImage((Tag) mst, newData, 0, true);
    }

    /**
     * Imports an image to morph shape tag.
     *
     * @param mst Morph shape tag
     * @param newData New image data
     * @param tagType Tag type
     * @param fill Fill flag
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public Tag importImage(MorphShapeTag mst, byte[] newData, int tagType, boolean fill) throws IOException {
        return importImage((Tag) mst, newData, tagType, fill);
    }

    /**
     * Imports an image to shape tag.
     *
     * @param st Shape tag
     * @param newData New image data
     * @param tagType Tag type
     * @param fill Fill flag
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public Tag importImage(ShapeTag st, byte[] newData, int tagType, boolean fill) throws IOException {
        return importImage((Tag) st, newData, tagType, fill);
    }

    private Tag importImage(Tag st, byte[] newData, int tagType, boolean fill) throws IOException {
        ImageTag imageTag = addImage(st, newData, tagType);
        st.setModified(true);

        RECT rect = null;
        int shapeNum = 0;
        if (st instanceof ShapeTag) {
            rect = ((ShapeTag) st).getRect();
            shapeNum = ((ShapeTag) st).getShapeNum();
        }
        if (st instanceof MorphShapeTag) {
            rect = ((MorphShapeTag) st).getRect();
            int morphShapeNum = ((MorphShapeTag) st).getShapeNum();
            if (morphShapeNum == 2) {
                shapeNum = 4;
            } else {
                shapeNum = 3;
            }
        }
        if (!fill) {
            Dimension dimension = imageTag.getImageDimension();
            rect.Xmax = rect.Xmin + (int) (SWF.unitDivisor * dimension.getWidth());
            rect.Ymax = rect.Ymin + (int) (SWF.unitDivisor * dimension.getHeight());
        }

        SHAPEWITHSTYLE shapes = imageTag.getShape(rect, fill, shapeNum);
        if (st instanceof ShapeTag) {
            ShapeTag shapeTag = (ShapeTag) st;
            shapeTag.shapes = shapes;
        }
        if (st instanceof MorphShapeTag) {
            MorphShapeTag morphShapeTag = (MorphShapeTag) st;
            shapes.updateMorphShapeTag(morphShapeTag, fill);
        }
        return (Tag) st;
    }

    /**
     * Adds an image tag before the specified tag.
     *
     * @param st Tag
     * @param newData New image data
     * @param tagType Tag type
     * @return Imported tag
     * @throws IOException On I/O error
     */
    public ImageTag addImage(Tag st, byte[] newData, int tagType) throws IOException {
        SWF swf = st.getSwf();

        if (newData[0] == 'B' && newData[1] == 'M') {
            BufferedImage b = ImageHelper.read(newData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageHelper.write(b, ImageFormat.PNG, baos);
            newData = baos.toByteArray();
        }

        if (newData.length >= 4
                && newData[0] == 'R'
                && newData[1] == 'I'
                && newData[2] == 'F'
                && newData[3] == 'F'
            ) {
            if (!ImageFormat.WEBP.available()) {
                throw new RuntimeException("WEBP format is not supported on your platform");
            }
            BufferedImage b = WebPCodec.decodeImage(newData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageHelper.write(b, ImageFormat.PNG, baos);
            newData = baos.toByteArray();
        }

        if (tagType == 0) {
            if (ImageTag.getImageFormat(newData) == ImageFormat.JPEG) {
                tagType = DefineBitsJPEG2Tag.ID;
            } else {
                tagType = DefineBitsLossless2Tag.ID;
            }
        }

        ImageTag imageTag;
        switch (tagType) {
            case DefineBitsJPEG2Tag.ID: {
                imageTag = new DefineBitsJPEG2Tag(swf, null, swf.getNextCharacterId(), newData);
                break;
            }
            case DefineBitsJPEG3Tag.ID: {
                imageTag = new DefineBitsJPEG3Tag(swf, null, swf.getNextCharacterId(), newData);
                break;
            }
            case DefineBitsJPEG4Tag.ID: {
                imageTag = new DefineBitsJPEG4Tag(swf, null, swf.getNextCharacterId(), newData);
                break;
            }
            case DefineBitsLosslessTag.ID: {
                DefineBitsLosslessTag losslessTag = new DefineBitsLosslessTag(swf);
                losslessTag.setImage(newData);
                imageTag = losslessTag;
                break;
            }
            case DefineBitsLossless2Tag.ID: {
                DefineBitsLossless2Tag lossless2Tag = new DefineBitsLossless2Tag(swf);
                lossless2Tag.setImage(newData);
                imageTag = lossless2Tag;
                break;
            }
            default:
                throw new Error("Unsupported image type tag.");
        }

        swf.addTagBefore(imageTag, st);
        swf.updateCharacters();
        return imageTag;
    }

    /**
     * Gets the shape tag type.
     *
     * @param format Format
     * @return Shape tag type
     */
    public static int getShapeTagType(String format) {
        int res = 0;
        switch (format) {
            case "shape":
                res = DefineShapeTag.ID;
                break;
            case "shape2":
                res = DefineShape2Tag.ID;
                break;
            case "shape3":
                res = DefineShape3Tag.ID;
                break;
            case "shape4":
                res = DefineShape4Tag.ID;
                break;
        }

        return res;
    }

    /**
     * Bulk import shapes.
     *
     * @param shapesDir Shapes directory
     * @param swf SWF
     * @param noFill No fill flag
     * @param printOut Print out flag
     * @return Number of imported shapes
     */
    public int bulkImport(File shapesDir, SWF swf, boolean noFill, boolean printOut) {
        SvgImporter svgImporter = new SvgImporter();

        Map<Integer, CharacterTag> characters = swf.getCharacters(false);
        int shapeCount = 0;
        List<String> extensions = Arrays.asList("svg", "png", "jpg", "jpeg", "gif", "bmp");
        File[] allFiles = shapesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String nameLower = name.toLowerCase();
                for (String ext : extensions) {
                    if (nameLower.endsWith("." + ext)) {
                        return true;
                    }
                }
                return false;
            }
        });
        for (int characterId : characters.keySet()) {
            CharacterTag tag = characters.get(characterId);
            if (tag instanceof ShapeTag) {
                ShapeTag shapeTag = (ShapeTag) tag;
                List<File> existingFilesForShapeTag = new ArrayList<>();

                List<String> classNameExpectedFileNames = new ArrayList<>();
                for (String className : shapeTag.getClassNames()) {
                    classNameExpectedFileNames.add(Helper.makeFileName(className));
                }

                for (File f : allFiles) {
                    if (f.getName().startsWith("" + characterId + ".") || f.getName().startsWith("" + characterId + "_")) {
                        existingFilesForShapeTag.add(f);
                    } else {
                        String nameNoExt = f.getName();
                        if (nameNoExt.contains(".")) {
                            nameNoExt = nameNoExt.substring(0, nameNoExt.lastIndexOf("."));
                        }
                        if (classNameExpectedFileNames.contains(nameNoExt)) {
                            existingFilesForShapeTag.add(f);
                        }
                    }
                }
                existingFilesForShapeTag.sort(new Comparator<File>() {
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

                if (existingFilesForShapeTag.isEmpty()) {
                    continue;
                }

                if (existingFilesForShapeTag.size() > 1) {
                    Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Multiple matching files for shape tag {0} exists, {1} selected", new Object[]{characterId, existingFilesForShapeTag.get(0).getName()});
                }
                File sourceFile = existingFilesForShapeTag.get(0);

                try {
                    if (printOut) {
                        System.out.println("Importing character " + characterId + " from file " + sourceFile.getName());
                    }
                    if (sourceFile.getAbsolutePath().toLowerCase().endsWith(".svg")) {
                        svgImporter.importSvg(shapeTag, Helper.readTextFile(sourceFile.getAbsolutePath()), !noFill);
                    } else {
                        importImage(shapeTag, Helper.readFile(sourceFile.getAbsolutePath()), 0, !noFill);
                    }
                    shapeCount++;
                } catch (IOException ex) {
                    Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Cannot import shape " + characterId + " from file " + sourceFile.getName(), ex);
                }
                if (CancellableWorker.isInterrupted()) {
                    break;
                }
            }
        }
        return shapeCount;
    }
}
