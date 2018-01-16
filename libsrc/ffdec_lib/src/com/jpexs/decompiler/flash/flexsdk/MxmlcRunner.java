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
package com.jpexs.decompiler.flash.flexsdk;

import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MxmlcRunner {

    private String flexSdkPath;

    public MxmlcRunner(String flexSdkPath) {
        this.flexSdkPath = flexSdkPath;
    }

    public static String getMxmlcPath(String flexSdkPath) {
        boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
        return flexSdkPath + File.separator + "bin" + File.separator + "mxmlc" + (isWin ? ".exe" : "");
    }

    public void mxmlc(String... arguments) throws MxmlcException, InterruptedException, IOException {
        String runArgs[] = new String[arguments.length + 1];
        runArgs[0] = getMxmlcPath(flexSdkPath);
        System.arraycopy(arguments, 0, runArgs, 1, arguments.length);
        //System.out.println("" + String.join(" ", runArgs));
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(runArgs);
            Helper.readStream(proc.getInputStream());
            String errstring = "";
            try {
                errstring = new String(Helper.readStream(proc.getErrorStream()), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                //should not happen
            }
            proc.waitFor();
            int exitValue = proc.exitValue();
            if (exitValue > 0) {
                throw new MxmlcException(errstring);
            }
        } finally {
            if (proc != null) {
                try {
                    proc.destroy();
                } catch (Exception ex2) {
                    //ignore
                }
            }
        }
    }

}
