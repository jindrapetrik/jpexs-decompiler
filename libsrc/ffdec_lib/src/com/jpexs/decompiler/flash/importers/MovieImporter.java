/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.flv.FLVInputStream;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.flv.VIDEODATA;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Reference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class MovieImporter {
    public void importMovie(DefineVideoStreamTag movie, InputStream fis) throws IOException {
        List<FLVTAG> videoTags = new ArrayList<>();

        FLVInputStream flvIs = new FLVInputStream(fis);
        Reference<Boolean> audioPresent = new Reference<>(false);
        Reference<Boolean> videoPresent = new Reference<>(false);
        flvIs.readHeader(audioPresent, videoPresent);
        List<FLVTAG> flvTags = flvIs.readTags();

        for (FLVTAG tag : flvTags) {
            if (tag.tagType == FLVTAG.DATATYPE_VIDEO) {
                videoTags.add(tag);
            }
        }
        if (!videoTags.isEmpty()) {
            FLVTAG firstVideoTag = videoTags.get(0);
            VIDEODATA videoData = ((VIDEODATA) firstVideoTag.data);
            FLVInputStream dis = new FLVInputStream(new ByteArrayInputStream(videoData.videoData));
            int newWidth = 0;
            int newHeight = 0;
            switch (videoData.codecId) {
                case VIDEODATA.CODEC_SORENSON_H263:
                    dis.readUB(17); //pictureStartCode
                    dis.readUB(5); //version
                    dis.readUB(8); //temporalReference
                    int pictureSize = (int) dis.readUB(3);
                    switch (pictureSize) {
                        case 0:
                            newWidth = (int) dis.readUB(8);
                            newHeight = (int) dis.readUB(8);
                            break;
                        case 1:
                            newWidth = (int) dis.readUB(16);
                            newHeight = (int) dis.readUB(16);
                            break;
                        case 2:
                            newWidth = 352;
                            newHeight = 288;
                            break;
                        case 3:
                            newWidth = 176;
                            newHeight = 144;
                            break;
                        case 4:
                            newWidth = 128;
                            newHeight = 96;
                            break;
                        case 5:
                            newWidth = 320;
                            newHeight = 240;
                            break;
                        case 6:
                            newWidth = 160;
                            newHeight = 120;
                            break;
                    }

                    break;
                case VIDEODATA.CODEC_SCREEN_VIDEO:
                case VIDEODATA.CODEC_SCREEN_VIDEO_V2:
                    dis.readUB(4); //blockWidth
                    newWidth = (int) dis.readUB(12);
                    dis.readUB(4); //blockHeight
                    newHeight = (int) dis.readUB(12);
                    break;
                case VIDEODATA.CODEC_VP6:
                case VIDEODATA.CODEC_VP6_ALPHA:
                    int horizontalAdjustment = (int) dis.readUB(4);
                    int verticalAdjustment = (int) dis.readUB(4);
                    if (videoData.codecId == VIDEODATA.CODEC_VP6_ALPHA) {
                        dis.readUI24(); //offsetToAlpha
                    }

                    int frameMode = (int) dis.readUB(1); //frameMode

                    dis.readUB(6); //qp
                    int multiStream = (int) dis.readUB(1); //marker
                    if (frameMode == 0) {
                        int vp3VersionNo = (int) dis.readUB(5);
                        int vpProfile = (int) dis.readUB(2);
                        dis.readUB(1); //interlace
                        if (multiStream == 1 || vpProfile == 0) {
                            dis.readUI16(); //offset
                        }
                        int dim_y = dis.readUI8();
                        int dim_x = dis.readUI8();
                        //dis.readUI8(); //render_y
                        //dis.readUI8(); //render_x
                        newWidth = 16 * dim_x - horizontalAdjustment;
                        newHeight = 16 * dim_y - verticalAdjustment;
                    }
                    break;
                case VIDEODATA.CODEC_AVC:
                    throw new IOException("AVC codec is not supported for import");
                case VIDEODATA.CODEC_JPEG:
                    throw new IOException("JPEG codec is not supported for import");
            }
            if (newWidth <= 0 || newHeight <= 0) {
                throw new IOException("Invalid dimension");
            }
            movie.codecID = videoData.codecId;
            movie.width = newWidth;
            movie.height = newHeight;
            movie.videoFlagsDeblocking = DefineVideoStreamTag.DEBLOCKING_OFF;
            movie.videoFlagsSmoothing = true;
            movie.numFrames = videoTags.size();
            movie.setPauseRendering(true);

            videoTags.sort(new Comparator<FLVTAG>() {
                @Override
                public int compare(FLVTAG o1, FLVTAG o2) {
                    return Long.compare(o1.timeStamp, o2.tagType);
                }
            });
            
            SWF swf = movie.getSwf();

            //remove old VideoFrame tags
            Map<Integer, VideoFrameTag> frames = new HashMap<>();
            SWF.populateVideoFrames(movie.characterID, swf.getTags(), frames);
            Timelined timelined = null;
            for (VideoFrameTag t : frames.values()) {
                t.getTimelined().removeTag(t);
                timelined = t.getTimelined();
            }
            timelined.resetTimeline();

            int startFrame = 0;
            int placeDepth = -1;
            for (Tag t : timelined.getTags()) {
                if (t instanceof ShowFrameTag) {
                    startFrame++;
                }
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                    if (pt.getCharacterId() == movie.characterID) {
                        placeDepth = pt.getDepth();
                        break;
                    }
                }
            }

            int importLastFrame = -1;
            if (timelined != null) {
                VideoFrameTag lastVideoFrame = null;
                for (FLVTAG ftag : videoTags) {
                    videoData = ((VIDEODATA) ftag.data);
                    if (videoData.codecId == VIDEODATA.CODEC_VP6 || videoData.codecId == VIDEODATA.CODEC_VP6_ALPHA) {
                        int horizontalAdjustment = (int) dis.readUB(4);
                        int verticalAdjustment = (int) dis.readUB(4);
                        if (videoData.codecId == VIDEODATA.CODEC_VP6_ALPHA) {
                            dis.readUI24(); //offsetToAlpha
                        }

                        int frameMode = (int) dis.readUB(1); //frameMode

                        dis.readUB(6); //qp
                        int multiStream = (int) dis.readUB(1); //marker
                        if (frameMode == 1) {
                            if (multiStream == 1) { // || version2 == 0) { ???
                                dis.readUI16(); //buff2Offset
                            }
                            dis.readUB(1); //refreshGoldenFrame
                            int useLoopFilter = (int) dis.readUB(1); //useLoopFilter
                            if (useLoopFilter == 1) {
                                int loopFilterSelector = (int) dis.readUB(1);
                                if (loopFilterSelector == 0) {
                                    movie.videoFlagsDeblocking = DefineVideoStreamTag.DEBLOCKING_LEVEL1;
                                } else if (loopFilterSelector == 1) {
                                    movie.videoFlagsDeblocking = DefineVideoStreamTag.DEBLOCKING_LEVEL4;
                                }
                            }
                        }
                    }

                    int idealFrame = startFrame + (int) Math.floor(swf.frameRate * ftag.timeStamp / 1000.0);
                    if (idealFrame == importLastFrame) {
                        idealFrame++;
                    }
                    int swfFrameNum = -1;
                    ReadOnlyTagList tagList = timelined.getTags();
                    int p = 0;
                    boolean found = false;
                    for (; p < tagList.size(); p++) {
                        Tag t = tagList.get(p);
                        if (t instanceof ShowFrameTag) {
                            swfFrameNum++;
                            if (swfFrameNum == idealFrame) {
                                found = true;
                                break;
                            }
                        }
                    }

                    //add frames when necessary
                    if (!found) {
                        p--;
                        swfFrameNum++;
                        for (; swfFrameNum <= idealFrame; swfFrameNum++) {
                            PlaceObject2Tag placeObject = new PlaceObject2Tag(swf);
                            placeObject.setTimelined(timelined);
                            placeObject.depth = placeDepth;
                            placeObject.placeFlagMove = true;
                            placeObject.placeFlagHasRatio = true;
                            placeObject.ratio = swfFrameNum - startFrame;
                            timelined.addTag(placeObject);
                            p++;
                            ShowFrameTag sft = new ShowFrameTag(swf);
                            sft.setTimelined(timelined);
                            timelined.addTag(sft);
                            p++;
                        }
                        swfFrameNum--;
                    }
                    VideoFrameTag videoFrameTag = new VideoFrameTag(swf);
                    videoFrameTag.streamID = movie.characterID;
                    videoFrameTag.frameNum = swfFrameNum - startFrame;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    VIDEODATA vdata = (VIDEODATA) ftag.data;
                    switch (movie.codecID) {
                        case DefineVideoStreamTag.CODEC_VP6:
                        case DefineVideoStreamTag.CODEC_VP6_ALPHA:
                            baos.write(Arrays.copyOfRange(vdata.videoData, 1 /*strip adjustment*/, vdata.videoData.length));
                            break;
                        default:
                            baos.write(vdata.videoData);
                            break;
                    }
                    videoFrameTag.videoData = new ByteArrayRange(baos.toByteArray());
                    timelined.addTag(p, videoFrameTag);
                    videoFrameTag.setTimelined(timelined);
                    importLastFrame = idealFrame;
                }
            }

            List<Tag> tagsToRemove = new ArrayList<>();
            for (Tag t : timelined.getTags()) {
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                    if (pt.getDepth() == placeDepth) {
                        int ratio = pt.getRatio();
                        if (ratio > importLastFrame - startFrame) {
                            tagsToRemove.add(t);
                        }
                    }
                }
            }
            for (Tag t : tagsToRemove) {
                timelined.removeTag(t);
            }

            movie.setModified(true);
            timelined.resetTimeline();
            movie.resetTimeline();
            movie.resetPlayer();
            movie.setPauseRendering(false);
        }
    }
}
