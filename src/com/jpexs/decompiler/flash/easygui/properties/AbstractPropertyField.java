/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties;

import java.awt.AWTEvent;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractPropertyField<E> extends JPanel {

    private static final String CARD_READ = "Read";
    private static final String CARD_WRITE = "Write";

    protected JLabel readLabel;
    protected JTextField writeField;

    private final List<PropertyValidationInterface<E>> validations = new ArrayList<>();
    private final List<ChangeListener> changeListeners = new ArrayList<>();

    private AWTEventListener aeListener;

    private boolean undetermined = false;
    private boolean editing = false;

    public void addValidation(PropertyValidationInterface<E> validation) {
        validations.add(validation);
    }

    public void removeValidation(PropertyValidationInterface<E> validation) {
        validations.remove(validation);
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    private void fireChange() {
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    @SuppressWarnings("unchecked")
    public AbstractPropertyField(String text) {
        setLayout(new CardLayout());
        readLabel = new DottedUnderlineLabel(text);

        readLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        writeField = new JTextField(text);
        add(readLabel, CARD_READ);
        add(writeField, CARD_WRITE);

        readLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    ((CardLayout) AbstractPropertyField.this.getLayout()).show(AbstractPropertyField.this, CARD_WRITE);
                    writeField.requestFocus();
                    writeField.selectAll();

                    Toolkit.getDefaultToolkit().addAWTEventListener(aeListener, AWTEvent.MOUSE_EVENT_MASK);
                    editing = true;
                }
            }
        });

        writeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finishEdit();
            }
        });
        writeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    finishEdit();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    e.consume();
                    cancelEdit();
                }
            }
        });

        aeListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) event;
                    if (!SwingUtilities.isDescendingFrom(me.getComponent(), writeField)) {
                        if (me.getClickCount() > 0) {
                            finishEdit();
                        }
                    }
                }
            }
        };

    }

    protected abstract E textToValue(String text);

    protected abstract String valueToText(E value);

    private synchronized void finishEdit() {
        if (!editing) {
            return;
        }

        String textBefore = readLabel.getText();
        String textAfter = writeField.getText();

        if (textBefore.equals(textAfter)) {
            cancelEdit();
            return;
        }

        Toolkit.getDefaultToolkit().removeAWTEventListener(aeListener);

        boolean ok = true;
        E value = textToValue(textAfter);
        if (value == null) {
            ok = false;
        } else {
            for (PropertyValidationInterface<E> validation : validations) {
                if (!validation.validate(value)) {
                    ok = false;
                    break;
                }
            }
        }

        if (!ok) {
            cancelEdit();
            return;
        }
        undetermined = false;
        readLabel.setText(valueToText(value));
        ((CardLayout) AbstractPropertyField.this.getLayout()).show(AbstractPropertyField.this, CARD_READ);
        editing = false;
        fireChange();
    }

    private void cancelEdit() {
        if (!editing) {
            return;
        }
        Toolkit.getDefaultToolkit().removeAWTEventListener(aeListener);
        if (undetermined) {
            writeField.setText("");
        } else {
            writeField.setText(readLabel.getText());
        }
        ((CardLayout) AbstractPropertyField.this.getLayout()).show(AbstractPropertyField.this, CARD_READ);
        editing = false;
    }

    public E getValue() {
        if (undetermined) {
            return null;
        }
        return textToValue(writeField.getText());
    }

    public void setValue(Set<E> value) {
        setValue(value, false);
    }

    public void setValue(Set<E> value, boolean silent) {
        if (value.size() != 1) {
            setValue((E) null, silent);
        } else {
            setValue(value.iterator().next(), silent);
        }
    }

    public void setValue(E value) {
        setValue(value, false);
    }

    public void setValue(E value, boolean silent) {
        if (value == null) {
            readLabel.setText("-");
            writeField.setText("");
            undetermined = true;
            if (!silent) {
                fireChange();
            }
            return;
        }
        String text = valueToText(value);
        readLabel.setText(text);
        writeField.setText(text);
        if (!silent) {
            fireChange();
        }
    }
}
