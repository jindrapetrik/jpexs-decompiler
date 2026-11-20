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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.action.fastactionlist.ActionItem;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionList;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionDelete;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.helpers.CancellableWorker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Removes paired debugger resolver wrappers before graph translation.
 *
 * @author JPEXS
 */
public class ActionDebugResolveCleaner {

    private static final Object[] DELETE = {"_global", ActionGetVariable.class, "__resolve", ActionDelete.class, ActionPop.class};

    private static final Object[] RESTORE = {"_global", ActionGetVariable.class, "__resolve", "_global",
        ActionGetVariable.class, "__debugResolve", ActionGetMember.class, ActionSetMember.class};

    /**
     * Cleans matching wrappers within individual straight-line code sections.
     *
     * @param actions Actions to modify
     * @return Whether any wrappers were removed
     * @throws InterruptedException On cancellation
     */
    public boolean clean(ActionList actions) throws InterruptedException {
        FastActionList fast = new FastActionList(actions);
        fast.expandPushes();
        List<ActionItem> items = new ArrayList<>();
        for (ActionItem item : fast) {
            items.add(item);
        }
        Deque<Integer> starts = new ArrayDeque<>();
        List<ActionItem> remove = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            ActionItem item = items.get(i);
            if (item.isJumpTarget()) {
                starts.clear();
            }
            if (matches(items, i, DELETE)) {
                starts.push(i);
                i += DELETE.length - 1;
            } else if (matches(items, i, RESTORE) && !starts.isEmpty()) {
                int start = starts.pop();
                remove.addAll(items.subList(start, start + DELETE.length));
                remove.addAll(items.subList(i, i + RESTORE.length));
                i += RESTORE.length - 1;
            }
            item = items.get(i);
            if (item.action.isJump() || item.action.isBranch() || item.action.isExit()
                    || item.action instanceof GraphSourceItemContainer || item.isContainerLastAction()) {
                starts.clear();
            }
        }
        for (ActionItem item : remove) {
            fast.removeItem(item);
        }
        // expandPushes also changes the original push instructions. Always
        // retain the expanded operands, including when no wrapper matched.
        actions.setActions(fast.toActionList());
        return !remove.isEmpty();
    }

    private boolean matches(List<ActionItem> items, int start, Object[] pattern) {
        if (start + pattern.length > items.size()) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            ActionItem item = items.get(start + i);
            if ((i > 0 && item.isJumpTarget()) || (i < pattern.length - 1 && item.isContainerLastAction())) {
                return false;
            }
            Object expected = pattern[i];
            if (expected instanceof String) {
                if (!(item.action instanceof ActionPush)) {
                    return false;
                }
                ActionPush push = (ActionPush) item.action;
                if (push.values.size() != 1) {
                    return false;
                }
                Object value = push.values.get(0);
                if (value instanceof ConstantIndex) {
                    int index = ((ConstantIndex) value).index;
                    if (push.constantPool == null || index < 0 || index >= push.constantPool.size()) {
                        return false;
                    }
                    value = push.constantPool.get(index);
                }
                if (!expected.equals(value)) {
                    return false;
                }
            } else if (!((Class<?>) expected).isInstance(item.action)) {
                return false;
            }
        }
        return true;
    }
}
