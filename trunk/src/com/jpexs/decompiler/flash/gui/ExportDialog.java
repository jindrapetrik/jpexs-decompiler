package com.jpexs.decompiler.flash.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 *
 * @author JPEXS
 */
public class ExportDialog extends AppDialog {

    boolean cancelled = false;
    String options[][] = {
        {translate("shapes.svg")},
        {translate("texts.plain"), translate("texts.formatted")},
        {translate("images.pngjpeg")},
        {translate("movies.flv")},
        {translate("sounds.mp3wavflv"), translate("sounds.flv")},
        {translate("actionscript.as"), translate("actionscript.pcode")}
    };
    String optionNames[] = {
        translate("shapes"),
        translate("texts"),
        translate("images"),
        translate("movies"),
        translate("sounds"),
        translate("actionscript")
    };
    public static final int OPTION_SHAPES = 0;
    public static final int OPTION_TEXTS = 1;
    public static final int OPTION_IMAGES = 2;
    public static final int OPTION_MOVIES = 3;
    public static final int OPTION_SOUNDS = 4;
    public static final int OPTION_ACTIONSCRIPT = 5;
    private JComboBox combos[];

    public int getOption(int index) {
        return combos[index].getSelectedIndex();
    }

    public ExportDialog() {
        setTitle(translate("dialog.title"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelled = true;
            }
        });

        setLayout(null);

        Container cnt = getContentPane();
        combos = new JComboBox[optionNames.length];
        int top = 10;
        for (int i = 0; i < optionNames.length; i++) {
            JLabel lab = new JLabel(optionNames[i]);
            lab.setBounds(10, top, 75, 25);
            cnt.add(lab);
            combos[i] = new JComboBox<>(options[i]);
            combos[i].setBounds(90, top, 125, 25);
            cnt.add(combos[i]);
            top += 25;
        }
        top += 10;


        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        okButton.setBounds(43, top, 75, 25);

        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
                setVisible(false);
            }
        });
        cancelButton.setBounds(118, top, 75, 25);

        top += 25;
        cnt.add(okButton);
        cnt.add(cancelButton);

        top += 15;
        pack();
        setSize(245, top + getInsets().top);
        View.centerScreen(this);
        View.setWindowIcon(this);
        getRootPane().setDefaultButton(okButton);
        setModal(true);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            cancelled = false;
        }
        super.setVisible(b);
    }
}
