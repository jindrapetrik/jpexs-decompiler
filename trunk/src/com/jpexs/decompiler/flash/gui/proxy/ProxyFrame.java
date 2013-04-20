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
package com.jpexs.decompiler.flash.gui.proxy;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.proxy.CatchedListener;
import com.jpexs.proxy.ReplacedListener;
import com.jpexs.proxy.Replacement;
import com.jpexs.proxy.Server;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.*;

/**
 * Frame with Proxy
 *
 * @author JPEXS
 */
public class ProxyFrame extends JFrame implements ActionListener, CatchedListener, MouseListener, ReplacedListener {

    private JList swfList;
    private SWFListModel listModel;
    private JButton switchButton = new JButton("Start proxy");
    private boolean started = false;
    private JTextField portField = new JTextField("55555");
    private JCheckBox sniffSWFCheckBox = new JCheckBox("SWF", false);
    private JCheckBox sniffOSCheckBox = new JCheckBox("OctetStream", false);
    private JCheckBox sniffJSCheckBox = new JCheckBox("JS", false);
    private JCheckBox sniffXMLCheckBox = new JCheckBox("XML", false);

    /**
     * Is server running
     *
     * @return True when running
     */
    public boolean isRunning() {
        return started;
    }

    /**
     * Sets port for the proxy
     *
     * @param port Port number
     */
    public void setPort(int port) {
        portField.setText("" + port);
    }

    /**
     * Constructor
     */
    public ProxyFrame() {

        listModel = new SWFListModel(Configuration.getReplacements());
        swfList = new JList(listModel);
        swfList.addMouseListener(this);
        swfList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        switchButton.addActionListener(this);
        switchButton.setActionCommand("SWITCHSTATE");
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(new JScrollPane(swfList), BorderLayout.CENTER);


        portField.setPreferredSize(new Dimension(60, 22));
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(new Label("Port:"));
        buttonsPanel.add(portField);
        buttonsPanel.add(switchButton);
        cnt.add(buttonsPanel, BorderLayout.NORTH);

        JPanel buttonsPanel23 = new JPanel();
        buttonsPanel23.setLayout(new BoxLayout(buttonsPanel23, BoxLayout.Y_AXIS));


        JPanel buttonsPanel2 = new JPanel();
        buttonsPanel2.setLayout(new FlowLayout());
        JButton openButton = new JButton("Open");
        openButton.setActionCommand("OPEN");
        openButton.addActionListener(this);
        buttonsPanel2.add(openButton);
        JButton clearButton = new JButton("Clear");
        clearButton.setActionCommand("CLEAR");
        clearButton.addActionListener(this);
        buttonsPanel2.add(clearButton);
        JButton renameButton = new JButton("Rename");
        renameButton.setActionCommand("RENAME");
        renameButton.addActionListener(this);
        buttonsPanel2.add(renameButton);
        JButton removeButton = new JButton("Remove");
        removeButton.setActionCommand("REMOVE");
        removeButton.addActionListener(this);
        buttonsPanel2.add(removeButton);


        JPanel buttonsPanel3 = new JPanel();
        buttonsPanel3.setLayout(new FlowLayout());
        buttonsPanel3.add(new JLabel("Sniff:"));
        buttonsPanel3.add(sniffSWFCheckBox);
        buttonsPanel3.add(sniffOSCheckBox);
        //buttonsPanel3.add(sniffJSCheckBox);
        //buttonsPanel3.add(sniffXMLCheckBox);

        buttonsPanel23.add(buttonsPanel2);
        buttonsPanel23.add(buttonsPanel3);

        cnt.add(buttonsPanel23, BorderLayout.SOUTH);
        setSize(400, 300);
        View.centerScreen(this);
        View.setWindowIcon(this);
        setTitle("Proxy");
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                Main.removeTrayIcon();
                if (Main.mainFrame != null) {
                    if (Main.mainFrame.isVisible()) {
                        return;
                    }
                }
                Main.showModeFrame();
            }

