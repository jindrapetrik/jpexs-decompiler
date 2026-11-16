/*
 *  Copyright (C) 2026 JPEXS, All rights reserved.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultFormatter;

/**
 * Selects a font family, style, and point size with a live preview.
 */
class FontChooserDialog extends AppDialog {

    private final JList<String> families = new JList<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    private final JList<String> styles = new JList<>(new String[]{
        translate("style.plain"), translate("style.bold"), translate("style.italic"), translate("style.boldItalic")
    });
    private final JList<Integer> sizes = new JList<>(new Integer[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72});
    private final JSpinner sizeSpinner;
    private final JLabel preview = new JLabel(translate("preview.text"), JLabel.CENTER);
    private final JPanel previewPanel = new JPanel(new BorderLayout()) {
        @Override
        public Dimension getPreferredSize() {
            Dimension minimum = getMinimumSize();
            return new Dimension(minimum.width, Math.max(minimum.height, super.getPreferredSize().height));
        }
    };
    private Font selectedFont;

    FontChooserDialog(Window owner, Font initialFont) {
        super(owner);
        setTitle(translate("dialog.title"));
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        sizeSpinner = new JSpinner(new SpinnerNumberModel(Math.max(1, initialFont.getSize()), 1, Integer.MAX_VALUE, 1));
        JFormattedTextField sizeEditor = ((JSpinner.DefaultEditor) sizeSpinner.getEditor()).getTextField();
        sizeEditor.setColumns(4);
        sizeEditor.setFocusLostBehavior(JFormattedTextField.PERSIST);
        ((DefaultFormatter) sizeEditor.getFormatter()).setCommitsOnValidEdit(true);

        families.setSelectedValue(initialFont.getFamily(), false);
        if (families.isSelectionEmpty()) {
            families.setSelectedIndex(0);
        }
        styles.setSelectedIndex(initialFont.getStyle());
        sizes.setSelectedValue(sizeSpinner.getValue(), true);

        JPanel selectors = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(0, 0, 0, 8);
        selectors.add(createColumn("label.family", families, families, null), constraints);
        constraints.weightx = 0;
        selectors.add(createColumn("label.style", styles, styles, null), constraints);
        constraints.insets = new Insets(0, 0, 0, 0);
        selectors.add(createColumn("label.size", sizes, sizeEditor, sizeSpinner), constraints);

        previewPanel.add(preview, BorderLayout.CENTER);
        previewPanel.setBorder(BorderFactory.createTitledBorder(translate("preview.title")));
        previewPanel.setMinimumSize(new Dimension(60, 50));

        JButton okButton = new JButton(AppStrings.translate("button.ok"));
        okButton.addActionListener(e -> confirmSelection(sizeEditor));
        JButton cancelButton = new JButton(AppStrings.translate("button.cancel"));
        cancelButton.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        buttons.add(okButton);
        buttons.add(cancelButton);
        getRootPane().setDefaultButton(okButton);

        JPanel lowerPanel = new JPanel(new BorderLayout(0, 8));
        lowerPanel.add(previewPanel, BorderLayout.CENTER);
        lowerPanel.add(buttons, BorderLayout.SOUTH);
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(selectors, BorderLayout.CENTER);
        content.add(lowerPanel, BorderLayout.SOUTH);
        setContentPane(content);

        families.addListSelectionListener(e -> updatePreview());
        styles.addListSelectionListener(e -> updatePreview());
        sizes.addListSelectionListener(e -> {
            Integer size = sizes.getSelectedValue();
            if (size != null) {
                sizeSpinner.setValue(size);
            }
        });
        sizeSpinner.addChangeListener(e -> {
            sizes.setSelectedValue(sizeSpinner.getValue(), true);
            updatePreview();
        });

        updatePreview();
        pack();
        families.ensureIndexIsVisible(families.getSelectedIndex());
        setLocationRelativeTo(owner);
        View.setWindowIcon(this, "font");
    }

    private JPanel createColumn(String labelKey, JList<?> list, JComponent labelTarget, JComponent editor) {
        JLabel label = new JLabel(translate(labelKey));
        label.setLabelFor(labelTarget);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(8);
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(label, BorderLayout.NORTH);
        if (editor != null) {
            header.add(editor, BorderLayout.CENTER);
        }
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private Font getFontChoice() {
        return new Font(families.getSelectedValue(), styles.getSelectedIndex(), (Integer) sizeSpinner.getValue());
    }

    private void updatePreview() {
        int oldHeight = previewPanel.getPreferredSize().height;
        preview.setFont(getFontChoice());
        int heightChange = previewPanel.getPreferredSize().height - oldHeight;
        if (isDisplayable() && heightChange != 0) {
            setSize(getWidth(), getHeight() + heightChange);
        }
    }

    private void confirmSelection(JFormattedTextField sizeEditor) {
        try {
            sizeSpinner.commitEdit();
        } catch (ParseException ex) {
            sizeEditor.requestFocusInWindow();
            sizeEditor.selectAll();
            return;
        }
        selectedFont = getFontChoice();
        dispose();
    }

    Font getSelectedFont() {
        return selectedFont;
    }
}
