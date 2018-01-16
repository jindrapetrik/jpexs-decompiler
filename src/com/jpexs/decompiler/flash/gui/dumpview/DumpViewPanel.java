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
package com.jpexs.decompiler.flash.gui.dumpview;

import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.gui.MyTextField;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.hexview.HexView;
import com.jpexs.decompiler.flash.gui.hexview.HexViewListener;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private JTextField filterField = new MyTextField("");

    private JPanel searchPanel;

    private final DumpTree dumpTree;

    private DumpInfo selectedDumpInfo;

    private boolean skipNextScroll;

    private boolean skipValueChange;

    public DumpViewPanel(final DumpTree dumpTree) {
        super(new BorderLayout());

        this.dumpTree = dumpTree;

        selectedByteInfo = new JLabel();
        selectedByteInfo.setMinimumSize(new Dimension(100, 20));
        selectedByteInfo.setText("-");
        add(selectedByteInfo, BorderLayout.NORTH);

        dumpViewLabel = new JLabel();
        dumpViewLabel.setMinimumSize(new Dimension(100, 20));
        dumpViewLabel.setText("-");
        add(dumpViewLabel, BorderLayout.SOUTH);

        dumpViewHexTable = new HexView();
        dumpViewHexTable.addListener(new HexViewListener() {
            private int lastAddressUnderCursor = -1;

            @Override
            public void byteValueChanged(int address, byte b) {
                if (skipValueChange) {
                    return;
                }

                if (address != -1) {
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
                    skipNextScroll = true;
                    dumpTree.setSelectionPath(tp);
                    dumpTree.scrollPathToVisible(tp);
                }

                byte[] data = dumpViewHexTable.getData();
                byteMouseMoved(lastAddressUnderCursor, lastAddressUnderCursor == -1 ? 0 : data[lastAddressUnderCursor]);
            }

            @Override
            public void byteMouseMoved(int address, byte b) {
                lastAddressUnderCursor = address;
                if (address == -1) {
                    address = dumpViewHexTable.getFocusedByteIdx();
                    if (address != -1) {
                        byte[] data = dumpViewHexTable.getData();
                        b = data[address];
                    }
                }

                if (address != -1) {
                    int b2 = b & 0xff;
                    selectedByteInfo.setText("Addr: " + String.format("%08X", address)
                            + " Hex: " + String.format("%02X", b)
                            + " Dec: " + b2
                            + " Bin: " + Helper.padZeros(Integer.toBinaryString(b2), 8)
                            + " Ascii: " + (char) b2
                    );
                } else {
                    selectedByteInfo.setText("-");
                }
            }
        });

        searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(filterField, BorderLayout.CENTER);
        searchPanel.add(new JLabel(View.getIcon("search16")), BorderLayout.WEST);
        JLabel closeSearchButton = new JLabel(View.getIcon("cancel16"));
        closeSearchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                closeDumpViewSearch();
            }
        });
        searchPanel.add(closeSearchButton, BorderLayout.EAST);
        searchPanel.setVisible(false);

        dumpViewHexTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == 'F') && (e.isControlDown())) {
                    searchPanel.setVisible(true);
                    filterField.requestFocusInWindow();
                }
            }
        });

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                doSearch();
            }
        });

        JPanel hexPanel = new JPanel(new BorderLayout());
        hexPanel.add(new JScrollPane(dumpViewHexTable), BorderLayout.CENTER);
        hexPanel.add(searchPanel, BorderLayout.SOUTH);
        add(hexPanel, BorderLayout.CENTER);
    }

    public void closeDumpViewSearch() {
        filterField.setText("");
        doSearch();
        searchPanel.setVisible(false);
    }

    private void doSearch() {
        filterField.setBackground(Color.white);

        String text = filterField.getText();
        if (text.length() == 0) {
            dumpViewHexTable.clearSelectedBytes();
            return;
        }

        byte[] data = dumpViewHexTable.getData();

        byte[] textBytes = Utf8Helper.getBytes(text);
        byte[] hex = getAsHex(text);
        byte[] foundArray = textBytes;

        int pos = textBytes == null ? -1 : findHex(data, textBytes, 0, data.length);
        int hexPos = hex == null ? -1 : findHex(data, hex, 0, data.length);

        if (pos == -1 || (hexPos != -1 && hexPos < pos)) {
            pos = hexPos;
            foundArray = hex;
        }

        if (pos != -1) {
            dumpViewHexTable.selectBytes(pos, foundArray.length);
        } else {
            dumpViewHexTable.clearSelectedBytes();
            filterField.setBackground(Color.red);
        }
    }

    private int findHex(byte[] data, byte[] searchData, int from, int to) {
        for (int i = from; i < to; i++) {
            if (isMatch(data, searchData, i)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isMatch(byte[] data, byte[] searchData, int pos) {
        if (pos + searchData.length > data.length) {
            return false;
        }

        for (int i = 0; i < searchData.length; i++) {
            if (data[pos + i] != searchData[i]) {
                return false;
            }
        }

        return true;
    }

    private byte[] getAsHex(String text) {
        int charCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toUpperCase(text.charAt(i));
            boolean whiteSpace = Character.isWhitespace(ch);
            if (!((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || whiteSpace)) {
                return null;
            }

            if (!whiteSpace) {
                charCount++;
            }
        }

        if (charCount % 2 == 1) {
            // hex character count should be even
            return null;
        }

        byte[] result = new byte[charCount / 2];
        int cnt = 0;
        int v0 = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toUpperCase(text.charAt(i));
            if (Character.isWhitespace(ch)) {
                continue;
            }

            int v = Integer.parseInt(Character.toString(ch), 16);

            if (cnt % 2 == 1) {
                result[cnt / 2] = (byte) (v0 * 16 + v);
            } else {
                v0 = v;
            }

            cnt++;
        }

        return result;
    }

    public void clear() {
        selectedDumpInfo = null;
    }

    public void setSelectedNode(DumpInfo dumpInfo) {
        if (this.selectedDumpInfo == dumpInfo) {
            skipNextScroll = false;
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
        dumpViewHexTable.revalidate();

        if (dumpInfo.lengthBytes != 0 || dumpInfo.lengthBits != 0) {
            int selectionStart = (int) dumpInfo.startByte;
            int selectionEnd = (int) dumpInfo.getEndByte();

            if (!skipNextScroll) {
                skipValueChange = true;
                dumpViewHexTable.scrollToByte(highlightStarts, highlightEnds);
                skipValueChange = false;
            }

            setLabelText("startByte: " + dumpInfo.startByte
                    + " startBit: " + dumpInfo.startBit
                    + " lengthBytes: " + dumpInfo.lengthBytes
                    + " lengthBits: " + dumpInfo.lengthBits
                    + " selectionStart: " + selectionStart
                    + " selectionEnd: " + selectionEnd);
        }

        skipNextScroll = false;
        repaint();
    }

    public void setLabelText(String text) {
        dumpViewLabel.setText(text);
    }
}
