/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

/**
 * Set of AVM2 instructions.
 *
 * @author JPEXS
 */
public class AVM2Instructions {

    /**
     * Constructor.
     */
    private AVM2Instructions() {

    }

    /**
     * bkpt
     */
    public static final int Bkpt = 0x01;

    /**
     * nop
     */
    public static final int Nop = 0x02;

    /**
     * throw
     */
    public static final int Throw = 0x03;

    /**
     * getsuper
     */
    public static final int GetSuper = 0x04;

    /**
     * setsuper
     */
    public static final int SetSuper = 0x05;

    /**
     * dxns
     */
    public static final int DXNS = 0x06;

    /**
     * dxnslate
     */
    public static final int DXNSLate = 0x07;

    /**
     * kill
     */
    public static final int Kill = 0x08;

    /**
     * label
     */
    public static final int Label = 0x09;

    /**
     * lf32x4
     */
    public static final int Lf32x4 = 0x0A;

    /**
     * sf32x4
     */
    public static final int Sf32x4 = 0x0B;

    /**
     * ifnlt
     */
    public static final int IfNLt = 0x0C;

    /**
     * ifnle
     */
    public static final int IfNLe = 0x0D;

    /**
     * ifngt
     */
    public static final int IfNGt = 0x0E;

    /**
     * ifnge
     */
    public static final int IfNGe = 0x0F;

    /**
     * jump
     */
    public static final int Jump = 0x10;

    /**
     * iftrue
     */
    public static final int IfTrue = 0x11;

    /**
     * iffalse
     */
    public static final int IfFalse = 0x12;

    /**
     * ifeq
     */
    public static final int IfEq = 0x13;

    /**
     * ifne
     */
    public static final int IfNe = 0x14;

    /**
     * iflt
     */
    public static final int IfLt = 0x15;

    /**
     * ifle
     */
    public static final int IfLe = 0x16;

    /**
     * ifgt
     */
    public static final int IfGt = 0x17;

    /**
     * ifge
     */
    public static final int IfGe = 0x18;

    /**
     * ifstricteq
     */
    public static final int IfStrictEq = 0x19;

    /**
     * ifstrictne
     */
    public static final int IfStrictNe = 0x1A;

    /**
     * lookupswitch
     */
    public static final int LookupSwitch = 0x1B;

    /**
     * pushwith
     */
    public static final int PushWith = 0x1C;

    /**
     * popscope
     */
    public static final int PopScope = 0x1D;

    /**
     * nextname
     */
    public static final int NextName = 0x1E;

    /**
     * hasnext
     */
    public static final int HasNext = 0x1F;

    /**
     * pushnull
     */
    public static final int PushNull = 0x20;

    /**
     * pushundefined
     */
    public static final int PushUndefined = 0x21;

    /**
     * pushconstant
     */
    public static final int PushConstant = 0x22;

    /**
     * nextvalue
     */
    public static final int PushFloat = 0x22;

    /**
     * nextvalue
     */
    public static final int NextValue = 0x23;

    /**
     * pushbyte
     */
    public static final int PushByte = 0x24;

    /**
     * pushshort
     */
    public static final int PushShort = 0x25;

    /**
     * pushtrue
     */
    public static final int PushTrue = 0x26;

    /**
     * pushfalse
     */
    public static final int PushFalse = 0x27;

    /**
     * pushnan
     */
    public static final int PushNan = 0x28;

    /**
     * pop
     */
    public static final int Pop = 0x29;

    /**
     * dup
     */
    public static final int Dup = 0x2A;

    /**
     * swap
     */
    public static final int Swap = 0x2B;

    /**
     * pushstring
     */
    public static final int PushString = 0x2C;

    /**
     * pushint
     */
    public static final int PushInt = 0x2D;

    /**
     * pushuint
     */
    public static final int PushUInt = 0x2E;

    /**
     * pushdouble
     */
    public static final int PushDouble = 0x2F;

    /**
     * pushscope
     */
    public static final int PushScope = 0x30;

    /**
     * pushnamespace
     */
    public static final int PushNamespace = 0x31;

    /**
     * hasnext2
     */
    public static final int HasNext2 = 0x32;

    /**
     * pushdecimal
     */
    public static final int PushDecimal = 0x33;

