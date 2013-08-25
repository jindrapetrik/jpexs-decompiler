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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.action.Action;
import java.util.List;

/**
 * Object containing ASM source
 *
 * @author JPEXS
 */
public interface ASMSource {

    /**
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @param hex Add hexadecimal?
     * @return ASM source
     */
    public String getASMSource(int version, boolean hex, boolean highlight);

    /**
     * Whether or not this object contains ASM source
     *
     * @return True when contains
     */
    public boolean containsSource();

    /**
     * Returns actions associated with this object
     *
     * @param version Version
     * @return List of actions
     */
    public List<Action> getActions(int version);

    /**
     * Sets actions associated with this object
     *
     * @param version Version
     * @param actions Action list
     */
    public void setActions(List<Action> actions, int version);

    public byte[] getActionBytes();

    public void setActionBytes(byte actionBytes[]);

    public long getPos();

    public void addDisassemblyListener(DisassemblyListener listener);

    public void removeDisassemblyListener(DisassemblyListener listener);

    public String getActionSourcePrefix();

    public String getActionSourceSuffix();

    public int getActionSourceIndent();
}
