/*
 * Copyright (C) 2018 Jindra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.graph;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class GraphVizDotCommands {

    public static boolean graphVizAvailable() {
        String dotPath = Configuration.graphVizDotLocation.get();
        if (dotPath.isEmpty() || !new File(dotPath).exists()) {
            return false;
        }
        return true;
    }

    private static void runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            while ((s = reader.readLine()) != null) {

            }
        } catch (IOException e) {
            //ignore
        }
    }

    private static boolean runDotCommand(String command) {
        String dotLocation = Configuration.graphVizDotLocation.get();
        if (dotLocation.isEmpty() && !new File(dotLocation).exists()) {
            return false;
        }//
        runCommand("\"" + dotLocation + "\" " + command);
        return true;
    }

    public BufferedImage dotToImage(String text) throws IOException {
        File gvFile = File.createTempFile("graphexport", ".gv");
        File pngFile = File.createTempFile("graphexport", ".png");

        PrintWriter pw = new PrintWriter(gvFile);
        pw.println(text);
        pw.close();
        String extraParams = " -Nfontname=times-bold -Nfontsize=12";
        if (!runDotCommand("-Tpng" + extraParams + " -o \"" + pngFile.getAbsolutePath() + "\" \"" + gvFile.getAbsolutePath() + "\"")) {
            gvFile.delete();
            return null;
        }
        if (!pngFile.exists()) {
            throw new IOException("Dot did not produce any file");
        }
        BufferedImage ret = ImageIO.read(pngFile);
        gvFile.delete();
        pngFile.delete();
        return ret;
    }
}
