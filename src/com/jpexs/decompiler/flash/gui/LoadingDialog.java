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

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * Dialog showing loading of SWF file
 *
 * @author JPEXS
 */
public class LoadingDialog extends AppDialog {

    private final JLabel detailLabel;

    private CancellableWorker<?> worker;

    private final JProgressBar progressBar = new JProgressBar(0, 100);

    public void setWroker(CancellableWorker<?> worker) {
        this.worker = worker;
    }

    public void setDetail(String d) {
        detailLabel.setText(d);
        detailLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void setPercent(final int percent) {
        View.execInEventDispatch(() -> {
            if (percent == -1) {
                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(false);
            } else {
                progressBar.setIndeterminate(false);
                progressBar.setValue(percent);
                progressBar.setStringPainted(true);
            }
        });
    }

    /**
     * Constructor
     *
     */
    public LoadingDialog() {
        super();
        setResizable(false);
        setTitle(ApplicationInfo.shortApplicationVerName);
        Container cntp = getContentPane();
        JPanel cnt = new JPanel();
        cntp.setLayout(new BorderLayout());
        cntp.add(cnt);
        cnt.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cnt.setLayout(new BorderLayout());

        JPanel pan = new JPanel();
        pan.setLayout(new ListLayout(5));
        JLabel loadingLabel = new JLabel(translate("loadingpleasewait"));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailLabel = new JLabel("", JLabel.CENTER);
        detailLabel.setPreferredSize(new Dimension(loadingLabel.getPreferredSize()));
        detailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pan.add(loadingLabel);
        pan.add(detailLabel);
        pan.add(progressBar);
        cnt.add(pan, BorderLayout.CENTER);
        progressBar.setStringPainted(true);
        View.setWindowIcon(this);
        detailLabel.setHorizontalAlignment(SwingConstants.LEFT);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.shouldCloseWhenClosingLoadingDialog) {
                    System.exit(0);
                } else if (worker != null) {
                    worker.cancel(true);
                }
            }
        });
        pack();
        Dimension siz = getSize();
        setSize(Math.max(300, 150 + getFontMetrics(new JLabel().getFont()).stringWidth(translate("loadingpleasewait"))), siz.height);
        View.centerScreen(this);
    }

    @Override
    public void dispose() {
        removeAll();
        for (WindowListener windowListener : getWindowListeners()) {
            removeWindowListener(windowListener);
        }

        Helper.emptyObject(this);
        super.dispose();
    }
}
