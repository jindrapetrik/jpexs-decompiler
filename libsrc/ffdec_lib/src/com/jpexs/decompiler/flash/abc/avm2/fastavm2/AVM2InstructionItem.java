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
package com.jpexs.decompiler.flash.abc.avm2.fastavm2;

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class AVM2InstructionItem {

    public AVM2Instruction ins;

    public AVM2InstructionItem prev;

    public AVM2InstructionItem next;

    private AVM2InstructionItem jumpTarget;

    public Set<AVM2InstructionItem> jumpsHere;

    public Set<AVM2InstructionItem> lastInsOf;

    private List<AVM2InstructionItem> containerLastInstructions;

    // 1 means reachable, 2 means reachable and processed
    int reachable;

    public boolean excluded;

    public AVM2InstructionItem(AVM2Instruction ins) {
        this.ins = ins;
    }

    public boolean isJumpTarget() {
        return jumpsHere != null && !jumpsHere.isEmpty();
    }

    public int jumpsHereSize() {
        return jumpsHere == null ? 0 : jumpsHere.size();
    }

    public boolean isContainerLastInstruction() {
        return lastInsOf != null && !lastInsOf.isEmpty();
    }

    public void removeJumpTarget() {
        if (jumpTarget == null) {
            return;
        }

        if (jumpTarget.jumpsHere != null) {
            jumpTarget.jumpsHere.remove(this);
        }

        jumpTarget = null;
    }

    public AVM2InstructionItem getJumpTarget() {
        return jumpTarget;
    }

    public AVM2Instruction getJumpTargetInstruction() {
        return jumpTarget == null ? null : jumpTarget.ins;
    }

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

    public List<AVM2InstructionItem> getContainerLastInstructions() {
        return containerLastInstructions;
    }

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

    public void setContainerLastInstructions(List<AVM2InstructionItem> lastInstructions) {
        removeContainerLastInstructions();

        for (AVM2InstructionItem lastIns : lastInstructions) {
            lastIns.ensureLastInstructionOf().add(this);
        }

        containerLastInstructions = lastInstructions;
    }

    private Set<AVM2InstructionItem> ensureLastInstructionOf() {
        if (lastInsOf == null) {
            lastInsOf = new HashSet<>();
        }

        return lastInsOf;
    }

    public boolean isExcluded() {
        return excluded;
    }
}
