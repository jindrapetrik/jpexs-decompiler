package com.jpexs.asdec.gui;

import com.jpexs.asdec.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Frame with selection on application startServer
 *
 * @author JPEXS
 */
public class ModeFrame extends JFrame implements ActionListener {
    private JButton openButton = new JButton("Open local file");
    private JButton proxyButton = new JButton("Open via proxy");
    private JButton exitButton = new JButton("Exit application");

    /**
     * Constructor
     */
    public ModeFrame() {
        setSize(300, 200);
        openButton.addActionListener(this);
        openButton.setActionCommand("OPEN");
        openButton.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/open24.png")));
        proxyButton.addActionListener(this);
        proxyButton.setActionCommand("PROXY");
        proxyButton.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/proxy24.png")));
        exitButton.addActionListener(this);
        exitButton.setActionCommand("EXIT");
        exitButton.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/exit24.png")));
        setResizable(false);
        Container cont = getContentPane();
        cont.setLayout(new GridLayout(4, 1));
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(new ImageIcon(View.loadImage("com/jpexs/asdec/gui/graphics/logo.png")));
        cont.add(logoLabel);
        cont.add(openButton);
        cont.add(proxyButton);
        cont.add(exitButton);
        View.centerScreen(this);
        View.setWindowIcon(this);
        setTitle(Main.applicationName);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Main.exit();
            }
        });
    }

    /**
     * Method handling actions from buttons
     *
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OPEN")) {
            setVisible(false);
            if (!Main.openFileDialog()) setVisible(true);
        }
        if (e.getActionCommand().equals("PROXY")) {
            setVisible(false);
            Main.showProxy();
        }
        if (e.getActionCommand().equals("EXIT")) {
            setVisible(false);
            Main.exit();
        }
    }
}
