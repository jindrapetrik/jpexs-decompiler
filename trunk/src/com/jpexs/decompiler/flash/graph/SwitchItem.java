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
package com.jpexs.decompiler.flash.graph;

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
        List<List<GraphTargetItem>> ret = new ArrayList<List<GraphTargetItem>>();
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
    public String toString(List<Object> localData) {
        String ret = "";
        ret += "loopswitch" + loop.id + ":\r\n";
        ret += hilight("switch(") + switchedObject.toString(localData) + hilight(")") + "\r\n{\r\n";
        for (int i = 0; i < caseCommands.size(); i++) {
            for (int k = 0; k < valuesMapping.size(); k++) {
                if (valuesMapping.get(k) == i) {
                    ret += "case " + caseValues.get(k).toString(localData) + ":\r\n";
                }
            }
            ret += Graph.INDENTOPEN + "\r\n";
            for (int j = 0; j < caseCommands.get(i).size(); j++) {
                if (!caseCommands.get(i).get(j).isEmpty()) {
                    ret += caseCommands.get(i).get(j).toStringSemicoloned(localData) + "\r\n";
                }
            }
            ret += Graph.INDENTCLOSE + "\r\n";
        }
        if (defaultCommands != null) {
            if (defaultCommands.size() > 0) {
                ret += hilight("default") + ":\r\n";
                ret += Graph.INDENTOPEN + "\r\n";
                for (int j = 0; j < defaultCommands.size(); j++) {
                    if (!defaultCommands.get(j).isEmpty()) {
                        ret += defaultCommands.get(j).toStringSemicoloned(localData) + "\r\n";
                    }
                }
                ret += Graph.INDENTCLOSE + "\r\n";
            }
        }
        ret += hilight("}") + "\r\n";
        ret += ":loop" + loop.id;
        return ret;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<ContinueItem>();

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
}
