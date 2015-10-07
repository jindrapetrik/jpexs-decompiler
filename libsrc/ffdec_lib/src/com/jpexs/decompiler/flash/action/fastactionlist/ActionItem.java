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
package com.jpexs.decompiler.flash.action.fastactionlist;

import com.jpexs.decompiler.flash.action.Action;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ActionItem {

    public Action action;

    public ActionItem prev;

    public ActionItem next;

    public ActionItem jumpTarget;

    public List<ActionItem> containerLastActions;

    // 1 means reachable, 2 means reachable and processed
    int reachable;

    public ActionItem(Action action) {
        this.action = action;
    }
}
