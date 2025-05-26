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

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.pushingpixels.substance.internal.ui.SubstanceCheckBoxUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;

/**
 *
 * @author JPEXS
 */
public class FocusablePanel extends JPanel {

    private final List<ActionListener> listeners = new ArrayList<>();

    private static final Set<Integer> pressedModifiers = new HashSet<>();

    private static void setupGlobalKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                pressedModifiers.add(e.getKeyCode());
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                pressedModifiers.remove(e.getKeyCode());
            }
            return false;
        });
    }

    private static int getCurrentModifiers() {
        int mods = 0;
        if (pressedModifiers.contains(KeyEvent.VK_CONTROL)) {
            mods |= InputEvent.CTRL_DOWN_MASK;
        }
        if (pressedModifiers.contains(KeyEvent.VK_SHIFT)) {
            mods |= InputEvent.SHIFT_DOWN_MASK;
        }
        if (pressedModifiers.contains(KeyEvent.VK_ALT)) {
            mods |= InputEvent.ALT_DOWN_MASK;
        }
        return mods;
    }

    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(l);
    }

    public FocusablePanel() {
        setupGlobalKeyListener();
        setFocusable(true);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "activate");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "activate");

        getActionMap().put("activate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireAction(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                fireAction(null);
            }
        });
    }

    private void fireAction(ActionEvent e) {
        if (e == null) {
            e = new ActionEvent(FocusablePanel.this, ActionEvent.ACTION_PERFORMED, "CLICK", getCurrentModifiers());
        }
        for (ActionListener l : listeners) {
            l.actionPerformed(e);
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);

        if (Configuration.useRibbonInterface.get() && isFocusOwner()) {
            SubstanceCoreUtilities.paintFocus(
                    g,
                    this,
                    this,
                    (new SubstanceCheckBoxUI(new JCheckBox())),
                    null,
                    new Rectangle(),
                    1.0f,
                    SubstanceSizeUtils.getFocusRingPadding(SubstanceSizeUtils.getComponentFontSize(this))
            );
        }
    }

}
