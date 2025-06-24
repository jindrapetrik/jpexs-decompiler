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
package com.jpexs.decompiler.flash.abc.avm2.parser.pcode;

/**
 * AVM2 P-code parser symbol.
 *
 * @author JPEXS
 */
public class ParsedSymbol {

    /**
     * Position
     */
    public int pos;

    /**
     * Type
     */
    public int type;

    /**
     * Value
     */
    public Object value;

    /**
     * Type: String
     */
    public static final int TYPE_STRING = 1;

    /**
     * Type: Multiname
     */
    public static final int TYPE_MULTINAME = 2;

    /**
     * Type: Instruction name
     */
    public static final int TYPE_INSTRUCTION_NAME = 3;

    /**
     * Type: Number
     */
    public static final int TYPE_NUMBER = 4;
    
    /**
     * Type: Identifier
     */
    public static final int TYPE_IDENTIFIER = 6;

    /**
     * Type: End of file
     */
    public static final int TYPE_EOF = 7;

    /**
     * Type: Label
     */
    public static final int TYPE_LABEL = 8;

    /**
     * Type: Comment
     */
    public static final int TYPE_COMMENT = 9;

    /**
     * Type: Exception start
     */
    public static final int TYPE_EXCEPTION_START = 10;

    /**
     * Type: Exception end
     */
    public static final int TYPE_EXCEPTION_END = 11;

    /**
     * Type: Exception target
     */
    public static final int TYPE_EXCEPTION_TARGET = 12;

    /**
     * Type: Keyword QName
     */
    public static final int TYPE_KEYWORD_QNAME = 13;

    /**
     * Type: Keyword QNameA
     */
    public static final int TYPE_KEYWORD_QNAMEA = 14;

    /**
     * Type: Keyword RTQName
     */
    public static final int TYPE_KEYWORD_RTQNAME = 15;

    /**
     * Type: Keyword RTQNameA
     */
    public static final int TYPE_KEYWORD_RTQNAMEA = 16;

    /**
     * Type: Keyword RTQNameL
     */
    public static final int TYPE_KEYWORD_RTQNAMEL = 17;

    /**
     * Type: Keyword RTQNameLA
     */
    public static final int TYPE_KEYWORD_RTQNAMELA = 18;

    /**
     * Type: Keyword Multiname
     */
    public static final int TYPE_KEYWORD_MULTINAME = 19;

    /**
     * Type: Keyword MultinameA
     */
    public static final int TYPE_KEYWORD_MULTINAMEA = 20;

    /**
     * Type: Keyword MultinameL
     */
    public static final int TYPE_KEYWORD_MULTINAMEL = 21;

    /**
     * Type: Keyword MultinameLA
     */
    public static final int TYPE_KEYWORD_MULTINAMELA = 22;

    /**
     * Type: Keyword Typename
     */
    public static final int TYPE_KEYWORD_TYPENAME = 23;

    /**
     * Type: Parent open
     */
    public static final int TYPE_PARENT_OPEN = 24;

    /**
     * Type: Parent close
     */
    public static final int TYPE_PARENT_CLOSE = 25;

    /**
     * Type: Comma
     */
    public static final int TYPE_COMMA = 26;

    /**
     * Type: Keyword null
     */
    public static final int TYPE_KEYWORD_NULL = 27;

    /**
     * Type: Bracket open
     */
    public static final int TYPE_BRACKET_OPEN = 28;

    /**
     * Type: Bracket close
     */
    public static final int TYPE_BRACKET_CLOSE = 29;

    /**
     * Type: Lower than
     */
    public static final int TYPE_LOWERTHAN = 30;

    /**
     * Type: Greater than
     */
    public static final int TYPE_GREATERTHAN = 31;

    /**
     * Type: Keyword Namespace
     */
    public static final int TYPE_KEYWORD_NAMESPACE = 32;

    /**
     * Type: Keyword PrivateNamespace
     */
    public static final int TYPE_KEYWORD_PRIVATENAMESPACE = 33;

    /**
     * Type: Keyword PackageNamespace
     */
    public static final int TYPE_KEYWORD_PACKAGENAMESPACE = 34;

    /**
     * Type: Keyword PackageInternalNs
     */
    public static final int TYPE_KEYWORD_PACKAGEINTERNALNS = 35;

    /**
     * Type: Keyword ProtectedNamespace
     */
    public static final int TYPE_KEYWORD_PROTECTEDNAMESPACE = 36;

    /**
     * Type: Keyword ExplicitNamespace
     */
    public static final int TYPE_KEYWORD_EXPLICITNAMESPACE = 37;

    /**
     * Type: Keyword StaticProtectedNs
     */
    public static final int TYPE_KEYWORD_STATICPROTECTEDNS = 38;

