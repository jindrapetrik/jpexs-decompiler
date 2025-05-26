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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.settings.MovieExportSettings;
import com.jpexs.decompiler.flash.flv.FLVOutputStream;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.flv.SCRIPTDATA;
import com.jpexs.decompiler.flash.flv.VIDEODATA;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Movie exporter.
 *
 * @author JPEXS
 */
public class MovieExporter {

    public List<File> exportMovies(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, final MovieExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (CancellableWorker.isInterrupted()) {
            return ret;
        }
        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof DefineVideoStreamTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (Tag t : tags) {
            if (t instanceof DefineVideoStreamTag) {
                if (evl != null) {
                    evl.handleExportingEvent("movie", currentIndex, count, t.getName());
                }

                final DefineVideoStreamTag videoStream = (DefineVideoStreamTag) t;
                final File file = new File(outdir + File.separator + Helper.makeFileName(videoStream.getCharacterExportFileName() + ".flv"));
                new RetryTask(() -> {
                    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                        fos.write(exportMovie(videoStream, settings.mode));
                    }
                }, handler).run();

                Set<String> classNames = videoStream.getClassNames();
                if (Configuration.as3ExportNamesUseClassNamesOnly.get() && !classNames.isEmpty()) {
                    for (String className : classNames) {
                        File classFile = new File(outdir + File.separator + Helper.makeFileName(className + ".flv"));
                        new RetryTask(() -> {
                            Files.copy(file.toPath(), classFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }, handler).run();
                        ret.add(classFile);
                    }
                    file.delete();
                } else {
                    ret.add(file);
                }

                if (CancellableWorker.isInterrupted()) {
                    break;
                }
                if (evl != null) {
                    evl.handleExportedEvent("movie", currentIndex, count, t.getName());
                }

                currentIndex++;
            }
        }
        return ret;
    }

    public byte[] exportMovie(DefineVideoStreamTag videoStream, MovieExportMode mode) throws IOException {
        return exportMovie(videoStream, mode, false);
    }

    public byte[] exportMovie(DefineVideoStreamTag videoStream, MovieExportMode mode, boolean ffdecInternal) throws IOException {
        SWF swf = videoStream.getSwf();
        Map<Integer, VideoFrameTag> frames = new HashMap<>();
        SWF.populateVideoFrames(videoStream.characterID, swf.getTags(), frames);
        if (frames.isEmpty()) {
            return SWFInputStream.BYTE_ARRAY_EMPTY;
        }

        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        OutputStream tos = fos;
        FLVOutputStream flv = new FLVOutputStream(tos);
        flv.writeHeader(false, true);
        int numFrames = videoStream.numFrames;
        if (ffdecInternal) {
            numFrames += 2;
        }
        int internalFrameDelaySec = 5;
        flv.writeTag(new FLVTAG(0, SCRIPTDATA.simpleVideOnMetadata(ffdecInternal ? numFrames * internalFrameDelaySec : numFrames / swf.frameRate, videoStream.width, videoStream.height, ffdecInternal ? internalFrameDelaySec : swf.frameRate, videoStream.codecID)));
        int horizontalAdjustment = 0;
        int verticalAdjustment = 0;
        int[] frameNumArray = Helper.toIntArray(frames.keySet());
        Arrays.sort(frameNumArray);
        FLVTAG lastTag = null;
        int frameNum = 0;
        int internalFrameDelay = internalFrameDelaySec * 1000;

        for (int i = 0; i < frameNumArray.length; i++) {
            VideoFrameTag tag = frames.get(frameNumArray[i]);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            frameNum = frameNumArray[i];

            int frameType = 1;

            if ((videoStream.codecID == DefineVideoStreamTag.CODEC_VP6)
                    || (videoStream.codecID == DefineVideoStreamTag.CODEC_VP6_ALPHA)) {
                SWFInputStream sis = new SWFInputStream(swf, tag.videoData.getRangeData());
                if (videoStream.codecID == DefineVideoStreamTag.CODEC_VP6_ALPHA) {
                    sis.readUI24("offsetToAlpha"); //offsetToAlpha
                }
                int frameMode = (int) sis.readUB(1, "frameMode");

                if (frameMode == 0) {
                    frameType = 1; //intra
                } else {
                    frameType = 2; //inter
                }
                sis.readUB(6, "qp"); //qp
                int marker = (int) sis.readUB(1, "marker");
                if (frameMode == 0) {
                    int version = (int) sis.readUB(5, "version");
                    int version2 = (int) sis.readUB(2, "version2");
                    sis.readUB(1, "interlace"); //interlace
                    if (marker == 1 || version2 == 0) {
                        sis.readUI16("offset"); //offset
                    }
                    int dim_y = sis.readUI8("dim_y");
                    int dim_x = sis.readUI8("dim_x");
                    sis.readUI8("render_y"); //render_y
                    sis.readUI8("render_x"); //render_x
                    horizontalAdjustment = (int) (dim_x * Math.ceil(((double) videoStream.width) / (double) dim_x)) - videoStream.width;
                    verticalAdjustment = (int) (dim_y * Math.ceil(((double) videoStream.height) / (double) dim_y)) - videoStream.height;

                }

                SWFOutputStream sos = new SWFOutputStream(baos, swf.version, swf.getCharset());
                sos.writeUB(4, horizontalAdjustment);
                sos.writeUB(4, verticalAdjustment);
            }
            if (videoStream.codecID == DefineVideoStreamTag.CODEC_SORENSON_H263) {
                SWFInputStream sis = new SWFInputStream(swf, tag.videoData.getRangeData());
                sis.readUB(17, "pictureStartCode"); //pictureStartCode
                sis.readUB(5, "version"); //version
                sis.readUB(8, "temporalReference"); //temporalReference
                int pictureSize = (int) sis.readUB(3, "pictureSize"); //pictureSize
                if (pictureSize == 0) {
                    sis.readUB(8, "customWidth"); //customWidth
                    sis.readUB(8, "customHeight"); //customHeight
                }
                if (pictureSize == 1) {
                    sis.readUB(16, "customWidth"); //customWidth
                    sis.readUB(16, "customHeight"); //customHeight
                }
                int pictureType = (int) sis.readUB(2, "pictureType");
                switch (pictureType) {
                    case 0: //intra
                        frameType = 1; //keyframe
                        break;
                    case 1://inter
                        frameType = 2;
                        break;
                    case 2: //disposable
                        frameType = 3;
                        break;
                }
            }

            baos.write(tag.videoData.getRangeData());
            flv.writeTag(lastTag = new FLVTAG((long) Math.floor(ffdecInternal ? frameNum * internalFrameDelay : (frameNum * 1000.0 / swf.frameRate)), new VIDEODATA(frameType, videoStream.codecID, baos.toByteArray())));
        }
        if (ffdecInternal && lastTag != null) {
            lastTag.timeStamp = frameNum * internalFrameDelay + 2 * internalFrameDelay;
            flv.writeTag(lastTag);
        }
        return fos.toByteArray();
    }
}