    /**
     * pushdnan
     */
    public static final int PushDNan = 0x34;

    /**
     * li8
     */
    public static final int Li8 = 0x35;

    /**
     * li16
     */
    public static final int Li16 = 0x36;

    /**
     * li32
     */
    public static final int Li32 = 0x37;

    /**
     * lf32
     */
    public static final int Lf32 = 0x38;

    /**
     * lf64
     */
    public static final int Lf64 = 0x39;

    /**
     * si8
     */
    public static final int Si8 = 0x3A;

    /**
     * si16
     */
    public static final int Si16 = 0x3B;

    /**
     * si32
     */
    public static final int Si32 = 0x3C;

    /**
     * sf32
     */
    public static final int Sf32 = 0x3D;

    /**
     * sf64
     */
    public static final int Sf64 = 0x3E;

    /**
     * newfunction
     */
    public static final int NewFunction = 0x40;

    /**
     * call
     */
    public static final int Call = 0x41;

    /**
     * construct
     */
    public static final int Construct = 0x42;

    /**
     * callmethod
     */
    public static final int CallMethod = 0x43;

    /**
     * callstatic
     */
    public static final int CallStatic = 0x44;

    /**
     * callsuper
     */
    public static final int CallSuper = 0x45;

    /**
     * callproperty
     */
    public static final int CallProperty = 0x46;

    /**
     * returnvoid
     */
    public static final int ReturnVoid = 0x47;

    /**
     * returnvalue
     */
    public static final int ReturnValue = 0x48;

    /**
     * constructsuper
     */
    public static final int ConstructSuper = 0x49;

    /**
     * constructprop
     */
    public static final int ConstructProp = 0x4A;

    /**
     * callproplex
     */
    public static final int CallSuperId = 0x4B;

    /**
     * callproplex
     */
    public static final int CallPropLex = 0x4C;

    /**
     * callinterface
     */
    public static final int CallInterface = 0x4D;

    /**
     * callsupervoid
     */
    public static final int CallSuperVoid = 0x4E;

    /**
     * callpropvoid
     */
    public static final int CallPropVoid = 0x4F;

    /**
     * sxi1
     */
    public static final int Sxi1 = 0x50;

    /**
     * sxi8
     */
    public static final int Sxi8 = 0x51;

    /**
     * sxi16
     */
    public static final int Sxi16 = 0x52;

    /**
     * applytype
     */
    public static final int ApplyType = 0x53;

    /**
     * pushfloat4
     */
    public static final int PushFloat4 = 0x54;

    /**
     * newobject
     */
    public static final int NewObject = 0x55;

    /**
     * newarray
     */
    public static final int NewArray = 0x56;

    /**
     * newactivation
     */
    public static final int NewActivation = 0x57;

    /**
     * newclass
     */
    public static final int NewClass = 0x58;

    /**
     * getdescendants
     */
    public static final int GetDescendants = 0x59;

    /**
     * newcatch
     */
    public static final int NewCatch = 0x5A;

    /**
     * deldescendants
     */
    public static final int DelDescendants = 0x5B;

    /**
     * findpropglobal
     */
    public static final int FindPropGlobal = 0x5C;

    /**
     * findpropstrict
     */
    public static final int FindPropertyStrict = 0x5D;

    /**
     * findproperty
     */
    public static final int FindProperty = 0x5E;

    /**
     * finddef
     */
    public static final int FindDef = 0x5F;

    /**
     * getlex
     */
    public static final int GetLex = 0x60;

    /**
     * setproperty
     */
    public static final int SetProperty = 0x61;

    /**
     * getlocal
     */
    public static final int GetLocal = 0x62;

    /**
     * setlocal
     */
    public static final int SetLocal = 0x63;

    /**
     * getglobalscope
     */
    public static final int GetGlobalScope = 0x64;

    /**
     * getscopeobject
     */
    public static final int GetScopeObject = 0x65;

    /**
     * getproperty
     */
    public static final int GetProperty = 0x66;

    /**
     * getouterscope
     */
    public static final int GetOuterScope = 0x67;

    /**
     * initproperty
     */
    public static final int InitProperty = 0x68;

