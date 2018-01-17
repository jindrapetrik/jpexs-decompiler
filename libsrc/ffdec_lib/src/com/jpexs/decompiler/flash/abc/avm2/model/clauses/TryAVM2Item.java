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
package com.jpexs.decompiler.flash.abc.avm2.model.clauses;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AssignableAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.NameAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TryAVM2Item extends AVM2Item implements Block {

    public List<GraphTargetItem> tryCommands;

    public List<ABCException> catchExceptions;

    public List<NameAVM2Item> catchExceptions2;

    public List<List<GraphTargetItem>> catchCommands;

    public List<GraphTargetItem> finallyCommands;

    public List<List<AssignableAVM2Item>> catchVariables = new ArrayList<>();

    public String finCatchName = "";

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (tryCommands != null) {
            ret.add(tryCommands);
        }
        ret.addAll(catchCommands);
        if (finallyCommands != null) {
            ret.add(finallyCommands);
        }
        return ret;
    }

    public TryAVM2Item(List<GraphTargetItem> tryCommands, List<ABCException> catchExceptions, List<List<GraphTargetItem>> catchCommands, List<GraphTargetItem> finallyCommands, String finCatchName) {
        super(null, null, NOPRECEDENCE);
        this.tryCommands = tryCommands;
        this.catchExceptions = catchExceptions;
        this.catchCommands = catchCommands;
        this.finallyCommands = finallyCommands;
        this.finCatchName = finCatchName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("try");
        appendBlock(null, writer, localData, tryCommands);
        for (int e = 0; e < catchExceptions.size(); e++) {
            writer.newLine();
            writer.append("catch(");
            String localName = catchExceptions.get(e).getVarName(localData.constantsAvm2, localData.fullyQualifiedNames);
            if (localName.isEmpty()) {
                localName = finCatchName;
            }

            HighlightData data = new HighlightData();
            data.localName = localName;
            data.declaration = true;

            int eti = catchExceptions.get(e).type_index;

            data.declaredType = eti <= 0 ? DottedChain.ALL : localData.constantsAvm2.getMultiname(eti).getNameWithNamespace(localData.constantsAvm2, true);
            writer.hilightSpecial(localName, HighlightSpecialType.TRY_NAME, e, data);
            writer.append(":");
            writer.hilightSpecial(catchExceptions.get(e).getTypeName(localData.constantsAvm2, localData.fullyQualifiedNames), HighlightSpecialType.TRY_TYPE, e);
            writer.append(")");
            List<GraphTargetItem> commands = catchCommands.get(e);
            appendBlock(null, writer, localData, commands);
        }
        if (catchExceptions.isEmpty() || finallyCommands.size() > 0) {
            writer.newLine();
            writer.append("finally");
            appendBlock(null, writer, localData, finallyCommands);
        }
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        for (GraphTargetItem ti : tryCommands) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        if (finallyCommands != null) {
            for (GraphTargetItem ti : finallyCommands) {
                if (ti instanceof ContinueItem) {
                    ret.add((ContinueItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
        for (List<GraphTargetItem> commands : catchCommands) {
            for (GraphTargetItem ti : commands) {
                if (ti instanceof ContinueItem) {
                    ret.add((ContinueItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return ((AVM2SourceGenerator) generator).generate(localData, this);
    }
}
