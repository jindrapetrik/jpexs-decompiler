package com.jpexs.proxy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static int port = 55555;
    public static final String REPLACEMENTSFILE = "." + File.separator + "config" + File.separator + "replacements.ini";
    public static boolean DEBUG_MODE = false;


    public static void enterToContinue() {
        Scanner keyIn = new Scanner(System.in);
        System.out.print("Press the enter key to continue");
        keyIn.nextLine();
    }

    public static void main(String args[]) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase().equals("--help")) {
                System.out.println("JPEXS Replacement Proxy");
                System.out.println("-----------------------");
                System.out.println("Replacements list is read from \"" + REPLACEMENTSFILE + "\" file.");
                System.out.println("Optional commandline parameters:");
                System.out.println(" -d");
                System.out.println("  Print debug info to console ");
                System.out.println(" -p<NUMBER>");
                System.out.println("  Set proxy port to <NUMBER>. Default is 55555.");
                System.exit(0);
            }
            if (args[i].toLowerCase().equals("-d")) {
                System.out.println("DEBUG mode ON");
                DEBUG_MODE = true;
            }
            if (args[i].toLowerCase().startsWith("-p")) {
                try {
                    port = Integer.parseInt(args[i].substring(2));
                    System.out.println("PORT set to " + port);
                } catch (NumberFormatException ex) {
                    System.out.println("Invalit port, reset to " + port);
                }

            }
        }
        List<Replacement> replacements = new ArrayList<Replacement>();
        if ((new File(REPLACEMENTSFILE)).exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(REPLACEMENTSFILE));
                String s = "";
                while ((s = br.readLine()) != null) {
                    String fileName = br.readLine();
                    if (fileName == null) break;
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
        Server.startServer(port, replacements, new ArrayList<String>(), new CatchedListener() {
            /**
             * Method called when specified contentType is received
             *
             * @param contentType Content type
             * @param url         URL of the method
             * @param data        Data stream
             */
            public void catched(String contentType, String url, InputStream data) {

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
