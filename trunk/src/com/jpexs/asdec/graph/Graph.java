package com.jpexs.asdec.graph;

import com.jpexs.asdec.abc.gui.GraphFrame;
import com.jpexs.asdec.helpers.Helper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Graph {
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
    
    public GraphPart deepCopy(GraphPart part,List<GraphPart> visited,List<GraphPart> copies){
       if(visited==null){
          visited=new ArrayList<GraphPart>();
       }
       if(copies==null){
          copies=new ArrayList<GraphPart>();
       }
       if(visited.contains(part)){
          return copies.get(visited.indexOf(part));
       }
       visited.add(part);
       GraphPart copy=new GraphPart(part.start,part.end);
       copy.path=part.path;
       copies.add(copy);
       copy.nextParts=new ArrayList<GraphPart>();
       for(int i=0;i<part.nextParts.size();i++){          
          copy.nextParts.add(deepCopy(part.nextParts.get(i),visited,copies));
       }       
       for(int i=0;i<part.refs.size();i++){          
          copy.refs.add(deepCopy(part.refs.get(i),visited,copies));
       }
       return copy;
    }
    
    public void resetGraph(GraphPart part,List<GraphPart> visited){
       if(visited.contains(part)){
          return;
       }
       visited.add(part);
       int pos=0;
       for(GraphPart p:part.nextParts){   
          if(!visited.contains(p)){
            p.path=part.path+pos;
          }          
          resetGraph(p, visited);
          pos++;
       }
    }
    
    public GraphPart getCommonPart(List<GraphPart> parts){
       GraphPart head=new GraphPart(0, 0);       
       head.nextParts.addAll(parts);
       List<GraphPart> allVisited=new ArrayList<GraphPart>();
       head=deepCopy(head,allVisited,null);
       for(GraphPart g:head.nextParts){
          for(GraphPart r:g.refs){
             r.nextParts.remove(g);
          }
          g.refs.clear();
          g.refs.add(head);
       }
       head.path="0";
       resetGraph(head,new ArrayList<GraphPart>());             
       fixGraph(head);
       
       /*Graph gr=new Graph();
       gr.heads=new ArrayList<GraphPart>();
       gr.heads.add(head);
       GraphFrame gf=new GraphFrame(gr, "");
       gf.setVisible(true);
       */
       
       GraphPart next=head.getNextPartPath(new ArrayList<GraphPart>());
       if(next==null){
          return null;
       }
       for(GraphPart g:allVisited){
          if(g.start==next.start){
             return g;
          }
       }
       return null;
    }
}
