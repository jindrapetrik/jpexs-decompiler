/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphSource;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Lf32Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Lf64Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Li16Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Li32Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Li8Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sf32Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sf64Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Si16Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Si32Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Si8Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sxi16Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sxi1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.alchemy.Sxi8Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DivideIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.ModuloIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.MultiplyIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.MultiplyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NegateIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NegateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.SubtractIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.SubtractIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitAndIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitNotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitOrIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitXorIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.LShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.RShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.URShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.EqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.GreaterEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.GreaterThanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.LessEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.LessThanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.StrictEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructPropIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewActivationIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewArrayIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewCatchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewClassIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewObjectIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugFileIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallMethodIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallStaticIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallSuperVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfEqIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfGeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfGtIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfLeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfLtIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfNGeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfNGtIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfNLeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfNLtIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfNeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictEqIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictNeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.DeletePropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindDefIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetDescendantsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetGlobalScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetGlobalSlotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetScopeObjectIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetSlotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.HasNext2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.HasNextIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.InIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.InitPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.LabelIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.NextNameIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.NextValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.NopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetGlobalSlotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetSlotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ThrowIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushDoubleIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNamespaceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNullIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushShortIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUndefinedIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushWithIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.SwapIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ApplyTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.AsTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.AsTypeLateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceAIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceOrConvertTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceSIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertBIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertDIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertOIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertSIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertUIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.InstanceOfIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.IsTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.IsTypeLateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.TypeOfIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.xml.CheckFilterIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.xml.DXNSIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.xml.DXNSLateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.xml.EscXAttrIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.xml.EscXElemIns;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.HasNextAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVM2Code implements Cloneable {

    private static final Logger logger = Logger.getLogger(AVM2Code.class.getName());
    private static final boolean DEBUG_MODE = false;
    public static int toSourceLimit = -1;
    public List<AVM2Instruction> code = new ArrayList<>();
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
    public static final int DAT_DECIMAL_PARAMS = OPT_U30 + 0x13;
    public static InstructionDefinition[] instructionSet = new InstructionDefinition[]{
        new AddIns(),
        new InstructionDefinition(0x9b, "add_d", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                return -2 + 1; //?
            }
        },
        new AddIIns(),
        new InstructionDefinition(0xb5, "add_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}),
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
        new InstructionDefinition(0x7a, "convert_m_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}) {
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
        new InstructionDefinition(0xb8, "divide_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}) {
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
        new FindDefIns(),
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
        new InstructionDefinition(0x9c, "increment_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}),
        new InstructionDefinition(0x9d, "inclocal_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS, AVM2Code.DAT_REGISTER_INDEX}),
        new InstructionDefinition(0x9e, "decrement_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}),
        new InstructionDefinition(0x9f, "declocal_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS, AVM2Code.DAT_REGISTER_INDEX}),
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
        new InstructionDefinition(0xb9, "modulo_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                return -2 + 1; //?
            }
        },
        new MultiplyIns(),
        new MultiplyIIns(),
        new InstructionDefinition(0xb7, "multiply_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                return -2 + 1; //?
            }
        },
        new NegateIns(),
        new NegateIIns(),
        new InstructionDefinition(0x8f, "negate_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}) {
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
        new InstructionDefinition(0xb6, "subtract_p", new int[]{AVM2Code.DAT_DECIMAL_PARAMS}) {
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
        new Li8Ins(),
        new Li16Ins(),
        new Li32Ins(),
        new Lf32Ins(),
        new Lf64Ins(),
        new Si8Ins(),
        new Si16Ins(),
        new Si32Ins(),
        new Sf32Ins(),
        new Sf64Ins(),
        new Sxi1Ins(),
        new Sxi8Ins(),
        new Sxi16Ins(),
        new InstructionDefinition(0xf5, "verifypass", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xf6, "alloc", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xf7, "mark", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xf8, "wb", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xf9, "prologue", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xfa, "sendenter", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xfb, "doubletoatom", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xfc, "sweep", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xfd, "codegenop", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xfe, "verifyop", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xff, "decode", new int[]{}) {
            @Override
            public int getStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
                throw new UnsupportedOperationException();
            }
        },
        new InstructionDefinition(0xee, "abs_jump", new int[]{}) {
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
    public static InstructionDefinition[] instructionSetByCode = buildInstructionSetByCode();
    public boolean hideTemporaryRegisters = true;

    private static InstructionDefinition[] buildInstructionSetByCode() {
        InstructionDefinition[] result = new InstructionDefinition[256];
        for (InstructionDefinition id : instructionSet) {
            if (result[id.instructionCode] != null) {
                logger.log(Level.WARNING, "Duplicate OPCODE for instruction {0} {1}", new Object[]{result[id.instructionCode], id});
            }
            result[id.instructionCode] = id;
        }
        return result;
    }
    public static final String IDENTOPEN = "/*IDENTOPEN*/";
    public static final String IDENTCLOSE = "/*IDENTCLOSE*/";

    public AVM2Code() {
    }

    public Object execute(HashMap<Integer, Object> arguments, AVM2ConstantPool constants) {
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

    public AVM2Code(ABCInputStream ais) throws IOException {
        Map<Long, AVM2Instruction> codeMap = new TreeMap<>();
        DumpInfo diParent = ais.dumpInfo;
        List<Long> addresses = new ArrayList<>();
        addresses.add(ais.getPosition());
        while (!addresses.isEmpty()) {
            long address = addresses.remove(0);
            if (codeMap.containsKey(address)) {
                continue;
            }
            try {
                ais.seek(address);
                while (ais.available() > 0) {
                    DumpInfo di = ais.newDumpLevel("instruction", "instruction");
                    long startOffset = ais.getPosition();
                    int instructionCode = ais.read("instructionCode");
                    InstructionDefinition instr = instructionSetByCode[instructionCode];
                    if (di != null) {
                        di.name = instr.instructionName;
                    }
                    if (instr != null) {
                        int[] actualOperands = null;
                        if (instructionCode == 0x1b) { //switch
                            int firstOperand = ais.readS24("default_offset");
                            int case_count = ais.readU30("case_count");
                            actualOperands = new int[case_count + 3];
                            actualOperands[0] = firstOperand;
                            actualOperands[1] = case_count;
                            for (int c = 0; c < case_count + 1; c++) {
                                actualOperands[2 + c] = ais.readS24("actualOperand");
                            }
                        } else {
                            if (instr.operands.length > 0) {
                                actualOperands = new int[instr.operands.length];
                                for (int op = 0; op < instr.operands.length; op++) {
                                    switch (instr.operands[op] & 0xff00) {
                                        case OPT_U30:
                                            actualOperands[op] = ais.readU30("operand");
                                            break;
                                        case OPT_U8:
                                            actualOperands[op] = ais.read("operand");
                                            break;
                                        case OPT_BYTE:
                                            actualOperands[op] = (byte) ais.read("operand");
                                            break;
                                        case OPT_S24:
                                            actualOperands[op] = ais.readS24("operand");
                                            break;
                                    }
                                }
                            }
                        }

                        if (instr instanceof IfTypeIns) {
                            long target = ais.getPosition() + actualOperands[0];
                            addresses.add(target);
                        }
                        codeMap.put(startOffset, new AVM2Instruction(startOffset, instr, actualOperands));
                        ais.endDumpLevel(instr.instructionCode);
                    } else {
                        ais.endDumpLevel();
                        break; // Unknown instructions are ignored (Some of the obfuscators add unknown instructions)
                        //throw new UnknownInstructionCode(instructionCode);
                    }
                }
            } catch (EndOfStreamException ex) {
                // lookupswitch obfuscation, ignore
                ais.endDumpLevelUntil(diParent);
            }
        }

        code.addAll(codeMap.values());
    }

    public void compact() {
        if (code instanceof ArrayList) {
            ((ArrayList) code).trimToSize();
        }
    }

    public byte[] getBytes() {
        return getBytes(null);
    }

    public byte[] getBytes(byte[] origBytes) {
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
        StringBuilder s = new StringBuilder();
        for (AVM2Instruction instruction : code) {
            s.append(instruction.toString());
            s.append("\r\n");
        }
        return s.toString();
    }

    public GraphTextWriter toString(GraphTextWriter writer, LocalData localData) {
        int i = 0;
        for (AVM2Instruction instruction : code) {
            writer.appendNoHilight(Helper.formatAddress(i));
            writer.appendNoHilight(" ");
            instruction.toString(writer, localData).newLine();
            i++;
        }
        return writer;
    }

    public GraphTextWriter toASMSource(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, ScriptExportMode exportMode, GraphTextWriter writer) {
        return toASMSource(constants, trait, info, body, new ArrayList<Integer>(), exportMode, writer);
    }

    public GraphTextWriter toASMSource(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, List<Integer> outputMap, ScriptExportMode exportMode, GraphTextWriter writer) {
        invalidateCache();
        if (trait != null) {
            if (trait instanceof TraitFunction) {
                TraitFunction tf = (TraitFunction) trait;
                writer.appendNoHilight("trait ");
                writer.hilightSpecial("function ", "traittype");
                writer.hilightSpecial(constants.multinameToString(tf.name_index), "traitname");
                writer.appendNoHilight(" slotid ");
                writer.hilightSpecial("" + tf.slot_id, "slotid");
                writer.newLine();
            }
            if (trait instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tm = (TraitMethodGetterSetter) trait;
                writer.appendNoHilight("trait ");
                switch (tm.kindType) {
                    case Trait.TRAIT_METHOD:
                        writer.hilightSpecial("method ", "traittype");
                        break;
                    case Trait.TRAIT_GETTER:
                        writer.hilightSpecial("getter ", "traittype");
                        break;
                    case Trait.TRAIT_SETTER:
                        writer.hilightSpecial("setter ", "traittype");
                        break;
                }
                writer.hilightSpecial(constants.multinameToString(tm.name_index), "traitname");
                writer.appendNoHilight(" dispid ");
                writer.hilightSpecial("" + tm.disp_id, "dispid");
                writer.newLine();
            }
        }
        if (info != null) {
            writer.appendNoHilight("method").newLine();
            writer.appendNoHilight("name ");
            writer.hilightSpecial(info.name_index == 0 ? "null" : "\"" + Helper.escapeString(info.getName(constants)) + "\"", "methodname");
            writer.newLine();
            if (info.flagExplicit()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("EXPLICIT", "flag.EXPLICIT");
                writer.newLine();
            }
            if (info.flagHas_optional()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("HAS_OPTIONAL", "flag.HAS_OPTIONAL");
                writer.newLine();
                writer.appendNoHilight("flag HAS_OPTIONAL").newLine();
            }
            if (info.flagHas_paramnames()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("HAS_PARAM_NAMES", "flag.HAS_PARAM_NAMES");
                writer.newLine();
            }
            if (info.flagIgnore_rest()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("EXPLICIT", "flag.IGNORE_REST");
                writer.newLine();
            }
            if (info.flagNeed_activation()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("NEED_ACTIVATION", "flag.NEED_ACTIVATION");
                writer.newLine();
            }
            if (info.flagNeed_arguments()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("NEED_ARGUMENTS", "flag.NEED_ARGUMENTS");
                writer.newLine();
            }
            if (info.flagNeed_rest()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("NEED_REST", "flag.NEED_REST");
                writer.newLine();
            }
            if (info.flagSetsdxns()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("SET_DXNS", "flag.SET_DXNS");
                writer.newLine();
            }
            for (int p = 0; p < info.param_types.length; p++) {
                writer.appendNoHilight("param ");
                writer.hilightSpecial(constants.multinameToString(info.param_types[p]), "param", p);
                writer.newLine();
            }
            if (info.flagHas_paramnames()) {
                for (int n : info.paramNames) {
                    writer.appendNoHilight("paramname ");
                    if (n == 0) {
                        writer.appendNoHilight("null");
                    } else {
                        writer.appendNoHilight("\"");
                        writer.appendNoHilight(constants.getString(n));
                        writer.appendNoHilight("\"");
                    }
                    writer.newLine();
                }
            }
            if (info.flagHas_optional()) {
                for (int i = 0; i < info.optional.length; i++) {
                    ValueKind vk = info.optional[i];
                    writer.appendNoHilight("optional ");
                    writer.hilightSpecial(vk.toString(constants), "optional", i);
                    writer.newLine();
                }
            }
            writer.appendNoHilight("returns ");
            writer.hilightSpecial(constants.multinameToString(info.ret_type), "returns");
            writer.newLine();
        }
        writer.newLine();
        writer.appendNoHilight("body").newLine();

        writer.appendNoHilight("maxstack ");
        writer.appendNoHilight(body.max_stack);
        writer.newLine();

        writer.appendNoHilight("localcount ");
        writer.appendNoHilight(body.max_regs);
        writer.newLine();

        writer.appendNoHilight("initscopedepth ");
        writer.appendNoHilight(body.init_scope_depth);
        writer.newLine();

        writer.appendNoHilight("maxscopedepth ");
        writer.appendNoHilight(body.max_scope_depth);
        writer.newLine();

        List<Long> offsets = new ArrayList<>();
        for (int e = 0; e < body.exceptions.length; e++) {
            writer.appendNoHilight("try");

            writer.appendNoHilight(" from ");
            writer.appendNoHilight("ofs");
            writer.appendNoHilight(Helper.formatAddress(body.exceptions[e].start));
            offsets.add((long) body.exceptions[e].start);

            writer.appendNoHilight(" to ");
            writer.appendNoHilight("ofs");
            writer.appendNoHilight(Helper.formatAddress(body.exceptions[e].end));
            offsets.add((long) body.exceptions[e].end);

            writer.appendNoHilight(" target ");
            writer.appendNoHilight("ofs");
            writer.appendNoHilight(Helper.formatAddress(body.exceptions[e].target));
            offsets.add((long) body.exceptions[e].target);

            writer.appendNoHilight(" type ");
            writer.hilightSpecial(body.exceptions[e].type_index == 0 ? "null" : constants.getMultiname(body.exceptions[e].type_index).toString(constants, new ArrayList<String>()), "try.type", e);

            writer.appendNoHilight(" name ");
            writer.hilightSpecial(body.exceptions[e].name_index == 0 ? "null" : constants.getMultiname(body.exceptions[e].name_index).toString(constants, new ArrayList<String>()), "try.name", e);
            writer.newLine();
        }

        writer.newLine();
        writer.appendNoHilight("code").newLine();

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
        int ip = 0;
        int largeLimit = 20000;
        boolean markOffsets = code.size() <= largeLimit;

        if (exportMode == ScriptExportMode.HEX) {
            Helper.byteArrayToHexWithHeader(writer, getBytes());
        } else {
            for (AVM2Instruction ins : code) {
                long ofs = ins.offset;
                if (exportMode == ScriptExportMode.PCODE_HEX) {
                    writer.appendNoHilight("; ");
                    writer.appendNoHilight(Helper.bytesToHexString(ins.getBytes()));
                    writer.newLine();
                }
                if (Configuration.showAllAddresses.get() || offsets.contains(ofs)) {
                    writer.appendNoHilight("ofs" + Helper.formatAddress(ofs) + ":");
                }
                /*for (int e = 0; e < body.exceptions.length; e++) {
                 if (body.exceptions[e].start == ofs) {
                 ret.append("exceptionstart " + e + ":");
                 }
                 if (body.exceptions[e].end == ofs) {
                 ret.append("exceptionend " + e + ":");
                 }
                 if (body.exceptions[e].target == ofs) {
                 ret.append("exceptiontarget " + e + ":");
                 }
                 }*/
                if (ins.replaceWith != null) {
                    for (Object o : ins.replaceWith) {
                        if (o instanceof Integer) {
                            AVM2Instruction ins2 = code.get((Integer) o);
                            if (ins2.isIgnored()) {
                                continue;
                            }
                            writer.append("", ins2.mappedOffset > -1 ? ins2.mappedOffset : ofs);
                            writer.appendNoHilight(ins2.toStringNoAddress(constants, new ArrayList<String>()) + " ;copy from " + Helper.formatAddress(pos2adr((Integer) o)));
                            writer.newLine();
                            outputMap.add((Integer) o);
                        } else if (o instanceof ControlFlowTag) {
                            ControlFlowTag cft = (ControlFlowTag) o;
                            if (cft.name.equals("appendjump")) {
                                writer.appendNoHilight("jump ofs" + Helper.formatAddress(pos2adr(cft.value))).newLine();
                                outputMap.add(-1);
                            }
                            if (cft.name.equals("mark")) {
                                writer.appendNoHilight("ofs" + Helper.formatAddress(pos2adr(cft.value)) + ":");
                            }
                        }
                    }
                } else {
                    if (!ins.isIgnored()) {
                        if (markOffsets) {
                            writer.append("", ins.mappedOffset > -1 ? ins.mappedOffset : ofs);
                        }
                        int fixBranch = ins.getFixBranch();
                        if (fixBranch > -1) {
                            if (ins.definition instanceof IfTypeIns) {
                                for (int i = 0; i < -ins.definition.getStackDelta(ins, null/*IfTypeIns do not require ABCs*/); i++) {
                                    writer.appendNoHilight(new DeobfuscatePopIns().instructionName).newLine();
                                }
                                if (fixBranch == 0) { //jump
                                    writer.appendNoHilight(new JumpIns().instructionName + " ofs" + Helper.formatAddress(ofs + ins.getBytes().length + ins.operands[0]));
                                } else {
                                    //nojump, ignore
                                }
                            }
                            //TODO: lookupswitch ?
                        } else {
                            if (ins.changeJumpTo > -1) {
                                writer.appendNoHilight(ins.definition.instructionName + " ofs" + Helper.formatAddress(pos2adr(ins.changeJumpTo)));
                            } else {
                                writer.appendNoHilight(ins.toStringNoAddress(constants, new ArrayList<String>()));
                            }
                        }
                        writer.newLine();
                        outputMap.add(ip);
                    }
                }
                ip++;
            }
        }
        return writer;
    }
    private boolean cacheActual = false;
    private List<Long> posCache;

    private void buildCache() {
        posCache = new ArrayList<>();
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
    private List<Integer> unknownJumps;
    private List<Integer> ignoredIns;
    boolean isCatched = false;

    /**
     * Test for killed register. CalcKilledStats must be called before
     * @param regName
     * @param start
     * @param end
     * @return 
     */
    public boolean isKilled(int regName, int start, int end) {
        return killedRegs.contains(regName);
    }
    private int toSourceCount = 0;

    public HashMap<Integer, String> getLocalRegNamesFromDebug(ABC abc) {
        HashMap<Integer, String> localRegNames = new HashMap<>();
        for (AVM2Instruction ins : code) {
            if (ins.definition instanceof DebugIns) {
                if (ins.operands[0] == 1) {
                    localRegNames.put(ins.operands[2] + 1, abc.constants.getString(ins.operands[1]));
                }
            }
        }
        return localRegNames;
    }

    private Set<Integer> killedRegs=new HashSet<Integer>();
    
    public void calcKilledStats(MethodBody body){
        try {
            HashMap<Integer,List<Integer>> vis=visitCode(body);
            for(int k=0;k<code.size();k++){
                if(vis.get(k).isEmpty()){
                    continue;
                }
                if (code.get(k).definition instanceof KillIns) {
                    killedRegs.add(code.get(k).operands[0]);
                }
            }
            
        } catch (InterruptedException ex) {
            //ignored
        }
    }
    
    public List<GraphTargetItem> clearTemporaryRegisters(List<GraphTargetItem> output) {        
         for (int i = 0; i < output.size(); i++) {
            if (output.get(i) instanceof SetLocalAVM2Item) {
                if (isKilled(((SetLocalAVM2Item) output.get(i)).regIndex, 0, code.size() - 1)) {
                    SetLocalAVM2Item lsi = (SetLocalAVM2Item) output.get(i);
                    if (i + 1 < output.size()) {
                        if (output.get(i + 1) instanceof ReturnValueAVM2Item) {
                            ReturnValueAVM2Item rv = (ReturnValueAVM2Item) output.get(i + 1);
                            if (rv.value instanceof LocalRegAVM2Item) {
                                LocalRegAVM2Item lr = (LocalRegAVM2Item) rv.value;
                                if (lr.regIndex == lsi.regIndex) {
                                    rv.value = lsi.value;
                                }
                            }
                        }
                    }
                    output.remove(i);
                    i--;
                }
            } else if (output.get(i) instanceof WithAVM2Item) {
                clearTemporaryRegisters(((WithAVM2Item) output.get(i)).items);
            }
        }
        return output;
    }

    public int fixIPAfterDebugLine(int ip) {
        if (code.isEmpty()) {
            return ip;
        }
        if (ip >= code.size()) {
            return code.size() - 1;
        }
        while (code.get(ip).definition instanceof DebugLineIns) {
            ip++;
        }
        return ip;
    }

    public int fixAddrAfterDebugLine(int addr) throws ConvertException {
        return pos2adr(fixIPAfterDebugLine(adr2pos(addr)));
    }

    public ConvertOutput toSourceOutput(String path, GraphPart part, boolean processJumps, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, ABC abc, AVM2ConstantPool constants, List<MethodInfo> method_info, MethodBody body, int start, int end, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames, boolean[] visited, HashMap<Integer, Integer> localRegAssigmentIps, HashMap<Integer, List<Integer>> refs) throws ConvertException, InterruptedException {
        calcKilledStats(body);
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
        List<GraphTargetItem> output = new ArrayList<>();
        int ip = start;
        //try {
        //int addr;
        iploop:
        while (ip <= end) {

            if (ignoredIns.contains(ip)) {
                ip++;
                continue;
            }
            boolean processTry = processJumps;
            //addr = pos2adr(ip);
            int ipfix = fixIPAfterDebugLine(ip);
            //int addrfix = pos2adr(ipfix);
            int maxend = -1;

            if (ip > end) {
                break;
            }

            if (unknownJumps.contains(ip)) {
                unknownJumps.remove(Integer.valueOf(ip));
                throw new UnknownJumpException(stack, ip, output);
            }
            if (visited[ip]) {
                logger.warning("Code already visited, ofs:" + Helper.formatAddress(pos2adr(ip)) + ", ip:" + ip);
                break;
            }
            visited[ip] = true;
            AVM2Instruction ins = code.get(ip);
            if (debugMode) {
                System.err.println("translating ip " + ip + " ins " + ins.toString() + " stack:" + stack.toString() + " scopeStack:" + scopeStack.toString());
            }
            if (ins.definition instanceof NewFunctionIns) {
                if (ip + 1 <= end) {
                    if (code.get(ip + 1).definition instanceof PopIns) {
                        ip += 2;
                        continue;
                    }
                }
            }
            /*if ((ip + 8 < code.size())) { //return in finally clause
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
             }//*/

            /*if ((ip + 2 < code.size()) && (ins.definition instanceof NewCatchIns)) { //Filling local register in catch clause
             if (code.get(ip + 1).definition instanceof DupIns) {
             if (code.get(ip + 2).definition instanceof SetLocalTypeIns) {
             ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames);
             ip += 3;
             continue;
             }
             }
             }*/
            if ((ins.definition instanceof GetLocalTypeIns) && (!output.isEmpty()) && (output.get(output.size() - 1) instanceof SetLocalAVM2Item) && (((SetLocalAVM2Item) output.get(output.size() - 1)).regIndex == ((GetLocalTypeIns) ins.definition).getRegisterId(ins)) && isKilled(((SetLocalAVM2Item) output.get(output.size() - 1)).regIndex, start, end)) {
                SetLocalAVM2Item slt = (SetLocalAVM2Item) output.remove(output.size() - 1);
                stack.push(slt.getValue());
                ip++;
            } else if ((ins.definition instanceof SetLocalTypeIns) && (ip + 1 <= end) && (isKilled(((SetLocalTypeIns) ins.definition).getRegisterId(ins), ip, end))) { //set_local_x,get_local_x..kill x
                AVM2Instruction insAfter = code.get(ip + 1);
                if ((insAfter.definition instanceof GetLocalTypeIns) && (((GetLocalTypeIns) insAfter.definition).getRegisterId(insAfter) == ((SetLocalTypeIns) ins.definition).getRegisterId(ins))) {
                    GraphTargetItem before = stack.peek();
                    ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
                    stack.push(before);
                    ip += 2;
                    continue iploop;
                } else {
                    ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
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
                        //addr = pos2adr(ip);
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
                                            ConvertOutput assignment = toSourceOutput(path, part, processJumps, isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, ip + 2, t - 1, localRegNames, fullyQualifiedNames, visited, localRegAssigmentIps, refs);
                                            GraphTargetItem tar = assignment.output.remove(assignment.output.size() - 1);
                                            tar.firstPart = part;
                                            stack.push(tar);
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
                                    if (stack.isEmpty()) {
                                        break;//FIXME?o
                                    }
                                    GraphTargetItem v = stack.pop();
                                    stack.push(new LocalRegAVM2Item(ins, reg, v));
                                    stack.push(v);
                                } else {
                                    break;
                                }
                            }
                        } else {
                            ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
                        }
                        ip++;
                        break;
                        //}

                    } else {
                        ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
                        ip++;
                        break;
                        //throw new ConvertException("Unknown pattern after DUP:" + insComparsion.toString());
                    }
                } while (ins.definition instanceof DupIns);
            } else if ((ins.definition instanceof ReturnValueIns) || (ins.definition instanceof ReturnVoidIns) || (ins.definition instanceof ThrowIns)) {
                ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
                //ip = end + 1;
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
                                                    functionName = abc.constants.getMultiname(code.get(ip + plus + 3).operands[0]).getName(constants, fullyQualifiedNames, true);
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
                ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
                NewFunctionAVM2Item nft = (NewFunctionAVM2Item) stack.peek();
                nft.functionName = functionName;
                ip++;
            } else {
                try {
                    ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
                } catch (RuntimeException re) {
                    /*String last="";
                     int len=5;
                     for(int i=(ip-len<0?0:ip-len);i<ip+len && ip<code.size();i++){
                     if(i==ip){
                     last+=">";
                     }
                     last+=""+i+": "+code.get(i).toString()+"\r\n";
                     }
                     Logger.getLogger(AVM2Code.class.getName()).log(Level.SEVERE, "ins list:\r\n{0}", last);*/
                    throw re;
                }
                ip++;
                //addr = pos2adr(ip);
            }

        }
        if (debugMode) {
            System.out.println("CLOSE SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
        }
        /*if (hideTemporaryRegisters) {
         clearTemporaryRegisters(output);
         }*/
        return new ConvertOutput(stack, output);
        /*} catch (ConvertException cex) {
         throw cex;
         }*/
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

    public HashMap<Integer, GraphTargetItem> getLocalRegTypes(AVM2ConstantPool constants, List<String> fullyQualifiedNames) {
        HashMap<Integer, GraphTargetItem> ret = new HashMap<>();
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
        unknownJumps = new ArrayList<>();
        ignoredIns = new ArrayList<>();
    }

    public List<GraphTargetItem> toGraphTargetItems(String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, AVM2ConstantPool constants, List<MethodInfo> method_info, MethodBody body, HashMap<Integer, String> localRegNames, ScopeStack scopeStack, boolean isStaticInitializer, List<String> fullyQualifiedNames, Traits initTraits, int staticOperation, HashMap<Integer, Integer> localRegAssigmentIps, HashMap<Integer, List<Integer>> refs) throws InterruptedException {
        initToSource();
        List<GraphTargetItem> list;
        HashMap<Integer, GraphTargetItem> localRegs = new HashMap<>();

        int regCount = getRegisterCount();

        //try {
        list = AVM2Graph.translateViaGraph(path, this, abc, body, isStatic, scriptIndex, classIndex, localRegs, scopeStack, localRegNames, fullyQualifiedNames, staticOperation, localRegAssigmentIps, refs);

        if (initTraits != null) {
            for (int i = 0; i < list.size(); i++) {
                GraphTargetItem ti = list.get(i);
                if ((ti instanceof InitPropertyAVM2Item) || (ti instanceof SetPropertyAVM2Item)) {
                    int multinameIndex = 0;
                    GraphTargetItem value = null;
                    if (ti instanceof InitPropertyAVM2Item) {
                        multinameIndex = ((InitPropertyAVM2Item) ti).propertyName.multinameIndex;
                        value = ((InitPropertyAVM2Item) ti).value;
                    }
                    if (ti instanceof SetPropertyAVM2Item) {
                        multinameIndex = ((FullMultinameAVM2Item) ((SetPropertyAVM2Item) ti).propertyName).multinameIndex;
                        value = ((SetPropertyAVM2Item) ti).value;
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
                    //In obfuscated code, SetLocal instructions comes first
                    //break;
                }
            }
        }
        if (isStaticInitializer) {
            List<GraphTargetItem> newList = new ArrayList<>();
            for (GraphTargetItem ti : list) {
                if (!(ti instanceof ReturnVoidAVM2Item)) {
                    if (!(ti instanceof InitPropertyAVM2Item)) {
                        if (!(ti instanceof SetPropertyAVM2Item)) {
                            newList.add(ti);
                        }
                    }
                }
            }
            list = newList;
            if (list.isEmpty()) {
                return list;
            }
        }
        //Declarations
        boolean[] declaredRegisters = new boolean[regCount];
        for (int b = 0; b < declaredRegisters.length; b++) {
            declaredRegisters[b] = false;
        }
        List<Slot> declaredSlots = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem ti = list.get(i);
            if (ti instanceof SetLocalAVM2Item) {
                int reg = ((SetLocalAVM2Item) ti).regIndex;
                if (!declaredRegisters[reg]) {
                    list.set(i, new DeclarationAVM2Item(ti));
                    declaredRegisters[reg] = true;
                }
            }
            if (ti instanceof SetSlotAVM2Item) {
                SetSlotAVM2Item ssti = (SetSlotAVM2Item) ti;
                Slot sl = new Slot(ssti.scope, ssti.slotName);
                if (!declaredSlots.contains(sl)) {
                    GraphTargetItem type = TypeItem.UNBOUNDED;
                    for (int t = 0; t < body.traits.traits.size(); t++) {
                        if (body.traits.traits.get(t).getName(abc) == sl.multiname) {
                            if (body.traits.traits.get(t) instanceof TraitSlotConst) {
                                type = PropertyAVM2Item.multinameToType(((TraitSlotConst) body.traits.traits.get(t)).type_index, abc.constants);
                            }
                        }
                    }
                    list.set(i, new DeclarationAVM2Item(ti, type));
                    declaredSlots.add(sl);
                }
            }
        }

        int lastPos = list.size() - 1;
        if (lastPos < 0) {
            lastPos = 0;
        }
        if ((list.size() > lastPos) && (list.get(lastPos) instanceof ScriptEndItem)) {
            lastPos--;
        }
        if (lastPos < 0) {
            lastPos = 0;
        }
        if ((list.size() > lastPos) && (list.get(lastPos) instanceof ReturnVoidAVM2Item)) {
            list.remove(lastPos);
        }

        return list;
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

    @SuppressWarnings("unchecked")
    private static AVM2LocalData prepareBranchLocalData(AVM2LocalData localData) {
        AVM2LocalData ret = new AVM2LocalData();
        ret.isStatic = localData.isStatic;
        ret.classIndex = localData.classIndex;
        ret.localRegs = new HashMap<>(localData.localRegs);
        ret.scopeStack = (ScopeStack) (localData.scopeStack).clone();
        ret.constants = localData.constants;
        ret.methodInfo = localData.methodInfo;
        ret.methodBody = localData.methodBody;
        ret.abc = localData.abc;
        ret.localRegNames = localData.localRegNames;
        ret.fullyQualifiedNames = localData.fullyQualifiedNames;
        ret.parsedExceptions = localData.parsedExceptions;
        ret.finallyJumps = localData.finallyJumps;
        ret.ignoredSwitches = localData.ignoredSwitches;
        ret.scriptIndex = localData.scriptIndex;
        ret.localRegAssignmentIps = localData.localRegAssignmentIps;
        ret.ip = localData.ip;
        ret.refs = localData.refs;
        ret.code = localData.code;
        return ret;
    }

    public int removeTraps(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, ABC abc, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        removeDeadCode(constants, trait, info, body);
        AVM2LocalData localData = new AVM2LocalData();
        localData.isStatic = isStatic;
        localData.classIndex = classIndex;
        localData.localRegs = new HashMap<>();
        localData.scopeStack = new ScopeStack();
        localData.constants = abc.constants;
        localData.methodInfo = abc.method_info;
        localData.methodBody = body;
        localData.abc = abc;
        localData.localRegNames = body.getLocalRegNames(abc);
        localData.fullyQualifiedNames = new ArrayList<>();
        localData.parsedExceptions = new ArrayList<>();
        localData.finallyJumps = new ArrayList<>();
        localData.ignoredSwitches = new ArrayList<>();
        localData.scriptIndex = scriptIndex;
        localData.localRegAssignmentIps = new HashMap<>();
        localData.ip = 0;
        HashMap<Integer, List<Integer>> refs = visitCode(body);
        localData.refs = refs;
        localData.code = this;
        int ret = 0;
        ret += removeTraps(constants, trait, info, body, localData, new AVM2GraphSource(this, false, -1, -1, new HashMap<Integer, GraphTargetItem>(), new ScopeStack(), abc, body, new HashMap<Integer, String>(), new ArrayList<String>(), new HashMap<Integer, Integer>(), refs), 0, path, refs);
        removeIgnored(constants, trait, info, body);
        removeDeadCode(constants, trait, info, body);

        return ret;
    }

    private void handleRegister(CodeStats stats, int reg) {
        if (reg + 1 > stats.maxlocal) {
            stats.maxlocal = reg + 1;
        }
    }

    private boolean walkCode(CodeStats stats, int pos, int stack, int scope, ABC abc) {
        while (pos < code.size()) {
            AVM2Instruction ins = code.get(pos);
            if (stats.instructionStats[pos].seen) {
                //check stack mismatch here
                return true;
            }

            if (ins.definition instanceof NewFunctionIns) {
                MethodBody innerBody = abc.findBody(ins.operands[0]);
                innerBody.autoFillStats(abc, stats.initscope + (stats.has_activation ? 1 : 0), false);
            }

            stats.instructionStats[pos].seen = true;
            stats.instructionStats[pos].stackpos = stack;
            stats.instructionStats[pos].scopepos = scope;

            int stackDelta = ins.definition.getStackDelta(ins, abc);
            int scopeDelta = ins.definition.getScopeStackDelta(ins, abc);
            int oldStack = stack;

            //+" deltaScope:"+(scopeDelta>0?"+"+scopeDelta:scopeDelta)+" stack:"+stack+" scope:"+scope);
            stack += stackDelta;
            scope += scopeDelta;

            stats.instructionStats[pos].stackpos_after = stack;
            stats.instructionStats[pos].scopepos_after = scope;

            if (stack > stats.maxstack) {
                stats.maxstack = stack;
            }
            if (scope > stats.maxscope) {
                stats.maxscope = scope;
            }

            //System.out.println("stack "+oldStack+(stackDelta>=0?"+"+stackDelta:stackDelta)+" max:"+stats.maxstack+" "+ins);
            if ((ins.definition instanceof DXNSIns) || (ins.definition instanceof DXNSLateIns)) {
                stats.has_set_dxns = true;
            }
            if (ins.definition instanceof NewActivationIns) {
                stats.has_activation = true;
            }
            if (ins.definition instanceof SetLocalTypeIns) {
                handleRegister(stats, ((SetLocalTypeIns) ins.definition).getRegisterId(ins));
            } else if (ins.definition instanceof GetLocalTypeIns) {
                handleRegister(stats, ((GetLocalTypeIns) ins.definition).getRegisterId(ins));
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
                    if (i == 1) {
                        continue;
                    }
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

    public CodeStats getStats(ABC abc, MethodBody body, int initScope) {
        CodeStats stats = new CodeStats(this);
        stats.initscope = initScope;
        if (!walkCode(stats, 0, 0, initScope, abc)) {
            return null;
        }
        int scopePos = -1;
        int prevStart = 0;
        for (int e = 0; e < body.exceptions.length; e++) {
            ABCException ex = body.exceptions[e];
            try {
                if (scopePos == -1) {
                    scopePos = stats.instructionStats[adr2pos(ex.end) - 1].scopepos_after;
                }
                List<Integer> visited = new ArrayList<>();
                for (int i = 0; i < stats.instructionStats.length; i++) {
                    if (stats.instructionStats[i].seen) {
                        visited.add(i);
                    }
                }
                if (!walkCode(stats, adr2pos(ex.target), 1 + (ex.isFinally() ? 1 : 0), scopePos, abc)) {
                    return null;
                }
                int maxIp = 0;
                //searching for visited instruction in second run which has maximum position
                for (int i = 0; i < stats.instructionStats.length; i++) {
                    if (stats.instructionStats[i].seen && !visited.contains(i)) {
                        maxIp = i;
                    }
                }
                scopePos = stats.instructionStats[maxIp].scopepos_after;
                int stackPos = stats.instructionStats[maxIp].stackpos_after;
                int nextIp = maxIp + 1;
                if (code.get(maxIp).definition instanceof JumpIns) {
                    nextIp = adr2pos(pos2adr(nextIp) + code.get(maxIp).operands[0]);
                }
                if (nextIp < stats.instructionStats.length) {
                    int origScopePos = stats.instructionStats[nextIp].scopepos;
                    int origStackPos = stats.instructionStats[nextIp].stackpos;

                    if (prevStart == ex.start && ex.isFinally() && !code.get(nextIp).isExit() && stats.instructionStats[nextIp].seen) {
                        for (int i = 0; i < stats.instructionStats.length; i++) {
                            stats.instructionStats[i].seen = false;
                        }
                        //Rerun rest with new scopePos, stackPos
                        if (!walkCode(stats, nextIp, origStackPos + 1/*magic!*/, scopePos - 1 /*magic!*/, abc)) {
                            return null;
                        }
                        scopePos--;
                    }
                }
                prevStart = ex.start;
            } catch (ConvertException ex1) {
                //ignore
            }
        }
        //stats.maxscope+=initScope;
        return stats;
    }

    private void visitCode(int ip, int lastIp, HashMap<Integer, List<Integer>> refs) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        while (ip < code.size()) {
            if (!refs.containsKey(ip)) {
                refs.put(ip, new ArrayList<Integer>());
            }
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
                    logger.log(Level.FINE, null, ex);
                }
            } else if (ins.definition instanceof IfTypeIns) {
                try {
                    visitCode(adr2pos(pos2adr(ip) + ins.getBytes().length + ins.operands[0]), ip, refs);
                } catch (ConvertException ex) {
                    logger.log(Level.FINE, null, ex);
                }
            }
            ip++;
        };
    }

    public HashMap<Integer, List<Integer>> visitCode(MethodBody body) throws InterruptedException {
        HashMap<Integer, List<Integer>> refs = new HashMap<>();
        for (int i = 0; i < code.size(); i++) {
            refs.put(i, new ArrayList<Integer>());
        }
        visitCode(0, 0, refs);
        int pos = 0;
        for (ABCException e : body.exceptions) {
            pos++;
            try {
                visitCode(adr2pos(e.start), adr2pos(e.start) - 1, refs);
                visitCode(adr2pos(e.start), -1, refs);
                visitCode(adr2pos(e.target), adr2pos(e.end), refs);
                visitCode(adr2pos(e.end), -pos, refs);
            } catch (ConvertException ex) {
                logger.log(Level.FINE, null, ex);
            }
        }
        return refs;
    }

    private int visitCodeTrap(int ip, int[] visited, AVM2Instruction prev, AVM2Instruction prev2) {
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
                    logger.log(Level.FINE, null, ex);
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
                    logger.log(Level.FINE, null, ex);
                }
            }
            ip++;
            prev2 = prev;
            prev = ins;
        };
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

    public void restoreControlFlow(int ip, HashMap<Integer, List<Integer>> refs, int[] visited2, HashMap<Integer, List<Object>> appended) throws ConvertException {
        List<Object> buf = new ArrayList<>();
        boolean cont = false;
        int continueip = 0;
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
                        buf = new ArrayList<>();
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
        }

    }

    private void restoreControlFlowPass(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, boolean secondpass) throws InterruptedException {
        try {
            HashMap<Integer, List<Integer>> refs;
            int[] visited2 = new int[code.size()];
            refs = visitCode(body);
            HashMap<Integer, List<Object>> appended = new HashMap<>();
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
                        logger.log(Level.FINE, null, ex);
                    }
                }
            }
            for (int ip : appended.keySet()) {
                code.get(ip).replaceWith = appended.get(ip);
            }
        } catch (ConvertException cex) {
            logger.log(Level.SEVERE, "Error during restore control flow", cex);
        }
        invalidateCache();
        try {
            List<Integer> outputMap = new ArrayList<>();
            HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), false);
            toASMSource(constants, trait, info, body, outputMap, ScriptExportMode.PCODE, writer);
            String src = writer.toString();

            AVM2Code acode = ASM3Parser.parse(new StringReader(src), constants, null, body, info);
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
            logger.log(Level.SEVERE, null, ex);
        } catch (AVM2ParseException ex) {
            logger.log(Level.FINE, null, ex);
        }
        invalidateCache();
        removeDeadCode(constants, trait, info, body);
    }

    public void restoreControlFlow(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body) throws InterruptedException {
        restoreControlFlowPass(constants, trait, info, body, false);
        //restoreControlFlowPass(constants, body, true);
    }

    /*private void removeIgnored(MethodBody body) {
     for (int rem = code.size() - 1; rem >= 0; rem--) {
     if (code.get(rem).ignored) {
     removeInstruction(rem, body);
     }
     }
     }*/
    public void removeIgnored(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body) throws InterruptedException {
        try {
            List<Integer> outputMap = new ArrayList<>();
            HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), false);
            toASMSource(constants, trait, info, body, outputMap, ScriptExportMode.PCODE, writer);
            String src = writer.toString();
            AVM2Code acode = ASM3Parser.parse(new StringReader(src), constants, trait, body, info);
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
        } catch (IOException | AVM2ParseException ex) {
        }
        invalidateCache();
    }

    public int removeDeadCode(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body) throws InterruptedException {
        HashMap<Integer, List<Integer>> refs = visitCode(body);

        int cnt = 0;
        for (int i = code.size() - 1; i >= 0; i--) {
            if (refs.get(i).isEmpty()) {
                code.get(i).ignored = true;
                //removeInstruction(i, body);
                cnt++;
            }
        }

        removeIgnored(constants, trait, info, body);
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
        removeIgnored(constants, trait, info, body);
        return cnt;
    }

    public void markMappedOffsets() {
        int ofs = 0;
        for (int i = 0; i < code.size(); i++) {
            code.get(i).mappedOffset = ofs;
            ofs += code.get(i).getBytes().length;
        }
    }

    private static class Decision {

        public boolean jumpUsed = false;
        public boolean skipUsed = false;
        public Set<Integer> casesUsed = new HashSet<>();
        HashMap<Integer, GraphTargetItem> registers = new HashMap<>();
    }

    private static int getMostCommonIp(AVM2GraphSource code, List<Integer> branches) {
        List<List<Integer>> reachable = new ArrayList<>();
        for (int i = 0; i < branches.size(); i++) {
            List<Integer> r = new ArrayList<>();
            getReachableIps(code, branches.get(i), r);
        }

        int commonLevel;
        Map<Integer, Integer> levelMap = new HashMap<>();
        for (List<Integer> first : reachable) {
            int maxclevel = 0;
            Set<Integer> visited = new HashSet<>();
            for (Integer p : first) {
                if (visited.contains(p)) {
                    continue;
                }
                visited.add(p);
                boolean common = true;
                commonLevel = 1;
                for (List<Integer> r : reachable) {
                    if (r == first) {
                        continue;
                    }
                    if (r.contains(p)) {
                        commonLevel++;
                    }
                }
                if (commonLevel <= maxclevel) {
                    continue;
                }
                maxclevel = commonLevel;
                if (levelMap.containsKey(p)) {
                    if (levelMap.get(p) > commonLevel) {
                        commonLevel = levelMap.get(p);
                    }
                }
                levelMap.put(p, commonLevel);
                if (common) {
                    //return p;
                }
            }
        }
        for (int i = reachable.size() - 1; i >= 2; i--) {
            for (Integer p : levelMap.keySet()) {
                if (levelMap.get(p) == i) {
                    return p;
                }
            }
        }
        for (Integer p : levelMap.keySet()) {
            if (levelMap.get(p) == branches.size()) {
                return p;
            }
        }
        return -1;
    }

    public static void getReachableIps(AVM2GraphSource code, int ip, List<Integer> reachable) {
        do {
            if (reachable.contains(ip)) {
                return;
            }
            reachable.add(ip);
            GraphSourceItem ins = code.get(ip);
            if (ins.isJump() || ins.isBranch()) {
                List<Integer> branches = ins.getBranches(code);
                for (int i = 1; i < branches.size(); i++) {
                    getReachableIps(code, branches.get(i), reachable);
                }
                ip = branches.get(0);
                continue;
            }
            ip++;
        } while (ip < code.size());
    }

    public static boolean isDirectAncestor(int currentIp, int ancestor, HashMap<Integer, List<Integer>> refs) {
        return isDirectAncestor(currentIp, ancestor, refs, new ArrayList<Integer>());
    }

    private static boolean isDirectAncestor(int currentIp, int ancestor, HashMap<Integer, List<Integer>> refs, List<Integer> visited) {
        if (currentIp == -1) {
            return true;
        }
        do {
            if (currentIp == ancestor) {
                return true;
            }
            if (currentIp == 0) {
                return false;
            }
            if (visited.contains(currentIp)) {
                return true;
            }
            visited.add(currentIp);
            if (refs.containsKey(currentIp)) {
                List<Integer> currentRefs = refs.get(currentIp);
                if ((currentRefs != null) && (!currentRefs.isEmpty())) {
                    for (int i = 1; i < currentRefs.size(); i++) {
                        if (!isDirectAncestor(currentRefs.get(i), ancestor, refs, visited)) {
                            return false;
                        }
                    }
                    currentIp = currentRefs.get(0);
                    continue;
                }
            }
            currentIp--;
        } while (currentIp >= 0);
        return false;
    }

    public static boolean getPreviousReachableIps(int currentIp, HashMap<Integer, List<Integer>> refs, Set<Integer> reachable, Set<Integer> visited) {
        do {
            if (visited.contains(currentIp)) {
                return false;
            }
            reachable.add(currentIp);
            visited.add(currentIp);
            if (refs.containsKey(currentIp)) {
                List<Integer> currentRefs = refs.get(currentIp);
                if ((currentRefs != null) && (!currentRefs.isEmpty())) {
                    if (currentRefs.size() == 1) {
                        currentIp = currentRefs.get(0);
                        continue;
                    }
                    boolean r = false;
                    for (int i = 0; i < currentRefs.size(); i++) {
                        Set<Integer> nr = new HashSet<>();
                        boolean v = getPreviousReachableIps(currentRefs.get(i), refs, nr, visited);
                        if ((!v) || nr.contains(0)) {
                            reachable.addAll(nr);
                        }
                        r = r || v;
                    }
                    return r;
                }
            }
            currentIp--;
        } while (currentIp >= 0);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static int removeTraps(HashMap<Integer, List<Integer>> refs, boolean secondPass, boolean indeterminate, AVM2LocalData localData, TranslateStack stack, List<GraphTargetItem> output, AVM2GraphSource code, int ip, HashMap<Integer, Integer> visited, HashMap<Integer, HashMap<Integer, GraphTargetItem>> visitedStates, HashMap<AVM2Instruction, Decision> decisions, String path, int recursionLevel) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        if (recursionLevel > code.size() + 1) {
            throw new TranslateException("removeTraps max recursion level reached.");
        }
        boolean debugMode = false;
        int ret = 0;
        iploop:
        while ((ip > -1) && ip < code.size()) {

            if (false) { //useVisited) {
                if (visited.containsKey(ip)) {
                    break;
                }
                if (!visited.containsKey(ip)) {
                    visited.put(ip, 0);
                } else {
                    visited.put(ip, visited.get(ip) + 1);
                }
            } else {
                HashMap<Integer, GraphTargetItem> currentState = localData.localRegs;

                if (visitedStates.containsKey(ip)) {
                    HashMap<Integer, GraphTargetItem> lastState = visitedStates.get(ip);
                    if (lastState.equals(currentState)) {
                        break;
                    }
                }
                visitedStates.put(ip, (HashMap<Integer, GraphTargetItem>) currentState.clone());

            }

            int curVisited;
            if (!visited.containsKey(ip)) {
                curVisited = 1;
            } else {
                curVisited = visited.get(ip) + 1;
            }
            visited.put(ip, curVisited);

            List<Integer> r = refs.get(ip);
            /*if (r != null) {
             if (r.size() > 1) {
             if (!stack.isEmpty()) {
             GraphTargetItem it = stack.pop();
             stack.push(new NotCompileTimeAVM2Item(null, it));
             }
             }
             }*/

            AVM2Instruction ins = code.get(ip);
            //Errorneous code inserted by some obfuscators
            if (ins.definition instanceof NewObjectIns) {
                if (ins.operands[0] > stack.size()) {
                    ins.setIgnored(true, 0);
                }
            }
            if (ins.definition instanceof NewArrayIns) {
                if (ins.operands[0] > stack.size()) {
                    ins.setIgnored(true, 0);
                }
            }

            if (ins.isIgnored()) {
                ip++;
                continue;
            }

            if (debugMode) {
                System.out.println((indeterminate ? "useV " : "") + (secondPass ? "secondPass " : "") + "Visit " + ip + ": " + ins + " stack:" + stack.toString());
                HashMap<Integer, GraphTargetItem> registers = localData.localRegs;
                System.out.print("Registers:");
                for (int reg : registers.keySet()) {
                    try {
                        System.out.print(" r" + reg + ": " + registers.get(reg).getResult());
                    } catch (NullPointerException npe) {
                        System.out.print(" r" + reg + ": " + "null");
                    }
                }
                System.out.println("");
            }
            if (secondPass) {
                /*if ((ins instanceof AVM2Instruction) && (((AVM2Instruction) ins).definition instanceof PopIns)) {
                 GraphTargetItem top = stack.peek();
                 for (GraphSourceItemPos p : top.getNeededSources()) {
                 if (p == null) {
                 continue;
                 }
                 if (p.item == null) {
                 continue;
                 }
                 if (p.item.isIgnored()) {
                 ins.setIgnored(true, 0);
                 break;
                 }
                 }
                 }*/
            }

            if (ins.definition instanceof NewFunctionIns) {
                stack.push(new BooleanAVM2Item(null, true));
            } else {
                localData.ip = ip;
                ins.translate(localData, stack, output, Graph.SOP_USE_STATIC, path);
            }

            if (ins.definition instanceof SetLocalTypeIns) {
                SetLocalTypeIns slt = (SetLocalTypeIns) ins.definition;
                int regId = slt.getRegisterId(ins);
                if (indeterminate) {
                    HashMap<Integer, GraphTargetItem> registers = localData.localRegs;
                    GraphTargetItem regVal = registers.get(regId);
                    if (regVal.isCompileTime()) {
                        registers.put(regId, new NotCompileTimeItem(null, regVal));
                    }
                }
            }

            if (ins.isExit()) {
                break;
            }

            if (ins.isBranch() || ins.isJump()) {
                List<Integer> branches = ins.getBranches(code);
                if (ins.definition instanceof IfTypeIns
                        && (!(ins.definition instanceof JumpIns))
                        && (!stack.isEmpty())
                        && (stack.peek().isCompileTime())
                        && (!stack.peek().hasSideEffect())) {
                    boolean condition = EcmaScript.toBoolean(stack.peek().getResult());
                    if (debugMode) {
                        if (condition) {
                            System.out.println("JUMP");
                        } else {
                            System.out.println("SKIP");
                        }
                    }
                    Decision dec = new Decision();
                    if (decisions.containsKey(ins)) {
                        dec = decisions.get(ins);
                    } else {
                        decisions.put(ins, dec);
                    }
                    if (condition) {
                        dec.jumpUsed = true;
                    } else {
                        dec.skipUsed = true;
                    }

                    if (branches.size() > 1) {
                        if (secondPass) {
                            if (condition && (dec.jumpUsed) && (!dec.skipUsed)) {
                                ins.setFixBranch(0);
                                //((AVM2Instruction) ins).definition = new JumpIns();
                            }
                            if ((!condition) && (!dec.jumpUsed) && (dec.skipUsed)) {
                                ins.setFixBranch(1);
                                //ins.setIgnored(true, 0);
                            }
                        }
                    }
                    GraphTargetItem tar = stack.pop();
                    /*if (secondPass && (dec.jumpUsed != dec.skipUsed)) {
                     for (GraphSourceItemPos pos : tar.getNeededSources()) {
                     if (pos.item instanceof AVM2Instruction) {
                     if (((AVM2Instruction) pos.item).definition instanceof DupIns) {
                     pos.item.setIgnored(true, 0);
                     break;
                     }
                     }
                     if (pos.item != ins) {
                     pos.item.setIgnored(true, 0);
                     }
                     }

                     }*/
                    if (branches.size() == 1) {
                        ip = branches.get(0);
                    } else {
                        ip = condition ? branches.get(0) : branches.get(1);
                    }
                    continue;
                } else {
                    if (ins.isBranch() && (!ins.isJump())) {
                        GraphTargetItem top = stack.pop();

                        Decision dec = new Decision();
                        if (decisions.containsKey(ins)) {
                            dec = decisions.get(ins);
                        } else {
                            decisions.put(ins, dec);
                        }
                        HashMap<Integer, GraphTargetItem> registers = localData.localRegs;
                        boolean regChanged = false;
                        if (!dec.registers.isEmpty()) {
                            if (dec.registers.size() != registers.size()) {
                                regChanged = true;
                            } else {
                                for (int reg : registers.keySet()) {
                                    if (!dec.registers.containsKey(reg)) {
                                        regChanged = true;
                                        break;
                                    }
                                    if (!registers.get(reg).isCompileTime() && dec.registers.get(reg).isCompileTime()) {
                                        regChanged = true;
                                        break;
                                    }
                                }
                            }
                        }
                        dec.registers.putAll(registers);
                        dec.jumpUsed = true;
                        dec.skipUsed = true;

                        if (!regChanged && ((!(top instanceof HasNextAVM2Item) && curVisited > 1) || (curVisited > 2))) {
                            for (int b : branches) {
                                int visc = 0;
                                if (visited.containsKey(b)) {
                                    visc = visited.get(b);
                                }
                                if (visc == 0) {//<curVisited){
                                    ip = b;
                                    continue iploop;
                                }
                            }
                            break;
                        }
                        indeterminate = true;
                    }

                    for (int b : branches) {
                        TranslateStack brStack = (TranslateStack) stack.clone();
                        if (b >= 0) { //useVisited || (!ins.isJump())
                            ret += removeTraps(refs, secondPass, indeterminate, prepareBranchLocalData(localData), brStack, output, code, b, visited, visitedStates, decisions, path, recursionLevel + 1);
                        } else {
                            if (debugMode) {
                                System.out.println("Negative branch:" + b);
                            }
                        }
                    }
                }
                break;
            }
            ip++;
        }
        if (ip < 0) {
            System.out.println("Visited Negative: " + ip);
        }
        return ret;
    }

    public static int removeTraps(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, AVM2LocalData localData, AVM2GraphSource code, int addr, String path, HashMap<Integer, List<Integer>> refs) throws InterruptedException {
        HashMap<AVM2Instruction, AVM2Code.Decision> decisions = new HashMap<>();
        removeTraps(refs, false, false, localData, new TranslateStack(), new ArrayList<GraphTargetItem>(), code, code.adr2pos(addr), new HashMap<Integer, Integer>(), new HashMap<Integer, HashMap<Integer, GraphTargetItem>>(), decisions, path, 0);
        int cnt = 0;
        for (AVM2Instruction src : decisions.keySet()) {
            Decision dec = decisions.get(src);
            if (dec != null) {
                if (src.definition instanceof LookupSwitchIns) {
                    if (dec.casesUsed.size() == 1) {
                        for (int c : dec.casesUsed) {
                            src.setFixBranch(c);
                            cnt++;
                        }
                    }
                } else {
                    if (dec.jumpUsed && !dec.skipUsed) {
                        src.setFixBranch(0);
                        cnt++;
                    }
                    if (!dec.jumpUsed && dec.skipUsed) {
                        src.setFixBranch(1);
                        cnt++;
                    }
                }
            }
        }
        //int cnt = removeTraps(refs, true, false, localData, new TranslateStack(), new ArrayList<GraphTargetItem>(), code, code.adr2pos(addr), new HashMap<Integer, Integer>(), new HashMap<Integer, HashMap<Integer, GraphTargetItem>>(), decisions, path);
        code.getCode().removeIgnored(constants, trait, info, body);
        return cnt;
    }
    /*public static int removeTraps(AVM2LocalData localData, AVM2GraphSource code, int addr) {
     AVM2Graph.translateViaGraph(localData, "", code, new ArrayList<Integer>(), Graph.SOP_REMOVE_STATIC);
     return 1;
     }*/

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        AVM2Code ret = (AVM2Code) super.clone();
        if (code != null) {
            List<AVM2Instruction> codeCopy = new ArrayList<>(code.size());
            for (AVM2Instruction ins : code) {
                codeCopy.add((AVM2Instruction) ins.clone());
            }
            ret.code = codeCopy;
        }
        return ret;
    }
}
