/*
 *  Copyright (C) 2010-2025 JPEXS
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
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * @author JPEXS
 */
public final class BinaryPanel extends JPanel {

    public HexView hexEditor = new HexView();

    private byte[] data;

    private JPanel swfOrPackedDataInsidePanel;

    private BinaryDataInterface binaryData = null;

    private final MainPanel mainPanel;

    private final JLabel swfOrPackedDataInsideLabel;

    public BinaryPanel(final MainPanel mainPanel) {
        super(new BorderLayout());
        this.mainPanel = mainPanel;

        add(new FasterScrollPane(hexEditor), BorderLayout.CENTER);

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
        swfOrPackedDataInsideLabel = new JLabel(AppStrings.translate("binarydata.swfInside"));

        swfOrPackedDataInsidePanel = new JPanel();
        swfOrPackedDataInsidePanel.setBackground(new Color(253, 205, 137));
        swfOrPackedDataInsidePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        swfOrPackedDataInsidePanel.add(swfOrPackedDataInsideLabel);
        swfOrPackedDataInsidePanel.setFocusable(true);
        swfOrPackedDataInsidePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        swfOrPackedDataInsidePanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (binaryData.getUsedPacker() != null) {
                    binaryData.unpack(binaryData.getUsedPacker(), binaryData.getPackerKey());
                }
                mainPanel.loadFromBinaryTag(binaryData);
                swfOrPackedDataInsidePanel.setVisible(false);
            }

        });
        add(swfOrPackedDataInsidePanel, BorderLayout.NORTH);
        swfOrPackedDataInsidePanel.setVisible(false);
    }

    public void setBinaryData(BinaryDataInterface binaryData) {
        this.binaryData = binaryData;
        data = binaryData == null ? null : binaryData.getDataBytes().getRangeData();
        if (data != null) {
            hexEditor.setData(data, null, null);
            boolean isSwfData = binaryData.isSwfData();
            if (isSwfData) {
                swfOrPackedDataInsideLabel.setText(AppStrings.translate("binarydata.swfInside"));
            } else {
                binaryData.detectPacker();
                if (binaryData.getUsedPacker() != null) {
                    swfOrPackedDataInsideLabel.setText(AppStrings.translate("binarydata.dataInside.packer").replace("%packer%", binaryData.getUsedPacker().getName()));
                }
            }
            swfOrPackedDataInsidePanel.setVisible(
                    (binaryData.getSub() == null && binaryData.getUsedPacker() != null)
                    || (isSwfData && binaryData.getInnerSwf() == null));
        } else {
            hexEditor.setData(new byte[0], null, null);
            swfOrPackedDataInsidePanel.setVisible(false);
        }

        hexEditor.revalidate();
        hexEditor.repaint();
    }
}
