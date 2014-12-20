/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.helpers.CheckResources;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.ScrollPane;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author JPEXS
 */
public abstract class MainFrameMenu {

    private final MainFrame mainFrame;
    private SWF swf;
    
    public abstract boolean isInternalFlashViewerSelected();

    public MainFrameMenu(MainFrame mainFrame) {
        registerHotKeys();
        this.mainFrame = mainFrame;
    }
    
    protected String translate(String key) {
        return mainFrame.translate(key);
    }

    protected boolean open() {
        Main.openFileDialog();
        return true;
    }
    
    protected boolean save() {
        if (swf != null) {
            boolean saved = false;
            if (swf.binaryData != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    swf.saveTo(baos);
                    swf.binaryData.binaryData = new ByteArrayRange(baos.toByteArray());
                    swf.binaryData.setModified(true);
                    saved = true;
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameClassicMenu.class.getName()).log(Level.SEVERE, "Cannot save SWF", ex);
                }
            } else if (swf.file == null) {
                saved = saveAs(swf, SaveFileMode.SAVEAS);
            } else {
                try {
                    Main.saveFile(swf, swf.file);
                    saved = true;
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameClassicMenu.class.getName()).log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, translate("error.file.save"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
            if (saved) {
                swf.clearModified();
            }
            
            return true;
        }
        
        return false;
    }
    
    protected boolean saveAs() {
        if (swf != null) {
            if (saveAs(swf, SaveFileMode.SAVEAS)) {
                swf.clearModified();
            }
            
            return true;
        }
        
        return false;
    }
    
    private boolean saveAs(SWF swf, SaveFileMode mode) {
        if (Main.saveFileDialog(swf, mode)) {
            mainFrame.setTitle(ApplicationInfo.applicationVerName + (Configuration.displayFileName.get() ? " - " + swf.getFileTitle() : ""));
            updateComponents(swf);
            return true;
        }
        return false;
    }

    protected void saveAsExe() {
        if (swf != null) {
            saveAs(swf, SaveFileMode.EXE);
        }
    }
    
    protected void close() {
        Main.closeFile(mainFrame.getPanel().getCurrentSwfList());
    }
    
    protected boolean closeAll() {
        if (swf != null) {
            Main.closeAll();
            return true;
        }
        
        return false;
    }
    
    protected void importText() {
        mainFrame.getPanel().importText(swf);
    }
    
    protected boolean export(boolean onlySelected) {
        if (swf != null) {
            mainFrame.getPanel().export(onlySelected);
            return true;
        }

        return false;
    }
    
    protected void exportFla() {
        mainFrame.getPanel().exportFla(swf);
    }
    
    protected boolean search(boolean searhInText) {
        if (swf != null) {
            mainFrame.getPanel().searchInActionScriptOrText(searhInText);
            return true;
        }
        
        return false;
    }
    
    protected void restoreControlFlow(boolean all) {
        mainFrame.getPanel().restoreControlFlow(all);
    }
    
    protected void showProxy() {
        Main.showProxy();
    }
    
    protected boolean clearLog() {
        ErrorLogFrame.getInstance().clearLog();
        return true;
    }
    
    protected void renameOneIdentifier() {
        mainFrame.getPanel().renameOneIdentifier(swf);
    }
    
    protected void renameIdentifiers() {
        mainFrame.getPanel().renameIdentifiers(swf);
    }
    
    protected void deobfuscate() {
        mainFrame.getPanel().deobfuscate();
    }
    
    protected void setSubLimiter(boolean value) {
        Main.setSubLimiter(value);
    }
    
    protected void removeNonScripts() {
        mainFrame.getPanel().removeNonScripts(swf);
    }
    
    protected void refreshDecompiled() {
        mainFrame.getPanel().refreshDecompiled();
    }
    
    protected void checkResources() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(os);
        CheckResources.checkResources(stream);
        final String str = new String(os.toByteArray(), Utf8Helper.charset);
        JDialog dialog = new JDialog() {

            @Override
            public void setVisible(boolean bln) {
                setSize(new Dimension(800, 600));
                Container cnt = getContentPane();
                cnt.setLayout(new BorderLayout());
                ScrollPane scrollPane = new ScrollPane();
                JEditorPane editor = new JEditorPane();
                editor.setEditable(false);
                editor.setText(str);
                scrollPane.add(editor);
                this.add(scrollPane, BorderLayout.CENTER);
                this.setModal(true);
                View.centerScreen(this);
                super.setVisible(bln);
            }
        };
        dialog.setVisible(true);
    }
    
    protected void checkUpdates() {
        if (!Main.checkForUpdates()) {
            View.showMessageDialog(null, translate("update.check.nonewversion"), translate("update.check.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    protected void helpUs() {
        String helpUsURL = ApplicationInfo.PROJECT_PAGE + "/help_us.html";
        if (!View.navigateUrl(helpUsURL)) {
            View.showMessageDialog(null, translate("message.helpus").replace("%url%", helpUsURL));
        }
    }
    
    protected void homePage() {
        String homePageURL = ApplicationInfo.PROJECT_PAGE;
        if (!View.navigateUrl(homePageURL)) {
            View.showMessageDialog(null, translate("message.homepage").replace("%url%", homePageURL));
        }
    }
    
    protected void about() {
        Main.about();
    }
    
    protected boolean reload() {
        if (swf != null) {
            if (View.showConfirmDialog(null, translate("message.confirm.reload"), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                Main.reloadApp();
            }
            
            return true;
        }
        
        return false;
    }
    
    protected void advancedSettings() {
        Main.advancedSettings();
    }
    
    protected void loadFromMemory() {
        Main.loadFromMemory();
    }
    
    protected void loadFromCache() {
        Main.loadFromCache();
    }
    
    protected void setLanguage() {
        new SelectLanguageDialog().display();
    }
    
    protected void exit() {
        mainFrame.getPanel().setVisible(false);
        if (Main.proxyFrame != null) {
            if (Main.proxyFrame.isVisible()) {
                return;
            }
        }
        Main.exit();
    }
    
    public void updateComponents(SWF swf) {
        this.swf = swf;
    }
    
    private void registerHotKeys() {
        
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (((JFrame) mainFrame).isActive()) {
                    int code = e.getKeyCode();
                    if (e.isControlDown() && e.isShiftDown()) {
                        switch (code) {
                            case KeyEvent.VK_O:
                                return open();
                            case KeyEvent.VK_S:
                                return save();
                            case KeyEvent.VK_A:
                                return saveAs();
                            case KeyEvent.VK_F:
                                return search(false);
                            case KeyEvent.VK_T:
                                return search(true);
                            case KeyEvent.VK_R:
                                return reload();
                            case KeyEvent.VK_X:
                                return closeAll();
                            case KeyEvent.VK_D:
                                return clearLog();
                            case KeyEvent.VK_E:
                                return export(false);
                        }
                    }
                }

                return false;
            }
        });
    }
}
