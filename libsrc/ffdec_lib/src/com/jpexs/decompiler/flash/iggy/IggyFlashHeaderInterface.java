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

import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import java.io.IOException;

/**
 * Flash header interface.
 *
 * @author JPEXS
 */
public interface IggyFlashHeaderInterface extends StructureInterface {

    public long getXMin();

    public long getYMin();

    public long getXMax();

    public long getYMax();

    public float getFrameRate();

    public long getBaseAddress();

    public long getDictionarySize();

    public long getFrameCount();

    public long getBackgroundColor();

    public long getSourceNameAddress();

    public long getTimelineAddress();

    public long getFrameLabelsAddress();

    public long getFrameLabelCount();

    public long getScenesAddress();

    public long getSceneCount();

    public void setMovieProperties(long xMin, long yMin, long xMax, long yMax,
            float fps, long frames, long color) throws IOException;

    default int getHeaderSize() {
        return 184;
    }

    default long getFlashFilenameAddress() {
        return 0;
    }

    default long getImportsAddress() {
        return 0;
    }

    default long getImportCount() {
        return 0;
    }

    default long getDictionaryFixupsAddress() {
        return 0;
    }

    default long getAbcAddress() {
        return 0;
    }

    default int getAbcPointerOffset() {
        return 0x98;
    }

    default long getSymbolClassesAddress() {
        return 0;
    }

    default long getSymbolClassCount() {
        return 0;
    }

    default boolean hasAS3() {
        return false;
    }
}
