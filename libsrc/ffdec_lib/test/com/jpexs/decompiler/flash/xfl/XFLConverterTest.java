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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.SWF;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class XFLConverterTest {
    @DataProvider(name = "provideFiles")
    public Object[][] provideFiles() {
        return new Object[][]{
            {"testdata/graphics/graphics.swf"},
            {"testdata/sounds/sounds.swf"}
        };
    }
    
    @Test(dataProvider = "provideFiles")
    public void testFlaExport(String fileName) throws Exception {       
        File outputDir = File.createTempFile("ffdec_flatest", "");                
        outputDir.delete();
        outputDir.mkdir();
        
        try(FileInputStream fis = new FileInputStream(fileName)) {
            SWF swf = new SWF(fis, true);

            swf.exportFla(new AbortRetryIgnoreHandler(){
                @Override
                public int handle(Throwable thrown) {
                    return AbortRetryIgnoreHandler.ABORT;
                }
                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }            
            }, 
            outputDir + "/" + ( new File(fileName).getName().replace(".swf", ".fla")),
            new File(fileName).getName(),
            ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, true, FLAVersion.CS6,
            null);            
            
            //TODO: Actually do some tests - asserts. Without them it is not much useful - it just tests that no exception is thrown.
            
        } finally {
            removeDirectory(outputDir);
        }
    }
    
    private void removeDirectory(File dir){
        try (Stream<Path> dirStream = Files.walk(Paths.get(dir.getAbsolutePath()))) {
            dirStream
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .forEach(File::delete);
        } catch (Exception ex) {
            //ignore
        }
    }
}
