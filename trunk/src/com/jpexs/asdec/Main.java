/*
 *  Copyright (C) 2010-2011 JPEXS
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

import com.jpexs.asdec.abc.NotSameException;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.gui.AboutDialog;
import com.jpexs.asdec.gui.LoadingDialog;
import com.jpexs.asdec.gui.ModeFrame;
import com.jpexs.asdec.gui.View;
import com.jpexs.asdec.gui.proxy.ProxyFrame;
import com.jpexs.asdec.tags.DoABCTag;
import com.jpexs.asdec.tags.Tag;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

/**
 * Main executable class
 *
 * @author JPEXS
 */
public class Main {

   public static com.jpexs.asdec.abc.gui.MainFrame abcMainFrame;
   public static com.jpexs.asdec.action.gui.MainFrame actionMainFrame;
   public static ProxyFrame proxyFrame;
   public static String file;
   public static String maskURL;
   public static SWF swf;
   public static final String version = "1.0.0";
   public static String applicationName = "JP ActionScript Decompiler v." + version;
   public static String shortApplicationName = "ASDec v." + version;
   public static LoadingDialog loadingDialog;
   public static ModeFrame modeFrame;
   private static boolean working = false;
   private static TrayIcon trayIcon;
   private static MenuItem stopMenuItem;
   private static boolean commandLineMode = false;

   public static boolean isCommandLineMode() {
      return commandLineMode;
   }
   public static boolean DEBUG_COPY = false;
   /**
    * Debug mode = throwing an error when comparing original file and recompiled
    */
   public static boolean DEBUG_MODE = false;
   /**
    * Turn off reading unsafe tags (tags which can cause problems with recompiling)
    */
   public static boolean DISABLE_DANGEROUS = false;
   /**
    * Turn off resolving constants in ActionScript 2
    */
   public static final boolean RESOLVE_CONSTANTS = true;
   /**
    * Turn off decompiling if needed
    */
   public static final boolean DO_DECOMPILE = true;
   /**
    * Find latest constant pool in the code
    */
   public static final boolean LATEST_CONSTANTPOOL_HACK = false;
   /**
    * Dump tags to stdout
    */
   public static boolean dump_tags = false;
   /**
    * Limit of code subs (for obfuscated code)
    */
   public static final int SUBLIMITER = 500;
   //using parameter names in decompiling may cause problems because oficial programs like Flash CS 5.5 inserts wrong parameter names indices
   public static final boolean PARAM_NAMES_ENABLE = false;

   public static String getFileTitle() {
      if (maskURL != null) {
         return maskURL;
      }
      return file;
   }

   public static void setSubLimiter(boolean value) {
      if (value) {
         AVM2Code.toSourceLimit = Main.SUBLIMITER;
      } else {
         AVM2Code.toSourceLimit = -1;
      }
   }

   public static boolean isWorking() {
      return working;
   }

   public static void showProxy() {
      if (proxyFrame == null) {
         proxyFrame = new ProxyFrame();
      }
      proxyFrame.setVisible(true);
      proxyFrame.setState(Frame.NORMAL);
   }

   public static void startWork(String name) {
      working = true;
      if (abcMainFrame != null) {
         abcMainFrame.setStatus(name);
      }
      if (actionMainFrame != null) {
         actionMainFrame.setStatus(name);
      }
      if (Main.isCommandLineMode()) {
         System.out.println(name);
      }
   }

   public static void stopWork() {
      working = false;
      if (abcMainFrame != null) {
         abcMainFrame.setStatus("");
      }
      if (actionMainFrame != null) {
         actionMainFrame.setStatus("");
      }
   }

   public static SWF parseSWF(String file) throws Exception {
      FileInputStream fis = new FileInputStream(file);
      InputStream bis = new BufferedInputStream(fis);
      SWF locswf = new SWF(bis);
      return locswf;
   }

   public static void saveFile(String outfile) throws IOException {
      file = outfile;
      swf.saveTo(new FileOutputStream(outfile));
   }

   public static boolean exportSWF(String inFile, String outdir, boolean isPcode) throws Exception {
      SWF swf = parseSWF(inFile);
      boolean asFound = false;
      for (Tag t : swf.tags) {
         if (t instanceof DoABCTag) {
            ((DoABCTag) t).abc.export(outdir, isPcode);
            asFound = true;
         }
      }
      return asFound;
   }

   private static class OpenFileWorker extends SwingWorker {

