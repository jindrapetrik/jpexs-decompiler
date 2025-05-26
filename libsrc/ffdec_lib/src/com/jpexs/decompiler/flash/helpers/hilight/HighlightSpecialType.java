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
package com.jpexs.decompiler.flash.helpers.hilight;

/**
 * Special types of highlighting.
 *
 * @author JPEXS
 */
public enum HighlightSpecialType {

    /**
     * Parameter name
     */
    PARAM_NAME,
    /**
     * Parameter
     */
    PARAM,
    /**
     * Optional
     */
    OPTIONAL,
    /**
     * Returns
     */
    RETURNS,
    /**
     * Type name
     */
    TYPE_NAME,
    /**
     * Class name
     */
    CLASS_NAME,
    /**
     * Method name
     */
    METHOD_NAME,
    /**
     * Trait type
     */
    TRAIT_TYPE,
    /**
     * Trait name
     */
    TRAIT_NAME,
    /**
     * Trait type name
     */
    TRAIT_TYPE_NAME,
    /**
     * Trait value
     */
    TRAIT_VALUE,
    /**
     * Slot id
     */
    SLOT_ID,
    /**
     * Dispatch id
     */
    DISP_ID,
    /**
     * Flag need rest
     */
    FLAG_NEED_REST,
    /**
     * Flag native
     */
    FLAG_NATIVE,
    /**
     * Flag has optional
     */
    FLAG_HAS_OPTIONAL,
    /**
     * Flag has param names
     */
    FLAG_HAS_PARAM_NAMES,
    /**
     * Flag ignore rest
     */
    FLAG_IGNORE_REST,
    /**
     * Flag need activation
     */
    FLAG_NEED_ACTIVATION,
    /**
     * Flag need arguments
     */
    FLAG_NEED_ARGUMENTS,
    /**
     * Flag set dxns
     */
    FLAG_SET_DXNS,
    /**
     * Try type
     */
    TRY_TYPE,
    /**
     * Try name
     */
    TRY_NAME,
    /**
     * Text
     */
    TEXT,
    /**
     * Attr metadata
     */
    ATTR_METADATA,
    /**
     * Attr final
     */
    ATTR_FINAL,
    /**
     * Attr override
     */
    ATTR_OVERRIDE,
    /**
     * Attr 0x8
     */
    ATTR_0x8,
    /**
     * Property type
     */
    PROPERTY_TYPE,
    /**
     * Instance name
     */
    INSTANCE_NAME,
    /**
     * Implements
     */
    IMPLEMENTS,
    /**
     * Extends
     */
    EXTENDS,
    /**
     * Protected namespace
     */
    PROTECTEDNS
}
