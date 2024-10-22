/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.colordialog;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
/**
 * The standard color swatch chooser.
 * Modified Java version
 */
public class MySwatchChooserPanel extends AbstractColorChooserPanel {
    MySwatchPanel swatchPanel;
    //RecentSwatchPanel recentSwatchPanel;
    MouseListener mainSwatchListener;
    MouseListener recentSwatchListener;
    private KeyListener mainSwatchKeyListener;
    private KeyListener recentSwatchKeyListener;
    public MySwatchChooserPanel() {
        super();
        setInheritsPopupMenu(true);
    }
    
    void setSelectedColor(Color color) {
        ColorSelectionModel model = getColorSelectionModel();
        if (model != null) {
            model.setSelectedColor(color);
        }
    }
    
    int getInt(Object key, int defaultValue) {
        Object value = UIManager.get(key, getLocale());
        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException nfe) {}
        }
        return defaultValue;
    }
    
    public String getDisplayName() {
        return UIManager.getString("ColorChooser.swatchesNameText", getLocale());
    }
    /**
     * Provides a hint to the look and feel as to the
     * <code>KeyEvent.VK</code> constant that can be used as a mnemonic to
     * access the panel. A return value <= 0 indicates there is no mnemonic.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way.
     * <p>
     * This implementation looks up the value from the default
     * <code>ColorChooser.swatchesMnemonic</code>, or if it
     * isn't available (or not an <code>Integer</code>) returns -1.
     * The lookup for the default is done through the <code>UIManager</code>:
     * <code>UIManager.get("ColorChooser.swatchesMnemonic");</code>.
     *
     * @return KeyEvent.VK constant identifying the mnemonic; <= 0 for no
     *         mnemonic
     * @see #getDisplayedMnemonicIndex
     * @since 1.4
     */
    public int getMnemonic() {
        return getInt("ColorChooser.swatchesMnemonic", -1);
    }
    /**
     * Provides a hint to the look and feel as to the index of the character in
     * <code>getDisplayName</code> that should be visually identified as the
     * mnemonic. The look and feel should only use this if
     * <code>getMnemonic</code> returns a value > 0.
     * <p>
     * The return value here is a hint, it is ultimately up to the look
     * and feel to honor the return value in some meaningful way. For example,
     * a look and feel may wish to render each
     * <code>AbstractColorChooserPanel</code> in a <code>JTabbedPane</code>,
     * and further use this return value to underline a character in
     * the <code>getDisplayName</code>.
     * <p>
     * This implementation looks up the value from the default
     * <code>ColorChooser.rgbDisplayedMnemonicIndex</code>, or if it
     * isn't available (or not an <code>Integer</code>) returns -1.
     * The lookup for the default is done through the <code>UIManager</code>:
     * <code>UIManager.get("ColorChooser.swatchesDisplayedMnemonicIndex");</code>.
     *
     * @return Character index to render mnemonic for; -1 to provide no
     *                   visual identifier for this panel.
     * @see #getMnemonic
     * @since 1.4
     */
    public int getDisplayedMnemonicIndex() {
        return getInt("ColorChooser.swatchesDisplayedMnemonicIndex", -1);
    }
    public Icon getSmallDisplayIcon() {
        return null;
    }
    public Icon getLargeDisplayIcon() {
        return null;
    }
    /**
     * The background color, foreground color, and font are already set to the
     * defaults from the defaults table before this method is called.
     */
    public void installChooserPanel(JColorChooser enclosingChooser) {
        super.installChooserPanel(enclosingChooser);
    }
    protected void buildChooser() {
        String recentStr = UIManager.getString("ColorChooser.swatchesRecentText", getLocale());
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel superHolder = new JPanel(gb);
        swatchPanel =  new MainSwatchPanel();
        swatchPanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                      getDisplayName());
        swatchPanel.setInheritsPopupMenu(true);
        //recentSwatchPanel = new RecentSwatchPanel();
        /*recentSwatchPanel.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                            recentStr);*/
        mainSwatchKeyListener = new MainSwatchKeyListener();
        mainSwatchListener = new MainSwatchListener();
        swatchPanel.addMouseListener(mainSwatchListener);
        swatchPanel.addKeyListener(mainSwatchKeyListener);
        recentSwatchListener = new RecentSwatchListener();
        recentSwatchKeyListener = new RecentSwatchKeyListener();
        //recentSwatchPanel.addMouseListener(recentSwatchListener);
        //recentSwatchPanel.addKeyListener(recentSwatchKeyListener);
        JPanel mainHolder = new JPanel(new BorderLayout());
        Border border = new CompoundBorder( new LineBorder(Color.black),
                                            new LineBorder(Color.white) );
        mainHolder.setBorder(border);
        mainHolder.add(swatchPanel, BorderLayout.CENTER);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        Insets oldInsets = gbc.insets;
        gbc.insets = new Insets(0, 0, 0, 10);
        superHolder.add(mainHolder, gbc);
        gbc.insets = oldInsets;
        //recentSwatchPanel.setInheritsPopupMenu(true);
        JPanel recentHolder = new JPanel( new BorderLayout() );
        recentHolder.setBorder(border);
        recentHolder.setInheritsPopupMenu(true);
        //recentHolder.add(recentSwatchPanel, BorderLayout.CENTER);
        JLabel l = new JLabel(recentStr);
        //l.setLabelFor(recentSwatchPanel);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = 1;
        gbc.weighty = 1.0;
        //superHolder.add(l, gbc);
        gbc.weighty = 0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(0, 0, 0, 2);
        //superHolder.add(recentHolder, gbc);
        superHolder.setInheritsPopupMenu(true);
        add(superHolder);
    }
    public void uninstallChooserPanel(JColorChooser enclosingChooser) {
        super.uninstallChooserPanel(enclosingChooser);
        swatchPanel.removeMouseListener(mainSwatchListener);
        swatchPanel.removeKeyListener(mainSwatchKeyListener);
        //recentSwatchPanel.removeMouseListener(recentSwatchListener);
        //recentSwatchPanel.removeKeyListener(recentSwatchKeyListener);
        swatchPanel = null;
        //recentSwatchPanel = null;
        mainSwatchListener = null;
        mainSwatchKeyListener = null;
        recentSwatchListener = null;
        recentSwatchKeyListener = null;
        removeAll();  // strip out all the sub-components
    }
    public void updateChooser() {
    }
    private class RecentSwatchKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (KeyEvent.VK_SPACE == e.getKeyCode()) {
                /*Color color = recentSwatchPanel.getSelectedColor();
                setSelectedColor(color);*/
            }
        }
    }
    private class MainSwatchKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (KeyEvent.VK_SPACE == e.getKeyCode()) {
                Color color = swatchPanel.getSelectedColor();
                setSelectedColor(color);
                //recentSwatchPanel.setMostRecentColor(color);
            }
        }
    }
    class RecentSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                /*Color color = recentSwatchPanel.getColorForLocation(e.getX(), e.getY());
                recentSwatchPanel.setSelectedColorFromLocation(e.getX(), e.getY());
                setSelectedColor(color);
                recentSwatchPanel.requestFocusInWindow();*/
            }
        }
    }
    class MainSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                Color color = swatchPanel.getColorForLocation(e.getX(), e.getY());
                setSelectedColor(color);
                swatchPanel.setSelectedColorFromLocation(e.getX(), e.getY());
                //recentSwatchPanel.setMostRecentColor(color);
                swatchPanel.requestFocusInWindow();
            }
        }
    }
}


