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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.model.operations.AddActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf3.ActionGetURL;
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import static com.jpexs.decompiler.graph.GraphTargetItem.toSourceMerge;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class FSCommandActionItem extends ActionItem {

    private final GraphTargetItem command;

    public FSCommandActionItem(GraphSourceItem instruction, GraphTargetItem command) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.command = command;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("fscommand");
        writer.spaceBeforeCallParenthesies(1);
        writer.append("(");
        try {
            command.appendTo(writer, localData);
        } catch (InterruptedException ex) {
            Logger.getLogger(FSCommandActionItem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asg=(ActionSourceGenerator)generator;
        if((command instanceof DirectValueActionItem)&&((DirectValueActionItem)command).isString()){
            return toSourceMerge(localData, generator, new ActionGetURL("FSCommand:" + ((DirectValueActionItem)command).getAsString(), ""));
        }
        return toSourceMerge(localData, generator, new AddActionItem(null, asg.pushConstTargetItem("FSCommand:"), command, true), asg.pushConstTargetItem(""), new ActionGetURL2(1/*GET*/, false, false));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
