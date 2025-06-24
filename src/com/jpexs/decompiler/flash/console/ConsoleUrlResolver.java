/*
 *  Copyright (C) 2025 JPEXS
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
package com.jpexs.decompiler.flash.console;

import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.UrlResolver;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Console Url resolver
 *
 * @author JPEXS
 */
public class ConsoleUrlResolver implements UrlResolver {

    private final boolean doResolve;
    private final boolean doAsk;
    private final boolean localOnly;

    private boolean ignoredInfoPrinted = false;
    private Character toAll = null;

    private Map<String, String> importSources = new HashMap<>();

    /**
     *
     * @param doResolve Do resolve?
     * @param doAsk Do ask?
     * @param localOnly Local only?
     * @param importSources Import sources
     */
    public ConsoleUrlResolver(boolean doResolve, boolean doAsk, boolean localOnly, Map<String, String> importSources) {
        this.doResolve = doResolve;
        this.doAsk = doAsk;
        this.localOnly = localOnly;
        this.importSources = importSources;
    }

    @Override
    public SWF resolveUrl(String basePath, String url) {

        Character choice = toAll;
        String currentUrl = url;

        if (importSources.containsKey(url)) {
            currentUrl = importSources.get(url);
            if (currentUrl.isEmpty()) {
                return null;
            }
            choice = 'y';
        } else {
            if (choice == null && doResolve) {
                choice = 'y';
            }
            if (!doResolve && !doAsk) {
                if (!ignoredInfoPrinted) {
                    System.out.println("WARNING: SWF tried to import assets from external SWF, the request was ignored. (See --help -importAssets for more options)");
                }
                ignoredInfoPrinted = true;
                return null;
            }
            if (choice == null && doAsk) {
                choice = ask(url);
            }
        }

        while (choice != 'n') {
            if (choice == 'c') {
                System.out.println("Enter new location instead (empty to cancel):");
                Scanner sc = new Scanner(System.in);
                String newUrl = sc.nextLine();
                importSources.put(url, newUrl);
                currentUrl = newUrl;
                if (currentUrl.isEmpty()) {
                    return null;
                }
            }

            if (currentUrl.startsWith("http://") || currentUrl.startsWith("https://")) {
                if (localOnly && choice != 'c') {
                    return null;
                }
                try {
                    URL u = URI.create(currentUrl).toURL();
                    SWF ret = open(u.openStream(), null, currentUrl); //?
                    return ret;
                } catch (Exception ex) {
                    //ignore
                }
            } else {
                File swf = new File(new File(basePath).getParentFile(), currentUrl);
                if (swf.exists()) {
                    try {
                        SWF ret = open(new FileInputStream(swf), swf.getAbsolutePath(), swf.getName());
                        return ret;
                    } catch (Exception ex) {
                        //ignore
                    }
                }
                // try .gfx if .swf failed
                if (currentUrl.endsWith(".swf")) {
                    File gfx = new File(new File(basePath).getParentFile(), url.substring(0, url.length() - 4) + ".gfx");
                    if (gfx.exists()) {
                        try {
                            SWF ret = open(new FileInputStream(gfx), gfx.getAbsolutePath(), gfx.getName());
                            return ret;
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }

            System.err.println("Cannot load imported SWF " + currentUrl);
            if (doAsk) {
                choice = 'c';
            } else {
                choice = 'n';
            }
        }
        return null;
    }

    private SWF open(InputStream is, String file, String fileTitle) throws Exception {
        OpenableSourceInfo sourceInfo = new OpenableSourceInfo(is, file, fileTitle);
        return new SWF(new BufferedInputStream(is), sourceInfo.getFile(), sourceInfo.getFileTitle(), null, Configuration.parallelSpeedUp.get(), false, true, this, null /*??*/);
    }

    private String prompt() {
        Scanner sc = new Scanner(System.in);
        String s;
        System.out.print("Select action: (Y)es, (N)o, (C)ustom, (YA) Yes to all, (NA) No to all: ");
        s = sc.nextLine();
        s = s.toLowerCase(Locale.ENGLISH);
        return s;
    }

    private char ask(String url) {
        String s;
        if (toAll != null) {
            s = "" + toAll;
        } else {
            System.out.println("The SWF file is trying to import assets from the following location:");
            System.out.println(url);
            s = prompt();
        }

        while (true) {
            switch (s) {
                case "ya":
                    toAll = 'y';
                    return 'y';
                case "na":
                    toAll = 'n';
                    return 'n';
                case "y":
                    return 'y';
                case "n":
                    return 'n';
                case "c":
                    return 'c';
            }
            System.out.println("Invalid action.");
            s = prompt();
        }
    }
}
