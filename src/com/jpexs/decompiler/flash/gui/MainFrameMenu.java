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
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import com.sun.jna.Platform;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
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
public abstract class MainFrameMenu implements MenuBuilder {

    private final MainFrame mainFrame;

    private SWF swf;

    public boolean isInternalFlashViewerSelected() {
        return isMenuChecked("/settings/internalViewer"); //miInternalViewer.isSelected();
    }

    private boolean externalFlashPlayerUnavailable;

    public MainFrameMenu(MainFrame mainFrame, boolean externalFlashPlayerUnavailable) {
        registerHotKeys();
        this.mainFrame = mainFrame;
        this.externalFlashPlayerUnavailable = externalFlashPlayerUnavailable;
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
        boolean swfLoaded = swf != null;
        List<ABCContainerTag> abcList = swf != null ? swf.getAbcList() : null;
        boolean hasAbc = swfLoaded && abcList != null && !abcList.isEmpty();
        boolean hasDebugger = hasAbc && DebuggerTools.hasDebugger(swf);

        setMenuEnabled("/file/save", swfLoaded);
        setMenuEnabled("/file/saveAs", swfLoaded);
        setMenuEnabled("/file/saveAsExe", swfLoaded);
        setMenuEnabled("/file/close", swfLoaded);
        setMenuEnabled("/file/closeAll", swfLoaded);

        setMenuEnabled("/file/export", swfLoaded);
        setMenuEnabled("/file/export/exportAll", swfLoaded);
        setMenuEnabled("/file/export/exportFla", swfLoaded);
        setMenuEnabled("/file/export/exportSel", swfLoaded);
        setMenuEnabled("/file/export/exportXml", swfLoaded);

        setMenuEnabled("/file/import", swfLoaded);
        setMenuEnabled("/file/import/importText", swfLoaded);
        setMenuEnabled("/file/import/importScript", swfLoaded);
        setMenuEnabled("/file/import/importSymbolClass", swfLoaded);
        setMenuEnabled("/file/import/importXml", swfLoaded);

        setMenuEnabled("/file/reload", swfLoaded);

        setMenuEnabled("/tools/deobfuscation", swfLoaded);
        setMenuEnabled("/tools/deobfuscation/renameOneIdentifier", swfLoaded);
        setMenuEnabled("/tools/deobfuscation/renameInvalidIdentifiers", swfLoaded);
        setMenuEnabled("/tools/deobfuscation/deobfuscation", hasAbc);

        setMenuEnabled("/tools/search", swfLoaded);
        setMenuEnabled("/tools/replace", swfLoaded);
        setMenuEnabled("/tools/timeline", swfLoaded);

        setMenuEnabled("/tools/gotodocumentclass", hasAbc);
        setMenuEnabled("/tools/debugger/debuggerSwitch", hasAbc);
        setMenuChecked("/tools/debugger/debuggerSwitch", hasDebugger);
        setMenuEnabled("/tools/debugger/debuggerReplaceTrace", hasAbc && hasDebugger);

    }

    private void registerHotKeys() {

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher((KeyEvent e) -> {
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
        });
    }

