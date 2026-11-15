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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LabelItem;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Regression tests for goto processing. */
public class GraphGotoTest {

    @Test
    public void testKeepGotoWhenItsTargetContainsTheJump() throws ReflectiveOperationException {
        GotoItem jump = new GotoItem(null, null, null, "target");
        List<GraphTargetItem> branch = new ArrayList<>(Collections.singletonList(jump));
        IfItem inner = new IfItem(null, null, null, null, branch, Collections.emptyList());
        IfItem outer = new IfItem(null, null, null, null, Collections.singletonList(inner), Collections.emptyList());
        LabelItem label = new LabelItem(null, null, null, "target");
        ReturnVoidAVM2Item exit = new ReturnVoidAVM2Item(null, null);
        List<GraphTargetItem> target = new ArrayList<>(Arrays.asList(label, outer, exit));
        List<List<GraphTargetItem>> processed = new ArrayList<>(Collections.singletonList(target));

        Method processIfGotos = Graph.class.getDeclaredMethod("processIfGotos2", List.class, List.class, List.class, List.class);
        processIfGotos.setAccessible(true);
        Graph graph = new Graph(null, null, Collections.emptyList(), 0);
        processIfGotos.invoke(graph, processed, Collections.singletonList(jump), branch, target);

        Assert.assertEquals(branch.size(), 1);
        Assert.assertSame(branch.get(0), jump);
        Assert.assertEquals(target.size(), 3);
        Assert.assertSame(target.get(0), label);
        Assert.assertSame(target.get(1), outer);
        Assert.assertSame(target.get(2), exit);
    }
}
