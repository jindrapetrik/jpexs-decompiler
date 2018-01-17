/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.helpers.CancellableWorker;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public class MainFrameStatusPanel extends JPanel {

    private final MainPanel mainPanel;

    private final LoadingPanel loadingPanel = new LoadingPanel(20, 20);

    private final JLabel statusLabel = new JLabel("");

    private final JButton cancelButton = new JButton();

    private JButton errorNotificationButton;

    private Icon currentIcon;

    private Timer blinkTimer;

    private int blinkPos;

    private CancellableWorker currentWorker;

    public MainFrameStatusPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        createStatusPanel();
    }

    private void createStatusPanel() {
        JPanel statusLeftPanel = new JPanel();
        statusLeftPanel.setLayout(new BoxLayout(statusLeftPanel, BoxLayout.X_AXIS));
        loadingPanel.setPreferredSize(new Dimension(30, 30));
        // todo: this button is a little bit ugly in the UI. Maybe it can be changed to an icon (as in NetBeans)
        cancelButton.setText(translate("button.cancel"));
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(false);
        cancelButton.addActionListener((ActionEvent e) -> {
            if (currentWorker != null) {
                currentWorker.cancel(true);
            }
        });
        statusLeftPanel.add(loadingPanel);
        statusLeftPanel.add(cancelButton);
        statusLeftPanel.add(statusLabel);
        setPreferredSize(new Dimension(1, 30));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new BorderLayout());
        add(statusLeftPanel, BorderLayout.WEST);

        errorNotificationButton = new JButton("");
        errorNotificationButton.setIcon(View.getIcon("okay16"));
        errorNotificationButton.setBorderPainted(false);
        errorNotificationButton.setFocusPainted(false);
        errorNotificationButton.setContentAreaFilled(false);
        errorNotificationButton.setMargin(new Insets(2, 2, 2, 2));
        errorNotificationButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        errorNotificationButton.addActionListener(this::showErrorLogButtonActionPerformed);
        errorNotificationButton.setToolTipText(translate("errors.none"));
        add(errorNotificationButton, BorderLayout.EAST);

        loadingPanel.setVisible(false);
        cancelButton.setVisible(false);
    }

    private void showErrorLogButtonActionPerformed(ActionEvent evt) {
        Main.displayErrorFrame();
    }

    private String translate(String key) {
        return mainPanel.translate(key);
    }

    public void setStatus(String s) {
        statusLabel.setText(s);
    }

    public void setWorkStatus(String s, CancellableWorker worker) {
        if (s.isEmpty()) {
            loadingPanel.setVisible(false);
        } else {
            loadingPanel.setVisible(true);
        }
        statusLabel.setText(s);
        currentWorker = worker;
        cancelButton.setVisible(worker != null);
    }

    public void setErrorState(ErrorState errorState) {
        switch (errorState) {
            case NO_ERROR:
                currentIcon = View.getIcon("okay16");
                errorNotificationButton.setToolTipText(translate("errors.none"));
                blinkPos = 0;
                break;
            case INFO:
                currentIcon = View.getIcon("information16");
                errorNotificationButton.setToolTipText(translate("errors.info"));
                break;
            case WARNING:
                currentIcon = View.getIcon("warning16");
                errorNotificationButton.setToolTipText(translate("errors.warning"));
                break;
            case ERROR:
                currentIcon = View.getIcon("error16");
                errorNotificationButton.setToolTipText(translate("errors.present"));
                break;
        }

        errorNotificationButton.setIcon(currentIcon);

        if (errorState != ErrorState.NO_ERROR) {
            if (blinkTimer != null) {
                blinkTimer.cancel();
            }
            blinkTimer = new Timer();
            blinkTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    View.execInEventDispatch(() -> {
                        blinkPos++;
                        if ((blinkPos % 2) == 0 || (blinkPos >= 4)) {
                            errorNotificationButton.setIcon(currentIcon);
                        } else {
                            errorNotificationButton.setIcon(null);
                            errorNotificationButton.setSize(16, 16);
                        }
                    });

                    if (blinkPos >= 4) {
                        cancel();
                    }
                }
            }, 500, 500);
        }
    }
}
