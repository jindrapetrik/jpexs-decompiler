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
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ContinueItem;
import java.util.ArrayList;
import java.util.List;

public class TryActionItem extends ActionItem implements Block {

    public List<GraphTargetItem> tryCommands;
    public List<GraphTargetItem> catchExceptions;
    public List<List<GraphTargetItem>> catchCommands;
    public List<GraphTargetItem> finallyCommands;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(tryCommands);
        ret.addAll(catchCommands);
        ret.add(finallyCommands);
        return ret;
    }

    public TryActionItem(List<GraphTargetItem> tryCommands, List<GraphTargetItem> catchExceptions, List<List<GraphTargetItem>> catchCommands, List<GraphTargetItem> finallyCommands) {
        super(null, NOPRECEDENCE);
        this.tryCommands = tryCommands;
        this.catchExceptions = catchExceptions;
        this.catchCommands = catchCommands;
        this.finallyCommands = finallyCommands;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        String ret = "";
        ret += hilight("try", highlight) + "\r\n" + hilight("{", highlight) + "\r\n";
        List<Object> localData = new ArrayList<>();
        localData.add(constants);
        for (GraphTargetItem ti : tryCommands) {
            if (!ti.isEmpty()) {
                ret += ti.toStringSemicoloned(highlight, localData) + "\r\n";
            }
        }
        ret += hilight("}", highlight);
        for (int e = 0; e < catchExceptions.size(); e++) {
            ret += "\r\n" + hilight("catch(", highlight) + catchExceptions.get(e).toStringNoQuotes(highlight, localData) + hilight(")", highlight) + "\r\n" + hilight("{", highlight) + "\r\n";
            List<GraphTargetItem> commands = catchCommands.get(e);
            for (GraphTargetItem ti : commands) {
                if (!ti.isEmpty()) {
                    ret += ti.toStringSemicoloned(highlight, localData) + "\r\n";
                }
            }
            ret += hilight("}", highlight);
        }
        if (finallyCommands.size() > 0) {
            ret += "\r\n" + hilight("finally", highlight) + "\r\n" + hilight("{", highlight) + "\r\n";
            for (GraphTargetItem ti : finallyCommands) {
                if (!ti.isEmpty()) {
                    ret += ti.toStringSemicoloned(highlight, localData) + "\r\n";
                }
            }
            ret += hilight("}", highlight);
        }
        return ret;
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
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        List<Action> tryCommandsA = asGenerator.toActionList(asGenerator.generate(localData, tryCommands));
        List<Action> finallyCommandsA = finallyCommands == null ? null : asGenerator.toActionList(asGenerator.generate(localData, finallyCommands));
        List<Action> catchCommandsA = null;
        String catchName = null;
        if (catchExceptions != null) {
            if (!catchExceptions.isEmpty()) {
                catchName = catchExceptions.get(0).toStringNoQuotes(false, new ConstantPool(asGenerator.getConstantPool()));
            }

        }
        int catchSize = 0;
        if (catchCommands != null && !catchCommands.isEmpty()) {
            catchCommandsA = asGenerator.toActionList(asGenerator.generate(localData, catchCommands.get(0)));
            catchSize = Action.actionsToBytes(catchCommandsA, false, SWF.DEFAULT_VERSION).length;
            tryCommandsA.add(new ActionJump(catchSize));
        }
        int finallySize = 0;
        if (finallyCommandsA != null) {
            finallySize = Action.actionsToBytes(finallyCommandsA, false, SWF.DEFAULT_VERSION).length;
        }
        int trySize = Action.actionsToBytes(tryCommandsA, false, SWF.DEFAULT_VERSION).length;
        ret.add(new ActionTry(false, finallyCommands != null, catchCommands != null, catchName, 0, trySize, catchSize, finallySize, SWF.DEFAULT_VERSION));
        ret.addAll(tryCommandsA);
        if (catchCommandsA != null) {
            ret.addAll(catchCommandsA);
        }
        if (finallyCommandsA != null) {
            ret.addAll(finallyCommandsA);
        }
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
