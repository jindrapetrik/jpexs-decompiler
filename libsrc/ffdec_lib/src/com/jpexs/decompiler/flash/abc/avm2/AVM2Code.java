/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorGetSet;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorJumps;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorRegistersOld;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorSimpleOld;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorZeroJumpsNullPushes;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2VerifyErrorException;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphSource;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.UnknownInstruction;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.BkptIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.BkptLineIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.DeletePropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindDefIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetDescendantsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetGlobalScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetGlobalSlotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetOuterScopeIns;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.TimestampIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.AddPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.ConvertMIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.ConvertMPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.DecLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.DecrementPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.DividePIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.IncLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.IncrementPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.ModuloPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.MultiplyPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.NegatePIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.PushDNanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.PushDecimalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.SubtractPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.deprecated.CoerceBIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.deprecated.CoerceDIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.deprecated.CoerceIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.deprecated.CoerceUIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.floatsupport.ConvertF4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.floatsupport.ConvertFIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.floatsupport.Lf32x4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.floatsupport.PushFloat4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.floatsupport.PushFloatIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.floatsupport.Sf32x4Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.floatsupport.UnPlusIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.AbsJumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.AddDIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.AllocIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.CallInterfaceIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.CallSuperIdIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.CodeGenOpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.ConcatIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.DecodeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.DelDescendantsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.DeletePropertyLateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.DoubleToAtomIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.InvalidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.MarkIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.PrologueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.SendEnterIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.SetPropertyLateIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.SweepIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.VerifyOpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.VerifyPassIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.unknown.WbIns;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceOIns;
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
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GlobalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PackageAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StoreNewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.TraitSlotConstAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForEachInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.AssignedValue;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.AbstractGraphTargetRecursiveVisitor;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.SecondPassException;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.LinkedIdentityHashSet;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.ReflectionTools;
import com.jpexs.helpers.stat.Statistics;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing AVM2 code inside a method body.
 *
 * @author JPEXS
 */
public class AVM2Code implements Cloneable {

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(AVM2Code.class.getName());

    /**
     * Debug mode - prints debug information.
     */
    private static final boolean DEBUG_MODE = false;

    /**
     * Debug testing whether same code is write to output stream.
     */
    public static boolean DEBUG_REWRITE = false;

    /**
     * Limit of toSource calls
     */
    public static int toSourceLimit = -1;

    /**
     * List of instructions in this code.
     */
    public List<AVM2Instruction> code;

    /**
     * U30 value (unsigned up to 30-bit integer)
     */
    public static final int OPT_U30 = 0x100;

    /**
     * U8 value (unsigned 8 bit integer)
     */
    public static final int OPT_U8 = 0x200;

    /**
     * S24 value (signed 24 bit integer)
     */
    public static final int OPT_S24 = 0x300;

    /**
     * Case offsets
     */
    public static final int OPT_CASE_OFFSETS = 0x400;

    /**
     * S8 value (signed 8 bit integer)
     */
    public static final int OPT_S8 = 0x500;

    /**
     * S16 value (signed 16 bit integer)
     */
    public static final int OPT_S16 = 0x600;

    /**
     * Multiname index data
     */
    public static final int DAT_MULTINAME_INDEX = OPT_U30 + 0x01;

    /**
     * Argument count data
     */
    public static final int DAT_ARG_COUNT = OPT_U30 + 0x02;

    /**
     * Method index data
     */
    public static final int DAT_METHOD_INDEX = OPT_U30 + 0x03;

    /**
     * String index data
     */
    public static final int DAT_STRING_INDEX = OPT_U30 + 0x04;

    /**
     * Debug type data
     */
    public static final int DAT_DEBUG_TYPE = OPT_U8 + 0x05;

    /**
     * Register index data
     */
    public static final int DAT_REGISTER_INDEX = OPT_U8 + 0x06;

    /**
     * Line number data
     */
    public static final int DAT_LINENUM = OPT_U30 + 0x07;

    /**
     * Local register index data
     */
    public static final int DAT_LOCAL_REG_INDEX = OPT_U30 + 0x08;

    /**
     * Slot index data
     */
    public static final int DAT_SLOT_INDEX = OPT_U30 + 0x09;

    /**
     * Scope index data
     */
    public static final int DAT_SCOPE_INDEX = OPT_U30 + 0x0A;

    /**
     * Offset data
     */
    public static final int DAT_OFFSET = OPT_S24 + 0x0B;

    /**
     * Exception index data
     */
    public static final int DAT_EXCEPTION_INDEX = OPT_U30 + 0x0C;

    /**
     * Class index data
     */
    public static final int DAT_CLASS_INDEX = OPT_U30 + 0x0D;

    /**
     * Integer index data
     */
    public static final int DAT_INT_INDEX = OPT_U30 + 0x0E;

    /**
     * Unsigned integer index data
     */
    public static final int DAT_UINT_INDEX = OPT_U30 + 0x0F;

    /**
     * Double index data
     */
    public static final int DAT_DOUBLE_INDEX = OPT_U30 + 0x10;

    /**
     * Decimal index data
     */
    public static final int DAT_DECIMAL_INDEX = OPT_U30 + 0x11;

    /**
     * Case base offset data
     */
    public static final int DAT_CASE_BASEOFFSET = OPT_S24 + 0x12;

    /**
     * Number context data
     */
    public static final int DAT_NUMBER_CONTEXT = OPT_U30 + 0x13;

    /**
     * Dispatch ID data
     */
    public static final int DAT_DISPATCH_ID = OPT_U30 + 0x14;

    /**
     * Float index data
     */
    public static final int DAT_FLOAT_INDEX = OPT_U30 + 0x15;

    /**
     * Float4 index data
     */
    public static final int DAT_FLOAT4_INDEX = OPT_U30 + 0x16;

    /**
     * Namespace index data
     */
    public static final int DAT_NAMESPACE_INDEX = OPT_U30 + 0x17;

    /**
     * Map of operand type identifiers
     */
    private static Map<Integer, String> operandDataTypeIdentifiers = ReflectionTools.getConstNamesMap(AVM2Code.class, Integer.class, "^DAT_(.*)$");

    /**
     * Instruction aliases array
     */
    private static final String[][] instructionAliasesArray = {
        //first is original name, then aliases
        {"getlocal0", "getlocal_0"},
        {"getlocal1", "getlocal_1"},
        {"getlocal2", "getlocal_2"},
        {"getlocal3", "getlocal_3"},
        {"setlocal0", "setlocal_0"},
        {"setlocal1", "setlocal_1"},
        {"setlocal2", "setlocal_2"},
        {"setlocal3", "setlocal_3"}
    };

    /**
     * Instruction aliases map
     */
    public static final Map<String, String> instructionAliases = new HashMap<>();

    static {
        for (String[] aliases : instructionAliasesArray) {
            for (int s = 1; s < aliases.length; s++) {
                instructionAliases.put(aliases[s], aliases[0]);
            }
        }
    }

    /**
     * Instruction set
     */
    public static final InstructionDefinition[] instructionSet = new InstructionDefinition[256];

    /**
     * All instruction set
     */
    public static final InstructionDefinition[] allInstructionSet = new InstructionDefinition[]{
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
        /*0x22*/ new PushFloatIns(), //major 47+
        /*0x22*/ //new PushConstantIns(), //before major 47
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
        /*0x33*/ new PushDecimalIns(), //pushdecimal(minor 17), lix8 (internal-only) according to Tamarin
        /*0x34*/ new PushDNanIns(), //pushdnan according to Flex SDK, lix16 (internal-only) according to Tamarin
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
        /*0x54*/ new PushFloat4Ins(), //major 47+
        /*0x55*/ new NewObjectIns(),
        /*0x56*/ new NewArrayIns(),
        /*0x57*/ new NewActivationIns(),
        /*0x58*/ new NewClassIns(),
        /*0x59*/ new GetDescendantsIns(),
        /*0x5A*/ new NewCatchIns(),
        /*0x5B*/ new DelDescendantsIns(), //deldescendants according to Flex, findpropglobalstrict(internal-only) according to Tamarin
        /*0x5C*/ //new FindPropGlobalIns(), //Tamarin (internal-only)
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
        /*0x79*/ new ConvertMIns(), //minor 17 (Flex)
        /*0x79*/ new ConvertFIns(), //major 47+, SWF 15+
        /*0x7A*/ new ConvertMPIns(), //minor 17 (Flex)
        /*0x7A*/ new UnPlusIns(), //major 47+, SWF 15+
        /*0x7B*/ new ConvertF4Ins(), //major 47+, SWF 15+
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
        /*0xFF*/ new DecodeIns()
    };
    // endoflist

    static {

        for (int i = 0; i < allInstructionSet.length; i++) {
            if (allInstructionSet[i] != null) {
                int opCode = allInstructionSet[i].instructionCode;
                if (instructionSet[opCode] == null) {
                    instructionSet[opCode] = allInstructionSet[i];
                } else if (instructionSet[opCode].hasFlag(AVM2InstructionFlag.NO_FLASH_PLAYER) && !allInstructionSet[i].hasFlag(AVM2InstructionFlag.NO_FLASH_PLAYER)) {
                    instructionSet[opCode] = allInstructionSet[i];
                } else if (instructionSet[opCode].hasFlag(AVM2InstructionFlag.ES4_NUMERICS_MINOR) && !allInstructionSet[i].hasFlag(AVM2InstructionFlag.ES4_NUMERICS_MINOR)) { //Prefer without decimal:
                    instructionSet[opCode] = allInstructionSet[i];
                } else if (instructionSet[opCode].hasFlag(AVM2InstructionFlag.FLOAT_MAJOR) && !allInstructionSet[i].hasFlag(AVM2InstructionFlag.FLOAT_MAJOR)) { //Prefer without float:
                    instructionSet[opCode] = allInstructionSet[i];
                }
            }
        }

        for (int i = 0;
                i < instructionSet.length;
                i++) {
            if (instructionSet[i] == null) {
                instructionSet[i] = new UnknownInstruction(i);
            }
        }

    }

    /**
     * To source call counter.
     */
    private int toSourceCount = 0;

    /**
     * Converts operand type to string.
     *
     * @param ot Operand type
     * @return Operand type as string
     */
    public static String operandTypeSizeToString(int ot) {
        int sizeType = ot & 0xff00;
        switch (sizeType) {
            case OPT_U30:
                return "U30";
            case OPT_S16:
                return "S16";
            case OPT_U8:
                return "U8";
            case OPT_S8:
                return "S8";
            case OPT_S24:
                return "S24";
            case OPT_CASE_OFFSETS:
                return "S24(=n), S24[n]";
        }
        return "";
    }

    /**
     * Converts operand type to string.
     *
     * @param ot Operand type
     * @param withTypeSize Whether to include type size
     * @return Operand type as string
     */
    public static String operandTypeToString(int ot, boolean withTypeSize) {
        String typeSize = operandTypeSizeToString(ot);
        if (ot == OPT_CASE_OFFSETS) {
            return "number" + (withTypeSize ? "(U30)" : "") + ", offset" + (withTypeSize ? "(S24)" : "") + ", offset" + (withTypeSize ? "(S24)" : "") + ", ...";
        }
        if (operandDataTypeIdentifiers.containsKey(ot)) {
            String dataType = operandDataTypeIdentifiers.get(ot);
            return dataType + (withTypeSize ? "(" + typeSize + ")" : "");
        } else {
            return typeSize;
        }

    }

