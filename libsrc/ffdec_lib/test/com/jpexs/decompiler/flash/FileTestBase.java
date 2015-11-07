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
package com.jpexs.decompiler.flash;

import java.io.File;
import java.io.FilenameFilter;
import org.testng.annotations.DataProvider;

/**
 *
 * @author JPEXS
 */
public abstract class FileTestBase {

    protected abstract String getTestDataDir();

    @DataProvider(name = "provideFiles")
    public Object[][] provideFiles() {
        File dir = new File(getTestDataDir());
        File[] files = new File[0];
        if (dir.exists()) {
            files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".swf") && !name.toLowerCase().endsWith(".recompiled.swf");
                }
            });
        }
        Object[][] ret = new Object[files.length + 2][1];
        ret[0][0] = "testdata/as2/as2.swf";
        ret[1][0] = "testdata/as3/as3.swf";
        for (int f = 0; f < files.length; f++) {
            ret[f + 2][0] = dir.getAbsolutePath() + File.separator + files[f].getName();
        }
        return ret;
    }
}
