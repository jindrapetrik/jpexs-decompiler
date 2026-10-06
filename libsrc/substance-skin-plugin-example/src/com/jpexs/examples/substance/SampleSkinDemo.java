/*
 * Copyright (C) 2010-2026 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.jpexs.examples.substance;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SkinInfo;

/**
 * Visual preview and command-line smoke test for the example plugin.
 */
public final class SampleSkinDemo {

    private SampleSkinDemo() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "--verify".equals(args[0])) {
            verifyPlugin();
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showDemo();
            }
        });
    }

    private static void verifyPlugin() {
        SampleBlueSkin skin = new SampleBlueSkin();
        if (!skin.isValid()) {
            throw new IllegalStateException("Sample Blue skin is not valid");
        }

        Map<String, SkinInfo> skins = SubstanceLookAndFeel.getAllSkins();
        SkinInfo discovered = skins.get(SampleBlueSkin.NAME);
        if (discovered == null) {
            throw new IllegalStateException(
                    "Substance did not discover META-INF/substance-plugin.xml");
        }
        System.out.println("Verified Substance skin plugin: "
                + SampleBlueSkin.NAME + " (" + skins.size()
                + " skins available)");
    }

    private static void showDemo() {
        try {
            UIManager.setLookAndFeel(new SampleBlueLookAndFeel());
            JFrame.setDefaultLookAndFeelDecorated(true);

            JFrame frame = new JFrame("Substance 6.2 - Sample Blue skin");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(8, 8));

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING));
            JButton disabledButton = new JButton("Disabled");
            disabledButton.setEnabled(false);
            controls.add(new JButton("Primary action"));
            controls.add(disabledButton);
            controls.add(new JCheckBox("Selected", true));
            controls.add(new JComboBox<String>(new String[]{"Alpha", "Beta", "Gamma"}));
            controls.add(new JTextField("Editable text", 14));

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Controls", controls);
            tabs.addTab("Table", new JScrollPane(new JTable(
                    new Object[][]{{"Plugin", "Loaded"}, {"Skin", SampleBlueSkin.NAME}},
                    new Object[]{"Item", "Value"})));

            JPanel status = new JPanel(new BorderLayout(8, 0));
            JProgressBar progress = new JProgressBar(0, 100);
            progress.setValue(65);
            status.add(new JLabel("Substance 6.2 plugin preview"), BorderLayout.WEST);
            status.add(new JSlider(0, 100, 65), BorderLayout.CENTER);
            status.add(progress, BorderLayout.EAST);

            frame.add(tabs, BorderLayout.CENTER);
            frame.add(status, BorderLayout.SOUTH);
            frame.setPreferredSize(new Dimension(720, 360));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot start the skin preview", ex);
        }
    }
}
