/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.helpers.utf8.Utf8Helper;
import com.jpexs.helpers.utf8.Utf8PrintWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class LicenseUpdater {

    public static void updateLicense() {
        updateLicenseInDir(new File(".\\src\\"), false);
        updateLicenseInDir(new File(".\\test\\"), false);
        updateLicenseInDir(new File(".\\libsrc\\ffdec_lib\\src\\"), true);
        updateLicenseInDir(new File(".\\libsrc\\ffdec_lib\\test\\"), true);
    }

    /**
     * Script for updating license header in java files :-)
     *
     * @param dir Star directory (e.g. "src/")
     * @param lgpl Is LGPL?
     */
    public static void updateLicenseInDir(File dir, boolean lgpl) {

        /*
         Copyright (c) ${year}, ${owner}, All rights reserved.

         This library is free software; you can redistribute it and/or
         modify it under the terms of the GNU Lesser General Public
         License as published by the Free Software Foundation; either
         version 3.0 of the License, or (at your option) any later version.

         This library is distributed in the hope that it will be useful,
         but WITHOUT ANY WARRANTY; without even the implied warranty of
         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
         Lesser General Public License for more details.

         You should have received a copy of the GNU Lesser General Public
         License along with this library.


         */
        int defaultStartYear = 2010;
        int defaultFinalYear = 2018;
        String defaultAuthor = "JPEXS";
        String defaultYearStr = Integer.toString(defaultStartYear);
        if (defaultFinalYear != defaultStartYear) {
            defaultYearStr += "-" + defaultFinalYear;
        }
        String gplLicense = "/*\r\n *  Copyright (C) {year} {author}\r\n * \r\n *  This program is free software: you can redistribute it and/or modify\r\n *  it under the terms of the GNU General Public License as published by\r\n *  the Free Software Foundation, either version 3 of the License, or\r\n *  (at your option) any later version.\r\n * \r\n *  This program is distributed in the hope that it will be useful,\r\n *  but WITHOUT ANY WARRANTY; without even the implied warranty of\r\n *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\r\n *  GNU General Public License for more details.\r\n * \r\n *  You should have received a copy of the GNU General Public License\r\n *  along with this program.  If not, see <http://www.gnu.org/licenses/>.\r\n */";

        String lgplLicense = "/*\r\n *  Copyright (C) {year} {author}, All rights reserved.\r\n * \r\n"
                + " * This library is free software; you can redistribute it and/or\r\n"
                + " * modify it under the terms of the GNU Lesser General Public\r\n"
                + " * License as published by the Free Software Foundation; either\r\n"
                + " * version 3.0 of the License, or (at your option) any later version.\r\n"
                + " * \r\n"
                + " * This library is distributed in the hope that it will be useful,\r\n"
                + " * but WITHOUT ANY WARRANTY; without even the implied warranty of\r\n"
                + " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\r\n"
                + " * Lesser General Public License for more details.\r\n"
                + " * \r\n"
                + " * You should have received a copy of the GNU Lesser General Public\r\n"
                + " * License along with this library.\r"
                + " */";
        String license = lgpl ? lgplLicense : gplLicense;

        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                updateLicenseInDir(f, lgpl);
            } else if (f.getName().endsWith(".java")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter pw = new Utf8PrintWriter(baos);
                try {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), Utf8Helper.charset))) {
                        String s;
                        boolean packageFound = false;
                        String author = defaultAuthor;
                        String yearStr = defaultYearStr;
                        while ((s = br.readLine()) != null) {
                            if (!packageFound) {
                                if (s.trim().startsWith("package")) {
                                    packageFound = true;
                                    pw.println(license.replace("{year}", yearStr).replace("{author}", author));
                                } else {
                                    Matcher mAuthor = Pattern.compile(lgpl ? "^.*Copyright \\(C\\) ([0-9]+)(-[0-9]+)? (.*), All rights reserved.*" : "^.*Copyright \\(C\\) ([0-9]+)(-[0-9]+)? (.*)$").matcher(s);
                                    if (mAuthor.matches()) {
                                        author = mAuthor.group(3).trim();
                                        int startYear = Integer.parseInt(mAuthor.group(1).trim());
                                        if (startYear == defaultFinalYear) {
                                            yearStr = Integer.toString(startYear);
                                        } else {
                                            yearStr = Integer.toString(startYear) + "-" + defaultFinalYear;
                                        }
                                        if (!author.equals(defaultAuthor)) {
                                            System.out.println("Detected nodefault author:" + author + " in " + f.getAbsolutePath());
                                        }
                                    }
                                }
                            }
                            if (packageFound) {
                                pw.println(s);
                            }
                        }
                    }
                    pw.close();
                } catch (IOException ex) {
                }

                try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {
                    fos.write(baos.toByteArray());
                } catch (IOException ex) {
                }
            }
        }
    }
}
