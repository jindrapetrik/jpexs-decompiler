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
        Object[][] ret = new Object[files.size() + 4][1];
        ret[0][0] = "testdata/as2/as2.swf";
        //ret[1][0] = "testdata/as3/as3.swf";
        ret[1][0] = "testdata/as2_slash_syntax/slash_syntax.swf";
        ret[2][0] = "testdata/as3_new/bin/as3_new.air.swf";
        ret[3][0] = "testdata/as3_new/bin/as3_new.flex.swf";
        for (int f = 0; f < files.size(); f++) {
            ret[f + 4][0] = files.get(f);
        }
        return ret;
    }
}
