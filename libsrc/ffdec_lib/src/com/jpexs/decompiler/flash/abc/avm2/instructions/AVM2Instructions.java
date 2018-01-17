/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.instructions;

/**
 *
 * @author JPEXS
 */
public class AVM2Instructions {

    public static final int Bkpt = 0x01;

    public static final int Nop = 0x02;

    public static final int Throw = 0x03;

    public static final int GetSuper = 0x04;

    public static final int SetSuper = 0x05;

    public static final int DXNS = 0x06;

    public static final int DXNSLate = 0x07;

    public static final int Kill = 0x08;

    public static final int Label = 0x09;

    public static final int Lf32x4 = 0x0A;

    public static final int Sf32x4 = 0x0B;

    public static final int IfNLt = 0x0C;

    public static final int IfNLe = 0x0D;

    public static final int IfNGt = 0x0E;

    public static final int IfNGe = 0x0F;

    public static final int Jump = 0x10;

    public static final int IfTrue = 0x11;

    public static final int IfFalse = 0x12;

    public static final int IfEq = 0x13;

    public static final int IfNe = 0x14;

    public static final int IfLt = 0x15;

    public static final int IfLe = 0x16;

    public static final int IfGt = 0x17;

    public static final int IfGe = 0x18;

    public static final int IfStrictEq = 0x19;

    public static final int IfStrictNe = 0x1A;

    public static final int LookupSwitch = 0x1B;

    public static final int PushWith = 0x1C;

    public static final int PopScope = 0x1D;

    public static final int NextName = 0x1E;

    public static final int HasNext = 0x1F;

    public static final int PushNull = 0x20;

    public static final int PushUndefined = 0x21;

    public static final int PushConstant = 0x22;

    public static final int PushFloat = 0x22;

    public static final int NextValue = 0x23;

    public static final int PushByte = 0x24;

    public static final int PushShort = 0x25;

    public static final int PushTrue = 0x26;

    public static final int PushFalse = 0x27;

    public static final int PushNan = 0x28;

    public static final int Pop = 0x29;

    public static final int Dup = 0x2A;

    public static final int Swap = 0x2B;

    public static final int PushString = 0x2C;

    public static final int PushInt = 0x2D;

    public static final int PushUInt = 0x2E;

    public static final int PushDouble = 0x2F;

    public static final int PushScope = 0x30;

    public static final int PushNamespace = 0x31;

    public static final int HasNext2 = 0x32;

    public static final int PushDecimal = 0x33;

    public static final int PushDNan = 0x34;

    public static final int Li8 = 0x35;

    public static final int Li16 = 0x36;

    public static final int Li32 = 0x37;

    public static final int Lf32 = 0x38;

    public static final int Lf64 = 0x39;

    public static final int Si8 = 0x3A;

    public static final int Si16 = 0x3B;

    public static final int Si32 = 0x3C;

    public static final int Sf32 = 0x3D;

    public static final int Sf64 = 0x3E;

    public static final int NewFunction = 0x40;

    public static final int Call = 0x41;

    public static final int Construct = 0x42;

    public static final int CallMethod = 0x43;

    public static final int CallStatic = 0x44;

    public static final int CallSuper = 0x45;

    public static final int CallProperty = 0x46;

    public static final int ReturnVoid = 0x47;

    public static final int ReturnValue = 0x48;

    public static final int ConstructSuper = 0x49;

    public static final int ConstructProp = 0x4A;

    public static final int CallSuperId = 0x4B;

    public static final int CallPropLex = 0x4C;

    public static final int CallInterface = 0x4D;

    public static final int CallSuperVoid = 0x4E;

    public static final int CallPropVoid = 0x4F;

    public static final int Sxi1 = 0x50;

    public static final int Sxi8 = 0x51;

    public static final int Sxi16 = 0x52;

    public static final int ApplyType = 0x53;

    public static final int PushFloat4 = 0x54;

    public static final int NewObject = 0x55;

    public static final int NewArray = 0x56;

    public static final int NewActivation = 0x57;

    public static final int NewClass = 0x58;

    public static final int GetDescendants = 0x59;

    public static final int NewCatch = 0x5A;

    public static final int DelDescendants = 0x5B;

    public static final int FindPropGlobal = 0x5C;

    public static final int FindPropertyStrict = 0x5D;

    public static final int FindProperty = 0x5E;

    public static final int FindDef = 0x5F;

    public static final int GetLex = 0x60;

    public static final int SetProperty = 0x61;

    public static final int GetLocal = 0x62;

    public static final int SetLocal = 0x63;

    public static final int GetGlobalScope = 0x64;

    public static final int GetScopeObject = 0x65;

    public static final int GetProperty = 0x66;

    public static final int GetOuterScope = 0x67;

