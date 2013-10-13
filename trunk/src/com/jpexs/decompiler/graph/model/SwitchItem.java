/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class SwitchItem extends LoopItem implements Block {

    public GraphTargetItem switchedObject;
    public List<GraphTargetItem> caseValues;
    public List<List<GraphTargetItem>> caseCommands;
    public List<GraphTargetItem> defaultCommands;
    public List<Integer> valuesMapping;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.addAll(caseCommands);
        ret.add(defaultCommands);
        return ret;
    }

    public SwitchItem(GraphSourceItem instruction, Loop loop, GraphTargetItem switchedObject, List<GraphTargetItem> caseValues, List<List<GraphTargetItem>> caseCommands, List<GraphTargetItem> defaultCommands, List<Integer> valuesMapping) {
        super(instruction, loop);
        this.switchedObject = switchedObject;
        this.caseValues = caseValues;
        this.caseCommands = caseCommands;
        this.defaultCommands = defaultCommands;
        this.valuesMapping = valuesMapping;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        hilight("loopswitch" + loop.id + ":", writer).appendNewLine();
        hilight("switch(", writer);
        switchedObject.toString(writer, localData);
        hilight(")", writer).appendNewLine();
        hilight("{", writer).appendNewLine();
        hilight(Graph.INDENTOPEN, writer).appendNewLine();
        for (int i = 0; i < caseCommands.size(); i++) {
            for (int k = 0; k < valuesMapping.size(); k++) {
                if (valuesMapping.get(k) == i) {
                    hilight("case ", writer);
                    caseValues.get(k).toString(writer, localData);
                    hilight(":", writer).appendNewLine();
                }
            }
            hilight(Graph.INDENTOPEN, writer).appendNewLine();
            for (int j = 0; j < caseCommands.get(i).size(); j++) {
                if (!caseCommands.get(i).get(j).isEmpty()) {
                    caseCommands.get(i).get(j).toStringSemicoloned(writer, localData.constants).appendNewLine();
                }
            }
            hilight(Graph.INDENTCLOSE, writer).appendNewLine();
        }
        if (defaultCommands != null) {
            if (defaultCommands.size() > 0) {
                hilight("default", writer);
                hilight(":", writer).appendNewLine();
                hilight(Graph.INDENTOPEN, writer).appendNewLine();
                for (int j = 0; j < defaultCommands.size(); j++) {
                    if (!defaultCommands.get(j).isEmpty()) {
                        defaultCommands.get(j).toStringSemicoloned(writer, localData.constants).appendNewLine();
                    }
                }
                hilight(Graph.INDENTCLOSE, writer).appendNewLine();
            }
        }
        hilight(Graph.INDENTCLOSE, writer).appendNewLine();
        hilight("}", writer).appendNewLine();
        hilight(":loop" + loop.id, writer);
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();

        for (List<GraphTargetItem> onecase : caseCommands) {
            for (GraphTargetItem ti : onecase) {
                if (ti instanceof ContinueItem) {
                    ret.add((ContinueItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
        if (defaultCommands != null) {
            for (GraphTargetItem ti : defaultCommands) {
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
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
