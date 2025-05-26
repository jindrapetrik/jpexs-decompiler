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
package com.jpexs.decompiler.flash.abc.avm2.fastavm2;

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Instruction item for fast AVM2 list.
 *
 * @author JPEXS
 */
public class AVM2InstructionItem {

    /**
     * Instruction
     */
    public AVM2Instruction ins;

    /**
     * Previous instruction
     */
    public AVM2InstructionItem prev;

    /**
     * Next instruction
     */
    public AVM2InstructionItem next;

    /**
     * Jump target
     */
    private AVM2InstructionItem jumpTarget;

    /**
     * Instructions that jump here
     */
    public Set<AVM2InstructionItem> jumpsHere;

    /**
     * Instructions that this instruction is the last instruction of
     */
    public Set<AVM2InstructionItem> lastInsOf;

    /**
     * Last instruction container
     */
    private List<AVM2InstructionItem> containerLastInstructions;

    //
    /**
     * Whether this instruction is reachable. 1 means reachable, 2 means
     * reachable and processed.
     */
    int reachable;

    /**
     * Excluded
     */
    public boolean excluded;

    /**
     * Constructs a new AVM2InstructionItem
     *
     * @param ins Instruction
     */
    public AVM2InstructionItem(AVM2Instruction ins) {
        this.ins = ins;
    }

    /**
     * Checks whether this instruction is a jump target.
     *
     * @return Whether this instruction is a jump target
     */
    public boolean isJumpTarget() {
        return jumpsHere != null && !jumpsHere.isEmpty();
    }

    /**
     * Gets the number of jumps to this instruction.
     *
     * @return Number of jumps to this instruction
     */
    public int jumpsHereSize() {
        return jumpsHere == null ? 0 : jumpsHere.size();
    }

    /**
     * Checks whether this instruction is the last instruction of a container.
     *
     * @return Whether this instruction is the last instruction of a container
     */
    public boolean isContainerLastInstruction() {
        return lastInsOf != null && !lastInsOf.isEmpty();
    }

    /**
     * Remove jump target.
     */
    public void removeJumpTarget() {
        if (jumpTarget == null) {
            return;
        }

        if (jumpTarget.jumpsHere != null) {
            jumpTarget.jumpsHere.remove(this);
        }

        jumpTarget = null;
    }

    /**
     * Get jump target.
     *
     * @return Instruction item
     */
    public AVM2InstructionItem getJumpTarget() {
        return jumpTarget;
    }

    /**
     * Get jump target instruction.
     *
     * @return Instruction
     */
    public AVM2Instruction getJumpTargetInstruction() {
        return jumpTarget == null ? null : jumpTarget.ins;
    }

    /**
     * Set jump target.
     *
     * @param item Instruction item
     */
    public void setJumpTarget(AVM2InstructionItem item) {
        removeJumpTarget();

        if (item == null) {
            return;
        }

        if (item.jumpsHere == null) {
            item.jumpsHere = new HashSet<>();
        }

        item.jumpsHere.add(this);
        jumpTarget = item;
    }

    /**
     * Get last instructions of container.
     *
     * @return List of instruction items
     */
    public List<AVM2InstructionItem> getContainerLastInstructions() {
        return containerLastInstructions;
    }

    /**
     * Remove last instructions of container.
     */
    public void removeContainerLastInstructions() {
        if (containerLastInstructions == null) {
            return;
        }

        for (AVM2InstructionItem lastIns : containerLastInstructions) {
            if (lastIns.lastInsOf != null) {
                lastIns.lastInsOf.remove(this);
            }
        }

        containerLastInstructions = null;
    }

    /**
     * Replace container last instruction.
     *
     * @param oldItem Old instruction item
     * @param newItem New instruction item
     */
    public void replaceContainerLastInstruction(AVM2InstructionItem oldItem, AVM2InstructionItem newItem) {
        if (containerLastInstructions == null) {
            return;
        }

        for (int i = 0; i < containerLastInstructions.size(); i++) {
            if (containerLastInstructions.get(i) == oldItem) {
                containerLastInstructions.set(i, newItem);
                if (oldItem.lastInsOf != null) {
                    oldItem.lastInsOf.remove(this);
                }

                newItem.ensureLastInstructionOf().add(this);
            }
        }
    }

    /**
     * Set container last instructions.
     *
     * @param lastInstructions List of instruction items
     */
    public void setContainerLastInstructions(List<AVM2InstructionItem> lastInstructions) {
        removeContainerLastInstructions();

        for (AVM2InstructionItem lastIns : lastInstructions) {
            lastIns.ensureLastInstructionOf().add(this);
        }

        containerLastInstructions = lastInstructions;
    }

    /**
     * Ensure last instruction is non-null.
     *
     * @return Set of instruction items
     */
    private Set<AVM2InstructionItem> ensureLastInstructionOf() {
        if (lastInsOf == null) {
            lastInsOf = new HashSet<>();
        }

        return lastInsOf;
    }

    /**
     * Checks whether this instruction is excluded.
     *
     * @return Whether this instruction is excluded
     */
    public boolean isExcluded() {
        return excluded;
    }
}
