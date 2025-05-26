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
package com.jpexs.decompiler.flash.easygui.properties.panels;

import com.jpexs.decompiler.flash.easygui.EasyStrings;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractPropertiesPanel extends JPanel {

    protected String titleIdentifier;

    private final Map<String, JPanel> cardContents = new LinkedHashMap<>();
    private final Map<String, JLabel> cardPlusMinusLabels = new LinkedHashMap<>();

    private static final char PLUS_CHAR = '\u2BC8';
    private static final char MINUS_CHAR = '\u2BC6';
    
    private JPanel verticalFiller = new JPanel();

    public AbstractPropertiesPanel(String titleIdentifier) {
        this.titleIdentifier = titleIdentifier;
    }

    protected String formatPropertyName(String id) {
        String item = EasyStrings.translate("property." + titleIdentifier + "." + id);
        return EasyStrings.translate("property.label").replace("%item%", item);
    }

    protected void addCard(JPanel cardPanel, String id, String icon, JPanel contents, GridBagConstraints gbc, boolean last) {
        //JPanel cardPanel = new JPanel();

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(EasyStrings.translate("properties." + titleIdentifier + ".header." + id));
        if (icon != null) {
            label.setIcon(View.getIcon(icon));
        }
        label.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(label, BorderLayout.CENTER);
        JLabel plusMinusLabel = new JLabel("" + PLUS_CHAR);
        plusMinusLabel.setFont(plusMinusLabel.getFont().deriveFont(plusMinusLabel.getFont().getSize2D() * 1.4f));
        plusMinusLabel.setHorizontalAlignment(JLabel.CENTER);
        plusMinusLabel.setPreferredSize(new Dimension(25, 20));
        headerPanel.add(plusMinusLabel, BorderLayout.WEST);
        headerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        headerPanel.setMinimumSize(new Dimension(0, 30));        
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    boolean newOpened = !isCardOpened(id);
                    if (last) {
                        verticalFiller.setVisible(!newOpened);
                    }
                    setCardOpened(id, newOpened);
                    cardPanel.revalidate();
                    cardPanel.repaint();
                }
            }
        });
        //contents.setAlignmentX(Component.LEFT_ALIGNMENT);
        contents.setVisible(false);        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        cardPanel.add(headerPanel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = last ? 1 : 0;
        cardPanel.add(contents, gbc);
        contents.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        //contents.setBorder(BorderFactory.createLineBorder(Color.green, 5));
        //contents.setMaximumSize(new Dimension(getPreferredSize().width, contents.getPreferredSize().height + 10));
        //cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        //cardPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        cardContents.put(id, contents);
        cardPlusMinusLabels.put(id, plusMinusLabel);
        
        if (last) {
            gbc.gridy++;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            cardPanel.add(verticalFiller, gbc);
        }
        
        //cardPanel.setBorder(BorderFactory.createLineBorder(Color.red, 2));
        //return cardPanel;
    }

    private boolean isCardOpened(String id) {
        return cardContents.get(id).isVisible();
    }

    protected void setCardOpened(String id, boolean opened) {
        JPanel contents = cardContents.get(id);
        contents.setVisible(opened);
        contents.setMaximumSize(new Dimension(Integer.MAX_VALUE, contents.getPreferredSize().height));
        JLabel plusMinusLabel = cardPlusMinusLabels.get(id);
        if (opened) {
            plusMinusLabel.setText("" + MINUS_CHAR);
        } else {
            plusMinusLabel.setText("" + PLUS_CHAR);
        }
    }

    protected void addToGrid(GridBagLayout layout, Container parent, Component component, int x, int y) {
        addToGrid(layout, parent, component, x, y, 1, 1);
    }

    protected void addToGrid(GridBagLayout layout, Container parent, Component component, int x, int y, int w, int h) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = x;
        gbc.gridy = y;

        gbc.gridwidth = w;
        gbc.gridheight = h;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 0;

        if (x == 0 && w == 1) {
            gbc.anchor = GridBagConstraints.EAST;
        }

        parent.add(component, gbc);
    }
}
