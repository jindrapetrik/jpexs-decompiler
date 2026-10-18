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
package com.jpexs.decompiler.flash.importers;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.importers.svg.SvgImporter;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import com.jpexs.images.apng.Png;
import com.jpexs.images.apng.chunks.Chunk;
import com.jpexs.images.apng.chunks.Fctl;
import com.jpexs.images.apng.chunks.Fdat;
import com.jpexs.images.apng.chunks.Idat;
import com.jpexs.images.apng.chunks.Iend;
import com.jpexs.images.apng.chunks.Ihdr;
import dev.matrixlab.webp4j.WebPCodec;
import dev.matrixlab.webp4j.model.AnimatedWebPData;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * Sprite importer.
 *
 * @author JPEXS
 */
public class SpriteImporter {

    private void removeCharacters(Set<Integer> usedCharacters, SWF swf) {
        for (int ch : usedCharacters) {
            CharacterTag ct = swf.getCharacter(ch);
            if (ct == null) {
                continue;
            }
            if (!ct.getClassNames().isEmpty()) {
                continue;
            }
            if (ct.getExportName() != null) {
                continue;
            }
            Set<Integer> dependentCharacters = swf.getDependentCharacters(ch);
            if (dependentCharacters.isEmpty()) {
                Set<Integer> needed = new LinkedHashSet<>();
                Set<String> neededClasses = new LinkedHashSet<>();                
                ct.getNeededCharacters(needed, neededClasses, swf);
                //TODO: handle classes?
                List<CharacterIdTag> attachedTags = swf.getCharacterIdTags(ch);
                for (CharacterIdTag cit : attachedTags) {
                    if (cit instanceof Tag) {
                        Tag citt = (Tag) cit;
                        citt.getTimelined().removeTag(citt);
                    }
                }
                ct.getTimelined().removeTag(ct);
                swf.computeDependentCharacters();
                removeCharacters(needed, swf);
            }
        }
    }

