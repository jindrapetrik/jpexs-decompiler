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
package com.jpexs.decompiler.flash.action.fastactionlist;

import com.jpexs.decompiler.flash.action.Action;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ActionItem {

    public Action action;

    public ActionItem prev;

    public ActionItem next;

    private ActionItem jumpTarget;

    public Set<ActionItem> jumpsHere;

    public Set<ActionItem> lastActionOf;

    private List<ActionItem> containerLastActions;

    // 1 means reachable, 2 means reachable and processed
    int reachable;

    public boolean excluded;

    public ActionItem(Action action) {
        this.action = action;
    }

    public boolean isJumpTarget() {
        return jumpsHere != null && !jumpsHere.isEmpty();
    }

    public int jumpsHereSize() {
        return jumpsHere == null ? 0 : jumpsHere.size();
    }

    public boolean isContainerLastAction() {
        return lastActionOf != null && !lastActionOf.isEmpty();
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

    public ActionItem getJumpTarget() {
        return jumpTarget;
    }

    public Action getJumpTargetAction() {
        return jumpTarget == null ? null : jumpTarget.action;
    }

    public void setJumpTarget(ActionItem item) {
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

    public List<ActionItem> getContainerLastActions() {
        return containerLastActions;
    }

    public void removeContainerLastActions() {
        if (containerLastActions == null) {
            return;
        }

        for (ActionItem lastAction : containerLastActions) {
            if (lastAction.lastActionOf != null) {
                lastAction.lastActionOf.remove(this);
            }
        }

        containerLastActions = null;
    }

    public void replaceContainerLastAction(ActionItem oldItem, ActionItem newItem) {
        if (containerLastActions == null) {
            return;
        }

        for (int i = 0; i < containerLastActions.size(); i++) {
            if (containerLastActions.get(i) == oldItem) {
                containerLastActions.set(i, newItem);
                if (oldItem.lastActionOf != null) {
                    oldItem.lastActionOf.remove(this);
                }

                newItem.ensureLastActionOf().add(this);
            }
        }
    }

    public void setContainerLastActions(List<ActionItem> lastActions) {
        removeContainerLastActions();

        for (ActionItem lastAction : lastActions) {
            lastAction.ensureLastActionOf().add(this);
        }

        containerLastActions = lastActions;
    }

    private Set<ActionItem> ensureLastActionOf() {
        if (lastActionOf == null) {
            lastActionOf = new HashSet<>();
        }

        return lastActionOf;
    }

    public boolean isExcluded() {
        return excluded;
    }
}
