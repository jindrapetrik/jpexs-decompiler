package com.jpexs.asdec.gui;

import com.jpexs.asdec.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;

/**
 * Dialog showing loading of SWF file
 *
 * @author JPEXS
 */
public class LoadingDialog extends JFrame implements ImageObserver {
    private JLabel loadingLabel = new JLabel("Loading SWF file, please wait...");
    private LoadingPanel loadingPanel;

    /**
     * Constructor
     */
    public LoadingDialog() {
        setResizable(false);
        setTitle(Main.applicationName);
        setSize(300, 150);
        setLayout(new BorderLayout());

        loadingPanel = new LoadingPanel(50, 50);
        loadingPanel.setPreferredSize(new Dimension(100, 100));
        add(loadingPanel, BorderLayout.WEST);
        add(loadingLabel, BorderLayout.CENTER);
        View.centerScreen(this);
        View.setWindowIcon(this);
        loadingLabel.setHorizontalAlignment(SwingConstants.LEFT);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }


}