    /**
     * setpropertylate
     */
    public static final int SetPropertyLate = 0x69;

    /**
     * deleteproperty
     */
    public static final int DeleteProperty = 0x6A;

    /**
     * deletepropertylate
     */
    public static final int DeletePropertyLate = 0x6B;

    /**
     * getslot
     */
    public static final int GetSlot = 0x6C;

    /**
     * setslot
     */
    public static final int SetSlot = 0x6D;

    /**
     * getglobalslot
     */
    public static final int GetGlobalSlot = 0x6E;

    /**
     * setglobalslot
     */
    public static final int SetGlobalSlot = 0x6F;

    /**
     * convert_s
     */
    public static final int ConvertS = 0x70;

    /**
     * esc_xelem
     */
    public static final int EscXElem = 0x71;

    /**
     * esc_xattr
     */
    public static final int EscXAttr = 0x72;

    /**
     * convert_i
     */
    public static final int ConvertI = 0x73;

    /**
     * convert_u
     */
    public static final int ConvertU = 0x74;

    /**
     * convert_d
     */
    public static final int ConvertD = 0x75;

    /**
     * convert_b
     */
    public static final int ConvertB = 0x76;

    /**
     * convert_o
     */
    public static final int ConvertO = 0x77;

    /**
     * checkfilter
     */
    public static final int CheckFilter = 0x78;

    /**
     * convert_m
     */
    public static final int ConvertM = 0x79;

    /**
     * convert_f
     */
    public static final int ConvertF = 0x79;

    /**
     * convert_mp
     */
    public static final int ConvertMP = 0x7A;

    /**
     * unplus
     */
    public static final int UnPlus = 0x7A;

    /**
     * convert_f4
     */
    public static final int ConvertF4 = 0x7B;

    /**
     * coerce
     */
    public static final int Coerce = 0x80;

    /**
     * coerce_b
     */
    public static final int CoerceB = 0x81;

    /**
     * coerce_a
     */
    public static final int CoerceA = 0x82;

    /**
     * coerce_i
     */
    public static final int CoerceI = 0x83;

    /**
     * coerce_d
     */
    public static final int CoerceD = 0x84;

    /**
     * coerce_s
     */
    public static final int CoerceS = 0x85;

    /**
     * astype
     */
    public static final int AsType = 0x86;

    /**
     * astypelate
     */
    public static final int AsTypeLate = 0x87;

    /**
     * coerce_u
     */
    public static final int CoerceU = 0x88;

    /**
     * coerce_o
     */
    public static final int CoerceO = 0x89;

    /**
     * negate_p
     */
    public static final int NegateP = 0x8F;

    /**
     * negate
     */
    public static final int Negate = 0x90;

    /**
     * increment
     */
    public static final int Increment = 0x91;

    /**
     * inclocal
     */
    public static final int IncLocal = 0x92;

    /**
     * decrement
     */
    public static final int Decrement = 0x93;

    /**
     * declocal
     */
    public static final int DecLocal = 0x94;

    /**
     * typeof
     */
    public static final int TypeOf = 0x95;

    /**
     * not
     */
    public static final int Not = 0x96;

    /**
     * bitnot
     */
    public static final int BitNot = 0x97;

    /**
     * concat
     */
    public static final int Concat = 0x9A;

    /**
     * add_d
     */
    public static final int AddD = 0x9B;

    /**
     * increment_p
     */
    public static final int IncrementP = 0x9C;

    /**
     * inclocal_p
     */
    public static final int IncLocalP = 0x9D;

    /**
     * decrement_p
     */
    public static final int DecrementP = 0x9E;

    /**
     * declocal_p
     */
    public static final int DecLocalP = 0x9F;

    /**
     * add
     */
    public static final int Add = 0xA0;

    /**
     * subtract
     */
    public static final int Subtract = 0xA1;

    /**
     * multiply
     */
    public static final int Multiply = 0xA2;

    /**
     * divide
     */
    public static final int Divide = 0xA3;

    /**
     * modulo
     */
    public static final int Modulo = 0xA4;

    /**
     * lshift
     */
    public static final int LShift = 0xA5;

    /**
     * rshift
     */
    public static final int RShift = 0xA6;

    /**
     * urshift
     */
    public static final int URShift = 0xA7;

