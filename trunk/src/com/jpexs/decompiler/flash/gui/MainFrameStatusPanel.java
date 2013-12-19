/*
 *  Copyright (C) 2010-2013 JPEXS
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
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public class MainFrameStatusPanel extends JPanel implements ActionListener {
    
    static final String ACTION_SHOW_ERROR_LOG = "SHOWERRORLOG";

    private MainFrame mainFrame;
    
    private LoadingPanel loadingPanel = new LoadingPanel(20, 20);
    private JLabel statusLabel = new JLabel("");
    private JButton cancelButton = new JButton();
    private JButton errorNotificationButton;

    private Timer blinkTimer;
    private int blinkPos;

    private CancellableWorker currentWorker;

    public MainFrameStatusPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
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
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentWorker != null) {
                    currentWorker.cancel(true);
                }
            }
        });
        statusLeftPanel.add(loadingPanel);
        statusLeftPanel.add(statusLabel);
        statusLeftPanel.add(cancelButton);
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
        errorNotificationButton.setActionCommand(ACTION_SHOW_ERROR_LOG);
        errorNotificationButton.addActionListener(this);
        errorNotificationButton.setToolTipText(translate("errors.none"));
        add(errorNotificationButton, BorderLayout.EAST);

        loadingPanel.setVisible(false);
        cancelButton.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_SHOW_ERROR_LOG:
                Main.displayErrorFrame();
                break;
        }
    }
    
    public String translate(String key) {
        return mainFrame.translate(key);
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

    public void clearErrorState() {
        errorNotificationButton.setIcon(View.getIcon("okay16"));
        errorNotificationButton.setToolTipText(translate("errors.none"));
    }

    public void setErrorState() {
        if (errorNotificationButton == null) {
            // todo: honfika
            // why null?
            return;
        }
        errorNotificationButton.setIcon(View.getIcon("error16"));
        errorNotificationButton.setToolTipText(translate("errors.present"));
        if (blinkTimer != null) {
            blinkTimer.cancel();
        }
        blinkTimer = new Timer();
        blinkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                View.execInEventDispatch(new Runnable() {
                    @Override
                    public void run() {
                        blinkPos++;
                        if ((blinkPos % 2) == 0 || (blinkPos >= 4)) {
                            errorNotificationButton.setIcon(View.getIcon("error16"));
                        } else {
                            errorNotificationButton.setIcon(null);
                            errorNotificationButton.setSize(16, 16);
                        }
                    }
                });

                if (blinkPos >= 4) {
                    cancel();
                }
            }
        }, 500, 500);
    }

}
