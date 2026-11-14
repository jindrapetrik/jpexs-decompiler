/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.as3decompile;

import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.IOException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A field found through the scope chain must be qualified when a local
 * variable of the same name shadows it.
 *
 * @see testdata/haxe_shadowed_field/README.md
 */
public class ActionScript3HaxeShadowedFieldDecompileTest extends ActionScript3DecompileTestBase {

    private boolean previousParamNames;

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("haxe_shadowed_field", "testdata/haxe_shadowed_field/bin/haxe_shadowed_field.swf");
    }

    @BeforeMethod
    public void enableParamNames() {
        previousParamNames = Configuration.paramNamesEnable.get();
        Configuration.paramNamesEnable.set(true);
    }

    @AfterMethod(alwaysRun = true)
    public void restoreParamNames() {
        Configuration.paramNamesEnable.set(previousParamNames);
    }

    @Test
    public void testHaxeShadowedField() {
        decompileMethod("haxe_shadowed_field", "testHaxeShadowedField",
                "var old:String = this.key;\r\n"
                + "this.key = key;\r\n"
                + "return old + key;\r\n",
                false);
    }
}
