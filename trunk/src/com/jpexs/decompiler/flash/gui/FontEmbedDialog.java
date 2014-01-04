/*
 *  Copyright (C) 2013 JPEXS
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

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.font.CharacterRanges;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author JPEXS
 */
public class FontEmbedDialog extends AppDialog implements ActionListener {

    private static final String ACTION_OK = "OK";
    private static final String ACTION_CANCEL = "CANCEL";
    
    private static final int SAMPLE_MAX_LENGTH = 50;
    
    private JComboBox<String> sourceFont;
    private JCheckBox[] rangeCheckboxes;
    private String rangeNames[];
    private JLabel[] rangeSamples;
    private JTextField individualCharsField;
    private boolean result = false;
    private JLabel individialSample;
    private int style;

    public String getSelectedFont() {
        return sourceFont.getSelectedItem().toString();
    }

    public Set<Integer> getSelectedChars() {
        Set<Integer> chars = new TreeSet<>();
        Font f = new Font(getSelectedFont(), style, new JLabel().getFont().getSize());
        for (int i = 0; i < rangeCheckboxes.length; i++) {
            if (rangeCheckboxes[i].isSelected()) {
                int codes[] = CharacterRanges.rangeCodes(i);
                for (int c : codes) {
                    if (f.canDisplay(c)) {
                        chars.add(c);
                    }
                }
            }
        }
        String indStr = individualCharsField.getText();
        for (int i = 0; i < indStr.length(); i++) {
            if (f.canDisplay(indStr.codePointAt(i))) {
                chars.add(indStr.codePointAt(i));
            }
        }
        return chars;
    }

    @SuppressWarnings("unchecked")
    public FontEmbedDialog(String selectedFont, String selectedChars, int style) {
        setSize(900, 600);
        this.style = style;
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));

        individialSample = new JLabel();
        sourceFont = new JComboBox<>(new Vector(FontTag.fontNames));
        sourceFont.setSelectedItem(selectedFont);
        cnt.add(sourceFont);
        JPanel rangesPanel = new JPanel();
        rangesPanel.setLayout(new BoxLayout(rangesPanel, BoxLayout.Y_AXIS));
        int rc = CharacterRanges.rangeCount();
        rangeCheckboxes = new JCheckBox[rc];
        rangeSamples = new JLabel[rc];
        rangeNames = new String[rc];
        for (int i = 0; i < rc; i++) {
            rangeNames[i] = CharacterRanges.rangeName(i);
            rangeSamples[i] = new JLabel("");
            rangeCheckboxes[i] = new JCheckBox(rangeNames[i]);
            JPanel rangeRowPanel = new JPanel();
            rangeRowPanel.setLayout(new BoxLayout(rangeRowPanel, BoxLayout.X_AXIS));
            rangeRowPanel.add(rangeCheckboxes[i]);
            rangeRowPanel.add(Box.createHorizontalGlue());
            rangeRowPanel.add(rangeSamples[i]);
            rangeRowPanel.setAlignmentX(0);
            rangesPanel.add(rangeRowPanel);
        }
        cnt.add(new JScrollPane(rangesPanel));


        JPanel specialPanel = new JPanel();
        specialPanel.setLayout(new BoxLayout(specialPanel, BoxLayout.X_AXIS));
        specialPanel.add(new JLabel(translate("label.individual")));
        individualCharsField = new JTextField();
        individualCharsField.setPreferredSize(new Dimension(100, individualCharsField.getPreferredSize().height));
        individialSample = new JLabel();
        specialPanel.add(individualCharsField);
        cnt.add(specialPanel);
        cnt.add(individialSample);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton(AppStrings.translate("button.ok"));
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        JButton cancelButton = new JButton(AppStrings.translate("button.cancel"));
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        cnt.add(buttonsPanel);
        View.setWindowIcon(this);
        View.centerScreen(this);
        setModalityType(ModalityType.APPLICATION_MODAL);
        individualCharsField.setText(selectedChars);
        getRootPane().setDefaultButton(okButton);
        sourceFont.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateCheckboxes();
            }
        });
        updateCheckboxes();
        individualCharsField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                updateIndividual();
            }
        });
    }

    private void updateIndividual() {
        String chars = individualCharsField.getText();
        Font f = new Font(getSelectedFont(), style, new JLabel().getFont().getSize());
        String visibleChars = "";
        for (int i = 0; i < chars.length(); i++) {
            if (f.canDisplay(chars.codePointAt(i))) {
                visibleChars += "" + chars.charAt(i);
            }
        }
        individialSample.setText(visibleChars);
    }

    private void updateCheckboxes() {
        String fontStr = sourceFont.getSelectedItem().toString();
        Font f = new Font(fontStr, style, new JLabel().getFont().getSize());
        int rc = CharacterRanges.rangeCount();
        for (int i = 0; i < rc; i++) {
            rangeNames[i] = CharacterRanges.rangeName(i);
            int codes[] = CharacterRanges.rangeCodes(i);
            int avail = 0;
            String sample = "";
            for (int c = 0; c < codes.length; c++) {
                if (f.canDisplay(codes[c])) {
                    if (avail < SAMPLE_MAX_LENGTH) {
                        sample += "" + (char) codes[c];
                        avail++;
                    }
                }
            }
            rangeSamples[i].setText(sample);
            rangeSamples[i].setFont(f);
            rangeCheckboxes[i].setText(translate("range.description").replace("%available%", "" + avail).replace("%name%", rangeNames[i]).replace("%total%", "" + codes.length));
        }
        individialSample.setFont(f);
        updateIndividual();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_OK:
                result = true;
                setVisible(false);
                break;
            case ACTION_CANCEL:
                result = false;
                setVisible(false);
                break;
        }
    }

    public boolean display() {
        result = false;
        setVisible(true);
        return result;
    }
}
