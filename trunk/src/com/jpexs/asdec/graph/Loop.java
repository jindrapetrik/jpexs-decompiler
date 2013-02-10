package com.jpexs.asdec.graph;

/**
 *
 * @author JPEXS
 */
public class Loop {
   public GraphPart loopContinue;
   public GraphPart loopBreak;

   public Loop(GraphPart loopContinue, GraphPart loopBreak) {
      this.loopContinue = loopContinue;
      this.loopBreak = loopBreak;
   }
   
}
