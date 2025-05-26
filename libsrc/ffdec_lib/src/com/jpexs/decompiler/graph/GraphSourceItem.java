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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import java.io.Serializable;
import java.util.List;

/**
 * Item of Graph source. Usually an instruction.
 *
 * @author JPEXS
 */
public interface GraphSourceItem extends Serializable, Cloneable {

    /**
     * Translate the item to target items.
     *
     * @param localData Local data
     * @param stack Stack
     * @param output Output list
     * @param staticOperation Unused
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException;

    /**
     * Gets the number of stack items that are popped by this item.
     *
     * @param localData Local data
     * @param stack Stack
     * @return Number of stack items that are popped by this item
     */
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack);

    /**
     * Gets the number of stack items that are pushed by this item.
     *
     * @param localData Local data
     * @param stack Stack
     * @return Number of stack items that are pushed by this item
     */
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack);

    /**
     * Gets file offset.
     *
     * @return File offset
     */
    public long getFileOffset();

    /**
     * Checks whether this item is a jump.
     *
     * @return True if this item is a jump, false otherwise
     */
    public boolean isJump();

    /**
     * Checks whether this item is a branch.
     *
     * @return True if this item is a branch, false otherwise
     */
    public boolean isBranch();

    /**
     * Checks whether this item is an exit (throw, return, etc.).
     *
     * @return True if this item is an exit, false otherwise
     */
    public boolean isExit();

    /**
     * Gets the address.
     *
     * @return Address
     */
    public long getAddress();

    /**
     * Gets the line offset.
     *
     * @return Line offset
     */
    public long getLineOffset();

    /**
     * Checks whether the loops are ignored. FIXME: What is this, how to use it?
     *
     * @return True if the loops are ignored, false otherwise
     */
    public boolean ignoredLoops();

    /**
     * Gets branches
     *
     * @param code Code
     * @return List of IPs to branch to
     */
    public List<Integer> getBranches(GraphSource code);

    /**
     * Checks whether this item is ignored.
     *
     * @return True if this item is ignored, false otherwise
     */
    public boolean isIgnored();

    /**
     * Sets whether this item is ignored.
     *
     * @param ignored True if this item is ignored, false otherwise
     * @param pos Sub position
     */
    public void setIgnored(boolean ignored, int pos);

    /**
     * Checks whether this item is a DeobfuscatePop instruction. It is a special
     * instruction for deobfuscation.
     *
     * @return True if this item is a DeobfuscatePop instruction, false
     * otherwise
     */
    public boolean isDeobfuscatePop();

    /**
     * Gets the line in the high level source code.
     *
     * @return Line
     */
    public int getLine();

    /**
     * Gets the high level source code file name.
     *
     * @return File name
     */
    public String getFile();

    /**
     * Gets length of the item in bytes.
     *
     * @return Length of the item in bytes
     */
    public abstract int getBytesLength();

    /**
     * Gets virtual address. A virtual address can be used for storing original
     * address before applying deobfuscation.
     *
     * @return Virtual address
     */
    public long getVirtualAddress();

    /**
     * Sets virtual address. A virtual address can be used for storing original
     * address before applying deobfuscation.
     *
     * @param virtualAddress Virtual address
     */
    public void setVirtualAddress(long virtualAddress);
}