    /**
     * Imports sprite from an image. Animated images are resampled to the SWF
     * frame rate; static images and SVGs create a single-frame sprite.
     *
     * @param spriteTag Sprite tag
     * @param is Input stream
     * @return True if import was successful
     * @throws IOException On I/O error
     */
    public boolean importSprite(DefineSpriteTag spriteTag, InputStream is) throws IOException {
        byte[] data = Helper.readStream(is);
        List<BufferedImage> images = new ArrayList<>();
        List<Double> delays = new ArrayList<>();
        int textStart = data.length >= 3 && data[0] == (byte) 0xef
                && data[1] == (byte) 0xbb && data[2] == (byte) 0xbf ? 3 : 0;
        while (textStart < data.length && Character.isWhitespace((char) data[textStart])) {
            textStart++;
        }
        boolean isSvg = textStart < data.length && data[textStart] == '<';
        String svg = isSvg ? new String(data, textStart, data.length - textStart, Utf8Helper.charset) : null;
        if (!isSvg) {
            if (data.length >= 3 && data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
                GifImage gif = GifDecoder.read(new ByteArrayInputStream(data));
                for (int i = 0; i < gif.getFrameCount(); i++) {
                    images.add(gif.getFrame(i));
                    delays.add(gif.getDelay(i) * 10.0);
                }
            } else if (data.length >= 8 && Arrays.equals(Arrays.copyOf(data, 8), Png.SIGNATURE)) {
                readPng(data, images, delays);
            } else if (data.length >= 12 && data[0] == 'R' && data[1] == 'I'
                    && data[2] == 'F' && data[3] == 'F' && data[8] == 'W'
                    && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
                if (!ImageFormat.WEBP.available()) {
                    throw new IOException("WEBP format is not supported on your platform");
                }
                AnimatedWebPData webp = WebPCodec.decodeAnimatedWebP(data);
                int[] frameDelays = webp.getDelays();
                for (int i = 0; i < webp.getFrames().size(); i++) {
                    images.add(webp.getFrames().get(i).getImage());
                    delays.add((double) frameDelays[i]);
                }
            } else {
                try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
                    if (!ImageIO.getImageReaders(input).hasNext()) {
                        throw new IOException("Unsupported image format");
                    }
                }
                images.add(ImageHelper.read(data));
                delays.add(0.0);
            }
            if (images.isEmpty() || images.contains(null)) {
                throw new IOException("Invalid image");
            }
        }
        double duration = 0;
        for (double delay : delays) {
            duration += delay;
        }
        SWF swf = spriteTag.getSwf();
        int frameCount = 1;
        if (images.size() > 1) {
            double count = Math.ceil(duration * swf.frameRate / 1000.0);
            if (!Double.isFinite(count) || count > 65535 || swf.frameRate <= 0) {
                throw new IOException("Animation exceeds the SWF frame limit or has an invalid frame rate");
            }
            frameCount = Math.max(1, (int) count);
        }
        Set<Integer> usedCharacters = new LinkedHashSet<>();
        for (int i = spriteTag.getTags().size() - 1; i >= 0; i--) {
            Tag t = spriteTag.getTags().get(i);
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                int characterId = pt.getCharacterId();
                if (characterId != -1) {
                    usedCharacters.add(characterId);
                }
            }
            spriteTag.removeTag(i);
        }
        spriteTag.getSwf().computeDependentCharacters();

        removeCharacters(usedCharacters, spriteTag.getSwf());

        ShapeImporter shapeImporter = new ShapeImporter();
        double frameEnd = delays.isEmpty() ? 0 : delays.get(0);
        int imageFrame = 0;
        int lastImageFrame = -1;
        for (int swfFrame = 0; swfFrame < frameCount; swfFrame++) {
            double time = swfFrame * 1000.0 / swf.frameRate;
            while (imageFrame + 1 < images.size() && frameEnd <= time) {
                imageFrame++;
                frameEnd += delays.get(imageFrame);
            }
            if (lastImageFrame != imageFrame) {
                ShapeTag shapeTag = isSvg ? new DefineShape4Tag(swf) : new DefineShape2Tag(swf);
                SWF.addTagBefore(shapeTag, spriteTag);
                if (isSvg) {
                    shapeTag = (ShapeTag) new SvgImporter().importSvg(shapeTag, svg, false);
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(images.get(imageFrame), "png", baos);
                    shapeImporter.importImage(shapeTag, baos.toByteArray(), 0, false);
                }

                PlaceObject2Tag placeObject = new PlaceObject2Tag(swf);
                placeObject.placeFlagHasCharacter = true;
                placeObject.characterId = shapeTag.getCharacterId();
                placeObject.depth = 1;
                placeObject.placeFlagHasMatrix = true;
                placeObject.matrix = new MATRIX();

                if (swfFrame > 0) {
                    placeObject.placeFlagMove = true;
                }

                spriteTag.addTag(placeObject);
                placeObject.setTimelined(spriteTag);
            }

            ShowFrameTag showFrame = new ShowFrameTag(swf);
            spriteTag.addTag(showFrame);
            showFrame.setTimelined(spriteTag);
            lastImageFrame = imageFrame;
        }
        spriteTag.frameCount = frameCount;
        spriteTag.hasEndTag = true;
        spriteTag.resetTimeline();
        swf.resetTimeline();

        return true;
    }

    private void readPng(byte[] data, List<BufferedImage> images, List<Double> delays) throws IOException {
        Png png = new Png(new ByteArrayInputStream(data));
        Ihdr header = null;
        List<Chunk> shared = new ArrayList<>();
        List<Chunk> frameData = new ArrayList<>();
        Fctl control = null;
        BufferedImage canvas = null;
        boolean imageDataSeen = false;
        for (Chunk chunk : png.getChunks()) {
            if (chunk instanceof Ihdr) {
                header = (Ihdr) chunk;
                canvas = new BufferedImage((int) header.getWidth(), (int) header.getHeight(), BufferedImage.TYPE_INT_ARGB);
            } else if (chunk instanceof Fctl || chunk instanceof Iend) {
                if (control != null) {
                    // A frame can span multiple IDAT/fdAT chunks. Decode it only
                    // when the next frame control (or the end of the PNG) arrives.
                    Png frame = new Png();
                    frame.addChunk(new Ihdr(control.getWidth(), control.getHeight(), header.getBitDepth(),
                            header.getColorType(), header.getCompressionMethod(), header.getFilterMethod(), header.getInterlaceMethod()));
                    frame.addAllChunks(shared);
                    frame.addAllChunks(frameData);
                    frame.addChunk(new Iend());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    frame.writeTo(baos);
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                    if (image == null) {
                        throw new IOException("Invalid APNG frame");
                    }
                    BufferedImage composed = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = composed.createGraphics();
                    g.drawImage(canvas, 0, 0, null);
                    g.setComposite(control.getBlendOp() == Fctl.BLEND_OP_SOURCE ? AlphaComposite.Src : AlphaComposite.SrcOver);
                    int x = (int) control.getxOffset();
                    int y = (int) control.getyOffset();
                    g.drawImage(image, x, y, null);
                    g.dispose();
                    images.add(composed);
                    delays.add(control.getDelayNum() * 1000.0 / (control.getDelayDen() == 0 ? 100 : control.getDelayDen()));
                    if (control.getDisposeOp() != Fctl.DISPOSE_OP_PREVIOUS) {
                        canvas = new BufferedImage(composed.getWidth(), composed.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        g = canvas.createGraphics();
                        g.drawImage(composed, 0, 0, null);
                        if (control.getDisposeOp() == Fctl.DISPOSE_OP_BACKGROUND) {
                            g.setComposite(AlphaComposite.Clear);
                            g.fillRect(x, y, image.getWidth(), image.getHeight());
                        }
                        g.dispose();
                    }
                }
                control = chunk instanceof Fctl ? (Fctl) chunk : null;
                frameData.clear();
            } else if (chunk instanceof Idat || chunk instanceof Fdat) {
                imageDataSeen = true;
                if (control != null) {
                    frameData.add(chunk instanceof Fdat ? new Idat(((Fdat) chunk).getFrameData()) : chunk);
                }
            } else if (!imageDataSeen && !"acTL".equals(chunk.getType())) {
                shared.add(chunk);
            }
        }
        if (images.isEmpty()) {
            images.add(ImageHelper.read(data));
            delays.add(0.0);
        }
    }

    /**
     * Bulk import sprites from directory.
     * @param spritesDir Directory with sprites
     * @param swf SWF
     * @param printOut Print out
     * @return Number of imported sprites
     */
    public int bulkImport(File spritesDir, SWF swf, boolean printOut) {
        Map<Integer, CharacterTag> characters = swf.getCharacters(false);
        int spriteCount = 0;
        List<String> extensions = Arrays.asList("gif", "png", "apng", "webp", "jpg", "jpeg", "bmp", "svg");
        File[] allFiles = spritesDir.listFiles(new FilenameFilter() {
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
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag spriteTag = (DefineSpriteTag) tag;
                List<File> existingFilesForSpriteTag = new ArrayList<>();

                List<String> classNameExpectedFileNames = new ArrayList<>();
                for (String className : spriteTag.getClassNames()) {
                    classNameExpectedFileNames.add(Helper.makeFileName(className));
                }

                for (File f : allFiles) {
                    if (f.getName().startsWith("" + characterId + ".") || f.getName().startsWith("" + characterId + "_")) {
                        existingFilesForSpriteTag.add(f);
                    } else {
                        String nameNoExt = f.getName();
                        if (nameNoExt.contains(".")) {
                            nameNoExt = nameNoExt.substring(0, nameNoExt.lastIndexOf("."));
                        }
                        if (classNameExpectedFileNames.contains(nameNoExt)) {
                            existingFilesForSpriteTag.add(f);
                        }
                    }
                }
                existingFilesForSpriteTag.sort(new Comparator<File>() {
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

                if (existingFilesForSpriteTag.isEmpty()) {
                    continue;
                }

                if (existingFilesForSpriteTag.size() > 1) {
                    Logger.getLogger(SpriteImporter.class.getName()).log(Level.WARNING, "Multiple matching files for sprite tag {0} exists, {1} selected", new Object[]{characterId, existingFilesForSpriteTag.get(0).getName()});
                }
                File sourceFile = existingFilesForSpriteTag.get(0);

                if (printOut) {
                    System.out.println("Importing character " + characterId + " from file " + sourceFile.getName());
                }

                try (FileInputStream fis = new FileInputStream(sourceFile.getAbsolutePath())) {
                    importSprite(spriteTag, fis);
                    spriteCount++;
                } catch (IOException ex) {
                    Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Cannot import sprite " + characterId + " from file " + sourceFile.getName(), ex);
                }
                if (CancellableWorker.isInterrupted()) {
                    break;
                }
            }
        }
        return spriteCount;
    }
}
