package com.jpexs.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final String REPLACEMENTSFILE = "." + File.separator + "config" + File.separator + "replacements.ini";
    public static boolean DEBUG_MODE = false;

    public static void main(String[] argv) {
        List<Replacement> replacements = new ArrayList<Replacement>();
        if ((new File(REPLACEMENTSFILE)).exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(REPLACEMENTSFILE));
                String s = "";
                while ((s = br.readLine()) != null) {
                    String fileName = br.readLine();
                    if (fileName == null) {
                        break;
                    }
                    fileName = fileName.replaceAll("[\\\\/]", File.separator);
                    Replacement r = new Replacement(s, fileName);
                    if (DEBUG_MODE) {
                        System.out.println("Added Replacement: " + r.urlPattern + " => " + r.targetFile);
                    }
                    replacements.add(r);
                }
                br.close();
            } catch (IOException e) {

            }
        } else {
            if (DEBUG_MODE) {
                System.out.println("WARNING:REPLACEMENTS FILE NOT FOUND.");
            }
        }
        Server.startServer(ProxyConfig.port, replacements, new ArrayList<String>(), new CatchedListener() {

            /**
             * Method called when specified contentType is received
             *
             * @param contentType Content type
             * @param url URL of the method
             * @param data Data stream
             */
            public byte[] catched(String contentType, String url, InputStream data) {
                return null;
            }
        }, new ReplacedListener() {

            public void replaced(Replacement replacement, String url, String contentType) {
                if (DEBUG_MODE) {
                    System.out.println("REPLACED:" + url + " (Content-type:" + contentType + ") WITH FILE " + replacement.targetFile);
                }
            }
        });

    }
}
