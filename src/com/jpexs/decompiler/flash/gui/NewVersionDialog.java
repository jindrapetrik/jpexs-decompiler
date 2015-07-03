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
import com.jpexs.decompiler.flash.Version;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 * @author JPEXS
 */
public class NewVersionDialog extends AppDialog {

    private Version latestVersion;

    public NewVersionDialog(List<Version> versions) {
        setSize(new Dimension(500, 300));
        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.PAGE_AXIS));
        JEditorPane changesText = new JEditorPane();
        changesText.setEditable(false);
        changesText.setFont(UIManager.getFont("TextField.font"));
        SimpleDateFormat serverFormatter = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat formatter;
        String customFormat = translate("customDateFormat");
        if (customFormat.equals("default")) {
            formatter = DateFormat.getDateInstance();
        } else {
            formatter = new SimpleDateFormat(customFormat);
        }

        StringBuilder changesStr = new StringBuilder();
        changesStr.append("<html>");

        boolean first = true;
        for (Version v : versions) {
            if (!first) {
                changesStr.append("<hr />");
            }
            first = false;
            changesStr.append("<b>").append(translate("version")).append(" ").append(v.versionName).append("</b><br />");
            String releaseDate = v.releaseDate;
            try {
                Date date = serverFormatter.parse(releaseDate);
                releaseDate = formatter.format(date);
            } catch (ParseException ex) {
                Logger.getLogger(NewVersionDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
            changesStr.append(translate("releasedate")).append(" ").append(releaseDate);
            if (!v.changes.isEmpty()) {
                changesStr.append("<br />");
                changesStr.append("<pre>");
                for (String type : v.changes.keySet()) {
                    changesStr.append(type).append(":" + "<br />");
                    for (String ch : v.changes.get(type)) {
                        changesStr.append(" - ").append(ch).append("<br />");
                    }
                }
                changesStr.append("</pre>");
            }
        }

        changesStr.append("</html>");
        latestVersion = null;
        if (!versions.isEmpty()) {
            latestVersion = versions.get(0);
        }
        changesText.setContentType("text/html");
        changesText.setText(changesStr.toString());
        if (latestVersion != null) {
            JLabel newAvailableLabel = new JLabel("<html><b><center>" + translate("newversionavailable") + " " + latestVersion.appName + " " + translate("version") + " " + latestVersion.versionName + "</center></b></html>", SwingConstants.CENTER);
            newAvailableLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            cnt.add(newAvailableLabel);
        }

        JLabel changeslogLabel = new JLabel("<html>" + translate("changeslog") + "</html>");
        changeslogLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cnt.add(changeslogLabel);

        JScrollPane span = new JScrollPane(changesText);
        span.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cnt.add(span);
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton buttonOk = new JButton(translate("button.ok"));
        buttonOk.addActionListener(this::okButtonActionPerformed);

        JButton buttonCancel = new JButton(translate("button.cancel"));
        buttonCancel.addActionListener(this::cancelButtonActionPerformed);

        buttonsPanel.add(buttonOk);
        buttonsPanel.add(buttonCancel);
        buttonsPanel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel downloadNowLabel = new JLabel("<html><b><center>" + translate("downloadnow") + "</center></b></html>", SwingConstants.CENTER);
        downloadNowLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        cnt.add(downloadNowLabel);
        cnt.add(buttonsPanel);

        setResizable(false);
        setTitle(translate("dialog.title"));
        this.getRootPane().setDefaultButton(buttonOk);
        View.centerScreen(this);
        setModalityType(ModalityType.APPLICATION_MODAL);
        changesText.setCaretPosition(0);
        View.setWindowIcon(this);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        String url;
        if (latestVersion.updateLink != null) {
            url = latestVersion.updateLink;
        } else {
            url = ApplicationInfo.updateUrl;
        }
        if (View.navigateUrl(url)) {
            Main.exit();
        } else {
            View.showMessageDialog(null, translate("newvermessage").replace("%oldAppName%", ApplicationInfo.SHORT_APPLICATION_NAME).replace("%newAppName%", latestVersion.appName).replace("%projectPage%", ApplicationInfo.PROJECT_PAGE), translate("newversion"), JOptionPane.INFORMATION_MESSAGE);
        }

        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }
}
