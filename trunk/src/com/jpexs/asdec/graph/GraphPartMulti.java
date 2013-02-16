package com.jpexs.asdec.graph;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPartMulti extends GraphPart {

   public List<GraphPart> parts;

   public GraphPartMulti(List<GraphPart> parts) {
      super(parts.get(0).start, parts.get(parts.size() - 1).end);
      this.parts = parts;
      this.path=parts.get(0).path;
   }

   @Override
   public String toString() {
      String ret="";
      ret+="[multi ";
      boolean first=true;
      for(GraphPart g:parts){
         if(first){
            first=false;
         }else{
            ret+=", ";
         }
         ret+=g.toString();
      }
      ret+="]";
      return ret;
   }
   
   @Override
   public int getHeight(){
      int ret=0;
      for(GraphPart p:parts){
         ret+=p.getHeight();
      }
      return ret;
   }

   @Override
   public int getPosAt(int offset) {
      int ofs=0;
      int pos=0;
      for(GraphPart p:parts){
         for(int i=0;i<p.getHeight();i++){
            pos=p.start+i;
            ofs+=1;
            if(ofs==offset){
               return pos;
            }
         }
      }
      return -1;
   }
   
   @Override
   public List<GraphPart> getSubParts(){     
      return Collections.unmodifiableList(parts);
   }
   
}
