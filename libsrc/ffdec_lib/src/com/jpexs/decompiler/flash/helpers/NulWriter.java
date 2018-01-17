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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import java.util.Stack;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class NulWriter extends GraphTextWriter {

    private final Stack<LoopWithType> loopStack = new Stack<>();

    private final Stack<Boolean> stringAddedStack = new Stack<>();

    private boolean stringAdded = false;

    public NulWriter() {
        super(new CodeFormatting());
    }

    public void startLoop(long loopId, int loopType) {
        LoopWithType loop = new LoopWithType();
        loop.loopId = loopId;
        loop.type = loopType;
        loopStack.add(loop);
    }

    public LoopWithType endLoop(long loopId) {
        LoopWithType loopIdInStack = loopStack.pop();
        if (loopId != loopIdInStack.loopId) {
            throw new Error("LoopId mismatch");
        }
        return loopIdInStack;
    }

    public long getLoop() {
        if (loopStack.isEmpty()) {
            return -1;
        }
        return loopStack.peek().loopId;
    }

    public long getNonSwitchLoop() {
        if (loopStack.isEmpty()) {
            return -1;
        }

        int pos = loopStack.size() - 1;
        LoopWithType loop;
        do {
            loop = loopStack.get(pos);
            pos--;
        } while ((pos >= 0) && (loop.type == LoopWithType.LOOP_TYPE_SWITCH));

        if (loop.type == LoopWithType.LOOP_TYPE_SWITCH) {
            return -1;
        }

        return loop.loopId;
    }

    public void setLoopUsed(long loopId) {
        if (loopStack.isEmpty()) {
            return;
        }

        int pos = loopStack.size() - 1;
        LoopWithType loop = null;
        do {
            loop = loopStack.get(pos);
            pos--;
        } while ((pos >= 0) && (loop.loopId != loopId));

        if (loop.loopId == loopId) {
            loop.used = true;
        }
    }

    @Override
    public NulWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue, HighlightData data) {
        stringAdded = true;
        return this;
    }

    @Override
    public GraphTextWriter append(char value) {
        stringAdded = true;
        return this;
    }

    @Override
    public GraphTextWriter append(int value) {
        stringAdded = true;
        return this;
    }

    @Override
    public GraphTextWriter append(long value) {
        stringAdded = true;
        return this;
    }

    @Override
    public NulWriter append(String str) {
        stringAdded = true;
        return this;
    }

    @Override
    public GraphTextWriter appendWithData(String str, HighlightData data) {
        stringAdded = true;
        return this;
    }

    @Override
    public NulWriter append(String str, long offset, long fileOffset) {
        stringAdded = true;
        return this;
    }

    @Override
    public NulWriter appendNoHilight(int i) {
        stringAdded = true;
        return this;
    }

    @Override
    public NulWriter appendNoHilight(String str) {
        stringAdded = true;
        return this;
    }

    public void mark() {
        stringAddedStack.add(stringAdded);
        stringAdded = false;
    }

    public boolean getMark() {
        boolean result = stringAdded;
        stringAdded = stringAddedStack.pop() || result;
        return result;
    }
}
