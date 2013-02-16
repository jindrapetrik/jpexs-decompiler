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
package com.jpexs.asdec.abc.avm2.graph;

import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.graph.GraphPart;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ForException extends RuntimeException {

   public List<TreeItem> output;
   public List<TreeItem> finalOutput;
   public GraphPart continuePart;

   public ForException(List<TreeItem> output, List<TreeItem> finalOutput, GraphPart continuePart) {
      this.output = output;
      this.finalOutput = finalOutput;
      this.continuePart = continuePart;
   }
}
