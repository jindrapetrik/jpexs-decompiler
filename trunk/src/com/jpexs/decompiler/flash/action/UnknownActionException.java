package com.jpexs.decompiler.flash.action;

/**
 *
 * @author JPEXS
 */
public class UnknownActionException extends RuntimeException {

   public int opCode;

   public UnknownActionException(int opCode) {
      super("Unknown opCode: 0x" + Integer.toHexString(opCode));
      this.opCode = opCode;
   }
}