    /**
     * Type: Keyword try
     */
    public static final int TYPE_KEYWORD_TRY = 39;

    /**
     * Type: Keyword from
     */
    public static final int TYPE_KEYWORD_FROM = 40;

    /**
     * Type: Keyword to
     */
    public static final int TYPE_KEYWORD_TO = 41;

    /**
     * Type: Keyword target
     */
    public static final int TYPE_KEYWORD_TARGET = 42;

    /**
     * Type: Keyword type
     */
    public static final int TYPE_KEYWORD_TYPE = 43;

    /**
     * Type: Keyword name
     */
    public static final int TYPE_KEYWORD_NAME = 44;

    /**
     * Type: Keyword flag
     */
    public static final int TYPE_KEYWORD_FLAG = 45;

    /**
     * Type: Keyword NATIVE
     */
    public static final int TYPE_KEYWORD_NATIVE = 46;

    /**
     * Type: Keyword HAS_OPTIONAL
     */
    public static final int TYPE_KEYWORD_HAS_OPTIONAL = 47;

    /**
     * Type: Keyword HAS_PARAM_NAMES
     */
    public static final int TYPE_KEYWORD_HAS_PARAM_NAMES = 48;

    /**
     * Type: Keyword IGNORE_REST
     */
    public static final int TYPE_KEYWORD_IGNORE_REST = 49;

    /**
     * Type: Keyword NEED_ACTIVATION
     */
    public static final int TYPE_KEYWORD_NEED_ACTIVATION = 50;

    /**
     * Type: Keyword NEED_ARGUMENTS
     */
    public static final int TYPE_KEYWORD_NEED_ARGUMENTS = 51;

    /**
     * Type: Keyword NEED_REST
     */
    public static final int TYPE_KEYWORD_NEED_REST = 52;


    /**
     * Type: Keyword SET_DXNS
     */
    public static final int TYPE_KEYWORD_SET_DXNS = 53;

    /**
     * Type: Keyword param
     */
    public static final int TYPE_KEYWORD_PARAM = 54;

    /**
     * Type: Keyword paramname
     */
    public static final int TYPE_KEYWORD_PARAMNAME = 55;

    /**
     * Type: Keyword optional
     */
    public static final int TYPE_KEYWORD_OPTIONAL = 56;

    /**
     * Type: Keyword returns
     */
    public static final int TYPE_KEYWORD_RETURNS = 57;

    /**
     * Type: Keyword body
     */
    public static final int TYPE_KEYWORD_BODY = 58;

    /**
     * Type: Keyword maxstack
     */
    public static final int TYPE_KEYWORD_MAXSTACK = 59;

    /**
     * Type: Keyword localcount
     */
    public static final int TYPE_KEYWORD_LOCALCOUNT = 60;

    /**
     * Type: Keyword initscopedepth
     */
    public static final int TYPE_KEYWORD_INITSCOPEDEPTH = 61;

    /**
     * Type: Keyword maxscopedepth
     */
    public static final int TYPE_KEYWORD_MAXSCOPEDEPTH = 62;

    /**
     * Type: Keyword code
     */
    public static final int TYPE_KEYWORD_CODE = 63;

    /**
     * Type: Keyword Integer
     */
    public static final int TYPE_KEYWORD_INTEGER = 64;

    /**
     * Type: Keyword UInteger
     */
    public static final int TYPE_KEYWORD_UINTEGER = 65;

    /**
     * Type: Keyword Double
     */
    public static final int TYPE_KEYWORD_DOUBLE = 66;

    /**
     * Type: Keyword Decimal
     */
    public static final int TYPE_KEYWORD_DECIMAL = 67;

    /**
     * Type: Keyword Utf8
     */
    public static final int TYPE_KEYWORD_UTF8 = 68;

    /**
     * Type: Keyword True
     */
    public static final int TYPE_KEYWORD_TRUE = 69;

    /**
     * Type: Keyword False
     */
    public static final int TYPE_KEYWORD_FALSE = 70;

    /**
     * Type: Keyword Void
     */
    public static final int TYPE_KEYWORD_VOID = 71;

    /**
     * Type: Keyword trait
     */
    public static final int TYPE_KEYWORD_TRAIT = 72;

    /**
     * Type: Keyword slot
     */
    public static final int TYPE_KEYWORD_SLOT = 73;

    /**
     * Type: Keyword const
     */
    public static final int TYPE_KEYWORD_CONST = 74;

    /**
     * Type: Keyword method
     */
    public static final int TYPE_KEYWORD_METHOD = 75;

    /**
     * Type: Keyword getter
     */
    public static final int TYPE_KEYWORD_GETTER = 76;

