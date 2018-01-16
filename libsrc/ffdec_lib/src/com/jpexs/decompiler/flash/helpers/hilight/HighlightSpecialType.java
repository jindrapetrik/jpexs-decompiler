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
package com.jpexs.decompiler.flash.helpers.hilight;

/**
 *
 * @author JPEXS
 */
public enum HighlightSpecialType {

    PARAM_NAME, PARAM, OPTIONAL, RETURNS,
    TYPE_NAME, CLASS_NAME, METHOD_NAME,
    TRAIT_TYPE, TRAIT_NAME, TRAIT_TYPE_NAME, TRAIT_VALUE,
    SLOT_ID, DISP_ID,
    FLAG_NEED_REST, FLAG_EXPLICIT, FLAG_HAS_OPTIONAL, FLAG_HAS_PARAM_NAMES,
    FLAG_IGNORE_REST, FLAG_NEED_ACTIVATION, FLAG_NEED_ARGUMENTS, FLAG_SET_DXNS,
    TRY_TYPE, TRY_NAME,
    TEXT,
    ATTR_METADATA, ATTR_FINAL, ATTR_OVERRIDE, ATTR_0x8
}
