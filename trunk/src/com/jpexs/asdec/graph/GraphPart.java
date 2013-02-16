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
package com.jpexs.asdec.graph;

import com.jpexs.asdec.abc.avm2.treemodel.ContinueTreeItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
   public String path="";
   public List<GraphPart> refs=new ArrayList<GraphPart>();
   public boolean ignored=false;
   public List<Object> forContinues=new ArrayList<Object>();

   private boolean leadsTo(GraphPart part,List<GraphPart> visited,List<GraphPart> ignored){
      if(visited.contains(this)){
         return false;
      }
      if(ignored.contains(this)){
         return false;
      }
      visited.add(this);
      for(GraphPart p:nextParts){
         if(p==part){
            return true;
         }else{
            if(p.leadsTo(part,visited,ignored)){
               return true;
            }
         }
      }
      return false;
   }
   public boolean leadsTo(GraphPart part,List<GraphPart> ignored){
      
      return leadsTo(part,new ArrayList<GraphPart>(),ignored);
   }
   
   public GraphPart(int start, int end) {
      this.start = start;
      this.end = end;
   }
   
   
   private GraphPart getNextPartPath(GraphPart original,String path,List<GraphPart> visited){
      if(visited.contains(this)){
         return null;
      }
      visited.add(this);
      for(GraphPart p:nextParts){
         if(p==original){
            continue;
         }
         if(p.path.equals(path)){
            return p;
         }else if(p.path.length()>=path.length()){
            GraphPart gp=p.getNextPartPath(original,path,visited);
            if(gp!=null){
               return gp;
            }
         }
      }
      return null;
   }
   public GraphPart getNextPartPath(List<GraphPart> ignored){
      List<GraphPart> visited=new ArrayList<GraphPart>();
      visited.addAll(ignored);
      return getNextPartPath(this,path,visited);
   }
   
   public GraphPart getNextSuperPartPath(List<GraphPart> ignored){
      List<GraphPart> visited=new ArrayList<GraphPart>();
      visited.addAll(ignored);
      return getNextSuperPartPath(this,path,visited);
   }

   private GraphPart getNextSuperPartPath(GraphPart original,String path,List<GraphPart> visited){
      if(visited.contains(this)){
         return null;
      }
      visited.add(this);
      for(GraphPart p:nextParts){
         if(p==original){
            continue;
         }
         if(p.path.length()<path.length()){
            return p;
         }else {
            GraphPart gp=p.getNextSuperPartPath(original,path,visited);
            if(gp!=null){
               return gp;
            }
         } 
      }
      return null;
   }
   
   @Override
   public String toString() {
      if (end < start) {
         return "<-> "+(start+1)+"-"+(end+1);
      }
      return "" + (start + 1) + "-" + (end + 1) + (instanceCount > 1 ? "(" + instanceCount + " links)" : "")+"  p"+path;
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

   public int getHeight(){
      return end-start+1;
   }
   
   public int getPosAt(int offset){
      return start+offset;
   }
   
   public boolean containsPart(GraphPart what) {
      return containsPart(this, what, new ArrayList<GraphPart>());
   }
   
   public List<GraphPart> getSubParts(){
      List<GraphPart> ret=new ArrayList<GraphPart>();
      ret.add(this);
      return  ret;
   }
}
