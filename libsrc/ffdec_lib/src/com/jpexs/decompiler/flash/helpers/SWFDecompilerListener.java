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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;

/**
 * Listener for SWF decompiler events.
 *
 * @author JPEXS
 */
public interface SWFDecompilerListener {

    /**
     * Called when a file is proxied
     *
     * @param data file data
     * @return proxied file data
     */
    byte[] proxyFileCatched(byte[] data);

    /**
     * Called when a SWF file is parsed
     *
     * @param swf SWF object
     */
    void swfParsed(SWF swf);

    /**
     * Called when an action list is parsed
     *
     * @param actions Action list
     * @param swf SWF object
     * @throws InterruptedException On interrupt
     */
    void actionListParsed(ActionList actions, SWF swf) throws InterruptedException;

    /**
     * Called when an action tree is created
     *
     * @param tree Action tree
     * @param swf SWF object
     * @throws InterruptedException On interrupt
     */
    void actionTreeCreated(List<GraphTargetItem> tree, SWF swf) throws InterruptedException;

    /**
     * Called when an ABC is parsed
     *
     * @param abc ABC object
     * @param swf SWF object
     */
    void abcParsed(ABC abc, SWF swf);

    /**
     * Called when a method body is parsed
     *
     * @param abc ABC object
     * @param body Method body
     * @param swf SWF object
     */
    void methodBodyParsed(ABC abc, MethodBody body, SWF swf);

    /**
     * This method is only called when deobfuscation is enabled and new
     * deobfuscation mode is selected.
     *
     * @param path Path
     * @param classIndex Class index
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param abc ABC object
     * @param trait Trait object
     * @param methodInfo Method info
     * @param body Method body
     * @throws InterruptedException On interrupt
     */
    void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException;
}