    public void createMenuBar() {
        initMenu();
        if (supportsAppMenu()) {
            addMenuItem("_", null, null, null, 0, null, false);
            addMenuItem("_/open", translate("menu.file.open"), "open32", this::open, PRIORITY_TOP, this::loadRecent, false);
            addMenuItem("_/save", translate("menu.file.save"), "save32", this::save, PRIORITY_TOP, null, true);
            addMenuItem("_/saveAs", translate("menu.file.saveas"), "saveas32", this::saveAs, PRIORITY_TOP, null, true);
            addSeparator("_");
            addMenuItem("_/exportFla", translate("menu.file.export.fla"), "exportfla32", this::exportFla, PRIORITY_TOP, null, true);
            addMenuItem("_/exportAll", translate("menu.file.export.all"), "export32", this::exportAll, PRIORITY_TOP, null, true);
            addMenuItem("_/exportSel", translate("menu.file.export.selection"), "exportsel32", this::exportSelected, PRIORITY_TOP, null, true);
            addSeparator("_");
            addMenuItem("_/checkUpdates", translate("menu.help.checkupdates"), "update32", this::checkUpdates, PRIORITY_TOP, null, true);
            addMenuItem("_/about", translate("menu.help.about"), "about32", this::about, PRIORITY_TOP, null, true);
            addMenuItem("_/close", translate("menu.file.close"), "close32", this::close, PRIORITY_TOP, null, true);
            addMenuItem("_/closeAll", translate("menu.file.closeAll"), "close32", this::closeAll, PRIORITY_TOP, null, true);
            addMenuItem("_/$exit", translate("menu.file.exit"), "exit32", this::exit, PRIORITY_TOP, null, true);
            finishMenu("_");
        }

        addMenuItem("/file", translate("menu.file"), null, null, 0, null, false);
        addMenuItem("/file/open", translate("menu.file.open"), "open32", this::open, PRIORITY_TOP, this::loadRecent, !supportsMenuAction());

        if (!supportsMenuAction()) {
            addMenuItem("/file/recent", translate("menu.recentFiles"), null, null, 0, this::loadRecent, false);
            finishMenu("/file/recent");
        } else {
            finishMenu("/file/open");
        }
        addMenuItem("/file/save", translate("menu.file.save"), "save32", this::save, PRIORITY_TOP, null, true);
        addMenuItem("/file/saveAs", translate("menu.file.saveas"), "saveas16", this::saveAs, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/saveAsExe", translate("menu.file.saveasexe"), "saveasexe16", this::saveAsExe, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/reload", translate("menu.file.reload"), "reload16", this::reload, PRIORITY_MEDIUM, null, true);

        addSeparator("/file");

        addMenuItem("/file/export", translate("menu.export"), null, null, 0, null, false);
        addMenuItem("/file/export/exportFla", translate("menu.file.export.fla"), "exportfla32", this::exportFla, PRIORITY_TOP, null, true);
        addMenuItem("/file/export/exportXml", translate("menu.file.export.xml"), "exportxml32", this::exportSelected, PRIORITY_MEDIUM, null, true);

        addMenuItem("/file/export/exportAll", translate("menu.file.export.all"), "export16", this::exportAll, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/export/exportSel", translate("menu.file.export.selection"), "exportsel16", this::exportSelected, PRIORITY_MEDIUM, null, true);
        finishMenu("/file/export");

        addMenuItem("/file/import", translate("menu.import"), null, null, 0, null, false);
        addMenuItem("/file/import/importXml", translate("menu.file.import.xml"), "importxml32", this::importSwfXml, PRIORITY_TOP, null, true);
        addMenuItem("/file/import/importText", translate("menu.file.import.text"), "importtext32", this::importText, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/import/importScript", translate("menu.file.import.script"), "importtext32", this::importScript, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/import/importSymbolClass", translate("menu.file.import.symbolClass"), "importsymbolclass32", this::importSymbolClass, PRIORITY_MEDIUM, null, true);
        finishMenu("/file/import");

        addSeparator("/file");

        addMenuItem("/file/view", translate("menu.view"), null, null, 0, null, false);
        addToggleMenuItem("/file/view/viewResources", translate("menu.file.view.resources"), "view", "viewresources16", this::viewModeResourcesButtonActionPerformed, PRIORITY_MEDIUM);
        addToggleMenuItem("/file/view/viewHex", translate("menu.file.view.hex"), "view", "viewhex16", this::viewModeHexDumpButtonActionPerformed, PRIORITY_MEDIUM);
        finishMenu("/file/view");

        addSeparator("/file");
        addMenuItem("/file/close", translate("menu.file.close"), "close32", this::close, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/closeAll", translate("menu.file.closeAll"), "close32", this::closeAll, PRIORITY_MEDIUM, null, true);

        if (!supportsAppMenu()) {
            addMenuItem("/file/exit", translate("menu.file.exit"), "exit32", this::exit, PRIORITY_TOP, null, true);
        }
        finishMenu("/file");

        if (Configuration.dumpView.get()) {
            setGroupSelection("view", "/file/view/viewHex");
        } else {
            setGroupSelection("view", "/file/view/viewResources");
        }

        addMenuItem("/tools", translate("menu.tools"), null, null, 0, null, false);
        addMenuItem("/tools/search", translate("menu.tools.search"), "search16", (ActionEvent e) -> {
            search(e, null);
        }, PRIORITY_TOP, null, true);

        addMenuItem("/tools/replace", translate("menu.tools.replace"), "replace32", this::replace, PRIORITY_TOP, null, true);
        addToggleMenuItem("/tools/timeline", translate("menu.tools.timeline"), null, "timeline32", this::timelineButtonActionPerformed, PRIORITY_TOP);

        addMenuItem("/tools/proxy", translate("menu.tools.proxy"), "proxy16", this::showProxy, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/searchmemory", translate("menu.tools.searchmemory"), "loadmemory16", this::loadFromMemory, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/searchcache", translate("menu.tools.searchcache"), "loadcache16", this::loadFromCache, PRIORITY_MEDIUM, null, true);

        addMenuItem("/tools/deobfuscation", translate("menu.tools.deobfuscation"), "deobfuscate16", null, 0, null, false);
        addMenuItem("/tools/deobfuscation/renameOneIdentifier", translate("menu.tools.deobfuscation.globalrename"), "rename16", this::renameOneIdentifier, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/deobfuscation/renameInvalidIdentifiers", translate("menu.tools.deobfuscation.renameinvalid"), "renameall16", this::renameIdentifiers, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/deobfuscation/deobfuscation", translate("menu.tools.deobfuscation.pcode"), "deobfuscate32", this::deobfuscate, PRIORITY_TOP, null, true);
        finishMenu("/tools/deobfuscation");

        addMenuItem("/tools/debugger", translate("menu.debugger"), null, null, 0, null, false);
        addToggleMenuItem("/tools/debugger/debuggerSwitch", translate("menu.debugger.switch"), null, "debugger32", this::debuggerSwitchButtonActionPerformed, PRIORITY_TOP);
        addMenuItem("/tools/debugger/debuggerReplaceTrace", translate("menu.debugger.replacetrace"), "debuggerreplace16", this::debuggerReplaceTraceCalls, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/debugger/debuggerShowLog", translate("menu.debugger.showlog"), "debuggerlog16", this::debuggerShowLog, PRIORITY_MEDIUM, null, true);
        finishMenu("/tools/debugger");

        addMenuItem("/tools/gotodocumentclass", translate("menu.tools.gotodocumentclass"), "gotomainclass32", this::gotoDucumentClass, PRIORITY_TOP, null, true);
        finishMenu("/tools");

        //Settings
        addMenuItem("/settings", translate("menu.settings"), null, null, 0, null, false);

        addToggleMenuItem("/settings/autodeobfuscation", translate("menu.settings.autodeobfuscation"), null, null, this::autoDeobfuscate, 0);

        addToggleMenuItem("/settings/internalViewer", translate("menu.settings.internalflashviewer"), null, null, this::internalViewerSwitch, 0);

        addToggleMenuItem("/settings/parallelspeedup", translate("menu.settings.parallelspeedup"), null, null, this::parallelSpeedUp, 0);

        addToggleMenuItem("/settings/disableDecompilation", translate("menu.settings.disabledecompilation"), null, null, this::disableDecompilation, 0);

        addToggleMenuItem("/settings/cacheOnDisk", translate("menu.settings.cacheOnDisk"), null, null, this::cacheOnDisk, 0);

        addToggleMenuItem("/settings/gotoMainClassOnStartup", translate("menu.settings.gotoMainClassOnStartup"), null, null, this::gotoDucumentClassOnStartup, 0);

        addToggleMenuItem("/settings/autoRenameIdentifiers", translate("menu.settings.autoRenameIdentifiers"), null, null, this::autoRenameIdentifiers, 0);

        if (Platform.isWindows()) {
            addToggleMenuItem("/settings/associate", translate("menu.settings.addtocontextmenu"), null, null, this::associate, 0);
        }
        addMenuItem("/settings/language", translate("menu.language"), null, null, 0, null, false);
        addMenuItem("/settings/language/setlanguage", translate("menu.settings.language"), "setlanguage32", this::setLanguage, PRIORITY_TOP, null, true);
        finishMenu("/settings/language");

        addMenuItem("/settings/deobfuscation", translate("menu.deobfuscation"), null, null, 0, null, false);
        addToggleMenuItem("/settings/deobfuscation/old", translate("menu.file.deobfuscation.old"), "deobfuscation", "deobfuscateold16", (ActionEvent e) -> {
            deobfuscationMode(e, 0);
        }, 0);
        addToggleMenuItem("/settings/deobfuscation/new", translate("menu.file.deobfuscation.new"), "deobfuscation", "deobfuscatenew16", (ActionEvent e) -> {
            deobfuscationMode(e, 1);
        }, 0);

        finishMenu("/settings/deobfuscation");

        addMenuItem("/settings/advancedSettings", translate("menu.advancedsettings.advancedsettings"), null, null, 0, null, false);
        addMenuItem("/settings/advancedSettings/advancedSettings", translate("menu.advancedsettings.advancedsettings"), "settings32", this::advancedSettings, PRIORITY_TOP, null, true);
        addMenuItem("/settings/advancedSettings/clearRecentFiles", translate("menu.tools.otherTools.clearRecentFiles"), "clearrecent16", this::clearRecentFiles, PRIORITY_MEDIUM, null, true);

        finishMenu("/settings/advancedSettings");

        finishMenu("/settings");

        setMenuChecked("/settings/autodeobfuscation", Configuration.autoDeobfuscate.get());
        setMenuChecked("/settings/internalViewer", Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        setMenuChecked("/settings/parallelspeedup", Configuration.parallelSpeedUp.get());
        setMenuChecked("/settings/disableDecompilation", !Configuration.decompile.get());
        setMenuChecked("/settings/cacheOnDisk", !Configuration.cacheOnDisk.get());
        setMenuChecked("/settings/gotoMainClassOnStartup", Configuration.gotoMainClassOnStartup.get());
        setMenuChecked("/settings/autoRenameIdentifiers", Configuration.autoRenameIdentifiers.get());

        if (externalFlashPlayerUnavailable) {
            setMenuEnabled("/settings/internalViewer", false);
        }
        int deobfuscationMode = Configuration.deobfuscationMode.get();
        switch (deobfuscationMode) {
            case 0:
                setGroupSelection("deobfuscation", "/settings/deobfuscation/old");
                break;
            case 1:
                setGroupSelection("deobfuscation", "/settings/deobfuscation/new");
                break;
        }
        if (Platform.isWindows()) {
            setMenuChecked("/settings/associate", ContextMenuTools.isAddedToContextMenu());

        }
        //Help
        addMenuItem("/help", translate("menu.help"), null, null, 0, null, false);
        addMenuItem("/help/about", translate("menu.help.about"), "about32", this::about, PRIORITY_TOP, null, true);
        addMenuItem("/help/helpUs", translate("menu.help.helpus"), "donate32", this::helpUs, PRIORITY_TOP, null, true);
        addMenuItem("/help/checkUpdates", translate("menu.help.checkupdates"), "update16", this::checkUpdates, PRIORITY_MEDIUM, null, true);
        addMenuItem("/help/homepage", translate("menu.help.homepage"), "homepage16", this::homePage, PRIORITY_MEDIUM, null, true);
        finishMenu("/help");

        if (Configuration.showDebugMenu.get() || Configuration.debugMode.get()) {

            addMenuItem("/debug", "Debug", null, null, 0, null, false);
            addMenuItem("/debug/removeNonScripts", "Remove non scripts", "update16", e -> removeNonScripts(), PRIORITY_MEDIUM, null, true);
            addMenuItem("/debug/refreshDecompiled", "Refresh decompiled script", "update16", e -> refreshDecompiled(), PRIORITY_MEDIUM, null, true);
            addMenuItem("/debug/checkResources", "Check resources", "update16", e -> checkResources(), PRIORITY_MEDIUM, null, true);
            addMenuItem("/debug/callGc", "Call System.gc()", "update16", e -> System.gc(), PRIORITY_MEDIUM, null, true);
            addMenuItem("/debug/emptyCache", "Empty cache", "update16", e -> {
                SWF nswf = mainFrame.getPanel().getCurrentSwf();
                if (nswf != null) {
                    nswf.clearAllCache();
                }
            }, PRIORITY_MEDIUM, null, true);
            addMenuItem("/debug/memoryInformation", "Memory information", "update16", e -> {
                String architecture = System.getProperty("sun.arch.data.model");
                Runtime runtime = Runtime.getRuntime();
                String info = "Architecture: " + architecture + Helper.newLine
                        + "Max: " + (runtime.maxMemory() / 1024 / 1024) + "MB" + Helper.newLine
                        + "Used: " + (runtime.totalMemory() / 1024 / 1024) + "MB" + Helper.newLine
                        + "Free: " + (runtime.freeMemory() / 1024 / 1024) + "MB";
                View.showMessageDialog(null, info);
                SWF nswf = mainFrame.getPanel().getCurrentSwf();
                if (nswf != null) {
                    nswf.clearAllCache();
                }
            }, PRIORITY_MEDIUM, null, true);
            addMenuItem("/debug/fixAs3Code", "Fix AS3 code", "update16", e -> {
                SWF nswf = mainFrame.getPanel().getCurrentSwf();
                if (nswf != null) {
                    nswf.fixAS3Code();
                }
            }, PRIORITY_MEDIUM, null, true);
            finishMenu("/debug");
        }

        finishMenu("");
    }

    private void viewModeResourcesButtonActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(false);
        mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
        setGroupSelection("view", "/file/view/viewResources");
        setMenuChecked("/tools/timeline", false);

    }

    private void viewModeHexDumpButtonActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(true);
        mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
        setGroupSelection("view", "/file/view/viewHex");
        setMenuChecked("/tools/timeline", false);
    }

    private void debuggerSwitchButtonActionPerformed(ActionEvent evt) {
        boolean debuggerOn = isMenuChecked("/tools/debugger/debuggerSwitch");
        if (!debuggerOn || View.showConfirmDialog((Component) mainFrame, translate("message.debugger"), translate("dialog.message.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, Configuration.displayDebuggerInfo, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            switchDebugger();
            mainFrame.getPanel().refreshDecompiled();
        } else {
            if (debuggerOn) {
                setMenuChecked("/tools/debugger/debuggerSwitch", false);
            }
        }
        setMenuEnabled("/tools/debugger/debuggerReplaceTrace", isMenuChecked("/tools/debugger/debuggerSwitch"));
    }

    private void timelineButtonActionPerformed(ActionEvent evt) {
        if (isMenuChecked("/tools/timeline")) {
            if (!mainFrame.getPanel().showView(MainPanel.VIEW_TIMELINE)) {
                setMenuChecked("/tools/timeline", false);
            } else {
                setGroupSelection("view", null);
            }
        } else {
            if (Configuration.dumpView.get()) {
                setGroupSelection("view", "/file/view/viewHex");
                mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
            } else {
                setGroupSelection("view", "/file/view/viewResources");
                mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
            }
        }
    }

    protected void loadRecent(ActionEvent evt) {
        List<String> recentFiles = Configuration.getRecentFiles();
        clearMenu("/file/" + (supportsMenuAction() ? "open" : "recent"));
        clearMenu("_/open");

        for (int i = recentFiles.size() - 1; i >= 0; i--) {
            final String f = recentFiles.get(i);
            ActionListener a = (ActionEvent e) -> {
                if (Main.openFile(f, null) == OpenFileResult.NOT_FOUND) {
                    if (View.showConfirmDialog(null, translate("message.confirm.recentFileNotFound"), translate("message.confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
                        Configuration.removeRecentFile(f);
                    }
                }
            };
            addMenuItem("/file/" + (supportsMenuAction() ? "open" : "recent") + "/" + i, f, null, a, 0, null, true);
            addMenuItem("_/open/" + i, f, null, a, 0, null, true);
        }

        finishMenu("/file/" + (supportsMenuAction() ? "open" : "recent"));
        finishMenu("_/open");

    }

}
