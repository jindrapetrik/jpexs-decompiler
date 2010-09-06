package com.jpexs.asdec;

import com.jpexs.asdec.abc.NotSameException;
import com.jpexs.asdec.gui.LoadingDialog;
import com.jpexs.asdec.gui.ModeFrame;
import com.jpexs.asdec.gui.View;
import com.jpexs.asdec.gui.proxy.ProxyFrame;
import com.jpexs.asdec.tags.DoABCTag;
import com.jpexs.asdec.tags.Tag;
import com.jpexs.proxy.Replacement;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


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
    public static final String version = "alpha7";
    public static String applicationName = "JP ActionScript Decompiler v." + version;
    public static LoadingDialog loadingDialog = new LoadingDialog();
    public static ModeFrame modeFrame;
    private static boolean working = false;
    private static TrayIcon trayIcon;
    private static MenuItem stopMenuItem;
    public static boolean DEBUG_COPY = false;
    public static boolean DEBUG_MODE = false;
    public static boolean DISABLE_DANGEROUS = false;

    public static String getFileTitle() {
        if (maskURL != null) return maskURL;
        return file;
    }

    /**
     * List of replacements
     */
    public static java.util.List<Replacement> replacements = new ArrayList<Replacement>();


    private static String getASDecHome() {
        String dir = ".";//System.getProperty("user.home");
        if (!dir.endsWith(File.separator)) dir += File.separator;
        dir += "config" + File.separator;
        return dir;
    }

    private static String getReplacementsFile() {
        return getASDecHome() + "replacements.ini";
    }

    /**
     * Saves replacements to file for future use
     */
    public static void saveReplacements() {
        try {
            File f = new File(getASDecHome());
            if (!f.exists()) f.mkdir();
            PrintWriter pw = new PrintWriter(new FileWriter(getReplacementsFile()));
            for (Replacement r : replacements) {
                pw.println(r.urlPattern);
                pw.println(r.targetFile);
            }
            pw.close();
        } catch (IOException e) {

        }
    }

    /**
     * Load replacements from file
     */
    public static void loadReplacements() {
        replacements = new ArrayList<Replacement>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(getReplacementsFile()));
            String s = "";
            while ((s = br.readLine()) != null) {
                Replacement r = new Replacement(s, br.readLine());
                replacements.add(r);
            }
            br.close();
        } catch (IOException e) {

        }
    }

    public static boolean isWorking() {
        return working;
    }

    public static void showProxy() {
        if (proxyFrame == null) proxyFrame = new ProxyFrame();
        proxyFrame.setVisible(true);
        proxyFrame.setState(Frame.NORMAL);
    }

    public static void startWork(String name) {
        working = true;
        abcMainFrame.setStatus(name);
    }

    public static void stopWork() {
        working = false;
        abcMainFrame.setStatus("");
    }

    public static SWF parseSWF(String file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        SWF locswf = new SWF(fis);
        return locswf;
    }


    public static void saveFile(String outfile) throws IOException {
        file = outfile;
        swf.saveTo(new FileOutputStream(outfile));
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
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Cannot load SWF file.");
                loadingDialog.setVisible(false);
                return false;
            }
            List<Tag> listAbc = new ArrayList<Tag>();
            for (Tag t : swf.tags) {
                if (t instanceof DoABCTag) listAbc.add(t);
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
        if (abcMainFrame != null)
            abcMainFrame.setVisible(false);
        if (actionMainFrame != null)
            actionMainFrame.setVisible(false);
        Main.file = swfFile;
        Main.loadingDialog.setVisible(true);
        (new OpenFileWorker()).execute();
        return true;
    }


    public static boolean saveFileDialog(Frame f) {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                Main.saveFile(file.getAbsolutePath());
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
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selfile = fc.getSelectedFile();
            Main.openFile(selfile.getAbsolutePath());
            return true;
        } else {
            return false;
        }
    }


    public static void showModeFrame() {
        if (modeFrame == null) modeFrame = new ModeFrame();
        modeFrame.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        View.setWinLookAndFeel();
        loadReplacements();
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
                if (proxyFrame == null) proxyFrame = new ProxyFrame();
                proxyFrame.setPort(port);
                addTrayIcon();
                switchProxy();
            } else {
                openFile(args[0]);
            }
        }
    }


    public static String tempFile(String url) {
        File f = new File(getASDecHome() + "saved" + File.separator);
        if (!f.exists()) f.mkdirs();
        return getASDecHome() + "saved" + File.separator + "asdec_" + Integer.toHexString(url.hashCode()) + ".tmp";

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
        if (trayIcon != null) return;
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
                    if (e.getActionCommand().equals("EXIT")) Main.exit();
                    if (e.getActionCommand().equals("SHOW")) Main.showProxy();
                    if (e.getActionCommand().equals("SWITCH")) Main.switchProxy();
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
        saveReplacements();
        System.exit(0);
    }
}