    /**
     * Type: Keyword setter
     */
    public static final int TYPE_KEYWORD_SETTER = 77;

    /**
     * Type: Keyword class
     */
    public static final int TYPE_KEYWORD_CLASS = 78;

    /**
     * Type: Keyword function
     */
    public static final int TYPE_KEYWORD_FUNCTION = 79;

    /**
     * Type: Keyword disp_id
     */
    public static final int TYPE_KEYWORD_DISPID = 80;

    /**
     * Type: Keyword slot_id
     */
    public static final int TYPE_KEYWORD_SLOTID = 81;

    /**
     * Type: Keyword value
     */
    public static final int TYPE_KEYWORD_VALUE = 82;

    /**
     * Type: Keyword FINAL
     */
    public static final int TYPE_KEYWORD_FINAL = 83;

    /**
     * Type: Keyword METADATA
     */
    public static final int TYPE_KEYWORD_METADATA = 84;

    /**
     * Type: Keyword OVERRIDE
     */
    public static final int TYPE_KEYWORD_OVERRIDE = 85;

    /**
     * Type: Keyword metadata
     */
    public static final int TYPE_KEYWORD_METADATA_BLOCK = 86;

    /**
     * Type: Keyword item
     */
    public static final int TYPE_KEYWORD_ITEM = 87;

    /**
     * Type: Keyword end
     */
    public static final int TYPE_KEYWORD_END = 88;

    /**
     * Type: Keyword Unknown
     */
    public static final int TYPE_KEYWORD_UNKNOWN = 89;

    /**
     * Type: Keyword SEALED
     */
    public static final int TYPE_KEYWORD_SEALED = 90;

    /**
     * Type: Keyword INTERFACE
     */
    public static final int TYPE_KEYWORD_INTERFACE = 91;

    /**
     * Type: Keyword PROTECTEDNS
     */
    public static final int TYPE_KEYWORD_PROTECTEDNS = 92;

    /**
     * Type: Keyword NON_NULLABLE
     */
    public static final int TYPE_KEYWORD_NON_NULLABLE = 93;

    /**
     * Type: Keyword instance
     */
    public static final int TYPE_KEYWORD_INSTANCE = 94;

    /**
     * Type: Keyword extends
     */
    public static final int TYPE_KEYWORD_EXTENDS = 95;

    /**
     * Type: Keyword implements
     */
    public static final int TYPE_KEYWORD_IMPLEMENTS = 96;

    /**
     * Type: Keyword protectedns
     */
    public static final int TYPE_KEYWORD_PROTECTEDNS_BLOCK = 97;

    /**
     * Type: Keyword Number
     */
    public static final int TYPE_KEYWORD_NUMBER = 98;
    
    /**
     * Type: Keyword int
     */
    public static final int TYPE_KEYWORD_INT = 99;
    
    /**
     * Type: Keyword uint
     */
    public static final int TYPE_KEYWORD_UINT = 100;
    
    /**
     * Type: Keyword NumberContext
     */
    public static final int TYPE_KEYWORD_NUMBERCONTEXT = 101;
    
    /**
     * Type: Keyword CEILING
     */
    public static final int TYPE_KEYWORD_CEILING = 102;
    
    /**
     * Type: Keyword UP
     */
    public static final int TYPE_KEYWORD_UP = 103;
    
    /**
     * Type: Keyword HALF_UP
     */
    public static final int TYPE_KEYWORD_HALF_UP = 104;
    
    /**
     * Type: Keyword HALF_EVEN
     */
    public static final int TYPE_KEYWORD_HALF_EVEN = 105;
    
    /**
     * Type: Keyword HALF_DOWN 
     */
    public static final int TYPE_KEYWORD_HALF_DOWN = 106;
    
    /**
     * Type: Keyword DOWN
     */
    public static final int TYPE_KEYWORD_DOWN = 107;
    
    /**
     * Type: Keyword FLOOR
     */
    public static final int TYPE_KEYWORD_FLOOR = 108;
    
    /**
     * Type: Keyword Float
     */
    public static final int TYPE_KEYWORD_FLOAT = 109;
    
    /**
     * Type: Keyword Float4
     */
    public static final int TYPE_KEYWORD_FLOAT4 = 110;
    
    /**
     * Constructor.
     * @param pos Position
     * @param type Type
     * @param value Value
     */
    public ParsedSymbol(int pos, int type, Object value) {
        this.pos = pos;
        this.type = type;
        this.value = value;
    }

    /**
     * Constructor.
     * @param pos Position
     * @param type Type
     */
    public ParsedSymbol(int pos, int type) {
        this.pos = pos;
        this.type = type;
    }
}
