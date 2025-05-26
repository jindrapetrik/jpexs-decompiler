/*
 *  Copyright (C) 2021-2025 JPEXS
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
package com.jpexs.build;

import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * @author JPEXS
 */
public class LocaleConverter {

    private static void convertFile(File f) throws UnsupportedEncodingException {
        String data = Helper.readTextFile(f.getAbsolutePath());
        StringBuilder sb = new StringBuilder();
        boolean modified = false;
        for (int i = 0; i < data.length(); i++) {
            char c = (char) data.charAt(i);
            if (c > 127) {
                sb.append("\\u");
                sb.append(String.format("%04x", c & 0xFFFF));
                modified = true;
            } else {
                sb.append(c);
            }
        }
        if (modified) {
            System.err.println("converted: " + f);
            Helper.writeFile(f.getAbsolutePath(), sb.toString().getBytes("UTF-8"));
        }

    }

    private static void walk(String path) throws UnsupportedEncodingException {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
            } else {
                if (f.getName().endsWith(".properties")) {
                    convertFile(f);
                }
            }
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        walk("src");
        walk("libsrc/ffdec_lib/src");
    }
}
