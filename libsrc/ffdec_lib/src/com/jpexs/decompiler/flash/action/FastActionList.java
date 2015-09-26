/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author JPEXS
 */
public class FastActionList {

    private LinkedList<Action> actions;

    public FastActionList(List<Action> actions) {
        this.actions = new LinkedList<>(actions);
    }

    public void expandPushes() {
        ListIterator<Action> iterator = actions.listIterator();
        while (iterator.hasNext()) {
            Action action = iterator.next();
            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                if (push.values.size() > 1) {
                    for (int i = 1; i < push.values.size(); i++) {
                        Object value = push.values.get(i);
                        ActionPush newPush = new ActionPush(value);
                        newPush.constantPool = push.constantPool;
                        iterator.add(newPush);
                    }

                    Object obj = push.values.get(0);
                    push.values.clear();
                    push.values.add(obj);
                }
            }
        }
    }

    private void updateActionLengths() {
        for (Action action : actions) {
            action.updateLength();
        }
    }

    public ActionList toActionList() {
        ActionList result = new ActionList(actions);
        updateActionLengths();
        return result;
    }
}
