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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.console.ContextMenuTools;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.sun.jna.Platform;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *
 * @author JPEXS
 */
public class MainFrameClassicMenu extends MainFrameMenu {

    private final MainFrameClassic mainFrame;

    private JCheckBoxMenuItem miAutoDeobfuscation;

    private JCheckBoxMenuItem miInternalViewer;

    private JCheckBoxMenuItem miParallelSpeedUp;

    private JCheckBoxMenuItem miAssociate;

    private JCheckBoxMenuItem miDecompile;

    private JCheckBoxMenuItem miCacheDisk;

    private JCheckBoxMenuItem miGotoMainClassOnStartup;

    private JCheckBoxMenuItem miAutoRenameIdentifiers;

    public MainFrameClassicMenu(MainFrameClassic mainFrame, boolean externalFlashPlayerUnavailable) {
        super(mainFrame);
        this.mainFrame = mainFrame;

        createMenuBar(externalFlashPlayerUnavailable);
    }

    @Override
    public boolean isInternalFlashViewerSelected() {
        return miInternalViewer.isSelected();
    }

    private String fixCommandTitle(String title) {
        if (title.length() > 2) {
            if (title.charAt(1) == ' ') {
                title = title.charAt(0) + "\u00A0" + title.substring(2);
            }
        }
        return title;
    }

    private void createMenuBar(boolean externalFlashPlayerUnavailable) {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu(translate("menu.file"));
        JMenuItem miOpen = new JMenuItem(translate("menu.file.open"));
        miOpen.setIcon(View.getIcon("open16"));
        miOpen.addActionListener(this::open);
        JMenuItem miSave = new JMenuItem(translate("menu.file.save"));
        miSave.setIcon(View.getIcon("save16"));
        miSave.addActionListener(this::save);
        JMenuItem miSaveAs = new JMenuItem(translate("menu.file.saveas"));
        miSaveAs.setIcon(View.getIcon("saveas16"));
        miSaveAs.addActionListener(this::saveAs);
        JMenuItem miSaveAsExe = new JMenuItem(translate("menu.file.saveasexe"));
        miSaveAsExe.setIcon(View.getIcon("saveas16"));
        miSaveAsExe.addActionListener(this::saveAsExe);

        JMenuItem menuExportFla = new JMenuItem(translate("menu.file.export.fla"));
        menuExportFla.addActionListener(this::exportFla);
        menuExportFla.setIcon(View.getIcon("flash16"));

        JMenuItem menuExportAll = new JMenuItem(translate("menu.file.export.all"));
        menuExportAll.addActionListener(this::exportAll);
        JMenuItem menuExportSel = new JMenuItem(translate("menu.file.export.selection"));
        menuExportSel.addActionListener(this::exportSelected);
        menuExportAll.setIcon(View.getIcon("export16"));
        menuExportSel.setIcon(View.getIcon("exportsel16"));

        menuFile.add(miOpen);
        menuFile.add(miSave);
        menuFile.add(miSaveAs);
        menuFile.add(miSaveAsExe);
        menuFile.add(menuExportFla);
        menuFile.add(menuExportAll);
        menuFile.add(menuExportSel);
        menuFile.addSeparator();
        JMenuItem miClose = new JMenuItem(translate("menu.file.exit"));
        miClose.setIcon(View.getIcon("exit16"));
        miClose.addActionListener(this::exit);
        menuFile.add(miClose);
        menuBar.add(menuFile);
        JMenu menuDeobfuscation = new JMenu(translate("menu.tools.deobfuscation"));
        menuDeobfuscation.setIcon(View.getIcon("deobfuscate16"));

        JMenuItem miDeobfuscation = new JMenuItem(translate("menu.tools.deobfuscation.pcode"));
        miDeobfuscation.addActionListener(this::deobfuscate);

        miAutoDeobfuscation = new JCheckBoxMenuItem(translate("menu.settings.autodeobfuscation"));
        miAutoDeobfuscation.setSelected(Configuration.autoDeobfuscate.get());
        miAutoDeobfuscation.addActionListener(this::autoDeobfuscate);

        JMenuItem miRenameOneIdentifier = new JMenuItem(translate("menu.tools.deobfuscation.globalrename"));
        miRenameOneIdentifier.addActionListener(this::renameOneIdentifier);

        JMenuItem miRenameIdentifiers = new JMenuItem(translate("menu.tools.deobfuscation.renameinvalid"));
        miRenameIdentifiers.addActionListener(this::renameIdentifiers);

        menuDeobfuscation.add(miRenameOneIdentifier);
        menuDeobfuscation.add(miRenameIdentifiers);
        menuDeobfuscation.add(miDeobfuscation);
        JMenu menuTools = new JMenu(translate("menu.tools"));
        JMenuItem miProxy = new JMenuItem(translate("menu.tools.proxy"));
        miProxy.setIcon(View.getIcon("proxy16"));
        miProxy.addActionListener(this::showProxy);

        JMenuItem miSearchScript = new JMenuItem(translate("menu.tools.searchas"));
        miSearchScript.addActionListener((ActionEvent e) -> {
            search(e, null);
        });
        miSearchScript.setIcon(View.getIcon("search16"));

        menuTools.add(miSearchScript);

        miInternalViewer = new JCheckBoxMenuItem(translate("menu.settings.internalflashviewer"));
        miInternalViewer.setSelected(Configuration.internalFlashViewer.get() || externalFlashPlayerUnavailable);
        if (externalFlashPlayerUnavailable) {
            miInternalViewer.setEnabled(false);
        }
        miInternalViewer.addActionListener(this::internalViewerSwitch);

        miParallelSpeedUp = new JCheckBoxMenuItem(translate("menu.settings.parallelspeedup"));
        miParallelSpeedUp.setSelected(Configuration.parallelSpeedUp.get());
        miParallelSpeedUp.addActionListener(this::parallelSpeedUp);

        menuTools.add(miProxy);

        menuTools.add(menuDeobfuscation);

        JMenuItem miGotoDocumentClass = new JMenuItem(translate("menu.tools.gotodocumentclass"));
        miGotoDocumentClass.addActionListener(this::gotoDucumentClass);
        menuBar.add(menuTools);

        miDecompile = new JCheckBoxMenuItem(translate("menu.settings.disabledecompilation"));
        miDecompile.setSelected(!Configuration.decompile.get());
        miDecompile.addActionListener(this::disableDecompilation);

        miCacheDisk = new JCheckBoxMenuItem(translate("menu.settings.cacheOnDisk"));
        miCacheDisk.setSelected(Configuration.cacheOnDisk.get());
        miCacheDisk.addActionListener(this::cacheOnDisk);

        miGotoMainClassOnStartup = new JCheckBoxMenuItem(translate("menu.settings.gotoMainClassOnStartup"));
        miGotoMainClassOnStartup.setSelected(Configuration.gotoMainClassOnStartup.get());
        miGotoMainClassOnStartup.addActionListener(this::gotoDucumentClassOnStartup);

        miAutoRenameIdentifiers = new JCheckBoxMenuItem(translate("menu.settings.autoRenameIdentifiers"));
        miAutoRenameIdentifiers.setSelected(Configuration.autoRenameIdentifiers.get());
        miAutoRenameIdentifiers.addActionListener(this::autoRenameIdentifiers);

        JMenu menuSettings = new JMenu(translate("menu.settings"));
        menuSettings.add(miAutoDeobfuscation);
        menuSettings.add(miInternalViewer);
        menuSettings.add(miParallelSpeedUp);
        menuSettings.add(miDecompile);
        menuSettings.add(miCacheDisk);
        menuSettings.add(miGotoMainClassOnStartup);
        menuSettings.add(miAutoRenameIdentifiers);

        miAssociate = new JCheckBoxMenuItem(translate("menu.settings.addtocontextmenu"));
        miAssociate.addActionListener(this::associate);
        miAssociate.setSelected(ContextMenuTools.isAddedToContextMenu());

        JMenuItem miLanguage = new JMenuItem(translate("menu.settings.language"));
        miLanguage.addActionListener(this::setLanguage);

        if (Platform.isWindows()) {
            menuSettings.add(miAssociate);
        }
        menuSettings.add(miLanguage);

        JMenuItem advancedSettingsCommandButton = new JMenuItem(translate("menu.advancedsettings.advancedsettings"));
        advancedSettingsCommandButton.setIcon(View.getIcon("settings16"));
        advancedSettingsCommandButton.addActionListener(this::advancedSettings);
        menuSettings.add(advancedSettingsCommandButton);

        menuBar.add(menuSettings);
        JMenu menuHelp = new JMenu(translate("menu.help"));
        JMenuItem miAbout = new JMenuItem(translate("menu.help.about"));
        miAbout.setIcon(View.getIcon("about16"));
        miAbout.addActionListener(this::about);

        JMenuItem miCheckUpdates = new JMenuItem(translate("menu.help.checkupdates"));
        miCheckUpdates.setIcon(View.getIcon("update16"));
        miCheckUpdates.addActionListener(this::checkUpdates);

        JMenuItem miHelpUs = new JMenuItem(translate("menu.help.helpus"));
        miHelpUs.setIcon(View.getIcon("donate16"));
        miHelpUs.addActionListener(this::helpUs);

        JMenuItem miHomepage = new JMenuItem(translate("menu.help.homepage"));
        miHomepage.setIcon(View.getIcon("homepage16"));
        miHomepage.addActionListener(this::homePage);

        menuHelp.add(miCheckUpdates);
        menuHelp.add(miHelpUs);
        menuHelp.add(miHomepage);
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);

