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
package com.jpexs.decompiler.graph;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public interface GraphSourceItem extends Serializable {

    public void translate(List<Object> localData, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException;

    public boolean isJump();

    public boolean isBranch();

    public boolean isExit();

    public long getOffset();

    public boolean ignoredLoops();

    public List<Integer> getBranches(GraphSource code);

    public boolean isIgnored();

    public void setIgnored(boolean ignored, int pos);

    public boolean isDeobfuscatePop();
}
