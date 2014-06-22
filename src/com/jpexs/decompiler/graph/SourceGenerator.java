/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface SourceGenerator {

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, AndItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, OrItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, IfItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TernarOpItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WhileItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DoWhileItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, SwitchItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, NotItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DuplicateItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, BreakItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ContinueItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, CommaExpressionItem item) throws CompilationException;

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TypeItem item) throws CompilationException;

    public List<GraphSourceItem> generateDiscardValue(SourceGeneratorLocalData localData, GraphTargetItem item) throws CompilationException;
}
