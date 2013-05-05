/*
 * Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.Main;
import static com.jpexs.decompiler.flash.Main.projectPage;
import static com.jpexs.decompiler.flash.Main.shortApplicationName;
import com.jpexs.decompiler.flash.Version;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 * @author JPEXS
 */
public class NewVersionDialog extends JDialog implements ActionListener {

    Version latestVersion;    
    
    public NewVersionDialog(List<Version> versions) {
        setSize(new Dimension(500, 300));
        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.PAGE_AXIS));
        JTextArea changesText = new JTextArea();
        changesText.setEditable(false);
        changesText.setFont(UIManager.getFont("TextField.font"));
        String changesStr = "";
        for (Version v : versions) {
            changesStr += "version " + v.versionName + "\r\n";
            changesStr += "-----------------------\r\n";
            changesStr += "Release date:" + v.releaseDate + "\r\n";
            for (String type : v.changes.keySet()) {
                changesStr += type + ":" + "\r\n";
                for (String ch : v.changes.get(type)) {
                    changesStr += " - " + ch + "\r\n";
                }
            }
            changesStr += "\r\n";
        }
        latestVersion = null;
        if (!versions.isEmpty()) {
            latestVersion = versions.get(0);
        }
        changesText.setText(changesStr);
        JLabel newAvailableLabel = new JLabel("<html><b><center>New version is available: " + latestVersion.appName + " version " + latestVersion.versionName + "</center></b></html>", SwingConstants.CENTER);
        newAvailableLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cnt.add(newAvailableLabel);

        JLabel changeslogLabel = new JLabel("<html>Changeslog:</html>");
        changeslogLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cnt.add(changeslogLabel);

        JScrollPane span = new JScrollPane(changesText);
        span.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cnt.add(span);
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton buttonOk = new JButton("OK");
        buttonOk.setActionCommand("OK");
        buttonOk.addActionListener(this);

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.setActionCommand("CANCEL");
        buttonCancel.addActionListener(this);

        buttonsPanel.add(buttonOk);
        buttonsPanel.add(buttonCancel);
        buttonsPanel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel downloadNowLabel = new JLabel("<html><b><center>Download now?</center></b></html>", SwingConstants.CENTER);
        downloadNowLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cnt.add(downloadNowLabel);
        cnt.add(buttonsPanel);

        setResizable(false);
        setTitle("New version available");
        this.getRootPane().setDefaultButton(buttonOk);
        View.centerScreen(this);
        setModalityType(ModalityType.APPLICATION_MODAL);
        changesText.setCaretPosition(0);    
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")) {
            java.awt.Desktop desktop = null;
            if (java.awt.Desktop.isDesktopSupported()) {
                desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    try {
                        if (latestVersion.updateLink != null) {
                            java.net.URI uri = new java.net.URI(latestVersion.updateLink);
                            desktop.browse(uri);
                        } else {
                            java.net.URI uri = new java.net.URI(Main.updatePage);
                            desktop.browse(uri);
                        }
                        Main.exit();
                    } catch (Exception ex) {
                    }
                } else {
                    desktop = null;
                }
            }
            if (desktop == null) {
                JOptionPane.showMessageDialog(null, "New version of " + shortApplicationName + " is available: " + latestVersion.appName + ".\r\nPlease go to " + projectPage + " to download it.", "New version", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        setVisible(false);
    }
}
