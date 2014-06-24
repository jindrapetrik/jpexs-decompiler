/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.gui.dumpview;

import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.gui.HexView;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class DumpViewPanel extends JPanel {

    private final JLabel dumpViewLabel;
    private final HexView dumpViewHexTable;

    public DumpViewPanel() {
        super(new BorderLayout());

        dumpViewLabel = new JLabel();
        dumpViewLabel.setMinimumSize(new Dimension(100, 20));
        add(dumpViewLabel, BorderLayout.SOUTH);

        dumpViewHexTable = new HexView();
        add(new JScrollPane(dumpViewHexTable), BorderLayout.CENTER);
    }

    private int getEndIndex(DumpInfo dumpInfo) {
        int end = (int) dumpInfo.startByte;
        if (dumpInfo.lengthBytes != 0) {
            end += dumpInfo.lengthBytes;
        } else {
            int bits = dumpInfo.startBit + dumpInfo.lengthBits;
            end += bits / 8;
            if (bits % 8 != 0) {
                end++;
            }
        }
        return end - 1;
    }
    
    public void setData(byte[] data, DumpInfo dumpInfo) {
        List<DumpInfo> dumpInfos = new ArrayList<>();
        DumpInfo di = dumpInfo;
        while (di.parent != null) {
            dumpInfos.add(di);
            di = di.parent;
        }
        long[] highlightStarts = new long[dumpInfos.size()];
        long[] highlightEnds = new long[dumpInfos.size()];
        for (int i = 0; i < dumpInfos.size(); i++) {
            DumpInfo di2 = dumpInfos.get(highlightStarts.length - i - 1);
            highlightStarts[i] = di2.startByte;
            highlightEnds[i] = getEndIndex(di2);
        }
        dumpViewHexTable.setData(data, highlightStarts, highlightEnds);

        if (dumpInfo.lengthBytes != 0 || dumpInfo.lengthBits != 0) {
            int selectionStart = (int) dumpInfo.startByte;
            int selectionEnd = getEndIndex(dumpInfo);

            dumpViewHexTable.scrollToByte(dumpInfo.startByte);
            
            setLabelText("startByte: " + dumpInfo.startByte
                    + " startBit: " + dumpInfo.startBit
                    + " lengthBytes: " + dumpInfo.lengthBytes
                    + " lengthBits: " + dumpInfo.lengthBits
                    + " selectionStart: " + selectionStart
                    + " selectionEnd: " + selectionEnd);
        }
        
        repaint();
    }

    public void setLabelText(String text) {
        dumpViewLabel.setText(text);
    }
}
