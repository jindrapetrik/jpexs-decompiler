package com.jpexs.asdec.abc.avm2.flowgraph;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPartMulti extends GraphPart {

   List<GraphPart> parts;

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
}
