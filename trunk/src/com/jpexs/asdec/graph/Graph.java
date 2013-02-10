package com.jpexs.asdec.graph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class Graph {
   public List<GraphPart> heads;
   
   protected static void populateParts(GraphPart part, List<GraphPart> allParts) {
      if (allParts.contains(part)) {
         return;
      }
      allParts.add(part);
      for (GraphPart p : part.nextParts) {
         populateParts(p, allParts);
      }
   }
   
   protected void fixGraph(GraphPart part) {
      while (fixGraphOnce(part, new ArrayList<GraphPart>(), false)) {
      }
   }

   private boolean fixGraphOnce(GraphPart part, List<GraphPart> visited, boolean doChildren) {
      if (visited.contains(part)) {
         return false;
      }
      visited.add(part);
      boolean fixed = false;
      int i = 1;
      String lastpref = null;
      boolean modify = true;
      int prvni = -1;

      if (!doChildren) {

         List<GraphPart> uniqueRefs = new ArrayList<GraphPart>();
         for (GraphPart r : part.refs) {
            if (!uniqueRefs.contains(r)) {
               uniqueRefs.add(r);
            }
         }
         loopi:
         for (; i <= part.path.length(); i++) {
            lastpref = null;
            int pos = -1;
            for (GraphPart r : uniqueRefs) {
               pos++;
               if (r.path.startsWith("e")) {
                  continue;
               }
               if (part.leadsTo(r, new ArrayList<GraphPart>())) {
                  //modify=false;
                  //continue;
               }

               prvni = pos;
               if (i > r.path.length()) {
                  i--;
                  break loopi;
               }
               if (lastpref == null) {
                  lastpref = r.path.substring(0, i);
               } else {
                  if (!r.path.startsWith(lastpref)) {
                     i--;
                     break loopi;
                  }
               }
            }
         }
         if (i > part.path.length()) {
            i = part.path.length();
         }
         if (modify && ((uniqueRefs.size() > 1) && (prvni >= 0))) {
            String newpath = uniqueRefs.get(prvni).path.substring(0, i);
            if (!part.path.equals(newpath)) {
               if (part.path.startsWith(newpath)) {
                  String origPath = part.path;
                  GraphPart p = part;
                  part.path = newpath;
                  while (p.nextParts.size() == 1) {
                     p = p.nextParts.get(0);
                     if (!p.path.equals(origPath)) {
                        break;
                     }
                     p.path = newpath;
                  }
                  fixGraphOnce(part, new ArrayList<GraphPart>(), true);
                  fixed = true;
               }
            }
         }
      } else {

         if (!fixed) {
            if (part.nextParts.size() == 1) {
               if (!(part.path.startsWith("e") && (!part.nextParts.get(0).path.startsWith("e")))) {
                  if (part.nextParts.get(0).path.length() > part.path.length()) {
                     part.nextParts.get(0).path = part.path;
                     fixed = true;
                  }
               }
            }
            if (part.nextParts.size() > 1) {
               for (int j = 0; j < part.nextParts.size(); j++) {
                  GraphPart npart = part.nextParts.get(j);

                  if (npart.path.length() > part.path.length() + 1) {
                     npart.path = part.path + "" + j;
                     fixed = true;
                  }
               }
            }
         }

      }
      for (GraphPart p : part.nextParts) {
         fixGraphOnce(p, visited, doChildren);
      }
      return fixed;
   }
   
    protected void makeMulti(GraphPart part, List<GraphPart> visited) {
      if (visited.contains(part)) {
         return;
      }
      visited.add(part);
      GraphPart p = part;
      List<GraphPart> multiList = new ArrayList<GraphPart>();
      multiList.add(p);
      while ((p.nextParts.size() == 1) && (p.nextParts.get(0).refs.size() == 1)) {
         p = p.nextParts.get(0);
         multiList.add(p);
      }
      if (multiList.size() > 1) {
         GraphPartMulti gpm = new GraphPartMulti(multiList);
         gpm.refs = part.refs;
         GraphPart lastPart = multiList.get(multiList.size() - 1);
         gpm.nextParts = lastPart.nextParts;
         for (GraphPart next : gpm.nextParts) {
            int index = next.refs.indexOf(lastPart);
            if (index == -1) {

               continue;
            }
            next.refs.remove(lastPart);
            next.refs.add(index, gpm);
         }
         for (GraphPart parent : part.refs) {
            if (parent.start == -1) {
               continue;
            }
            int index = parent.nextParts.indexOf(part);
            if (index == -1) {
               continue;
            }
            parent.nextParts.remove(part);
            parent.nextParts.add(index, gpm);
         }
      }
      for (int i = 0; i < part.nextParts.size(); i++) {
         makeMulti(part.nextParts.get(i), visited);
      }
   }
}
