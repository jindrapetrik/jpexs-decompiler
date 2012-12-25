/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec.abc.avm2.flowgraph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPart {

   public int start = 0;
   public int end = 0;
   public int instanceCount = 0;
   public List<GraphPart> nextParts = new ArrayList<GraphPart>();
   public int posX = -1;
   public int posY = -1;

   public GraphPart(int start, int end) {
      this.start = start;
      this.end = end;
   }

   @Override
   public String toString() {
      if (end < start) {
         return "<->";
      }
      return "" + (start + 1) + "-" + (end + 1) + (instanceCount > 1 ? "(" + instanceCount + " links)" : "");
   }

   public boolean containsIP(int ip) {
      return (ip >= start) && (ip <= end);
   }

   private boolean containsPart(GraphPart part, GraphPart what, List<GraphPart> used) {
      if (used.contains(part)) {
         return false;
      }
      used.add(part);
      for (GraphPart subpart : part.nextParts) {
         if (subpart == what) {
            return true;
         }
         if (containsPart(subpart, what, used)) {
            return true;
         }
      }
      return false;
   }

   public boolean containsPart(GraphPart what) {
      return containsPart(this, what, new ArrayList<GraphPart>());
   }
}
