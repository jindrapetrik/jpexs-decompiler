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
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;

/**
 *
 * @author JPEXS
 */
public abstract class FileTestBase {

    protected abstract String[] getTestDataDirs();

    protected static final String FREE_ACTIONSCRIPT_AS2 = "testdata/freeactionscript.com/as2";

    protected static final String FREE_ACTIONSCRIPT_AS3 = "testdata/freeactionscript.com/as3";

    @DataProvider(name = "provideFiles")
    public Object[][] provideFiles() {
        String[] dirs = getTestDataDirs();
        List<String> files = new ArrayList<>();
        for (String d : dirs) {
            File dir = new File(d);
            if (dir.exists()) {
                File[] fs = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".swf") && !name.toLowerCase().endsWith(".recompiled.swf");
                    }
                });
                for (File f : fs) {
                    files.add(dir.getAbsolutePath() + File.separator + f.getName());
                }
            }
        }
        Object[][] ret = new Object[files.size() + 2][1];
        ret[0][0] = "testdata/as2/as2.swf";
        ret[1][0] = "testdata/as3/as3.swf";
        ret[1][0] = "testdata/as2_slash_syntax/slash_syntax.swf";
        for (int f = 0; f < files.size(); f++) {
            ret[f + 2][0] = files.get(f);
        }
        return ret;
    }
}
