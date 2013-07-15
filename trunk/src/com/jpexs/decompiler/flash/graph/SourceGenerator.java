/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.graph;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface SourceGenerator {

    public List<GraphSourceItem> generate(List<Object> localData, AndItem item);

    public List<GraphSourceItem> generate(List<Object> localData, OrItem item);

    public List<GraphSourceItem> generate(List<Object> localData, IfItem item);

    public List<GraphSourceItem> generate(List<Object> localData, TernarOpItem item);

    public List<GraphSourceItem> generate(List<Object> localData, WhileItem item);

    public List<GraphSourceItem> generate(List<Object> localData, DoWhileItem item);

    public List<GraphSourceItem> generate(List<Object> localData, ForItem item);

    public List<GraphSourceItem> generate(List<Object> localData, SwitchItem item);

    public List<GraphSourceItem> generate(List<Object> localData, NotItem item);

    public List<GraphSourceItem> generate(List<Object> localData, DuplicateItem item);

    public List<GraphSourceItem> generate(List<Object> localData, BreakItem item);

    public List<GraphSourceItem> generate(List<Object> localData, ContinueItem item);

    public List<GraphSourceItem> generate(List<Object> localData, List<GraphTargetItem> commands);

    public List<GraphSourceItem> generate(List<Object> localData, CommaExpressionItem item);
}