    /**
     * Constructs AVM2Code object.
     */
    public AVM2Code() {
        code = new ArrayList<>();
    }

    /**
     * Constructs AVM2Code object.
     *
     * @param capacity Capacity
     */
    public AVM2Code(int capacity) {
        code = new ArrayList<>(capacity);
    }

    /**
     * Constructs AVM2Code object.
     *
     * @param instructions List of instructions
     */
    public AVM2Code(ArrayList<AVM2Instruction> instructions) {
        code = instructions;
    }

    /**
     * Executes the code.
     *
     * @param arguments Local registers values
     * @param constants Constant pool
     * @return Result of the execution
     * @throws AVM2ExecutionException On execution error
     */
    public Object execute(HashMap<Integer, Object> arguments, AVM2ConstantPool constants) throws AVM2ExecutionException {
        return execute(arguments, constants, null);
    }

    /**
     * Executes the code.
     *
     * @param arguments Local registers values
     * @param constants Constant pool
     * @param runtimeInfo Runtime information
     * @return Result of the execution
     * @throws AVM2ExecutionException On execution error
     */
    public Object execute(HashMap<Integer, Object> arguments, AVM2ConstantPool constants, AVM2RuntimeInfo runtimeInfo) throws AVM2ExecutionException {
        int pos = 0;
        LocalDataArea lda = new LocalDataArea();
        lda.methodName = "methodName"; // todo: needed for VerifyError exception message
        lda.localRegisters = arguments;
        lda.runtimeInfo = runtimeInfo;

        for (AVM2Instruction ins : code) {
            if (!(ins.definition instanceof CallSuperVoidIns)) {
                ins.definition.verify(lda, constants, ins);
            }
        }

        while (pos < code.size()) {
            AVM2Instruction ins = code.get(pos);
            if (!ins.definition.execute(lda, constants, ins)) {
                return null;
            }

            if (lda.jump != null) {
                try {
                    pos = adr2pos(lda.jump);
                } catch (ConvertException ex) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.BRANCH_TARGET_INVALID_INSTRUCTION, lda.isDebug());
                }
                lda.jump = null;
            } else {
                pos++;
            }

