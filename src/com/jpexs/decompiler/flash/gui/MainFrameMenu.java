/*
 *  Copyright (C) 2010-2015 JPEXS
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
import com.jpexs.decompiler.flash.SWFBundle;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.decompiler.flash.gui.debugger.DebuggerTools;
import com.jpexs.decompiler.flash.gui.helpers.CheckResources;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
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

    protected boolean open(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }

        Main.openFileDialog();
        return true;
    }

    protected boolean save(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }

        if (swf != null) {
            boolean saved = false;
            if (swf.swfList != null && swf.swfList.isBundle()) {
                SWFBundle bundle = swf.swfList.bundle;
                if (!bundle.isReadOnly()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        swf.saveTo(baos);
                        saved = bundle.putSWF(swf.getFileTitle(), new ByteArrayInputStream(baos.toByteArray()));
                    } catch (IOException ex) {
                        Logger.getLogger(MainFrameMenu.class.getName()).log(Level.SEVERE, "Cannot save SWF", ex);
                    }
                }
            } else if (swf.binaryData != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    swf.saveTo(baos);
                    swf.binaryData.binaryData = new ByteArrayRange(baos.toByteArray());
                    swf.binaryData.setModified(true);
                    saved = true;
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameMenu.class.getName()).log(Level.SEVERE, "Cannot save SWF", ex);
                }
            } else if (swf.getFile() == null) {
                saved = saveAs(swf, SaveFileMode.SAVEAS);
            } else {
                try {
                    Main.saveFile(swf, swf.getFile());
                    saved = true;
                } catch (IOException ex) {
                    Logger.getLogger(MainFrameMenu.class.getName()).log(Level.SEVERE, null, ex);
                    View.showMessageDialog(null, translate("error.file.save"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
            if (saved) {
                swf.clearModified();
                mainFrame.getPanel().refreshTree(swf);
            }

            return true;
        }

        return false;
    }

    protected boolean saveAs(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }

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

    protected void saveAsExe(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        if (swf != null) {
            saveAs(swf, SaveFileMode.EXE);
        }
    }

    protected void close(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        if (swf == null) {
            return;
        }

        Main.closeFile(swf.swfList);
    }

    protected boolean closeAll(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }

        if (swf != null) {
            Main.closeAll();
            return true;
        }

        return false;
    }

    protected void importText(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importText(swf);
    }

    protected void importScript(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importScript(swf);
    }

    protected void importSymbolClass(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importSymbolClass(swf);
    }

    protected void exportAll(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        export(false);
    }

    protected void exportSelected(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        export(true);
    }

    protected boolean export(boolean onlySelected) {
        if (swf != null) {
            mainFrame.getPanel().export(onlySelected);
            return true;
        }

        return false;
    }

    protected void exportFla(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().exportFla(swf);
    }

    protected void importSwfXml(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importSwfXml();
    }

    protected void exportSwfXml(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().exportSwfXml();
    }

    protected boolean search(ActionEvent evt, Boolean searchInText) {
        if (swf != null) {
            mainFrame.getPanel().searchInActionScriptOrText(searchInText);
            return true;
        }

        return false;
    }

    protected boolean replace(ActionEvent evt) {
        if (swf != null) {
            mainFrame.getPanel().replaceText();
            return true;
        }

        return false;
    }

    protected void restoreControlFlow(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        restoreControlFlow(false);
    }

    protected void restoreControlFlowAll(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        restoreControlFlow(true);
    }

    protected void restoreControlFlow(boolean all) {
        mainFrame.getPanel().restoreControlFlow(all);
    }

    protected void showProxy(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        Main.showProxy();
    }

    protected boolean clearLog() {
        ErrorLogFrame.getInstance().clearLog();
        return true;
    }

    protected void renameOneIdentifier(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().renameOneIdentifier(swf);
    }

    protected void renameIdentifiers(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().renameIdentifiers(swf);
    }

    protected void deobfuscate(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().deobfuscate();
    }

    protected void setSubLimiter(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();
        Main.setSubLimiter(selected);
    }

    protected void switchDebugger() {
        DebuggerTools.switchDebugger(swf);
    }

    protected void debuggerShowLog(ActionEvent evt) {
        DebuggerTools.debuggerShowLog();
    }

    protected void debuggerReplaceTraceCalls(ActionEvent evt) {
        ReplaceTraceDialog rtd = new ReplaceTraceDialog(Configuration.lastDebuggerReplaceFunction.get());
        rtd.setVisible(true);
        if (rtd.getValue() != null) {
            String fname = rtd.getValue();
            DebuggerTools.replaceTraceCalls(swf, fname);
            mainFrame.getPanel().refreshDecompiled();
            Configuration.lastDebuggerReplaceFunction.set(rtd.getValue());
        }
    }

    protected void clearRecentFiles(ActionEvent evt) {
        Configuration.recentFiles.set(null);
    }

    protected void removeNonScripts() {
        mainFrame.getPanel().removeNonScripts(swf);
    }

    protected void refreshDecompiled() {
        mainFrame.getPanel().refreshDecompiled();
    }

    protected boolean previousTag() {
        return mainFrame.getPanel().previousTag();
    }

    protected boolean nextTag() {
        return mainFrame.getPanel().nextTag();
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

    protected void checkUpdates(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        if (!Main.checkForUpdates()) {
            View.showMessageDialog(null, translate("update.check.nonewversion"), translate("update.check.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void helpUs(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        String helpUsURL = ApplicationInfo.PROJECT_PAGE + "/help_us.html?utm_source=app&utm_medium=menu&utm_campaign=app";
        if (!View.navigateUrl(helpUsURL)) {
            View.showMessageDialog(null, translate("message.helpus").replace("%url%", helpUsURL));
        }
    }

    protected void homePage(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        String homePageURL = ApplicationInfo.PROJECT_PAGE + "?utm_source=app&utm_medium=menu&utm_campaign=app";
        if (!View.navigateUrl(homePageURL)) {
            View.showMessageDialog(null, translate("message.homepage").replace("%url%", homePageURL));
        }
    }

    protected void about(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        Main.about();
    }

    protected boolean reload(ActionEvent evt) {
        if (swf != null) {
            if (View.showConfirmDialog(null, translate("message.confirm.reload"), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                Main.reloadApp();
            }

            return true;
        }

        return false;
    }

    protected void advancedSettings(ActionEvent evt) {
        Main.advancedSettings();
    }

    protected void loadFromMemory(ActionEvent evt) {
        Main.loadFromMemory();
    }

    protected void loadFromCache(ActionEvent evt) {
        Main.loadFromCache();
    }

    protected void gotoDucumentClassOnStartup(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.gotoMainClassOnStartup.set(selected);
    }

    protected void autoRenameIdentifiers(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.autoRenameIdentifiers.set(selected);
    }

    protected void cacheOnDisk(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.cacheOnDisk.set(selected);
        if (selected) {
            Cache.setStorageType(Cache.STORAGE_FILES);
        } else {
            Cache.setStorageType(Cache.STORAGE_MEMORY);
        }
    }

    protected void setLanguage(ActionEvent evt) {
        new SelectLanguageDialog().display();
    }

    protected void disableDecompilation(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.decompile.set(!selected);
        mainFrame.getPanel().disableDecompilationChanged();
    }

    protected void associate(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        if (selected == ContextMenuTools.isAddedToContextMenu()) {
            return;
        }
        ContextMenuTools.addToContextMenu(selected, false);

        // Update checkbox menuitem accordingly (User can cancel rights elevation)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                button.setSelected(ContextMenuTools.isAddedToContextMenu());
            }
        }, 1000); // It takes some time registry change to apply
    }

    protected void gotoDucumentClass(ActionEvent evt) {
        mainFrame.getPanel().gotoDocumentClass(mainFrame.getPanel().getCurrentSwf());
    }

    protected void parallelSpeedUp(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        String confStr = translate("message.confirm.parallel") + "\r\n";
        if (selected) {
            confStr += " " + translate("message.confirm.on");
        } else {
            confStr += " " + translate("message.confirm.off");
        }
        if (View.showConfirmDialog(null, confStr, translate("message.parallel"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.parallelSpeedUp.set(selected);
        } else {
            button.setSelected(Configuration.parallelSpeedUp.get());
        }
    }

    protected void internalViewerSwitch(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.internalFlashViewer.set(selected);
        mainFrame.getPanel().reload(true);
    }

    protected void autoDeobfuscate(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        if (View.showConfirmDialog(mainFrame.getPanel(), translate("message.confirm.autodeobfuscate") + "\r\n" + (selected ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.autoDeobfuscate.set(selected);
            mainFrame.getPanel().autoDeobfuscateChanged();
        } else {
            button.setSelected(Configuration.autoDeobfuscate.get());
        }
    }

    protected void deobfuscationMode(ActionEvent evt, int mode) {
        Configuration.deobfuscationMode.set(mode);
        mainFrame.getPanel().autoDeobfuscateChanged();
    }

    protected void exit(ActionEvent evt) {
        JFrame frame = (JFrame) mainFrame;
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void updateComponents(SWF swf) {
        this.swf = swf;
    }

    private void registerHotKeys() {

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (((JFrame) mainFrame).isActive() && e.getID() == KeyEvent.KEY_PRESSED) {
                    int code = e.getKeyCode();
                    if (e.isControlDown() && e.isShiftDown()) {
                        switch (code) {
                            case KeyEvent.VK_O:
                                return open(null);
                            case KeyEvent.VK_S:
                                return save(null);
                            case KeyEvent.VK_A:
                                return saveAs(null);
                            case KeyEvent.VK_F:
                                return search(null, false);
                            case KeyEvent.VK_T:
                                return search(null, true);
                            case KeyEvent.VK_R:
                                return reload(null);
                            case KeyEvent.VK_X:
                                return closeAll(null);
                            case KeyEvent.VK_D:
                                return clearLog();
                            case KeyEvent.VK_E:
                                return export(false);
                        }
                    } else if (e.isControlDown() && !e.isShiftDown()) {
                        switch (code) {
                            case KeyEvent.VK_UP:
                                return previousTag();
                            case KeyEvent.VK_DOWN:
                                return nextTag();
                        }
                    }
                }

                return false;
            }
        });
    }
}
