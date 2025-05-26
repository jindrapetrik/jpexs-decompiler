/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.colordialog;

import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 *
 * @author JPEXS
 */
public class MyColorChooserDialog extends JDialog {

    private JColorChooser chooserPane;
    private MyRecentSwatchPanel recentSwatchPanel;

    private Color color = null;

    public MyColorChooserDialog(Component parentComponent, Color initialColor, boolean colorTransparencySelectionEnabled) {
        setTitle(AppStrings.translate("com.jpexs.decompiler.flash.gui.locales.ColorChooserDialog", "dialog.title"));
        JColorChooser chooser = new JColorChooser(initialColor);
        List<AbstractColorChooserPanel> choosers = new ArrayList<>(Arrays.asList(chooser.getChooserPanels()));
        choosers.set(0, new MySwatchChooserPanel());

        if (!colorTransparencySelectionEnabled) {
            if (Helper.getJavaVersion() >= 9) {
                for (AbstractColorChooserPanel ccPanel : choosers) {
                    Method m;
                    try {
                        m = AbstractColorChooserPanel.class.getDeclaredMethod("setColorTransparencySelectionEnabled", boolean.class);
                        m.invoke(ccPanel, colorTransparencySelectionEnabled);
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
                        //ignore
                    }
                }
            } else {
                for (int i = 1; i < choosers.size(); i++) {
                    AbstractColorChooserPanel cp = choosers.get(i);

                    Field f;
                    try {
                        f = cp.getClass().getDeclaredField("panel");

                        f.setAccessible(true);

                        Object colorPanel = f.get(cp);
                        Field f2 = colorPanel.getClass().getDeclaredField("spinners");
                        f2.setAccessible(true);
                        Object spinners = f2.get(colorPanel);

                        Object transpSlispinner = Array.get(spinners, 3);
                        if (i == choosers.size() - 1) {
                            transpSlispinner = Array.get(spinners, 4);
                        }
                        Field f3 = transpSlispinner.getClass().getDeclaredField("slider");
                        f3.setAccessible(true);
                        JSlider slider = (JSlider) f3.get(transpSlispinner);
                        slider.setEnabled(false);
                        Field f4 = transpSlispinner.getClass().getDeclaredField("spinner");
                        f4.setAccessible(true);
                        JSpinner spinner = (JSpinner) f4.get(transpSlispinner);
                        spinner.setEnabled(false);
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                        //ignore
                    }
                }
            }
        }

        chooser.setChooserPanels(choosers.toArray(new AbstractColorChooserPanel[0]));

        this.chooserPane = chooser;
        Locale locale = getLocale();
        String okString = UIManager.getString("ColorChooser.okText", locale);
        String cancelString = UIManager.getString("ColorChooser.cancelText", locale);
        String resetString = UIManager.getString("ColorChooser.resetText", locale);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane, BorderLayout.CENTER);

        recentSwatchPanel = new MyRecentSwatchPanel();
        RecentSwatchListener recentSwatchListener = new RecentSwatchListener();
        RecentSwatchKeyListener recentSwatchKeyListener = new RecentSwatchKeyListener();
        recentSwatchPanel.addMouseListener(recentSwatchListener);
        recentSwatchPanel.addKeyListener(recentSwatchKeyListener);

        JLabel recentLabel = new JLabel(UIManager.getString("ColorChooser.swatchesRecentText", getLocale()));

        JButton recentAddButton = new JButton(AppStrings.translate("com.jpexs.decompiler.flash.gui.locales.ColorChooserDialog", "recent.add"));
        recentAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recentSwatchPanel.setMostRecentColor(chooserPane.getColor());
            }
        });

        recentSwatchPanel.setMaximumSize(recentSwatchPanel.getPreferredSize());

        recentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        recentSwatchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        recentAddButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel recentPanel = new JPanel();
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS));
        recentPanel.add(Box.createVerticalStrut(20));
        recentPanel.add(recentLabel);
        recentPanel.add(recentSwatchPanel);
        recentPanel.add(Box.createVerticalStrut(5));
        recentPanel.add(recentAddButton);
        recentPanel.add(Box.createVerticalGlue());

        recentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        contentPane.add(recentPanel, BorderLayout.EAST);

        /*
         * Create Lower button panel
         */
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(okString);
        getRootPane().setDefaultButton(okButton);
        okButton.getAccessibleContext().setAccessibleDescription(okString);
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener() {
            @SuppressWarnings("deprecation")
            public void actionPerformed(ActionEvent e) {
                color = chooserPane.getColor();
                recentSwatchPanel.setMostRecentColor(color);
                setVisible(false);
            }
        });
        buttonPane.add(okButton);

        JButton cancelButton = new JButton(cancelString);
        cancelButton.getAccessibleContext().setAccessibleDescription(cancelString);

        Action cancelKeyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        InputMap inputMap = cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = cancelButton.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
        }

        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(new ActionListener() {
            @SuppressWarnings("deprecation")
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.getAccessibleContext().setAccessibleDescription(resetString);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooserPane.setColor(initialColor);
            }
        });
        Object mnemonic = UIManager.get("ColorChooser.resetMnemonic", locale);
        if (mnemonic instanceof Integer) {
            resetButton.setMnemonic((int) mnemonic);
        }
        buttonPane.add(resetButton);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations
                    = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
            }
        }
        applyComponentOrientation(((parentComponent == null) ? getRootPane() : parentComponent).getComponentOrientation());

        pack();
        setLocationRelativeTo(parentComponent);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setModal(true);
    }

    public Color getColor() {
        return color;
    }

    private class RecentSwatchKeyListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            if (KeyEvent.VK_SPACE == e.getKeyCode()) {
                Color color = recentSwatchPanel.getSelectedColor();
                chooserPane.setColor(color);
                //setSelectedColor(color);
            }
        }
    }

    class RecentSwatchListener extends MouseAdapter implements Serializable {

        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                Color color = recentSwatchPanel.getColorForLocation(e.getX(), e.getY());
                recentSwatchPanel.setSelectedColorFromLocation(e.getX(), e.getY());
                //setSelectedColor(color);
                chooserPane.setColor(color);
                recentSwatchPanel.requestFocusInWindow();
            }
        }
    }
}
