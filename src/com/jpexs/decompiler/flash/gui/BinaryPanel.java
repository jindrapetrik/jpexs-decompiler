/*
 *  Copyright (C) 2010-2018 JPEXS
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

import com.jpexs.decompiler.flash.gui.hexview.HexView;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public final class BinaryPanel extends JPanel {

    public HexView hexEditor = new HexView();

    private byte[] data;

    private JPanel swfInsidePanel;

    private DefineBinaryDataTag binaryDataTag = null;

    private final MainPanel mainPanel;

    public BinaryPanel(final MainPanel mainPanel) {
        super(new BorderLayout());
        this.mainPanel = mainPanel;

        add(new JScrollPane(hexEditor), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // todo: honfika: dynamically resize the hex data
        /*addComponentListener(new ComponentAdapter() {

         @Override
         public void componentResized(ComponentEvent e) {
         setBinaryData(binaryDataTag);
         }
         });*/
        swfInsidePanel = new JPanel();
        swfInsidePanel.setBackground(new Color(253, 205, 137));
        swfInsidePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        swfInsidePanel.add(new JLabel(AppStrings.translate("binarydata.swfInside")));
        swfInsidePanel.setFocusable(true);
        swfInsidePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        swfInsidePanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                mainPanel.loadFromBinaryTag(binaryDataTag);
                swfInsidePanel.setVisible(false);
            }

        });
        add(swfInsidePanel, BorderLayout.NORTH);
        swfInsidePanel.setVisible(false);
    }

    public void setBinaryData(DefineBinaryDataTag binaryDataTag) {
        this.binaryDataTag = binaryDataTag;
        data = binaryDataTag == null ? null : binaryDataTag.binaryData.getRangeData();
        if (data != null) {
            hexEditor.setData(data, null, null);
            swfInsidePanel.setVisible(binaryDataTag.innerSwf == null && binaryDataTag.isSwfData());
        } else {
            hexEditor.setData(new byte[0], null, null);
            swfInsidePanel.setVisible(false);
        }

        hexEditor.revalidate();
        hexEditor.repaint();
    }
}