class MainSwatchPanel extends MySwatchPanel {
    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesSwatchSize", getLocale());
        numSwatches = new Dimension( 31, 9 );
        gap = new Dimension(1, 1);
    }
    protected void initColors() {
        int[] rawValues = initRawValues();
        int numColors = rawValues.length / 3;
        colors = new Color[numColors];
        for (int i = 0; i < numColors ; i++) {
            colors[i] = new Color( rawValues[(i*3)], rawValues[(i*3)+1], rawValues[(i*3)+2] );
        }
    }
    private int[] initRawValues() {
        int[] rawValues = {
255, 255, 255, // first row.
204, 255, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
204, 204, 255,
255, 204, 255,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 204, 204,
255, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 255, 204,
204, 204, 204,  // second row.
153, 255, 255,
153, 204, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
153, 153, 255,
204, 153, 255,
255, 153, 255,
255, 153, 204,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 153, 153,
255, 204, 153,
255, 255, 153,
204, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 153,
153, 255, 204,
204, 204, 204,  // third row
102, 255, 255,
102, 204, 255,
102, 153, 255,
102, 102, 255,
102, 102, 255,
102, 102, 255,
102, 102, 255,
102, 102, 255,
153, 102, 255,
204, 102, 255,
255, 102, 255,
255, 102, 204,
255, 102, 153,
255, 102, 102,
255, 102, 102,
255, 102, 102,
255, 102, 102,
255, 102, 102,
255, 153, 102,
255, 204, 102,
255, 255, 102,
204, 255, 102,
153, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 102,
102, 255, 153,
102, 255, 204,
153, 153, 153, // fourth row
51, 255, 255,
51, 204, 255,
51, 153, 255,
51, 102, 255,
51, 51, 255,
51, 51, 255,
51, 51, 255,
102, 51, 255,
153, 51, 255,
204, 51, 255,
255, 51, 255,
255, 51, 204,
255, 51, 153,
255, 51, 102,
255, 51, 51,
255, 51, 51,
255, 51, 51,
255, 102, 51,
255, 153, 51,
255, 204, 51,
255, 255, 51,
204, 255, 51,
153, 255, 51,
102, 255, 51,
51, 255, 51,
51, 255, 51,
51, 255, 51,
51, 255, 102,
51, 255, 153,
51, 255, 204,
153, 153, 153, // Fifth row
0, 255, 255,
0, 204, 255,
0, 153, 255,
0, 102, 255,
0, 51, 255,
0, 0, 255,
51, 0, 255,
102, 0, 255,
153, 0, 255,
204, 0, 255,
255, 0, 255,
255, 0, 204,
255, 0, 153,
255, 0, 102,
255, 0, 51,
255, 0 , 0,
255, 51, 0,
255, 102, 0,
255, 153, 0,
255, 204, 0,
255, 255, 0,
204, 255, 0,
153, 255, 0,
102, 255, 0,
51, 255, 0,
0, 255, 0,
0, 255, 51,
0, 255, 102,
0, 255, 153,
0, 255, 204,
102, 102, 102, // sixth row
0, 204, 204,
0, 204, 204,
0, 153, 204,
0, 102, 204,
0, 51, 204,
0, 0, 204,
51, 0, 204,
102, 0, 204,
153, 0, 204,
204, 0, 204,
204, 0, 204,
204, 0, 204,
204, 0, 153,
204, 0, 102,
204, 0, 51,
204, 0, 0,
204, 51, 0,
204, 102, 0,
204, 153, 0,
204, 204, 0,
204, 204, 0,
204, 204, 0,
153, 204, 0,
102, 204, 0,
51, 204, 0,
0, 204, 0,
0, 204, 51,
0, 204, 102,
0, 204, 153,
0, 204, 204,
102, 102, 102, // seventh row
0, 153, 153,
0, 153, 153,
0, 153, 153,
0, 102, 153,
0, 51, 153,
0, 0, 153,
51, 0, 153,
102, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 153,
153, 0, 102,
153, 0, 51,
153, 0, 0,
153, 51, 0,
153, 102, 0,
153, 153, 0,
153, 153, 0,
153, 153, 0,
153, 153, 0,
153, 153, 0,
102, 153, 0,
51, 153, 0,
0, 153, 0,
0, 153, 51,
0, 153, 102,
0, 153, 153,
0, 153, 153,
51, 51, 51, // eighth row
0, 102, 102,
0, 102, 102,
0, 102, 102,
0, 102, 102,
0, 51, 102,
0, 0, 102,
51, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 102,
102, 0, 51,
102, 0, 0,
102, 51, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
102, 102, 0,
51, 102, 0,
0, 102, 0,
0, 102, 51,
0, 102, 102,
0, 102, 102,
0, 102, 102,
0, 0, 0, // ninth row
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 51,
51, 0, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
51, 51, 0,
0, 51, 0,
0, 51, 51,
0, 51, 51,
0, 51, 51,
0, 51, 51,
51, 51, 51 };
        return rawValues;
    }
}