            /**
             * Invoked when a window is iconified.
             */
            @Override
            public void windowIconified(WindowEvent e) {
                setVisible(false);
            }
        });
        java.util.List<Image> images = new ArrayList<Image>();
        images.add(View.loadImage("proxy16"));
        images.add(View.loadImage("proxy32"));
        setIconImages(images);
    }

    private void open() {
        if (swfList.getSelectedIndex() > -1) {
            Replacement r = (Replacement) listModel.getElementAt(swfList.getSelectedIndex());
            Main.maskURL = r.urlPattern;
            Main.openFile(r.targetFile);
        }
    }

    /**
     * Method handling actions from buttons
     *
     * @param e event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OPEN")) {
            open();
        }
        if (e.getActionCommand().equals("RENAME")) {
            if (swfList.getSelectedIndex() > -1) {
                Replacement r = (Replacement) listModel.getElementAt(swfList.getSelectedIndex());
                String s = JOptionPane.showInputDialog("URL", r.urlPattern);
                r.urlPattern = s;
                listModel.dataChanged(swfList.getSelectedIndex());
            }
        }
        if (e.getActionCommand().equals("CLEAR")) {
            for (int i = 0; i < listModel.getSize(); i++) {
                Replacement r = (Replacement) listModel.getElementAt(i);
                File f = (new File(Main.tempFile(r.targetFile)));
                if (f.exists()) {
                    f.delete();
                }
            }
            listModel.clear();
        }
        if (e.getActionCommand().equals("REMOVE")) {
            int lastIndex = -1;
            for (int k = listModel.getSize() - 1; k >= 0; k--) {
                if (swfList.isSelectedIndex(k)) {
                    Replacement r = listModel.removeURL(k);
                    File f = (new File(r.targetFile));
                    if (f.exists()) {
                        f.delete();
                    }
                    lastIndex = k;
                }
            }
            if (lastIndex >= listModel.getSize()) {
                lastIndex--;
            }
            if (lastIndex > -1) {
                swfList.setSelectedIndex(lastIndex);
            }
        }
        if (e.getActionCommand().equals("SWITCHSTATE")) {
            Main.switchProxy();
        }
    }

    /**
     * Switch proxy state
     */
    public void switchState() {
        started = !started;
        if (started) {
            int port = 0;
            try {
                port = Integer.parseInt(portField.getText());
            } catch (NumberFormatException nfe) {
            }
            if ((port <= 0) || (port > 65535)) {
                JOptionPane.showMessageDialog(this, "Wrong format for port number.", "Error", JOptionPane.ERROR_MESSAGE);
                started = false;
                return;
            }
            java.util.List<String> catchedContentTypes = new ArrayList<String>();
            catchedContentTypes.add("application/x-shockwave-flash");
            catchedContentTypes.add("application/x-javascript");
            catchedContentTypes.add("application/javascript");
            catchedContentTypes.add("text/javascript");
            catchedContentTypes.add("application/json");
            catchedContentTypes.add("text/xml");
            catchedContentTypes.add("application/xml");
            catchedContentTypes.add("application/octet-stream");
            Server.startServer(port, Configuration.getReplacements(), catchedContentTypes, this, this);
            switchButton.setText("Stop proxy");
            portField.setEditable(false);
        } else {
            Server.stopServer();
            switchButton.setText("Start proxy");
            portField.setEditable(true);
        }
    }

    /**
     * Mouse clicked event
     *
     * @param e event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == swfList) {
            if (e.getClickCount() == 2) {
                open();
            }
        }
    }

    /**
     * Mouse pressed event
     *
     * @param e event
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Mouse released event
     *
     * @param e event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Mouse entered event
     *
     * @param e event
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Mouse exited event
     *
     * @param e event
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Method called when specified contentType is received
     *
     * @param contentType Content type
     * @param url URL of the method
     * @param data Data stream
     */
    @Override
    public void catched(String contentType, String url, InputStream data) {
        boolean swfOnly = false;
        if (contentType.contains(";")) {
            contentType = contentType.substring(0, contentType.indexOf(";"));
        }
        if ((!sniffSWFCheckBox.isSelected()) && (contentType.equals("application/x-shockwave-flash"))) {
            return;
        }
        if ((!sniffJSCheckBox.isSelected()) && (contentType.equals("application/javascript") || contentType.equals("application/x-javascript") || contentType.equals("text/javascript") || contentType.equals("application/json"))) {
            return;
        }
        if ((!sniffXMLCheckBox.isSelected()) && (contentType.equals("application/xml") || contentType.equals("text/xml"))) {
            return;
        }
        if ((!sniffOSCheckBox.isSelected()) && (contentType.equals("application/octet-stream"))) {
            return;
        }
        if (!listModel.contains(url)) {
            try {
                byte hdr[] = new byte[3];
                data.read(hdr);
                String shdr = new String(hdr);
                if ((swfOnly) && ((!shdr.equals("FWS")) && (!shdr.equals("CWS")))) {
                    return; //NOT SWF
                }

                File f = new File(Main.tempFile(url));
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(hdr);
                byte buf[] = new byte[2048];
                int count;
                while ((count = data.read(buf)) > 0) {
                    fos.write(buf, 0, count);
                }
                fos.close();
                Replacement r = new Replacement(url, Main.tempFile(url));
                r.lastAccess = Calendar.getInstance();
                listModel.addURL(r);
            } catch (IOException e) {
            }

        }
    }

    /**
     * Shows or hides this {@code Window} depending on the value of parameter
     * {@code b}.
     *
     * @param b if {@code true}, makes the {@code Window} visible, otherwise
     * hides the {@code Window}. If the {@code Window} and/or its owner are not
     * yet displayable, both are made displayable. The {@code Window} will be
     * validated prior to being made visible. If the {@code Window} is already
     * visible, this will bring the {@code Window} to the front.<p> If
     * {@code false}, hides this {@code Window}, its subcomponents, and all of
     * its owned children. The {@code Window} and its subcomponents can be made
     * visible again with a call to {@code #setVisible(true)}.
     * @see java.awt.Component#isDisplayable
     * @see java.awt.Component#setVisible
     * @see java.awt.Window#toFront
     * @see java.awt.Window#dispose
     */
    @Override
    public void setVisible(boolean b) {
        if (b == true) {
            Main.addTrayIcon();
        }
        super.setVisible(b);
    }

    @Override
    public void replaced(Replacement replacement, String url, String contentType) {
        listModel.dataChanged(listModel.indexOf(replacement));
    }
}
