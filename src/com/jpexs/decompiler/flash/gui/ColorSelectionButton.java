/*
 *  Copyright (C) 2025 JPEXS
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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public class ColorSelectionButton extends FocusablePanel {
    private JPanel colorPanel;
    private final JLabel colorLabel;

    public ColorSelectionButton(Color value, String description) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        colorPanel = new JPanel();
        setToolTipText(description);
        colorPanel.setSize(16, 16);
        colorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        colorLabel = new JLabel();
        colorPanel.setBackground(value);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                colorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }   

            @Override
            public void mouseReleased(MouseEvent e) {
                colorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            }                                

            @Override
            public void mouseClicked(MouseEvent e) {
                
            }                                                                
        });
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = ViewMessages.showColorDialog(colorPanel, colorPanel.getBackground(), false);
                if (newColor != null) {
                    colorPanel.setBackground(newColor);
                }
            }            
        });
        colorLabel.setText(colorToHex(value));
        
        add(colorPanel);
        add(colorLabel);
    }
    
    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public Color getValue() {
        return colorPanel.getBackground();
    }
    
    public void setValue(Color color) {
        String colorName = ColorNames.getNameOfColor(color);
        String text = colorToHex(color);
        if (colorName != null) {
            text += " (" + colorName + ")";
        }
        colorLabel.setText(text);
        colorPanel.setBackground(color);
    }
    
}
