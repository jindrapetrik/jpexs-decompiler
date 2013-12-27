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
import com.jpexs.decompiler.flash.tags.base.FontTag;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

    private MainFramePanel mainFramePanel;

    public Map<Integer, String> sourceFontsMap = new HashMap<>();

    private JLabel fontNameLabel;
    private JLabel fontIsBoldLabel;
    private JLabel fontIsItalicLabel;
    private JLabel fontAscentLabel;
    private JLabel fontDescentLabel;
    private JLabel fontLeadingLabel;
    private JTextArea fontCharactersTextArea;
    private JTextField fontAddCharactersField;
    private ComponentListener fontChangeList;
    private JComboBox<String> fontSelection;

    public FontPanel(MainFramePanel mainFramePanel) {
        this.mainFramePanel = mainFramePanel;
        createFontPanel();
    }
    
    private JPanel createFontPanel() {
        //TODO: This layout SUCKS! If you know something better, please fix it!
        final JPanel fontPanel = new JPanel();
        final JPanel fontParams2 = new JPanel();
        fontParams2.setLayout(null);
        final Component[][] ctable = new Component[][]{
            {new JLabel(translate("font.name")), fontNameLabel = new JLabel(translate("value.unknown"))},
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
                Insets is = fontPanel.getInsets();
                Insets is2 = fontParams2.getInsets();
                for (int i = 0; i < ctable.length; i++) {
                    Dimension d = ctable[i][0].getPreferredSize();
                    Dimension d2 = ctable[i][1].getPreferredSize();
                    ctable[i][0].setBounds(borderLeft, h, maxws[0], d2.height);

                    int w2 = fontPanel.getWidth() - 3 * borderLeft - maxws[0] - is.left - is.right - 10;
                    ctable[i][1].setBounds(borderLeft + maxws[0] + borderLeft, h, w2, d2.height);
                    h += Math.max(d.height, d2.height);
                }

                fontParams2.setPreferredSize(new Dimension(fontPanel.getWidth() - 20, h));
                fontPanel.revalidate();
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
        fontPanel.addComponentListener(fontChangeList);

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

        JButton fontEmbedButton = new JButton(translate("button.font.embed"));
        fontEmbedButton.setActionCommand(ACTION_FONT_EMBED);
        fontEmbedButton.addActionListener(this);
        //fontAddCharsPanel.add(fontEmbedButton);

        fontParams1.add(fontAddCharsPanel);
        JPanel fontSelectionPanel = new JPanel(new FlowLayout());
        fontSelectionPanel.add(new JLabel(translate("font.source")));
        fontSelection = new JComboBox<>(FontTag.fontNames.toArray(new String[FontTag.fontNames.size()]));
        fontSelection.setSelectedIndex(0);
        fontSelection.setSelectedItem("Times New Roman");
        fontSelection.setSelectedItem("Arial");
        fontSelection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (mainFramePanel.oldValue instanceof FontTag) {
                    FontTag f = (FontTag) mainFramePanel.oldValue;
                    sourceFontsMap.put(f.getFontId(), (String) fontSelection.getSelectedItem());
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
        fontPanel.setLayout(new BorderLayout());
        fontParams1.add(Box.createVerticalGlue());
        fontPanel.add(new JScrollPane(fontParams1), BorderLayout.CENTER);
        
        return fontPanel;
    }
    
    public String getSelectedFont() {
        return fontSelection.getSelectedItem().toString();
    }
    
    private String translate(String key) {
        return mainFramePanel.translate(key);
    }

    private void fontAddChars(FontTag ft, Set<Integer> selChars, String selFont) {
        FontTag f = (FontTag) mainFramePanel.oldValue;
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
    }

    public void showFontTag(FontTag ft) {
        SWF swf = ft.getSwf();
        fontNameLabel.setText(ft.getFontName(swf.tags));
        fontIsBoldLabel.setText(ft.isBold() ? translate("yes") : translate("no"));
        fontIsItalicLabel.setText(ft.isItalic() ? translate("yes") : translate("no"));
        fontDescentLabel.setText(ft.getDescent() == -1 ? translate("value.unknown") : "" + ft.getDescent());
        fontAscentLabel.setText(ft.getAscent() == -1 ? translate("value.unknown") : "" + ft.getAscent());
        fontLeadingLabel.setText(ft.getLeading() == -1 ? translate("value.unknown") : "" + ft.getLeading());
        String chars = ft.getCharacters(swf.tags);
        fontCharactersTextArea.setText(chars);
        if (sourceFontsMap.containsKey(ft.getFontId())) {
            fontSelection.setSelectedItem(sourceFontsMap.get(ft.getFontId()));
        } else {
            fontSelection.setSelectedItem(FontTag.findInstalledFontName(ft.getFontName(swf.tags)));
        }
        fontChangeList.componentResized(null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_FONT_EMBED:
                if (mainFramePanel.oldValue instanceof FontTag) {
                    FontEmbedDialog fed = new FontEmbedDialog(fontSelection.getSelectedItem().toString(), fontAddCharactersField.getText(), ((FontTag) mainFramePanel.oldValue).getFontStyle());
                    if (fed.display()) {
                        Set<Integer> selChars = fed.getSelectedChars();
                        if (!selChars.isEmpty()) {
                            String selFont = fed.getSelectedFont();
                            fontSelection.setSelectedItem(selFont);
                            fontAddChars((FontTag) mainFramePanel.oldValue, selChars, selFont);
                            fontAddCharactersField.setText("");
                            mainFramePanel.reload(true);
                        }
                    }
                }
                break;
            case ACTION_FONT_ADD_CHARS:
                String newchars = fontAddCharactersField.getText();
                if (mainFramePanel.oldValue instanceof FontTag) {
                    Set<Integer> selChars = new TreeSet<>();
                    for (int c = 0; c < newchars.length(); c++) {
                        selChars.add(newchars.codePointAt(c));
                    }
                    fontAddChars((FontTag) mainFramePanel.oldValue, selChars, fontSelection.getSelectedItem().toString());
                    fontAddCharactersField.setText("");
                    mainFramePanel.reload(true);
                }
                break;
        }
    }

}