      @Override
      protected Object doInBackground() throws Exception {
         try {
            swf = parseSWF(Main.file);
            FileInputStream fis = new FileInputStream(file);
            DEBUG_COPY = true;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
               swf.saveTo(baos);
            } catch (NotSameException nse) {
               if (DEBUG_MODE) {
                  nse.printStackTrace();
                  System.exit(0);
               }
               JOptionPane.showMessageDialog(null, "WARNING: The SWF decompiler may have problems saving this file. Recommended usage is READ ONLY.");
            }
            DEBUG_COPY = false;
            //DEBUG_COPY=true;
         } catch (Exception ex) {
            if (DEBUG_MODE) {
               ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Cannot load SWF file.");
            loadingDialog.setVisible(false);
            return false;
         }
         List<Tag> listAbc = new ArrayList<Tag>();
         for (Tag t : swf.tags) {
            if (t instanceof DoABCTag) {
               listAbc.add(t);
            }
         }


         if (false) {
            JOptionPane.showMessageDialog(null, "This SWF file does not contain any ActionScript parts");
            loadingDialog.setVisible(false);
            if (!openFileDialog()) {
               System.exit(0);
            }
         } else {
            if (listAbc.size() > 0) {
               List<DoABCTag> listAbc2 = new ArrayList<DoABCTag>();
               for (Tag tag : listAbc) {
                  listAbc2.add((DoABCTag) tag);
               }
               abcMainFrame = new com.jpexs.asdec.abc.gui.MainFrame(listAbc2);
               abcMainFrame.display();
            } else {
               actionMainFrame = new com.jpexs.asdec.action.gui.MainFrame(swf.tags);
               actionMainFrame.display();
            }
         }
         loadingDialog.setVisible(false);
         return true;
      }
   }

   public static boolean openFile(String swfFile) {
      if (abcMainFrame != null) {
         abcMainFrame.setVisible(false);
      }
      if (actionMainFrame != null) {
         actionMainFrame.setVisible(false);
      }
      Main.file = swfFile;
      if (Main.loadingDialog == null) {
         Main.loadingDialog = new LoadingDialog();
      }
      Main.loadingDialog.setVisible(true);
      (new OpenFileWorker()).execute();
      return true;
   }

