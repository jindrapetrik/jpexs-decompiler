/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorJumps;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorRegisters;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorSimple;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.AbsJumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.AddDIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.AddPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.AllocIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.BkptIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.BkptLineIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CallInterfaceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CallSuperIdIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CodeGenOpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CoerceBIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CoerceDIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CoerceIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CoerceOIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.CoerceUIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.ConcatIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.ConvertF4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.ConvertMIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.ConvertMPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DecLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DecodeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DecrementPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DelDescendantsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DeletePropertyLateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DividePIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DoubleToAtomIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.FindPropGlobalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.GetOuterScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.IncLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.IncrementPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.InvalidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.Lf32x4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.MarkIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.ModuloPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.MultiplyPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.NegatePIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.PrologueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.PushConstantIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.PushDNanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.PushDecimalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.PushFloat4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.SendEnterIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.SetPropertyLateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.Sf32x4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.SubtractPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.SweepIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.TimestampIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.VerifyOpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.VerifyPassIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.WbIns;
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
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForEachInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
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
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.ExitItem;
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
import java.util.Objects;
import java.util.Set;
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
        /*0x00*/null,
        /*0x01*/ new BkptIns(),
        /*0x02*/ new NopIns(),
        /*0x03*/ new ThrowIns(),
        /*0x04*/ new GetSuperIns(),
        /*0x05*/ new SetSuperIns(),
        /*0x06*/ new DXNSIns(),
        /*0x07*/ new DXNSLateIns(),
        /*0x08*/ new KillIns(),
        /*0x09*/ new LabelIns(),
        /*0x0A*/ new Lf32x4Ins(),
        /*0x0B*/ new Sf32x4Ins(),
        /*0x0C*/ new IfNLtIns(),
        /*0x0D*/ new IfNLeIns(),
        /*0x0E*/ new IfNGtIns(),
        /*0x0F*/ new IfNGeIns(),
        /*0x10*/ new JumpIns(),
        /*0x11*/ new IfTrueIns(),
        /*0x12*/ new IfFalseIns(),
        /*0x13*/ new IfEqIns(),
        /*0x14*/ new IfNeIns(),
        /*0x15*/ new IfLtIns(),
        /*0x16*/ new IfLeIns(),
        /*0x17*/ new IfGtIns(),
        /*0x18*/ new IfGeIns(),
        /*0x19*/ new IfStrictEqIns(),
        /*0x1A*/ new IfStrictNeIns(),
        /*0x1B*/ new LookupSwitchIns(),
        /*0x1C*/ new PushWithIns(),
        /*0x1D*/ new PopScopeIns(),
        /*0x1E*/ new NextNameIns(),
        /*0x1F*/ new HasNextIns(),
        /*0x20*/ new PushNullIns(),
        /*0x21*/ new PushUndefinedIns(),
        /*0x22*/ new PushConstantIns(), // new PushUninitializedIns()
        /*0x23*/ new NextValueIns(),
        /*0x24*/ new PushByteIns(),
        /*0x25*/ new PushShortIns(),
        /*0x26*/ new PushTrueIns(),
        /*0x27*/ new PushFalseIns(),
        /*0x28*/ new PushNanIns(),
        /*0x29*/ new PopIns(),
        /*0x2A*/ new DupIns(),
        /*0x2B*/ new SwapIns(),
        /*0x2C*/ new PushStringIns(),
        /*0x2D*/ new PushIntIns(),
        /*0x2E*/ new PushUIntIns(),
        /*0x2F*/ new PushDoubleIns(),
        /*0x30*/ new PushScopeIns(),
        /*0x31*/ new PushNamespaceIns(),
        /*0x32*/ new HasNext2Ins(),
        /*0x33*/ new PushDecimalIns(),
        /*0x34*/ new PushDNanIns(),
        /*0x35*/ new Li8Ins(),
        /*0x36*/ new Li16Ins(),
        /*0x37*/ new Li32Ins(),
        /*0x38*/ new Lf32Ins(),
        /*0x39*/ new Lf64Ins(),
        /*0x3A*/ new Si8Ins(),
        /*0x3B*/ new Si16Ins(),
        /*0x3C*/ new Si32Ins(),
        /*0x3D*/ new Sf32Ins(),
        /*0x3E*/ new Sf64Ins(),
        /*0x3F*/ null,
        /*0x40*/ new NewFunctionIns(),
        /*0x41*/ new CallIns(),
        /*0x42*/ new ConstructIns(),
        /*0x43*/ new CallMethodIns(),
        /*0x44*/ new CallStaticIns(),
        /*0x45*/ new CallSuperIns(),
        /*0x46*/ new CallPropertyIns(),
        /*0x47*/ new ReturnVoidIns(),
        /*0x48*/ new ReturnValueIns(),
        /*0x49*/ new ConstructSuperIns(),
        /*0x4A*/ new ConstructPropIns(),
        /*0x4B*/ new CallSuperIdIns(),
        /*0x4C*/ new CallPropLexIns(),
        /*0x4D*/ new CallInterfaceIns(),
        /*0x4E*/ new CallSuperVoidIns(),
        /*0x4F*/ new CallPropVoidIns(),
        /*0x50*/ new Sxi1Ins(),
        /*0x51*/ new Sxi8Ins(),
        /*0x52*/ new Sxi16Ins(),
        /*0x53*/ new ApplyTypeIns(),
        /*0x54*/ new PushFloat4Ins(),
        /*0x55*/ new NewObjectIns(),
        /*0x56*/ new NewArrayIns(),
        /*0x57*/ new NewActivationIns(),
        /*0x58*/ new NewClassIns(),
        /*0x59*/ new GetDescendantsIns(),
        /*0x5A*/ new NewCatchIns(),
        /*0x5B*/ new DelDescendantsIns(),
        /* // Duplicate OPCODE with deldescendants. Prefering deldescendants (found in FLEX compiler)
         new FindPropGlobalStrictIns(),*/
        /*0x5C*/ new FindPropGlobalIns(),
        /*0x5D*/ new FindPropertyStrictIns(),
        /*0x5E*/ new FindPropertyIns(),
        /*0x5F*/ new FindDefIns(),
        /*0x60*/ new GetLexIns(),
        /*0x61*/ new SetPropertyIns(),
        /*0x62*/ new GetLocalIns(),
        /*0x63*/ new SetLocalIns(),
        /*0x64*/ new GetGlobalScopeIns(),
        /*0x65*/ new GetScopeObjectIns(),
        /*0x66*/ new GetPropertyIns(),
        /*0x67*/ new GetOuterScopeIns(), // new GetPropertyLateIns()
        /*0x68*/ new InitPropertyIns(),
        /*0x69*/ new SetPropertyLateIns(),
        /*0x6A*/ new DeletePropertyIns(),
        /*0x6B*/ new DeletePropertyLateIns(),
        /*0x6C*/ new GetSlotIns(),
        /*0x6D*/ new SetSlotIns(),
        /*0x6E*/ new GetGlobalSlotIns(),
        /*0x6F*/ new SetGlobalSlotIns(),
        /*0x70*/ new ConvertSIns(),
        /*0x71*/ new EscXElemIns(),
        /*0x72*/ new EscXAttrIns(),
        /*0x73*/ new ConvertIIns(),
        /*0x74*/ new ConvertUIns(),
        /*0x75*/ new ConvertDIns(),
        /*0x76*/ new ConvertBIns(),
        /*0x77*/ new ConvertOIns(),
        /*0x78*/ new CheckFilterIns(),
        /*0x79*/ new ConvertMIns(),
        /*0x7A*/ new ConvertMPIns(),
        /*0x7B*/ new ConvertF4Ins(),
        /*0x7C*/ null,
        /*0x7D*/ null,
        /*0x7E*/ null,
        /*0x7F*/ null,
        /*0x80*/ new CoerceIns(),
        /*0x81*/ new CoerceBIns(),
        /*0x82*/ new CoerceAIns(),
        /*0x83*/ new CoerceIIns(),
        /*0x84*/ new CoerceDIns(),
        /*0x85*/ new CoerceSIns(),
        /*0x86*/ new AsTypeIns(),
        /*0x87*/ new AsTypeLateIns(),
        /*0x88*/ new CoerceUIns(),
        /*0x89*/ new CoerceOIns(),
        /*0x8A*/ null,
        /*0x8B*/ null,
        /*0x8C*/ null,
        /*0x8D*/ null,
        /*0x8E*/ null,
        /*0x8F*/ new NegatePIns(),
        /*0x90*/ new NegateIns(),
        /*0x91*/ new IncrementIns(),
        /*0x92*/ new IncLocalIns(),
        /*0x93*/ new DecrementIns(),
        /*0x94*/ new DecLocalIns(),
        /*0x95*/ new TypeOfIns(),
        /*0x96*/ new NotIns(),
        /*0x97*/ new BitNotIns(),
        /*0x98*/ null,
        /*0x99*/ null,
        /*0x9A*/ new ConcatIns(),
        /*0x9B*/ new AddDIns(),
        /*0x9C*/ new IncrementPIns(),
        /*0x9D*/ new IncLocalPIns(),
        /*0x9E*/ new DecrementPIns(),
        /*0x9F*/ new DecLocalPIns(),
        /*0xA0*/ new AddIns(),
        /*0xA1*/ new SubtractIns(),
        /*0xA2*/ new MultiplyIns(),
        /*0xA3*/ new DivideIns(),
        /*0xA4*/ new ModuloIns(),
        /*0xA5*/ new LShiftIns(),
        /*0xA6*/ new RShiftIns(),
        /*0xA7*/ new URShiftIns(),
        /*0xA8*/ new BitAndIns(),
        /*0xA9*/ new BitOrIns(),
        /*0xAA*/ new BitXorIns(),
        /*0xAB*/ new EqualsIns(),
        /*0xAC*/ new StrictEqualsIns(),
        /*0xAD*/ new LessThanIns(),
        /*0xAE*/ new LessEqualsIns(),
        /*0xAF*/ new GreaterThanIns(),
        /*0xB0*/ new GreaterEqualsIns(),
        /*0xB1*/ new InstanceOfIns(),
        /*0xB2*/ new IsTypeIns(),
        /*0xB3*/ new IsTypeLateIns(),
        /*0xB4*/ new InIns(),
        /*0xB5*/ new AddPIns(),
        /*0xB6*/ new SubtractPIns(),
        /*0xB7*/ new MultiplyPIns(),
        /*0xB8*/ new DividePIns(),
        /*0xB9*/ new ModuloPIns(),
        /*0xBA*/ null,
        /*0xBB*/ null,
        /*0xBC*/ null,
        /*0xBD*/ null,
        /*0xBE*/ null,
        /*0xBF*/ null,
        /*0xC0*/ new IncrementIIns(),
        /*0xC1*/ new DecrementIIns(),
        /*0xC2*/ new IncLocalIIns(),
        /*0xC3*/ new DecLocalIIns(),
        /*0xC4*/ new NegateIIns(),
        /*0xC5*/ new AddIIns(),
        /*0xC6*/ new SubtractIIns(),
        /*0xC7*/ new MultiplyIIns(),
        /*0xC8*/ null,
        /*0xC9*/ null,
        /*0xCA*/ null,
        /*0xCB*/ null,
        /*0xCC*/ null,
        /*0xCD*/ null,
        /*0xCE*/ null,
        /*0xCF*/ null,
        /*0xD0*/ new GetLocal0Ins(),
        /*0xD1*/ new GetLocal1Ins(),
        /*0xD2*/ new GetLocal2Ins(),
        /*0xD3*/ new GetLocal3Ins(),
        /*0xD4*/ new SetLocal0Ins(),
        /*0xD5*/ new SetLocal1Ins(),
        /*0xD6*/ new SetLocal2Ins(),
        /*0xD7*/ new SetLocal3Ins(),
        /*0xD8*/ null,
        /*0xD9*/ null,
        /*0xDA*/ null,
        /*0xDB*/ null,
        /*0xDC*/ null,
        /*0xDD*/ null,
        /*0xDE*/ null,
        /*0xDF*/ null,
        /*0xE0*/ null,
        /*0xE1*/ null,
        /*0xE2*/ null,
        /*0xE3*/ null,
        /*0xE4*/ null,
        /*0xE5*/ null,
        /*0xE6*/ null,
        /*0xE7*/ null,
        /*0xE8*/ null,
        /*0xE9*/ null,
        /*0xEA*/ null,
        /*0xEB*/ null,
        /*0xEC*/ null,
        /*0xED*/ new InvalidIns(),
        /*0xEE*/ new AbsJumpIns(),
        /*0xEF*/ new DebugIns(),
        /*0xF0*/ new DebugLineIns(),
        /*0xF1*/ new DebugFileIns(),
        /*0xF2*/ new BkptLineIns(),
        /*0xF3*/ new TimestampIns(),
        /*0xF4*/ null,
        /*0xF5*/ new VerifyPassIns(),
        /*0xF6*/ new AllocIns(),
        /*0xF7*/ new MarkIns(),
        /*0xF8*/ new WbIns(),
        /*0xF9*/ new PrologueIns(),
        /*0xFA*/ new SendEnterIns(),
        /*0xFB*/ new DoubleToAtomIns(),
        /*0xFC*/ new SweepIns(),
        /*0xFD*/ new CodeGenOpIns(),
        /*0xFE*/ new VerifyOpIns(),
        /*0xFF*/ new DecodeIns(),};
    // endoflist

    public boolean hideTemporaryRegisters = true;

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

    public void calculateDebugFileLine(ABC abc) {
        calculateDebugFileLine(null, 0, 0, abc, new HashSet<>());
    }

    private boolean calculateDebugFileLine(String debugFile, int debugLine, int pos, ABC abc, Set<Integer> seen) {
        while (pos < code.size()) {
            AVM2Instruction ins = code.get(pos);
            if (seen.contains(pos)) {
                return true;
            }

            seen.add(pos);

            if (ins.definition instanceof DebugFileIns) {
                debugFile = abc.constants.getString(ins.operands[0]);
            }

            if (ins.definition instanceof DebugLineIns) {
                debugLine = ins.operands[0];
            }

            ins.setFileLine(debugFile, debugLine);

            if (ins.definition instanceof NewFunctionIns) {
                //Only analyze NewFunction objects that are not immediately discarded by Pop.
                //This avoids bogus functions used in obfuscation or special compilers that can lead to infinite recursion.
                if ((pos + 1 < code.size()) && !(code.get(pos + 1).definition instanceof PopIns)) {
                    MethodBody innerBody = abc.findBody(ins.operands[0]);
                    if (innerBody != null) { //Ignore functions without body
                        innerBody.getCode().calculateDebugFileLine(debugFile, debugLine, 0, abc, new HashSet<>());
                    }
                }
            }

            if (ins.definition instanceof ReturnValueIns) {
                return true;
            }
            if (ins.definition instanceof ReturnVoidIns) {
                return true;
            }
            if (ins.definition instanceof JumpIns) {
                try {
                    pos = adr2pos(pos2adr(pos) + ins.getBytesLength() + ins.operands[0]);
                    continue;
                } catch (ConvertException ex) {
                    return false;
                }
            } else if (ins.definition instanceof IfTypeIns) {
                try {
                    int newpos = adr2pos(pos2adr(pos) + ins.getBytesLength() + ins.operands[0]);
                    calculateDebugFileLine(debugFile, debugLine, newpos, abc, seen);
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
                        if (!calculateDebugFileLine(debugFile, debugLine, newpos, abc, seen)) {
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

    /**
     * Removes nonexistent indices to constants from instruction operands.
     *
     * @param constants
     */
    public void removeWrongIndices(AVM2ConstantPool constants) {
        for (AVM2Instruction ins : code) {
            for (int i = 0; i < ins.definition.operands.length; i++) {
                if (ins.definition.operands[i] == DAT_MULTINAME_INDEX && ins.operands[i] >= constants.constant_multiname.size()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_DOUBLE_INDEX && ins.operands[i] >= constants.constant_double.size()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_INT_INDEX && ins.operands[i] >= constants.constant_int.size()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_UINT_INDEX && ins.operands[i] >= constants.constant_uint.size()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_STRING_INDEX && ins.operands[i] >= constants.constant_string.size()) {
                    ins.operands[i] = 0;
                }
            }
        }
    }

    public AVM2Code(ABCInputStream ais, MethodBody body) throws IOException {
        Map<Long, AVM2Instruction> codeMap = new HashMap<>();
        DumpInfo diParent = ais.dumpInfo;
        List<Long> addresses = new ArrayList<>();
        //Do not add new jumps when processing these addresses (unreachable code,etc.)
        List<Long> unAdresses = new ArrayList<>();
        //Handle lookupswitches at the end - they can be invalid. Handle other instruction first so we can decide lookupswitch to be invalid based on other instructions inside it
        //Flashplayer does not check casecount in lookupswitch instruction so the instruction can "be" long and over other instructions
        List<Long> switchAddresses = new ArrayList<>();
        int availableBytes = ais.available();
        for (int i = 0; i < availableBytes; i++) {
            codeMap.put((long) i, new AVM2Instruction(i, new NopIns(), null));
        }

        long startPos = ais.getPosition();
        addresses.add(startPos);
        if (body != null) {
            for (ABCException e : body.exceptions) {
                addresses.add((long) e.start);
                addresses.add((long) e.end);
                addresses.add((long) e.target);
            }
        }

        loopaddr:
        while (!addresses.isEmpty() || !switchAddresses.isEmpty() || !unAdresses.isEmpty()) {
            long address;
            boolean isSwitch = false;
            boolean handleJumps = true;
            if (!addresses.isEmpty()) {
                address = addresses.remove(0);
            } else if (!switchAddresses.isEmpty()) {
                address = switchAddresses.remove(0);
                isSwitch = true;
            } else {
                address = unAdresses.remove(0);
                handleJumps = false;
            }
            if (address < startPos) // no jump outside block
            {
                continue;
            }
            try {
                ais.seek(address);
                while (ais.available() > 0) {
                    DumpInfo di = ais.newDumpLevel("instruction", "instruction");
                    long startOffset = ais.getPosition();

                    if (codeMap.containsKey(startOffset) && !(codeMap.get(startOffset).definition instanceof NopIns)) {
                        continue loopaddr;
                    }

                    int instructionCode = ais.read("instructionCode");
                    InstructionDefinition instr = instructionSet[instructionCode];
                    if (instr instanceof LookupSwitchIns) {
                        if (!isSwitch) {
                            switchAddresses.add(startOffset);
                            continue loopaddr;
                        } else {
                            isSwitch = false;
                        }
                    }
                    if (di != null) {
                        di.name = instr.instructionName;
                    }
                    if (instr != null) {
                        int[] actualOperands = null;

                        if (instructionCode == 0x1b) { // switch
                            int firstOperand = ais.readS24("default_offset");
                            int case_count = ais.readU30("case_count");
                            long afterCasePos = ais.getPosition() + 3 * (case_count + 1);

                            boolean invalidSwitch = false;
                            //If there are already some instructions in the lookupswitch bytes, the lookupswitch is invalid (obfuscation)
                            for (long a = startOffset; a < afterCasePos; a++) {
                                if (codeMap.containsKey(a) && (!(codeMap.get(a).definition instanceof NopIns))) {
                                    invalidSwitch = true;
                                    break;
                                }
                            }

                            long totalBytes = ais.getPosition() + ais.available();

                            //If the lookupswitch case_count are larger than available bytes, the lookupswitch is invalid (obfuscation)
                            if (afterCasePos > totalBytes) {
                                invalidSwitch = true;
                            }
                            if (invalidSwitch) {
                                continue loopaddr;
                            } else {
                                actualOperands = new int[case_count + 3];
                                actualOperands[0] = firstOperand;
                                actualOperands[1] = case_count;
                                for (int c = 0; c < case_count + 1; c++) {
                                    actualOperands[2 + c] = ais.readS24("actualOperand");
                                }
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

                        AVM2Instruction ai = new AVM2Instruction(startOffset, instr, actualOperands);
                        long endOffset = ais.getPosition();

                        boolean hasRoom = true;
                        for (long p = startOffset; p < endOffset; p++) {
                            if (codeMap.containsKey(p) && !(codeMap.get(p).definition instanceof NopIns)) {
                                hasRoom = false;
                            }
                        }

                        //There is no room for this instruction (it is invalid?)
                        if (!hasRoom) {
                            continue loopaddr;
                        }
                        for (long p = startOffset; p < endOffset; p++) {
                            codeMap.put(p, ai);
                        }

                        ais.endDumpLevel(instr.instructionCode);

                        if ((instr instanceof IfTypeIns)) {
                            if (handleJumps) {
                                long target = ais.getPosition() + actualOperands[0];
                                addresses.add(target);
                            } else {
                                actualOperands[0] = 0;
                            }
                        }

                        if (instr instanceof JumpIns) {
                            if (handleJumps) {
                                long target = ais.getPosition() + actualOperands[0];
                                addresses.add(target);
                                unAdresses.add(ais.getPosition());
                                continue loopaddr;
                            } else {
                                actualOperands[0] = 0;
                            }
                        }

                        if (instr.isExitInstruction()) { //do not process jumps if there is return/throw instruction
                            if (handleJumps) {
                                unAdresses.add(ais.getPosition());
                                continue loopaddr;
                            }
                        }
                        if (instr instanceof LookupSwitchIns) {
                            if (handleJumps) {
                                addresses.add(startOffset + actualOperands[0]);

                                for (int c = 2; c < actualOperands.length; c++) {
                                    addresses.add(startOffset + actualOperands[c]);
                                }
                                unAdresses.add(ais.getPosition());
                                continue loopaddr;
                            } else {
                                int swlen = (int) (endOffset - startOffset);
                                actualOperands[0] = swlen;
                                for (int c = 2; c < actualOperands.length; c++) {
                                    actualOperands[c] = swlen;
                                }
                            }
                        }

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
        AVM2Instruction prev = null;
        for (int i = 0; i < availableBytes; i++) {
            AVM2Instruction ins = codeMap.get((long) i);
            if (prev != ins) {
                code.add(ins);
            }
            prev = ins;
        }
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
            s.append(Helper.newLine);
        }
        return s.toString();
    }

    public String toASMSource() {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        toASMSource(new AVM2ConstantPool(), null, null, null, new ArrayList<>(), ScriptExportMode.PCODE, writer);
        return writer.toString();
    }

    public GraphTextWriter toASMSource(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, ScriptExportMode exportMode, GraphTextWriter writer) {
        return toASMSource(constants, trait, info, body, new ArrayList<>(), exportMode, writer);
    }

    public GraphTextWriter toASMSource(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, List<Integer> outputMap, ScriptExportMode exportMode, GraphTextWriter writer) {
        invalidateCache();
        if (trait != null) {
            if (trait instanceof TraitFunction) {
                TraitFunction tf = (TraitFunction) trait;
                writer.appendNoHilight("trait ");
                writer.hilightSpecial("function ", HighlightSpecialType.TRAIT_TYPE);
                writer.hilightSpecial(constants.multinameToString(tf.name_index), HighlightSpecialType.TRAIT_NAME);
                writer.appendNoHilight(" slotid ");
                writer.hilightSpecial("" + tf.slot_id, HighlightSpecialType.SLOT_ID);
                writer.newLine();
            }
            if (trait instanceof TraitMethodGetterSetter) {
                TraitMethodGetterSetter tm = (TraitMethodGetterSetter) trait;
                writer.appendNoHilight("trait ");
                switch (tm.kindType) {
                    case Trait.TRAIT_METHOD:
                        writer.hilightSpecial("method ", HighlightSpecialType.TRAIT_TYPE);
                        break;
                    case Trait.TRAIT_GETTER:
                        writer.hilightSpecial("getter ", HighlightSpecialType.TRAIT_TYPE);
                        break;
                    case Trait.TRAIT_SETTER:
                        writer.hilightSpecial("setter ", HighlightSpecialType.TRAIT_TYPE);
                        break;
                }
                writer.hilightSpecial(constants.multinameToString(tm.name_index), HighlightSpecialType.TRAIT_NAME);
                writer.appendNoHilight(" dispid ");
                writer.hilightSpecial("" + tm.disp_id, HighlightSpecialType.DISP_ID);
                writer.newLine();
            }
        }

        if (info != null) {
            writer.appendNoHilight("method").newLine();
            writer.appendNoHilight("name ");
            writer.hilightSpecial(info.name_index == 0 ? "null" : "\"" + Helper.escapeActionScriptString(info.getName(constants)) + "\"", HighlightSpecialType.METHOD_NAME);
            writer.newLine();
            if (info.flagExplicit()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("EXPLICIT", HighlightSpecialType.FLAG_EXPLICIT);
                writer.newLine();
            }
            if (info.flagHas_optional()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("HAS_OPTIONAL", HighlightSpecialType.FLAG_HAS_OPTIONAL);
                writer.newLine();
                writer.appendNoHilight("flag HAS_OPTIONAL").newLine();
            }
            if (info.flagHas_paramnames()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("HAS_PARAM_NAMES", HighlightSpecialType.FLAG_HAS_PARAM_NAMES);
                writer.newLine();
            }
            if (info.flagIgnore_rest()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("EXPLICIT", HighlightSpecialType.FLAG_IGNORE_REST);
                writer.newLine();
            }
            if (info.flagNeed_activation()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("NEED_ACTIVATION", HighlightSpecialType.FLAG_NEED_ACTIVATION);
                writer.newLine();
            }
            if (info.flagNeed_arguments()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("NEED_ARGUMENTS", HighlightSpecialType.FLAG_NEED_ARGUMENTS);
                writer.newLine();
            }
            if (info.flagNeed_rest()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("NEED_REST", HighlightSpecialType.FLAG_NEED_REST);
                writer.newLine();
            }
            if (info.flagSetsdxns()) {
                writer.appendNoHilight("flag ");
                writer.hilightSpecial("SET_DXNS", HighlightSpecialType.FLAG_SET_DXNS);
                writer.newLine();
            }
            for (int p = 0; p < info.param_types.length; p++) {
                writer.appendNoHilight("param ");
                writer.hilightSpecial(constants.multinameToString(info.param_types[p]), HighlightSpecialType.PARAM, p);
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
                    writer.hilightSpecial(vk.toString(constants), HighlightSpecialType.OPTIONAL, i);
                    writer.newLine();
                }
            }
            writer.appendNoHilight("returns ");
            writer.hilightSpecial(constants.multinameToString(info.ret_type), HighlightSpecialType.RETURNS);
            writer.newLine();
        }
        writer.newLine();

        List<Long> offsets = new ArrayList<>();
        if (body != null) {
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
                writer.hilightSpecial(body.exceptions[e].type_index == 0 ? "null" : constants.getMultiname(body.exceptions[e].type_index).toString(constants, new ArrayList<>()), HighlightSpecialType.TRY_TYPE, e);

                writer.appendNoHilight(" name ");
                writer.hilightSpecial(body.exceptions[e].name_index == 0 ? "null" : constants.getMultiname(body.exceptions[e].name_index).toString(constants, new ArrayList<>()), HighlightSpecialType.TRY_NAME, e);
                writer.newLine();
            }
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
        } else if (exportMode == ScriptExportMode.PCODE || exportMode == ScriptExportMode.PCODE_HEX) {
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
                            writer.appendNoHilight(ins2.toStringNoAddress(constants, new ArrayList<>()) + " ;copy from " + Helper.formatAddress(pos2adr((Integer) o)));
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
                                if (fixBranch == 0) { // jump
                                    writer.appendNoHilight(new JumpIns().instructionName + " ofs" + Helper.formatAddress(ofs + ins.getBytesLength() + ins.operands[0]));
                                } else {
                                    // nojump, ignore
                                }
                            }
                            // TODO: lookupswitch ?
                        } else {
                            if (ins.changeJumpTo > -1) {
                                writer.appendNoHilight(ins.definition.instructionName + " ofs" + Helper.formatAddress(pos2adr(ins.changeJumpTo)));
                            } else {
                                writer.appendNoHilight(ins.toStringNoAddress(constants, new ArrayList<>()));
                            }
                        }
                        writer.newLine();
                        outputMap.add(ip);
                    }
                }
                ip++;
            }
        } else if (exportMode == ScriptExportMode.CONSTANTS) {
            writer.appendNoHilight("Constant export mode is not supported.").newLine();
        }

        return writer;
    }

    private boolean cacheActual = false;

    private List<Long> posCache;

    private void buildCache() {
        posCache = new ArrayList<>();
        long a = 0;
        for (int i = 0; i < code.size(); i++) {
            AVM2Instruction ins = code.get(i);
            posCache.add(ins.offset);
            a = ins.offset + ins.getBytesLength();
        }

        posCache.add(a);
        cacheActual = true;
    }

    public int adr2pos(long address) throws ConvertException {
        return adr2pos(address, false);
    }

    public int adr2pos(long address, boolean nearest) throws ConvertException {
        if (!cacheActual) {
            buildCache();
        }
        int ret = posCache.indexOf(address);
        if (ret == -1) {
            if (nearest) {
                for (long a : posCache) {
                    if (a > address) {
                        return posCache.indexOf(a);
                    }
                }
            }
            throw new ConvertException("Invalid jump to ofs" + Helper.formatAddress(address), -1);
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
     *
     * @param regName
     * @param start
     * @param end
     * @return
     */
    public boolean isKilled(int regName, int start, int end) {
        if (!killedRegs.containsKey(regName)) {
            return false;
        }
        for (int ip : killedRegs.get(regName)) {
            if (ip >= start && ip <= end) {
                return true;
            }
        }
        return false;
    }

    private int toSourceCount = 0;

    public Map<Integer, String> getLocalRegNamesFromDebug(ABC abc) {
        Map<Integer, String> localRegNames = new HashMap<>();

        for (AVM2Instruction ins : code) {
            if (ins.definition instanceof DebugIns) {
                if (ins.operands[0] == 1) {
                    String v = abc.constants.getString(ins.operands[1]);
                    // Same name already exists, it may be wrong names inserted by obfuscator
                    if (localRegNames.values().contains(v)) {
                        return new HashMap<>();
                    }
                    localRegNames.put(ins.operands[2] + 1, v);
                }
            }
        }

        // TODO: Make this immune to using existing multinames (?)
        return localRegNames;
    }

    private Map<Integer, Set<Integer>> killedRegs = new HashMap<>();

    public void calcKilledStats(MethodBody body) throws InterruptedException {
        killedRegs.clear();
        HashMap<Integer, List<Integer>> vis = visitCode(body);

        for (int k = 0; k < code.size(); k++) {
            if (vis.get(k).isEmpty()) {
                continue;
            }
            if (code.get(k).definition instanceof KillIns) {
                int regid = code.get(k).operands[0];
                if (!killedRegs.containsKey(regid)) {
                    killedRegs.put(regid, new HashSet<>());
                }
                killedRegs.get(regid).add(k);
            }
        }
    }

    public List<GraphTargetItem> clearTemporaryRegisters(List<GraphTargetItem> output) {
        for (int i = 0; i < output.size(); i++) {
            if (output.get(i) instanceof SetLocalAVM2Item) {
                if (isKilled(((SetLocalAVM2Item) output.get(i)).regIndex, 0, code.size() - 1)) {
                    SetLocalAVM2Item lsi = (SetLocalAVM2Item) output.get(i);
                    if (i + 1 < output.size()) {
                        if (output.get(i + 1) instanceof ExitItem) {
                            GraphTargetItem rv = output.get(i + 1);
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
        return pos2adr(fixIPAfterDebugLine(adr2pos(addr, true)));
    }

    public ConvertOutput toSourceOutput(String path, GraphPart part, boolean processJumps, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, ABC abc, AVM2ConstantPool constants, List<MethodInfo> method_info, MethodBody body, int start, int end, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, boolean[] visited, HashMap<Integer, Integer> localRegAssigmentIps, HashMap<Integer, List<Integer>> refs) throws ConvertException, InterruptedException {
        calcKilledStats(body);
        boolean debugMode = DEBUG_MODE;
        if (debugMode) {
            System.err.println("OPEN SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
        }
        if (visited == null) {
            visited = new boolean[code.size()];
        }
        //if(true) return "";
        toSourceCount++;
        if (toSourceLimit > 0) {
            if (toSourceCount >= toSourceLimit) {
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
                //logger.warning(path + ": Code already visited, ofs:" + Helper.formatAddress(pos2adr(ip)) + ", ip:" + ip);
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

            /*if ((ip + 2 < code.size()) && (ins.definition instanceof NewCatchIns)) { // Filling local register in catch clause
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
            } else if ((ins.definition instanceof SetLocalTypeIns) && (ip + 1 <= end) && (isKilled(((SetLocalTypeIns) ins.definition).getRegisterId(ins), ip, end))) { // set_local_x,get_local_x..kill x
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
                    AVM2Instruction insAfter = ip + 1 < code.size() ? code.get(ip + 1) : null;
                    if (insAfter == null) {
                        ins.definition.translate(isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc, localRegNames, fullyQualifiedNames, path, localRegAssigmentIps, ip, refs, this);
                        ip++;
                        break;
                    }
                    AVM2Instruction insBefore = ins;
                    if (ip - 1 >= start) {
                        insBefore = code.get(ip - 1);
                    }
                    if (insAfter.definition instanceof ConvertBIns) { // SWF compiled with debug contain convert_b
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
                        // chained assignments
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
                                            if (!assignment.output.isEmpty()) {
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
                        }
                        if (!isKilled(reg, 0, end)) {
                            GraphTargetItem vx = stack.pop().getThroughDuplicate();
                            int dupCnt = 1;
                            for (int i = ip - 1; i >= start; i--) {
                                if (code.get(i).definition instanceof DupIns) {
                                    if (stack.isEmpty()) {
                                        break; // FIXME?o
                                    }
                                    stack.pop();
                                    dupCnt++;
                                    //stack.push(v);
                                } else {
                                    break;
                                }
                            }
                            for (int i = 0; i < dupCnt; i++) {
                                stack.push(new LocalRegAVM2Item(ins, reg, vx));
                            }
                            stack.push(vx);
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
                                                    scopeStack.pop();// with
                                                    output.remove(output.size() - 1); // with
                                                    ip = ip + plus + 4; // +1 below
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // What to do when hasDup is false?
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
            System.err.println("CLOSE SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
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

    public HashMap<Integer, GraphTargetItem> getLocalRegTypes(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
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

        public final GraphTargetItem scope;

        public final Multiname multiname;

        public Slot(GraphTargetItem scope, Multiname multiname) {
            this.scope = scope;
            this.multiname = multiname;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Slot) {
                Slot slot = (Slot) obj;
                return (slot.scope.getThroughRegister() == scope.getThroughRegister())
                        && (slot.multiname == multiname);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (scope != null ? Objects.hashCode(scope.getThroughRegister()) : 0);
            hash = 59 * hash + Objects.hashCode(multiname);
            return hash;
        }
    }

    public void initToSource() {
        toSourceCount = 0;
        unknownJumps = new ArrayList<>();
        ignoredIns = new ArrayList<>();
    }

    private void injectDeclarations(List<GraphTargetItem> list, boolean[] declaredRegisters, List<Slot> declaredSlots, ABC abc, MethodBody body) {
        //List<Integer> nowdeclaredRegs=new ArrayList<>();
        //List<Slot> nowdeclaredSlots=new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem ti = list.get(i);
            if (ti instanceof ForEachInAVM2Item) {
                ForEachInAVM2Item fei = (ForEachInAVM2Item) ti;
                if (fei.expression.object instanceof LocalRegAVM2Item) {
                    int reg = ((LocalRegAVM2Item) fei.expression.object).regIndex;
                    if (!declaredRegisters[reg]) {
                        fei.expression.object = new DeclarationAVM2Item(fei.expression.object);
                        declaredRegisters[reg] = true;
                        //nowdeclaredRegs.add(reg);
                    }
                }
            }
            if (ti instanceof ForInAVM2Item) {
                ForInAVM2Item fi = (ForInAVM2Item) ti;
                if (fi.expression.object instanceof LocalRegAVM2Item) {
                    int reg = ((LocalRegAVM2Item) fi.expression.object).regIndex;
                    if (!declaredRegisters[reg]) {
                        fi.expression.object = new DeclarationAVM2Item(fi.expression.object);
                        declaredRegisters[reg] = true;
                        //nowdeclaredRegs.add(reg);
                    }
                }
            }
            if (ti instanceof Block) {
                Block bl = (Block) ti;
                for (List<GraphTargetItem> s : bl.getSubs()) {
                    injectDeclarations(s, declaredRegisters, declaredSlots, abc, body);
                }
            }
            if (ti instanceof SetLocalAVM2Item) {
                int reg = ((SetLocalAVM2Item) ti).regIndex;
                if (!declaredRegisters[reg]) {
                    list.set(i, new DeclarationAVM2Item(ti));
                    declaredRegisters[reg] = true;
                    //nowdeclaredRegs.add(reg);
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
                    //nowdeclaredSlots.add(sl);
                }
            }
        }

        /*
         //undeclare registers at the end of the block?
         for(int reg:nowdeclaredRegs){
         declaredRegisters[reg] = false;
         }

         for(Slot s:nowdeclaredSlots){
         declaredSlots.remove(s);
         }*/
    }

    public List<GraphTargetItem> toGraphTargetItems(String path, boolean isStatic, int scriptIndex, int classIndex, ABC abc, AVM2ConstantPool constants, List<MethodInfo> method_info, MethodBody body, HashMap<Integer, String> localRegNames, ScopeStack scopeStack, boolean isStaticInitializer, List<DottedChain> fullyQualifiedNames, Traits initTraits, int staticOperation, HashMap<Integer, Integer> localRegAssigmentIps, HashMap<Integer, List<Integer>> refs) throws InterruptedException {
        initToSource();
        List<GraphTargetItem> list;
        HashMap<Integer, GraphTargetItem> localRegs = new HashMap<>();

        int regCount = getRegisterCount();
        for (int i = 0; i < regCount; i++) {
            localRegs.put(0, new UndefinedAVM2Item(null));
        }

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
                    Multiname m = abc.constants.getMultiname(multinameIndex);
                    for (Trait t : initTraits.traits) {
                        Multiname tm = abc.constants.getMultiname(t.name_index);

                        if (tm != null && tm.equals(m)) {
                            if ((t instanceof TraitSlotConst)) {
                                if (((TraitSlotConst) t).isConst() || isStaticInitializer) {
                                    if ((((TraitSlotConst) t).assignedValue) == null) {
                                        ((TraitSlotConst) t).assignedValue = value;
                                        list.remove(i);
                                        i--;
                                        continue;
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else {
                    // In obfuscated code, SetLocal instructions comes first
                    //break;
                }
            }
        }
        if (isStaticInitializer) {
            List<GraphTargetItem> newList = new ArrayList<>();
            for (GraphTargetItem ti : list) {
                if (!(ti instanceof ReturnVoidAVM2Item)) {
                    if (!(ti instanceof InitPropertyAVM2Item)) {
                        newList.add(ti);
                    }
                }
            }
            list = newList;
            if (list.isEmpty()) {
                return list;
            }
        }
        // Declarations
        injectDeclarations(list, new boolean[regCount], new ArrayList<>(), abc, body);

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

    public void updateOffsets(OffsetUpdater updater, MethodBody body) {
        for (int i = 0; i < code.size(); i++) {
            AVM2Instruction ins = code.get(i);
            if (ins.definition instanceof LookupSwitchIns) {
                long target = ins.offset + ins.operands[0];
                ins.operands[0] = updater.updateOperandOffset(ins.offset, target, ins.operands[0]);
                for (int k = 2; k < ins.operands.length; k++) {
                    target = ins.offset + ins.operands[k];
                    ins.operands[k] = updater.updateOperandOffset(ins.offset, target, ins.operands[k]);
                }
            } else {
                /*for (int j = 0; j < ins.definition.operands.length; j++) {
                 if (ins.definition.operands[j] == AVM2Code.DAT_OFFSET) {
                 long target = ins.offset + ins.getBytes().length + ins.operands[j];
                 ins.operands[j] = updater.updateOperandOffset(target, ins.operands[j]);
                 }
                 }*/
                //Faster, but not so universal
                if ((ins.definition instanceof JumpIns) || (ins.definition instanceof IfTypeIns)) {
                    long target = ins.offset + ins.getBytesLength() + ins.operands[0];
                    try {
                        ins.operands[0] = updater.updateOperandOffset(ins.offset, target, ins.operands[0]);
                    } catch (ConvertException cex) {
                        throw new ConvertException("Invalid offset (" + ins + ")", i);
                    }
                }
            }
            ins.offset = updater.updateInstructionOffset(ins.offset);
        }

        for (ABCException ex : body.exceptions) {
            ex.start = updater.updateOperandOffset(-1, ex.start, ex.start);
            ex.end = updater.updateOperandOffset(-1, ex.end, ex.end);
            ex.target = updater.updateOperandOffset(-1, ex.target, ex.target);
        }
    }

    public void fixJumps(final String path, MethodBody body) throws InterruptedException {
        buildCache();
        if (code.isEmpty()) {
            return;
        }
        AVM2Instruction lastInstuction = code.get(code.size() - 1);
        final List<Long> insAddrToRemove = new ArrayList<>();
        final long endOffset = lastInstuction.offset + lastInstuction.getBytesLength();
        updateOffsets(new OffsetUpdater() {

            @Override
            public long updateInstructionOffset(long address) {
                return address;
            }

            @Override
            public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                if (targetAddress > endOffset || targetAddress < 0 || posCache.indexOf(targetAddress) == -1) {
                    insAddrToRemove.add(insAddr);
                }
                return offset;
            }

        }, body);

        boolean someIgnored = false;
        for (Long insAddr : insAddrToRemove) {
            int pos = posCache.indexOf(insAddr);
            if (pos > -1) {
                code.get(pos).ignored = true;
                someIgnored = true;
            }
        }
        if (someIgnored) {
            Logger.getLogger(AVM2Code.class.getName()).log(Level.WARNING, path + ": One or more invalid jump offsets found in the code. Those instructions were ignored.");
        }
        removeIgnored(body);
    }

    public void checkValidOffsets(MethodBody body) {
        invalidateCache();
        updateOffsets(new OffsetUpdater() {

            @Override
            public long updateInstructionOffset(long address) {
                adr2pos(address);
                return address;
            }

            @Override
            public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                if (insAddr == -1) { //do not check exceptions
                    return offset;
                }
                adr2pos(targetAddress);
                return offset;
            }

        }, body);
    }

    public void removeInstruction(int pos, MethodBody body) {
        if ((pos < 0) || (pos >= code.size())) {
            throw new IndexOutOfBoundsException();
        }

        AVM2Instruction ins = code.get(pos);
        final long remOffset = ins.offset;
        int bc = ins.getBytesLength();

        final int byteCount = bc;
        updateOffsets(new OffsetUpdater() {
            @Override
            public long updateInstructionOffset(long address) {
                if (address > remOffset) {
                    return address - byteCount;
                }
                return address;
            }

            @Override
            public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                if (targetAddress > remOffset && insAddr < remOffset) {
                    return offset - byteCount;
                }
                if (targetAddress <= remOffset && insAddr > remOffset) {
                    return offset + byteCount;
                }
                return offset;
            }
        }, body);
        code.remove(pos);
        invalidateCache();
        //checkValidOffsets(body);
    }

    /**
     * Inserts instruction at specified point. Handles offsets properly. Note:
     * If newinstruction is jump, the offset operand must be handled properly by
     * caller. All old jump offsets to pos are targeted before new instruction.
     *
     * @param pos Position in the list
     * @param instruction Instruction False means before new instruction
     * @param body Method body (used for try handling)
     */
    public void insertInstruction(int pos, AVM2Instruction instruction, MethodBody body) {
        insertInstruction(pos, instruction, false, body);
    }

    /**
     * Replaces instruction by another. Properly handles offsets. Note: If
     * newinstruction is jump, the offset operand must be handled properly by
     * caller.
     *
     * @param pos
     * @param instruction
     * @param body
     */
    public void replaceInstruction(int pos, AVM2Instruction instruction, MethodBody body) {
        if (pos < 0) {
            pos = 0;
        }
        if (pos > code.size()) {
            pos = code.size();
        }
        instruction.offset = code.get(pos).offset;
        int oldByteCount = code.get(pos).getBytesLength();
        int newByteCount = instruction.getBytesLength();
        int byteDelta = newByteCount - oldByteCount;

        if (byteDelta != 0) {
            updateOffsets(new OffsetUpdater() {

                @Override
                public long updateInstructionOffset(long address) {
                    if (address > instruction.offset) {
                        return address + byteDelta;
                    }
                    return address;
                }

                @Override
                public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                    if (targetAddress > instruction.offset && insAddr <= instruction.offset) {
                        return offset + byteDelta;
                    }
                    if (targetAddress <= instruction.offset && insAddr > instruction.offset) {
                        return offset - byteDelta;
                    }
                    return offset;
                }
            }, body);
        }
        code.set(pos, instruction);
        invalidateCache();
        //checkValidOffsets(body);
    }

    /**
     * Inserts instruction at specified point. Handles offsets properly. Note:
     * If newinstruction is jump, the offset operand must be handled properly by
     * caller.
     *
     * @param pos Position in the list
     * @param instruction Instruction
     * @param mapOffsetsAfterIns Map all jumps to the pos after new instruction?
     * False means before new instruction
     * @param body Method body (used for try handling)
     */
    public void insertInstruction(int pos, AVM2Instruction instruction, boolean mapOffsetsAfterIns, MethodBody body) {
        //checkValidOffsets(body);
        if (pos < 0) {
            pos = 0;
        }
        if (pos > code.size()) {
            pos = code.size();
        }
        final int byteCount = instruction.getBytesLength();
        if (pos == code.size()) {
            instruction.offset = code.get(pos - 1).offset + code.get(pos - 1).getBytesLength();
        } else {
            instruction.offset = code.get(pos).offset;
        }
        updateOffsets(new OffsetUpdater() {

            @Override
            public long updateInstructionOffset(long offset) {
                if (offset >= instruction.offset) {
                    return offset + byteCount;
                }
                return offset;
            }

            @Override
            public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                if (((targetAddress > instruction.offset) || (mapOffsetsAfterIns && (targetAddress == instruction.offset)))
                        && (insAddr < instruction.offset)) {
                    return offset + byteCount;
                }
                if (((targetAddress < instruction.offset) || (mapOffsetsAfterIns && (targetAddress == instruction.offset)))
                        && (insAddr > instruction.offset)) {
                    return offset - byteCount;
                }
                return offset;
            }
        }, body);
        code.add(pos, instruction);
        invalidateCache();
        //checkValidOffsets(body);
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
        ret.ignoredSwitches2 = localData.ignoredSwitches2;
        ret.scriptIndex = localData.scriptIndex;
        ret.localRegAssignmentIps = localData.localRegAssignmentIps;
        ret.ip = localData.ip;
        ret.refs = localData.refs;
        ret.code = localData.code;
        return ret;
    }

    private int removeTrapsOld(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, ABC abc, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        removeDeadCode(body);
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
        localData.finallyJumps = new HashMap<>();
        localData.ignoredSwitches = new HashMap<>();
        localData.ignoredSwitches2 = new ArrayList<>();
        localData.scriptIndex = scriptIndex;
        localData.localRegAssignmentIps = new HashMap<>();
        localData.ip = 0;
        HashMap<Integer, List<Integer>> refs = visitCode(body);
        localData.refs = refs;
        localData.code = this;
        int ret = 0;
        ret += removeTrapsOld(constants, trait, info, body, localData, new AVM2GraphSource(this, false, -1, -1, new HashMap<>(), new ScopeStack(), abc, body, new HashMap<>(), new ArrayList<>(), new HashMap<>(), refs), 0, path, refs);
        removeIgnored(body);
        removeDeadCode(body);

        return ret;
    }

    public int removeTraps(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, ABC abc, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        if (Configuration.deobfuscationOldMode.get()) {
            return removeTrapsOld(constants, trait, info, body, abc, scriptIndex, classIndex, isStatic, path);
        } else {
            new AVM2DeobfuscatorSimple().deobfuscate(path, classIndex, isStatic, scriptIndex, abc, constants, trait, info, body);
            new AVM2DeobfuscatorRegisters().deobfuscate(path, classIndex, isStatic, scriptIndex, abc, constants, trait, info, body);
            new AVM2DeobfuscatorJumps().deobfuscate(path, classIndex, isStatic, scriptIndex, abc, constants, trait, info, body);
            return 1;
        }
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
                // check stack mismatch here
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
                // check stack=1
                return true;
            }
            if (ins.definition instanceof ReturnVoidIns) {
                // check stack=0
                return true;
            }
            if (ins.definition instanceof JumpIns) {
                try {
                    pos = adr2pos(pos2adr(pos) + ins.getBytesLength() + ins.operands[0]);
                    continue;
                } catch (ConvertException ex) {
                    return false;
                }
            } else if (ins.definition instanceof IfTypeIns) {
                try {
                    int newpos = adr2pos(pos2adr(pos) + ins.getBytesLength() + ins.operands[0]);
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
                // searching for visited instruction in second run which has maximum position
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
                        // Rerun rest with new scopePos, stackPos
                        if (!walkCode(stats, nextIp, origStackPos + 1/*magic!*/, scopePos - 1 /*magic!*/, abc)) {
                            return null;
                        }
                        scopePos--;
                    }
                }
                prevStart = ex.start;
            } catch (ConvertException ex1) {
                // ignore
            }
        }
        //stats.maxscope+=initScope;
        return stats;
    }

    private void visitCode(int ip, int lastIp, HashMap<Integer, List<Integer>> refs) throws InterruptedException {
        List<Integer> toVisit = new ArrayList<>();
        List<Integer> toVisitLast = new ArrayList<>();
        toVisit.add(ip);
        toVisitLast.add(lastIp);
        while (!toVisit.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            ip = toVisit.remove(0);
            lastIp = toVisitLast.remove(0);
            while (ip < code.size()) {
                if (!refs.containsKey(ip)) {
                    refs.put(ip, new ArrayList<>());
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
                            toVisit.add(adr2pos(pos2adr(ip) + ins.operands[i]));
                            toVisitLast.add(ip);
                            //visitCode(, ip, refs);
                        }
                        ip = adr2pos(pos2adr(ip) + ins.operands[0]);
                        continue;
                    } catch (ConvertException ex) {
                    }
                }
                if (ins.definition instanceof JumpIns) {
                    try {
                        ip = adr2pos(pos2adr(ip) + ins.getBytesLength() + ins.operands[0]);
                        continue;
                    } catch (ConvertException ex) {
                        logger.log(Level.FINE, null, ex);
                    }
                } else if (ins.definition instanceof IfTypeIns) {
                    try {
                        toVisit.add(adr2pos(pos2adr(ip) + ins.getBytesLength() + ins.operands[0]));
                        toVisitLast.add(ip);
                        //visitCode(adr2pos(pos2adr(ip) + ins.getBytesLength() + ins.operands[0]), ip, refs);
                    } catch (ConvertException ex) {
                        logger.log(Level.FINE, null, ex);
                    }
                }
                ip++;
            }
        };
    }

    public HashMap<Integer, List<Integer>> visitCode(MethodBody body) throws InterruptedException {
        HashMap<Integer, List<Integer>> refs = new HashMap<>();
        for (int i = 0; i < code.size(); i++) {
            refs.put(i, new ArrayList<>());
        }
        visitCode(0, 0, refs);
        int pos = 0;
        for (ABCException e : body.exceptions) {
            pos++;
            try {
                visitCode(adr2pos(e.start, true), adr2pos(e.start, true) - 1, refs);
                visitCode(adr2pos(e.start, true), -1, refs);
                visitCode(adr2pos(e.target), adr2pos(e.end, true), refs);
                visitCode(adr2pos(e.end, true), -pos, refs);
            } catch (ConvertException ex) {
                logger.log(Level.SEVERE, "Visitcode error", ex);
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
                    ip = adr2pos(pos2adr(ip) + ins.getBytesLength() + ins.operands[0]);
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
                    ret += visitCodeTrap(adr2pos(pos2adr(ip) + ins.getBytesLength() + ins.operands[0]), visited, prev, prev2);
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
                        restoreControlFlow(adr2pos(e.start, true), refs, visited2, appended);
                        restoreControlFlow(adr2pos(e.target), refs, visited2, appended);
                        restoreControlFlow(adr2pos(e.end, true), refs, visited2, appended);
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
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
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
        removeDeadCode(body);
    }

    public void restoreControlFlow(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body) throws InterruptedException {
        restoreControlFlowPass(constants, trait, info, body, false);
        //restoreControlFlowPass(constants, body, true);
    }
    /*
     public void removeIgnored(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body) throws InterruptedException {
     try {
     List<Integer> outputMap = new ArrayList<>();
     HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
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
     }*/

    public void removeIgnored(MethodBody body) throws InterruptedException {
        //System.err.println("removing ignored...");
        for (int i = 0; i < code.size(); i++) {
            if (code.get(i).ignored) {
                removeInstruction(i, body);
                i--;
            }
        }
        //System.err.println("/ignored removed");
    }

    public int removeDeadCode(MethodBody body) throws InterruptedException {
        invalidateCache();
        HashMap<Integer, List<Integer>> refs = visitCode(body);
        int cnt = 0;
        for (int i = code.size() - 1; i >= 0; i--) {
            if (refs.get(i).isEmpty()) {
                code.get(i).ignored = true;
                //removeInstruction(i, body);
                cnt++;
            }
        }

        removeIgnored(body);

        for (int i = code.size() - 1; i >= 0; i--) {
            AVM2Instruction ins = code.get(i);
            if (ins.definition instanceof JumpIns) {
                if (ins.operands[0] == 0) {
                    ins.ignored = true;
                    cnt++;
                }
            }
        }

        removeIgnored(body);

        return cnt;
    }

    public void inlineJumpExit() {
        int csize = code.size();
        for (int i = 0; i < csize; i++) {
            AVM2Instruction ins = code.get(i);
            int insLen = code.get(i).getBytesLength();
            int ofs = pos2adr(i);
            if (ins.definition instanceof JumpIns) {
                int targetOfs = ofs + insLen + ins.operands[0];
                try {
                    int ni = adr2pos(targetOfs);
                    if (ni < code.size() && ni > -1) {
                        AVM2Instruction ins2 = code.get(ni);
                        if (ins2.isExit()) {
                            code.set(i, new AVM2Instruction(ofs, ins2.definition, ins2.operands));
                            AVM2Instruction nopIns;
                            nopIns = new AVM2Instruction(ofs + 1, new NopIns(), null);
                            code.add(i + 1, nopIns);
                            nopIns = new AVM2Instruction(ofs + 2, new NopIns(), null);
                            code.add(i + 2, nopIns);
                            nopIns = new AVM2Instruction(ofs + 3, new NopIns(), null);
                            code.add(i + 3, nopIns);
                            i += 3;
                            csize = code.size();
                            buildCache();
                        }
                    }
                } catch (ConvertException ex) {
                    //ignore
                }
            }
            //ofs += insLen;
        }
    }

    public void markMappedOffsets() {
        int ofs = 0;
        for (int i = 0; i < code.size(); i++) {
            code.get(i).mappedOffset = ofs;
            ofs += code.get(i).getBytesLength();
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
        return isDirectAncestor(currentIp, ancestor, refs, new ArrayList<>());
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
    private static int removeTrapsOld(HashMap<Integer, List<Integer>> refs, boolean secondPass, boolean indeterminate, AVM2LocalData localData, TranslateStack stack, List<GraphTargetItem> output, AVM2GraphSource code, int ip, HashMap<Integer, Integer> visited, HashMap<Integer, HashMap<Integer, GraphTargetItem>> visitedStates, HashMap<AVM2Instruction, Decision> decisions, String path, int recursionLevel) throws InterruptedException {
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
            // Errorneous code inserted by some obfuscators
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
                            ret += removeTrapsOld(refs, secondPass, indeterminate, prepareBranchLocalData(localData), brStack, output, code, b, visited, visitedStates, decisions, path, recursionLevel + 1);
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
        if (ip < 0 && debugMode) {
            System.out.println("Visited Negative: " + ip);
        }
        return ret;
    }

    public static int removeTrapsOld(AVM2ConstantPool constants, Trait trait, MethodInfo info, MethodBody body, AVM2LocalData localData, AVM2GraphSource code, int addr, String path, HashMap<Integer, List<Integer>> refs) throws InterruptedException {
        HashMap<AVM2Instruction, AVM2Code.Decision> decisions = new HashMap<>();
        removeTrapsOld(refs, false, false, localData, new TranslateStack(path), new ArrayList<>(), code, code.adr2pos(addr), new HashMap<>(), new HashMap<>(), decisions, path, 0);
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
        code.getCode().removeIgnored(body);
        return cnt;
    }
    /*public static int removeTraps(AVM2LocalData localData, AVM2GraphSource code, int addr) {
     AVM2Graph.translateViaGraph(localData, "", code, new ArrayList<Integer>(), Graph.SOP_REMOVE_STATIC);
     return 1;
     }*/

    @Override
    public AVM2Code clone() {
        try {
            AVM2Code ret = (AVM2Code) super.clone();
            if (code != null) {
                List<AVM2Instruction> codeCopy = new ArrayList<>(code.size());
                for (AVM2Instruction ins : code) {
                    codeCopy.add(ins.clone());
                }
                ret.code = codeCopy;
            }

            ret.cacheActual = false;
            ret.ignoredIns = new ArrayList<>();
            ret.killedRegs = new HashMap<>();
            ret.unknownJumps = new ArrayList<>();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