        mainFrame.setJMenuBar(menuBar);

        //if (hasAbc) {
        menuTools.add(miGotoDocumentClass);
        //}
    }

    @Override
    public void updateComponents(SWF swf) {
        super.updateComponents(swf);
        boolean swfLoaded = swf != null;
        List<ABCContainerTag> abcList = swfLoaded ? swf.getAbcList() : null;
        boolean hasAbc = swfLoaded && abcList != null && !abcList.isEmpty();

        /*saveCommandButton.setEnabled(swfLoaded);
         saveasCommandButton.setEnabled(swfLoaded);
         saveasexeCommandButton.setEnabled(swfLoaded);
         exportAllCommandButton.setEnabled(swfLoaded);
         exportFlaCommandButton.setEnabled(swfLoaded);
         exportSelectionCommandButton.setEnabled(swfLoaded);
         reloadCommandButton.setEnabled(swfLoaded);

         renameInvalidCommandButton.setEnabled(swfLoaded);
         globalRenameCommandButton.setEnabled(swfLoaded);
         deobfuscationCommandButton.setEnabled(swfLoaded);
         searchCommandButton.setEnabled(swfLoaded);

         gotoDocumentClassCommandButton.setEnabled(hasAbc);
         deobfuscationCommandButton.setEnabled(hasAbc);*/
    }
}
