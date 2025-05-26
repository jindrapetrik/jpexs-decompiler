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
package com.jpexs.decompiler.flash.flexsdk;

import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Runs mxmlc compiler.
 */
public class MxmlcRunner {

    private String flexSdkPath;

    public MxmlcRunner(String flexSdkPath) {
        this.flexSdkPath = flexSdkPath;
    }

    /**
     * Gets the path to mxmlc compiler.
     * @param flexSdkPath Path to Flex SDK.
     * @return Path to mxmlc compiler.
     */
    public static String getMxmlcPath(String flexSdkPath) {
        boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
        String path = flexSdkPath + File.separator + "bin" + File.separator + "mxmlc";

        String exePath = path + ".exe";
        String batPath = path + ".bat";

        if (isWin) {
            if (new File(exePath).exists()) {
                return exePath;
            }
            if (new File(batPath).exists()) {
                return batPath;
            }
        } else {
            if (new File(path).exists()) {
                return exePath;
            }
        }
        return null;
    }

    /**
     * Runs mxmlc compiler.
     * @param arguments Arguments for mxmlc.
     * @throws MxmlcException If mxmlc fails to compile a file
     * @throws InterruptedException On interrupt
     * @throws IOException On I/O error
     */
    public void mxmlc(List<String> arguments) throws MxmlcException, InterruptedException, IOException {
        String[] runArgs = new String[arguments.size() + 1];
        runArgs[0] = getMxmlcPath(flexSdkPath);
        for (int i = 0; i < arguments.size(); i++) {
            runArgs[i + 1] = arguments.get(i);
        }
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
