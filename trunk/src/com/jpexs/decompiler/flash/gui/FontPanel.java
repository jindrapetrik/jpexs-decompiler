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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author JPEXS
 */
public class FontPanel extends JPanel implements ActionListener {
    
    static final String ACTION_FONT_EMBED = "FONTEMBED";
    static final String ACTION_FONT_ADD_CHARS = "FONTADDCHARS";

    private MainPanel mainPanel;

    private JLabel fontNameLabel;
    private JLabel fontDisplayNameLabel;
    private JLabel fontCopyrightLabel;
    private JLabel fontIsBoldLabel;
    private JLabel fontIsItalicLabel;
    private JLabel fontAscentLabel;
    private JLabel fontDescentLabel;
    private JLabel fontLeadingLabel;
    private JTextArea fontCharactersTextArea;
    private JTextField fontAddCharactersField;
    private JCheckBox updateTextsCheckBox;
    private ComponentListener fontChangeList;
    private JComboBox<String> fontSelection;

    public FontPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        createFontPanel();
    }
    
    private void createFontPanel() {
        //TODO: This layout SUCKS! If you know something better, please fix it!
        final JPanel fontParams2 = new JPanel();
        fontParams2.setLayout(null);
        final Component[][] ctable = new Component[][]{
            {new JLabel(translate("font.name")), fontNameLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("fontName.name")), fontDisplayNameLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("fontName.copyright")), fontCopyrightLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.isbold")), fontIsBoldLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.isitalic")), fontIsItalicLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.ascent")), fontAscentLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.descent")), fontDescentLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.leading")), fontLeadingLabel = new JLabel(translate("value.unknown"))},
            {new JLabel(translate("font.characters")), fontCharactersTextArea = new JTextArea("")}
        };
        fontCharactersTextArea.setLineWrap(true);
        fontCharactersTextArea.setWrapStyleWord(true);
        fontCharactersTextArea.setOpaque(false);
        fontCharactersTextArea.setEditable(false);
        fontCharactersTextArea.setFont(new JLabel().getFont());

        final int borderLeft = 10;

        final int[] maxws = new int[ctable[0].length];
        for (int x = 0; x < ctable[0].length; x++) {
            int maxw = 0;
            for (int y = 0; y < ctable.length; y++) {
                Dimension d = ctable[y][x].getPreferredSize();
                if (d.width > maxw) {
                    maxw = d.width;
                }
            }
            maxws[x] = maxw;
        }

        for (int i = 0; i < ctable.length; i++) {
            fontParams2.add(ctable[i][0]);
            fontParams2.add(ctable[i][1]);
        }

        //fontParams2.setPreferredSize(new Dimension(600, ctable.length * 25));
        fontChangeList = new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                int h = 0;
                Insets is = getInsets();
                Insets is2 = fontParams2.getInsets();
                for (int i = 0; i < ctable.length; i++) {
                    Dimension d = ctable[i][0].getPreferredSize();
                    Dimension d2 = ctable[i][1].getPreferredSize();
                    ctable[i][0].setBounds(borderLeft, h, maxws[0], d2.height);

                    int w2 = getWidth() - 3 * borderLeft - maxws[0] - is.left - is.right - 10;
                    ctable[i][1].setBounds(borderLeft + maxws[0] + borderLeft, h, w2, d2.height);
                    h += Math.max(d.height, d2.height);
                }

                fontParams2.setPreferredSize(new Dimension(getWidth() - 20, h));
                revalidate();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                componentResized(null);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                componentResized(null);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                componentResized(null);
            }
        };
        final JPanel fontParams1 = new JPanel();
        addComponentListener(fontChangeList);

        fontChangeList.componentResized(null);
        fontParams1.setLayout(new BoxLayout(fontParams1, BoxLayout.Y_AXIS));
        fontParams1.add(fontParams2);

        JPanel fontAddCharsPanel = new JPanel(new FlowLayout());
        fontAddCharsPanel.add(new JLabel(translate("font.characters.add")));
        fontAddCharactersField = new MyTextField();
        fontAddCharactersField.setPreferredSize(new Dimension(150, fontAddCharactersField.getPreferredSize().height));
        fontAddCharsPanel.add(fontAddCharactersField);
        JButton fontAddCharsButton = new JButton(translate("button.ok"));
        fontAddCharsButton.setActionCommand(ACTION_FONT_ADD_CHARS);
        fontAddCharsButton.addActionListener(this);
        fontAddCharsPanel.add(fontAddCharsButton);
        
        updateTextsCheckBox = new JCheckBox(translate("font.updateTexts"));
        fontAddCharsPanel.add(updateTextsCheckBox);

        JButton fontEmbedButton = new JButton(translate("button.font.embed"));
        fontEmbedButton.setActionCommand(ACTION_FONT_EMBED);
        fontEmbedButton.addActionListener(this);
        //fontAddCharsPanel.add(fontEmbedButton);

        fontParams1.add(fontAddCharsPanel);
        JPanel fontSelectionPanel = new JPanel(new FlowLayout());
        fontSelectionPanel.add(new JLabel(translate("font.source")));
        fontSelection = new JComboBox<>(FontTag.fontNamesArray);
        fontSelection.setSelectedItem(FontTag.defaultFontName);
        fontSelection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (mainPanel.oldTag instanceof FontTag) {
                    FontTag f = (FontTag) mainPanel.oldTag;
                    SWF swf = f.getSwf();
                    String selectedSystemFont = (String) fontSelection.getSelectedItem();
                    swf.sourceFontsMap.put(f.getFontId(), selectedSystemFont);
                    Configuration.addFontPair(swf.getShortFileName(), f.getFontId(), f.getFontName(), selectedSystemFont);
                }
            }
        });
        fontSelectionPanel.add(fontSelection);

        JPanel fontCharPanel = new JPanel();
        fontCharPanel.setLayout(new ListLayout());
        fontCharPanel.add(fontAddCharsPanel);
        fontCharPanel.add(fontSelectionPanel);
        fontParams1.add(fontCharPanel);
        fontParams1.add(fontEmbedButton);
        setLayout(new BorderLayout());
        fontParams1.add(Box.createVerticalGlue());
        add(new JScrollPane(fontParams1), BorderLayout.CENTER);
    }
    
    public String getSelectedFont() {
        return fontSelection.getSelectedItem().toString();
    }
    
    private String translate(String key) {
        return mainPanel.translate(key);
    }

    private void fontAddChars(FontTag ft, Set<Integer> selChars, String selFont) {
        FontTag f = (FontTag) mainPanel.oldTag;
        SWF swf = ft.getSwf();
        String oldchars = f.getCharacters(swf.tags);
        for (int ic : selChars) {
            char c = (char) ic;
            if (oldchars.indexOf((int) c) == -1) {
                Font font = new Font(selFont, f.getFontStyle(), 1024);
                if (!font.canDisplay(c)) {
                    View.showMessageDialog(null, translate("error.font.nocharacter").replace("%char%", "" + c), translate("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        String[] yesno = new String[]{translate("button.yes"), translate("button.no"), translate("button.yes.all"), translate("button.no.all")};
        boolean yestoall = false;
        boolean notoall = false;
        for (int ic : selChars) {
            char c = (char) ic;
            if (oldchars.indexOf((int) c) > -1) {
                int opt; //yes
                if (!(yestoall || notoall)) {
                    opt = View.showOptionDialog(null, translate("message.font.add.exists").replace("%char%", "" + c), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, yesno, translate("button.yes"));
                    if (opt == 2) {
                        yestoall = true;
                    }
                    if (opt == 3) {
                        notoall = true;
                    }
                }
                if (yestoall) {
                    opt = 0; //yes                
                } else if (notoall) {
                    opt = 1; //no
                } else {
                    opt = 1;
                }

                if (opt == 1) {
                    continue;
                }
            }
            f.addCharacter(swf.tags, c, fontSelection.getSelectedItem().toString());
            oldchars += c;
        }
        
        int fontId = ft.getFontId();
        if (updateTextsCheckBox.isSelected()) {
            for (Tag tag : swf.tags) {
                if (tag instanceof TextTag) {
                    TextTag textTag = (TextTag) tag;
                    if (textTag.getFontIds(swf.tags).contains(fontId)) {
                        String text = textTag.getFormattedText(textTag.getSwf().tags);
                        mainPanel.saveText(textTag, text);
                    }
                }
            }
        }
    }

    public void showFontTag(FontTag ft) {
        SWF swf = ft.getSwf();
        DefineFontNameTag fontNameTag = null;
        for (Tag tag : swf.tags) {
            if (tag instanceof DefineFontNameTag) {
                DefineFontNameTag dfnt = (DefineFontNameTag) tag;
                if (dfnt.fontId == ft.getFontId()) {
                    fontNameTag = dfnt;
                }
            }
        }

        fontNameLabel.setText(ft.getFontName());
        if (fontNameTag != null) {
            fontDisplayNameLabel.setText(fontNameTag.fontName);
            fontCopyrightLabel.setText(fontNameTag.fontCopyright);
        }
        
        fontIsBoldLabel.setText(ft.isBold() ? translate("yes") : translate("no"));
        fontIsItalicLabel.setText(ft.isItalic() ? translate("yes") : translate("no"));
        fontDescentLabel.setText(ft.getDescent() == -1 ? translate("value.unknown") : "" + ft.getDescent());
        fontAscentLabel.setText(ft.getAscent() == -1 ? translate("value.unknown") : "" + ft.getAscent());
        fontLeadingLabel.setText(ft.getLeading() == -1 ? translate("value.unknown") : "" + ft.getLeading());
        String chars = ft.getCharacters(swf.tags);
        fontCharactersTextArea.setText(chars);
        String key = swf.getShortFileName() + "_" + ft.getFontId() + "_" + ft.getFontName();
        if (swf.sourceFontsMap.containsKey(ft.getFontId())) {
            fontSelection.setSelectedItem(swf.sourceFontsMap.get(ft.getFontId()));
        } else if (Configuration.getFontPairs().containsKey(key)) {
            fontSelection.setSelectedItem(Configuration.getFontPairs().get(key));
        } else if (Configuration.getFontPairs().containsKey(ft.getFontName())) {
            fontSelection.setSelectedItem(Configuration.getFontPairs().get(ft.getFontName()));
        } else {
            fontSelection.setSelectedItem(FontTag.findInstalledFontName(ft.getFontName()));
        }
        fontChangeList.componentResized(null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_FONT_EMBED:
                if (mainPanel.oldTag instanceof FontTag) {
                    FontEmbedDialog fed = new FontEmbedDialog(fontSelection.getSelectedItem().toString(), fontAddCharactersField.getText(), ((FontTag) mainPanel.oldTag).getFontStyle());
                    if (fed.display()) {
                        Set<Integer> selChars = fed.getSelectedChars();
                        if (!selChars.isEmpty()) {
                            String selFont = fed.getSelectedFont();
                            fontSelection.setSelectedItem(selFont);
                            fontAddChars((FontTag) mainPanel.oldTag, selChars, selFont);
                            fontAddCharactersField.setText("");
                            mainPanel.reload(true);
                        }
                    }
                }
                break;
            case ACTION_FONT_ADD_CHARS:
                String newchars = fontAddCharactersField.getText();
                if (mainPanel.oldTag instanceof FontTag) {
                    Set<Integer> selChars = new TreeSet<>();
                    for (int c = 0; c < newchars.length(); c++) {
                        selChars.add(newchars.codePointAt(c));
                    }
                    fontAddChars((FontTag) mainPanel.oldTag, selChars, fontSelection.getSelectedItem().toString());
                    fontAddCharactersField.setText("");
                    mainPanel.reload(true);
                }
                break;
        }
    }

}