    /**
     * bitand
     */
    public static final int BitAnd = 0xA8;

    /**
     * bitor
     */
    public static final int BitOr = 0xA9;

    /**
     * bitxor
     */
    public static final int BitXor = 0xAA;

    /**
     * equals
     */
    public static final int Equals = 0xAB;

    /**
     * strictequals
     */
    public static final int StrictEquals = 0xAC;

    /**
     * lessthan
     */
    public static final int LessThan = 0xAD;

    /**
     * lessequals
     */
    public static final int LessEquals = 0xAE;

    /**
     * greaterthan
     */
    public static final int GreaterThan = 0xAF;

    /**
     * greaterequals
     */
    public static final int GreaterEquals = 0xB0;

    /**
     * instanceof
     */
    public static final int InstanceOf = 0xB1;

    /**
     * istype
     */
    public static final int IsType = 0xB2;

    /**
     * istypelate
     */
    public static final int IsTypeLate = 0xB3;

    /**
     * in
     */
    public static final int In = 0xB4;

    /**
     * add_p
     */
    public static final int AddP = 0xB5;

    /**
     * subtract_p
     */
    public static final int SubtractP = 0xB6;

    /**
     * multiply_p
     */
    public static final int MultiplyP = 0xB7;

    /**
     * divide_p
     */
    public static final int DivideP = 0xB8;

    /**
     * modulo_p
     */
    public static final int ModuloP = 0xB9;

    /**
     * increment_i
     */
    public static final int IncrementI = 0xC0;

    /**
     * decrement_i
     */
    public static final int DecrementI = 0xC1;

    /**
     * inclocal_i
     */
    public static final int IncLocalI = 0xC2;

    /**
     * declocal_i
     */
    public static final int DecLocalI = 0xC3;

    /**
     * negate_i
     */
    public static final int NegateI = 0xC4;

    /**
     * add_i
     */
    public static final int AddI = 0xC5;

    /**
     * subtract_i
     */
    public static final int SubtractI = 0xC6;

    /**
     * multiply_i
     */
    public static final int MultiplyI = 0xC7;

    /**
     * getlocal0
     */
    public static final int GetLocal0 = 0xD0;

    /**
     * getlocal1
     */
    public static final int GetLocal1 = 0xD1;

    /**
     * getlocal2
     */
    public static final int GetLocal2 = 0xD2;

    /**
     * getlocal3
     */
    public static final int GetLocal3 = 0xD3;

    /**
     * setlocal0
     */
    public static final int SetLocal0 = 0xD4;

    /**
     * setlocal1
     */
    public static final int SetLocal1 = 0xD5;

    /**
     * setlocal2
     */
    public static final int SetLocal2 = 0xD6;

    /**
     * setlocal3
     */
    public static final int SetLocal3 = 0xD7;

    /**
     * invalid
     */
    public static final int Invalid = 0xED;

    /**
     * abs_jump
     */
    public static final int AbsJump = 0xEE;

    /**
     * debug
     */
    public static final int Debug = 0xEF;

    /**
     * debugline
     */
    public static final int DebugLine = 0xF0;

    /**
     * debugfile
     */
    public static final int DebugFile = 0xF1;

    /**
     * bkptline
     */
    public static final int BkptLine = 0xF2;

    /**
     * timestamp
     */
    public static final int Timestamp = 0xF3;

    /**
     * verifypass
     */
    public static final int VerifyPass = 0xF5;

    /**
     * alloc
     */
    public static final int Alloc = 0xF6;

    /**
     * mark
     */
    public static final int Mark = 0xF7;

    /**
     * wb
     */
    public static final int Wb = 0xF8;

    /**
     * prologue
     */
    public static final int Prologue = 0xF9;

    /**
     * sendenter
     */
    public static final int SendEnter = 0xFA;

    /**
     * doubletoatom
     */
    public static final int DoubleToAtom = 0xFB;

    /**
     * sweep
     */
    public static final int Sweep = 0xFC;

    /**
     * codegenop
     */
    public static final int CodeGenOp = 0xFD;

    /**
     * verifyop
     */
    public static final int VerifyOp = 0xFE;

    /**
     * decode
     */
    public static final int Decode = 0xFF;
}
