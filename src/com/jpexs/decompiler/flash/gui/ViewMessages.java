/*
 * Copyright (C) 2021 JPEXS
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

import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class ViewMessages {
    public static int showOptionDialog(final Component parentComponent, final Object message, final String title, final int optionType, final int messageType, final Icon icon, final Object[] options, final Object initialValue) {
        final int[] ret = new int[1];
        View.execInEventDispatch(() -> {
            ret[0] = JOptionPane.showOptionDialog(parentComponent, message, title, optionType, messageType, icon, options, initialValue);
        });
        return ret[0];
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType) {
        return showConfirmDialog(parentComponent, message, title, optionType, JOptionPane.PLAIN_MESSAGE);
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title, final int optionType, final int messageTyp) {
        final int[] ret = new int[1];
        View.execInEventDispatch(() -> {
            ret[0] = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageTyp);
        });
        return ret[0];
    }

    public static int showConfirmDialog(Component parentComponent, String message, String title, int optionType, ConfigurationItem<Boolean> showAgainConfig, int defaultOption) {
        return showConfirmDialog(parentComponent, message, title, optionType, JOptionPane.PLAIN_MESSAGE, showAgainConfig, defaultOption);
    }

    public static int showConfirmDialog(final Component parentComponent, String message, final String title, final int optionType, final int messageType, ConfigurationItem<Boolean> showAgainConfig, int defaultOption) {

        JCheckBox donotShowAgainCheckBox = null;
        JPanel warPanel = null;
        if (showAgainConfig != null) {
            if (!showAgainConfig.get()) {
                return defaultOption;
            }

            JLabel warLabel = new JLabel("<html>" + message.replace("\r\n", "<br>") + "</html>");
            warPanel = new JPanel(new BorderLayout());
            warPanel.add(warLabel, BorderLayout.CENTER);
            donotShowAgainCheckBox = new JCheckBox(AppStrings.translate("message.confirm.donotshowagain"));
            warPanel.add(donotShowAgainCheckBox, BorderLayout.SOUTH);
        }

        final int[] ret = new int[1];
        final Object messageObj = warPanel == null ? message : warPanel;
        View.execInEventDispatch(() -> {
            ret[0] = JOptionPane.showConfirmDialog(parentComponent, messageObj, title, optionType, messageType);
        });

        if (donotShowAgainCheckBox != null) {
            showAgainConfig.set(!donotShowAgainCheckBox.isSelected());
        }

        return ret[0];
    }

    public static void showMessageDialog(final Component parentComponent, final String message, final String title, final int messageType) {
        showMessageDialog(parentComponent, message, title, messageType, null);
    }

    public static void showMessageDialog(final Component parentComponent, final String message, final String title, final int messageType, ConfigurationItem<Boolean> showAgainConfig) {

        View.execInEventDispatch(() -> {
            Object msg = message;
            JCheckBox donotShowAgainCheckBox = null;
            if (showAgainConfig != null) {
                if (!showAgainConfig.get()) {
                    return;
                }

                JLabel warLabel = new JLabel("<html>" + message.replace("\r\n", "<br>") + "</html>");
                final JPanel warPanel = new JPanel(new BorderLayout());
                warPanel.add(warLabel, BorderLayout.CENTER);
                donotShowAgainCheckBox = new JCheckBox(AppStrings.translate("message.confirm.donotshowagain"));
                warPanel.add(donotShowAgainCheckBox, BorderLayout.SOUTH);
                msg = warPanel;
            }
            final Object fmsg = msg;

            JOptionPane.showMessageDialog(parentComponent, fmsg, title, messageType);
            if (donotShowAgainCheckBox != null) {
                showAgainConfig.set(!donotShowAgainCheckBox.isSelected());
            }
        });
    }

    public static void showMessageDialog(final Component parentComponent, final Object message) {
        View.execInEventDispatch(() -> {
            JOptionPane.showMessageDialog(parentComponent, message);
        });
    }

    public static String showInputDialog(final Component parentComponent, final Object message, final Object initialSelection) {
        final String[] ret = new String[1];
        View.execInEventDispatch(() -> {
            ret[0] = JOptionPane.showInputDialog(parentComponent, message, initialSelection);
        });
        return ret[0];
    }

    public static String showInputDialog(Component parentComponent, final Object message, final String title, final Object initialSelection) {
        final String[] ret = new String[1];
        View.execInEventDispatch(() -> {
            ret[0] = (String) JOptionPane.showInputDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE, null, null, initialSelection);
        });
        return ret[0];
    }
}
