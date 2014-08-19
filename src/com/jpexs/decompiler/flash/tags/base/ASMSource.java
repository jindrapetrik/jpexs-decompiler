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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.List;

/**
 * Object containing ASM source
 *
 * @author JPEXS
 */
public interface ASMSource extends TreeItem {

    /**
     * Converts actions to ASM source
     *
     * @param exportMode PCode or hex?
     * @param writer
     * @param actions
     * @return ASM source
     * @throws java.lang.InterruptedException
     */
    public GraphTextWriter getASMSource(ScriptExportMode exportMode, GraphTextWriter writer, List<Action> actions) throws InterruptedException;

    /**
     * Whether or not this object contains ASM source
     *
     * @return True when contains
     */
    public boolean containsSource();

    /**
     * Returns actions associated with this object
     *
     * @return List of actions
     * @throws java.lang.InterruptedException
     */
    public ActionList getActions() throws InterruptedException;

    /**
     * Sets actions associated with this object
     *
     * @param actions Action list
     */
    public void setActions(List<Action> actions);

    public void setModified();

    public byte[] getActionBytes();

    public void setActionBytes(byte[] actionBytes);

    public GraphTextWriter getActionBytesAsHex(GraphTextWriter writer);

    public void addDisassemblyListener(DisassemblyListener listener);

    public void removeDisassemblyListener(DisassemblyListener listener);

    public GraphTextWriter getActionSourcePrefix(GraphTextWriter writer);

    public GraphTextWriter getActionSourceSuffix(GraphTextWriter writer);

    public int getPrefixLineCount();

    public String removePrefixAndSuffix(String source);

    public Tag getSourceTag();
}
