/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.generators;

import com.jpexs.helpers.Helper;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 *
 * @author JPEXS
 */
public class AS3NewAssemblyGenerator {
    public static void main(String[] args) throws UnsupportedEncodingException {
        Scanner sc = new Scanner(System.in);
        String swfName = "as3_assembled";
        System.out.print("Enter test class name:");
        final String DIR = "testdata/" + swfName + "/abc/" + swfName + "-0/";
        String className = sc.nextLine();
        String templateClassData = Helper.readTextFile(DIR + "tests/Template.class.asasm");
        String templateScriptData = Helper.readTextFile(DIR + "tests/Template.script.asasm");
        String mainData = Helper.readTextFile(DIR + swfName + "-0.main.asasm");

        String classData = templateClassData.replace("Template", className);;
        String scriptData = templateScriptData.replace("Template", className);
        final String PLACEHOLDER = " ; place to add next\r\n";

        String mainDataUpdated = mainData.replace(PLACEHOLDER, " #include \"tests/" + className + ".script.asasm\"\r\n" + PLACEHOLDER);

        Helper.writeFile(DIR + "tests/" + className + ".class.asasm", classData.getBytes("UTF-8"));
        Helper.writeFile(DIR + "tests/" + className + ".script.asasm", scriptData.getBytes("UTF-8"));
        Helper.writeFile(DIR + swfName + "-0.main.asasm", mainDataUpdated.getBytes("UTF-8"));

    }
}
