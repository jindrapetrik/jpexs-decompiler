/*
 *  Copyright (C) 2010-2012 JPEXS
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
package com.jpexs.asdec;

import com.jpexs.proxy.Replacement;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Configuration {

   private static final String CONFIG_NAME = "asdec.cfg";
   private static final String REPLACEMENTS_NAME = "replacements.ini";
   private static HashMap<String, Object> config = new HashMap<String, Object>();

   public static String getASDecHome() {
      String dir = ".";//System.getProperty("user.home");
      if (!dir.endsWith(File.separator)) {
         dir += File.separator;
      }
      dir += "config" + File.separator;
      return dir;
   }

   private static String getReplacementsFile() {
      return getASDecHome() + REPLACEMENTS_NAME;
   }

   private static String getConfigFile() {
      return getASDecHome() + CONFIG_NAME;
   }
   /**
    * List of replacements
    */
   public static java.util.List<Replacement> replacements = new ArrayList<Replacement>();

   /**
    * Saves replacements to file for future use
    */
   private static void saveReplacements() {
      try {
         if (replacements.isEmpty()) {
            File rf = new File(getReplacementsFile());
            if (rf.exists()) {
               rf.delete();
            }
         } else {
            File f = new File(getASDecHome());
            if (!f.exists()) {
               f.mkdir();
            }
            PrintWriter pw = new PrintWriter(new FileWriter(getReplacementsFile()));
            for (Replacement r : replacements) {
               pw.println(r.urlPattern);
               pw.println(r.targetFile);
            }
            pw.close();
         }
      } catch (IOException e) {
      }
   }

   /**
    * Load replacements from file
    */
   private static void loadReplacements() {
      replacements = new ArrayList<Replacement>();
      try {
         BufferedReader br = new BufferedReader(new FileReader(getReplacementsFile()));
         String s;
         while ((s = br.readLine()) != null) {
            Replacement r = new Replacement(s, br.readLine());
            replacements.add(r);
         }
         br.close();
      } catch (IOException e) {
      }
   }

   public static Object getConfig(String cfg) {
      return getConfig(cfg, null);
   }

   public static Object getConfig(String cfg, Object defaultValue) {
      if (!config.containsKey(cfg)) {
         return defaultValue;
      }
      return config.get(cfg);
   }

   public static Object setConfig(String cfg, Object value) {
      return config.put(cfg, value);
   }

   public static void load() {
      ObjectInputStream ois = null;
      try {
         ois = new ObjectInputStream(new FileInputStream(getConfigFile()));
         config = (HashMap<String, Object>) ois.readObject();
      } catch (FileNotFoundException ex) {
      } catch (ClassNotFoundException cnf) {
      } catch (IOException ex) {
      } finally {
         if (ois != null) {
            try {
               ois.close();
            } catch (IOException ex1) {
               //ignore
            }
         }
      }
      loadReplacements();
   }

   public static void save() {
      ObjectOutputStream oos = null;
      try {
         oos = new ObjectOutputStream(new FileOutputStream(getConfigFile()));
         oos.writeObject(config);
      } catch (FileNotFoundException ex) {
      } catch (IOException ex) {
      } finally {
         if (oos != null) {
            try {
               oos.close();
            } catch (IOException ex1) {
               //ignore
            }
         }
      }
      saveReplacements();
   }

   public static List<Replacement> getReplacements() {
      return replacements;
   }
}
