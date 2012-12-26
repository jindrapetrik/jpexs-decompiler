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
package com.jpexs.asdec.abc.avm2;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.ABCInputStream;
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.IfTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.arithmetic.*;
import com.jpexs.asdec.abc.avm2.instructions.bitwise.*;
import com.jpexs.asdec.abc.avm2.instructions.comparsion.*;
import com.jpexs.asdec.abc.avm2.instructions.construction.*;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugFileIns;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.asdec.abc.avm2.instructions.executing.*;
import com.jpexs.asdec.abc.avm2.instructions.jumps.*;
import com.jpexs.asdec.abc.avm2.instructions.localregs.*;
import com.jpexs.asdec.abc.avm2.instructions.other.*;
import com.jpexs.asdec.abc.avm2.instructions.stack.*;
import com.jpexs.asdec.abc.avm2.instructions.types.*;
import com.jpexs.asdec.abc.avm2.instructions.xml.*;
import com.jpexs.asdec.abc.avm2.treemodel.*;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.*;
import com.jpexs.asdec.abc.avm2.treemodel.operations.AndTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.OrTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreDecrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreIncrementTreeItem;
import com.jpexs.asdec.abc.types.ABCException;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.helpers.Highlighting;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AVM2Code {

   private static final boolean DEBUG_MODE=true;
   public static int toSourceLimit = -1;
   public ArrayList<AVM2Instruction> code = new ArrayList<AVM2Instruction>();
   public static boolean DEBUG_REWRITE = false;
   public static final int OPT_U30 = 0x100;
   public static final int OPT_U8 = 0x200;
   public static final int OPT_S24 = 0x300;
   public static final int OPT_CASE_OFFSETS = 0x400;
   public static final int OPT_BYTE = 0x500;
   public static final int DAT_MULTINAME_INDEX = OPT_U30 + 0x01;
   public static final int DAT_ARG_COUNT = OPT_U30 + 0x02;
   public static final int DAT_METHOD_INDEX = OPT_U30 + 0x03;
   public static final int DAT_STRING_INDEX = OPT_U30 + 0x04;
   public static final int DAT_DEBUG_TYPE = OPT_U8 + 0x05;
   public static final int DAT_REGISTER_INDEX = OPT_U8 + 0x06;
   public static final int DAT_LINENUM = OPT_U30 + 0x07;
   public static final int DAT_LOCAL_REG_INDEX = OPT_U30 + 0x08;
   public static final int DAT_SLOT_INDEX = OPT_U30 + 0x09;
   public static final int DAT_SLOT_SCOPE_INDEX = OPT_U30 + 0x0A;
   public static final int DAT_OFFSET = OPT_S24 + 0x0B;
   public static final int DAT_EXCEPTION_INDEX = OPT_U30 + 0x0C;
   public static final int DAT_CLASS_INDEX = OPT_U30 + 0x0D;
   public static final int DAT_INT_INDEX = OPT_U30 + 0x0E;
   public static final int DAT_UINT_INDEX = OPT_U30 + 0x0F;
   public static final int DAT_DOUBLE_INDEX = OPT_U30 + 0x10;
   public static final int DAT_DECIMAL_INDEX = OPT_U30 + 0x11;
   public static final int DAT_CASE_BASEOFFSET = OPT_S24 + 0x12;
   public static InstructionDefinition instructionSet[] = new InstructionDefinition[]{
      new AddIns(),
      new InstructionDefinition(0x9b, "add_d", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            return -2 + 1; //?
         }
      },
      new AddIIns(),
      new InstructionDefinition(0xb5, "add_p", new int[]{AVM2Code.OPT_U30}),
      new ApplyTypeIns(),
      new AsTypeIns(),
      new AsTypeLateIns(),
      new BitAndIns(),
      new BitNotIns(),
      new BitOrIns(),
      new BitXorIns(),
      new InstructionDefinition(0x01, "bkpt", new int[]{}),
      new InstructionDefinition(0xf2, "bkptline", new int[]{AVM2Code.OPT_U30}),
      new CallIns(),
      new InstructionDefinition(0x4d, "callinterface", new int[]{AVM2Code.OPT_U30}),
      new CallMethodIns(),
      new CallPropertyIns(),
      new CallPropLexIns(),
      new CallPropVoidIns(),
      new CallStaticIns(),
      new CallSuperIns(),
      new InstructionDefinition(0x4b, "callsuperid", new int[]{}),
      new CallSuperVoidIns(),
      new CheckFilterIns(),
      new CoerceIns(),
      new CoerceAIns(),
      new InstructionDefinition(0x81, "coerce_b", new int[]{}), //stack:-1+1
      new InstructionDefinition(0x84, "coerce_d", new int[]{}), //stack:-1+1
      new InstructionDefinition(0x83, "coerce_i", new int[]{}), //stack:-1+1
      new InstructionDefinition(0x89, "coerce_o", new int[]{}), //stack:-1+1
      new CoerceSIns(),
      new InstructionDefinition(0x88, "coerce_u", new int[]{}), //stack:-1+1
      new InstructionDefinition(0x9a, "concat", new int[]{}) {
 @Override
 public int getStackDelta(AVM2Instruction ins, ABC abc) {
    return -2 + 1; //?
 }
},
      new ConstructIns(),
      new ConstructPropIns(),
      new ConstructSuperIns(),
      new ConvertBIns(),
      new ConvertIIns(),
      new ConvertDIns(),
      new ConvertOIns(),
      new ConvertUIns(),
      new ConvertSIns(),
      new InstructionDefinition(0x79, "convert_m", new int[]{}), //-1 +1
      new InstructionDefinition(0x7a, "convert_m_p", new int[]{AVM2Code.OPT_U30 /*param (?)*/}) {
 @Override
 public int getStackDelta(AVM2Instruction ins, ABC abc) {
    throw new UnsupportedOperationException();
 }
},
      new DebugIns(),
      new DebugFileIns(),
      new DebugLineIns(),
      new DecLocalIns(),
      new DecLocalIIns(),
      new DecrementIns(),
      new DecrementIIns(),
      new InstructionDefinition(0x5b, "deldescendants", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new DeletePropertyIns(),
      new InstructionDefinition(0x6b, "deletepropertylate", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new DivideIns(),
      new InstructionDefinition(0xb8, "divide_p", new int[]{AVM2Code.OPT_U30}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            return -2 + 1; //?
         }
      },
      new DupIns(),
      new DXNSIns(),
      new DXNSLateIns(),
      new EqualsIns(),
      new EscXAttrIns(),
      new EscXElemIns(),
      new InstructionDefinition(0x5f, "finddef", new int[]{AVM2Code.DAT_MULTINAME_INDEX}),
      /* //Duplicate OPCODE with deldescendants. Prefering deldescendants (found in FLEX compiler)
       new InstructionDefinition(0x5b,"findpropglobalstrict",new int[]{AVM2Code.DAT_MULTINAME_INDEX}){

       @Override
       public int getStackDelta(AVM2Instruction ins, ABC abc) {
       throw new UnsupportedOperationException();
       }

       @Override
       public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
       throw new UnsupportedOperationException();
       }

       },*/
      new InstructionDefinition(0x5c, "findpropglobal", new int[]{AVM2Code.DAT_MULTINAME_INDEX}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new FindPropertyIns(),
      new FindPropertyStrictIns(),
      new GetDescendantsIns(),
      new GetGlobalScopeIns(),
      new GetGlobalSlotIns(),
      new GetLexIns(),
      new GetLocalIns(),
      new GetLocal0Ins(),
      new GetLocal1Ins(),
      new GetLocal2Ins(),
      new GetLocal3Ins(),
      new InstructionDefinition(0x67, "getouterscope", new int[]{AVM2Code.DAT_MULTINAME_INDEX}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new GetPropertyIns(),
      new GetScopeObjectIns(),
      new GetSlotIns(),
      new GetSuperIns(),
      new GreaterEqualsIns(),
      new GreaterThanIns(),
      new HasNextIns(),
      new HasNext2Ins(),
      new IfEqIns(),
      new IfFalseIns(),
      new IfGeIns(),
      new IfGtIns(),
      new IfLeIns(),
      new IfLtIns(),
      new IfNGeIns(),
      new IfNGtIns(),
      new IfNLeIns(),
      new IfNLtIns(),
      new IfNeIns(),
      new IfStrictEqIns(),
      new IfStrictNeIns(),
      new IfTrueIns(),
      new InIns(),
      new IncLocalIns(),
      new IncLocalIIns(),
      new IncrementIns(),
      new IncrementIIns(),
      new InstructionDefinition(0x9c, "increment_p", new int[]{AVM2Code.OPT_U30 /*param*/}),
      new InstructionDefinition(0x9d, "inclocal_p", new int[]{AVM2Code.OPT_U30 /*param*/, AVM2Code.DAT_REGISTER_INDEX}),
      new InstructionDefinition(0x9e, "decrement_p", new int[]{AVM2Code.OPT_U30 /*param*/}),
      new InstructionDefinition(0x9f, "declocal_p", new int[]{AVM2Code.OPT_U30 /*param*/, AVM2Code.DAT_REGISTER_INDEX}),
      new InitPropertyIns(),
      new InstanceOfIns(),
      new IsTypeIns(),
      new IsTypeLateIns(),
      new JumpIns(),
      new KillIns(),
      new LabelIns(),
      new LessEqualsIns(),
      new LessThanIns(),
      new LookupSwitchIns(),
      new LShiftIns(),
      new ModuloIns(),
      new InstructionDefinition(0xb9, "modulo_p", new int[]{AVM2Code.OPT_U30}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            return -2 + 1; //?
         }
      },
      new MultiplyIns(),
      new MultiplyIIns(),
      new InstructionDefinition(0xb7, "multiply_p", new int[]{AVM2Code.OPT_U30}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            return -2 + 1; //?
         }
      },
      new NegateIns(),
      new NegateIIns(),
      new InstructionDefinition(0x8f, "negate_p", new int[]{AVM2Code.OPT_U30 /* param */}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new NewActivationIns(),
      new NewArrayIns(),
      new NewCatchIns(),
      new NewClassIns(),
      new NewFunctionIns(),
      new NewObjectIns(),
      new NextNameIns(),
      new NextValueIns(),
      new NopIns(),
      new NotIns(),
      new PopIns(),
      new PopScopeIns(),
      new PushByteIns(),
      new InstructionDefinition(0x22, "pushconstant", new int[]{AVM2Code.DAT_STRING_INDEX}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            return 1; //?
         }
      },
      new InstructionDefinition(0x33, "pushdecimal", new int[]{AVM2Code.DAT_DECIMAL_INDEX}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            return 1; //?
         }
      },
      new InstructionDefinition(0x34, "pushdnan", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            return 1; //?
         }
      },
      new PushDoubleIns(),
      new PushFalseIns(),
      new PushIntIns(),
      new PushNamespaceIns(),
      new PushNanIns(),
      new PushNullIns(),
      new PushScopeIns(),
      new PushShortIns(),
      new PushStringIns(),
      new PushTrueIns(),
      new PushUIntIns(),
      new PushUndefinedIns(),
      new PushWithIns(),
      new ReturnValueIns(),
      new ReturnVoidIns(),
      new RShiftIns(),
      new SetLocalIns(),
      new SetLocal0Ins(),
      new SetLocal1Ins(),
      new SetLocal2Ins(),
      new SetLocal3Ins(),
      new SetGlobalSlotIns(),
      new SetPropertyIns(),
      new InstructionDefinition(0x69, "setpropertylate", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new SetSlotIns(),
      new SetSuperIns(),
      new StrictEqualsIns(),
      new SubtractIns(),
      new SubtractIIns(),
      new InstructionDefinition(0xb6, "subtract_p", new int[]{AVM2Code.OPT_U30}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new SwapIns(),
      new ThrowIns(),
      new InstructionDefinition(0xf3, "timestamp", new int[]{}),
      new TypeOfIns(),
      new URShiftIns(),
      new InstructionDefinition(0x35, "li8", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x36, "li16", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x37, "li32", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x38, "lf32", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x39, "lf64", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x3A, "si8", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x3B, "si16", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x3C, "si32", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x3D, "sf32", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x3E, "sf64", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x50, "sxi1", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x51, "sxi8", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      },
      new InstructionDefinition(0x52, "sxi16", new int[]{}) {
         @Override
         public int getStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }

         @Override
         public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
            throw new UnsupportedOperationException();
         }
      }
   };
   //endoflist
   public static InstructionDefinition instructionSetByCode[] = buildInstructionSetByCode();
   
   public boolean hideTemporaryRegisters=true;

   private static InstructionDefinition[] buildInstructionSetByCode() {
      InstructionDefinition result[] = new InstructionDefinition[256];
      for (InstructionDefinition id : instructionSet) {
         if (result[id.instructionCode] != null) {
            System.out.println("Warning: Duplicate OPCODE for instruction " + result[id.instructionCode] + " " + id);
         }
         result[id.instructionCode] = id;
      }
      return result;
   }
   public static final String IDENTOPEN = "/*IDENTOPEN*/";
   public static final String IDENTCLOSE = "/*IDENTCLOSE*/";

   private class ConvertOutput {

      public Stack<TreeItem> stack;
      public List<TreeItem> output;

      public ConvertOutput(Stack<TreeItem> stack, List<TreeItem> output) {
         this.stack = stack;
         this.output = output;
      }
   }

   public AVM2Code() {
   }

   public Object execute(HashMap arguments, ConstantPool constants) {
      int pos = 0;
      LocalDataArea lda = new LocalDataArea();
      lda.localRegisters = arguments;
      try {
         while (true) {
            AVM2Instruction ins = code.get(pos);
            if (ins.definition instanceof JumpIns) {
               pos = adr2pos((Long) ins.getParamsAsList(constants).get(0));
               continue;
            }
            if (ins.definition instanceof IfFalseIns) {
               Boolean b = (Boolean) lda.operandStack.pop();
               if (b == false) {
                  pos = adr2pos((Long) ins.getParamsAsList(constants).get(0));
               } else {
                  pos++;
               }
               continue;
            }
            if (ins.definition instanceof IfTrueIns) {
               Boolean b = (Boolean) lda.operandStack.pop();
               if (b == true) {
                  pos = adr2pos((Long) ins.getParamsAsList(constants).get(0));
               } else {
                  pos++;
               }
               continue;
            }
            if (ins.definition instanceof ReturnValueIns) {
               return lda.operandStack.pop();
            }
            if (ins.definition instanceof ReturnVoidIns) {
               return null;
            }
            ins.definition.execute(lda, constants, ins.getParamsAsList(constants));
            pos++;
         }
      } catch (ConvertException e) {
      }
      return null;
   }

   public AVM2Code(InputStream is) throws IOException {
      ABCInputStream ais = new ABCInputStream(is);
      while (ais.available() > 0) {
         long startOffset = ais.getPosition();
         ais.startBuffer();
         int instructionCode = ais.read();
         InstructionDefinition instr = instructionSetByCode[instructionCode];
         if (instr != null) {
            int actualOperands[];
            if (instructionCode == 0x1b) { //switch
               int firstOperand = ais.readS24();
               int case_count = ais.readU30();
               actualOperands = new int[case_count + 3];
               actualOperands[0] = firstOperand;
               actualOperands[1] = case_count;
               for (int c = 0; c < case_count + 1; c++) {
                  actualOperands[2 + c] = ais.readS24();
               }
            } else {
               actualOperands = new int[instr.operands.length];
               for (int op = 0; op < instr.operands.length; op++) {
                  switch (instr.operands[op] & 0xff00) {
                     case OPT_U30:
                        actualOperands[op] = ais.readU30();
                        break;
                     case OPT_U8:
                        actualOperands[op] = ais.read();
                        break;
                     case OPT_BYTE:
                        actualOperands[op] = (byte) ais.read();
                        break;
                     case OPT_S24:
                        actualOperands[op] = ais.readS24();
                        break;
                  }
               }
            }

            code.add(new AVM2Instruction(startOffset, instr, actualOperands, ais.stopBuffer()));
         } else {
            throw new UnknownInstructionCode(instructionCode);
         }
      }
   }

   public void compact() {
      code.trimToSize();
   }

   public byte[] getBytes() {
      return getBytes(null);
   }

   public byte[] getBytes(byte origBytes[]) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      OutputStream cos;
      if ((origBytes != null) && (DEBUG_REWRITE)) {
         ByteArrayInputStream origis = new ByteArrayInputStream(origBytes);
         cos = new CopyOutputStream(bos, origis);
      } else {
         cos = bos;
      }
      try {
         for (AVM2Instruction instruction : code) {
            cos.write(instruction.getBytes());
         }
      } catch (IOException ex) {
      }
      return bos.toByteArray();
   }

   @Override
   public String toString() {
      String s = "";
      for (AVM2Instruction instruction : code) {
         s += instruction.toString() + "\r\n";
      }
      return s;
   }

   public String toString(ConstantPool constants) {
      String s = "";
      int i = 0;
      for (AVM2Instruction instruction : code) {
         s += Helper.formatAddress(i) + " " + instruction.toString(constants) + "\r\n";
         i++;
      }
      return s;
   }

   private static String popStack(Stack<String> stack) {
      for (int i = stack.size() - 1; i >= 0; i--) {
         String s = stack.get(i);
         if (!s.startsWith("//")) {
            stack.remove(i);
            return s;
         }
      }
      return null;
   }

   public String toASMSource(ConstantPool constants, MethodBody body) {
      String ret = "";
      for (int e = 0; e < body.exceptions.length; e++) {
         ret += "exception " + e + " m[" + body.exceptions[e].name_index + "]\"" + Helper.escapeString(body.exceptions[e].getVarName(constants)) + "\" "
                 + "m[" + body.exceptions[e].type_index + "]\"" + Helper.escapeString(body.exceptions[e].getTypeName(constants)) + "\"\n";
      }
      List<Long> offsets = new ArrayList<Long>();
      for (AVM2Instruction ins : code) {
         offsets.addAll(ins.getOffsets());
      }
      long ofs = 0;
      for (AVM2Instruction ins : code) {
         if (offsets.contains(ofs)) {
            ret += "ofs" + Helper.formatAddress(ofs) + ":";
         }
         for (int e = 0; e < body.exceptions.length; e++) {
            if (body.exceptions[e].start == ofs) {
               ret += "exceptionstart " + e + ":";
            }
            if (body.exceptions[e].end == ofs) {
               ret += "exceptionend " + e + ":";
            }
            if (body.exceptions[e].target == ofs) {
               ret += "exceptiontarget " + e + ":";
            }
         }
         ret += ins.toStringNoAddress(constants) + "\n";
         ofs += ins.getBytes().length;
      }

      return ret;
   }
   private boolean cacheActual = false;
   private List<Long> posCache;

   private void buildCache() {
      posCache = new ArrayList<Long>();
      long a = 0;
      for (int i = 0; i < code.size(); i++) {
         posCache.add(a);
         a += code.get(i).getBytes().length;
      }
      posCache.add(a);
      cacheActual = true;
   }

   public int adr2pos(long address) throws ConvertException {
      if (!cacheActual) {
         buildCache();
      }
      int ret = posCache.indexOf(address);
      if (ret == -1) {
         throw new ConvertException("Bad jump", -1);
      }
      return ret;
   }

   public int pos2adr(int pos) {
      if (!cacheActual) {
         buildCache();
      }
      return posCache.get(pos).intValue();
   }

   private static String listToString(List<TreeItem> stack, ConstantPool constants, HashMap<Integer, String> localRegNames) {
      String ret = "";
      for (int d = 0; d < stack.size(); d++) {
         TreeItem o = stack.get(d);
         ret += o.toStringSemicoloned(constants, localRegNames) + "\r\n";
      }
      return ret;
   }

   private static String innerStackToString(List stack) {
      String ret = "";
      for (int d = 0; d < stack.size(); d++) {
         Object o = stack.get(d);
         ret += o.toString();
         if (d < stack.size() - 1) {
            if (!ret.endsWith("\r\n")) {
               ret += "\r\n";
            }
         }
      }
      return ret;
   }

   private class Loop {

      public int loopContinue;
      public int loopBreak;
      public int continueCount = 0;
      public int breakCount = 0;

      public Loop(int loopContinue, int loopBreak) {
         this.loopContinue = loopContinue;
         this.loopBreak = loopBreak;
      }
   }
   private List<Loop> loopList;
   private List<Integer> unknownJumps;
   private List<Integer> finallyJumps;
   private List<ABCException> parsedExceptions;
   private List<Integer> ignoredIns;

   private String stripBrackets(String s) {
      if (s.startsWith("(") && (s.endsWith(")"))) {
         s = s.substring(1, s.length() - 1);
      }
      return s;
   }

   private int checkCatches(ABC abc, ConstantPool constants, MethodInfo method_info[], Stack<TreeItem> stack, Stack<TreeItem> scopeStack, List<TreeItem> output, MethodBody body, int ip) throws ConvertException {
      /*int newip = ip;
       loope:
       for (int e = 0; e < body.exceptions.length; e++) {
       if (pos2adr(ip) == body.exceptions[e].end) {
       for (int f = 0; f < e; f++) {
       if (body.exceptions[e].startServer == body.exceptions[f].startServer) {
       if (body.exceptions[e].end == body.exceptions[f].end) {
       continue loope;
       }
       }
       }
       output.add("}");
       if (!(code.get(ip).definition instanceof JumpIns)) {
       throw new ConvertException("No jump to skip catches");
       }
       int addrAfterCatches = pos2adr(ip + 1) + code.get(ip).operands[0];
       int posAfterCatches = adr2pos(addrAfterCatches);
       for (int g = 0; g < body.exceptions.length; g++) {
       if (body.exceptions[e].startServer == body.exceptions[g].startServer) {
       if (body.exceptions[e].end == body.exceptions[g].end) {
       if (body.exceptions[g].isFinally()) {
       output.add("finally");
       } else {
       output.add("catch(" + body.exceptions[g].getVarName(constants) + ":" + body.exceptions[g].getTypeName(constants) + ")");
       }
       output.add("{");

       if (body.exceptions[g].isFinally()) {
       int jumppos = adr2pos(body.exceptions[g].target) - 1;
       AVM2Instruction jumpIns = code.get(jumppos);
       if (!(jumpIns.definition instanceof JumpIns)) {
       throw new ConvertException("No jump in finally block");
       }
       int nextAddr = pos2adr(jumppos + 1) + jumpIns.operands[0];
       int nextins = adr2pos(nextAddr);
       int pos = nextins;
       Integer uj = new Integer(nextins);
       if (unknownJumps.contains(uj)) {
       unknownJumps.remove(uj);
       }
       int endpos = 0;
       do {
       if (code.get(pos).definition instanceof LookupSwitchIns) {
       if (code.get(pos).operands[0] == 0) {
       if (adr2pos(pos2adr(pos) + code.get(pos).operands[2]) < pos) {
       endpos = pos - 1;
       newip = endpos + 1;
       break;
       }
       }
       }
       pos++;
       } while (pos < code.size());
       output.addAll(toSource(stack, scopeStack, abc, constants, method_info, body, nextins, endpos).output);
       } else {

       int pos = adr2pos(body.exceptions[g].target);
       int endpos = posAfterCatches - 1;
       for (int p = pos; p < posAfterCatches; p++) {
       if (code.get(p).definition instanceof JumpIns) {
       int nextAddr = pos2adr(p + 1) + code.get(p).operands[0];
       int nextPos = adr2pos(nextAddr);
       if (nextPos == posAfterCatches) {
       endpos = p - 1;
       break;
       }
       }
       }
       Stack cstack = new Stack<TreeItem>();
       cstack.push("catched " + body.exceptions[g].getVarName(constants));
       List outcatch = toSource(cstack, new Stack<TreeItem>(), abc, constants, method_info, body, pos, endpos).output;
       output.addAll(outcatch);
       newip = endpos + 1;
       }
       output.add("}");
       }
       }
       }
       }
       }
       return newip;*/
      return ip;
   }
   boolean isCatched = false;

   private boolean isKilled(int regName, int start, int end) {
      for (int k = start; k <= end; k++) {
         if (code.get(k).definition instanceof KillIns) {
            if (code.get(k).operands[0] == regName) {
               return true;
            }
         }
      }
      return false;
   }
   private int toSourceCount = 0;

   private int ipOfType(int from, boolean up, Class search, Class skipped, int start, int end) {
      if (up) {
         for (int i = from; i >= start; i--) {
            if (search.isInstance(code.get(i).definition)) {
               return i;
            } else if ((skipped != null) && skipped.isInstance(code.get(i).definition)) {
               //skipped
            } else {
               return -1;
            }
         }
      } else {
         for (int i = from; i <= end; i++) {
            if (search.isInstance(code.get(i).definition)) {
               return i;
            } else if ((skipped != null) && skipped.isInstance(code.get(i).definition)) {
               //skipped
            } else {
               return -1;
            }
         }

      }
      return -1;
   }

   public HashMap<Integer, String> getLocalRegNamesFromDebug(ABC abc) {
      HashMap<Integer, String> localRegNames = new HashMap<Integer, String>();
      for (AVM2Instruction ins : code) {
         if (ins.definition instanceof DebugIns) {
            if (ins.operands[0] == 1) {
               localRegNames.put(ins.operands[2] + 1, abc.constants.constant_string[ins.operands[1]]);
            }
         }
      }
      return localRegNames;
   }

   private void clearTemporaryRegisters(List<TreeItem> output) {
      for (int i = 0; i < output.size(); i++) {
         if (output.get(i) instanceof SetLocalTreeItem) {
            if (isKilled(((SetLocalTreeItem) output.get(i)).regIndex, 0, code.size() - 1)) {
               output.remove(i);
               i--;
            }
         }
      }
   }

   private int fixIPAfterDebugLine(int ip) {
      if (ip >= code.size()) {
         return code.size() - 1;
      }
      if (code.get(ip).definition instanceof DebugLineIns) {
         return ip + 1;
      }
      return ip;
   }

   private int fixAddrAfterDebugLine(int addr) throws ConvertException {
      return pos2adr(fixIPAfterDebugLine(adr2pos(addr)));
   }

   private ConvertOutput toSource(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, Stack<TreeItem> scopeStack, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, int start, int end, HashMap<Integer, String> localRegNames) throws ConvertException {
      boolean debugMode = false;
      if (debugMode) {
         System.out.println("OPEN SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
      }
      //if(true) return "";
      toSourceCount++;
      if (toSourceLimit > 0) {
         if (toSourceCount > toSourceLimit) {
            throw new ConvertException("Limit of subs(" + toSourceLimit + ") was reached", start);
         }
      }
      List<TreeItem> output = new ArrayList();
      String ret = "";
      int ip = start;
      try {
         int addr;
         iploop:
         while (ip <= end) {

            if (ignoredIns.contains(ip)) {
               ip++;
               continue;
            }
            addr = pos2adr(ip);
            int ipfix = fixIPAfterDebugLine(ip);
            int addrfix = pos2adr(ipfix);
            int maxend = -1;
            List<ABCException> catchedExceptions = new ArrayList<ABCException>();
            for (int e = 0; e < body.exceptions.length; e++) {
               if (addrfix == fixAddrAfterDebugLine(body.exceptions[e].start)) {
                  if (!body.exceptions[e].isFinally()) {
                     if ((fixAddrAfterDebugLine(body.exceptions[e].end) > maxend) && (!parsedExceptions.contains(body.exceptions[e]))) {
                        catchedExceptions.clear();
                        maxend = fixAddrAfterDebugLine(body.exceptions[e].end);
                        catchedExceptions.add(body.exceptions[e]);
                     } else if (fixAddrAfterDebugLine(body.exceptions[e].end) == maxend) {
                        catchedExceptions.add(body.exceptions[e]);
                     }
                  }
               }
            }
            if (catchedExceptions.size() > 0) {
               ip = ipfix;
               addr = addrfix;
               parsedExceptions.addAll(catchedExceptions);
               int endpos = adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(0).end));


               List<List<TreeItem>> catchedCommands = new ArrayList<List<TreeItem>>();
               if (code.get(endpos).definition instanceof JumpIns) {
                  int afterCatchAddr = pos2adr(endpos + 1) + code.get(endpos).operands[0];
                  int afterCatchPos = adr2pos(afterCatchAddr);
                  Collections.sort(catchedExceptions, new Comparator<ABCException>() {
                     public int compare(ABCException o1, ABCException o2) {
                        try {
                           return fixAddrAfterDebugLine(o1.target) - fixAddrAfterDebugLine(o2.target);
                        } catch (ConvertException ex) {
                           return 0;
                        }
                     }
                  });


                  List<TreeItem> finallyCommands = new ArrayList<TreeItem>();
                  int returnPos = afterCatchPos;
                  for (int e = 0; e < body.exceptions.length; e++) {
                     if (body.exceptions[e].isFinally()) {
                        if (addr == fixAddrAfterDebugLine(body.exceptions[e].start)) {
                           if (afterCatchPos + 1 == adr2pos(fixAddrAfterDebugLine(body.exceptions[e].end))) {
                              AVM2Instruction jmpIns = code.get(adr2pos(fixAddrAfterDebugLine(body.exceptions[e].end)));
                              if (jmpIns.definition instanceof JumpIns) {
                                 int finStart = adr2pos(fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytes().length + jmpIns.operands[0]);
                                 finallyJumps.add(finStart);
                                 if (unknownJumps.contains(finStart)) {
                                    unknownJumps.remove((Integer) finStart);
                                 }
                                 for (int f = finStart; f <= end; f++) {
                                    if (code.get(f).definition instanceof LookupSwitchIns) {
                                       AVM2Instruction swins = code.get(f);
                                       if (swins.operands.length >= 3) {
                                          if (swins.operands[0] == swins.getBytes().length) {
                                             if (adr2pos(pos2adr(f) + swins.operands[2]) < finStart) {
                                                finallyCommands = toSource(isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, finStart, f - 1, localRegNames).output;
                                                returnPos = f + 1;
                                                break;
                                             }
                                          }
                                       }
                                    }
                                 }

                                 break;
                              }
                           }
                        }
                     }
                  }

                  for (int e = 0; e < catchedExceptions.size(); e++) {
                     int eendpos = 0;
                     if (e < catchedExceptions.size() - 1) {
                        eendpos = adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(e + 1).target)) - 2;
                     } else {
                        eendpos = afterCatchPos - 1;
                     }
                     Stack<TreeItem> substack = new Stack<TreeItem>();
                     substack.add(new ExceptionTreeItem(catchedExceptions.get(e)));
                     catchedCommands.add(toSource(isStatic, classIndex, localRegs, substack, new Stack<TreeItem>(), abc, constants, method_info, body, adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(e).target)), eendpos, localRegNames).output);
                  }

                  List<TreeItem> tryCommands = toSource(isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, ip, endpos - 1, localRegNames).output;


                  output.add(new TryTreeItem(tryCommands, catchedExceptions, catchedCommands, finallyCommands));
                  ip = returnPos;
                  addr = pos2adr(ip);
               }

            }

            if (ip > end) {
               break;
            }

            if (unknownJumps.contains(ip)) {
               unknownJumps.remove(new Integer(ip));
               throw new UnknownJumpException(stack, ip, output);
            }
            AVM2Instruction ins = code.get(ip);

            if ((ip + 8 < code.size())) { //return in finally clause
               if (ins.definition instanceof SetLocalTypeIns) {
                  if (code.get(ip + 1).definition instanceof PushByteIns) {
                     AVM2Instruction jmp = code.get(ip + 2);
                     if (jmp.definition instanceof JumpIns) {
                        if (jmp.operands[0] == 0) {
                           if (code.get(ip + 3).definition instanceof LabelIns) {
                              if (code.get(ip + 4).definition instanceof PopIns) {
                                 if (code.get(ip + 5).definition instanceof LabelIns) {
                                    AVM2Instruction gl = code.get(ip + 6);
                                    if (gl.definition instanceof GetLocalTypeIns) {
                                       if (((GetLocalTypeIns) gl.definition).getRegisterId(gl) == ((SetLocalTypeIns) ins.definition).getRegisterId(ins)) {
                                          AVM2Instruction ki = code.get(ip + 7);
                                          if (ki.definition instanceof KillIns) {
                                             if (ki.operands[0] == ((SetLocalTypeIns) ins.definition).getRegisterId(ins)) {
                                                if (code.get(ip + 8).definition instanceof ReturnValueIns) {
                                                   ip = ip + 8;
                                                   continue;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            if ((ip + 2 < code.size()) && (ins.definition instanceof NewCatchIns)) { //Filling local register in catch clause
               if (code.get(ip + 1).definition instanceof DupIns) {
                  if (code.get(ip + 2).definition instanceof SetLocalTypeIns) {
                     ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames);
                     ip += 3;
                     continue;
                  }
               }
            }

            if (ins.definition instanceof JumpIns) { //Ifs with multiple conditions
               if (ins.operands[0] == 0) {
                  ip++;
                  addr = pos2adr(ip);
               } else if (ins.operands[0] > 0) {
                  int secondAddr = addr + ins.getBytes().length;
                  int jumpAddr = secondAddr + ins.operands[0];
                  int jumpPos = adr2pos(jumpAddr);//

                  if (finallyJumps.contains(jumpPos)) {
                     if (code.get(ip + 1).definition instanceof LabelIns) {
                        if (code.get(ip + 2).definition instanceof PopIns) {
                           if (code.get(ip + 3).definition instanceof LabelIns) {
                              if (code.get(ip + 4).definition instanceof GetLocalTypeIns) {
                                 if (code.get(ip - 1).definition instanceof PushByteIns) {
                                    if (code.get(ip - 2).definition instanceof SetLocalTypeIns) {
                                       if (((SetLocalTypeIns) code.get(ip - 2).definition).getRegisterId(code.get(ip - 2)) == ((GetLocalTypeIns) code.get(ip + 4).definition).getRegisterId(code.get(ip + 4))) {
                                          SetLocalTreeItem ti = (SetLocalTreeItem) output.remove(output.size() - 1);
                                          stack.add(ti.value);
                                          ip = ip + 5;
                                          continue;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                     //continue;
                     ip++;
                     continue;
                  }
                  for (Loop l : loopList) {
                     if (l.loopBreak == jumpPos) {
                        output.add(new BreakTreeItem(ins, l.loopBreak));
                        addr = secondAddr;
                        ip = ip + 1;
                        continue iploop;
                     }
                     if (l.loopContinue == jumpPos) {
                        l.continueCount++;
                        output.add(new ContinueTreeItem(ins, l.loopBreak));
                        addr = secondAddr;
                        ip = ip + 1;
                        continue iploop;
                     }
                  }


                  boolean backJumpFound = false;
                  int afterBackJumpAddr = 0;
                  AVM2Instruction backJumpIns = null;
                  boolean isSwitch = false;
                  int switchPos = 0;
                  loopj:
                  for (int j = jumpPos; j <= end; j++) {
                     if (code.get(j).definition instanceof IfTypeIns) {
                        afterBackJumpAddr = pos2adr(j + 1);

                        if (afterBackJumpAddr + code.get(j).operands[0] == secondAddr) {
                           backJumpFound = true;
                           backJumpIns = code.get(j);
                           break;
                        }
                     }
                     if (code.get(j).definition instanceof LookupSwitchIns) {
                        for (int h = 2; h < code.get(j).operands.length; h++) {
                           int ofs = code.get(j).operands[h] + pos2adr(j);
                           if (ofs == secondAddr) {
                              isSwitch = true;
                              switchPos = j;
                              break loopj;
                           }
                        }
                     }
                  }
                  if (isSwitch) {
                     AVM2Instruction killIns = code.get(switchPos - 1);
                     if (!(killIns.definition instanceof KillIns)) {
                        throw new ConvertException("Unknown pattern: no kill before lookupswitch", switchPos - 1);
                     }
                     int userReg = killIns.operands[0];
                     int evalTo = -1;
                     for (int g = jumpPos; g < switchPos; g++) {
                        if ((code.get(g).definition instanceof SetLocal0Ins) && (userReg == 0)) {
                           evalTo = g;
                           break;
                        } else if ((code.get(g).definition instanceof SetLocal1Ins) && (userReg == 1)) {
                           evalTo = g;
                           break;
                        } else if ((code.get(g).definition instanceof SetLocal2Ins) && (userReg == 2)) {
                           evalTo = g;
                           break;
                        } else if ((code.get(g).definition instanceof SetLocal3Ins) && (userReg == 3)) {
                           evalTo = g;
                           break;
                        }
                        if ((code.get(g).definition instanceof SetLocalIns) && (userReg == code.get(g).operands[0])) {
                           evalTo = g;
                           break;
                        }
                     }
                     if (evalTo == -1) {
                        throw new ConvertException("Unknown pattern: no setlocal before lookupswitch", switchPos);
                     }
                     loopList.add(new Loop(ip, switchPos + 1));
                     Stack<TreeItem> substack = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, jumpPos, evalTo - 1, localRegNames).stack;
                     TreeItem switchedValue = substack.pop();
                     //output.add("loop" + (switchPos + 1) + ":");
                     int switchBreak = switchPos + 1;
                     List<TreeItem> casesList = new ArrayList<TreeItem>();
                     List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
                     List<TreeItem> defaultCommands = new ArrayList<TreeItem>();
                     //output.add("switch(" + switchedValue + ")");
                     //output.add("{");
                     int curPos = evalTo + 1;
                     int casePos = 0;
                     do {
                        evalTo = -1;
                        for (int g = curPos; g < switchPos; g++) {
                           if ((code.get(g).definition instanceof GetLocal0Ins) && (userReg == 0)) {
                              evalTo = g;
                              break;
                           } else if ((code.get(g).definition instanceof GetLocal1Ins) && (userReg == 1)) {
                              evalTo = g;
                              break;
                           } else if ((code.get(g).definition instanceof GetLocal2Ins) && (userReg == 2)) {
                              evalTo = g;
                              break;
                           } else if ((code.get(g).definition instanceof GetLocal3Ins) && (userReg == 3)) {
                              evalTo = g;
                              break;
                           }
                           if ((code.get(g).definition instanceof GetLocalIns) && (userReg == code.get(g).operands[0])) {
                              evalTo = g;
                              break;
                           }
                        }


                        if (evalTo > -1) {
                           substack = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, curPos, evalTo - 1, localRegNames).stack;
                           casesList.add(substack.pop());
                        }
                        int substart = adr2pos(code.get(switchPos).operands[2 + casePos] + pos2adr(switchPos));
                        int subend = jumpPos - 1;
                        if (casePos + 1 < code.get(switchPos).operands.length - 2) {
                           subend = adr2pos(code.get(switchPos).operands[2 + casePos + 1] + pos2adr(switchPos)) - 1;
                        }

                        if (evalTo == -1) {
                           subend--;
                        }
                        List commands = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, substart, subend, localRegNames).output;
                        if ((evalTo == -1) && (casePos + 1 < code.get(switchPos).operands.length - 2)) {
                           if (commands.size() == 1) {
                              commands.remove(0);
                           }
                           if (commands.size() > 0) {
                              //hasDefault=true;
                           }
                        }
                        List<TreeItem> caseCommandPart = new ArrayList<TreeItem>();
                        if (evalTo == -1) {
                           defaultCommands.addAll(commands);
                        } else {
                           caseCommandPart.addAll(commands);
                           caseCommands.add(caseCommandPart);
                        }
                        curPos = evalTo + 4;
                        casePos++;
                        if (evalTo == -1) {
                           break;
                        }
                     } while (true);
                     output.add(new SwitchTreeItem(code.get(switchPos), switchBreak, switchedValue, casesList, caseCommands, defaultCommands));
                     ip = switchPos + 1;
                     addr = pos2adr(ip);
                     continue;
                  }

                  if (!backJumpFound) {
                     if (jumpPos <= end + 1) { //probably skipping catch
                        ip = jumpPos;
                        addr = pos2adr(ip);
                        continue;
                     }
                     output.add(new ContinueTreeItem(ins, jumpPos, false));
                     addr = secondAddr;
                     ip = ip + 1;
                     if (!unknownJumps.contains(jumpPos)) {
                        unknownJumps.add(jumpPos);
                     }
                     continue;
                     //throw new ConvertException("Unknown pattern: forjump with no backjump");
                  }
                  Loop currentLoop = new Loop(jumpPos, adr2pos(afterBackJumpAddr));
                  loopList.add(currentLoop);


                  ConvertOutput co = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, jumpPos, adr2pos(afterBackJumpAddr) - 2, localRegNames);
                  Stack<TreeItem> substack = co.stack;
                  backJumpIns.definition.translate(isStatic, classIndex, localRegs, substack, scopeStack, constants, backJumpIns, method_info, output, body, abc, localRegNames);

                  TreeItem expression = substack.pop();
                  List<TreeItem> subins = new ArrayList<TreeItem>();
                  boolean isFor = false;
                  List<TreeItem> finalExpression = new ArrayList<TreeItem>();
                  try {
                     subins = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, adr2pos(secondAddr) + 1/*label*/, jumpPos - 1, localRegNames).output;
                  } catch (UnknownJumpException uje) {
                     if ((uje.ip >= start) && (uje.ip <= end)) {
                        currentLoop.loopContinue = uje.ip;
                        subins = uje.output;

                        List<ContinueTreeItem> contList = new ArrayList<ContinueTreeItem>();
                        for (TreeItem ti : subins) {
                           if (ti instanceof ContinueTreeItem) {
                              contList.add((ContinueTreeItem) ti);
                           }
                           if (ti instanceof Block) {
                              contList.addAll(((Block) ti).getContinues());
                           }
                        }
                        for (int u = 0; u < contList.size(); u++) {
                           if (contList.get(u) instanceof ContinueTreeItem) {
                              if (((ContinueTreeItem) contList.get(u)).loopPos == uje.ip) {
                                 if (!((ContinueTreeItem) contList.get(u)).isKnown) {
                                    ((ContinueTreeItem) contList.get(u)).isKnown = true;
                                    ((ContinueTreeItem) contList.get(u)).loopPos = currentLoop.loopBreak;
                                 }
                              }
                           }
                        }
                        finalExpression = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, uje.ip, jumpPos - 1, localRegNames).output;
                        isFor = true;
                     } else {
                        throw new ConvertException("Unknown pattern: jump to nowhere", ip);
                     }
                  }
                  boolean isDoWhile = false;

                  if (jumpPos == ip + 2) {
                     if (code.get(ip + 1).definition instanceof LabelIns) {
                        isDoWhile = true;
                     }
                  }
                  if (!isDoWhile) {
                     if (!isFor) {
                        for (Loop l : loopList) {
                           if (l.loopContinue == jumpPos) {
                              if (l.continueCount == 0) {
                                 //isFor = true;
                                 //finalExpression = subins.remove(subins.size() - 1).toString();
                              }
                              break;
                           }
                        }
                     }
                  }

                  String firstIns = "";
                  if (isFor) {
                     if (output.size() > 0) {
                        //firstIns = output.remove(output.size() - 1).toString();
                     }
                  }

                  List<TreeItem> loopBody = new ArrayList<TreeItem>();
                  loopBody.addAll(co.output);
                  loopBody.addAll(subins);

                  if (isFor) {
                     output.add(new ForTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, new ArrayList<TreeItem>(), expression, finalExpression, loopBody));
                  } else if (isDoWhile) {
                     output.add(new DoWhileTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, loopBody, expression));
                  } else {
                     if (expression instanceof InTreeItem) {
                        boolean found = false;
                        for (int g = ip + 1; g < jumpPos; g++) {
                           if (code.get(g).definition instanceof NextValueIns) {
                              output.add(new ForEachInTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, (InTreeItem) expression, loopBody));
                              found = true;
                              break;
                           }
                           if (code.get(g).definition instanceof NextNameIns) {
                              output.add(new ForInTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, (InTreeItem) expression, loopBody));
                              found = true;
                              break;
                           }
                        }
                        if (!found) {
                           throw new ConvertException("Unknown pattern: hasnext without nextvalue/nextname", ip);
                        }

                     } else {
                        output.add(new WhileTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, expression, loopBody));
                     }
                  }
                  addr = afterBackJumpAddr;
                  ip = adr2pos(addr);
               } else {
                  throw new ConvertException("Unknown pattern: back jump ", ip);
               }
            } else if (ins.definition instanceof DupIns) {
               int nextPos = 0;
               do {
                  AVM2Instruction insAfter = code.get(ip + 1);
                  AVM2Instruction insBefore = ins;
                  if (ip - 1 >= start) {
                     insBefore = code.get(ip - 1);
                  }
                  if (insAfter.definition instanceof ConvertBIns) { //SWF compiled with debug contain convert_b
                     ip++;
                     addr = pos2adr(ip);
                     insAfter = code.get(ip + 1);
                  }

                  boolean isAnd = false;
                  if (insAfter.definition instanceof IfFalseIns) {
                     //stack.add("(" + stack.pop() + ")&&");
                     isAnd = true;
                  } else if (insAfter.definition instanceof IfTrueIns) {
                     //stack.add("(" + stack.pop() + ")||");
                     isAnd = false;
                  } else if (insAfter.definition instanceof SetLocalTypeIns) {
                     //chained assignments
                     int reg = (((SetLocalTypeIns) insAfter.definition).getRegisterId(insAfter));
                     for (int t = ip + 1; t < end - 1; t++) {
                        if (code.get(t).definition instanceof GetLocalTypeIns) {
                           if (((GetLocalTypeIns) code.get(t).definition).getRegisterId(code.get(t)) == reg) {
                              if (code.get(t + 1).definition instanceof KillIns) {
                                 if (code.get(t + 1).operands[0] == reg) {
                                    ConvertOutput assignment = toSource(isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, ip + 2, t - 1, localRegNames);
                                    stack.push(assignment.output.remove(assignment.output.size() - 1));
                                    ip = t + 2;
                                    continue iploop;
                                 }
                              }
                           }
                        }
                     }
                     ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames);
                     ip++;
                     addr = pos2adr(ip);
                     break;
                     //}

                  } else {
                     ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames);
                     ip++;
                     addr = pos2adr(ip);
                     break;
                     //throw new ConvertException("Unknown pattern after DUP:" + insComparsion.toString());
                  }
                  addr = addr + ins.getBytes().length + insAfter.getBytes().length + insAfter.operands[0];
                  nextPos = adr2pos(addr) - 1;
                  if (isAnd) {
                     stack.add(new AndTreeItem(insAfter, stack.pop(), toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, ip + 3, nextPos, localRegNames).stack.pop()));
                  } else {
                     stack.add(new OrTreeItem(insAfter, stack.pop(), toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, ip + 3, nextPos, localRegNames).stack.pop()));
                  }
                  ins = code.get(nextPos + 1);
                  ip = nextPos + 1;
               } while (ins.definition instanceof DupIns);
            } else if (ins.definition instanceof IfTypeIns) {
               int targetAddr = pos2adr(ip) + ins.getBytes().length + ins.operands[0];
               int targetIns = adr2pos(targetAddr);
               ((IfTypeIns) ins.definition).translateInverted(localRegs, stack, ins);

               TreeItem condition = stack.pop();

               if (condition.isFalse()) {
                  //ins.definition = new JumpIns();
                  //continue;
               }
               if (condition.isTrue()) {
                  //ip = targetIns;
                  //continue;
               }
               //stack.add("if"+stack.pop());
               //stack.add("{");
               boolean hasElse = false;
               boolean hasReturn = false;
               if (code.get(targetIns - 1).definition instanceof JumpIns) {

                  if ((targetIns - 2 > ip) && ((code.get(targetIns - 2).definition instanceof ReturnValueIns) || (code.get(targetIns - 2).definition instanceof ReturnVoidIns) || (code.get(targetIns - 2).definition instanceof ThrowIns))) {
                     hasElse = false;
                     hasReturn = true;
                  } else {
                     int jumpAddr = targetAddr + code.get(targetIns - 1).operands[0];
                     int jumpPos = adr2pos(jumpAddr);
                     hasElse = true;

                     for (Loop l : loopList) {
                        if (l.loopBreak == jumpPos) {
                           hasElse = false;
                           break;
                        }
                     }
                     if (hasElse) {
                        if (adr2pos(jumpAddr) > end + 1) {
                           hasElse = false;
                           //throw new ConvertException("Unknown pattern: forward jump outside of the block");
                        }
                     }
                  }
               }
               ConvertOutput onTrue = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, ip + 1, targetIns - 1 - ((hasElse || hasReturn) ? 1 : 0), localRegNames);
               addr = targetAddr;
               ip = targetIns;
               ConvertOutput onFalse = new ConvertOutput(new Stack<TreeItem>(), new ArrayList<TreeItem>());
               if (hasElse) {
                  int finalAddr = targetAddr + code.get(targetIns - 1).operands[0];
                  int finalIns = adr2pos(finalAddr);
                  onFalse = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, targetIns, finalIns - 1, localRegNames);
                  addr = finalAddr;
                  ip = finalIns;
               }
               if ((onTrue.stack.size() > 0) && (onFalse != null) && (onFalse.stack.size() > 0)) {
                  stack.add(new TernarOpTreeItem(ins, condition, onTrue.stack.pop(), onFalse.stack.pop()));
               } else {
                  output.add(new IfTreeItem(ins, condition, onTrue.output, onFalse.output));
               }

            } else if ((ins.definition instanceof ReturnValueIns) || (ins.definition instanceof ReturnVoidIns) || (ins.definition instanceof ThrowIns)) {
               ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames);
               ip = end + 1;
               break;
            } else {
               ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames);

               addr += ins.getBytes().length;
               ip++;
            }

         }
         if (debugMode) {
            System.out.println("CLOSE SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
         }
         if(hideTemporaryRegisters)
         {
            clearTemporaryRegisters(output);
         }
         return new ConvertOutput(stack, output);
      } catch (ConvertException cex) {
         throw cex;
      } catch (Exception ex) {
         if (ex instanceof UnknownJumpException) {
            throw (UnknownJumpException) ex;
         }
         if(DEBUG_MODE)
         {
            ex.printStackTrace();
         }
         throw new ConvertException(ex.getClass().getSimpleName(), ip);
      }
   }

   public String tabString(int len) {
      String ret = "";
      for (int i = 0; i < len; i++) {
         ret += ABC.IDENT_STRING;
      }
      return ret;
   }

   public String toSource(boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, HashMap<Integer, String> localRegNames,Stack<TreeItem> scopeStack) {
      return toSource(isStatic, classIndex, abc, constants, method_info, body, false, localRegNames,scopeStack);
   }

   public List<TreeItem> toTree(boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, HashMap<Integer, String> localRegNames) {
      toSourceCount = 0;
      loopList = new ArrayList<Loop>();
      unknownJumps = new ArrayList<Integer>();
      parsedExceptions = new ArrayList<ABCException>();
      finallyJumps = new ArrayList<Integer>();
      HashMap<Integer, TreeItem> localRegs = new HashMap<Integer, TreeItem>();
      try {
         return toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), new Stack<TreeItem>(), abc, constants, method_info, body, 0, code.size() - 1, localRegNames).output;
      } catch (ConvertException ex) {
         return new ArrayList<TreeItem>();
      }
   }

   public int getRegisterCount() {
      int maxRegister = -1;
      for (AVM2Instruction ins : code) {
         int regId = -1;
         if (ins.definition instanceof SetLocalTypeIns) {
            regId = ((SetLocalTypeIns) ins.definition).getRegisterId(ins);
         }
         if (ins.definition instanceof GetLocalTypeIns) {
            regId = ((GetLocalTypeIns) ins.definition).getRegisterId(ins);
         }
         if (regId > maxRegister) {
            maxRegister = regId;
         }
      }
      return maxRegister + 1;
   }

   public HashMap<Integer, String> getLocalRegTypes(ConstantPool constants) {
      HashMap<Integer, String> ret = new HashMap<Integer, String>();
      AVM2Instruction prev = null;
      for (AVM2Instruction ins : code) {
         if (ins.definition instanceof SetLocalTypeIns) {
            if (prev != null) {
               if (prev.definition instanceof CoerceOrConvertTypeIns) {
                  ret.put(((SetLocalTypeIns) ins.definition).getRegisterId(ins), ((CoerceOrConvertTypeIns) prev.definition).getTargetType(constants, prev));
               }
            }
         }
         prev = ins;
      }
      return ret;
   }

   private class Slot{
      public TreeItem scope;
      public Multiname multiname;

      public Slot(TreeItem scope, Multiname multiname) {
         this.scope = scope;
         this.multiname = multiname;
      }

      @Override
      public boolean equals(Object obj) {
         if(obj instanceof Slot){
            return (((Slot)obj).scope.getThroughRegister()==scope.getThroughRegister())
                    &&(((Slot)obj).multiname==multiname);
         }
         return false;
      }            

      @Override
      public int hashCode() {
         int hash = 7;
         hash = 59 * hash + (this.scope != null ? this.scope.hashCode() : 0);
         hash = 59 * hash + (this.multiname != null ? this.multiname.hashCode() : 0);
         return hash;
      }
   }
   
   public String toSource(boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, boolean hilighted, HashMap<Integer, String> localRegNames,Stack<TreeItem> scopeStack) {
      toSourceCount = 0;
      loopList = new ArrayList<Loop>();
      unknownJumps = new ArrayList<Integer>();
      finallyJumps = new ArrayList<Integer>();
      parsedExceptions = new ArrayList<ABCException>();
      ignoredIns = new ArrayList<Integer>();
      List<TreeItem> list;
      String s = "";
      HashMap<Integer, TreeItem> localRegs = new HashMap<Integer, TreeItem>();
      
       int regCount = getRegisterCount();
      int paramCount = 0;
      if (body.method_info != -1) {
         MethodInfo mi = method_info[body.method_info];
         paramCount = mi.param_types.length;
         if (mi.flagNeed_rest()) {
            paramCount++;
         }
      }
      
      try {
         list = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, 0, code.size() - 1, localRegNames).output;
         
         //Declarations
         boolean declaredRegisters[]=new boolean[regCount];
         for(int b=0;b<declaredRegisters.length;b++){
            declaredRegisters[b]=false;
         }
         List<Slot> declaredSlots=new ArrayList<Slot>();
         for(int i=0;i<list.size();i++){
            TreeItem ti=list.get(i);
            if(ti instanceof SetLocalTreeItem){
               int reg=((SetLocalTreeItem)ti).regIndex;
               if(!declaredRegisters[reg]){
                  list.set(i, new DeclarationTreeItem(ti));
                  declaredRegisters[reg]=true;
               }
            }
            if(ti instanceof SetSlotTreeItem){
               SetSlotTreeItem ssti=(SetSlotTreeItem)ti;
               Slot sl=new Slot(ssti.scope,ssti.slotName);
               if(!declaredSlots.contains(sl)){
                  String type="*";
                  for (int t = 0; t < body.traits.traits.length; t++) {     
                     if(body.traits.traits[t].getMultiName(constants)==sl.multiname){
                        if(body.traits.traits[t] instanceof TraitSlotConst){
                           type=((TraitSlotConst)body.traits.traits[t]).getType(constants);
                        }
                     }                     
                  }
                  list.set(i, new DeclarationTreeItem(ti,type));
                  declaredSlots.add(sl);
               }
            }
         }
      
         s = listToString(list, constants, localRegNames);
      } catch (Exception ex) {
         if(DEBUG_MODE){
            ex.printStackTrace();
         }
         s = "/*\r\n * Decompilation error\r\n * Code may be obfuscated\r\n * Error Message: " + ex.getMessage() + "\r\n */";
         return s;
      }


      String sub = "";
      int level = 0;           
      
      String parts[] = s.split("\r\n");
      
      
      try {
         Stack<String> loopStack = new Stack<String>();
         for (int p = 0; p < parts.length; p++) {
            String stripped = Highlighting.stripHilights(parts[p]);
            if (stripped.endsWith(":") && (!stripped.startsWith("case ")) && (!stripped.equals("default:"))) {
               loopStack.add(stripped.substring(0, stripped.length() - 1));
            }
            if (stripped.startsWith("break ")) {
               if (stripped.equals("break " + loopStack.peek() + ";")) {
                  parts[p] = parts[p].replace(" " + loopStack.peek(), "");
               }
            }
            if (stripped.startsWith("continue ")) {
               if (loopStack.size() > 0) {
                  if (stripped.equals("continue " + loopStack.peek() + ";")) {
                     parts[p] = parts[p].replace(" " + loopStack.peek(), "");
                  }
               }
            }
            if (stripped.startsWith(":")) {
               loopStack.pop();
            }
         }
      } catch (Exception ex) {
      }
      for (int p = 0; p < parts.length; p++) {
         if (p == parts.length - 1) {
            if (parts[p].equals("")) {
               continue;
            }
         }
         String strippedP = Highlighting.stripHilights(parts[p]);
         if (strippedP.endsWith(":") && (!strippedP.startsWith("case ")) && (!strippedP.equals("default:"))) {
            String loopname = strippedP.substring(0, strippedP.length() - 1);
            boolean dorefer = false;
            for (int q = p + 1; q < parts.length; q++) {
               String strippedQ = Highlighting.stripHilights(parts[q]);
               if (strippedQ.equals("break " + loopname + ";")) {
                  dorefer = true;
                  break;
               }
               if (strippedQ.equals("continue " + loopname + ";")) {
                  dorefer = true;
                  break;
               }
               if (strippedQ.equals(":" + loopname)) {
                  break;
               }
            }
            if (!dorefer) {
               continue;
            }
         }
         if (strippedP.startsWith(":")) {
            continue;
         }
         if (strippedP.equals(IDENTOPEN)) {
            level++;
         } else if (strippedP.equals(IDENTCLOSE)) {
            level--;
         } else if (strippedP.equals("{")) {
            level++;
            sub += tabString(level) + parts[p] + "\r\n";
            level++;
         } else if (strippedP.equals("}") || strippedP.equals("};")) {
            level--;
            sub += tabString(level) + parts[p] + "\r\n";
            level--;
         } else {
            sub += tabString(level) + parts[p] + "\r\n";
         }
      }
      if (!hilighted) {
         sub = Highlighting.stripHilights(sub);
      }
      return sub;
   }

   public static void main(String[] args) {
      FileInputStream fis = null;
      try {
         fis = new FileInputStream("src/asdec/abc/avm2/AVM2Code.java");
         byte[] data = new byte[fis.available()];
         fis.read(data);

         String content = new String(data);
         Pattern partPat = Pattern.compile("private static InstructionDefinition instructionSet(.*)endoflist", Pattern.MULTILINE | Pattern.DOTALL);
         Matcher m = partPat.matcher(content);
         if (m.find()) {
            System.out.println("1 found");
            content = m.group(1);
            System.out.println(content);
            Pattern part2Pat = Pattern.compile("new InstructionDefinition(\\([^\\)]*\"([^\"]*)\"[^\\)]*\\))\\{(.*)\\},", Pattern.MULTILINE | Pattern.DOTALL);
            m = part2Pat.matcher(content);
            while (m.find()) {
               System.out.println("2 found");
               String superCall = m.group(1);
               String name = m.group(2);
               String methods = m.group(3);
               FileOutputStream fos = new FileOutputStream("src/asdec/abc/avm2/instructions/generated/" + name + "Ins.java");
               String out = "public class " + name + "Ins extends InstructionDefinition {\r\n public " + name + "Ins(){\r\nsuper" + superCall + ";\r\n}" + methods + "}";
               fos.write(out.getBytes());
               fos.close();
            }
         }
      } catch (IOException ex) {
      } finally {
         try {
            fis.close();
         } catch (IOException ex) {
            Logger.getLogger(AVM2Code.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   public void removeInstruction(int pos, MethodBody body) {
      if ((pos < 0) || (pos >= code.size())) {
         throw new IndexOutOfBoundsException();
      }
      int byteCount = code.get(pos).getBytes().length;
      long remOffset = code.get(pos).offset;
      for (int i = pos + 1; i < code.size(); i++) {
         code.get(i).offset -= byteCount;
      }

      for (ABCException ex : body.exceptions) {
         if (ex.start > remOffset) {
            ex.start -= byteCount;
         }
         if (ex.end > remOffset) {
            ex.end -= byteCount;
         }
         if (ex.target > remOffset) {
            ex.target -= byteCount;
         }
      }


      for (int i = 0; i < pos; i++) {
         if (code.get(i).definition instanceof LookupSwitchIns) {
            long target = code.get(i).offset + code.get(i).operands[0];
            if (target > remOffset) {
               code.get(i).operands[0] -= byteCount;
            }
            for (int k = 2; k < code.get(i).operands.length; k++) {
               target = code.get(i).offset + code.get(i).operands[k];
               if (target > remOffset) {
                  code.get(i).operands[k] -= byteCount;
               }
            }
         } else {
            for (int j = 0; j < code.get(i).definition.operands.length; j++) {
               if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
                  long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
                  if (target > remOffset) {
                     code.get(i).operands[j] -= byteCount;
                  }
               }
            }
         }
      }
      for (int i = pos + 1; i < code.size(); i++) {
         if (code.get(i).definition instanceof LookupSwitchIns) {
            long target = code.get(i).offset + code.get(i).operands[0];
            if (target < remOffset) {
               code.get(i).operands[0] += byteCount;
            }
            for (int k = 2; k < code.get(i).operands.length; k++) {
               target = code.get(i).offset + code.get(i).operands[k];
               if (target < remOffset) {
                  code.get(i).operands[k] += byteCount;
               }
            }
         } else {
            for (int j = 0; j < code.get(i).definition.operands.length; j++) {
               if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
                  long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
                  if (target < remOffset) {
                     code.get(i).operands[j] += byteCount;
                  }
               }
            }
         }
      }

      code.remove(pos);

   }

   public void insertInstruction(int pos, AVM2Instruction instruction) {
      if (pos < 0) {
         pos = 0;
      }
      if (pos > code.size()) {
         pos = code.size();
      }
      int byteCount = instruction.getBytes().length;
      if (pos == code.size()) {
         instruction.offset = code.get(pos - 1).offset + code.get(pos - 1).getBytes().length;
      } else {
         instruction.offset = code.get(pos).offset;
      }

      for (int i = 0; i < pos; i++) {
         for (int j = 0; j < code.get(i).definition.operands.length; j++) {
            if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
               long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
               if (target >= instruction.offset) {
                  code.get(i).operands[j] += byteCount;
               }
            }
         }
      }
      for (int i = pos; i < code.size(); i++) {
         for (int j = 0; j < code.get(i).definition.operands.length; j++) {
            if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
               long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
               if (target < instruction.offset) {
                  code.get(i).operands[j] -= byteCount;
               }
            }
         }
      }

      for (int i = pos + 1; i < code.size(); i++) {
         code.get(i).offset += byteCount;
      }
      code.add(pos, instruction);
   }

   private void removeFreeBlocks(ConstantPool constants, MethodBody body) throws ConvertException {
      List<Long> offsets = new ArrayList<Long>();
      for (AVM2Instruction ins : code) {
         offsets.addAll(ins.getOffsets());
      }
      for (ABCException ex : body.exceptions) {
         offsets.add((long) ex.start);
         offsets.add((long) ex.end);
         offsets.add((long) ex.target);
      }

      int clearedCount = 0;
      loopip:
      for (int ip = 0; ip < code.size(); ip++) {
         AVM2Instruction ins = code.get(ip);
         if (ins.definition instanceof JumpIns) {
            int secondAddr = pos2adr(ip + 1);
            int jumpAddr = secondAddr + ins.operands[0];
            int jumpPos = adr2pos(jumpAddr);
            if (jumpPos <= ip) {
               continue;
            }
            if (jumpPos > code.size()) {
               continue;
            }
            for (int k = ip + 1; k < jumpPos; k++) {
               if (offsets.contains((long) pos2adr(k))) {
                  continue loopip;
               }
            }
            for (int k = ip; k < jumpPos; k++) {
               removeInstruction(ip, body);
               clearedCount++;
            }
            offsets.clear();
            for (AVM2Instruction ins2 : code) {
               offsets.addAll(ins2.getOffsets());
            }
            for (ABCException ex : body.exceptions) {
               offsets.add((long) ex.start);
               offsets.add((long) ex.end);
               offsets.add((long) ex.target);
            }
            ip--;
            //ip=jumpPos;
         }
      }
      if (clearedCount > 0) {
         //System.out.println("Cleared " + clearedCount + " lines of code TO:");
         //System.out.println(toASMSource(constants));
         //System.exit(1);
      }
   }

   public void clearSecureSWF(ConstantPool constants, MethodBody body) throws ConvertException {
      if (code.size() > 4) {
         AVM2Instruction first = code.get(0);
         AVM2Instruction second = code.get(1);
         boolean firstValue = false;
         boolean secondValue = false;
         boolean isSecure = true;
         if (first.definition instanceof PushFalseIns) {
            firstValue = false;
         } else if (first.definition instanceof PushTrueIns) {
            firstValue = true;
         } else {
            isSecure = false;
         }
         if (isSecure) {
            if (second.definition instanceof PushFalseIns) {
               secondValue = false;
            } else if (second.definition instanceof PushTrueIns) {
               secondValue = true;
            } else {
               isSecure = false;
            }
            if (isSecure) {
               AVM2Instruction third = code.get(2);
               int pos = 2;
               if (third.definition instanceof SwapIns) {
                  pos++;
                  boolean dup = firstValue;
                  firstValue = secondValue;
                  secondValue = dup;
               }
               AVM2Instruction firstSet = code.get(pos);
               AVM2Instruction secondSet = code.get(pos + 1);
               int trueIndex = -1;
               int falseIndex = -1;
               if (firstSet.definition instanceof SetLocalTypeIns) {
                  if (secondValue == true) {
                     trueIndex = ((SetLocalTypeIns) firstSet.definition).getRegisterId(firstSet);
                  }
                  if (secondValue == false) {
                     falseIndex = ((SetLocalTypeIns) firstSet.definition).getRegisterId(firstSet);
                  }
               } else {
                  isSecure = false;
               }
               if (isSecure) {
                  if (secondSet.definition instanceof SetLocalTypeIns) {
                     if (firstValue == true) {
                        trueIndex = ((SetLocalTypeIns) secondSet.definition).getRegisterId(firstSet);
                     }
                     if (firstValue == false) {
                        falseIndex = ((SetLocalTypeIns) secondSet.definition).getRegisterId(firstSet);
                     }

                     //Yes, secure
                     pos += 2;
                     for (int i = 0; i < pos; i++) {
                        code.get(i).ignored = true;
                        //removeInstruction(0, body);
                     }
                     System.out.println("trueIndex:" + trueIndex);
                     System.out.println("falseIndex:" + falseIndex);
                     boolean found = false;
                     do {
                        found = false;
                        for (int ip = pos; ip < code.size(); ip++) {
                           if (code.get(ip).ignored) {
                              continue;
                           }
                           if (code.get(ip).definition instanceof GetLocalTypeIns) {
                              int regIndex = ((GetLocalTypeIns) code.get(ip).definition).getRegisterId(code.get(ip));
                              if ((regIndex == trueIndex) || (regIndex == falseIndex)) {
                                 found = true;
                                 Stack<Boolean> myStack = new Stack<Boolean>();
                                 do {
                                    AVM2Instruction ins = code.get(ip);
                                    if (ins.ignored) {
                                       ip++;
                                       continue;
                                    } else if (ins.definition instanceof GetLocalTypeIns) {
                                       regIndex = ((GetLocalTypeIns) ins.definition).getRegisterId(ins);
                                       if (regIndex == trueIndex) {
                                          myStack.push(true);
                                       }
                                       if (regIndex == falseIndex) {
                                          myStack.push(false);
                                       }
                                       ip++;
                                       ins.ignored = true;
                                    } else if (ins.definition instanceof DupIns) {
                                       Boolean b = myStack.pop();
                                       myStack.push(b);
                                       myStack.push(b);
                                       ins.ignored = true;
                                       ip++;
                                    } else if (ins.definition instanceof PopIns) {
                                       myStack.pop();
                                       ins.ignored = true;
                                       ip++;
                                    } else if (ins.definition instanceof IfTrueIns) {
                                       System.out.println("iftrue found");
                                       boolean val = myStack.pop();
                                       if (val) {
                                          code.get(ip).definition = new JumpIns();
                                          System.out.println("changed to jump");
                                          ip = adr2pos(pos2adr(ip + 1) + code.get(ip).operands[0]);
                                       } else {
                                          code.get(ip).ignored = true;
                                          ip++;
                                       }
                                    } else if (ins.definition instanceof IfFalseIns) {
                                       boolean val = myStack.pop();
                                       if (!val) {
                                          code.get(ip).definition = new JumpIns();
                                          ip = adr2pos(pos2adr(ip + 1) + code.get(ip).operands[0]);
                                       } else {
                                          code.get(ip).ignored = true;
                                          ip++;
                                       }
                                    } else if (ins.definition instanceof JumpIns) {
                                       ip = adr2pos(pos2adr(ip + 1) + code.get(ip).operands[0]);
                                    } else {
                                    }

                                 } while (myStack.size() > 0);

                                 /*for(int rem=code.size();rem>=0;rem--){
                                  if(code.get(rem).ignored){
                                  code.remove(rem);
                                  }
                                  } */
                                 break;
                              }

                           }
                        }
                     } while (found);
                  } else {
                     isSecure = false;
                  }
               }

            }
         }
      }
   }

   public void clearCode(ConstantPool constants, MethodBody body) throws ConvertException {

      if (code.size() > 3) {
         if (code.get(0).definition instanceof PushByteIns) {
            if (code.get(1).definition instanceof PushByteIns) {
               if (code.get(2).definition instanceof IfNeIns) {
                  if (code.get(0).operands[0] != code.get(1).operands[0]) {
                     int targetAddr = pos2adr(2) + code.get(2).getBytes().length + code.get(2).operands[0];
                     int targetPos = adr2pos(targetAddr);
                     for (int i = 0; i < targetPos; i++) {
                        removeInstruction(0, body);
                     }
                  }
               }
            }
         }
      }

      removeFreeBlocks(constants, body);
      //clearSecureSWF(constants, body);


   }

   private void handleRegister(CodeStats stats, int reg) {
      if (reg + 1 > stats.maxlocal) {
         stats.maxlocal = reg + 1;
      }
   }

   private boolean walkCode(CodeStats stats, int pos, int stack, int scope, ABC abc) {
      while (pos < code.size()) {
         if (stats.instructionStats[pos].seen) {
            //check stack mismatch here
            return true;
         }
         stats.instructionStats[pos].seen = true;
         stats.instructionStats[pos].stackpos = stack;
         stats.instructionStats[pos].scopepos = scope;
         AVM2Instruction ins = code.get(pos);
         stack += ins.definition.getStackDelta(ins, abc);
         scope += ins.definition.getScopeStackDelta(ins, abc);
         if (stack > stats.maxstack) {
            stats.maxstack = stack;
         }
         if (scope > stats.maxscope) {
            stats.maxscope = scope;
         }
         if ((ins.definition instanceof DXNSIns) || (ins.definition instanceof DXNSLateIns)) {
            stats.has_set_dxns = true;
         }
         if (ins.definition instanceof NewActivationIns) {
            stats.has_activation = true;
         }
         if (ins.definition instanceof SetLocalTypeIns) {
            handleRegister(stats, ((SetLocalTypeIns) ins.definition).getRegisterId(ins));
         } else {
            for (int i = 0; i < ins.definition.operands.length; i++) {
               if (ins.definition.operands[i] == DAT_REGISTER_INDEX) {
                  handleRegister(stats, ins.operands[i]);
               }
            }
         }
         if (ins.definition instanceof ReturnValueIns) {
            //check stack=1
            return true;
         }
         if (ins.definition instanceof ReturnVoidIns) {
            //check stack=0
            return true;
         }
         if (ins.definition instanceof JumpIns) {
            try {
               pos = adr2pos(pos2adr(pos) + ins.getBytes().length + ins.operands[0]);
               continue;
            } catch (ConvertException ex) {
               return false;
            }
         } else if (ins.definition instanceof IfTypeIns) {
            try {
               int newpos = adr2pos(pos2adr(pos) + ins.getBytes().length + ins.operands[0]);
               walkCode(stats, newpos, stack, scope, abc);
            } catch (ConvertException ex) {
               return false;
            }
         }
         if (ins.definition instanceof LookupSwitchIns) {
            for (int i = 0; i < ins.operands.length; i++) {
               try {
                  int newpos = adr2pos(pos2adr(pos) + ins.operands[i]);
                  if (!walkCode(stats, newpos, stack, scope, abc)) {
                     return false;
                  }
               } catch (ConvertException ex) {
                  return false;
               }
            }
         }
         pos++;
      }
      return true;
   }

   public CodeStats getStats(ABC abc, MethodBody body) {
      CodeStats stats = new CodeStats(this);
      if (!walkCode(stats, 0, 0, 0, abc)) {
         return null;
      }
      for (ABCException ex : body.exceptions) {
         try {
            int exStart = adr2pos(ex.start);
            if (!walkCode(stats, adr2pos(ex.target), stats.instructionStats[exStart].stackpos, stats.instructionStats[exStart].scopepos, abc)) {
               return null;
            }
         } catch (ConvertException ex1) {
         }
      }
      return stats;
   }
}
