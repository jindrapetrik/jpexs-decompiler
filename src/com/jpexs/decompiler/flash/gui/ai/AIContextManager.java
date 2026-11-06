/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.types.RECT;

public class AIContextManager {

    public String describe(SWF swf) {
        if (swf == null) {
            return "";
        }
        StringBuilder context = new StringBuilder();
        context.append("File: ").append(swf.getTitleOrShortFileName()).append('\n');
        context.append("SWF version: ").append(swf.version).append('\n');
        context.append("Frame rate: ").append(swf.frameRate).append(" fps\n");
        context.append("Frame count: ").append(swf.frameCount).append('\n');
        RECT rect = swf.displayRect;
        if (rect != null) {
            context.append("Stage size: ")
                    .append(Math.max(0, rect.Xmax - rect.Xmin) / 20)
                    .append(" x ")
                    .append(Math.max(0, rect.Ymax - rect.Ymin) / 20)
                    .append(" pixels\n");
        }
        context.append("Root-level tag count: ").append(swf.getTags().size());
        return context.toString();
    }
}
