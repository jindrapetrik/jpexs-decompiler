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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparsion.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugFileIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.*;
import com.jpexs.decompiler.flash.abc.avm2.instructions.xml.*;
import com.jpexs.decompiler.flash.abc.avm2.parser.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.*;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.*;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.graph.Graph;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVM2Code implements Serializable {

   private static final boolean DEBUG_MODE = false;
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
   public boolean hideTemporaryRegisters = true;

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
         s += Helper.formatAddress(i) + " " + instruction.toString(constants, new ArrayList<String>()) + "\r\n";
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
      return toASMSource(constants, body, new ArrayList<Integer>());
   }

   public String toASMSource(ConstantPool constants, MethodBody body, List<Integer> outputMap) {
      invalidateCache();
      StringBuffer ret = new StringBuffer();
      String t = "";
      for (int e = 0; e < body.exceptions.length; e++) {
         ret.append("exception " + e + " m[" + body.exceptions[e].name_index + "]\"" + Helper.escapeString(body.exceptions[e].getVarName(constants, new ArrayList<String>())) + "\" "
                 + "m[" + body.exceptions[e].type_index + "]\"" + Helper.escapeString(body.exceptions[e].getTypeName(constants, new ArrayList<String>())) + "\"\n");
      }
      List<Long> offsets = new ArrayList<Long>();
      for (AVM2Instruction ins : code) {
         offsets.addAll(ins.getOffsets());
      }
      for (AVM2Instruction ins : code) {
         if (ins.replaceWith != null) {
            for (Object o : ins.replaceWith) {
               if (o instanceof ControlFlowTag) {
                  ControlFlowTag cft = (ControlFlowTag) o;
                  if (cft.name.equals("appendjump")) {
                     offsets.add((long) pos2adr(cft.value));
                  }
               }
            }
         }
      }
      long ofs = 0;
      int ip = 0;
      int largeLimit = 20000;
      boolean markOffsets = code.size() <= largeLimit;
      for (AVM2Instruction ins : code) {
         if (ins.labelname != null) {
            ret.append(ins.labelname + ":");
         } else if (offsets.contains(ofs)) {
            ret.append("ofs" + Helper.formatAddress(ofs) + ":");
         }
         for (int e = 0; e < body.exceptions.length; e++) {
            if (body.exceptions[e].start == ofs) {
               ret.append("exceptionstart " + e + ":");
            }
            if (body.exceptions[e].end == ofs) {
               ret.append("exceptionend " + e + ":");
            }
            if (body.exceptions[e].target == ofs) {
               ret.append("exceptiontarget " + e + ":");
            }
         }
         if (ins.replaceWith != null) {
            for (Object o : ins.replaceWith) {
               if (o instanceof Integer) {
                  AVM2Instruction ins2 = code.get((Integer) o);
                  if (ins2.isIgnored()) {
                     continue;
                  }
                  t = Highlighting.hilighOffset("", ins2.mappedOffset > -1 ? ins2.mappedOffset : ofs) + ins2.toStringNoAddress(constants, new ArrayList<String>()) + " ;copy from " + Helper.formatAddress(pos2adr((Integer) o)) + "\n";
                  ret.append(t);
                  outputMap.add((Integer) o);
               } else if (o instanceof ControlFlowTag) {
                  ControlFlowTag cft = (ControlFlowTag) o;
                  if (cft.name.equals("appendjump")) {
                     t = "jump ofs" + Helper.formatAddress(pos2adr(cft.value)) + "\n";
                     ret.append(t);
                     outputMap.add(-1);
                  }
                  if (cft.name.equals("mark")) {
                     ret.append("ofs" + Helper.formatAddress(pos2adr(cft.value)) + ":");
                  }
               }
            }
         } else {
            if (!ins.isIgnored()) {
               t = ins.toStringNoAddress(constants, new ArrayList<String>());
               if (ins.changeJumpTo > -1) {
                  t = ins.definition.instructionName + " ofs" + Helper.formatAddress(pos2adr(ins.changeJumpTo));
               }
               if (markOffsets) {
                  t = Highlighting.hilighOffset("", ins.mappedOffset > -1 ? ins.mappedOffset : ofs) + t + "\n";
               } else {
                  t = t + "\n";
               }
               ret.append(t);
               outputMap.add(ip);
            }
         }
         ofs += ins.getBytes().length;
         ip++;
      }
      String r = ret.toString();
      return r;
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
         throw new ConvertException("Bad jump try conver ofs" + Helper.formatAddress(address) + " ", -1);
      }
      return ret;
   }

   public int pos2adr(int pos) {
      if (!cacheActual) {
         buildCache();
      }
      return posCache.get(pos).intValue();
   }

   public void invalidateCache() {
      cacheActual = false;
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

   public List<GraphTargetItem> clearTemporaryRegisters(List<GraphTargetItem> output) {
      for (int i = 0; i < output.size(); i++) {
         if (output.get(i) instanceof SetLocalTreeItem) {
            if (isKilled(((SetLocalTreeItem) output.get(i)).regIndex, 0, code.size() - 1)) {
               output.remove(i);
               i--;
            }
         } else if (output.get(i) instanceof WithTreeItem) {
            clearTemporaryRegisters(((WithTreeItem) output.get(i)).items);
         }
      }
      return output;
   }

   public int fixIPAfterDebugLine(int ip) {
      if (ip >= code.size()) {
         return code.size() - 1;
      }
      if (code.get(ip).definition instanceof DebugLineIns) {
         return ip + 1;
      }
      return ip;
   }

   public int fixAddrAfterDebugLine(int addr) throws ConvertException {
      return pos2adr(fixIPAfterDebugLine(adr2pos(addr)));
   }

   public ConvertOutput toSourceOutput(boolean processJumps, boolean isStatic, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, Stack<GraphTargetItem> scopeStack, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, int start, int end, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, boolean visited[]) throws ConvertException {
      boolean debugMode = DEBUG_MODE;
      if (debugMode) {
         System.out.println("OPEN SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
      }
      if (visited == null) {
         visited = new boolean[code.size()];
      }
      //if(true) return "";
      toSourceCount++;
      if (toSourceLimit > 0) {
         if (toSourceCount > toSourceLimit) {
            throw new ConvertException("Limit of subs(" + toSourceLimit + ") was reached", start);
         }
      }
      List<GraphTargetItem> output = new ArrayList<GraphTargetItem>();
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
            boolean processTry = processJumps;
            addr = pos2adr(ip);
            int ipfix = fixIPAfterDebugLine(ip);
            int addrfix = pos2adr(ipfix);
            int maxend = -1;
            if (processTry) {
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


                  List<List<GraphTargetItem>> catchedCommands = new ArrayList<List<GraphTargetItem>>();
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


                     List<GraphTargetItem> finallyCommands = new ArrayList<GraphTargetItem>();
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
                                                   finallyCommands = toSourceOutput(processJumps, isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, finStart, f - 1, localRegNames, fullyQualifiedNames, visited).output;
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
                        int eendpos;
                        if (e < catchedExceptions.size() - 1) {
                           eendpos = adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(e + 1).target)) - 2;
                        } else {
                           eendpos = afterCatchPos - 1;
                        }
                        Stack<GraphTargetItem> substack = new Stack<GraphTargetItem>();
                        substack.add(new ExceptionTreeItem(catchedExceptions.get(e)));
                        catchedCommands.add(toSourceOutput(processJumps, isStatic, classIndex, localRegs, substack, new Stack<GraphTargetItem>(), abc, constants, method_info, body, adr2pos(fixAddrAfterDebugLine(catchedExceptions.get(e).target)), eendpos, localRegNames, fullyQualifiedNames, visited).output);
                     }

                     List<GraphTargetItem> tryCommands = toSourceOutput(processJumps, isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, ip, endpos - 1, localRegNames, fullyQualifiedNames, visited).output;


                     output.add(new TryTreeItem(tryCommands, catchedExceptions, catchedCommands, finallyCommands));
                     ip = returnPos;
                     addr = pos2adr(ip);
                  }

               }
            }

            if (ip > end) {
               break;
            }

            if (unknownJumps.contains(ip)) {
               unknownJumps.remove(new Integer(ip));
               throw new UnknownJumpException(stack, ip, output);
            }
            //System.out.println("ip"+ip+" ofs"+Helper.formatAddress(pos2adr(ip)));
            if (ip == 21) {
               //System.out.println("hh");
            }
            if (visited[ip]) {
               Logger.getLogger(AVM2Code.class.getName()).warning("Code already visited, ofs:" + Helper.formatAddress(pos2adr(ip)) + ", ip:" + ip);
               break;
            }
            visited[ip] = true;
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

            /*if ((ip + 2 < code.size()) && (ins.definition instanceof NewCatchIns)) { //Filling local register in catch clause
             if (code.get(ip + 1).definition instanceof DupIns) {
             if (code.get(ip + 2).definition instanceof SetLocalTypeIns) {
             ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
             ip += 3;
             continue;
             }
             }
             }*/

            if ((ins.definition instanceof SetLocalTypeIns) && (ip + 1 <= end) && (isKilled(((SetLocalTypeIns) ins.definition).getRegisterId(ins), ip, end))) { //set_local_x,get_local_x..kill x

               AVM2Instruction insAfter = code.get(ip + 1);
               if ((insAfter.definition instanceof GetLocalTypeIns) && (((GetLocalTypeIns) insAfter.definition).getRegisterId(insAfter) == ((SetLocalTypeIns) ins.definition).getRegisterId(ins))) {
                  GraphTargetItem before = stack.peek();
                  ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
                  stack.push(before);
                  ip += 2;
                  continue iploop;
               } else {
                  ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
                  ip++;
                  continue iploop;
               }
            } else if (ins.definition instanceof DupIns) {
               int nextPos;
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

                  boolean isAnd;
                  if (processJumps && (insAfter.definition instanceof IfFalseIns)) {
                     //stack.add("(" + stack.pop() + ")&&");
                     isAnd = true;
                  } else if (processJumps && (insAfter.definition instanceof IfTrueIns)) {
                     //stack.add("(" + stack.pop() + ")||");
                     isAnd = false;
                  } else if (insAfter.definition instanceof SetLocalTypeIns) {
                     //chained assignments
                     int reg = (((SetLocalTypeIns) insAfter.definition).getRegisterId(insAfter));
                     for (int t = ip + 1; t <= end - 1; t++) {
                        if (code.get(t).definition instanceof KillIns) {
                           if (code.get(t).operands[0] == reg) {
                              break;
                           }
                        }
                        if (code.get(t).definition instanceof GetLocalTypeIns) {
                           if (((GetLocalTypeIns) code.get(t).definition).getRegisterId(code.get(t)) == reg) {
                              if (code.get(t + 1).definition instanceof KillIns) {
                                 if (code.get(t + 1).operands[0] == reg) {
                                    ConvertOutput assignment = toSourceOutput(processJumps, isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, ip + 2, t - 1, localRegNames, fullyQualifiedNames, visited);
                                    stack.push(assignment.output.remove(assignment.output.size() - 1));
                                    ip = t + 2;
                                    continue iploop;
                                 }
                              }
                           }
                        }
                     }
                     if (!isKilled(reg, 0, end)) {
                        for (int i = ip; i >= start; i--) {
                           if (code.get(i).definition instanceof DupIns) {
                              TreeItem v = (TreeItem) stack.pop();
                              stack.push(new LocalRegTreeItem(ins, reg, v));
                              stack.push(v);
                           } else {
                              break;
                           }
                        }
                     } else {
                        ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
                     }
                     ip++;
                     break;
                     //}

                  } else {
                     ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
                     ip++;
                     break;
                     //throw new ConvertException("Unknown pattern after DUP:" + insComparsion.toString());
                  }
               } while (ins.definition instanceof DupIns);
            } else if ((ins.definition instanceof ReturnValueIns) || (ins.definition instanceof ReturnVoidIns) || (ins.definition instanceof ThrowIns)) {
               ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
               ip = end + 1;
               break;
            } else if (ins.definition instanceof NewFunctionIns) {
               String functionName = "";
               if ((ip >= start + 2) && (ip <= end - 4)) {
                  AVM2Instruction prev2 = code.get(ip - 2);
                  if (prev2.definition instanceof NewObjectIns) {
                     if (prev2.operands[0] == 0) {
                        if (code.get(ip - 1).definition instanceof PushWithIns) {
                           boolean hasDup = false;
                           int plus = 0;
                           if (code.get(ip + 1).definition instanceof DupIns) {
                              hasDup = true;
                              plus = 1;
                           }
                           AVM2Instruction psco = code.get(ip + 1 + plus);
                           if (psco.definition instanceof GetScopeObjectIns) {
                              if (psco.operands[0] == scopeStack.size() - 1) {
                                 if (code.get(ip + plus + 2).definition instanceof SwapIns) {
                                    if (code.get(ip + plus + 4).definition instanceof PopScopeIns) {
                                       if (code.get(ip + plus + 3).definition instanceof SetPropertyIns) {
                                          functionName = abc.constants.constant_multiname[code.get(ip + plus + 3).operands[0]].getName(constants, fullyQualifiedNames);
                                          scopeStack.pop();//with
                                          output.remove(output.size() - 1); //with
                                          ip = ip + plus + 4; //+1 below
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
               //What to do when hasDup is false?
               ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
               NewFunctionTreeItem nft = (NewFunctionTreeItem) stack.peek();
               nft.functionName = functionName;
               ip++;
            } else {
               ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);

               ip++;
               addr = pos2adr(ip);
            }

         }
         if (debugMode) {
            System.out.println("CLOSE SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
         }
         /*if (hideTemporaryRegisters) {
          clearTemporaryRegisters(output);
          }*/
         return new ConvertOutput(stack, output);
      } catch (ConvertException cex) {
         throw cex;
      }
   }

   public String tabString(int len) {
      String ret = "";
      for (int i = 0; i < len; i++) {
         ret += ABC.IDENT_STRING;
      }
      return ret;
   }

   public String toSource(String path, boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, HashMap<Integer, String> localRegNames, Stack<GraphTargetItem> scopeStack, boolean isStaticInitializer, List<String> fullyQualifiedNames, Traits initTraits) {
      return toSource(path, isStatic, classIndex, abc, constants, method_info, body, false, localRegNames, scopeStack, isStaticInitializer, fullyQualifiedNames, initTraits);
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

   public HashMap<Integer, String> getLocalRegTypes(ConstantPool constants, List<String> fullyQualifiedNames) {
      HashMap<Integer, String> ret = new HashMap<Integer, String>();
      AVM2Instruction prev = null;
      for (AVM2Instruction ins : code) {
         if (ins.definition instanceof SetLocalTypeIns) {
            if (prev != null) {
               if (prev.definition instanceof CoerceOrConvertTypeIns) {
                  ret.put(((SetLocalTypeIns) ins.definition).getRegisterId(ins), ((CoerceOrConvertTypeIns) prev.definition).getTargetType(constants, prev, fullyQualifiedNames));
               }
            }
         }
         prev = ins;
      }
      return ret;
   }

   private class Slot {

      public GraphTargetItem scope;
      public Multiname multiname;

      public Slot(GraphTargetItem scope, Multiname multiname) {
         this.scope = scope;
         this.multiname = multiname;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof Slot) {
            return (((Slot) obj).scope.getThroughRegister() == scope.getThroughRegister())
                    && (((Slot) obj).multiname == multiname);
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

   public void initToSource() {
      toSourceCount = 0;
      loopList = new ArrayList<Loop>();
      unknownJumps = new ArrayList<Integer>();
      finallyJumps = new ArrayList<Integer>();
      parsedExceptions = new ArrayList<ABCException>();
      ignoredIns = new ArrayList<Integer>();
   }

   public String toSource(String path, boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, boolean hilighted, HashMap<Integer, String> localRegNames, Stack<GraphTargetItem> scopeStack, boolean isStaticInitializer, List<String> fullyQualifiedNames, Traits initTraits) {
      initToSource();
      List<GraphTargetItem> list;
      String s;
      HashMap<Integer, GraphTargetItem> localRegs = new HashMap<Integer, GraphTargetItem>();

      int regCount = getRegisterCount();
      int paramCount;
      if (body.method_info != -1) {
         MethodInfo mi = method_info[body.method_info];
         paramCount = mi.param_types.length;
         if (mi.flagNeed_rest()) {
            paramCount++;
         }
      }

      //try {

      try {
         list = AVM2Graph.translateViaGraph(path, this, abc, body, isStatic, classIndex, localRegs, scopeStack, localRegNames, fullyQualifiedNames);
      } catch (Exception ex2) {
         Logger.getLogger(AVM2Code.class.getName()).log(Level.SEVERE, "Decompilation error in " + path, ex2);
         return "/*\r\n * Decompilation error\r\n * Code may be obfuscated\r\n * Error Message: " + ex2.getMessage() + "\r\n */";
      }
      /*try{
       list=toSourceOutput(true,isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, 0, code.size() - 1, localRegNames, fullyQualifiedNames, null).output;
       }catch(Exception ex){
            
       }*/
      if (initTraits != null) {
         for (int i = 0; i < list.size(); i++) {
            GraphTargetItem ti = list.get(i);
            if ((ti instanceof InitPropertyTreeItem) || (ti instanceof SetPropertyTreeItem)) {
               int multinameIndex = 0;
               GraphTargetItem value = null;
               if (ti instanceof InitPropertyTreeItem) {
                  multinameIndex = ((InitPropertyTreeItem) ti).propertyName.multinameIndex;
                  value = ((InitPropertyTreeItem) ti).value;
               }
               if (ti instanceof SetPropertyTreeItem) {
                  multinameIndex = ((SetPropertyTreeItem) ti).propertyName.multinameIndex;
                  value = ((SetPropertyTreeItem) ti).value;
               }
               for (Trait t : initTraits.traits) {
                  if (t.name_index == multinameIndex) {
                     if ((t instanceof TraitSlotConst)) {
                        if (((TraitSlotConst) t).isConst() || isStaticInitializer) {
                           ((TraitSlotConst) t).assignedValue = value;
                           list.remove(i);
                           i--;
                           continue;
                        }
                        break;
                     }
                  }
               }
            } else {
               break;
            }
         }
      }
      if (isStaticInitializer) {
         List<GraphTargetItem> newList = new ArrayList<GraphTargetItem>();
         for (GraphTargetItem ti : list) {
            if (!(ti instanceof ReturnVoidTreeItem)) {
               if (!(ti instanceof InitPropertyTreeItem)) {
                  if (!(ti instanceof SetPropertyTreeItem)) {
                     newList.add(ti);
                  }
               }
            }
         }
         list = newList;
         if (list.isEmpty()) {
            return "";
         }
      }
      //Declarations
      boolean declaredRegisters[] = new boolean[regCount];
      for (int b = 0; b < declaredRegisters.length; b++) {
         declaredRegisters[b] = false;
      }
      List<Slot> declaredSlots = new ArrayList<Slot>();
      for (int i = 0; i < list.size(); i++) {
         GraphTargetItem ti = list.get(i);
         if (ti instanceof SetLocalTreeItem) {
            int reg = ((SetLocalTreeItem) ti).regIndex;
            if (!declaredRegisters[reg]) {
               list.set(i, new DeclarationTreeItem(ti));
               declaredRegisters[reg] = true;
            }
         }
         if (ti instanceof SetSlotTreeItem) {
            SetSlotTreeItem ssti = (SetSlotTreeItem) ti;
            Slot sl = new Slot(ssti.scope, ssti.slotName);
            if (!declaredSlots.contains(sl)) {
               String type = "*";
               for (int t = 0; t < body.traits.traits.length; t++) {
                  if (body.traits.traits[t].getName(abc) == sl.multiname) {
                     if (body.traits.traits[t] instanceof TraitSlotConst) {
                        type = ((TraitSlotConst) body.traits.traits[t]).getType(constants, fullyQualifiedNames);
                     }
                  }
               }
               list.set(i, new DeclarationTreeItem(ti, type));
               declaredSlots.add(sl);
            }
         }
      }

      s = Graph.graphToString(list, constants, localRegNames, fullyQualifiedNames);
      if (!hilighted) {
         return Highlighting.stripHilights(s);
      }

      return s;
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
      invalidateCache();
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

   public int removeTraps(ConstantPool constants, MethodBody body) {

      removeDeadCode(constants, body);
      boolean isSecure = true;
      try {
         if (code.size() > 4) {
            AVM2Instruction first = code.get(0);
            AVM2Instruction second = code.get(1);
            boolean firstValue = false;
            boolean secondValue = false;
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
                  int pos = 2;
                  AVM2Instruction third = code.get(pos);
                  if (third.definition instanceof SwapIns) {
                     pos++;
                     boolean dup = firstValue;
                     firstValue = secondValue;
                     secondValue = dup;
                     third.ignored = true;
                  }
                  while (third.definition instanceof JumpIns) {
                     pos = adr2pos(pos2adr(pos) + third.getBytes().length + third.operands[0]);
                     third = code.get(pos);
                  }
                  AVM2Instruction firstSet = code.get(pos);
                  while (firstSet.definition instanceof JumpIns) {
                     pos = adr2pos(pos2adr(pos) + firstSet.getBytes().length + firstSet.operands[0]);
                     firstSet = code.get(pos);
                  }
                  pos++;
                  AVM2Instruction secondSet = code.get(pos);
                  while (secondSet.definition instanceof JumpIns) {
                     pos = adr2pos(pos2adr(pos) + secondSet.getBytes().length + secondSet.operands[0]);
                     secondSet = code.get(pos);
                  }
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
                           trueIndex = ((SetLocalTypeIns) secondSet.definition).getRegisterId(secondSet);
                        }
                        if (firstValue == false) {
                           falseIndex = ((SetLocalTypeIns) secondSet.definition).getRegisterId(secondSet);
                        }
                        secondSet.ignored = true;
                        firstSet.ignored = true;
                        first.ignored = true;
                        second.ignored = true;
                        boolean found;
                        do {
                           found = false;
                           for (int ip = 0; ip < code.size(); ip++) {
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
                                       /*if (ins.ignored) {
                                        ip++;
                                        continue;
                                        } else*/ if (ins.definition instanceof GetLocalTypeIns) {
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
                                          boolean val = myStack.pop();
                                          if (val) {
                                             code.get(ip).definition = new JumpIns();
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
                                          ip++;
                                       }

                                    } while (myStack.size() > 0);

                                    break;
                                 }

                              }
                           }
                        } while (found);
                        removeIgnored(constants, body);
                        removeDeadCode(constants, body);
                     } else {
                        //isSecure = false;
                     }
                  }

               }
            }
         }
      } catch (ConvertException cex) {
      }
      int ret = isSecure ? 1 : 0;
      ret += visitCodeTrap(body, new int[code.size()]);
      removeIgnored(constants, body);
      removeDeadCode(constants, body);

      return ret;
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

   private void visitCode(int ip, int lastIp, HashMap<Integer, List<Integer>> refs) {
      while (ip < code.size()) {
         refs.get(ip).add(lastIp);
         lastIp = ip;
         if (refs.get(ip).size() > 1) {
            break;
         }
         AVM2Instruction ins = code.get(ip);
         if (ins.definition instanceof ThrowIns) {
            break;
         }
         if (ins.definition instanceof ReturnValueIns) {
            break;
         }
         if (ins.definition instanceof ReturnVoidIns) {
            break;
         }
         if (ins.definition instanceof LookupSwitchIns) {
            try {
               for (int i = 2; i < ins.operands.length; i++) {
                  visitCode(adr2pos(pos2adr(ip) + ins.operands[i]), ip, refs);
               }
               ip = adr2pos(pos2adr(ip) + ins.operands[0]);
               continue;
            } catch (ConvertException ex) {
            }
         }
         if (ins.definition instanceof JumpIns) {
            try {
               ip = adr2pos(pos2adr(ip) + ins.getBytes().length + ins.operands[0]);
               continue;
            } catch (ConvertException ex) {
               Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
            }
         } else if (ins.definition instanceof IfTypeIns) {
            try {
               visitCode(adr2pos(pos2adr(ip) + ins.getBytes().length + ins.operands[0]), ip, refs);
            } catch (ConvertException ex) {
               Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
            }
         }
         ip++;
      };
   }

   public HashMap<Integer, List<Integer>> visitCode(MethodBody body) {
      HashMap<Integer, List<Integer>> refs = new HashMap<Integer, List<Integer>>();
      for (int i = 0; i < code.size(); i++) {
         refs.put(i, new ArrayList<Integer>());
      }
      visitCode(0, 0, refs);
      int pos = 0;
      for (ABCException e : body.exceptions) {
         pos++;
         try {
            visitCode(adr2pos(e.start), -pos, refs);
            visitCode(adr2pos(e.target), -pos, refs);
            visitCode(adr2pos(e.end), -pos, refs);
         } catch (ConvertException ex) {
            Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
         }
      }
      return refs;
   }

   private int visitCodeTrap(int ip, int visited[], AVM2Instruction prev, AVM2Instruction prev2) {
      int ret = 0;
      while (ip < visited.length) {
         visited[ip]++;
         if (visited[ip] > 1) {
            break;
         }
         AVM2Instruction ins = code.get(ip);
         if (ins.definition instanceof ThrowIns) {
            break;
         }
         if (ins.definition instanceof ReturnValueIns) {
            break;
         }
         if (ins.definition instanceof ReturnVoidIns) {
            break;
         }
         if (ins.definition instanceof LookupSwitchIns) {
            try {
               for (int i = 2; i < ins.operands.length; i++) {
                  ret += visitCodeTrap(adr2pos(pos2adr(ip) + ins.operands[i]), visited, prev, prev2);
               }
               ip = adr2pos(pos2adr(ip) + ins.operands[0]);
               prev2 = prev;
               prev = ins;
               continue;
            } catch (ConvertException ex) {
            }
         }
         if (ins.definition instanceof JumpIns) {
            try {
               ip = adr2pos(pos2adr(ip) + ins.getBytes().length + ins.operands[0]);
               prev2 = prev;
               prev = ins;
               continue;
            } catch (ConvertException ex) {
               Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
            }
         } else if (ins.definition instanceof IfTypeIns) {
            if ((prev != null) && (prev2 != null)) {
               if ((prev.definition instanceof PushByteIns) && (prev2.definition instanceof PushByteIns)) {
                  if (ins.definition instanceof IfEqIns) {
                     prev.ignored = true;
                     prev2.ignored = true;
                     if (prev.operands[0] == prev2.operands[0]) {
                        ins.definition = new JumpIns();
                        visited[ip]--;
                     } else {
                        ins.ignored = true;
                        ip++;
                     }
                     ret++;
                     continue;
                  }
                  if (ins.definition instanceof IfNeIns) {
                     prev.ignored = true;
                     prev2.ignored = true;
                     if (prev.operands[0] != prev2.operands[0]) {
                        ins.definition = new JumpIns();
                        visited[ip]--;
                     } else {
                        ins.ignored = true;
                        ip++;
                     }
                     ret++;
                     continue;
                  }
               }
            }
            if ((prev != null) && ins.definition instanceof IfTrueIns) {
               if (prev.definition instanceof PushTrueIns) {
                  prev.ignored = true;
                  ins.definition = new JumpIns();
                  visited[ip]--;
                  ret++;
                  continue;
               } else if (prev.definition instanceof PushFalseIns) {
                  prev.ignored = true;
                  ins.ignored = true;
                  ret++;
                  ip++;
                  continue;
               }
            }
            if ((prev != null) && ins.definition instanceof IfFalseIns) {
               if (prev.definition instanceof PushFalseIns) {
                  prev.ignored = true;
                  ins.definition = new JumpIns();
                  visited[ip]--;
                  ret++;
                  continue;
               } else if (prev.definition instanceof PushTrueIns) {
                  prev.ignored = true;
                  ins.ignored = true;
                  ret++;
                  ip++;
                  continue;
               }
            }
            try {
               ret += visitCodeTrap(adr2pos(pos2adr(ip) + ins.getBytes().length + ins.operands[0]), visited, prev, prev2);
            } catch (ConvertException ex) {
               Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
            }
         }
         ip++;
         prev2 = prev;
         prev = ins;
      };
      return ret;
   }

   private int visitCodeTrap(MethodBody body, int visited[]) {
      int ret = 0;
      for (int i = 0; i < visited.length; i++) {
         visited[i] = 0;
      }
      ret += visitCodeTrap(0, visited, null, null);
      for (ABCException e : body.exceptions) {
         try {
            ret += visitCodeTrap(adr2pos(e.start), visited, null, null);
            ret += visitCodeTrap(adr2pos(e.target), visited, null, null);
            ret += visitCodeTrap(adr2pos(e.end), visited, null, null);
         } catch (ConvertException ex) {
            Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
         }
      }
      return ret;
   }

   private static class ControlFlowTag {

      public String name;
      public int value;

      public ControlFlowTag(String name, int value) {
         this.name = name;
         this.value = value;
      }
   }

   public void restoreControlFlow(int ip, HashMap<Integer, List<Integer>> refs, int visited2[], HashMap<Integer, List> appended) throws ConvertException {
      List buf = new ArrayList();
      boolean cont = false;
      int continueip = 0;
      AVM2Instruction prev = null;
      for (; ip < code.size(); ip++) {
         AVM2Instruction ins = code.get(ip);

         if ((refs.containsKey(ip) && refs.get(ip).size() > 1) || (visited2[ip] > 0)) {
            if (cont) {
               buf.add(new ControlFlowTag("appendjump", ip));
            }
            cont = false;
            if (visited2[ip] > 0) {
               break;
            }
         }
         visited2[ip]++;
         if (ins.definition instanceof LookupSwitchIns) {

            if (cont) {
               buf.add(new ControlFlowTag("appendjump", ip));
            }
            cont = false;
            restoreControlFlow(adr2pos(pos2adr(ip) + ins.operands[0]), refs, visited2, appended);
            for (int i = 2; i < ins.operands.length; i++) {
               restoreControlFlow(adr2pos(pos2adr(ip) + ins.operands[i]), refs, visited2, appended);
            }
            break;
         }
         if (ins.definition instanceof JumpIns) {
            int newip = adr2pos(pos2adr(ip + 1) + ins.operands[0]);

            boolean allJumpsOrIfs = true;
            for (int ref : refs.get(ip)) {
               if (ref < 0) {
                  continue;
               }
               if (!(code.get(ref).definition instanceof JumpIns)) {
                  if (!(code.get(ref).definition instanceof IfTypeIns)) {
                     allJumpsOrIfs = false;
                     break;
                  } else {
                     if (adr2pos(pos2adr(ref + 1) + code.get(ref).operands[0]) != ip) {
                        allJumpsOrIfs = false;
                        break;
                     }
                  }
               }
            }
            if (allJumpsOrIfs) {
               for (int ref : refs.get(ip)) {
                  if (ref < 0) {
                     continue;
                  }
                  code.get(ref).changeJumpTo = newip;
               }
            }
            if ((newip < code.size()) && (refs.containsKey(newip) && refs.get(newip).size() == 1)) {
               if (!cont) {
                  continueip = ip;
                  buf = new ArrayList<AVM2Instruction>();
                  appended.put(continueip, buf);
               }
               cont = true;
            } else {
               if (cont) {
                  buf.add(new ControlFlowTag("appendjump", newip));
               }
               cont = false;
            }
            ip = newip - 1;
         } else if (ins.definition instanceof IfTypeIns) {
            int newip = adr2pos(pos2adr(ip + 1) + ins.operands[0]);
            if (cont) {
               buf.add(new ControlFlowTag("appendjump", ip));
            }
            cont = false;
            restoreControlFlow(newip, refs, visited2, appended);
         } else if ((ins.definition instanceof ReturnVoidIns) || (ins.definition instanceof ReturnValueIns) || (ins.definition instanceof ThrowIns)) {
            if (cont) {
               buf.add(ip);
            }
            break;
         } else if (cont) {
            buf.add(ip);
         }
         prev = ins;
      }

   }

   private void restoreControlFlowPass(ConstantPool constants, MethodBody body, boolean secondpass) {
      try {
         HashMap<Integer, List<Integer>> refs;
         int visited2[] = new int[code.size()];
         refs = visitCode(body);
         HashMap<Integer, List> appended = new HashMap<Integer, List>();
         /*if (secondpass) {
          restoreControlFlow(code.size() - 1, refs, visited2, appended);
          } else*/ {
            restoreControlFlow(0, refs, visited2, appended);
            for (ABCException e : body.exceptions) {
               try {
                  restoreControlFlow(adr2pos(e.start), refs, visited2, appended);
                  restoreControlFlow(adr2pos(e.target), refs, visited2, appended);
                  restoreControlFlow(adr2pos(e.end), refs, visited2, appended);
               } catch (ConvertException ex) {
                  Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
               }
            }
         }
         for (int ip : appended.keySet()) {
            code.get(ip).replaceWith = appended.get(ip);
         }
      } catch (ConvertException cex) {
         cex.printStackTrace();
      }
      invalidateCache();
      try {
         List<Integer> outputMap = new ArrayList<Integer>();
         String src = Highlighting.stripHilights(toASMSource(constants, body, outputMap));

         AVM2Code acode = ASM3Parser.parse(new ByteArrayInputStream(src.getBytes()), constants, null, body);
         for (int i = 0; i < acode.code.size(); i++) {
            if (outputMap.size() > i) {
               int tpos = outputMap.get(i);
               if (tpos == -1) {
               } else if (code.get(tpos).mappedOffset >= 0) {
                  acode.code.get(i).mappedOffset = code.get(tpos).mappedOffset;
               } else {
                  acode.code.get(i).mappedOffset = pos2adr(tpos);
               }

            }
         }
         this.code = acode.code;
      } catch (IOException ex) {
         Logger.getLogger(AVM2Code.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ParseException ex) {
         Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
      }
      invalidateCache();
      removeDeadCode(constants, body);
   }

   public void restoreControlFlow(ConstantPool constants, MethodBody body) {
      restoreControlFlowPass(constants, body, false);
      //restoreControlFlowPass(constants, body, true);
   }

   /*private void removeIgnored(MethodBody body) {
    for (int rem = code.size() - 1; rem >= 0; rem--) {
    if (code.get(rem).ignored) {
    removeInstruction(rem, body);
    }
    }            
    }*/
   public void removeIgnored(ConstantPool constants, MethodBody body) {
      try {
         List<Integer> outputMap = new ArrayList<Integer>();
         String src = toASMSource(constants, body, outputMap);
         AVM2Code acode = ASM3Parser.parse(new ByteArrayInputStream(src.getBytes()), constants, body);
         for (int i = 0; i < acode.code.size(); i++) {
            if (outputMap.size() > i) {
               int tpos = outputMap.get(i);
               if (tpos == -1) {
               } else if (code.get(tpos).mappedOffset >= 0) {
                  acode.code.get(i).mappedOffset = code.get(tpos).mappedOffset;
               } else {
                  acode.code.get(i).mappedOffset = pos2adr(tpos);
               }
            }
         }
         this.code = acode.code;
      } catch (Exception ex) {
      }
      invalidateCache();
   }

   public int removeDeadCode(ConstantPool constants, MethodBody body) {
      HashMap<Integer, List<Integer>> refs = visitCode(body);

      int cnt = 0;
      for (int i = code.size() - 1; i >= 0; i--) {
         if (refs.get(i).isEmpty()) {
            code.get(i).ignored = true;
            //removeInstruction(i, body);
            cnt++;
         }
      }

      removeIgnored(constants, body);
      for (int i = code.size() - 1; i >= 0; i--) {
         AVM2Instruction ins = code.get(i);
         if (ins.definition instanceof JumpIns) {
            if (ins.operands[0] == 0) {
               code.get(i).ignored = true;
               //removeInstruction(i, body);
               cnt++;
            }
         }
      }
      removeIgnored(constants, body);
      return cnt;
   }

   public void markMappedOffsets() {
      int ofs = 0;
      for (int i = 0; i < code.size(); i++) {
         code.get(i).mappedOffset = ofs;
         ofs += code.get(i).getBytes().length;
      }
   }

   public AVM2Code deepCopy() {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);
         oos.flush();
         oos.close();
         ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
         AVM2Code copy = (AVM2Code) ois.readObject();
         ois.close();
         return copy;
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }
}