   public static boolean saveFileDialog() {
      JFileChooser fc = new JFileChooser();
      fc.setCurrentDirectory(new File((String) Configuration.getConfig("lastSaveDir", ".")));
      JFrame f = new JFrame();
      View.setWindowIcon(f);
      int returnVal = fc.showSaveDialog(f);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         try {
            Main.saveFile(file.getAbsolutePath());
            Configuration.setConfig("lastSaveDir", file.getParentFile().getAbsolutePath());
            maskURL = null;
            return true;
         } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot write to the file");
         }
      }
      return false;
   }

   public static boolean openFileDialog() {
      maskURL = null;
      JFileChooser fc = new JFileChooser();
      fc.setCurrentDirectory(new File((String) Configuration.getConfig("lastOpenDir", ".")));
      fc.setFileFilter(new FileFilter() {
         @Override
         public boolean accept(File f) {
            return (f.getName().endsWith(".swf")) || (f.isDirectory());
         }

         @Override
         public String getDescription() {
            return "SWF files (*.swf)";
         }
      });
      JFrame f = new JFrame();
      View.setWindowIcon(f);
      int returnVal = fc.showOpenDialog(f);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         Configuration.setConfig("lastOpenDir", fc.getSelectedFile().getParentFile().getAbsolutePath());
         File selfile = fc.getSelectedFile();
         Main.openFile(selfile.getAbsolutePath());
         return true;
      } else {
         return false;
      }
   }

   public static void showModeFrame() {
      if (modeFrame == null) {
         modeFrame = new ModeFrame();
      }
      modeFrame.setVisible(true);
   }

   /**
    * Script for updating license header in java files :-)
    *
    * @param dir Star directory (e.g. "src/")
    */
   public static void updateLicenseInDir(File dir) {
      int defaultStartYear = 2010;
      int defaultFinalYear = 2011;
      String defaultAuthor = "JPEXS";
      String defaultYearStr = "" + defaultStartYear;
      if (defaultFinalYear != defaultStartYear) {
         defaultYearStr += "-" + defaultFinalYear;
      }
      String license = "/*\r\n *  Copyright (C) {year} {author}\r\n * \r\n *  This program is free software: you can redistribute it and/or modify\r\n *  it under the terms of the GNU General Public License as published by\r\n *  the Free Software Foundation, either version 3 of the License, or\r\n *  (at your option) any later version.\r\n * \r\n *  This program is distributed in the hope that it will be useful,\r\n *  but WITHOUT ANY WARRANTY; without even the implied warranty of\r\n *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\r\n *  GNU General Public License for more details.\r\n * \r\n *  You should have received a copy of the GNU General Public License\r\n *  along with this program.  If not, see <http://www.gnu.org/licenses/>.\r\n */\r\n";

      File files[] = dir.listFiles();
      for (File f : files) {
         if (f.isDirectory()) {
            updateLicenseInDir(f);
         } else {
            if (f.getName().endsWith(".java")) {
               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               PrintWriter pw = null;
               try {
                  pw = new PrintWriter(new OutputStreamWriter(baos, "utf8"));
               } catch (UnsupportedEncodingException ex) {
               }
               try {
                  BufferedReader br = new BufferedReader(new FileReader(f));
                  String s = "";
                  boolean packageFound = false;
                  String author = defaultAuthor;
                  String yearStr = defaultYearStr;
                  while ((s = br.readLine()) != null) {
                     if (!packageFound) {
                        if (s.trim().startsWith("package")) {
                           packageFound = true;
                           pw.println(license.replace("{year}", yearStr).replace("{author}", author));
                        } else {
                           Matcher mAuthor = Pattern.compile("^.*Copyright \\(C\\) ([0-9]+)(-[0-9]+)? (.*)$").matcher(s);
                           if (mAuthor.matches()) {
                              author = mAuthor.group(3).trim();
                              int startYear = Integer.parseInt(mAuthor.group(1).trim());
                              if (startYear == defaultFinalYear) {
                                 yearStr = "" + startYear;
                              } else {
                                 yearStr = "" + startYear + "-" + defaultFinalYear;
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
                  br.close();
                  pw.close();
               } catch (IOException ex) {
               }

               FileOutputStream fos;
               try {
                  fos = new FileOutputStream(f);
                  fos.write(baos.toByteArray());
                  fos.close();
               } catch (IOException ex) {
               }
            }
         }
      }

   }

   public static void badArguments() {
      System.err.println("Error: Bad Commandline Arguments!");
      printCmdLineUsage();
      System.exit(1);
   }

   public static void printHeader() {
      System.out.println(applicationName);
      for (int i = 0; i < applicationName.length(); i++) {
         System.out.print("-");
      }
      System.out.println();
   }

   public static void printCmdLineUsage() {
      System.out.println("Commandline arguments:");
      System.out.println(" 1) -help | --help | /?");
      System.out.println(" ...shows commandline arguments (this help)");
      System.out.println(" 2) infile");
      System.out.println(" ...opens SWF file with the decompiler GUI");
      System.out.println(" 3) -proxy (-PXXX)");
      System.out.println("  ...auto start proxy in the tray. Optional parameter -P specifies port for proxy. Defaults to 55555. ");
      System.out.println(" 4) -export (as|pcode) outdirectory infile");
      System.out.println("  ...export infile actionscript to outdirectory as AsctionScript code (\"as\" argument) or as PCode (\"pcode\" argument)");
      System.out.println(" 5) -dumpSWF infile");
      System.out.println("  ...dumps list of SWF tags to console");
      System.out.println(" 6) -compress infile outfile");
      System.out.println("  ...Compress SWF infile and save it to outfile");
      System.out.println(" 7) -decompress infile outfile");
      System.out.println("  ...Decompress infile and save it to outfile");
      System.out.println();
      System.out.println("Examples:");
      System.out.println("java -jar ASDec.jar myfile.swf");
      System.out.println("java -jar ASDec.jar -proxy");
      System.out.println("java -jar ASDec.jar -proxy -P1234");
      System.out.println("java -jar ASDec.jar -export as \"C:\\decompiled\\\" myfile.swf");
      System.out.println("java -jar ASDec.jar -export pcode \"C:\\decompiled\\\" myfile.swf");
      System.out.println("java -jar ASDec.jar -dumpSWF myfile.swf");
      System.out.println("java -jar ASDec.jar -compress myfile.swf myfiledec.swf");
      System.out.println("java -jar ASDec.jar -deccompress myfiledec.swf myfile.swf");
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws IOException {
      View.setWinLookAndFeel();
      Configuration.load();
      if (args.length < 1) {
         showModeFrame();
      } else {
         if (args[0].equals("-proxy")) {
            int port = 55555;
            for (int i = 0; i < args.length; i++) {
               if (args[i].startsWith("-P")) {
                  try {
                     port = Integer.parseInt(args[i].substring(2));
                  } catch (NumberFormatException nex) {
                     System.err.println("Bad port number");
                  }
               }
            }
            if (proxyFrame == null) {
               proxyFrame = new ProxyFrame();
            }
            proxyFrame.setPort(port);
            addTrayIcon();
            switchProxy();
         } else if (args[0].equals("-export")) {
            if (args.length < 4) {
               badArguments();
            }
            String exportFormat = args[1];
            if (!exportFormat.toLowerCase().equals("as")) {
               if (!exportFormat.toLowerCase().equals("pcode")) {
                  System.err.println("Invalid export format:" + exportFormat);
                  badArguments();
               }
            }
            File outDir = new File(args[2]);
            File inFile = new File(args[3]);
            if (!inFile.exists()) {
               System.err.println("Input SWF file does not exist!");
               badArguments();
            }
            commandLineMode = true;
            boolean exportOK = true;
            try {
               printHeader();
               exportOK = exportSWF(inFile.getAbsolutePath(), outDir.getAbsolutePath(), exportFormat.equals("pcode"));
            } catch (Exception ex) {
               exportOK = false;
               System.err.print("FAIL: Exporting Failed on Exception - ");
               System.err.println(ex.getLocalizedMessage());
               System.exit(1);
            }
            if (exportOK) {
               System.out.println("OK");
               System.exit(0);
            } else {
               System.err.println("FAIL: No ActionScript version 3 found in the input file.");
               System.exit(1);
            }
         } else if (args[0].equals("-compress")) {
            if (args.length < 3) {
               badArguments();
            }

            if (SWF.fws2cws(new FileInputStream(args[1]), new FileOutputStream(args[2]))) {
               System.out.println("OK");
            } else {
               System.err.println("FAIL");;
            }
         } else if (args[0].equals("-decompress")) {
            if (args.length < 3) {
               badArguments();
            }

            if (SWF.cws2fws(new FileInputStream(args[1]), new FileOutputStream(args[2]))) {
               System.out.println("OK");
               System.exit(0);
            } else {
               System.err.println("FAIL");
               System.exit(1);
            }
         } else if (args[0].equals("-dumpSWF")) {
            if (args.length < 2) {
               badArguments();
            }
            try {
               dump_tags = true;
               parseSWF(args[1]);
            } catch (Exception ex) {
               System.err.println("Dump failed due to Exception - " + ex.getLocalizedMessage());
               System.exit(1);
            }
            System.exit(0);
         } else if (args[0].equals("-help") || args[0].equals("--help") || args[0].equals("/?")) {
            printHeader();
            printCmdLineUsage();
            System.exit(0);
         } else if (args.length == 1) {
            openFile(args[0]);
         } else {
            badArguments();
         }
      }
   }

   public static String tempFile(String url) {
      File f = new File(Configuration.getASDecHome() + "saved" + File.separator);
      if (!f.exists()) {
         f.mkdirs();
      }
      return Configuration.getASDecHome() + "saved" + File.separator + "asdec_" + Integer.toHexString(url.hashCode()) + ".tmp";

   }

   public static void removeTrayIcon() {
      if (SystemTray.isSupported()) {
         SystemTray tray = SystemTray.getSystemTray();
         if (trayIcon != null) {
            tray.remove(trayIcon);
            trayIcon = null;
         }
      }
   }

   public static void switchProxy() {
      proxyFrame.switchState();
      if (stopMenuItem != null) {
         if (proxyFrame.isRunning()) {
            stopMenuItem.setLabel("Stop proxy");
         } else {
            stopMenuItem.setLabel("Start proxy");
         }
      }
   }

   public static void addTrayIcon() {
      if (trayIcon != null) {
         return;
      }
      if (SystemTray.isSupported()) {
         SystemTray tray = SystemTray.getSystemTray();
         trayIcon = new TrayIcon(View.loadImage("com/jpexs/asdec/gui/graphics/proxy16.png"), "JP ASDec Proxy");
         trayIcon.setImageAutoSize(true);
         PopupMenu trayPopup = new PopupMenu();


         ActionListener trayListener = new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
               if (e.getActionCommand().equals("EXIT")) {
                  Main.exit();
               }
               if (e.getActionCommand().equals("SHOW")) {
                  Main.showProxy();
               }
               if (e.getActionCommand().equals("SWITCH")) {
                  Main.switchProxy();
               }
            }
         };


         MenuItem showMenuItem = new MenuItem("Show proxy");
         showMenuItem.setActionCommand("SHOW");
         showMenuItem.addActionListener(trayListener);
         trayPopup.add(showMenuItem);
         stopMenuItem = new MenuItem("Start proxy");
         stopMenuItem.setActionCommand("SWITCH");
         stopMenuItem.addActionListener(trayListener);
         trayPopup.add(stopMenuItem);
         trayPopup.addSeparator();
         MenuItem exitMenuItem = new MenuItem("Exit");
         exitMenuItem.setActionCommand("EXIT");
         exitMenuItem.addActionListener(trayListener);
         trayPopup.add(exitMenuItem);

         trayIcon.setPopupMenu(trayPopup);
         trayIcon.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void mouseClicked(MouseEvent e) {
               if (e.getButton() == MouseEvent.BUTTON1) {
                  Main.showProxy();
               }
            }
         });
         try {
            tray.add(trayIcon);
         } catch (AWTException ex) {
         }
      }
   }

   public static void exit() {
      Configuration.save();
      System.exit(0);
   }

   public static void about() {
      (new AboutDialog()).setVisible(true);
   }
}
