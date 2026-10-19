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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 * 32 legacy root consumed by IggyCreateFromMemoryCustom (0x900). Only the
 * first 64 bytes are known. Later data stays in the original graph; it must not
 * be interpreted as a shortened version of the x64 root.
 */
public class IggyFlashHeader32Legacy implements IggyFlashHeaderInterface {

    public static final int STRUCT_SIZE = 64;
    private long dictionary;
    private long dictionarySize;
    private long timeline;
    private long otherTimeline;
    private long xmin;
    private long ymin;
    private long xmax;
    private long ymax;
    private long frameCount;
    private long timelineParameter;
    private long color;
    private long labels;
    private long labelCount;
    private long sceneCount;
    private long scenes;
    private long source;

    public IggyFlashHeader32Legacy(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        dictionary = IggyLayout.readPointer(s, false);
        dictionarySize = s.readUI32();
        timeline = IggyLayout.readPointer(s, false);
        otherTimeline = IggyLayout.readPointer(s, false);
        xmin = s.readSI32();
        ymin = s.readSI32();
        xmax = s.readSI32();
        ymax = s.readSI32();
        frameCount = s.readUI32();
        timelineParameter = s.readUI32();
        color = s.readUI32();
        labels = IggyLayout.readPointer(s, false);
        labelCount = s.readUI32();
        sceneCount = s.readUI32();
        scenes = IggyLayout.readPointer(s, false);
        source = IggyLayout.readPointer(s, false);
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        IggyLayout.writePointer(s, false, dictionary);
        s.writeUI32(dictionarySize);
        IggyLayout.writePointer(s, false, timeline);
        IggyLayout.writePointer(s, false, otherTimeline);
        s.writeSI32(xmin);
        s.writeSI32(ymin);
        s.writeSI32(xmax);
        s.writeSI32(ymax);
        s.writeUI32(frameCount);
        s.writeUI32(timelineParameter);
        s.writeUI32(color);
        IggyLayout.writePointer(s, false, labels);
        s.writeUI32(labelCount);
        s.writeUI32(sceneCount);
        IggyLayout.writePointer(s, false, scenes);
        IggyLayout.writePointer(s, false, source);
        if (s.getIndexing() != null) {
            s.getIndexing().writeLengthCustom(64, new int[]{0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 44, 48, 52, 56, 60}, new int[]{2, 5, 2, 2, 5, 5, 5, 5, 5, 5, 2, 5, 5, 2, 2});
        }
    }

    @Override
    public int getAbcPointerOffset() {
        return -1;
    }

    @Override
    public int getHeaderSize() {
        return STRUCT_SIZE;
    }

    @Override
    public long getBaseAddress() {
        return dictionary;
    }

    @Override
    public long getDictionarySize() {
        return dictionarySize;
    }

    @Override
    public long getTimelineAddress() {
        return timeline;
    }

    @Override
    public long getSourceNameAddress() {
        return source;
    }

    @Override
    public long getFrameLabelsAddress() {
        return labels;
    }

    @Override
    public long getFrameLabelCount() {
        return labelCount;
    }

    @Override
    public long getScenesAddress() {
        return scenes;
    }

    @Override
    public long getSceneCount() {
        return sceneCount;
    }

    @Override
    public long getFrameCount() {
        return frameCount;
    }

    @Override
    public long getBackgroundColor() {
        return color;
    }

    @Override
    public long getXMin() {
        return xmin;
    }

    @Override
    public long getYMin() {
        return ymin;
    }

    @Override
    public long getXMax() {
        return xmax;
    }

    @Override
    public long getYMax() {
        return ymax;
    }

    // This loader sets 24 in the Player, without reading FPS from the root.
    @Override
    public float getFrameRate() {
        return 24;
    }

    @Override
    public void setMovieProperties(long xMin, long yMin, long xMax, long yMax, float fps, long frames, long background) throws IOException {
        if (fps != 24) {
            throw new IOException("The Iggy 32 legacy layout has a fixed frame rate of 24 FPS");
        }
        xmin = xMin;
        ymin = yMin;
        xmax = xMax;
        ymax = yMax;
        frameCount = frames;
        color = background;
    }
}
