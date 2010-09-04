/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConvertException;
import com.jpexs.asdec.helpers.Highlighting;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DecompiledEditorPane extends JEditorPane implements MouseListener {

    private List<Highlighting> highlights = new ArrayList<Highlighting>();
    private List<Highlighting> traitHighlights = new ArrayList<Highlighting>();
    private ABC abc;
    private int classIndex;
    public int lastTraitIndex = 0;

    private class BufferedClass {

        public String text;
        public List<Highlighting> highlights;
        public List<Highlighting> traitHighlights;

        public BufferedClass(String text, List<Highlighting> highlights, List<Highlighting> traitHighlights) {
            this.text = text;
            this.highlights = highlights;
            this.traitHighlights = traitHighlights;
        }
    }

    private HashMap<Integer, BufferedClass> bufferedClasses = new HashMap<Integer, BufferedClass>();

    public void gotoLastTrait() {
        gotoTrait(lastTraitIndex);
    }

    public void gotoTrait(int traitId) {
        for (Highlighting th : traitHighlights) {
            if (th.offset == traitId) {
                setCaretPosition(th.startPos + th.len - 1);
                setCaretPosition(th.startPos);
                break;
            }
        }
        int mi = abc.findMethodIdByTraitId(classIndex, traitId);
        int bi = abc.findBodyIndex(mi);
        if (bi == -1) {
            Main.abcMainFrame.sourceTextArea.setText("");
            return;
        }
        if (Main.abcMainFrame.sourceTextArea.bodyIndex != bi) {
            Main.abcMainFrame.sourceTextArea.setBodyIndex(bi, abc);
        }
    }

    public DecompiledEditorPane() {
        /*setFont(new Font("Courier new", Font.PLAIN, 16));
        setBackground(new Color(0, 0, 0x80));
        setForeground(Color.white);
        setCaretColor(Color.white);*/
        addMouseListener(this);
        setEditable(false);
    }

    public void setClassIndex(int index, ABC abc) {
        setText("//Please wait...");

        String hilightedCode = "";
        if (!bufferedClasses.containsKey(index)) {
            hilightedCode = abc.classToString(index, true);
            highlights = Highlighting.getInstrHighlights(hilightedCode);
            traitHighlights = Highlighting.getTraitHighlights(hilightedCode);
            hilightedCode = Highlighting.stripHilights(hilightedCode);
            bufferedClasses.put(index, new BufferedClass(hilightedCode, highlights, traitHighlights));
        } else {
            BufferedClass bc = bufferedClasses.get(index);
            hilightedCode = bc.text;
            highlights = bc.highlights;
            traitHighlights = bc.traitHighlights;
        }
        setText(hilightedCode);
        this.abc = abc;
        classIndex = index;
    }

    public void reloadClass() {
        if (bufferedClasses.containsKey(classIndex)) {
            bufferedClasses.remove(classIndex);
        }
        setClassIndex(classIndex, abc);
    }

    public void setABC(ABC abc) {
        this.abc = abc;
        bufferedClasses.clear();
        setText("");
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int pos = getCaretPosition();
        for (Highlighting th : traitHighlights) {
            if ((pos >= th.startPos) && (pos < th.startPos + th.len)) {

                int bi = abc.findBodyIndex(abc.findMethodIdByTraitId(classIndex, (int) th.offset));
                if (bi == -1) {
                    Main.abcMainFrame.sourceTextArea.setText("");
                    break;
                }
                lastTraitIndex = (int) th.offset;
                if (Main.abcMainFrame.sourceTextArea.bodyIndex != bi) {
                    /*try {
                        abc.bodies[bi].code.clearCode(abc.constants, abc.bodies[bi]);
                    } catch (ConvertException ex) {
                        Logger.getLogger(DecompiledEditorPane.class.getName()).log(Level.SEVERE, null, ex);
                    }*/
                    Main.abcMainFrame.sourceTextArea.setBodyIndex(bi, abc);
                }
                for (Highlighting h : highlights) {
                    if ((pos >= h.startPos) && (pos < h.startPos + h.len)) {
                        try {
                            Main.abcMainFrame.sourceTextArea.selectInstruction(abc.bodies[bi].code.adr2pos(h.offset));

                        } catch (ConvertException ex) {
                        }
                        break;
                    }
                }
            }
        }

    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
