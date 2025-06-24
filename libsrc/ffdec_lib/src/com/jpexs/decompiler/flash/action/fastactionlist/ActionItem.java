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
package com.jpexs.decompiler.flash.action.fastactionlist;

import com.jpexs.decompiler.flash.action.Action;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Action in the fast action list.
 *
 * @author JPEXS
 */
public class ActionItem {

    /**
     * Action
     */
    public Action action;

    /**
     * Previous action in the list
     */
    public ActionItem prev;

    /**
     * Next action in the list
     */
    public ActionItem next;

    /**
     * Jump target of this action
     */
    private ActionItem jumpTarget;

    /**
     * Actions that jump to this action
     */
    public Set<ActionItem> jumpsHere;

    /**
     * Last actions
     */
    public Set<ActionItem> lastActionOf;

    /**
     * Container last actions
     */
    private List<ActionItem> containerLastActions;

    /**
     * Reachable flag. 1 means reachable, 2 means reachable and processed
     */
    int reachable;

    /**
     * Excluded flag
     */
    public boolean excluded;

    /**
     * Constructs a new ActionItem.
     *
     * @param action Action
     */
    public ActionItem(Action action) {
        this.action = action;
    }

    /**
     * Checks if this action is a jump target.
     *
     * @return true if this action is a jump target
     */
    public boolean isJumpTarget() {
        return jumpsHere != null && !jumpsHere.isEmpty();
    }

    /**
     * Gets the number of jumps to this action.
     *
     * @return Number of jumps to this action
     */
    public int jumpsHereSize() {
        return jumpsHere == null ? 0 : jumpsHere.size();
    }

    /**
     * Checks if this action is the last action of a container.
     *
     * @return True if this action is the last action of a container
     */
    public boolean isContainerLastAction() {
        return lastActionOf != null && !lastActionOf.isEmpty();
    }

    /**
     * Removes the jump target.
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
     * Gets the jump target.
     *
     * @return Jump target
     */
    public ActionItem getJumpTarget() {
        return jumpTarget;
    }

    /**
     * Gets the jump target action.
     *
     * @return Jump target action
     */
    public Action getJumpTargetAction() {
        return jumpTarget == null ? null : jumpTarget.action;
    }

    /**
     * Sets the jump target.
     *
     * @param item Jump target
     */
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

    /**
     * Gets container last actions.
     *
     * @return Container last actions
     */
    public List<ActionItem> getContainerLastActions() {
        return containerLastActions;
    }

    /**
     * Removes container last actions.
     */
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

    /**
     * Replaces container last action.
     *
     * @param oldItem Old action
     * @param newItem New action
     */
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

    /**
     * Sets container last actions.
     *
     * @param lastActions Container last actions
     */
    public void setContainerLastActions(List<ActionItem> lastActions) {
        removeContainerLastActions();

        for (ActionItem lastAction : lastActions) {
            lastAction.ensureLastActionOf().add(this);
        }

        containerLastActions = lastActions;
    }

    /**
     * Ensures last action of.
     *
     * @return Last action of
     */
    private Set<ActionItem> ensureLastActionOf() {
        if (lastActionOf == null) {
            lastActionOf = new HashSet<>();
        }

        return lastActionOf;
    }

    /**
     * Checks if this action is excluded.
     *
     * @return True if this action is excluded
     */
    public boolean isExcluded() {
        return excluded;
    }
}
