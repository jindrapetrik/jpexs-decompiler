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
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.gui.hexview.HexView;
import com.jpexs.decompiler.flash.gui.hexview.HexViewListener;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class DumpViewPanel extends JPanel {

    private final JLabel selectedByteInfo;
    private final JLabel dumpViewLabel;
    private final HexView dumpViewHexTable;
    private final DumpTree dumpTree;
    private DumpInfo selectedDumpInfo;

    public DumpViewPanel(final DumpTree dumpTree) {
        super(new BorderLayout());

        this.dumpTree = dumpTree;
        
        selectedByteInfo = new JLabel();
        selectedByteInfo.setMinimumSize(new Dimension(100, 20));
        selectedByteInfo.setText("-");
        add(selectedByteInfo, BorderLayout.NORTH);

        dumpViewLabel = new JLabel();
        dumpViewLabel.setMinimumSize(new Dimension(100, 20));
        add(dumpViewLabel, BorderLayout.SOUTH);

        dumpViewHexTable = new HexView();
        dumpViewHexTable.addListener(new HexViewListener() {

            @Override
            public void byteMouseClicked(int address, byte b) {
                TreeModel model = dumpTree.getModel();
                DumpInfo di = DumpInfoSwfNode.getSwfNode(selectedDumpInfo);
                while (model.getChildCount(di) > 0) {
                    boolean found = false;
                    for (DumpInfo child : di.getChildInfos()) {
                        if (child.startByte > address) {
                            break;
                        }
                        if (child.getEndByte() >= address) {
                            di = child;
                            found = true;
                        }
                    }
                    if (!found) {
                        break;
                    }
                }
                List<Object> path = new ArrayList<>();
                while (di != null) {
                    path.add(0, di);
                    di = di.parent;
                }
                path.add(0, model.getRoot());
                TreePath tp = new TreePath(path.toArray());
                dumpTree.setSelectionPath(tp);
                dumpTree.scrollPathToVisible(tp);
            }

            @Override
            public void byteMouseMoved(int address, byte b) {
                selectedByteInfo.setText("Addr: " + Helper.padZeros(address, 8) + 
                        " Hex: " + Helper.padZeros(Integer.toHexString(b), 2) + 
                        " Dec: " + b + 
                        " Bin: " + Helper.padZeros(Integer.toBinaryString(b), 8) + 
                        " Ascii: " + (char) b);
            }
        });
        
        add(new JScrollPane(dumpViewHexTable), BorderLayout.CENTER);
    }

    public void setSelectedNode(DumpInfo dumpInfo) {
        if (this.selectedDumpInfo == dumpInfo) {
            return;
        }
        
        this.selectedDumpInfo = dumpInfo;
        byte[] data = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf().originalUncompressedData;
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
            highlightEnds[i] = di2.getEndByte();
        }
        dumpViewHexTable.setData(data, highlightStarts, highlightEnds);

        if (dumpInfo.lengthBytes != 0 || dumpInfo.lengthBits != 0) {
            int selectionStart = (int) dumpInfo.startByte;
            int selectionEnd = (int) dumpInfo.getEndByte();

            dumpViewHexTable.scrollToByte(highlightStarts, highlightEnds);

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
