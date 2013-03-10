package com.jpexs.decompiler.flash.action;

/**
 *
 * @author JPEXS
 */
public class UnknownActionException extends RuntimeException{
   public int opCode;

   public UnknownActionException(int opCode) {
      this.opCode = opCode;
   }
   
}