    public static final int InitProperty = 0x68;

    public static final int SetPropertyLate = 0x69;

    public static final int DeleteProperty = 0x6A;

    public static final int DeletePropertyLate = 0x6B;

    public static final int GetSlot = 0x6C;

    public static final int SetSlot = 0x6D;

    public static final int GetGlobalSlot = 0x6E;

    public static final int SetGlobalSlot = 0x6F;

    public static final int ConvertS = 0x70;

    public static final int EscXElem = 0x71;

    public static final int EscXAttr = 0x72;

    public static final int ConvertI = 0x73;

    public static final int ConvertU = 0x74;

    public static final int ConvertD = 0x75;

    public static final int ConvertB = 0x76;

    public static final int ConvertO = 0x77;

    public static final int CheckFilter = 0x78;

    public static final int ConvertM = 0x79;

    public static final int ConvertMP = 0x7A;

    public static final int Coerce = 0x80;

    public static final int CoerceB = 0x81;

    public static final int CoerceA = 0x82;

    public static final int CoerceI = 0x83;

    public static final int CoerceD = 0x84;

    public static final int CoerceS = 0x85;

    public static final int AsType = 0x86;

    public static final int AsTypeLate = 0x87;

    public static final int CoerceU = 0x88;

    public static final int CoerceO = 0x89;

    public static final int NegateP = 0x8F;

    public static final int Negate = 0x90;

    public static final int Increment = 0x91;

    public static final int IncLocal = 0x92;

    public static final int Decrement = 0x93;

    public static final int DecLocal = 0x94;

    public static final int TypeOf = 0x95;

    public static final int Not = 0x96;

    public static final int BitNot = 0x97;

    public static final int Concat = 0x9A;

    public static final int AddD = 0x9B;

    public static final int IncrementP = 0x9C;

    public static final int IncLocalP = 0x9D;

    public static final int DecrementP = 0x9E;

    public static final int DecLocalP = 0x9F;

    public static final int Add = 0xA0;

    public static final int Subtract = 0xA1;

    public static final int Multiply = 0xA2;

    public static final int Divide = 0xA3;

    public static final int Modulo = 0xA4;

    public static final int LShift = 0xA5;

    public static final int RShift = 0xA6;

    public static final int URShift = 0xA7;

    public static final int BitAnd = 0xA8;

    public static final int BitOr = 0xA9;

    public static final int BitXor = 0xAA;

    public static final int Equals = 0xAB;

    public static final int StrictEquals = 0xAC;

    public static final int LessThan = 0xAD;

    public static final int LessEquals = 0xAE;

    public static final int GreaterThan = 0xAF;

    public static final int GreaterEquals = 0xB0;

    public static final int InstanceOf = 0xB1;

    public static final int IsType = 0xB2;

    public static final int IsTypeLate = 0xB3;

    public static final int In = 0xB4;

    public static final int AddP = 0xB5;

    public static final int SubtractP = 0xB6;

    public static final int MultiplyP = 0xB7;

    public static final int DivideP = 0xB8;

    public static final int ModuloP = 0xB9;

    public static final int IncrementI = 0xC0;

    public static final int DecrementI = 0xC1;

    public static final int IncLocalI = 0xC2;

    public static final int DecLocalI = 0xC3;

    public static final int NegateI = 0xC4;

    public static final int AddI = 0xC5;

    public static final int SubtractI = 0xC6;

    public static final int MultiplyI = 0xC7;

    public static final int GetLocal0 = 0xD0;

    public static final int GetLocal1 = 0xD1;

    public static final int GetLocal2 = 0xD2;

    public static final int GetLocal3 = 0xD3;

    public static final int SetLocal0 = 0xD4;

    public static final int SetLocal1 = 0xD5;

    public static final int SetLocal2 = 0xD6;

    public static final int SetLocal3 = 0xD7;

    public static final int Invalid = 0xED;

    public static final int AbsJump = 0xEE;

    public static final int Debug = 0xEF;

    public static final int DebugLine = 0xF0;

    public static final int DebugFile = 0xF1;

    public static final int BkptLine = 0xF2;

    public static final int Timestamp = 0xF3;

    public static final int VerifyPass = 0xF5;

    public static final int Alloc = 0xF6;

    public static final int Mark = 0xF7;

    public static final int Wb = 0xF8;

    public static final int Prologue = 0xF9;

    public static final int SendEnter = 0xFA;

    public static final int UnPlus = 0xFA;

    public static final int DoubleToAtom = 0xFB;

    public static final int ConvertF4 = 0x7B;

    public static final int Sweep = 0xFC;

    public static final int CodeGenOp = 0xFD;

    public static final int VerifyOp = 0xFE;

    public static final int Decode = 0xFF;
}