            if (lda.returnValue != null) {
                return lda.returnValue;
            }
        }

        return Undefined.INSTANCE;
    }

    /**
     * Calculates the line debug file/line info and sets it to the instructions.
     *
     * @param abc ABC
     */
    public void calculateDebugFileLine(ABC abc) {
        calculateDebugFileLine(null, 0, 0, abc, new HashSet<>(), new HashSet<>());
    }

    /**
     * Calculates the line debug file/line info and sets it to the instructions.
     *
     * @param debugFile Debug file
     * @param debugLine Debug line
     * @param pos Position
     * @param abc ABC
     * @param seen Seen instructions
     * @param seenMethods Seen methods
     * @return True of seen.
     */
    private boolean calculateDebugFileLine(String debugFile, int debugLine, int pos, ABC abc, Set<Integer> seen, Set<Integer> seenMethods) {
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
                    int newMethodInfo = ins.operands[0];
                    if (!seenMethods.contains(newMethodInfo)) { //avoid recursion
                        MethodBody innerBody = abc.findBody(newMethodInfo);
                        if (innerBody != null) { //Ignore functions without body
                            seenMethods.add(newMethodInfo);
                            innerBody.getCode().calculateDebugFileLine(debugFile, debugLine, 0, abc, new HashSet<>(), seenMethods);
                        }
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
                    pos = adr2pos(ins.getTargetAddress());
                    continue;
                } catch (ConvertException ex) {
                    return false;
                }
            } else if (ins.definition instanceof IfTypeIns) {
                try {
                    int newpos = adr2pos(ins.getTargetAddress());
                    calculateDebugFileLine(debugFile, debugLine, newpos, abc, seen, seenMethods);
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
                        if (!calculateDebugFileLine(debugFile, debugLine, newpos, abc, seen, seenMethods)) {
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
     * @param constants Constant pool
     */
    public void removeWrongIndices(AVM2ConstantPool constants) {
        //This is DANGEROUS as it may alter instruction size which may lead to incorrect jump offsets!!!
        /*for (AVM2Instruction ins : code) {
            for (int i = 0; i < ins.definition.operands.length; i++) {
                if (ins.definition.operands[i] == DAT_MULTINAME_INDEX && ins.operands[i] >= constants.getMultinameCount()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_DOUBLE_INDEX && ins.operands[i] >= constants.getDoubleCount()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_INT_INDEX && ins.operands[i] >= constants.getIntCount()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_UINT_INDEX && ins.operands[i] >= constants.getUIntCount()) {
                    ins.operands[i] = 0;
                }
                if (ins.definition.operands[i] == DAT_STRING_INDEX && ins.operands[i] >= constants.getStringCount()) {
                    ins.operands[i] = 0;
                }
            }
        }*/
    }

    /**
     * Constructs AVM2Code object from ABC input stream.
     *
     * @param ais ABC input stream
     * @param body Method body
     * @throws IOException On I/O error
     */
    public AVM2Code(ABCInputStream ais, MethodBody body) throws IOException {
        Map<Long, AVM2Instruction> codeMap = new HashMap<>();
        DumpInfo diParent = ais.dumpInfo;
        List<Long> addresses = new ArrayList<>();
        //Do not add new jumps when processing these addresses (unreachable code,etc.)
        List<Long> unAddresses = new ArrayList<>();
        //Handle lookupswitches at the end - they can be invalid. Handle other instruction first so we can decide lookupswitch to be invalid based on other instructions inside it
        //Flashplayer does not check casecount in lookupswitch instruction so the instruction can "be" long and over other instructions
        List<Long> switchAddresses = new ArrayList<>();
        int availableBytes = ais.available();
        for (int i = 0; i < availableBytes; i++) {
            codeMap.put((long) i, new AVM2Instruction(i, AVM2Instructions.Nop, null));
        }

        long startPos = ais.getPosition();
        addresses.add(startPos);
        if (body != null) {
            for (ABCException e : body.exceptions) {
                //do not process e.start and e.end - they can be not on an instruction boundary
                addresses.add((long) e.target);
            }
        }

        loopaddr:
        while (!addresses.isEmpty() || !switchAddresses.isEmpty() || !unAddresses.isEmpty()) {
            long address;
            boolean isSwitch = false;
            boolean handleJumps = true;
            if (!addresses.isEmpty()) {
                address = addresses.remove(0);
            } else if (!switchAddresses.isEmpty()) {
                address = switchAddresses.remove(0);
                isSwitch = true;
            } else {
                address = unAddresses.remove(0);
                handleJumps = false;
            }
            if (address < startPos) { // no jump outside block            
                continue;
            }
            try {
                ais.seek(address);
                while (ais.available() > 0) {
                    long startOffset = ais.getPosition();

                    if (codeMap.containsKey(startOffset) && !(codeMap.get(startOffset).definition instanceof NopIns)) {
                        continue loopaddr;
                    }

                    DumpInfo di = ais.newDumpLevel("instruction", "instruction");
                    InstructionDefinition instr = null;
                    try {
                        int instructionCode = ais.read("instructionCode");
                        instr = instructionSet[instructionCode];
                        if (instructionCode == AVM2Instructions.LookupSwitch) {
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

                            if (instructionCode == AVM2Instructions.LookupSwitch) { // switch
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
                            } else if (instr.operands.length > 0) {
                                actualOperands = new int[instr.operands.length];
                                for (int op = 0; op < instr.operands.length; op++) {
                                    switch (instr.operands[op] & 0xff00) {
                                        case OPT_U30:
                                            actualOperands[op] = ais.readU30("operand");
                                            break;
                                        case OPT_S16:
                                            actualOperands[op] = (short) ais.readU30("operand");
                                            break;
                                        case OPT_U8:
                                            actualOperands[op] = ais.read("operand");
                                            break;
                                        case OPT_S8:
                                            actualOperands[op] = (byte) ais.read("operand");
                                            break;
                                        case OPT_S24:
                                            actualOperands[op] = ais.readS24("operand");
                                            break;
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
                                    unAddresses.add(ais.getPosition());
                                    continue loopaddr;
                                } else {
                                    actualOperands[0] = 0;
                                }
                            }

                            if (instr.isExitInstruction()) { //do not process jumps if there is return/throw instruction
                                if (handleJumps) {
                                    unAddresses.add(ais.getPosition());
                                    continue loopaddr;
                                }
                            }
                            if ((instr instanceof LookupSwitchIns) && actualOperands != null) {
                                if (handleJumps) {
                                    addresses.add(startOffset + actualOperands[0]);

                                    for (int c = 2; c < actualOperands.length; c++) {
                                        addresses.add(startOffset + actualOperands[c]);
                                    }
                                    unAddresses.add(ais.getPosition());
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
                            break; // Unknown instructions are ignored (Some of the obfuscators add unknown instructions)
                            //throw new UnknownInstructionCode(instructionCode);
                        }
                    } finally {
                        if (instr == null) {
                            ais.endDumpLevel();
                        } else {
                            ais.endDumpLevel(instr.instructionCode);
                        }
                    }
                }
            } catch (EndOfStreamException ex) {
                // lookupswitch obfuscation, ignore
                ais.endDumpLevelUntil(diParent);
            }
        }

        if (diParent != null) {
            diParent.sortChildren();
        }

        code = new ArrayList<>(codeMap.size());
        AVM2Instruction prev = null;
        for (int i = 0; i < availableBytes; i++) {
            AVM2Instruction ins = codeMap.get((long) i);
            if (prev != ins) {
                code.add(ins);
            }
            prev = ins;
        }
    }

    /**
     * Trim code list to size.
     */
    public void compact() {
        if (code instanceof ArrayList) {
            ((ArrayList) code).trimToSize();
        }
    }

    /**
     * Sets instruction operand.
     *
     * @param ip Instruction pointer
     * @param operandIndex Operand index
     * @param value Value
     * @param body Method body
     */
    public void setInstructionOperand(int ip, int operandIndex, int value, MethodBody body) {
        int oldVal = code.get(ip).operands[ip];
        code.get(ip).operands[ip] = value;
    }

    /**
     * Gets code bytes.
     *
     * @return Code bytes
     */
    public byte[] getBytes() {
        return getBytes(null);
    }

    /**
     * Gets code bytes.
     *
     * @param origBytes Original bytes
     * @return Code bytes
     */
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
            //ignored
        }
        return bos.toByteArray();
    }

    /**
     * Sets instruction addresses by their position.
     */
    public void markOffsets() {
        long address = 0;
        for (int i = 0; i < code.size(); i++) {
            code.get(i).setAddress(address);
            address += code.get(i).getBytesLength();
        }
    }

    /**
     * To string.
     *
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (AVM2Instruction instruction : code) {
            s.append(instruction.toString());
            s.append(Helper.newLine);
        }
        return s.toString();
    }

    /**
     * Converts code to ASM source.
     *
     * @param abc ABC
     * @return ASM source
     */
    public String toASMSource(ABC abc) {
        return toASMSource(abc, abc.constants);
    }

    /**
     * Converts code to ASM source.
     *
     * @param abc ABC
     * @param constants Constant pool
     * @return ASM source
     */
    public String toASMSource(ABC abc, AVM2ConstantPool constants) {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        toASMSource(abc, constants, null, null, new ArrayList<>(), ScriptExportMode.PCODE, writer);
        writer.finishHilights();
        return writer.toString();
    }

    /**
     * Converts code to ASM source.
     *
     * @param abc ABC
     * @param constants Constant pool
     * @param info Method info
     * @param body Method body
     * @param exportMode Export mode
     * @param writer Writer
     * @return Writer
     */
    public GraphTextWriter toASMSource(ABC abc, AVM2ConstantPool constants, MethodInfo info, MethodBody body, ScriptExportMode exportMode, GraphTextWriter writer) {
        return toASMSource(abc, constants, info, body, new ArrayList<>(), exportMode, writer);
    }

    /**
     * Converts code to ASM source.
     *
     * @param abc ABC
     * @param constants Constant pool
     * @param info Method info
     * @param body Method body
     * @param outputMap Output map - list of ips
     * @param exportMode Export mode
     * @param writer Writer
     * @return Writer
     */
    public GraphTextWriter toASMSource(ABC abc, AVM2ConstantPool constants, MethodInfo info, MethodBody body, List<Integer> outputMap, ScriptExportMode exportMode, GraphTextWriter writer) {

        if (info != null) {
            writer.appendNoHilight("method");
            if (Configuration.indentAs3PCode.get()) {
                writer.indent();
            }
            writer.newLine();

            info.toASMSource(abc, writer);
        }
        writer.newLine();

        Set<Long> importantOffsets = getImportantOffsets(body, true);
        if (body != null) {
            writer.appendNoHilight("body");
            if (Configuration.indentAs3PCode.get()) {
                writer.indent();
            }
            writer.newLine();

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

            for (Trait t : body.traits.traits) {
                t.convertTraitHeader(abc, writer);
                if (Configuration.indentAs3PCode.get()) {
                    writer.unindent();
                }
                writer.appendNoHilight("end ; trait").newLine();
            }
        }

        writer.newLine();
        writer.appendNoHilight("code");
        if (Configuration.indentAs3PCode.get()) {
            writer.indent();
        }
        writer.newLine();
        int ip = 0;
        int largeLimit = Configuration.limitAs3PCodeOffsetMatching.get();
        boolean markOffsets = code.size() <= largeLimit;

        if (exportMode == ScriptExportMode.HEX) {
            Helper.byteArrayToHexWithHeader(writer, getBytes());
        } else if (exportMode == ScriptExportMode.PCODE || exportMode == ScriptExportMode.PCODE_HEX) {
            for (AVM2Instruction ins : code) {
                long addr = ins.getAddress();
                if (exportMode == ScriptExportMode.PCODE_HEX) {
                    writer.appendNoHilight("; ");
                    writer.appendNoHilight(Helper.bytesToHexString(ins.getBytes()));
                    writer.newLine();
                }
                if (Configuration.showAllAddresses.get() || importantOffsets.contains(addr)) {
                    String label = "ofs" + Helper.formatAddress(addr) + ":";
                    if (Configuration.labelOnSeparateLineAs3PCode.get() && Configuration.indentAs3PCode.get()) {
                        writer.unindent().unindent().unindent();
                    }

                    writer.appendNoHilight(label);

                    if (Configuration.labelOnSeparateLineAs3PCode.get()) {
                        writer.newLine();
                        if (Configuration.indentAs3PCode.get()) {
                            writer.indent().indent().indent();
                        }
                    }
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

                if (!ins.isIgnored()) {
                    if (markOffsets) {
                        writer.append("", addr, ins.getFileOffset());
                    }

                    writer.appendNoHilight(ins.toStringNoAddress(constants, new ArrayList<>()));
                    writer.newLine();
                    outputMap.add(ip);
                }

                ip++;
            }
        } else if (exportMode == ScriptExportMode.CONSTANTS) {
            writer.appendNoHilight("Constant export mode is not supported.").newLine();
        }
        if (Configuration.indentAs3PCode.get()) {
            writer.unindent();
        }
        writer.appendNoHilight("end ; code").newLine();
        if (body != null) {
            for (int e = 0; e < body.exceptions.length; e++) {
                ABCException exception = body.exceptions[e];
                writer.appendNoHilight("try");

                //Note: start and end address can be not on instruction boundary - call adr2pos( nearest=true) to make them legal
                writer.appendNoHilight(" from ");
                writer.appendNoHilight("ofs");
                writer.appendNoHilight(Helper.formatAddress(pos2adr(adr2pos(exception.start, true))));

                writer.appendNoHilight(" to ");
                writer.appendNoHilight("ofs");
                writer.appendNoHilight(Helper.formatAddress(pos2adr(adr2pos(exception.end, true))));

                writer.appendNoHilight(" target ");
                writer.appendNoHilight("ofs");
                writer.appendNoHilight(Helper.formatAddress(exception.target));

                writer.appendNoHilight(" type ");
                writer.hilightSpecial(exception.type_index == 0 ? "null" : constants.getMultiname(exception.type_index).toString(constants, new ArrayList<>()), HighlightSpecialType.TRY_TYPE, e);

                writer.appendNoHilight(" name ");
                writer.hilightSpecial(exception.name_index == 0 ? "null" : constants.getMultiname(exception.name_index).toString(constants, new ArrayList<>()), HighlightSpecialType.TRY_NAME, e);

                writer.appendNoHilight(" end");
                writer.newLine();
            }
            if (Configuration.indentAs3PCode.get()) {
                writer.unindent();
            }
            writer.appendNoHilight("end ; body").newLine();
        }
        if (info != null) {
            if (Configuration.indentAs3PCode.get()) {
                writer.unindent();
            }
            writer.appendNoHilight("end ; method").newLine();
        }

        return writer;
    }

    /**
     * Gets important offsets.
     *
     * @param body Method body
     * @param tryEnds Whether to include try ends
     * @return Important offsets
     */
    public Set<Long> getImportantOffsets(MethodBody body, boolean tryEnds) {
        Set<Long> ret = new HashSet<>();
        if (body != null) {
            for (ABCException exception : body.exceptions) {
                ret.add((long) pos2adr(adr2pos(exception.start, true)));
                if (tryEnds) {
                    ret.add((long) pos2adr(adr2pos(exception.end, true)));
                }
                ret.add((long) exception.target);
            }
        }

        for (AVM2Instruction ins : code) {
            ret.addAll(ins.getOffsets());
        }

        return ret;
    }

    /**
     * Gets instruction at specific address.
     *
     * @param address Address
     * @return Instruction or null if not found
     * @throws ConvertException On convert error
     */
    public AVM2Instruction adr2ins(long address) throws ConvertException {
        int pos = adr2pos(address, false);
        if (pos == code.size()) {
            // end
            return null;
        }

        return code.get(pos);
    }

    /**
     * Converts address to position.
     *
     * @param address Address
     * @return Position
     * @throws ConvertException On convert error
     */
    public int adr2pos(long address) throws ConvertException {
        return adr2pos(address, false);
    }

    /**
     * Converts address to position.
     *
     * @param address Address
     * @param nearest Whether to find nearest position
     * @return Position
     * @throws ConvertException On convert error
     */
    public int adr2pos(long address, boolean nearest) throws ConvertException {
        int ret = adr2posNoEx(address);
        if (ret < 0) {
            if (nearest && address < getEndOffset()) {
                return -ret - 1;
            }
            throw new ConvertException("Invalid jump to ofs" + Helper.formatAddress(address), -1);
        }
        return ret;
    }

    /**
     * Converts address to position without throwing an exception.
     *
     * @param address Address
     * @return Position
     */
    private int adr2posNoEx(long address) {
        int min = 0;
        int max = code.size() - 1;

        while (max >= min) {
            int mid = (min + max) / 2;
            long midValue = code.get(mid).getAddress();
            if (midValue == address) {
                return mid;
            } else if (midValue < address) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        if (address == getEndOffset()) {
            return code.size();
        }

        return -min - 1;
    }

    /**
     * Converts position to address.
     *
     * @param pos Position
     * @return Address
     */
    public long pos2adr(int pos) {
        if (pos == code.size()) {
            return getEndOffset();
        }
        return (int) code.get(pos).getAddress();
    }

    /**
     * Gets end address after the last instruction.
     *
     * @return End address
     */
    public long getEndOffset() {
        if (code.isEmpty()) {
            return 0;
        }

        AVM2Instruction ins = code.get(code.size() - 1);
        return (int) (ins.getAddress() + ins.getBytesLength());
    }

    /**
     * Gets local register names from debug info.
     *
     * @param abc ABC
     * @param maxRegs Maximal register id
     * @return Map from register index to name
     */
    public Map<Integer, String> getLocalRegNamesFromDebug(ABC abc, int maxRegs) {
        Map<Integer, String> regIndexToName = new HashMap<>();
        Map<String, Integer> regNameToIndex = new HashMap<>();

        Set<String> reservedRegNames = new HashSet<>();
        for (int i = 0; i < maxRegs; i++) {
            reservedRegNames.add(String.format(Configuration.registerNameFormat.get(), i));
        }

        for (AVM2Instruction ins : code) {
            if (ins.definition instanceof DebugIns) {
                if (ins.operands[0] == 1) {
                    String v = abc.constants.getString(ins.operands[1]);

                    if (reservedRegNames.contains(v)) {
                        //do not allow reassigning reserved _loc%d_ format to other local regs
                        continue;
                    }

                    int regIndex = ins.operands[2] + 1;

                    // Same name already exists, it may be wrong names inserted by obfuscator
                    if (regNameToIndex.containsKey(v)) {
                        int existingIndex = regNameToIndex.get(v);
                        if (existingIndex != regIndex) { //if it exists and has different regIndex
                            return new HashMap<>(); //ignore debug info
                        }
                        //it may exist and had same reg index, this is okay          
                    }
                    regNameToIndex.put(v, regIndex);
                    regIndexToName.put(regIndex, v);
                }
            }
        }

        // TODO: Make this immune to using existing multinames (?)
        return regIndexToName;
    }

    /**
     * Gets position after debugline instruction and after jump instruction.
     *
     * @param ip Current position
     * @return New position
     */
    public int getIpThroughJumpAndDebugLine(int ip) {
        if (code.isEmpty()) {
            return ip;
        }
        if (ip >= code.size()) {
            return code.size() - 1;
        }
        while (ip < code.size()) {
            if (code.get(ip).definition instanceof DebugLineIns) {
                ip++;
            } else if (code.get(ip).definition instanceof JumpIns) {
                ip = adr2pos(pos2adr(ip + 1) + code.get(ip).operands[0]);
            } else {
                break;
            }
        }
        if (ip >= code.size()) {
            return code.size() - 1;
        }
        return ip;
    }

    /**
     * Gets address after debugline instruction and after jump instruction.
     *
     * @param addr Current address
     * @return New address
     * @throws ConvertException On convert error
     */
    public long getAddrThroughJumpAndDebugLine(long addr) throws ConvertException {
        return pos2adr(getIpThroughJumpAndDebugLine(adr2pos(addr, true)));
    }

    /**
     * Converts to source output.
     * @param switchParts Switch parts
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param setLocalPosToGetLocalPos Set local position to get local position
     * @param thisHasDefaultToPrimitive Whether this has default to primitive
     * @param lineStartItem Line start item
     * @param path Path
     * @param part Part
     * @param processJumps Whether to process jumps
     * @param isStatic Whether is static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param localRegs Local registers
     * @param stack Stack
     * @param scopeStack Scope stack
     * @param localScopeStack Local scope stack
     * @param abc ABC
     * @param body Method body
     * @param start Start
     * @param end End
     * @param localRegNames Local register names
     * @param localRegTypes Local register types
     * @param fullyQualifiedNames Fully qualified names
     * @param visited Visited
     * @param localRegAssignmentIps Local register assignment IPs
     * @param bottomStackSetLocals Set locals on bottom of the stack
     * @return Convert output
     * @throws ConvertException On convert error
     * @throws InterruptedException On interrupt
     */
    public ConvertOutput toSourceOutput(Set<GraphPart> switchParts, List<MethodBody> callStack, AbcIndexing abcIndex, Map<Integer, Set<Integer>> setLocalPosToGetLocalPos, boolean thisHasDefaultToPrimitive, Reference<GraphSourceItem> lineStartItem, String path, GraphPart part, boolean processJumps, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, ScopeStack localScopeStack, ABC abc, MethodBody body, int start, int end, HashMap<Integer, String> localRegNames, HashMap<Integer, GraphTargetItem> localRegTypes, List<DottedChain> fullyQualifiedNames, boolean[] visited, HashMap<Integer, Integer> localRegAssignmentIps, LinkedIdentityHashSet<SetLocalAVM2Item> bottomStackSetLocals) throws ConvertException, InterruptedException {
        boolean debugMode = DEBUG_MODE;
        if (debugMode) {
            System.err.println("OPEN SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
        }
        if (visited == null) {
            visited = new boolean[code.size()];
        }
        //if(true) return "";
        toSourceCount++;
        if (toSourceLimit > 0 && toSourceCount >= toSourceLimit) {
            throw new ConvertException("Limit of subs(" + toSourceLimit + ") was reached", start);
        }
        List<GraphTargetItem> output = new ArrayList<>();
        int ip = start;
        //try {
        //int addr;
        iploop:
        while (ip <= end) {

            boolean processTry = processJumps;
            //addr = pos2adr(ip);
            //int ipfix = fixIPAfterDebugLine(ip);
            //int addrfix = pos2adr(ipfix);
            //int maxend = -1;

            if (ip > end) {
                break;
            }

            if (visited[ip]) {
                //logger.warning(path + ": Code already visited, ofs:" + Helper.formatAddress(pos2adr(ip)) + ", ip:" + ip);
                break;
            }

            if (Configuration.simplifyExpressions.get()) {
                stack.simplify();
            }
            visited[ip] = true;
            AVM2Instruction ins = code.get(ip);
            if (stack.isEmpty()) {
                lineStartItem.setVal(ins);
            }

            if (debugMode) {
                System.err.println("translating ip " + ip + " ins " + ins.toString() + " stack:" + stack.toString() + " localScopeStack:" + localScopeStack.toString());
            }
            if (ins.definition instanceof NewFunctionIns) {
                if (ip + 1 <= end) {
                    if (code.get(ip + 1).definition instanceof PopIns) {
                        ip += 2;
                        continue;
                    }
                }
            }
            
            if (ins.definition instanceof KillIns) {
                int killedReg = ins.operands[0];
                if (output.size() >= 2 && !stack.isEmpty()) {
                    if ((stack.peek() instanceof LocalRegAVM2Item) && (((LocalRegAVM2Item) stack.peek()).regIndex == killedReg)) {
                        if (output.get(output.size() - 1) instanceof SetPropertyAVM2Item) {
                            SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) output.get(output.size() - 1);
                            if ((output.get(output.size() - 2) instanceof SetLocalAVM2Item) && (((SetLocalAVM2Item) output.get(output.size() - 2)).regIndex == killedReg)) {
                                SetLocalAVM2Item setLoc = (SetLocalAVM2Item) output.get(output.size() - 2);
                                AVM2Instruction insAfter = ip + 1 < code.size() ? code.get(ip + 1) : null;
                                if (insAfter != null && (insAfter.definition instanceof PopIns)) {
                                    if (setProp.value instanceof LocalRegAVM2Item) {
                                        LocalRegAVM2Item locReg = (LocalRegAVM2Item) setProp.value;
                                        if  (locReg.regIndex == killedReg) {
                                            setProp.value = setLoc.value;
                                            output.remove(output.size() - 2);
                                            stack.pop();
                                            ip += 2;
                                            continue;
                                        }
                                    }
                                    
                                }
                            }
                        }
                    }
                }
            }
            /*
            if (ins.definition instanceof DupIns) {
                int nextPos;
                do {
                    AVM2Instruction insAfter = ip + 1 < code.size() ? code.get(ip + 1) : null;
                    if (insAfter == null) {
                        ins.definition.translate(switchParts, callStack, abcIndex, setLocalPosToGetLocalPos, lineStartItem, isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, localScopeStack, ins, output, body, abc, localRegNames, localRegTypes, fullyQualifiedNames, path, localRegAssignmentIps, ip, this, thisHasDefaultToPrimitive, bottomStackSetLocals);
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
                    } else {
                        ins.definition.translate(switchParts, callStack, abcIndex, setLocalPosToGetLocalPos, lineStartItem, isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, localScopeStack, ins, output, body, abc, localRegNames, localRegTypes, fullyQualifiedNames, path, localRegAssignmentIps, ip, this, thisHasDefaultToPrimitive, bottomStackSetLocals);
                        ip++;
                        break;
                        //throw new ConvertException("Unknown pattern after DUP:" + insComparison.toString());
                    }
                } while (ins.definition instanceof DupIns);
            } else
            */    
            if ((ins.definition instanceof ReturnValueIns) || (ins.definition instanceof ReturnVoidIns) || (ins.definition instanceof ThrowIns)) {
                ins.definition.translate(switchParts, callStack, abcIndex, setLocalPosToGetLocalPos, lineStartItem, isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, localScopeStack, ins, output, body, abc, localRegNames, localRegTypes, fullyQualifiedNames, path, localRegAssignmentIps, ip, this, thisHasDefaultToPrimitive, bottomStackSetLocals);
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
                                    if (psco.operands[0] == localScopeStack.size() - 1) {
                                        if (code.get(ip + plus + 2).definition instanceof SwapIns) {
                                            if (code.get(ip + plus + 4).definition instanceof PopScopeIns) {
                                                if (code.get(ip + plus + 3).definition instanceof SetPropertyIns) {
                                                    functionName = abc.constants.getMultiname(code.get(ip + plus + 3).operands[0]).getName(abc.constants, fullyQualifiedNames, true, true);
                                                    localScopeStack.pop(); // with
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
                ins.definition.translate(switchParts, callStack, abcIndex, setLocalPosToGetLocalPos, lineStartItem, isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, localScopeStack, ins, output, body, abc, localRegNames, localRegTypes, fullyQualifiedNames, path, localRegAssignmentIps, ip, this, thisHasDefaultToPrimitive, bottomStackSetLocals);
                NewFunctionAVM2Item nft = (NewFunctionAVM2Item) stack.peek();
                nft.functionName = functionName;
                ip++;
            } else {
                try {                    
                    ins.definition.translate(switchParts, callStack, abcIndex, setLocalPosToGetLocalPos, lineStartItem, isStatic, scriptIndex, classIndex, localRegs, stack, scopeStack, localScopeStack, ins, output, body, abc, localRegNames, localRegTypes, fullyQualifiedNames, path, localRegAssignmentIps, ip, this, thisHasDefaultToPrimitive, bottomStackSetLocals);
                    
                    
                    if (stack.size() == 1 && (stack.peek() instanceof SetLocalAVM2Item)) {
                       bottomStackSetLocals.add((SetLocalAVM2Item) stack.peek());
                    }
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

    /**
     * Gets number of registers used in the code.
     *
     * @return Number of registers
     */
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

    /**
     * Gets types of local registers.
     *
     * @param constants Constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @return Map from register id to type
     */
    public HashMap<Integer, GraphTargetItem> getLocalRegTypes(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        HashMap<Integer, GraphTargetItem> ret = new HashMap<>();
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

    /**
     * Slot class
     */
    private class Slot {

        /**
         * Scope
         */
        public final GraphTargetItem scope;

        /**
         * Multiname
         */
        public final Multiname multiname;

        /**
         * Constructs a new Slot.
         *
         * @param scope Scope
         * @param multiname Multiname
         */
        public Slot(GraphTargetItem scope, Multiname multiname) {
            this.scope = scope;
            this.multiname = multiname;
        }

        /**
         * Equals.
         *
         * @param obj Object
         * @return True if equal
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Slot) {
                Slot slot = (Slot) obj;
                return (Objects.equals(slot.scope.getThroughRegister(), scope.getThroughRegister()))
                        && (slot.multiname == multiname);
            }
            return false;
        }

        /**
         * Hash code.
         *
         * @return Hash code
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (scope != null ? Objects.hashCode(scope.getThroughRegister()) : 0);
            hash = 59 * hash + Objects.hashCode(multiname);
            return hash;
        }
    }

    /**
     * Initializes counter of toSource calls.
     */
    public void initToSource() {
        toSourceCount = 0;
    }

    /**
     * Handles declaration of registers.
     *
     * @param minreg Minimal register id
     * @param assignment Assignment
     * @param declaredRegisters Declared registers
     * @param declaredSlots Declared slots
     * @param reg Register id
     * @return Assignment
     */
    private GraphTargetItem handleDeclareReg(int minreg, GraphTargetItem assignment, DeclarationAVM2Item[] declaredRegisters, List<Slot> declaredSlots, int reg) {

        //do not add declarations for reserved local registers like function arguments
        if (reg < minreg) {
            return assignment;
        }
        GraphTargetItem vtype = TypeItem.UNBOUNDED;
        if (assignment.value instanceof ConvertAVM2Item) {
            vtype = ((ConvertAVM2Item) assignment.value).type;
        } else if (assignment.value instanceof CoerceAVM2Item) {
            vtype = ((CoerceAVM2Item) assignment.value).typeObj;
        } else if (assignment instanceof LocalRegAVM2Item) { //for..in
            vtype = ((LocalRegAVM2Item) assignment).type;
        } else if (assignment instanceof GetSlotAVM2Item) { //for..in
            vtype = ((GetSlotAVM2Item) assignment).slotType;
        } else if ((assignment.value instanceof SimpleValue) && ((SimpleValue) assignment.value).isSimpleValue()) {
            vtype = assignment.value.returnType();
        } else if (assignment.value instanceof GetLexAVM2Item) {
            vtype = assignment.value.returnType();
        }

        boolean isNull = false;
        if (vtype.equals(new TypeItem(DottedChain.NULL))) {
            vtype = TypeItem.UNBOUNDED;
            isNull = true;
        }

        if (declaredRegisters[reg] == null) {
            declaredRegisters[reg] = new DeclarationAVM2Item(assignment, vtype);
            if (assignment instanceof SetTypeAVM2Item) {
                ((SetTypeAVM2Item) assignment).setDeclaration(declaredRegisters[reg]);
            }
            declaredRegisters[reg].typeIsNull = isNull;
            return declaredRegisters[reg];
        }

        if (declaredRegisters[reg].typeIsNull) {
            declaredRegisters[reg].type = vtype;
            declaredRegisters[reg].typeIsNull = isNull;
        } else if (declaredRegisters[reg].type == TypeItem.UNBOUNDED) {
            //empty
        } else if (!declaredRegisters[reg].type.equals(vtype)) { //already declared with different type
            declaredRegisters[reg].type = TypeItem.UNBOUNDED;
        }

        if (assignment instanceof SetTypeAVM2Item) {
            ((SetTypeAVM2Item) assignment).setDeclaration(declaredRegisters[reg]);
        }

        return assignment;
    }

    /**
     * Calculates index of property name in the slot list.
     *
     * @param list List of slots
     * @param propertyName Property name
     * @param abc ABC
     * @return Index of property name in the slot list or -1 if not found
     */
    private int slotListIndexOf(List<Slot> list, String propertyName, ABC abc) {
        int index = 0;
        for (Slot s : list) {
            if (propertyName.equals(abc.constants.getString(s.multiname.name_index))) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Injects declarations of registers/slots/properties etc.
     *
     * @param level Level
     * @param paramNames Parameter names
     * @param items Items
     * @param minreg Minimal register id
     * @param declaredRegisters Declared registers
     * @param declaredSlots Declared slots
     * @param declaredSlotsDec Declared slots declarations
     * @param declaredProperties Declared properties
     * @param declaredPropsDec Declared properties declarations
     * @param abc ABC
     * @param body Method body
     */
    private void injectDeclarations(int level, List<String> paramNames, List<GraphTargetItem> items, int minreg, DeclarationAVM2Item[] declaredRegisters, List<Slot> declaredSlots, List<DeclarationAVM2Item> declaredSlotsDec, List<String> declaredProperties, List<DeclarationAVM2Item> declaredPropsDec, ABC abc, MethodBody body) {
        //boolean hasActivation = abc.method_info.get(body.method_info).flagNeed_activation();
        Map<String, TraitSlotConst> traits = new LinkedHashMap<>();
        for (Trait t : body.traits.traits) {
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                Multiname tratMultiname = abc.constants.getMultiname(tsc.name_index);
                String bodyTraitName = tratMultiname.getName(abc.constants, new ArrayList<>(), true, true);
                traits.put(bodyTraitName, tsc);
            }
        }
        if (level == 0) {
            Set<String> beginDeclaredSlotsNames = new LinkedHashSet<>();

            for (int i = 0; i < items.size(); i++) {
                GraphTargetItem item = items.get(i);
                String propNameStr = null;
                GraphTargetItem value = null;
                if (item instanceof StoreNewActivationAVM2Item) { //Special case
                    continue;
                } else if (item instanceof SetSlotAVM2Item) {
                    SetSlotAVM2Item ss = (SetSlotAVM2Item) item;
                    if (ss.slotName == null) {
                        break;
                    }
                    propNameStr = ss.slotName.getName(abc.constants, new ArrayList<>(), true, true);
                    value = ss.value;
                } else if (item instanceof SetPropertyAVM2Item) {
                    SetPropertyAVM2Item sp = (SetPropertyAVM2Item) item;
                    if (sp.object instanceof FindPropertyAVM2Item) {
                        if (sp.propertyName instanceof FullMultinameAVM2Item) {
                            FullMultinameAVM2Item propName = (FullMultinameAVM2Item) sp.propertyName;
                            propNameStr = propName.resolvedMultinameName;
                            value = sp.value;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }

                value = value.getNotCoerced();
                /*if (!((value instanceof NumberValueAVM2Item)
                        || (value instanceof StringAVM2Item)
                        || (value instanceof TrueItem)
                        || (value instanceof FalseItem)
                        || (value instanceof UndefinedAVM2Item)
                        || (value instanceof NullAVM2Item)
                        || (value instanceof NewFunctionAVM2Item))) {
                    break;
                }*/
                Reference<Boolean> hasPrevReference = new Reference<>(false);
                value.visitRecursivelyNoBlock(new AbstractGraphTargetRecursiveVisitor() {
                    @Override
                    public void visit(GraphTargetItem subItem, Stack<GraphTargetItem> parentStack) {
                        Multiname propertyMultiName;
                        String propertyName;
                        if (subItem instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item propItem = (GetPropertyAVM2Item) subItem;
                            if (propItem.object instanceof FindPropertyAVM2Item) {
                                propertyMultiName = abc.constants.getMultiname(((FullMultinameAVM2Item) propItem.propertyName).multinameIndex);
                            } else {
                                return;
                            }
                        } else if (subItem instanceof GetLexAVM2Item) {
                            GetLexAVM2Item lex = (GetLexAVM2Item) subItem;
                            propertyMultiName = lex.propertyName;
                        } else {
                            return;
                        }
                        propertyName = propertyMultiName.getName(abc.constants, new ArrayList<>(), true, true);

                        if (traits.containsKey(propertyName)) {
                            Slot sl = new Slot(new NewActivationAVM2Item(null, null), propertyMultiName);
                            if (!paramNames.contains(propertyName)) {
                                if (traits.containsKey(propertyName) && !beginDeclaredSlotsNames.contains(propertyName)) {
                                    hasPrevReference.setVal(true);
                                }
                            }
                        }
                    }
                });
                if (hasPrevReference.getVal()) {
                    break;
                }

                beginDeclaredSlotsNames.add(propNameStr);
            }

            int pos = 0;
            for (String traitName : traits.keySet()) {
                if (!paramNames.contains(traitName)) {
                    if (!beginDeclaredSlotsNames.contains(traitName)) {
                        Slot sl = new Slot(new NewActivationAVM2Item(null, null), abc.constants.getMultiname(traits.get(traitName).name_index));
                        TraitSlotConst tsc = (TraitSlotConst) traits.get(traitName);
                        GraphTargetItem type = AbcIndexing.multinameToType(tsc.type_index, abc.constants);
                        DeclarationAVM2Item d = new DeclarationAVM2Item(new GetLexAVM2Item(null, null, sl.multiname, abc.constants, type, TypeItem.UNBOUNDED /*?*/, false), type);
                        declaredSlotsDec.add(d);
                        declaredSlots.add(sl);

                        d.showValue = false;
                        items.add(pos, d);
                        pos++;

                        declaredPropsDec.add(d);
                        declaredProperties.add(traitName);
                    }
                }
            }
        }
        for (int i = 0; i < items.size(); i++) {
            GraphTargetItem currentItem = items.get(i);
            List<GraphTargetItem> itemsOnLine = new ArrayList<>();
            itemsOnLine.add(currentItem);
            currentItem.visitRecursivelyNoBlock(new AbstractGraphTargetRecursiveVisitor() {
                @Override
                public void visit(GraphTargetItem item, Stack<GraphTargetItem> parentStack) {
                    itemsOnLine.add(item);
                }
            });

            if (currentItem instanceof ForEachInAVM2Item) {
                ForEachInAVM2Item fei = (ForEachInAVM2Item) currentItem;
                if (fei.expression.object instanceof LocalRegAVM2Item) {
                    int reg = ((LocalRegAVM2Item) fei.expression.object).regIndex;
                    if (declaredRegisters[reg] == null) {
                        fei.expression.object = handleDeclareReg(minreg, fei.expression.object, declaredRegisters, declaredSlots, reg);
                    }
                }
            }
            if (currentItem instanceof ForInAVM2Item) {
                ForInAVM2Item fi = (ForInAVM2Item) currentItem;
                if (fi.expression.object instanceof LocalRegAVM2Item) {
                    int reg = ((LocalRegAVM2Item) fi.expression.object).regIndex;
                    fi.expression.object = handleDeclareReg(minreg, fi.expression.object, declaredRegisters, declaredSlots, reg);
                }
            }

            for (GraphTargetItem subItem : itemsOnLine) {
                if (subItem instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) subItem;
                    int reg = setLocal.regIndex;
                    GraphTargetItem dec = handleDeclareReg(minreg, subItem, declaredRegisters, declaredSlots, reg);
                    if (dec != subItem) { //declared right now
                        if (subItem == currentItem) {
                            items.set(i, dec);
                        } else {
                            ((DeclarationAVM2Item) dec).showValue = false;
                            items.add(i, dec);
                            i++;
                        }
                    }
                }
                if (subItem instanceof SetPropertyAVM2Item) {
                    SetPropertyAVM2Item sp = (SetPropertyAVM2Item) subItem;
                    if (sp.object instanceof FindPropertyAVM2Item) {
                        if (sp.propertyName instanceof FullMultinameAVM2Item) {
                            FullMultinameAVM2Item propName = (FullMultinameAVM2Item) sp.propertyName;
                            if (!paramNames.contains(propName.resolvedMultinameName)) {
                                if (!declaredProperties.contains(propName.resolvedMultinameName)) {
                                    if (traits.containsKey(propName.resolvedMultinameName)) {
                                        TraitSlotConst tsc = traits.get(propName.resolvedMultinameName);
                                        GraphTargetItem type = AbcIndexing.multinameToType(tsc.type_index, abc.constants);
                                        DeclarationAVM2Item d = new DeclarationAVM2Item(subItem, type);
                                        sp.setDeclaration(d);
                                        declaredPropsDec.add(d);
                                        declaredProperties.add(propName.resolvedMultinameName);

                                        Slot sl = new Slot(new NewActivationAVM2Item(null, null), abc.constants.getMultiname(tsc.name_index));
                                        declaredSlotsDec.add(d);
                                        declaredSlots.add(sl);

                                        if (subItem == currentItem) {
                                            items.set(i, d);
                                        } else {
                                            d.showValue = false;
                                            items.add(i, d);
                                            i++;
                                        }
                                    }
                                } else {
                                    int idx = declaredProperties.indexOf(propName.resolvedMultinameName);
                                    sp.setDeclaration(declaredPropsDec.get(idx));
                                }
                            }
                        }
                    }
                }
                if (subItem instanceof SetSlotAVM2Item) {
                    SetSlotAVM2Item ssti = (SetSlotAVM2Item) subItem;
                    if (ssti.scope instanceof NewActivationAVM2Item) {
                        Slot sl = new Slot(ssti.scope, ssti.slotName);
                        String slotPropertyName = sl.multiname.getName(abc.constants, new ArrayList<>(), true, false);
                        if (!paramNames.contains(slotPropertyName)) {

                            int index = slotListIndexOf(declaredSlots, slotPropertyName, abc);
                            if (index == -1) {
                                GraphTargetItem type = TypeItem.UNBOUNDED;
                                if (traits.containsKey(slotPropertyName)) {
                                    type = AbcIndexing.multinameToType(traits.get(slotPropertyName).type_index, abc.constants);
                                }
                                DeclarationAVM2Item d = new DeclarationAVM2Item(subItem, type);
                                ssti.setDeclaration(d);
                                declaredSlotsDec.add(d);
                                declaredSlots.add(sl);

                                declaredPropsDec.add(d);
                                declaredProperties.add(slotPropertyName);

                                if (subItem == currentItem) {
                                    items.set(i, d);
                                } else {
                                    d.showValue = false;
                                    items.add(i, d);
                                    i++;
                                }

                            } else {
                                ssti.setDeclaration(declaredSlotsDec.get(index));
                            }
                        }
                    }
                }
            }

            if (currentItem instanceof Block) {
                Block blk = (Block) currentItem;
                for (List<GraphTargetItem> sub : blk.getSubs()) {
                    injectDeclarations(level + 1, paramNames, sub, minreg, declaredRegisters, declaredSlots, declaredSlotsDec, declaredProperties, declaredPropsDec, abc, body);
                }
            }
        }

        /*if (level == 0) {
            int pos = 0;
            for (String propertyName : traits.keySet()) {
                if (!paramNames.contains(propertyName)) {
                    Slot sl = new Slot(new NewActivationAVM2Item(null, null), abc.constants.getMultiname(traits.get(propertyName).name_index));
                    if (!declaredSlots.contains(sl) && !declaredProperties.contains(propertyName)) {
                        TraitSlotConst tsc = traits.get(propertyName);
                        GraphTargetItem type = PropertyAVM2Item.multinameToType(tsc.type_index, abc.constants);
                        DeclarationAVM2Item d = new DeclarationAVM2Item(new GetLexAVM2Item(null, null, sl.multiname, abc.constants), type);
                        declaredSlotsDec.add(d);
                        declaredSlots.add(sl);

                        d.showValue = false;
                        items.add(pos, d);
                        pos++;
                    }
                }
            }
        }*/
    }
    
    private static interface BlockVisitor {
        public void visitBlock(List<GraphTargetItem> items);
    }

    /**
     * Converts code to source - list of GraphTargetItems.
     *
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param thisHasDefaultToPrimitive True if this has default to primitive
     * @param convertData Convert data
     * @param path Path
     * @param methodIndex Method index
     * @param isStatic True if static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param abc ABC
     * @param body Method body
     * @param localRegNames Local register names
     * @param scopeStack Scope stack
     * @param initializerType Initializer type
     * @param fullyQualifiedNames Fully qualified names
     * @param initTraits Initialized traits
     * @param staticOperation Static operation
     * @param localRegAssignmentIps Local register assignment IPs
     * @return List of GraphTargetItems
     * @throws InterruptedException On interrupt
     */
    public List<GraphTargetItem> toGraphTargetItems(List<MethodBody> callStack, AbcIndexing abcIndex, boolean thisHasDefaultToPrimitive, ConvertData convertData, String path, int methodIndex, boolean isStatic, int scriptIndex, int classIndex, ABC abc, MethodBody body, HashMap<Integer, String> localRegNames, ScopeStack scopeStack, int initializerType, List<DottedChain> fullyQualifiedNames, Traits initTraits, int staticOperation, HashMap<Integer, Integer> localRegAssignmentIps) throws InterruptedException {
        initToSource();
        List<GraphTargetItem> list;
        HashMap<Integer, GraphTargetItem> localRegs = new HashMap<>();

        int regCount = getRegisterCount();
        for (int i = 0; i < regCount; i++) {
            localRegs.put(0, new UndefinedAVM2Item(null, null));
        }
        HashMap<Integer, GraphTargetItem> localRegTypes = new HashMap<>();
        for (int i = 0; i < abc.method_info.get(methodIndex).param_types.length; i++) {
            localRegTypes.put(i + 1, AbcIndexing.multinameToType(abc.method_info.get(methodIndex).param_types[i], abc.constants));
        }

        try {
            list = AVM2Graph.translateViaGraph(null, callStack, abcIndex, path, this, abc, body, isStatic, scriptIndex, classIndex, localRegs, scopeStack, localRegNames, localRegTypes, fullyQualifiedNames, staticOperation, localRegAssignmentIps, thisHasDefaultToPrimitive);
        } catch (SecondPassException spe) {
            list = AVM2Graph.translateViaGraph(spe.getData(), callStack, abcIndex, path, this, abc, body, isStatic, scriptIndex, classIndex, localRegs, scopeStack, localRegNames, localRegTypes, fullyQualifiedNames, staticOperation, localRegAssignmentIps, thisHasDefaultToPrimitive);
        }
        if (initTraits != null) {
            loopi:
            for (int i = 0; i < list.size(); i++) {
                GraphTargetItem ti = list.get(i);
                if (ti instanceof SetSlotAVM2Item) {
                    SetSlotAVM2Item ss = (SetSlotAVM2Item) ti;
                    if ((ss.slotObject instanceof GlobalAVM2Item) && (initializerType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER)) {
                        for (Trait t : initTraits.traits) {
                            if (t instanceof TraitSlotConst) {
                                TraitSlotConst tsc = (TraitSlotConst) t;
                                if (tsc.slot_id == ss.slotIndex) {
                                    GraphTargetItem value = ss.value;
                                    if (value != null && !convertData.assignedValues.containsKey(tsc)) {
                                        if (value instanceof NewFunctionAVM2Item) {
                                            NewFunctionAVM2Item f = (NewFunctionAVM2Item) value;
                                            f.functionName = tsc.getName(abc).getName(abc.constants, fullyQualifiedNames, true, true);
                                        }
                                        AssignedValue av = new AssignedValue(ti, value, initializerType, methodIndex);
                                        convertData.assignedValues.put(tsc, av);
                                        //list.remove(i);
                                        //i--;
                                        continue loopi;
                                    }
                                }
                            }
                        }
                    }
                }
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
                    for (int j = 0; j < initTraits.traits.size(); j++) {
                        Trait t = initTraits.traits.get(j);
                        Multiname tm = abc.constants.getMultiname(t.name_index);
                        if (tm != null && tm.equals(m)) {
                            if ((t instanceof TraitSlotConst)) {
                                if (((TraitSlotConst) t).isConst() || initializerType == GraphTextWriter.TRAIT_CLASS_INITIALIZER || initializerType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
                                    TraitSlotConst tsc = (TraitSlotConst) t;
                                    if (value != null && !convertData.assignedValues.containsKey(tsc)) {

                                        /*if (ti instanceof SetPropertyAVM2Item) { //only for slots
                                            Set<GraphTargetItem> subItems = value.getAllSubItemsRecursively();
                                            subItems.add(value);
                                            List<Multiname> laterMultinames = new ArrayList<>();
                                            for (int k = j + 1; k < ts.traits.size(); k++) {
                                                int tMultinameIndex = ts.traits.get(k).name_index;
                                                if (tMultinameIndex > 0) {
                                                    Multiname tMultiname = abc.constants.getMultiname(tMultinameIndex);
                                                    laterMultinames.add(tMultiname);
                                                }
                                            }
                                            for (GraphTargetItem item : subItems) {

                                                //if later slot is referenced, we must add it as {} block instead of direct assignment
                                                if (item instanceof GetPropertyAVM2Item) {
                                                    Multiname multiName = abc.constants.getMultiname(((FullMultinameAVM2Item) ((GetPropertyAVM2Item) item).propertyName).multinameIndex);
                                                    if (laterMultinames.contains(multiName)) {
                                                        continue loopi;
                                                    }
                                                }
                                                if (item instanceof GetLexAVM2Item) {
                                                    Multiname multiName = ((GetLexAVM2Item) item).propertyName;
                                                    if (laterMultinames.contains(multiName)) {
                                                        continue loopi;
                                                    }
                                                }

                                                if (item instanceof LocalRegAVM2Item) { //it is surely in static initializer block, not in slot/const
                                                    continue loopi;
                                                }
                                            }
                                        }*/
                                        if (value instanceof NewFunctionAVM2Item) {
                                            NewFunctionAVM2Item f = (NewFunctionAVM2Item) value;
                                            f.functionName = tsc.getName(abc).getName(abc.constants, fullyQualifiedNames, true, true);
                                        }
                                        AssignedValue av = new AssignedValue(ti, value, initializerType, methodIndex);
                                        convertData.assignedValues.put(tsc, av);
                                        //list.remove(i);
                                        //i--;
                                        continue loopi;
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
        if (initializerType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
            if ((list.size() > lastPos) && (list.get(lastPos) instanceof ReturnValueAVM2Item)) {
                ReturnValueAVM2Item rv = (ReturnValueAVM2Item) list.get(lastPos);
                if (rv.value instanceof LocalRegAVM2Item) {
                    list.remove(lastPos);
                } else {
                    list.set(lastPos, rv.value);
                }
                
            }
        }
        
        if (initializerType == GraphTextWriter.TRAIT_CLASS_INITIALIZER || initializerType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
            Map<GraphTargetItem, AssignedValue> commandToAssigned = new IdentityHashMap<>();
            Map<GraphTargetItem, TraitSlotConst> commandToTrait = new IdentityHashMap<>();
            for (TraitSlotConst tsc : convertData.assignedValues.keySet()) {
                AssignedValue asv = convertData.assignedValues.get(tsc);
                commandToAssigned.put(asv.command, asv);
                commandToTrait.put(asv.command, tsc);
            }
                        
            for (int i = 0; i < list.size(); i++) {
                GraphTargetItem ti = list.get(i);                                
                if (commandToAssigned.containsKey(ti)) {
                    AssignedValue asv = commandToAssigned.get(ti);
                    TraitSlotConst tsc = commandToTrait.get(ti);
                    
                    int nsKind = tsc.getName(abc).getSimpleNamespaceKind(abc.constants);
                    if (classIndex == -1 && (nsKind == Namespace.KIND_PACKAGE || nsKind == Namespace.KIND_PACKAGE_INTERNAL)) {
                        list.remove(i);
                        i--;
                        continue;
                    }
                
                    
                    TraitSlotConstAVM2Item item = new TraitSlotConstAVM2Item(
                            ti.getSrc(), 
                            ti.getLineStartItem(),
                            tsc, 
                            asv.value, 
                            isStatic,
                            scriptIndex,
                            classIndex,
                            initializerType,
                            methodIndex,
                            initTraits.traits.indexOf(tsc)
                    );
                    list.set(i, item);
                }
            }
            
            if (initializerType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
                                
                //Eliminate all setlocals, can sometimes happen
                BlockVisitor bv = new BlockVisitor() {
                    @Override
                    public void visitBlock(List<GraphTargetItem> items) {
                                                
                        for (int i = 0; i < items.size(); i++) {
                            GraphTargetItem item = items.get(i);
                            if (item instanceof SetLocalAVM2Item) {
                                items.set(i, item.value);
                            }
                            
                            if (item instanceof Block) {
                                Block b = (Block) item;
                                for (List<GraphTargetItem> list : b.getSubs()) {
                                    visitBlock(list);
                                }
                            }
                        }
                    }
                };
                bv.visitBlock(list);
                
                PackageAVM2Item currentPkg = null;
                for (int i = 0; i < list.size(); i++) {
                    GraphTargetItem ti = list.get(i);
                    if (ti instanceof TraitSlotConstAVM2Item) {
                        TraitSlotConstAVM2Item tsci = (TraitSlotConstAVM2Item) ti;
                        Namespace ns = tsci.getTrait().getName(abc).getNamespace(abc.constants);
                        if (ns.kind == Namespace.KIND_PACKAGE || ns.kind == Namespace.KIND_PACKAGE_INTERNAL) {
                            String newPkgName = ns.getName(abc.constants).toRawString();
                            if (currentPkg == null) {
                                currentPkg = new PackageAVM2Item(new ArrayList<>(), newPkgName);
                                currentPkg.addItem(tsci);
                                list.set(i, currentPkg);
                            } else if (currentPkg.getPackageName().equals(newPkgName)){
                                currentPkg.addItem(tsci);
                                list.remove(i);
                                i--;
                            } else {
                                currentPkg = new PackageAVM2Item(new ArrayList<>(), newPkgName);
                                currentPkg.addItem(tsci);
                                list.set(i, currentPkg);
                            }
                        }
                    } else if (currentPkg != null) {
                        final String currentPkgName = currentPkg.getPackageName();
                        Reference<Boolean> insidePackage = new Reference<>(true);

                        //Check whether the command references internal traits of other package
                        ti.visitRecursively(new AbstractGraphTargetVisitor() {
                            @Override
                            public boolean visit(GraphTargetItem item) {
                                if (item instanceof GetSlotAVM2Item) {
                                    GetSlotAVM2Item gs = (GetSlotAVM2Item) item;
                                    if ((gs.slotObject instanceof GlobalAVM2Item) && (initializerType == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER)) {
                                        for (Trait t : initTraits.traits) {
                                            if (t instanceof TraitSlotConst) {
                                                TraitSlotConst tsc = (TraitSlotConst) t;
                                                if (tsc.slot_id == gs.slotIndex) {
                                                    int nsKind = tsc.getName(abc).getNamespace(abc.constants).kind;
                                                    if (
                                                            (
                                                            nsKind == Namespace.KIND_PACKAGE_INTERNAL
                                                            && !currentPkgName.equals(tsc.getName(abc).getNamespace(abc.constants).getRawName(abc.constants))
                                                            )
                                                            || (nsKind == Namespace.KIND_PRIVATE)
                                                        ) {
                                                            insidePackage.setVal(false);                                                    
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                return true;
                            }
                        });

                        if (insidePackage.getVal()) {
                            currentPkg.addItem(ti);
                            list.remove(i);
                            i--;
                        } else {
                            currentPkg = null;
                        }
                    }
                }
            }
            
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

        DeclarationAVM2Item[] d = new DeclarationAVM2Item[regCount];

        int[] param_types = abc.method_info.get(body.method_info).param_types;
        int r = 1;
        for (int i = 0; i < param_types.length; i++) {
            GraphTargetItem type;
            if (param_types[i] == 0) {
                type = TypeItem.UNBOUNDED;
            } else {
                type = new TypeItem(abc.constants.getMultiname(param_types[i]).getNameWithNamespace(abc.constants, true));
            }
            if (d.length > r) {
                d[r] = new DeclarationAVM2Item(new SetLocalAVM2Item(null, null, r, new NullAVM2Item(null, null), type), type);
            }
            r++;
        }
        if (abc.method_info.get(body.method_info).flagNeed_arguments()) {
            if (d.length > r) {
                d[r] = new DeclarationAVM2Item(new SetLocalAVM2Item(null, null, r, new NullAVM2Item(null, null), TypeItem.ARRAY), TypeItem.ARRAY /*?*/);
            }
            r++;
        }
        if (abc.method_info.get(body.method_info).flagNeed_rest()) {
            if (d.length > r) {
                d[r] = new DeclarationAVM2Item(new SetLocalAVM2Item(null, null, r, new NullAVM2Item(null, null), TypeItem.ARRAY), TypeItem.ARRAY/*?*/);
            }
            r++;
        }
        //

        //int minreg = abc.method_info.get(body.method_info).getMaxReservedReg() + 1;
        HashMap<Integer, String> registerNames = body.getLocalRegNames(abc);
        List<String> paramNamesList = new ArrayList<>();
        for (int ir = 0; ir < r; ir++) {
            paramNamesList.add(AVM2Item.localRegName(localRegNames, ir));
        }
        injectDeclarations(0, paramNamesList, list, 1, d, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), abc, body);       

        return list;
    }

    /**
     * Updates instruction byte count at given address.
     *
     * @param instructionAddress Instruction address
     * @param byteDelta Byte delta
     * @param body Method body
     */
    public void updateInstructionByteCountByAddr(long instructionAddress, int byteDelta, MethodBody body) {
        if (byteDelta != 0) {
            updateOffsets(new OffsetUpdater() {
                @Override
                public long updateInstructionOffset(long address) {
                    if (address > instructionAddress) {
                        return address + byteDelta;
                    }
                    return address;
                }

                @Override
                public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                    if (targetAddress > instructionAddress && insAddr <= instructionAddress) {
                        return offset + byteDelta;
                    }
                    if (targetAddress <= instructionAddress && insAddr > instructionAddress) {
                        return offset - byteDelta;
                    }
                    return offset;
                }
            }, body);
            body.setModified();
        }
    }

    /**
     * Updates instruction byte count at given position.
     *
     * @param pos Position
     * @param byteDelta Byte delta
     * @param body Method body
     */
    public void updateInstructionByteCount(int pos, int byteDelta, MethodBody body) {
        AVM2Instruction instruction = code.get(pos);
        updateInstructionByteCountByAddr(instruction.getAddress(), byteDelta, body);
    }

    /**
     * Updates offsets (jumps) in the code with given updater.
     *
     * @param updater Offset updater
     * @param body Method body
     */
    public void updateOffsets(OffsetUpdater updater, MethodBody body) {
        for (int i = 0; i < code.size(); i++) {
            AVM2Instruction ins = code.get(i);
            if (ins.definition instanceof LookupSwitchIns) {
                long target = ins.getAddress() + ins.operands[0];
                ins.operands[0] = updater.updateOperandOffset(ins.getAddress(), target, ins.operands[0]);
                for (int k = 2; k < ins.operands.length; k++) {
                    target = ins.getAddress() + ins.operands[k];
                    ins.operands[k] = updater.updateOperandOffset(ins.getAddress(), target, ins.operands[k]);
                }
            } else if (ins.definition instanceof IfTypeIns) {
                long target = ins.getTargetAddress();
                try {
                    ins.operands[0] = updater.updateOperandOffset(ins.getAddress(), target, ins.operands[0]);
                } catch (ConvertException cex) {
                    throw new ConvertException("Invalid offset (" + ins + ")", i);
                }
            }
            ins.setAddress(updater.updateInstructionOffset(ins.getAddress()));
            //Note: changing operands here does not change instruction byte length as offsets are always S24 (not variable length)
        }

        if (body != null) {
            for (ABCException ex : body.exceptions) {
                ex.start = updater.updateOperandOffset(-1, ex.start, ex.start);
                ex.end = updater.updateOperandOffset(-1, ex.end, ex.end);
                ex.target = updater.updateOperandOffset(-1, ex.target, ex.target);
            }
        }
    }

    /**
     * Fixes jumps to invalid addresses.
     *
     * @param path Path
     * @param body Method body
     * @throws InterruptedException On interrupt
     */
    public void fixJumps(final String path, MethodBody body) throws InterruptedException {
        if (code.isEmpty()) {
            return;
        }
        final List<Long> insAddrToRemove = new ArrayList<>();
        final long endOffset = getEndOffset();
        updateOffsets(new OffsetUpdater() {
            @Override
            public long updateInstructionOffset(long address) {
                return address;
            }

            @Override
            public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                if (targetAddress > endOffset || targetAddress < 0 || adr2posNoEx(targetAddress) < 0) {
                    insAddrToRemove.add(insAddr);
                }
                return offset;
            }
        }, body);

        boolean someIgnored = false;
        for (Long insAddr : insAddrToRemove) {
            int pos = adr2posNoEx(insAddr);
            if (pos > -1) {
                code.get(pos).setIgnored(true, 0);
                someIgnored = true;
            }
        }
        if (someIgnored) {
            logger.log(Level.WARNING, "{0}: One or more invalid jump offsets found in the code. Those instructions were ignored.", path);
        }
        removeIgnored(body);
    }

    /**
     * Checks for invalid offsets (jumps) in the code.
     *
     * @param body Method body
     */
    public void checkValidOffsets(MethodBody body) {
        updateOffsets(new OffsetUpdater() {
            @Override
            public long updateInstructionOffset(long address) {
                adr2pos(address);
                return address;
            }

            @Override
            public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                /*if (insAddr == -1) {
                 return offset;
                 }*/
                adr2pos(targetAddress);
                return offset;
            }
        }, body);
    }

    /**
     * Removes instruction at given position.
     *
     * @param pos Position
     * @param body Method body
     */
    public void removeInstruction(int pos, MethodBody body) {
        if ((pos < 0) || (pos >= code.size())) {
            throw new IndexOutOfBoundsException();
        }

        AVM2Instruction ins = code.get(pos);
        final long remOffset = ins.getAddress();
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
            public int updateOperandOffset(long jumpInsAddr, long jumpTargetAddr, int jumpOffset) {
                /*
                 a:jump d:
                 b:
                 c:X
                 d:
                 */
                if (jumpTargetAddr > remOffset && jumpInsAddr < remOffset) {
                    return jumpOffset - byteCount;
                }
                /*
                 a:X1
                 b:X2
                 c:
                 d:jump a:
                 */
                if (jumpTargetAddr <= remOffset && jumpInsAddr > remOffset) {
                    return jumpOffset + byteCount;
                }

                return jumpOffset;
            }
        }, body);
        code.remove(pos);
        //checkValidOffsets(body);
    }

    /**
     * Replaces instruction by another. Properly handles offsets. Note: If
     * newinstruction is jump, the offset operand must be handled properly by
     * caller.
     *
     * @param pos Position
     * @param instruction Instruction
     * @param body Method body
     */
    public void replaceInstruction(int pos, AVM2Instruction instruction, MethodBody body) {
        AVM2Instruction oldInstruction = code.get(pos);
        instruction.setAddress(oldInstruction.getAddress());
        int oldByteCount = oldInstruction.getBytesLength();
        int newByteCount = instruction.getBytesLength();
        int byteDelta = newByteCount - oldByteCount;

        if (byteDelta != 0) {
            updateOffsets(new OffsetUpdater() {
                @Override
                public long updateInstructionOffset(long address) {
                    if (address > instruction.getAddress()) {
                        return address + byteDelta;
                    }
                    return address;
                }

                @Override
                public int updateOperandOffset(long insAddr, long targetAddress, int offset) {
                    if (targetAddress > instruction.getAddress() && insAddr <= instruction.getAddress()) {
                        return offset + byteDelta;
                    }
                    if (targetAddress <= instruction.getAddress() && insAddr > instruction.getAddress()) {
                        return offset - byteDelta;
                    }
                    return offset;
                }
            }, body);
        }
        code.set(pos, instruction);
    }

    /**
     * Inserts instruction at specified point. Handles offsets properly. Note:
     * If newinstruction is jump, the offset operand must be handled properly by
     * caller. All old jump offsets to pos are targeted before new instruction.
     *
     * @param pos Position
     * @param instruction Instruction False means before new instruction
     * @param body Method body (used for try handling)
     */
    public void insertInstruction(int pos, AVM2Instruction instruction, MethodBody body) {
        insertInstruction(pos, instruction, false, body);
    }

    /**
     * Inserts instruction at specified point. Handles offsets properly. Note:
     * If newinstruction is jump, the offset operand must be handled properly by
     * caller.
     *
     * @param pos Position
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
        if (code.size() == 0) {
            instruction.setAddress(0);
        } else if (pos == code.size()) {
            instruction.setAddress(code.get(pos - 1).getAddress() + code.get(pos - 1).getBytesLength());
        } else {
            instruction.setAddress(code.get(pos).getAddress());
        }
        final long x = instruction.getAddress();
        updateOffsets(new OffsetUpdater() {
            @Override
            public long updateInstructionOffset(long offset) {
                if (offset >= x) {
                    return offset + byteCount;
                }
                return offset;
            }

            @Override
            public int updateOperandOffset(long j, long t, int offset_jt) {

                /*
                 j:jump t:
                 n:
                 n:
                 x:#
                 t:
                 */
                if (((t > x) || (mapOffsetsAfterIns && (t == x))) && (j < x)) {
                    return offset_jt + byteCount;
                }
                /*
                 t:
                 x:#
                 n:
                 n:
                 j:jump t:
                 */
                if (((t < x) || (mapOffsetsAfterIns && (t == x))) && (j > x)) {
                    return offset_jt - byteCount;
                }

                /*
                 t:
                 n:
                 n:
                 j:x: # jump t:
                 */
                if ((j == x) && (t < x)) {
                    return offset_jt - byteCount;
                }

                return offset_jt;
            }
        }, body);
        instruction.setAddress(x);
        code.add(pos, instruction);
        //checkValidOffsets(body);
    }

    /**
     * Removes traps (deobfuscation)
     *
     * @param trait Trait
     * @param methodInfo Method info
     * @param body Method body
     * @param abc ABC
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic True if static
     * @param path Path
     * @return 1
     * @throws InterruptedException On interrupt
     */
    public int removeTraps(Trait trait, int methodInfo, MethodBody body, ABC abc, int scriptIndex, int classIndex, boolean isStatic, String path) throws InterruptedException {
        SWFDecompilerPlugin.fireAvm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
        try (Statistics s = new Statistics("AVM2DeobfuscatorGetSet")) {
            new AVM2DeobfuscatorGetSet().avm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
        }
        try (Statistics s = new Statistics("AVM2DeobfuscatorSimple")) {
            new AVM2DeobfuscatorSimpleOld().avm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
        }
        try (Statistics s = new Statistics("AVM2DeobfuscatorRegisters")) {
            new AVM2DeobfuscatorRegistersOld().avm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
        }
        try (Statistics s = new Statistics("AVM2DeobfuscatorJumps")) {
            new AVM2DeobfuscatorJumps().avm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
        }
        try (Statistics s = new Statistics("AVM2DeobfuscatorZeroJumpsNullPushes")) {
            new AVM2DeobfuscatorZeroJumpsNullPushes().avm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
        }
        return 1;
    }

    /**
     * Handles register while walking code for stats.
     *
     * @param stats Code stats
     * @param reg Register
     */
    private void handleRegister(CodeStats stats, int reg) {
        if (reg + 1 > stats.maxlocal) {
            stats.maxlocal = reg + 1;
        }
    }

    /**
     * Walks code for stats.
     *
     * @param stats Code stats
     * @param pos Position
     * @param stack Stack
     * @param scope Scope
     * @param abc ABC
     * @param autoFill Auto fill
     * @return True if success
     */
    private boolean walkCode(CodeStats stats, int pos, int stack, int scope, ABC abc, boolean autoFill) {
        while (pos < code.size()) {
            AVM2Instruction ins = code.get(pos);
            if (stats.instructionStats[pos].seen) {
                // check stack mismatch here
                return true;
            }

            if (ins.definition instanceof NewFunctionIns) {
                MethodBody innerBody = abc.findBody(ins.operands[0]);
                if (autoFill) {
                    innerBody.autoFillStats(abc, stats.initscope + (stats.has_activation ? 1 : 0), false);
                }
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
                    int op = ins.definition.operands[i];
                    if (op == DAT_LOCAL_REG_INDEX) {
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
            if (ins.definition instanceof ThrowIns) {
                return true;
            }
            if (ins.definition instanceof JumpIns) {
                try {
                    pos = adr2pos(ins.getTargetAddress());
                    continue;
                } catch (ConvertException ex) {
                    return false;
                }
            } else if (ins.definition instanceof IfTypeIns) {
                try {
                    int newpos = adr2pos(ins.getTargetAddress());
                    walkCode(stats, newpos, stack, scope, abc, autoFill);
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
                        if (!walkCode(stats, newpos, stack, scope, abc, autoFill)) {
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
     * Gets stats.
     *
     * @param abc ABC
     * @param body Method body
     * @param initScope Initial scope
     * @param autoFill Auto fill
     * @return Code stats
     */
    public CodeStats getStats(ABC abc, MethodBody body, int initScope, boolean autoFill) {
        CodeStats stats = new CodeStats(this);
        stats.initscope = initScope;
        if (!walkCode(stats, 0, 0, initScope, abc, autoFill)) {
            return null;
        }
        int scopePos = -1;
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
                if (!walkCode(stats, adr2pos(ex.target), 1, scopePos, abc, autoFill)) {
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
                int nextIp = maxIp + 1;
                if (code.get(maxIp).definition instanceof JumpIns) {
                    nextIp = adr2pos(pos2adr(nextIp) + code.get(maxIp).operands[0]);
                }
            } catch (ConvertException ex1) {
                // ignore
            }
        }
        //stats.maxscope+=initScope;
        return stats;
    }

    /**
     * Calculates maxlocal value. Simplified version of getStats.
     *
     * @return Code stats
     */
    public CodeStats getMaxLocal() {
        CodeStats stats = new CodeStats();
        for (AVM2Instruction ins : code) {
            if (ins.definition instanceof SetLocalTypeIns) {
                handleRegister(stats, ((SetLocalTypeIns) ins.definition).getRegisterId(ins));
            } else if (ins.definition instanceof GetLocalTypeIns) {
                handleRegister(stats, ((GetLocalTypeIns) ins.definition).getRegisterId(ins));
            } else {
                for (int i = 0; i < ins.definition.operands.length; i++) {
                    int op = ins.definition.operands[i];
                    if (op == DAT_LOCAL_REG_INDEX) {
                        handleRegister(stats, ins.operands[i]);
                    }
                }
            }
        }

        return stats;
    }

    /**
     * Visit code.
     *
     * @param ip Position
     * @param lastIp Last position
     * @param refs Map from position to list of references
     * @throws InterruptedException On interrupt
     */
    private void visitCode(int ip, int lastIp, HashMap<Integer, List<Integer>> refs) throws InterruptedException {
        Queue<Integer> toVisit = new LinkedList<>();
        Queue<Integer> toVisitLast = new LinkedList<>();
        toVisit.add(ip);
        toVisitLast.add(lastIp);
        while (!toVisit.isEmpty()) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            ip = toVisit.remove();
            lastIp = toVisitLast.remove();
            while (ip < code.size()) {
                if (!refs.containsKey(ip)) {
                    refs.put(ip, new ArrayList<>(2));
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
                        }
                        ip = adr2pos(pos2adr(ip) + ins.operands[0]);
                        continue;
                    } catch (ConvertException ex) {
                        //ignored
                    }
                }
                if (ins.definition instanceof JumpIns) {
                    try {
                        ip = adr2pos(ins.getTargetAddress());
                        continue;
                    } catch (ConvertException ex) {
                        logger.log(Level.FINE, null, ex);
                    }
                } else if (ins.definition instanceof IfTypeIns) {
                    try {
                        toVisit.add(adr2pos(ins.getTargetAddress()));
                        toVisitLast.add(ip);
                    } catch (ConvertException ex) {
                        logger.log(Level.FINE, null, ex);
                    }
                }
                ip++;
            }
        }
    }

    /**
     * Visits code.
     *
     * @param body Method body
     * @return Map from position to list of references
     * @throws InterruptedException On interrupt
     */
    public HashMap<Integer, List<Integer>> visitCode(MethodBody body) throws InterruptedException {
        HashMap<Integer, List<Integer>> refs = new HashMap<>();
        for (int i = 0; i < code.size(); i++) {
            refs.put(i, new ArrayList<>());
        }
        visitCode(0, 0, refs);
        for (ABCException e : body.exceptions) {
            try {
                visitCode(adr2pos(e.target), -1, refs);
            } catch (ConvertException ex) {
                logger.log(Level.SEVERE, "Visitcode error", ex);
            }
        }
        return refs;
    }

    /**
     * Remove instructions that are marked as ignored.
     *
     * @param body Method body
     * @throws InterruptedException On interrupt
     */
    public void removeIgnored(MethodBody body) throws InterruptedException {
        //System.err.println("removing ignored...");
        for (int i = 0; i < code.size(); i++) {
            if (code.get(i).isIgnored()) {
                removeInstruction(i, body);
                i--;
            }
        }
        //System.err.println("/ignored removed");
    }

    /**
     * Removes dead code.
     *
     * @param body Method body
     * @return Number of removed instructions
     * @throws InterruptedException On interrupt
     */
    public int removeDeadCode(MethodBody body) throws InterruptedException {
        return removeDeadCode(body, new Reference<>(-1));
    }

    /**
     * Removes dead code.
     *
     * @param body Method body
     * @param minChangedIpRef Minimum changed instruction position (as
     * reference)
     * @return Number of removed instructions
     * @throws InterruptedException On interrupt
     */
    public int removeDeadCode(MethodBody body, Reference<Integer> minChangedIpRef) throws InterruptedException {
        HashMap<Integer, List<Integer>> refs = visitCode(body);
        int cnt = 0;
        Integer minChangedIp = -1;
        for (int i = code.size() - 1; i >= 0; i--) {
            if (refs.get(i).isEmpty()) {
                minChangedIp = i;
                code.get(i).setIgnored(true, 0);
                cnt++;
            }
        }

        removeIgnored(body);

        for (int i = code.size() - 1; i >= 0; i--) {
            AVM2Instruction ins = code.get(i);
            if (ins.definition instanceof JumpIns) {
                if (ins.operands[0] == 0) {
                    ins.setIgnored(true, 0);
                    if (minChangedIp == -1 || minChangedIp > i) {
                        minChangedIp = i;
                    }
                    cnt++;
                }
            }
        }

        removeIgnored(body);

        minChangedIpRef.setVal(minChangedIp);

        return cnt;
    }

    /**
     * Replaces jumps to exit instructions (return, throw) with exit
     * instruction.
     *
     * @return True if modified
     */
    public boolean inlineJumpExit() {
        boolean modified = false;
        int csize = code.size();
        for (int i = 0; i < csize; i++) {
            AVM2Instruction ins = code.get(i);
            int insLen = code.get(i).getBytesLength();
            long ofs = pos2adr(i);
            if (ins.definition instanceof JumpIns) {
                long targetOfs = ofs + insLen + ins.operands[0];
                try {
                    int ni = adr2pos(targetOfs);
                    if (ni < code.size() && ni > -1) {
                        AVM2Instruction ins2 = code.get(ni);
                        if (ins2.isExit()) {
                            code.set(i, new AVM2Instruction(ofs, ins2.definition, ins2.operands));
                            modified = true;
                        }
                    }
                } catch (ConvertException ex) {
                    //ignore
                }
            }
        }

        return modified;
    }

    /**
     * Gets reachable positions.
     *
     * @param code AVM2 code
     * @param ip Current position
     * @param reachable Result - list of reachable positions
     */
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

    /**
     * Checks if currentIp is direct ancestor.
     *
     * @param currentIp Current position
     * @param ancestor Ancestor position
     * @param refs Map from position to list of references
     * @return True if currentIp is direct ancestor
     */
    public static boolean isDirectAncestor(int currentIp, int ancestor, HashMap<Integer, List<Integer>> refs) {
        return isDirectAncestor(currentIp, ancestor, refs, new ArrayList<>());
    }

    /**
     * Checks if currentIp is direct ancestor.
     *
     * @param currentIp Current position
     * @param ancestor Ancestor position
     * @param refs Map from position to list of references
     * @param visited List of visited positions
     * @return True if currentIp is direct ancestor
     */
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

    /**
     * Gets reachable positions prior the current position.
     *
     * @param currentIp Current position
     * @param refs Map from position to list of references
     * @param reachable Result - set of reachable positions
     * @param visited List of visited positions
     * @return True if visited does not contain currentIp
     */
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

    /**
     * Clones AVM2 code.
     *
     * @return Cloned AVM2 code
     */
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

            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    /**
     * Marks virtual addresses. Virtual address is the address of the
     * instruction before any modifications to the code like deobfuscation etc.
     */
    public void markVirtualAddresses() {
        for (AVM2Instruction ins : code) {
            ins.setVirtualAddress(ins.getAddress());
        }
    }
}
