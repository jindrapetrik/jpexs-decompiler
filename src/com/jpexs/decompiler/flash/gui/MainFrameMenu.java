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
import com.jpexs.decompiler.flash.SWFSourceInfo;
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
import java.awt.KeyEventDispatcher;
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
import javax.swing.JComboBox;
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

    private KeyEventDispatcher keyEventDispatcher;

    private SWF swf;

    public boolean isInternalFlashViewerSelected() {
        return isMenuChecked("/settings/internalViewer"); //miInternalViewer.isSelected();
    }

    private final boolean externalFlashPlayerUnavailable;

    public MainFrameMenu(MainFrame mainFrame, boolean externalFlashPlayerUnavailable) {
        registerHotKeys();
        this.mainFrame = mainFrame;
        this.externalFlashPlayerUnavailable = externalFlashPlayerUnavailable;
    }

    protected String translate(String key) {
        return mainFrame.translate(key);
    }

    protected boolean openActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }

        Main.openFileDialog();
        return true;
    }

    protected boolean saveActionPerformed(ActionEvent evt) {
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

    protected boolean saveAsActionPerformed(ActionEvent evt) {
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

    protected void saveAsExeActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        if (swf != null) {
            saveAs(swf, SaveFileMode.EXE);
        }
    }

    protected void closeActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        if (swf == null) {
            return;
        }

        Main.closeFile(swf.swfList);
    }

    protected boolean closeAllActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return false;
        }

        if (swf != null) {
            Main.closeAll();
            return true;
        }

        return false;
    }

    protected void importTextActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importText(swf);
    }

    protected void importScriptActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importScript(swf);
    }

    protected void importSymbolClassActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importSymbolClass(swf);
    }

    protected void exportAllActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        export(false);
    }

    protected void exportSelectedActionPerformed(ActionEvent evt) {
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

    protected void exportFlaActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().exportFla(swf);
    }

    protected void importXmlActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().importSwfXml();
    }

    protected void exportXmlActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().exportSwfXml();
    }

    protected boolean searchActionPerformed(ActionEvent evt) {
        return search(evt, null);
    }

    protected boolean search(ActionEvent evt, Boolean searchInText) {
        if (swf != null) {
            mainFrame.getPanel().searchInActionScriptOrText(searchInText);
            return true;
        }

        return false;
    }

    protected boolean replaceActionPerformed(ActionEvent evt) {
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

    protected void showProxyActionPerformed(ActionEvent evt) {
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

    protected void renameInvalidIdentifiers(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        mainFrame.getPanel().renameIdentifiers(swf);
    }

    protected void deobfuscationActionPerformed(ActionEvent evt) {
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

    protected void debuggerShowLogActionPerformed(ActionEvent evt) {
        DebuggerTools.debuggerShowLog();
    }

    protected void debuggerReplaceTraceCallsActionPerformed(ActionEvent evt) {
        ReplaceTraceDialog rtd = new ReplaceTraceDialog(Configuration.lastDebuggerReplaceFunction.get());
        rtd.setVisible(true);
        if (rtd.getValue() != null) {
            String fname = rtd.getValue();
            DebuggerTools.replaceTraceCalls(swf, fname);
            mainFrame.getPanel().refreshDecompiled();
            Configuration.lastDebuggerReplaceFunction.set(rtd.getValue());
        }
    }

    protected void clearRecentFilesActionPerformed(ActionEvent evt) {
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
        CheckResources.checkResources(stream, null);
        final String str = new String(os.toByteArray(), Utf8Helper.charset);
        JDialog dialog = new JDialog() {

            @Override
            public void setVisible(boolean bln) {
                setSize(new Dimension(800, 600));
                Container cnt = getContentPane();
                cnt.setLayout(new BorderLayout());
                String[] languages = SelectLanguageDialog.getAvailableLanguages().clone();
                languages[0] = "all";
                JComboBox<String> languagesComboBox = new JComboBox<>(languages);
                this.add(languagesComboBox, BorderLayout.NORTH);
                ScrollPane scrollPane = new ScrollPane();
                JEditorPane editor = new JEditorPane();
                editor.setEditable(false);
                editor.setText(str);
                scrollPane.add(editor);
                this.add(scrollPane, BorderLayout.CENTER);
                this.setModal(true);
                View.centerScreen(this);
                languagesComboBox.addActionListener((ActionEvent e) -> {
                    String lang = (String) languagesComboBox.getSelectedItem();
                    if (lang.equals("all")) {
                        lang = null;
                    }
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    PrintStream stream = new PrintStream(os);
                    CheckResources.checkResources(stream, lang);
                    String str = new String(os.toByteArray(), Utf8Helper.charset);
                    editor.setText(str);
                });
                super.setVisible(bln);
            }
        };
        dialog.setVisible(true);
    }

    protected void checkUpdatesActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        if (!Main.checkForUpdates()) {
            View.showMessageDialog(null, translate("update.check.nonewversion"), translate("update.check.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void helpUsActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        String helpUsURL = ApplicationInfo.PROJECT_PAGE + "/help_us.html?utm_source=app&utm_medium=menu&utm_campaign=app";
        if (!View.navigateUrl(helpUsURL)) {
            View.showMessageDialog(null, translate("message.helpus").replace("%url%", helpUsURL));
        }
    }

    protected void homePageActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        String homePageURL = ApplicationInfo.PROJECT_PAGE + "?utm_source=app&utm_medium=menu&utm_campaign=app";
        if (!View.navigateUrl(homePageURL)) {
            View.showMessageDialog(null, translate("message.homepage").replace("%url%", homePageURL));
        }
    }

    protected void aboutActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        Main.about();
    }

    protected boolean reloadActionPerformed(ActionEvent evt) {
        if (swf != null) {
            if (View.showConfirmDialog(null, translate("message.confirm.reload"), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                Main.reloadApp();
            }

            return true;
        }

        Main.reloadApp();
        return true;
    }

    protected void advancedSettingsActionPerformed(ActionEvent evt) {
        Main.advancedSettings();
    }

    protected void searchMemoryActionPerformed(ActionEvent evt) {
        Main.loadFromMemory();
    }

    protected void searchCacheActionPerformed(ActionEvent evt) {
        Main.loadFromCache();
    }

    protected void gotoDucumentClassOnStartupActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.gotoMainClassOnStartup.set(selected);
    }

    protected void autoRenameIdentifiersActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.autoRenameIdentifiers.set(selected);
    }

    protected void cacheOnDiskActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.cacheOnDisk.set(selected);
        if (selected) {
            Cache.setStorageType(Cache.STORAGE_FILES);
        } else {
            Cache.setStorageType(Cache.STORAGE_MEMORY);
        }
    }

    protected void setLanguageActionPerformed(ActionEvent evt) {
        new SelectLanguageDialog().display();
    }

    protected void disableDecompilationActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.decompile.set(!selected);
        mainFrame.getPanel().disableDecompilationChanged();
    }

    protected void associateActionPerformed(ActionEvent evt) {
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

    protected void gotoDucumentClassActionPerformed(ActionEvent evt) {
        mainFrame.getPanel().gotoDocumentClass(mainFrame.getPanel().getCurrentSwf());
    }

    protected void parallelSpeedUpActionPerformed(ActionEvent evt) {
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

    protected void internalViewerSwitchActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        Configuration.internalFlashViewer.set(selected);
        mainFrame.getPanel().reload(true);
    }

    protected void autoDeobfuscationActionPerformed(ActionEvent evt) {
        AbstractButton button = (AbstractButton) evt.getSource();
        boolean selected = button.isSelected();

        if (View.showConfirmDialog(mainFrame.getPanel(), translate("message.confirm.autodeobfuscate") + "\r\n" + (selected ? translate("message.confirm.on") : translate("message.confirm.off")), translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.autoDeobfuscate.set(selected);
            mainFrame.getPanel().autoDeobfuscateChanged();
        } else {
            button.setSelected(Configuration.autoDeobfuscate.get());
        }
    }

    /*protected void deobfuscationMode(ActionEvent evt, int mode) {
     Configuration.deobfuscationMode.set(mode);
     mainFrame.getPanel().autoDeobfuscateChanged();
     }*/
    protected void exitActionPerformed(ActionEvent evt) {
        JFrame frame = (JFrame) mainFrame;
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void updateComponents() {
        updateComponents(swf);
    }

    public void updateComponents(SWF swf) {
        this.swf = swf;
        boolean swfSelected = swf != null;
        boolean isWorking = Main.isWorking();
        List<ABCContainerTag> abcList = swf != null ? swf.getAbcList() : null;
        boolean hasAbc = swfSelected && abcList != null && !abcList.isEmpty();
        boolean hasDebugger = hasAbc && DebuggerTools.hasDebugger(swf);
        MainPanel mainPanel = mainFrame.getPanel();
        boolean swfLoaded = mainPanel != null ? !mainPanel.getSwfs().isEmpty() : false;

        setMenuEnabled("_/open", !isWorking);
        setMenuEnabled("/file/open", !isWorking);
        setMenuEnabled("_/save", swfSelected && !isWorking);
        setMenuEnabled("/file/save", swfSelected && !isWorking);
        setMenuEnabled("_/saveAs", swfSelected && !isWorking);
        setMenuEnabled("/file/saveAs", swfSelected && !isWorking);
        setMenuEnabled("/file/saveAsExe", swfSelected && !isWorking);
        setMenuEnabled("_/close", swfSelected && !isWorking);
        setMenuEnabled("/file/close", swfSelected && !isWorking);
        setMenuEnabled("_/closeAll", swfLoaded && !isWorking);
        setMenuEnabled("/file/closeAll", swfLoaded && !isWorking);

        setMenuEnabled("/file/export", swfSelected);
        setMenuEnabled("_/exportAll", swfSelected && !isWorking);
        setMenuEnabled("/file/export/exportAll", swfSelected && !isWorking);
        setMenuEnabled("_/exportFla", swfSelected && !isWorking);
        setMenuEnabled("/file/export/exportFla", swfSelected && !isWorking);
        setMenuEnabled("_/exportSelected", swfSelected && !isWorking);
        setMenuEnabled("/file/export/exportSelected", swfSelected && !isWorking);
        setMenuEnabled("/file/export/exportXml", swfSelected && !isWorking);

        setMenuEnabled("/file/import", swfSelected);
        setMenuEnabled("/file/import/importText", swfSelected && !isWorking);
        setMenuEnabled("/file/import/importScript", swfSelected && !isWorking);
        setMenuEnabled("/file/import/importSymbolClass", swfSelected && !isWorking);
        setMenuEnabled("/file/import/importXml", swfSelected && !isWorking);

        setMenuEnabled("/tools/deobfuscation", swfSelected);
        setMenuEnabled("/tools/deobfuscation/renameOneIdentifier", swfSelected && !isWorking);
        setMenuEnabled("/tools/deobfuscation/renameInvalidIdentifiers", swfSelected && !isWorking);
        setMenuEnabled("/tools/deobfuscation/deobfuscation", hasAbc);

        setMenuEnabled("/tools/search", swfSelected);
        setMenuEnabled("/tools/replace", swfSelected);
        setMenuEnabled("/tools/timeline", swfSelected);
        setMenuEnabled("/tools/showProxy", !isWorking);

        setMenuEnabled("/tools/gotoDocumentClass", hasAbc);
        setMenuEnabled("/tools/debugger/debuggerSwitch", hasAbc);
        setMenuChecked("/tools/debugger/debuggerSwitch", hasDebugger);
        setMenuEnabled("/tools/debugger/debuggerReplaceTrace", hasAbc && hasDebugger);

        setMenuEnabled("_/checkUpdates", !isWorking);
        setMenuEnabled("/help/checkUpdates", !isWorking);
        setMenuEnabled("/help/helpUs", !isWorking);
        setMenuEnabled("/help/homePage", !isWorking);
        setMenuEnabled("_/about", !isWorking);
        setMenuEnabled("/help/about", !isWorking);
    }

    private void registerHotKeys() {

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(keyEventDispatcher = this::dispatchKeyEvent);
    }

    public void createMenuBar() {
        initMenu();

        if (supportsAppMenu()) {
            addMenuItem("_", null, null, null, 0, null, false);
            addMenuItem("_/open", translate("menu.file.open"), "open32", this::openActionPerformed, PRIORITY_TOP, this::loadRecent, false);
            addMenuItem("_/save", translate("menu.file.save"), "save32", this::saveActionPerformed, PRIORITY_TOP, null, true);
            addMenuItem("_/saveAs", translate("menu.file.saveas"), "saveas32", this::saveAsActionPerformed, PRIORITY_TOP, null, true);
            addSeparator("_");
            addMenuItem("_/exportFla", translate("menu.file.export.fla"), "exportfla32", this::exportFlaActionPerformed, PRIORITY_TOP, null, true);
            addMenuItem("_/exportAll", translate("menu.file.export.all"), "export32", this::exportAllActionPerformed, PRIORITY_TOP, null, true);
            addMenuItem("_/exportSelected", translate("menu.file.export.selection"), "exportsel32", this::exportSelectedActionPerformed, PRIORITY_TOP, null, true);
            addSeparator("_");
            addMenuItem("_/checkUpdates", translate("menu.help.checkupdates"), "update32", this::checkUpdatesActionPerformed, PRIORITY_TOP, null, true);
            addMenuItem("_/about", translate("menu.help.about"), "about32", this::aboutActionPerformed, PRIORITY_TOP, null, true);
            addMenuItem("_/close", translate("menu.file.close"), "close32", this::closeActionPerformed, PRIORITY_TOP, null, true);
            addMenuItem("_/closeAll", translate("menu.file.closeAll"), "closeall32", this::closeAllActionPerformed, PRIORITY_TOP, null, true);
            addMenuItem("_/$exit", translate("menu.file.exit"), "exit32", this::exitActionPerformed, PRIORITY_TOP, null, true);
            finishMenu("_");
        }

        addMenuItem("/file", translate("menu.file"), null, null, 0, null, false);
        addMenuItem("/file/open", translate("menu.file.open"), "open32", this::openActionPerformed, PRIORITY_TOP, this::loadRecent, !supportsMenuAction());

        if (!supportsMenuAction()) {
            addMenuItem("/file/recent", translate("menu.recentFiles"), null, null, 0, this::loadRecent, false);
            finishMenu("/file/recent");
        } else {
            finishMenu("/file/open");
        }

        addMenuItem("/file/save", translate("menu.file.save"), "save32", this::saveActionPerformed, PRIORITY_TOP, null, true);
        addMenuItem("/file/saveAs", translate("menu.file.saveas"), "saveas16", this::saveAsActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/saveAsExe", translate("menu.file.saveasexe"), "saveasexe16", this::saveAsExeActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/reload", translate("menu.file.reload"), "reload16", this::reloadActionPerformed, PRIORITY_MEDIUM, null, true);

        addSeparator("/file");

        addMenuItem("/file/export", translate("menu.export"), null, null, 0, null, false);
        addMenuItem("/file/export/exportFla", translate("menu.file.export.fla"), "exportfla32", this::exportFlaActionPerformed, PRIORITY_TOP, null, true);
        addMenuItem("/file/export/exportXml", translate("menu.file.export.xml"), "exportxml32", this::exportXmlActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/export/exportAll", translate("menu.file.export.all"), "export16", this::exportAllActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/export/exportSelected", translate("menu.file.export.selection"), "exportsel16", this::exportSelectedActionPerformed, PRIORITY_MEDIUM, null, true);
        finishMenu("/file/export");

        addMenuItem("/file/import", translate("menu.import"), null, null, 0, null, false);
        addMenuItem("/file/import/importXml", translate("menu.file.import.xml"), "importxml32", this::importXmlActionPerformed, PRIORITY_TOP, null, true);
        addMenuItem("/file/import/importText", translate("menu.file.import.text"), "importtext32", this::importTextActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/import/importScript", translate("menu.file.import.script"), "importscript32", this::importScriptActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/import/importSymbolClass", translate("menu.file.import.symbolClass"), "importsymbolclass32", this::importSymbolClassActionPerformed, PRIORITY_MEDIUM, null, true);
        finishMenu("/file/import");

        addMenuItem("/file/view", translate("menu.view"), null, null, 0, null, false);
        addToggleMenuItem("/file/view/viewResources", translate("menu.file.view.resources"), "view", "viewresources16", this::viewResourcesActionPerformed, PRIORITY_MEDIUM);
        addToggleMenuItem("/file/view/viewHex", translate("menu.file.view.hex"), "view", "viewhex16", this::viewHexActionPerformed, PRIORITY_MEDIUM);
        finishMenu("/file/view");

        addSeparator("/file");
        addMenuItem("/file/close", translate("menu.file.close"), "close32", this::closeActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/file/closeAll", translate("menu.file.closeAll"), "closeall32", this::closeAllActionPerformed, PRIORITY_MEDIUM, null, true);

        if (!supportsAppMenu()) {
            addSeparator("/file");
            addMenuItem("/file/exit", translate("menu.file.exit"), "exit32", this::exitActionPerformed, PRIORITY_TOP, null, true);
        }

        finishMenu("/file");

        if (Configuration.dumpView.get()) {
            setGroupSelection("view", "/file/view/viewHex");
        } else {
            setGroupSelection("view", "/file/view/viewResources");
        }

        addMenuItem("/tools", translate("menu.tools"), null, null, 0, null, false);
        addMenuItem("/tools/search", translate("menu.tools.search"), "search16", this::searchActionPerformed, PRIORITY_TOP, null, true);

        addMenuItem("/tools/replace", translate("menu.tools.replace"), "replace32", this::replaceActionPerformed, PRIORITY_TOP, null, true);
        addToggleMenuItem("/tools/timeline", translate("menu.tools.timeline"), null, "timeline32", this::timelineActionPerformed, PRIORITY_TOP);

        addMenuItem("/tools/showProxy", translate("menu.tools.proxy"), "proxy16", this::showProxyActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/searchMemory", translate("menu.tools.searchMemory"), "loadmemory16", this::searchMemoryActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/searchCache", translate("menu.tools.searchCache"), "loadcache16", this::searchCacheActionPerformed, PRIORITY_MEDIUM, null, true);

        addMenuItem("/tools/deobfuscation", translate("menu.tools.deobfuscation"), "deobfuscate16", null, 0, null, false);
        addMenuItem("/tools/deobfuscation/renameOneIdentifier", translate("menu.tools.deobfuscation.globalrename"), "rename16", this::renameOneIdentifier, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/deobfuscation/renameInvalidIdentifiers", translate("menu.tools.deobfuscation.renameinvalid"), "renameall16", this::renameInvalidIdentifiers, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/deobfuscation/deobfuscation", translate("menu.tools.deobfuscation.pcode"), "deobfuscate32", this::deobfuscationActionPerformed, PRIORITY_TOP, null, true);
        finishMenu("/tools/deobfuscation");

        addMenuItem("/tools/debugger", translate("menu.debugger"), null, null, 0, null, false);
        addToggleMenuItem("/tools/debugger/debuggerSwitch", translate("menu.debugger.switch"), null, "debugger32", this::debuggerSwitchActionPerformed, PRIORITY_TOP);
        addMenuItem("/tools/debugger/debuggerReplaceTrace", translate("menu.debugger.replacetrace"), "debuggerreplace16", this::debuggerReplaceTraceCallsActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/tools/debugger/debuggerShowLog", translate("menu.debugger.showlog"), "debuggerlog16", this::debuggerShowLogActionPerformed, PRIORITY_MEDIUM, null, true);
        finishMenu("/tools/debugger");

        addMenuItem("/tools/gotoDocumentClass", translate("menu.tools.gotoDocumentClass"), "gotomainclass32", this::gotoDucumentClassActionPerformed, PRIORITY_TOP, null, true);
        finishMenu("/tools");

        //Settings
        addMenuItem("/settings", translate("menu.settings"), null, null, 0, null, false);

        addToggleMenuItem("/settings/autoDeobfuscation", translate("menu.settings.autodeobfuscation"), null, null, this::autoDeobfuscationActionPerformed, 0);
        addToggleMenuItem("/settings/internalViewer", translate("menu.settings.internalflashviewer"), null, null, this::internalViewerSwitchActionPerformed, 0);
        addToggleMenuItem("/settings/parallelSpeedUp", translate("menu.settings.parallelspeedup"), null, null, this::parallelSpeedUpActionPerformed, 0);
        addToggleMenuItem("/settings/disableDecompilation", translate("menu.settings.disabledecompilation"), null, null, this::disableDecompilationActionPerformed, 0);
        addToggleMenuItem("/settings/cacheOnDisk", translate("menu.settings.cacheOnDisk"), null, null, this::cacheOnDiskActionPerformed, 0);
        addToggleMenuItem("/settings/gotoMainClassOnStartup", translate("menu.settings.gotoMainClassOnStartup"), null, null, this::gotoDucumentClassOnStartupActionPerformed, 0);
        addToggleMenuItem("/settings/autoRenameIdentifiers", translate("menu.settings.autoRenameIdentifiers"), null, null, this::autoRenameIdentifiersActionPerformed, 0);

        if (Platform.isWindows()) {
            addToggleMenuItem("/settings/associate", translate("menu.settings.addtocontextmenu"), null, null, this::associateActionPerformed, 0);
        }

        addMenuItem("/settings/language", translate("menu.language"), null, null, 0, null, false);
        addMenuItem("/settings/language/setLanguage", translate("menu.settings.language"), "setlanguage32", this::setLanguageActionPerformed, PRIORITY_TOP, null, true);
        finishMenu("/settings/language");

        /*addMenuItem("/settings/deobfuscation", translate("menu.deobfuscation"), null, null, 0, null, false);
         addToggleMenuItem("/settings/deobfuscation/old", translate("menu.file.deobfuscation.old"), "deobfuscation", "deobfuscateold16", (ActionEvent e) -> {
         deobfuscationMode(e, 0);
         }, 0);
         addToggleMenuItem("/settings/deobfuscation/new", translate("menu.file.deobfuscation.new"), "deobfuscation", "deobfuscatenew16", (ActionEvent e) -> {
         deobfuscationMode(e, 1);
         }, 0);

         finishMenu("/settings/deobfuscation");*/
        addMenuItem("/settings/advancedSettings", translate("menu.advancedsettings.advancedsettings"), null, null, 0, null, false);
        addMenuItem("/settings/advancedSettings/advancedSettings", translate("menu.advancedsettings.advancedsettings"), "settings32", this::advancedSettingsActionPerformed, PRIORITY_TOP, null, true);
        addMenuItem("/settings/advancedSettings/clearRecentFiles", translate("menu.tools.otherTools.clearRecentFiles"), "clearrecent16", this::clearRecentFilesActionPerformed, PRIORITY_MEDIUM, null, true);
        finishMenu("/settings/advancedSettings");

        finishMenu("/settings");

        setMenuChecked("/settings/autoDeobfuscation", Configuration.autoDeobfuscate.get());
        Configuration.autoDeobfuscate.addListener((Boolean newValue) -> {
            setMenuChecked("/settings/autoDeobfuscation", newValue);
        });

        setMenuChecked("/settings/internalViewer", Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        Configuration.internalFlashViewer.addListener((Boolean newValue) -> {
            setMenuChecked("/settings/internalViewer", newValue || externalFlashPlayerUnavailable);
        });

        setMenuChecked("/settings/parallelSpeedUp", Configuration.parallelSpeedUp.get());
        Configuration.parallelSpeedUp.addListener((Boolean newValue) -> {
            setMenuChecked("/settings/parallelSpeedUp", newValue);
        });

        setMenuChecked("/settings/disableDecompilation", !Configuration.decompile.get());
        Configuration.decompile.addListener((Boolean newValue) -> {
            setMenuChecked("/settings/disableDecompilation", !newValue);
        });

        setMenuChecked("/settings/cacheOnDisk", Configuration.cacheOnDisk.get());
        Configuration.cacheOnDisk.addListener((Boolean newValue) -> {
            setMenuChecked("/settings/cacheOnDisk", newValue);
        });

        setMenuChecked("/settings/gotoMainClassOnStartup", Configuration.gotoMainClassOnStartup.get());
        Configuration.gotoMainClassOnStartup.addListener((Boolean newValue) -> {
            setMenuChecked("/settings/gotoMainClassOnStartup", newValue);
        });

        setMenuChecked("/settings/autoRenameIdentifiers", Configuration.autoRenameIdentifiers.get());
        Configuration.autoRenameIdentifiers.addListener((Boolean newValue) -> {
            setMenuChecked("/settings/autoRenameIdentifiers", newValue);
        });

        if (externalFlashPlayerUnavailable) {
            setMenuEnabled("/settings/internalViewer", false);
        }

        /*int deobfuscationMode = Configuration.deobfuscationMode.get();
         switch (deobfuscationMode) {
         case 0:
         setGroupSelection("deobfuscation", "/settings/deobfuscation/old");
         break;
         case 1:
         setGroupSelection("deobfuscation", "/settings/deobfuscation/new");
         break;
         }*/
        if (Platform.isWindows()) {
            setMenuChecked("/settings/associate", ContextMenuTools.isAddedToContextMenu());
        }

        //Help
        addMenuItem("/help", translate("menu.help"), null, null, 0, null, false);
        addMenuItem("/help/helpUs", translate("menu.help.helpus"), "donate32", this::helpUsActionPerformed, PRIORITY_TOP, null, true);
        addMenuItem("/help/homePage", translate("menu.help.homepage"), "homepage16", this::homePageActionPerformed, PRIORITY_MEDIUM, null, true);
        addSeparator("/help");
        addMenuItem("/help/checkUpdates", translate("menu.help.checkupdates"), "update16", this::checkUpdatesActionPerformed, PRIORITY_MEDIUM, null, true);
        addMenuItem("/help/about", translate("menu.help.about"), "about32", this::aboutActionPerformed, PRIORITY_TOP, null, true);
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
            addMenuItem("/debug/openTestSwfs", "Open test SWFs", "update16", e -> {
                String path;

                SWFSourceInfo[] sourceInfos = new SWFSourceInfo[2];
                String mainPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                path = mainPath + "\\..\\..\\libsrc\\ffdec_lib\\testdata\\as2\\as2.swf";
                sourceInfos[0] = new SWFSourceInfo(null, path, null);
                path = mainPath + "\\..\\..\\libsrc\\ffdec_lib\\testdata\\as3\\as3.swf";
                sourceInfos[1] = new SWFSourceInfo(null, path, null);
                Main.openFile(sourceInfos);
            }, PRIORITY_MEDIUM, null, true);
            finishMenu("/debug");
        }

        finishMenu("");
    }

    private void viewResourcesActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(false);
        mainFrame.getPanel().showView(MainPanel.VIEW_RESOURCES);
        setGroupSelection("view", "/file/view/viewResources");
        setMenuChecked("/tools/timeline", false);
    }

    private void viewHexActionPerformed(ActionEvent evt) {
        Configuration.dumpView.set(true);
        mainFrame.getPanel().showView(MainPanel.VIEW_DUMP);
        setGroupSelection("view", "/file/view/viewHex");
        setMenuChecked("/tools/timeline", false);
    }

    private void debuggerSwitchActionPerformed(ActionEvent evt) {
        boolean debuggerOn = isMenuChecked("/tools/debugger/debuggerSwitch");
        if (!debuggerOn || View.showConfirmDialog((Component) mainFrame, translate("message.debugger"), translate("dialog.message.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, Configuration.displayDebuggerInfo, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            switchDebugger();
            mainFrame.getPanel().refreshDecompiled();
        } else {
            setMenuChecked("/tools/debugger/debuggerSwitch", false);
        }
        setMenuEnabled("/tools/debugger/debuggerReplaceTrace", isMenuChecked("/tools/debugger/debuggerSwitch"));
    }

    private void timelineActionPerformed(ActionEvent evt) {
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

    public void dispose() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.removeKeyEventDispatcher(keyEventDispatcher);
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        if (((JFrame) mainFrame).isActive() && e.getID() == KeyEvent.KEY_PRESSED) {
            int code = e.getKeyCode();
            if (e.isControlDown() && e.isShiftDown()) {
                switch (code) {
                    case KeyEvent.VK_O:
                        return openActionPerformed(null);
                    case KeyEvent.VK_S:
                        return saveActionPerformed(null);
                    case KeyEvent.VK_A:
                        return saveAsActionPerformed(null);
                    case KeyEvent.VK_F:
                        return search(null, false);
                    case KeyEvent.VK_T:
                        return search(null, true);
                    case KeyEvent.VK_R:
                        return reloadActionPerformed(null);
                    case KeyEvent.VK_X:
                        return closeAllActionPerformed(null);
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
}
